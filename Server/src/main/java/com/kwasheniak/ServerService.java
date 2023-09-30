package com.kwasheniak;

import com.kwasheniak.database.DatabaseService;
import lombok.extern.log4j.Log4j2;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

@Log4j2
public class ServerService {

    private final int portNumber;
    private final ServerController controller;
    private ServerSocket serverSocket;

    public ServerService(int portNumber, ServerController controller) {
        this.portNumber = portNumber;
        this.controller = controller;
        DatabaseService.establishConnection();
    }

    public void createServer() {
        try {
            serverSocket = new ServerSocket(portNumber);
            log.info("server start");
            while (!serverSocket.isClosed()) {
                Socket socket = serverSocket.accept();
                log.info("socket accepted");

                createClientConnectionThread(socket);
            }
        } catch (IOException e) {
            log.error(e);
            closeServer();
        }
    }

    public void createClientConnectionThread(Socket socket){
        ClientHandler clientHandler = new ClientHandler(socket, controller);
        if(!clientHandler.getSocket().isClosed()){
            Thread thread = new Thread(clientHandler);
            thread.start();
        }
    }

    public void closeServer() {
        if (serverSocket != null) {
            try {
                serverSocket.close();
                log.info("server closed");
            } catch (IOException e) {
                log.error(e);
            }
        }
    }

    public void closeDatabaseConnection(){
        DatabaseService.closeConnection();
    }

    public void closeAllConnection(){
        closeServer();
        closeDatabaseConnection();
    }
}
