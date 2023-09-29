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

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class LoginController implements Initializable {

    @FXML
    public BorderPane fxRootContainer;
    @FXML
    public TextField fxUsernameField;
    @FXML
    public TextField fxPasswordField;
    @FXML
    public Button fxSignInButton;
    @FXML
    public Hyperlink fxForgotPasswordLink;
    @FXML
    public Hyperlink fxSignUpLink;

    private ClientCore clientCore;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        fxSignInButton.setOnAction(event -> {
            try {
                openClientWindow(event);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

    }

    public void openClientWindow(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Client.fxml"));
        Scene scene = new Scene(loader.load());
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(scene);
        clientCore = new ClientCore();
        ClientController controller = loader.getController();
        controller.setClientCore(clientCore);
        clientCore.listenForMessages(controller);

        stage.show();

        stage.setOnCloseRequest(windowEvent -> clientCore.closeConnection());
    }
}
