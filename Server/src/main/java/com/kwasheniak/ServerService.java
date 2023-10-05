package com.kwasheniak;

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
    private ServerSocket serverSocket;

    public ServerService(int portNumber, ServerController controller) {
        this.portNumber = portNumber;
        this.controller = controller;
    }

    /**
     * starts server on given in constructor port number
     */
    public void createServer() {
        try {
            serverSocket = new ServerSocket(portNumber);
            log.info("server starts");
            connectToDatabase();
            listenForClients();
        } catch (IOException e) {
            log.error("create server(): " + e);
            closeAllConnection();
        }
    }

    /**
     * listening for connecting clients
     *
     * @throws IOException
     */
    public void listenForClients() throws IOException {
        //loop works until server is open
        while (serverSocket != null && !serverSocket.isClosed()) {
            //accept() holds loop until one of client connects to server
            Socket socket = serverSocket.accept();

            //when client connects to server loop is continue
            createClientConnectionThread(socket);
        }
    }

    /**
     * creates ClientHandler object which handle with connected client
     * and starts new thread for connection with client and server
     *
     * @param socket socket object of connected client
     */
    public void createClientConnectionThread(Socket socket) {
        ClientHandler clientHandler = new ClientHandler(socket, controller);
        //if connection with client is still available new thread is started
        if (!clientHandler.getSocket().isClosed()) {
            Thread thread = new Thread(clientHandler);
            thread.start();
        }
    }

    /**
     * closes connection with all clients
     */
    public void closeConnectionWithClients() {
        ClientHandler.closeConnectionWithAllClients();
    }

    /**
     * closes connection with server
     */
    public void closeServer() {
        if (serverSocket != null && !serverSocket.isClosed()) {
            try {
                serverSocket.close();
                log.info("server closed");
            } catch (IOException e) {
                log.error("closeServer(): " + e);
            }
        }
    }

    /**
     * connects to database
     */
    public void connectToDatabase() {
        try {
            DatabaseService.establishConnection();
        } catch (SQLException e) {
            log.error("closeServer(): " + e);
        }
    }

    /**
     * disconnects from database
     */
    public void disconnectFromDatabase() {
        try {
            DatabaseService.closeConnection();
        } catch (SQLException e) {
            log.error("closeServer(): " + e);
        }
    }

    /**
     * closes connection with clients, database and server
     */
    public void closeAllConnection() {
        closeConnectionWithClients();
        disconnectFromDatabase();
        closeServer();
    }
}
