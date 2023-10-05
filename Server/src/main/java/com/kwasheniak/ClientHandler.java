package com.kwasheniak;

import com.kwasheniak.database.DatabaseHandler;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.sql.SQLException;
import java.util.ArrayList;

@Log4j2
public class ClientHandler implements Runnable {
    //holds all clients verified in database and connected to server
    public static ArrayList<ClientHandler> clients = new ArrayList<>();

    @Getter
    private Socket socket;
    private DataInputStream dataInputStream;
    private DataOutputStream dataOutputStream;
    private String clientUsername;
    private ServerController controller;

    public ClientHandler(Socket socket, ServerController controller) {

        try {
            setConnectionWithClient(socket, controller);
            String loginOrSignUp = new String(dataInputStream.readNBytes(dataInputStream.readInt()));
            String username = new String(dataInputStream.readNBytes(dataInputStream.readInt()));
            String password = new String(dataInputStream.readNBytes(dataInputStream.readInt()));
            getDataFromClientOnFirstConnection(loginOrSignUp, username, password);
        } catch (IOException e) {
            log.error(e);
        }
    }

    public void setConnectionWithClient(Socket socket, ServerController controller) throws IOException {
        /*try {

        } catch (IOException e) {
            log.error("ClientHandler " + e);
            closeConnection();
        }*/
        this.socket = socket;
        this.controller = controller;
        this.dataInputStream = new DataInputStream(socket.getInputStream());
        this.dataOutputStream = new DataOutputStream(socket.getOutputStream());
    }

    public void getDataFromClientOnFirstConnection(String login, String username, String password) throws IOException {
        if(login.equals("login")){
            validateUserToLogIn(username, password);
            return;
        }
        if(login.equals("signup")){
            validateUserToSignUp(username, password);
        }
    }

    public void validateUserToLogIn(String username, String password) throws IOException {
        //If client's username and password are in database
        //send to client true value which means he is logged and added to clients static list
        try {
            if (DatabaseHandler.isUserInUsersTable(username, password)) {
                log.info("client exists");
                if (isUserAlreadyLogged(username)) {
                    log.info(username + " is already logged");
                    dataOutputStream.writeBoolean(false);
                    closeConnection();
                } else {
                    dataOutputStream.writeBoolean(true);
                    clientUsername = username;
                    clients.add(this);
                }
            }
            //send to client false value which means he is not logged
            //closes socket connection with server
            else {
                log.info("client not exists");
                dataOutputStream.writeBoolean(false);
                closeConnection();
            }
        } catch (SQLException e) {
            log.error("couldn't get answer from database: " + e);
        }
    }

    public void validateUserToSignUp(String username, String password) throws IOException {
        try {
            if (DatabaseHandler.isUserInUsersTable(username)) {
                log.info("username is already used by someone else");
                byte[] userInfo = "username is already used by someone else".getBytes();
                dataOutputStream.writeBoolean(false);
                dataOutputStream.writeInt(userInfo.length);
                dataOutputStream.write(userInfo);
                return;
            }
            if (DatabaseHandler.addUserToUsersTable(username, password)) {
                log.info("user successfully signed up");
                byte[] userInfo = "user successfully signed up".getBytes();
                dataOutputStream.writeBoolean(true);
                dataOutputStream.writeInt(userInfo.length);
                dataOutputStream.write(userInfo);
            }else{
                log.info("couldn't sign up");
                byte[] userInfo = "couldn't sign up".getBytes();
                dataOutputStream.writeBoolean(false);
                dataOutputStream.writeInt(userInfo.length);
                dataOutputStream.write(userInfo);
            }
        } catch (SQLException e) {
            log.error("cant sign up right now " + e);
            byte[] userInfo = "cant sign up right now".getBytes();
            dataOutputStream.writeBoolean(false);
            dataOutputStream.writeInt(userInfo.length);
            dataOutputStream.write(userInfo);
        }
    }

    public boolean isUserAlreadyLogged(String username) {
        return clients.stream().anyMatch(clientHandler -> username.equals(clientHandler.clientUsername));
    }

    @Override
    public void run() {
        log.info("connected with client");
        try {
            while (socket.isConnected()) {
                byte[] dataType = dataInputStream.readNBytes(dataInputStream.readInt());
                byte[] data = dataInputStream.readNBytes(dataInputStream.readInt());

                broadcastMessage(dataType, data);
            }
        } catch (IOException e) {
            closeConnection();
            removeClientHandler();
        }
        log.info("disconnected with client");
    }

    public void broadcastMessage(byte[] dataType, byte[] data) {
        clients.forEach(clientHandler -> {
            if (!clientUsername.equals(clientHandler.clientUsername)) {
                try {
                    clientHandler.dataOutputStream.writeInt(dataType.length);
                    clientHandler.dataOutputStream.write(dataType);
                    clientHandler.dataOutputStream.writeInt(data.length);
                    clientHandler.dataOutputStream.write(data);
                } catch (IOException e) {
                    log.error("broadcastMessage(): " + e);
                    closeConnection();
                }
            }
        });
    }

    public void closeConnection() {
        try {
            if (dataInputStream != null) {
                dataInputStream.close();
            }
            if (dataOutputStream != null) {
                dataOutputStream.close();
            }
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            log.error("closeConnection(): " + e);
        }
    }

    public static void closeConnectionWithAllClients() {
        clients.forEach(ClientHandler::closeConnection);
        clients.clear();
    }

    private void removeClientHandler() {
        clients.remove(this);
    }

}
