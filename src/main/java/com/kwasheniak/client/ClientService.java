package com.kwasheniak.client;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

@Log4j2
public class ClientService {

    public static final String HOST = "localhost";
    public static final int PORT = 1234;
    @Getter
    private static Socket socket;
    @Getter
    private static ObjectOutputStream objectOutputStream;
    @Getter
    private static ObjectInputStream objectInputStream;

    public static void connectToServer() {
        if (isConnectedToServer()) {
            log.info("client already connected to server");
            return;
        }
        try {
            socket = new Socket(HOST, PORT);
            objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            objectInputStream = new ObjectInputStream(socket.getInputStream());
            log.info("client connected to server");
        } catch (IOException e) {
            closeConnection();
        }

    }

    /*private void listenFromServer() {
        Thread thread = new Thread(() -> {
            try {
                while (socket.isConnected()) {
                    log.info("waiting for server response");
                    String serverResponse = captureServerResponse();
                    if (ClientRequests.LOGIN.toString().equals(serverResponse)) {

                    }
                }
            } catch (IOException e) {
                closeConnection();
            }
            log.info("disconnected from server");
        });
        thread.setName("Server Listener Thread");
        thread.start();
    }*/

    /*private static String captureServerResponse() throws IOException {
        return new String(dataInputStream.readNBytes(dataInputStream.readInt()));
    }*/

    public static boolean isConnectedToServer() {
        return socket != null && !socket.isClosed();
    }

    public static void closeConnection() {
        try {
            if (objectInputStream != null) {
                objectInputStream.close();
                objectInputStream = null;
            }
            if (objectOutputStream != null) {
                objectOutputStream.close();
                objectOutputStream = null;
            }
            if (socket != null) {
                socket.close();
                socket = null;
            }
            log.info("client disconnected from server");
        } catch (IOException e) {
            log.error("closeConnection() " + e);
        }
    }
}
