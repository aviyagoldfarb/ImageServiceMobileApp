package com.example.hana.imageservicemobileapp;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;

public class TcpClient {
    private Socket socket;
    private OutputStream output;

    public TcpClient() {
        try {
            InetAddress serverAddr = InetAddress.getByName("10.0.2.2");
            //create a socket to make the connection with the server
            this.socket = new Socket(serverAddr, 7000);
            this.output = socket.getOutputStream();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void SendBytes(byte[] bytesToWrite) {
        try {
            this.output.write(bytesToWrite);
            output.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void CloseTcpClient() {
        try {
            this.socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
