package com.kwasheniak.client;

import com.kwasheniak.data.Requests;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;

public class ClientUtils {
    @Getter
    @Setter
    private static String currentConversation;

    public static boolean sendLoginRequest(String username, String password) {
        try {
            sendRequest(Requests.LOGIN);
            sendData(username);
            sendData(password);
            return true;
        } catch (IOException e) {
            ClientService.closeConnection();
        }
        return false;
    }

    public static boolean sendLogoutRequest() {
        try {
            sendRequest(Requests.LOGOUT);
            return true;
        } catch (IOException e) {
            ClientService.closeConnection();
        }
        return false;
    }

    public static boolean sendSignUpRequest(String username, String password) {
        try {
            sendRequest(Requests.SIGN_UP);
            sendData(username);
            sendData(password);
            return true;
        } catch (IOException e) {
            ClientService.closeConnection();
        }
        return false;
    }

    public static void sendUsernameListRequest() throws IOException {
        sendRequest(Requests.USERNAMES_LIST);
    }

    public static boolean startChatWith(String username) throws IOException {
        if (ClientService.isConnectedToServer()) {
            sendRequest(Requests.START_CHAT);
            sendData(username);
            return true;
        }
        return false;
    }

    private static void sendRequest(Requests request) throws IOException {
        sendData(request);
    }

    private static void sendData(Object data) throws IOException {
        ObjectOutputStream objectOutputStream = ClientService.getObjectOutputStream();
        objectOutputStream.writeObject(data);
        objectOutputStream.flush();
    }

    public static Requests receiveResponse() throws IOException {
        ObjectInputStream objectInputStream = ClientService.getObjectInputStream();
        try {
            Requests response = (Requests) objectInputStream.readObject();
            if (response != null && Arrays.asList(Requests.values()).contains(response))
                return response;
        } catch (ClassNotFoundException e) {
            return null;
        }
        return null;
    }

    public static Object receiveData() throws IOException {
        ObjectInputStream objectInputStream = ClientService.getObjectInputStream();
        try {
            return objectInputStream.readObject();
        } catch (ClassNotFoundException e) {
            return null;
        }
    }
}
