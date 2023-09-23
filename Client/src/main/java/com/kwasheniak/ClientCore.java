package com.kwasheniak;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

import java.io.*;
import java.net.Socket;

@Log4j2
@Setter
@Getter
public class ClientCore {

    public static final String TEXT_MESSAGE_LABEL = "text";
    public static final String HOST = "localhost";
    public static final int PORT = 1234;

    private Socket socket;
    private DataOutputStream dataOutputStream;
    private DataInputStream dataInputStream;

    public ClientCore() {
        try {
            this.socket = new Socket(HOST, PORT);
            this.dataOutputStream = new DataOutputStream(socket.getOutputStream());
            this.dataInputStream = new DataInputStream(socket.getInputStream());
            log.info(socket.toString());
            log.info(dataOutputStream.toString());
            log.info(dataInputStream.toString());
        } catch (IOException e) {
            log.error("ClientCore " + e);
            closeConnection();
        }
    }

    public void sendMessageToServer(String textToSend) throws IOException {
        byte[] type = TEXT_MESSAGE_LABEL.getBytes();
        byte[] bytes = textToSend.getBytes();
        sendMessageToServer(type, bytes);
    }

    public void sendMessageToServer(String fileName, File fileToSend) throws IOException {
        byte[] name = fileName.getBytes();
        FileInputStream fileInputStream = new FileInputStream(fileToSend.getAbsolutePath());
        byte[] bytes = fileInputStream.readNBytes((int)fileToSend.length());
        fileInputStream.close();
        sendMessageToServer(name, bytes);
    }

    private void sendMessageToServer(byte[] labelBytes, byte[] dataBytes) throws IOException {
        dataOutputStream.writeInt(labelBytes.length);
        dataOutputStream.write(labelBytes);
        dataOutputStream.writeInt(dataBytes.length);
        dataOutputStream.write(dataBytes);
    }

    public void closeConnection(){
        try{
            if(dataInputStream != null)
                dataInputStream.close();
            if(dataOutputStream != null)
                dataOutputStream.close();
            if(socket != null)
                socket.close();
        }catch (IOException e){
            log.error("closeConnection() " + e);
        }
    }
}
