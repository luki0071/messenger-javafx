package com.kwasheniak.client;

import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;

public class ClientUtils {
    /*public static final String LOGIN = "login";
    public static final String LOGOUT = "logout";
    public static final String SIGN_UP = "signup";
    public static final String USERNAMES_LIST = "usernames";
    public static final String START_CHAT = "startchat";*/
    @Getter
    @Setter
    private static String currentConversation;

    public static boolean sendLoginRequest(String username, String password) {
        try {
            sendRequest(ClientRequests.LOGIN.toString());
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
            sendRequest(ClientRequests.LOGOUT.toString());
            return true;
        } catch (IOException e) {
            ClientService.closeConnection();
        }
        return false;
    }

    public static boolean sendSignUpRequest(String username, String password) {
        try {
            sendRequest(ClientRequests.SIGN_UP.toString());
            sendData(username);
            sendData(password);
            return true;
        } catch (IOException e) {
            ClientService.closeConnection();
        }
        return false;
    }

    public static void sendUsernameListRequest() throws IOException {
        sendRequest(ClientRequests.USERNAMES_LIST.toString());
    }

    public static boolean startChatWith(String username) throws IOException {
        if (ClientService.isConnectedToServer()) {
            sendRequest(ClientRequests.START_CHAT.toString());
            sendData(username);
            return true;
        }
        return false;
    }

    private static void sendRequest(String request) throws IOException {
        sendData(request);
    }

    private static void sendData(Object data) throws IOException {
        ObjectOutputStream objectOutputStream = ClientService.getObjectOutputStream();
        objectOutputStream.writeObject(data);
        objectOutputStream.flush();
    }

    public static String receiveResponse() throws IOException {
        ObjectInputStream objectInputStream = ClientService.getObjectInputStream();
        try {
            String response = (String) objectInputStream.readObject();
            if (response != null && Arrays.stream(ClientRequests.values()).anyMatch(clientRequest -> clientRequest.toString().equals(response)))
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

    /*public static byte[][] sendTextMessage(String textToSend) throws IOException {
        byte[] type = new byte[0];
        byte[] bytes = textToSend.getBytes();
        sendMessage(type);
        sendMessage(bytes);
        return new byte[][]{type, bytes};
    }

    public static byte[][] sendFileMessage(File fileToSend) throws IOException {
        byte[] fileName = fileToSend.getName().getBytes();
        FileInputStream fileInputStream = new FileInputStream(fileToSend.getAbsolutePath());
        byte[] file = fileInputStream.readNBytes((int) fileToSend.length());
        fileInputStream.close();
        sendMessage(fileName);
        sendMessage(file);
        return new byte[][]{fileName, file};
    }

    private static void sendMessage(byte[] data) throws IOException {
        ClientService.getObjectOutputStream().writeInt(data.length);
        ClientService.getObjectOutputStream().write(data);
    }*/
}
