package com.kwasheniak;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import lombok.extern.log4j.Log4j2;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;

@Log4j2
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

    private ClientService clientService;

    public static final String LOGIN_FXML_PATH = "/Login.fxml";
    public static final String USERNAME_PATTERN = "^[\\w]{4,45}$";
    public static final String EMAIL_PATTERN = "^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$";
    public static final String PASSWORD_PATTERN = "^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[a-zA-Z]).{8,45}$";

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        fxSignUpButton.setOnMouseEntered(mouseEvent -> fxSignUpButton.setStyle("-fx-background-color: #0339f9;"));

        fxSignUpButton.setOnMouseExited(mouseEvent -> fxSignUpButton.setStyle("-fx-background-color: #0374f9;"));

        fxSignUpButton.setOnAction(event -> {
            String username = fxUsernameField.getText();
            String email = fxEmailField.getText();
            String password = fxPasswordField.getText();
            String repeatPassword = fxRepeatPasswordField.getText();
            if(!isSignUpFormValid(username, email, password, repeatPassword)){
                return;
            }
            clientService = new ClientService();
            if(!clientService.isConnectedToServer()){
                log.info("couldn't connect to server");
                return;
            }

            try {
                clientService.sendLogin("signup", username, password);
                log.info("sign up form sent");
                listenForServerResponse(event);
            } catch (IOException e) {
                log.error(e);
            }
        });

        fxLoginLink.setOnAction(event -> {
            try {
                switchToLoginWindow(event);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });

    }

    public void listenForServerResponse(ActionEvent event) {
        new Thread(isClientLoggedTask(event)).start();
    }

    private Task<Boolean> isClientLoggedTask(ActionEvent event) {


        //BorderPane loadingScreen = getLoadingScreen();

        //task is waiting for server to send message
        Task<Boolean> task = new Task<>() {
            @Override
            protected Boolean call() throws Exception {
                //return value sent by server
                return clientService.getDataInputStream().readBoolean();
            }
        };
        /*task.setOnRunning(workerStateEvent -> {
            //loading screen appear
            Platform.runLater(() -> fxRootContainer.getChildren().add(loadingScreen));
        });*/

        //run when waitingForResponseFromServer task is completed (server sent message)
        task.setOnSucceeded(workerStateEvent -> {
            try {
                //loading screen disappear
                //Platform.runLater(() -> fxRootContainer.getChildren().remove(loadingScreen));
                //if response is true, stage switches to client scene
                //that means client logged successfully
                DataInputStream dataInputStream = clientService.getDataInputStream();
                String serverResponse = new String(dataInputStream.readNBytes(dataInputStream.readInt()));
                if (task.get()) {
                    log.info(serverResponse);
                } else {
                    log.info(serverResponse);
                }
            } catch (InterruptedException | ExecutionException | IOException e) {
                log.error(e);
            }
        });
        return task;
    }

    public BorderPane getLoadingScreen() {
        //loading Pane displayed when login sending
        BorderPane loading = new BorderPane();
        loading.setCenter(new Label("Loading..."));
        //sets half transparent background
        loading.setStyle("-fx-background-color: #00000090;");
        return loading;
    }

    private void switchToLoginWindow(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(LOGIN_FXML_PATH));
        Scene scene = new Scene(loader.load());
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(scene);
        stage.show();
    }

    private boolean isSignUpFormValid(String username, String email, String password, String repeatPassword){
        boolean isValid = true;

        //accepts any word character from 4 to 45 length
        if(!Pattern.matches(USERNAME_PATTERN, username)){
            log.info("username is incorrect");
            isValid = false;
        }

        if(!Pattern.matches(EMAIL_PATTERN, email)){
            log.info("email is incorrect");
            isValid = false;
        }

        if(!Pattern.matches(PASSWORD_PATTERN, password)){
            log.info("password is incorrect");
            isValid = false;
        }

        if(!password.equals(repeatPassword)){
            log.info("passwords are not equal");
            isValid = false;
        }

        return isValid;
    }

}
