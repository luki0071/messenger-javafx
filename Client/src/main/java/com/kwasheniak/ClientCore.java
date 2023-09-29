package com.kwasheniak;

import javafx.application.Platform;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

import java.io.*;
import java.net.Socket;


@Log4j2
@Setter
@Getter
public class ClientCore {

    public static final String HOST = "localhost";
    public static final int PORT = 1234;

    private Socket socket;
    private DataOutputStream dataOutputStream;
    private DataInputStream dataInputStream;

    public ClientCore() {
        try {
            this.socket = new Socket(HOST, PORT);
            this.dataOutputStream = new DataOutputStream(socket.getOutputStream());
            this.dataInputStream = new DataInputStream(socket.getInputStream());
            log.info(socket.toString());
            log.info(dataOutputStream.toString());
            log.info(dataInputStream.toString());
        } catch (IOException e) {
            log.error("ClientCore " + e);
            closeConnection();
        }
    }

    public byte[][] sendMessage(String textToSend) throws IOException {
        byte[] type = new byte[0];
        byte[] bytes = textToSend.getBytes();
        sendMessageToServer(type, bytes);
        return new byte[][]{type, bytes};
    }

    public byte[][] sendMessage(File fileToSend) throws IOException {
        byte[] fileName = fileToSend.getName().getBytes();
        FileInputStream fileInputStream = new FileInputStream(fileToSend.getAbsolutePath());
        byte[] file = fileInputStream.readNBytes((int) fileToSend.length());
        fileInputStream.close();
        sendMessageToServer(fileName, file);
        return new byte[][]{fileName, file};
    }

    private void sendMessageToServer(byte[] labelBytes, byte[] dataBytes) throws IOException {
        dataOutputStream.writeInt(labelBytes.length);
        dataOutputStream.write(labelBytes);
        dataOutputStream.writeInt(dataBytes.length);
        dataOutputStream.write(dataBytes);
    }

    public void listenForMessages(ClientController controller) {
        new Thread(() -> {
            try {
                if (socket != null) {
                    while (socket.isConnected()) {
                        byte[] dataType = dataInputStream.readNBytes(dataInputStream.readInt());
                        byte[] data = dataInputStream.readNBytes(dataInputStream.readInt());
                        Platform.runLater(() -> controller.addMessageToMessageBoard(MessageStatus.RECEIVED, new byte[][]{dataType, data}));
                    }
                }
            } catch (IOException e) {
                log.error(e);
            }
        }).start();
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
