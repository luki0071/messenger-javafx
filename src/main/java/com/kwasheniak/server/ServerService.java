package com.kwasheniak.server;

import com.kwasheniak.database.DatabaseService;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;

@Log4j2
public class ServerService {

    private final int portNumber;
    private final ServerController controller;
    private static ServerSocket serverSocket;

    public ServerService(int portNumber, ServerController controller) {
        this.portNumber = portNumber;
        this.controller = controller;
    }

    public void createServer() {
        if (serverSocket != null && !serverSocket.isClosed()) {
            log.info("server already started");
            return;
        }
        startServerThread();
    }

    private void startServerThread() {
        Thread serverThread = new Thread(() -> {
            try {
                serverSocket = new ServerSocket(portNumber);
                log.info("server starts");
                connectToDatabase();
                listenForClients();
            } catch (IOException | SQLException e) {
                log.error("startServerThread(): " + e);
                closeAllConnection();
            }
        });
        serverThread.setName("Server Thread");
        serverThread.start();
    }

    private void listenForClients() throws IOException {
        while (serverSocket != null && !serverSocket.isClosed()) {
            Socket socket = serverSocket.accept();
            log.info("new socket accepted");
            new ClientHandler(socket, controller);
        }
    }

    public static void closeConnectionWithClients() {
        ClientHandler.closeConnectionWithAllClients();
    }

    private static void closeServer() {
        if (serverSocket != null && !serverSocket.isClosed()) {
            try {
                serverSocket.close();
                serverSocket = null;
                log.info("server closed");
            } catch (IOException e) {
                log.error("closeServer(): " + e);
            }
        }
    }

    private void connectToDatabase() throws SQLException {
        DatabaseService.establishConnection();
    }

    private static void disconnectFromDatabase() {
        try {
            DatabaseService.closeConnection();
        } catch (SQLException e) {
            log.error("closeServer(): " + e);
        }
    }

    public static void closeAllConnection() {
        closeConnectionWithClients();
        disconnectFromDatabase();
        closeServer();
    }
}
