package com.kwasheniak;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

@Log4j2
public class LoginController implements Initializable {

    public static final String CLIENT_FXML_FILE_PATH = "/Client.fxml";
    public static final String SIGN_UP_FXML_FILE_PATH = "/SignUp.fxml";

    @FXML
    public BorderPane fxRootContainer;
    @FXML
    public TextField fxUsernameField;
    @FXML
    public TextField fxPasswordField;
    @FXML
    public Button fxLoginButton;
    @FXML
    public Hyperlink fxForgotPasswordLink;
    @FXML
    public Hyperlink fxRegisterLink;

    private ClientService clientService;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {


        fxLoginButton.setOnAction(event -> {
            try {
                String username = fxUsernameField.getText();
                String password = fxPasswordField.getText();
                if(!username.isEmpty() && !password.isEmpty()){
                    clientService = new ClientService();
                    if(clientService.loginToServer(username,password)){
                        switchToClientWindow(event);
                    }else{
                        log.info("invalid email or password");
                    }
                }else{
                    log.info("please type username and password");
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

        fxLoginButton.setOnMouseEntered(mouseEvent -> fxLoginButton.setStyle("-fx-background-color: #0339f9;"));

        fxLoginButton.setOnMouseExited(mouseEvent -> fxLoginButton.setStyle("-fx-background-color: #0374f9;"));

        fxRegisterLink.setOnAction(event -> {
            try {
                switchToSignUpWindow(event);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

    }

    public void switchToClientWindow(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(CLIENT_FXML_FILE_PATH));
        Scene scene = new Scene(loader.load());
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(scene);
        ClientController controller = loader.getController();
        setupClientService(controller);

        stage.show();

        stage.setOnCloseRequest(windowEvent -> clientService.closeConnection());
    }

    public void switchToSignUpWindow(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(SIGN_UP_FXML_FILE_PATH));
        Scene scene = new Scene(loader.load());
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(scene);
        stage.show();
    }

    public void setupClientService(ClientController controller){
        //clientService = new ClientService();
        controller.setClientCore(clientService);
        clientService.listenForMessages(controller);
    }

    /*public void listenForLogin(Socket socket) {
        new Thread(() -> {
            try {
                if (socket != null) {
                    while (socket.isConnected()) {
                        DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
                        byte[] returnMessage = dataInputStream.readNBytes(dataInputStream.readInt());
                        log.info(new String(returnMessage));
                        dataInputStream.close();
                        socket.close();
                    }
                }
            } catch (IOException e) {
                log.error(e);
            }
        }).start();
    }*/
}
