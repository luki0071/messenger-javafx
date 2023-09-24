package com.kwasheniak;

import javafx.application.Platform;
import lombok.extern.log4j.Log4j2;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

@Log4j2
public class ClientHandler implements Runnable{

    public static ArrayList<ClientHandler> clientHandlers = new ArrayList<>();
    public static int USER_ID = 1;
    private Socket socket;
    private DataInputStream dataInputStream;
    private DataOutputStream dataOutputStream;
    private int clientUserId;

    private ServerActivityController controller;

    public ClientHandler(Socket socket, ServerActivityController controller) {
        try {
            this.socket = socket;
            this.dataInputStream = new DataInputStream(socket.getInputStream());
            this.dataOutputStream = new DataOutputStream(socket.getOutputStream());
            this.clientUserId = USER_ID++;
            this.controller = controller;
            clientHandlers.add(this);
        } catch (IOException e) {
            log.error("ClientHandler " + e);
            closeConnection();
        }
    }

    @Override
    public void run() {
        log.info("connected with client");
        while(socket.isConnected()){
            try {
                dataInputStream = new DataInputStream(socket.getInputStream());
                byte[] dataType = dataInputStream.readNBytes(dataInputStream.readInt());
                byte[] data = dataInputStream.readNBytes(dataInputStream.readInt());

                Platform.runLater(() -> controller.addMessageOnBoard(dataType, data)); //Platform.runLater allows update a GUI component from a non-GUI thread

                broadcastMessage(dataType, data);

            } catch (IOException e) {
                log.error("run: lost connection with user: " + clientUserId + " " + e);
                closeConnection();
                break;
            }
        }
    }

    public void closeConnection(){
        removeClientHandler();
        try{
            if(dataInputStream != null)
                dataInputStream.close();
            if(dataOutputStream != null)
                dataOutputStream.close();
            if(socket != null)
                socket.close();
        }catch (IOException e){
            log.error("closeConnection " + e);
        }
    }

    public void broadcastMessage(byte[] dataType, byte[] data){
        clientHandlers.forEach(clientHandler -> {
            if(clientHandler.clientUserId != clientUserId){
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

    public void removeClientHandler(){
        clientHandlers.remove(this);
    }

}
