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
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutionException;

@Log4j2
public class LoginController implements Initializable {

    public static final String CLIENT_FXML_FILE_PATH = "/Client.fxml";
    public static final String SIGN_UP_FXML_FILE_PATH = "/SignUp.fxml";

    @FXML
    public StackPane fxRootContainer;
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
    @FXML
    public VBox fxLoginFormBox;
    @FXML
    public BorderPane fxLoginContainer;

    private ClientService clientService;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        fxLoginButton.setOnAction(event -> {
            try {
                String username = fxUsernameField.getText();
                String password = fxPasswordField.getText();
                if(!username.isEmpty() && !password.isEmpty()){
                    clientService = new ClientService();
                    if(clientService.isConnectedToServer()){
                        clientService.sendLoginToServer(username,password);
                        new Thread(getWaitingForResponseFromServerTask(event)).start();
                    }else{
                        log.info("no connection with server couldn't send login data");
                    }
                }else{
                    log.info("please type username and password");
                }
            } catch (Exception e) {
                log.error(e);
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

    public Boolean isUsernameAndPasswordFilled(){
        String username = fxUsernameField.getText();
        String password = fxPasswordField.getText();
        return !username.isEmpty() && !password.isEmpty();
    }

    private Task<Boolean> getWaitingForResponseFromServerTask(ActionEvent event) {


        BorderPane loadingScreen = getLoadingScreen();

        //task is waiting for server to send message
        Task<Boolean> waitingForResponseFromServer = new Task<>() {
            @Override
            protected Boolean call() throws Exception {
                //loading screen appear
                Platform.runLater(() -> fxRootContainer.getChildren().add(loadingScreen));
                //return value sent by server
                return clientService.getDataInputStream().readBoolean();
            }
        };

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
    }

    /**
     * switches to client scene
     * @param event
     * @throws IOException
     */
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

    /**
     * switches to sign up scene
     * @param event
     * @throws IOException
     */
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

    /**
     * creates loading screen
     * @return object of loading screen
     */
    public BorderPane getLoadingScreen(){
        //loading Pane displayed when login sending
        BorderPane loading = new BorderPane();
        loading.setCenter(new Label("Loading..."));
        //sets half transparent background
        loading.setStyle("-fx-background-color: #00000090;");
        return loading;
    }
}
