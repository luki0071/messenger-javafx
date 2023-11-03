package com.kwasheniak.client;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;

import java.net.URL;
import java.util.ResourceBundle;


public class MenuUserLabelController implements Initializable {
    @FXML
    public HBox fxRootContainer;
    @FXML
    private BorderPane fxImageFrame;
    @FXML
    private ImageView fxUserImage;
    @FXML
    private Label fxUsername;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        fxRootContainer.setOnMouseEntered(mouseEvent -> fxRootContainer.setStyle("-fx-background-color: lightblue;"));
        fxRootContainer.setOnMouseExited(mouseEvent -> fxRootContainer.setStyle("-fx-background-color: #ccffff;"));
    }

    public void setImageFrameColor(String color) {
        this.fxImageFrame.setStyle("-fx-background-color: " + color + "; -fx-background-radius: 90;");
    }

    public void setUserImage(String filePath){
        this.fxUserImage.setImage(new Image(filePath));
    }

    public void setUsername(String username) {
        this.fxUsername.setText(username);
    }

    public void setOnClick(EventHandler<MouseEvent> event){
        fxRootContainer.setOnMouseClicked(event);
    }
}
