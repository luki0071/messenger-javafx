package com.kwasheniak;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.TextFlow;
import javafx.stage.FileChooser;
import lombok.extern.log4j.Log4j2;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

@Log4j2
public class ClientActivityController implements Initializable {

    @FXML
    AnchorPane mainPane;
    @FXML
    HBox bottomHBox;
    @FXML
    ScrollPane scrollPane;
    @FXML
    VBox vBox;
    @FXML
    TextFlow textFlow;
    @FXML
    Button buttonSend;
    @FXML
    Button buttonAdd;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        buttonSend.setOnAction(event -> {
            String text = textFlow.getAccessibleText();
            /*try {
                ClientCore.sendDataToServer(text);
                addMessageOnBoard();
            } catch (IOException e) {
                log.info("can't establish connect with server");
            }*/
            addMessageOnBoard();
            textFlow.setAccessibleText("");
        });

        buttonAdd.setOnAction(event -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("file chooser");
            File file = fileChooser.showOpenDialog(((Node)event.getSource()).getScene().getWindow());
            log.info(file.getAbsolutePath());
            addImageOnBoard(file);
        });

    }



    public AnchorPane createMessagePane(String text){
        AnchorPane messageAnchorPane = new AnchorPane();
        TextArea messageTextArea = new TextArea(text);
        messageTextArea.setBackground(new Background(new BackgroundFill(Color.AQUA,null,null)));
        messageAnchorPane.getChildren().add(messageTextArea);
        return messageAnchorPane;
    }

    public AnchorPane createImagePane(File file){
        AnchorPane messageAnchorPane = new AnchorPane();
        ImageView imageView = new ImageView(new Image(file.getAbsolutePath()));
        messageAnchorPane.getChildren().add(imageView);
        return messageAnchorPane;
    }

    public void addMessageOnBoard(){
        vBox.getChildren().add(createMessagePane(textFlow.getAccessibleText()));
    }

    public void addImageOnBoard(File file){
        vBox.getChildren().add(createImagePane(file));
    }
}
