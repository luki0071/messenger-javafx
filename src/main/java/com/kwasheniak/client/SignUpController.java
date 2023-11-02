package com.kwasheniak.client;

import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.animation.Transition;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
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
    @FXML
    public Hyperlink fxLoginLink;
    @FXML
    public VBox fxSignUpInfoPanel;
    @FXML
    public VBox fxSignUpSuccessful;
    @FXML
    public Hyperlink fxSuccessfulLoginLink;
    @FXML
    public VBox fxSignUpFormBox;
    @FXML
    public StackPane fxSignUpContainer;

    public static final String LOGIN_FXML_PATH = "/Login.fxml";
    public static final String USERNAME_PATTERN = "^[\\w]{4,45}$";
    public static final String EMAIL_PATTERN = "^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$";
    public static final String PASSWORD_PATTERN = "^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[a-zA-Z]).{8,45}$";

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        fxSignUpButton.setOnAction(event ->
                signUpClient(fxUsernameField.getText(), fxEmailField.getText(), fxPasswordField.getText(), fxRepeatPasswordField.getText(), event));
        fxSignUpButton.setOnMouseEntered(mouseEvent -> fxSignUpButton.setStyle("-fx-background-color: #0339f9;"));
        fxSignUpButton.setOnMouseExited(mouseEvent -> fxSignUpButton.setStyle("-fx-background-color: #0374f9;"));

        fxLoginLink.setOnAction(this::switchToLoginWindow);
        fxSuccessfulLoginLink.setOnAction(this::switchToLoginWindow);
    }

    private void signUpClient(String username, String email, String password, String repeatPassword, Event event) {
        clearSignUpInfoPanel();
        if (!isSignUpFormValid(username, email, password, repeatPassword)) {
            return;
        }
        ClientService.connectToServer();
        if (!ClientService.isConnectedToServer()) {
            setTextSignUpInfoPanel("couldn't connect to server");
            return;
        }
        if (!ClientUtils.sendSignUpRequest(username, password)) {
            setTextSignUpInfoPanel("something went wrong, couldn't send sign up form");
            return;
        }
        listenFromServer(event);
    }

    private void setTextSignUpInfoPanel(String textInfo) {
        Text text = new Text();
        text.setFill(Color.RED);
        text.setWrappingWidth(fxSignUpInfoPanel.getWidth());
        text.setTextAlignment(TextAlignment.CENTER);
        text.setText(textInfo);
        fxSignUpInfoPanel.getChildren().add(text);
    }

    private void clearSignUpInfoPanel() {
        fxSignUpInfoPanel.getChildren().clear();
    }

    private void listenFromServer(Event event) {
        Thread thread = new Thread(getSignUpTask(event));
        thread.setName("SignUp Thread");
        thread.start();
    }

    private Task<Boolean> getSignUpTask(Event event) {

        BorderPane loadingScreen = getLoadingScreen();

        final String[] responseInfo = new String[1];

        Task<Boolean> signupTask = new Task<>() {
            @Override
            protected Boolean call() throws Exception {
                String response;
                do {
                    response = ClientUtils.receiveResponse();
                } while (response == null || !ClientRequests.SIGN_UP.toString().equals(response));
                boolean isLogged = (Boolean) ClientUtils.receiveData();
                responseInfo[0] = (String) ClientUtils.receiveData();
                return isLogged;
            }
        };
        signupTask.setOnRunning(workerStateEvent -> Platform.runLater(() -> showLoadingScreen(loadingScreen)));

        signupTask.setOnSucceeded(workerStateEvent -> {
            try {
                Platform.runLater(() -> hideLoadingScreen(loadingScreen));
                if (signupTask.get()) {
                    log.info("signed up successfully");
                    showSignUpSuccessfulScreen();
                } else {
                    setTextSignUpInfoPanel(responseInfo[0]);
                }
            } catch (InterruptedException | ExecutionException e) {
                log.error(e);
            } finally {
                ClientService.closeConnection();
            }
        });
        return signupTask;
    }

    private void showSignUpSuccessfulScreen() {
        fxSignUpSuccessful.setVisible(true);
    }

    private void switchToLoginWindow(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(LOGIN_FXML_PATH));
            Scene scene = new Scene(loader.load());
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            log.error("switchToLoginWindow() " + e);
        }
    }

    private boolean isSignUpFormValid(String username, String email, String password, String repeatPassword) {
        boolean isValid = true;

        if (!Pattern.matches(USERNAME_PATTERN, username)) {
            setTextSignUpInfoPanel("username is incorrect");
            isValid = false;
        }
        if (!Pattern.matches(EMAIL_PATTERN, email)) {
            setTextSignUpInfoPanel("email is incorrect");
            isValid = false;
        }
        if (!Pattern.matches(PASSWORD_PATTERN, password)) {
            setTextSignUpInfoPanel("password is incorrect");
            isValid = false;
        }
        if (!password.equals(repeatPassword)) {
            setTextSignUpInfoPanel("passwords are not equal");
            isValid = false;
        }

        return isValid;
    }

    private void showLoadingScreen(Pane loadingScreen) {
        fxSignUpContainer.getChildren().add(loadingScreen);
    }

    private void hideLoadingScreen(Pane loadingScreen) {
        fxSignUpContainer.getChildren().remove(loadingScreen);
    }

    public BorderPane getLoadingScreen() {
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