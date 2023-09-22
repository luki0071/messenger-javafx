package com.kwasheniak;

import javafx.application.Platform;
import lombok.extern.log4j.Log4j2;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;

@Log4j2
public class ServerCore {

    private final int portNumber;
    private final ServerActivityController controller;

    private ServerSocket serverSocket;
    public ServerCore(int portNumber, ServerActivityController controller) {
        this.portNumber = portNumber;
        this.controller = controller;
    }

    public void createServer(){
        try{
            serverSocket = new ServerSocket(portNumber);
            log.info("server start");
            while(true){
                Socket socket = serverSocket.accept();

                log.info("connected with client");
                DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
                byte[] dataType = dataInputStream.readNBytes(dataInputStream.readInt());
                byte[] data = dataInputStream.readNBytes(dataInputStream.readInt());
                Platform.runLater(() -> controller.addMessageOnBoard(dataType, data)); //Platform.runLater allows update a GUI component from a non-GUI thread

                dataInputStream.close();
                socket.close();
            }
        }catch (IOException e){
            log.error(e);
        }

    }

    public void closeServer(){
        if(serverSocket != null){
            try {
                serverSocket.close();
                log.info("server closed");
            }catch (IOException e){
                log.error(e);
            }
        }
    }

}
