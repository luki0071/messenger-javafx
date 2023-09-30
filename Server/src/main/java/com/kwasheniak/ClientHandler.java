package com.kwasheniak;

import com.kwasheniak.database.DatabaseService;
import javafx.application.Platform;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

@Log4j2
public class ClientHandler implements Runnable {

    public static ArrayList<ClientHandler> clientHandlers = new ArrayList<>();

    @Getter
    private Socket socket;
    private DataInputStream dataInputStream;
    private DataOutputStream dataOutputStream;
    private int clientPort;

    private ServerController controller;

    public ClientHandler(Socket socket, ServerController controller) {
        try {
            this.socket = socket;
            this.dataInputStream = new DataInputStream(socket.getInputStream());
            this.dataOutputStream = new DataOutputStream(socket.getOutputStream());
            this.clientPort = socket.getPort();
            this.controller = controller;
            if(isClientInDatabase()) {
                log.info("client exists");
                dataOutputStream.writeBoolean(true);
                clientHandlers.add(this);
            }
            else{
                log.info("client not exists");
                dataOutputStream.writeBoolean(false);
                closeConnection();
            }
        } catch (IOException e) {
            log.error("ClientHandler " + e);
            closeConnection();
        }
    }

    public Boolean isClientInDatabase(){
        try {
            if(DatabaseService.isConnectionAvailable()){
                String username = new String(dataInputStream.readNBytes(dataInputStream.readInt()));
                String password = new String(dataInputStream.readNBytes(dataInputStream.readInt()));
                return DatabaseService.isUserInUsersTable(username, password);
            }else{
                log.info("connection with database is unavailable");
                closeConnection();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return false;
    }

    @Override
    public void run() {
        log.info("connected with client");
        while (socket.isConnected()) {
            try {
                byte[] dataType = dataInputStream.readNBytes(dataInputStream.readInt());
                byte[] data = dataInputStream.readNBytes(dataInputStream.readInt());

                Platform.runLater(() -> controller.showMessageOnMessageBoard(dataType, data)); //Platform.runLater allows update a GUI component from a non-GUI thread

                broadcastMessage(dataType, data);

            } catch (IOException e) {
                //log.error("run: lost connection with user: " + clientPort + " " + e);
                closeConnection();
                break;
            }
        }
    }

    public void closeConnection() {
        removeClientHandler();
        try {
            if (dataInputStream != null)
                dataInputStream.close();
            if (dataOutputStream != null)
                dataOutputStream.close();
            if (socket != null)
                socket.close();
        } catch (IOException e) {
            log.error("closeConnection " + e);
        }
    }

    public void broadcastMessage(byte[] dataType, byte[] data) {
        clientHandlers.forEach(clientHandler -> {
            if (clientHandler.clientPort != clientPort) {
                try {
                    clientHandler.dataOutputStream.writeInt(dataType.length);
                    clientHandler.dataOutputStream.write(dataType);
                    clientHandler.dataOutputStream.writeInt(data.length);
                    clientHandler.dataOutputStream.write(data);
                } catch (IOException e) {
                    log.error("broadcastMessage: " + e);
                    closeConnection();
                }
            }
        });
    }

    public void removeClientHandler() {
        clientHandlers.remove(this);
    }

}
