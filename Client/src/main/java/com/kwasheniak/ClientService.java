package com.kwasheniak;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.scene.layout.BorderPane;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;


@Log4j2
@Getter
public class ClientService {

    public static final String HOST = "localhost";
    public static final int PORT = 1234;

    private Socket socket;
    private DataOutputStream dataOutputStream;
    private DataInputStream dataInputStream;

    public ClientService() {
        connectToServer();
    }

    public Boolean isConnectedToServer(){
        return socket != null && !socket.isClosed();
    }

    public void connectToServer(){
        if(isConnectedToServer()){
            log.info("client already connected to server");
            return;
        }
        try {
            this.socket = new Socket(HOST, PORT);
            this.dataOutputStream = new DataOutputStream(socket.getOutputStream());
            this.dataInputStream = new DataInputStream(socket.getInputStream());
        } catch (IOException e) {
            log.error("connectToServer() " + e);
            closeConnection();
        }
    }

    public byte[][] sendMessage(String textToSend) throws IOException {
        byte[] type = new byte[0];
        byte[] bytes = textToSend.getBytes();
        sendMessageToServer(type, bytes);
        return new byte[][]{type, bytes};
    }

    public byte[][] sendMessage(File fileToSend) throws IOException {
        byte[] fileName = fileToSend.getName().getBytes();
        FileInputStream fileInputStream = new FileInputStream(fileToSend.getAbsolutePath());
        byte[] file = fileInputStream.readNBytes((int) fileToSend.length());
        fileInputStream.close();
        sendMessageToServer(fileName, file);
        return new byte[][]{fileName, file};
    }

    private void sendMessageToServer(byte[] labelBytes, byte[] dataBytes) throws IOException {
        dataOutputStream.writeInt(labelBytes.length);
        dataOutputStream.write(labelBytes);
        dataOutputStream.writeInt(dataBytes.length);
        dataOutputStream.write(dataBytes);
    }

    public void listenForMessages(ClientController controller) {
        new Thread(() -> {
            try {
                /*if (socket != null) {
                    while (socket.isConnected()) {
                        byte[] dataType = dataInputStream.readNBytes(dataInputStream.readInt());
                        byte[] data = dataInputStream.readNBytes(dataInputStream.readInt());
                        Platform.runLater(() -> controller.addMessageToMessageBoard(MessageStatus.RECEIVED, new byte[][]{dataType, data}));
                    }
                }*/
                while (isConnectedToServer()) {
                    byte[] dataType = dataInputStream.readNBytes(dataInputStream.readInt());
                    byte[] data = dataInputStream.readNBytes(dataInputStream.readInt());
                    Platform.runLater(() -> controller.addMessageToMessageBoard(MessageStatus.RECEIVED, new byte[][]{dataType, data}));
                }
            } catch (IOException e) {
                log.error(e);
            }
        }).start();
    }

    public void sendLoginToServer(String username, String password) throws IOException {
        byte[] user = username.getBytes();
        byte[] pass = password.getBytes();
        sendMessageToServer(user, pass);
        /*dataOutputStream.writeInt(user.length);
        dataOutputStream.write(user);
        log.info("user sent");
        dataOutputStream.writeInt(pass.length);
        dataOutputStream.write(pass);
        log.info("password sent");*/
    }

    public void listenForLogin(){

    }

    /*private Task<Boolean> getWaitingForResponseFromServerTask(ActionEvent event) {


        BorderPane loadingScreen = controller getLoadingScreen();

        //task is waiting for server to send message
        Task<Boolean> waitingForResponseFromServer = new Task<>() {
            @Override
            protected Boolean call() throws Exception {
                //loading screen appear
                Platform.runLater(() -> fxRootContainer.getChildren().add(loadingScreen));
                //return value sent by server
                return dataInputStream.readBoolean();
            }
        };
        wait

        //run when waitingForResponseFromServer task is completed (server sent message)
        waitingForResponseFromServer.setOnSucceeded(workerStateEvent -> {
            try {
                //loading screen disappear
                Platform.runLater(() -> fxRootContainer.getChildren().remove(loadingScreen));
                //if response is true, stage switches to client scene
                //that means client logged successfully
                if(waitingForResponseFromServer.get()){
                    switchToClientWindow(event);
                }else{
                    log.info("invalid email or password");
                }
            } catch (InterruptedException | ExecutionException | IOException e) {
                log.error(e);
            }
        });
        return waitingForResponseFromServer;
    }*/

    public void closeConnection() {
        try {
            if (dataInputStream != null)
                dataInputStream.close();
            if (dataOutputStream != null)
                dataOutputStream.close();
            if (socket != null)
                socket.close();
        } catch (IOException e) {
            log.error("closeConnection() " + e);
        }
    }
}
