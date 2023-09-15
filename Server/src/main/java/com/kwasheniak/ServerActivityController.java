package com.kwasheniak;


import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import lombok.extern.log4j.Log4j2;

import java.net.URL;
import java.util.ResourceBundle;

@Log4j2
public class ServerActivityController implements Initializable {

    @FXML
    AnchorPane mainPane;
    @FXML
    HBox bottomHBox;
    @FXML
    ScrollPane scrollPane;
    @FXML
    VBox vBox;
    @FXML
    TextArea textArea;
    @FXML
    Button buttonSend;
    @FXML
    Button buttonAdd;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        buttonSend.setOnAction(event -> {
            log.info("buttonSend clicked");
            addMessageOnBoard(textArea.getText());
            textArea.clear();
        });

    }

    public AnchorPane createMessagePane(String text){
        AnchorPane messageAnchorPane = new AnchorPane();
        TextArea messageTextArea = new TextArea(text);
        messageTextArea.setBackground(new Background(new BackgroundFill(Color.AQUA,null,null)));
        messageAnchorPane.getChildren().add(messageTextArea);
        return messageAnchorPane;
    }

    public void addMessageOnBoard(String text){
        vBox.getChildren().add(createMessagePane(text));
    }
}
