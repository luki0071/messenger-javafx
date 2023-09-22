package com.kwasheniak;


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
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import lombok.extern.log4j.Log4j2;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Optional;
import java.util.ResourceBundle;

@Log4j2
public class ServerActivityController implements Initializable {

    @FXML
    private BorderPane fxRootContainer;
    @FXML
    private ScrollPane fxScrollPane;
    @FXML
    private VBox fxMessagesContainer;

    private static final int IMAGE_PREVIEW_HEIGHT = 150;
    private static final String HYPERLINK_FONT = "System Bold Italic";

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setMessageAutoScroll();
    }

    public boolean isWindowOpen(){
        return fxRootContainer.getScene().getWindow().isShowing();
    }

    public BorderPane createMessagePane(byte[] dataType, byte[] data){
        BorderPane messageBorderPane = new BorderPane();
        messageBorderPane.setPadding(new Insets(5));

        messageBorderPane.setLeft(createMessageLabel(dataType, data));
        return messageBorderPane;
    }


    public Label createMessageLabel(byte[] dataType, byte[] data){
        String dataTypeName = new String(dataType);
        Label messageLabel = new Label();
        messageLabel.setMaxWidth(fxRootContainer.getWidth()/2);
        setAutoResizableWidthMessageLabel(messageLabel);

        messageLabel.setContentDisplay(ContentDisplay.TOP);
        messageLabel.setAlignment(Pos.TOP_LEFT);
        messageLabel.setWrapText(true);
        messageLabel.setBackground(new Background(new BackgroundFill(Color.LIGHTBLUE,new CornerRadii(5.0),null)));
        messageLabel.setPadding(new Insets(5));

        if(dataTypeName.startsWith("text")){
            messageLabel.setText(new String(data));
        }else{
            //String fileName = dataTypeName.split(":")[1];
            if(dataTypeName.endsWith(".jpg") || dataTypeName.endsWith(".png")){
                Image img = new Image(new ByteArrayInputStream(data));
                ImageView imageView = new ImageView(img);
                imageView.setFitHeight(IMAGE_PREVIEW_HEIGHT);
                imageView.setPreserveRatio(true);
                messageLabel.setGraphic(imageView);
            }else {
                messageLabel.setText(dataTypeName);
                messageLabel.setUnderline(true);
                messageLabel.setFont(new Font(HYPERLINK_FONT, messageLabel.getFont().getSize()));
                messageLabel.setOnMouseEntered(mouseEvent -> {
                    messageLabel.setFont(new Font("System Bold Italic", messageLabel.getFont().getSize()));
                    messageLabel.setTextFill(Color.MEDIUMBLUE);
                });
                messageLabel.setOnMouseExited(mouseEvent -> {
                    messageLabel.setFont(new Font(HYPERLINK_FONT, messageLabel.getFont().getSize()));
                    messageLabel.setTextFill(Color.BLACK);
                });
            }
            messageLabel.setOnMouseClicked(mouseEvent -> {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setContentText("Do tou want to download this file?");
                alert.setHeaderText(dataTypeName);
                Optional<ButtonType> result = alert.showAndWait();
                if(result.isPresent()){
                    if(result.get() == ButtonType.OK){
                        File downloadFile = new File("src/main/resources", dataTypeName);

                        try(FileOutputStream fileOutputStream = new FileOutputStream(downloadFile)){
                            fileOutputStream.write(data);
                        }catch (IOException error){
                            log.error(error);
                        }
                    } else if (result.get() == ButtonType.CANCEL) {
                        alert.close();
                    }
                }
            });

        }

        return messageLabel;
    }

    public void addMessageOnBoard(byte[] dataType, byte[] data){
        fxMessagesContainer.getChildren().add(createMessagePane(dataType, data));
    }

    public void setMessageAutoScroll(){
        fxMessagesContainer.heightProperty().addListener(observable -> fxScrollPane.setVvalue(1.0));
        //scrollPane.vvalueProperty().bind(vBox.heightProperty());
    }

    public void setAutoResizableWidthMessageLabel(Label messageLabel){
        fxRootContainer.widthProperty().addListener(observable -> messageLabel.setMaxWidth(fxRootContainer.getWidth()/2));
        //messageLabel.maxWidthProperty().bind(Bindings.createDoubleBinding(() -> fxRootBorderPane.getWidth()/2, fxRootBorderPane.widthProperty()));
    }
}
