package com.kwasheniak.server;

import com.kwasheniak.database.DatabaseUtils;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.TreeMap;

@Log4j2
public class ClientHandler {
    public static volatile ArrayList<ClientHandler> loggedClients = new ArrayList<>();

    /*private static final String LOGIN = "login";
    private static final String LOGOUT = "logout";
    private static final String SIGN_UP = "signup";
    private static final String USERNAMES_LIST = "usernames";
    private static final String START_CHAT = "startchat";*/

    @Getter
    private Socket socket;
    private ObjectInputStream objectInputStream;
    private ObjectOutputStream objectOutputStream;
    private String clientUsername;
    private ServerController controller;

    public ClientHandler(Socket socket, ServerController controller) {
        establishConnectionWithClient(socket, controller);
    }

    public static void closeConnectionWithAllClients() {
        loggedClients.forEach(ClientHandler::closeConnection);
        loggedClients.clear();
    }

    public void establishConnectionWithClient(Socket socket, ServerController controller) {
        if (socket == null) {
            log.info("socket is null");
            return;
        }
        try {
            this.socket = socket;
            this.controller = controller;
            this.objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            this.objectInputStream = new ObjectInputStream(socket.getInputStream());

            String whatClientWant = receiveRequest();
            String username = String.valueOf(receiveData());
            String password = String.valueOf(receiveData());

            if (ClientRequests.LOGIN.toString().equals(whatClientWant)) {
                loginClient(username, password);
                return;
            }
            if (ClientRequests.SIGN_UP.toString().equals(whatClientWant)) {
                signUpClient(username, password);
            }
        } catch (IOException | SQLException e) {
            log.error(e);
            closeConnection();
        }
    }

    private void startListeningClientThread() {
        Thread thread = new Thread(() -> {
            log.info("connected with client");
            try {
                while (socket.isConnected()) {
                    log.info("waiting for request");
                    String request = receiveRequest();
                    if (ClientRequests.USERNAMES_LIST.toString().equals(request)) {
                        log.info("request usernames");
                        sendUsernamesListResponse(getUsersStatus());
                        continue;
                    }
                    if (ClientRequests.START_CHAT.toString().equals(request)) {
                        log.info("request start chat");
                        /*String username = new String(readData());
                        broadcastMessagesTo(username);*/
                        continue;
                    }
                    if (ClientRequests.LOGOUT.toString().equals(request)) {
                        closeConnection();
                        removeClient();
                    }
                }
            } catch (IOException e) {
                closeConnection();
                removeClient();
            }
            log.info("disconnected with client");
        });
        thread.setName(clientUsername + " Thread");
        thread.start();
    }

    public TreeMap<String, Boolean> getUsersStatus() {
        try {
            TreeMap<String, Boolean> usersStatus = new TreeMap<>();
            String[] usernames = DatabaseUtils.getAllUsernamesExcept(clientUsername);

            Arrays.stream(usernames).forEach(username -> {
                boolean status = loggedClients.stream().anyMatch(clientHandler -> username.equals(clientHandler.clientUsername));
                usersStatus.put(username, status);
            });
            return usersStatus;

        } catch (SQLException e) {
            log.error(e);
        }
        return null;
    }

    /*public void updateClientStatus() throws IOException {
        for (ClientHandler client : clients) {
            if (!clientUsername.equals(client.clientUsername)) {
                objectOutputStream.writeInt(1);
                writeData(clientUsername.getBytes());
                writeData("online".getBytes());
            }
        }

        try {
            String[] usernames = DatabaseUtils.getAllUsernamesExcept(clientUsername);
            objectOutputStream.writeInt(usernames.length);
            for (String username : usernames) {
                writeData(username.getBytes());
                if(clients.stream().anyMatch(clientHandler -> username.equals(clientHandler.clientUsername))){
                    writeData("online".getBytes());
                }else{
                    writeData("offline".getBytes());
                }
            }

        } catch (SQLException e) {
            log.error(e);
        }
    }*/

    public void loginClient(String username, String password) throws IOException, SQLException {
        if (DatabaseUtils.isLoginDataCorrect(username, password)) {
            log.info("client exists");
            if (isClientAlreadyLogged(username)) {
                log.info(username + " is already logged");
                sendLoginResponse(false, username + " is already logged");
                closeConnection();
            } else {
                sendLoginResponse(true, username + " successfully logged");
                clientUsername = username;
                loggedClients.add(this);
                startListeningClientThread();
            }
        } else {
            log.info("client not exists");
            sendLoginResponse(false, "invalid email or password");
            closeConnection();
        }
    }

    public void signUpClient(String username, String password) throws IOException {
        try {
            if (DatabaseUtils.isUserExists(username)) {
                log.info("username is already used by someone else");
                sendSignUpResponse(false, "username is already used by someone else");
                return;
            }
            if (!DatabaseUtils.addUser(username, password)) {
                log.info("couldn't sign up");
                sendSignUpResponse(false, "couldn't sign up");
                return;
            }
            log.info("user successfully signed up");
            sendSignUpResponse(true, "signed up successfully ");
        } catch (SQLException e) {
            log.error("cant sign up right now " + e);
            sendSignUpResponse(false, "cant sign up right now");
        } finally {
            closeConnection();
        }
    }


    public boolean isClientAlreadyLogged(String username) {
        return loggedClients.stream().anyMatch(clientHandler -> username.equals(clientHandler.clientUsername));
    }

    /*private void broadcastMessages() throws IOException {
        while (socket.isConnected()) {
            byte[][] message = captureMessage();

            for (ClientHandler client : clients) {
                if (!clientUsername.equals(client.clientUsername)) {
                    client.passMessage(message);
                }
            }
        }
    }

    private void broadcastClientStatus(String status) throws IOException {
        for (ClientHandler client : clients) {
            if (!clientUsername.equals(client.clientUsername)) {
                client.writeData(status.getBytes());
            }
        }
    }

    private void broadcastMessagesTo(String username) throws IOException {

        while (socket.isConnected()) {
            byte[][] message = captureMessage();

            for (ClientHandler client : clients)
                if (username.equals(client.clientUsername))
                    client.passMessage(message);
        }

    }*/

    /*private String captureRequest() throws IOException {
        return new String(readData());
    }

    private byte[][] captureMessage() throws IOException {
        byte[] dataType = readData();
        byte[] data = readData();
        return new byte[][]{dataType, data};
    }

    private void passMessage(byte[][] message) throws IOException {
        writeData(message[0]);
        writeData(message[1]);
    }

    private byte[] readData() throws IOException {
        return objectInputStream.readNBytes(objectInputStream.readInt());
    }

    private void writeData(byte[] data) throws IOException {
        objectOutputStream.writeInt(data.length);
        objectOutputStream.write(data);
    }*/

    private void sendLoginResponse(boolean isLogged, String info) throws IOException {
        sendResponse(ClientRequests.LOGIN.toString());
        sendData(isLogged);
        sendData(info);
    }

    private void sendSignUpResponse(boolean isSignedUp, String info) throws IOException {
        sendResponse(ClientRequests.SIGN_UP.toString());
        sendData(isSignedUp);
        sendData(info);
    }

    private void sendUsernamesListResponse(TreeMap<String, Boolean> usernamesList) throws IOException {
        sendResponse(ClientRequests.USERNAMES_LIST.toString());
        sendData(usernamesList);
    }

    private void sendResponse(String request) throws IOException {
        sendData(request);
    }

    private void sendData(Object data) throws IOException {
        objectOutputStream.writeObject(data);
        objectOutputStream.flush();
    }

    private String receiveRequest() throws IOException {
        try {
            String response = (String) objectInputStream.readObject();
            if (response != null && Arrays.stream(ClientRequests.values()).anyMatch(clientRequest -> clientRequest.toString().equals(response)))
                return response;
        } catch (ClassNotFoundException e) {
            return null;
        }
        return null;
    }

    private Object receiveData() throws IOException {
        try {
            return objectInputStream.readObject();
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    public void closeConnection() {
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
        } catch (IOException e) {
            log.error("closeConnection(): " + e);
        }
    }

    private void removeClient() {
        loggedClients.remove(this);
    }

}
