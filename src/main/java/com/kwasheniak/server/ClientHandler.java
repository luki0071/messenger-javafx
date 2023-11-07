package com.kwasheniak.server;

import com.kwasheniak.client.ClientStatus;
import com.kwasheniak.data.ChatMessage;
import com.kwasheniak.data.Requests;
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
import java.util.HashMap;
import java.util.TreeMap;

@Log4j2
public class ClientHandler {
    public static volatile ArrayList<ClientHandler> loggedClients = new ArrayList<>();

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

            Requests whatClientWant = receiveRequest();
            String username = String.valueOf(receiveData());
            String password = String.valueOf(receiveData());

            if (Requests.LOGIN.equals(whatClientWant)) {
                loginClient(username, password);
                return;
            }
            if (Requests.SIGN_UP.equals(whatClientWant)) {
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
                    Requests request = receiveRequest();
                    if (Requests.USERNAMES_LIST.equals(request)) {
                        log.info("request usernames");
                        sendUsernamesListResponse(getUsersStatus());
                        continue;
                    }
                    if (Requests.CHAT_MESSAGE.equals(request)) {
                        String messageReceiver = (String) receiveData();
                        ChatMessage message = (ChatMessage) receiveData();
                        broadcastMessageTo(messageReceiver, message);
                        continue;
                    }
                    if (Requests.LOGOUT.equals(request)) {
                        updateUserStatus(clientUsername, ClientStatus.OFFLINE);
                        closeConnection();
                        removeClient();
                    }
                }
            } catch (IOException e) {
                updateUserStatus(clientUsername, ClientStatus.OFFLINE);
                closeConnection();
                removeClient();
            }
            log.info("disconnected with client");
        });
        thread.setName(clientUsername + " Thread");
        thread.start();
    }

    public TreeMap<String, ClientStatus> getUsersStatus() {
        try {
            TreeMap<String, ClientStatus> usersStatus = new TreeMap<>();
            String[] usernames = DatabaseUtils.getAllUsernamesExcept(clientUsername);

            Arrays.stream(usernames).forEach(username -> {
                boolean status = loggedClients.stream().anyMatch(clientHandler -> username.equals(clientHandler.clientUsername));
                usersStatus.put(username, status ? ClientStatus.ONLINE : ClientStatus.OFFLINE);
            });
            return usersStatus;
        } catch (SQLException e) {
            log.error(e);
        }
        return null;
    }

    public void loginClient(String username, String password) throws IOException, SQLException {
        if (!DatabaseUtils.isLoginDataCorrect(username, password)) {
            log.info("client not exists");
            sendLoginResponse(false, "invalid email or password");
            closeConnection();
            return;
        }
        log.info("client exists");
        if (isClientAlreadyLogged(username)) {
            log.info(username + " is already logged");
            sendLoginResponse(false, username + " is already logged");
            closeConnection();
            return;
        }
        sendLoginResponse(true, username + " successfully logged");
        clientUsername = username;
        loggedClients.add(this);
        updateUserStatus(clientUsername, ClientStatus.ONLINE);
        startListeningClientThread();
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

    private void broadcastMessageTo(String username, ChatMessage message) throws IOException {

        for (ClientHandler client : loggedClients)
            if (username.equals(client.clientUsername)) {
                client.sendResponse(Requests.CHAT_MESSAGE);
                client.sendData(clientUsername);
                client.sendData(message);
            }
    }

    private void sendLoginResponse(boolean isLogged, String info) throws IOException {
        sendResponse(Requests.LOGIN);
        sendData(isLogged);
        sendData(info);
    }

    private void sendSignUpResponse(boolean isSignedUp, String info) throws IOException {
        sendResponse(Requests.SIGN_UP);
        sendData(isSignedUp);
        sendData(info);
    }

    private void sendUsernamesListResponse(TreeMap<String, ClientStatus> usernamesList) throws IOException {
        sendResponse(Requests.USERNAMES_LIST);
        sendData(usernamesList);
    }

    private void updateUserStatus(String username, ClientStatus status) {
        for (ClientHandler client : loggedClients) {
            if (!clientUsername.equals(client.clientUsername)) {
                try {
                    client.sendResponse(Requests.UPDATE_USER_STATUS);
                    HashMap<String, ClientStatus> map = new HashMap<>();
                    map.put(username, status);
                    client.sendData(map);
                } catch (IOException e) {
                    log.error("couldn't send data to " + client.clientUsername + " " + e);
                }
            }
        }
    }

    private void sendResponse(Requests request) throws IOException {
        sendData(request);
    }

    private void sendData(Object data) throws IOException {
        objectOutputStream.writeObject(data);
        objectOutputStream.flush();
    }

    private Requests receiveRequest() throws IOException {
        try {
            Requests response = (Requests) objectInputStream.readObject();
            if (response != null && Arrays.asList(Requests.values()).contains(response))
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
