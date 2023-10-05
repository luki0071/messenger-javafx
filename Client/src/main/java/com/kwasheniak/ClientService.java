package com.kwasheniak;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;

import java.io.*;
import java.net.Socket;


@Log4j2
@Getter
public class ClientService {

    public static final String HOST = "localhost";
    public static final int PORT = 1234;

    private Socket socket;
    private DataOutputStream dataOutputStream;
    private DataInputStream dataInputStream;

    public ClientService() {
        connectToServer();
    }

    public Boolean isConnectedToServer() {
        return socket != null && !socket.isClosed();
    }

    public void connectToServer() {
        if (isConnectedToServer()) {
            log.info("client already connected to server");
            return;
        }
        try {
            this.socket = new Socket(HOST, PORT);
            this.dataOutputStream = new DataOutputStream(socket.getOutputStream());
            this.dataInputStream = new DataInputStream(socket.getInputStream());
        } catch (IOException e) {
            log.error("connectToServer() " + e);
            closeConnection();
        }
    }

    public byte[][] sendTextMessage(String textToSend) throws IOException {
        byte[] type = new byte[0];
        byte[] bytes = textToSend.getBytes();
        sendMessage(type);
        sendMessage(bytes);
        return new byte[][]{type, bytes};
    }

    public byte[][] sendFileMessage(File fileToSend) throws IOException {
        byte[] fileName = fileToSend.getName().getBytes();
        FileInputStream fileInputStream = new FileInputStream(fileToSend.getAbsolutePath());
        byte[] file = fileInputStream.readNBytes((int) fileToSend.length());
        fileInputStream.close();
        sendMessage(fileName);
        sendMessage(file);
        return new byte[][]{fileName, file};
    }

    private void sendMessage(byte[] data) throws IOException {
        dataOutputStream.writeInt(data.length);
        dataOutputStream.write(data);
    }

    public void sendLogin(String login, String username, String password) throws IOException {
        byte[] log = login.getBytes();
        byte[] user = username.getBytes();
        byte[] pass = password.getBytes();
        sendMessage(log);
        sendMessage(user);
        sendMessage(pass);
    }

    public byte[] receiveMessage() throws IOException {
        return dataInputStream.readNBytes(dataInputStream.readInt());
    }

    public void closeConnection() {
        try {
            if (dataInputStream != null)
                dataInputStream.close();
            if (dataOutputStream != null)
                dataOutputStream.close();
            if (socket != null)
                socket.close();
        } catch (IOException e) {
            log.error("closeConnection() " + e);
        }
    }
}
