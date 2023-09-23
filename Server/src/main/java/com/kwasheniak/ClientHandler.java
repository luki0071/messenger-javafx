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
    private Socket socket;
    private DataInputStream dataInputStream;
    private DataOutputStream dataOutputStream;
    private String clientUsername;

    private ServerActivityController controller;

    public ClientHandler(Socket socket, ServerActivityController controller) {
        try {
            this.socket = socket;
            this.dataInputStream = new DataInputStream(socket.getInputStream());
            this.dataOutputStream = new DataOutputStream(socket.getOutputStream());
            clientHandlers.add(this);
            this.controller = controller;
        } catch (IOException e) {
            log.error(e);
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
                log.error(e);
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
            log.error(e);
        }
    }

    public void broadcastMessage(byte[] dataType, byte[] data){
        clientHandlers.forEach(clientHandler -> {
            try {
                dataOutputStream.writeInt(dataType.length);
                dataOutputStream.write(dataType);
                dataOutputStream.writeInt(data.length);
                dataOutputStream.write(data);
            } catch (IOException e) {
                log.error(e);
                closeConnection();
            }
            /*if(!clientHandler.socket.equals(socket)){

            }*/
        });
    }

    public void removeClientHandler(){
        clientHandlers.remove(this);
    }

}
