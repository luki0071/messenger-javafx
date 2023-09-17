package com.kwasheniak;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import lombok.extern.log4j.Log4j2;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

@Log4j2
public class ClientActivityController implements Initializable {

    @FXML
    BorderPane mainPane;
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

    public boolean isLeft = true;

    ArrayList<File> files;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        files = new ArrayList<>();

        setMessageAutoScroll();

        buttonSend.setOnAction(event -> {
            String text = textArea.getText();
            /*try {
                ClientCore.sendDataToServer(text);
                addMessageOnBoard();
            } catch (IOException e) {
                log.info("can't establish connect with server");
            }*/
            addMessageOnBoard();
        });

        buttonAdd.setOnAction(event -> {

            File file = getFileFromDialog(((Node)event.getSource()).getScene().getWindow());
            if(file != null){
                log.info(file.getAbsolutePath());
                files.add(file);
                addMessageOnBoard();
            }
        });

    }

    public BorderPane createMessagePane(String text){
        BorderPane messageBorderPane = new BorderPane();
        messageBorderPane.setPadding(new Insets(5));

        if(isLeft){
            log.info("left");
            messageBorderPane.setLeft(createMessageLabel(text));
            isLeft=false;
        }else{
            log.info("right");
            messageBorderPane.setRight(createMessageLabel(text));
            isLeft=true;
        }
        return messageBorderPane;
    }

    public Label createMessageLabel(String text){
        Label messageLabel = new Label();
        if(!"".equals(text)){
            messageLabel.setText(text);
        }
        messageLabel.setMaxWidth(mainPane.getWidth()/2);
        setAutoResizableWidthMessageLabel(messageLabel);
        if(!files.isEmpty()){

            files.forEach(file -> {
                ImageView imageView = new ImageView(new Image(file.getAbsolutePath()));
                imageView.setFitHeight(150);
                imageView.setPreserveRatio(true);
                messageLabel.setGraphic(imageView);
            });
            files.clear();
        }
        messageLabel.setContentDisplay(ContentDisplay.TOP);
        messageLabel.setAlignment(Pos.TOP_LEFT);
        messageLabel.setWrapText(true);
        messageLabel.setBackground(new Background(new BackgroundFill(Color.LIGHTBLUE,new CornerRadii(5.0),null)));
        messageLabel.setPadding(new Insets(5));
        return messageLabel;
    }

    public void addMessageOnBoard(){
        if(!"".equals(textArea.getText()) || !files.isEmpty()){
            vBox.getChildren().add(createMessagePane(textArea.getText()));
        }
        textArea.clear();
    }

    public void setMessageAutoScroll(){
        vBox.heightProperty().addListener(observable -> scrollPane.setVvalue(1.0));
        //scrollPane.vvalueProperty().bind(vBox.heightProperty());
    }

    public void setAutoResizableWidthMessageLabel(Label messageLabel){
        mainPane.widthProperty().addListener(observable -> messageLabel.setMaxWidth(mainPane.getWidth()/2));
    }

    public File getFileFromDialog(Window window){
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("file chooser");
        fileChooser.setInitialDirectory(new File("C:\\Users\\Kwasheniak\\Documents\\Różne\\zrózne\\Obrazy"));
        return fileChooser.showOpenDialog(window);
    }
}
