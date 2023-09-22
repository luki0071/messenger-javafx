package com.kwasheniak;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientCore {

    public static final String TEXT_MESSAGE_LABEL = "text";

    public static void sendMessageToServer(String textToSend) throws IOException {

        byte[] type = TEXT_MESSAGE_LABEL.getBytes();
        byte[] bytes = textToSend.getBytes();
        sendMessageToServer(type, bytes);
    }

    public static void sendMessageToServer(String fileName, File fileToSend) throws IOException {

        byte[] name = fileName.getBytes();
        FileInputStream fileInputStream = new FileInputStream(fileToSend.getAbsolutePath());
        byte[] bytes = fileInputStream.readNBytes((int)fileToSend.length());
        fileInputStream.close();
        sendMessageToServer(name, bytes);
    }

    private static void sendMessageToServer(byte[] labelBytes, byte[] dataBytes) throws IOException {
        Socket socket = new Socket("localhost", 1234);
        DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
        dataOutputStream.writeInt(labelBytes.length);
        dataOutputStream.write(labelBytes);
        dataOutputStream.writeInt(dataBytes.length);
        dataOutputStream.write(dataBytes);
        dataOutputStream.close();
        socket.close();
    }
}
