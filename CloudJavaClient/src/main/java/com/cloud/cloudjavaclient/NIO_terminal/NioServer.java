package com.cloud.cloudjavaclient.NIO_terminal;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class NioServer {

    private ServerSocketChannel server;
    private Selector selector;
    private String dir;

    public NioServer() throws IOException {
        server = ServerSocketChannel.open();
        selector = Selector.open();

        server.bind(new InetSocketAddress(8189));
        server.configureBlocking(false);
        server.register(selector, SelectionKey.OP_ACCEPT);
        dir = System.getProperty("user.home");

    }

    public void start() throws IOException {

        while (server.isOpen()) {
            selector.select();
            Set<SelectionKey> keys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = keys.iterator();

            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();
                if (key.isAcceptable()) {
                    handleAccept();
                }
                if (key.isReadable()) {
                    handleRead(key);
                }
                iterator.remove();
            }
        }
    }

    public void handleRead(SelectionKey key) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        SocketChannel channel = (SocketChannel) key.channel();
        StringBuilder sb = new StringBuilder();
        String[] files = new File(dir).list();

        while (channel.isOpen()) {
            int read = channel.read(buffer);
            if (read < 0) {
                channel.close();
                return;
            }
            if (read == 0) {
                break;
            }
            buffer.flip();
            while (buffer.hasRemaining()) {
                sb.append((char) buffer.get());
            }
            buffer.clear();
        }

        // Определение команды
        String[] com = sb.toString().replaceAll("[\r\n]+$", "").split(" ", 2);
        String caseCommand = com[0];

        if (caseCommand.equals("ls")) {
            for (String file : files) {
                sb.append(file + "\n");
            }
        }

        if (caseCommand.equals("cat")) {
            String s = sb.toString().replaceAll("[\r\n]+$", "");
            String[] command = s.split(" ", 2);
            try {
                Path file = Path.of(String.valueOf(dir), command[1]);
                List<String> stringList = Files.readAllLines(file);
                for (String s1 : stringList) {
                    sb.append(s1).append("\n");
                }
            } catch (IOException e) {
                for (SelectionKey selectedKey : selector.keys()) {
                    if (selectedKey.isValid() && selectedKey.channel() instanceof SocketChannel sc) {
                        sc.write(ByteBuffer.wrap("File not found\n".getBytes(StandardCharsets.UTF_8)));
                    }
                }
                sb.setLength(0);
            }
        }
        if (caseCommand.equals("cd")) {
            String str = sb.toString().replaceAll("[\r\n]+$", "");
            String[] command = str.split(" ", 2);
            // Поиск имени директории из списка
            if (Arrays.asList(files).contains(command[1]) || command[1].equals("..")) {
                dir = String.valueOf(Path.of(dir).resolve(command[1]).normalize());
            } else {
                sb.append("Directory doesn't exist\n");
            }

        }

        sb.append(dir);
        sb.append("-> ");

        byte[] message = sb.toString().getBytes(StandardCharsets.UTF_8);

        for (SelectionKey selectedKey : selector.keys()) {
            if (selectedKey.isValid() && selectedKey.channel() instanceof SocketChannel sc) {
                sc.write(ByteBuffer.wrap(message));
            }
        }
    }

    public void handleAccept() throws IOException {
        SocketChannel channel = server.accept();
        channel.configureBlocking(false);
        channel.register(selector, SelectionKey.OP_READ);
        channel.write(ByteBuffer.wrap("Welcome in NIO terminal\n->".getBytes(StandardCharsets.UTF_8)));

    }

}
