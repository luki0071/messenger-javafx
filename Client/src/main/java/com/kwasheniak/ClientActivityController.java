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
import javafx.scene.text.Font;
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
    private BorderPane mainPane;
    @FXML
    private HBox bottomHBox;
    @FXML
    private ScrollPane scrollPane;
    @FXML
    private VBox vBox;
    @FXML
    private TextArea textArea;
    @FXML
    private Button buttonSend;
    @FXML
    private Button buttonAdd;

    private boolean isLeft = true;

    private ArrayList<File> files;

    private static final int IMAGE_PREVIEW_HEIGHT = 150;
    private static final String HYPERLINK_FONT = "System Bold Italic";

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        files = new ArrayList<>();
        //Font.getFontNames().forEach(System.out::println);

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
                log.info(file.getName());
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
                if(file.getName().endsWith(".jpg") || file.getName().endsWith(".png")){
                    ImageView imageView = new ImageView(new Image(file.getAbsolutePath()));
                    imageView.setFitHeight(IMAGE_PREVIEW_HEIGHT);
                    imageView.setPreserveRatio(true);
                    messageLabel.setGraphic(imageView);
                }else {
                    messageLabel.setText(file.getName());
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
                    alert.setHeaderText(file.getName());
                    alert.show();
                });
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
