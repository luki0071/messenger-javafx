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

public class SignUpController implements Initializable {

    @FXML
    public BorderPane fxRootContainer;
    @FXML
    public TextField fxUsernameField;
    @FXML
    public TextField fxEmailField;
    @FXML
    public TextField fxPasswordField;
    @FXML
    public TextField fxRepeatPasswordField;
    @FXML
    public Button fxSignUpButton;
    public Hyperlink fxLoginLink;

    public static final String LOGIN_FXML_PATH = "/Login.fxml";

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        fxSignUpButton.setOnMouseEntered(mouseEvent -> fxSignUpButton.setStyle("-fx-background-color: #0339f9;"));

        fxSignUpButton.setOnMouseExited(mouseEvent -> fxSignUpButton.setStyle("-fx-background-color: #0374f9;"));

        fxLoginLink.setOnAction(event -> {
            try {
                switchToLoginWindow(event);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

    }

    public void switchToLoginWindow(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(LOGIN_FXML_PATH));
        Scene scene = new Scene(loader.load());
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(scene);
        stage.show();
    }

}
