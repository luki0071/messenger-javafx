package com.kwasheniak;

import javafx.application.Platform;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;

public class ServerCore {

    public static void createServer(int portNumber, ServerActivityController controller) throws IOException {

        ServerSocket serverSocket = new ServerSocket(portNumber);
        while(true){
            try{
                Socket socket = serverSocket.accept();

                DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
                byte[] bytes = dataInputStream.readNBytes(dataInputStream.readInt());
                String text = new String(bytes);

                //Platform.runLater allows update a GUI component from a non-GUI thread
                Platform.runLater(() -> controller.addMessageOnBoard(text));
                //controller.addMessageOnBoard(text);

                dataInputStream.close();
                socket.close();
            }catch (IOException e){
                e.getMessage();
            }
        }
    }

}
