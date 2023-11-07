package com.kwasheniak.client;

import com.kwasheniak.data.ChatMessage;
import com.kwasheniak.data.Requests;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;

@Log4j2
public class ClientUtils {

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

    public static boolean sendMessage(String receiver, ChatMessage message) {
        try {
            sendRequest(Requests.CHAT_MESSAGE);
            sendData(receiver);
            sendData(message);
            return true;
        } catch (IOException e) {
            log.error(e);
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