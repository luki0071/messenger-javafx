package com.kwasheniak.client;

import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.animation.Transition;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.NodeOrientation;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.util.Duration;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutionException;

@Log4j2
public class LoginController implements Initializable {

    public static final String MENU_FXML = "/Menu.fxml";
    public static final String SIGN_UP_FXML = "/SignUp.fxml";
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
    public Hyperlink fxSignUpLink;
    @FXML
    public VBox fxLoginFormBox;
    @FXML
    public StackPane fxLoginContainer;
    @FXML
    public VBox fxLoginInfoPanel;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        fxLoginButton.setOnAction(event -> loginClient(fxUsernameField.getText(), fxPasswordField.getText(), event));
        fxLoginButton.setOnMouseEntered(mouseEvent -> fxLoginButton.setStyle("-fx-background-color: #0339f9;"));
        fxLoginButton.setOnMouseExited(mouseEvent -> fxLoginButton.setStyle("-fx-background-color: #0374f9;"));

        fxSignUpLink.setOnAction(this::switchToSignUpWindow);
    }

    public void loginClient(String username, String password, Event event) {
        clearLoginInfoPanel();
        if (isLoginEmpty(username, password)) {
            setTextLoginInfoPanel("please type username and password");
            return;
        }
        ClientService.connectToServer();
        if (!ClientService.isConnectedToServer()) {
            setTextLoginInfoPanel("no connection with server, couldn't login");
            return;
        }
        if (!ClientUtils.sendLoginRequest(username, password)) {
            setTextLoginInfoPanel("something went wrong, couldn't send login data");
            return;
        }
        /*try {
            ClientUtils.sendLoginRequest(username, password);
        } catch (IOException e) {
            setLoginInfo("something went wrong, couldn't send login data");
            ClientService.closeConnection();
            return;
        }*/
        listenFromServer(event);
    }

    private boolean isLoginEmpty(String username, String password) {
        return username.isEmpty() || password.isEmpty();
    }

    private void setTextLoginInfoPanel(String textInfo) {
        Text text = new Text();
        text.setFill(Color.RED);
        text.setWrappingWidth(fxLoginContainer.getWidth());
        text.setTextAlignment(TextAlignment.CENTER);
        text.setText(textInfo);
        fxLoginInfoPanel.getChildren().add(text);
    }

    private void clearLoginInfoPanel() {
        fxLoginInfoPanel.getChildren().clear();
    }

    public void listenFromServer(Event event) {
        Thread thread = new Thread(getLoginTask(event));
        thread.setName("Login Thread");
        thread.start();
    }

    private Task<Boolean> getLoginTask(Event event) {

        Pane loadingScreen = getLoadingScreen();

        final String[] responseInfo = new String[1];

        Task<Boolean> loginTask = new Task<>() {
            @Override
            protected Boolean call() throws IOException, ClassNotFoundException {
                String response;
                do {
                    response = ClientUtils.receiveResponse();
                } while (response == null || !ClientRequests.LOGIN.toString().equals(response));
                boolean isLogged = (Boolean) ClientUtils.receiveData();
                responseInfo[0] = (String) ClientUtils.receiveData();
                return isLogged;
            }
        };
        loginTask.setOnRunning(workerStateEvent -> Platform.runLater(() -> showLoadingScreen(loadingScreen)));

        loginTask.setOnSucceeded(workerStateEvent -> {
            try {
                Platform.runLater(() -> hideLoadingScreen(loadingScreen));
                if (loginTask.get()) {
                    log.info("logged successfully");
                    switchToMenuWindow(event);
                } else {
                    setTextLoginInfoPanel(responseInfo[0]);
                    ClientService.closeConnection();
                }
            } catch (InterruptedException | ExecutionException e) {
                log.error(e);
            }
        });
        return loginTask;
    }

    public void switchToMenuWindow(Event event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(MENU_FXML));
            Scene scene = new Scene(loader.load());
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.setOnCloseRequest(windowEvent -> ClientService.closeConnection());
            stage.show();
        } catch (IOException e) {
            log.error(e);
        }
    }

    public void switchToSignUpWindow(Event event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(SIGN_UP_FXML));
            Scene scene = new Scene(loader.load());
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            log.error(e);
        }
    }

    private void showLoadingScreen(Pane loadingScreen) {
        fxLoginContainer.getChildren().add(loadingScreen);
    }

    private void hideLoadingScreen(Pane loadingScreen) {
        fxLoginContainer.getChildren().remove(loadingScreen);
    }

    private BorderPane getLoadingScreen() {
        BorderPane pane = new BorderPane();
        pane.setStyle("-fx-background-color: #FFFFFF;");
        ImageView loadingCircle = new ImageView(new Image("images/loading-circle2.png"));
        loadingCircle.setPreserveRatio(true);
        loadingCircle.setFitHeight(150);
        loadingCircle.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
        setLoadingAnimation(loadingCircle);
        pane.setCenter(loadingCircle);

        return pane;
    }

    private void setLoadingAnimation(Node node) {
        RotateTransition transition = new RotateTransition();
        transition.setNode(node);
        transition.setDuration(Duration.millis(1500));
        transition.setCycleCount(Transition.INDEFINITE);
        transition.setInterpolator(Interpolator.LINEAR);
        transition.setByAngle(360);
        transition.play();
    }
}