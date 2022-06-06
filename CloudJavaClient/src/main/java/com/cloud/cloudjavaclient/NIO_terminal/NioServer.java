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
import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class NioServer {

    private ServerSocketChannel server;
    private Selector selector;
    private Path dir;

    public NioServer() throws IOException {
        server = ServerSocketChannel.open();
        selector = Selector.open();

        server.bind(new InetSocketAddress(8189));
        server.configureBlocking(false);
        server.register(selector, SelectionKey.OP_ACCEPT);
        dir = Path.of("C:/Users/Winter/Documents");

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
        Path localPath = dir;
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

        if (sb.toString().startsWith("ls")) {
            Set<String> files = listFiles(String.valueOf(localPath));
            for (String file : files) {
                sb.append(file + "\n");
            }
        }
        if (sb.toString().startsWith("cat")) {
            String s = sb.toString().replaceAll("[\r\n]+$", "");
            String[] command = s.split(" ", 2);

            Path file = Path.of(String.valueOf(localPath), command[1]);
            List<String> stringList = Files.readAllLines(file);
            for (String s1 : stringList) {
                sb.append(s1).append("\n");
            }
        }
        if (sb.toString().startsWith("cd")) {
            String str = sb.toString().replaceAll("[\r\n]+$", "");
            String[] command = str.split(" ", 2);

            localPath = Path.of(String.valueOf(dir), command[1]);



        }

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

    public Set<String> listFiles(String dir) {
        return Stream.of(new File(dir).listFiles())
                .filter(file -> !file.isDirectory())
                .map(File::getName)
                .collect(Collectors.toSet());
    }

}
