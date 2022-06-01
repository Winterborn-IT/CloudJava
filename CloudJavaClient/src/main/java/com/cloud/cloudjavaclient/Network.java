package com.cloud.cloudjavaclient;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class Network {

    private DataInputStream is;
    private DataOutputStream os;

    public Network(int port) throws IOException {
        Socket socket = new Socket("localhost", port);
        is = new DataInputStream(socket.getInputStream());
        os = new DataOutputStream(socket.getOutputStream());
    }

    public String readMsg() throws IOException {
        return is.readUTF();
    }

    public void writeMsg(String msg) throws IOException {
        os.writeUTF(msg);
        os.flush();
    }
}
