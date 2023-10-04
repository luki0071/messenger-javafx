package com.kwasheniak;

import com.kwasheniak.database.DatabaseHandler;
import com.kwasheniak.database.DatabaseService;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.function.Predicate;

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
            this.socket = socket;
            this.controller = controller;
            this.dataInputStream = new DataInputStream(socket.getInputStream());
            this.dataOutputStream = new DataOutputStream(socket.getOutputStream());
            //this.clientPort = socket.getPort();

            answerToClientIfHeIsLogged();
        } catch (IOException e) {
            log.error("ClientHandler " + e);
            closeConnection();
        }
    }

    public void answerToClientIfHeIsLogged() throws IOException {
        //If client's username and password are in database
        //send to client true value which means he is logged and added to clients static list
        String username = new String(dataInputStream.readNBytes(dataInputStream.readInt()));
        String password = new String(dataInputStream.readNBytes(dataInputStream.readInt()));
        try {
            Thread.sleep(3000);
            if(DatabaseHandler.isUserInUsersTable(username, password)) {

                //controller.addLogOnLogsBoard("client exists");
                log.info("client exists");
                if(clients.stream().anyMatch(clientHandler -> username.equals(clientHandler.clientUsername))){
                    log.info(username + " is already logged");
                    try {
                        dataOutputStream.writeBoolean(false);
                    } catch (IOException e) {
                        log.error(e);
                    }
                    closeConnection();
                }else{

                    dataOutputStream.writeBoolean(true);
                    clientUsername = username;
                    clients.add(this);
                }
            }
            //send to client false value which means he is not logged
            //closes socket connection with server
            else{
                //controller.addLogOnLogsBoard("client not exists");
                log.info("client not exists");
                dataOutputStream.writeBoolean(false);
                closeConnection();
            }
        } catch (SQLException | InterruptedException e) {
            log.error(e);
        }
    }

    /*public Boolean isClientInDatabase(String username, String password){
        try {
            if(DatabaseService.isConnectionAvailable()){
                return DatabaseService.isUserInUsersTable(username, password);
            }else{
                //todo: send message to client that he cannot log in at the moment
                log.info("connection with database is unavailable");
                closeConnection();
            }
        } catch (IOException | SQLException e) {
            log.error("isClientInDatabase(): " + e);
        }
        return false;
    }*/

    @Override
    public void run() {
        //controller.addLogOnLogsBoard("connected with client");
        log.info("connected with client");
        try{
            while (socket.isConnected()) {
                byte[] dataType = dataInputStream.readNBytes(dataInputStream.readInt());
                byte[] data = dataInputStream.readNBytes(dataInputStream.readInt());

                broadcastMessage(dataType, data);
            }
        }catch (IOException e){
            closeConnection();
            removeClientHandler();
        }
        //controller.addLogOnLogsBoard("disconnected with client");
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
            if (dataInputStream != null ){
                dataInputStream.close();
            }
            if (dataOutputStream != null){
                dataOutputStream.close();
            }
            if (socket != null){
                socket.close();
            }
        } catch (IOException e) {
            log.error("closeConnection(): " + e);
        }
    }

    private void removeClientHandler() {
        clients.remove(this);
    }

    public static void closeConnectionWithAllClients(){
        clients.forEach(ClientHandler::closeConnection);
        clients.clear();
    }

}
