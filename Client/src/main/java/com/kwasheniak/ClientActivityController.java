package com.kwasheniak;

import javafx.beans.binding.Bindings;
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

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

@Log4j2
public class ClientActivityController implements Initializable {

    @FXML
    private BorderPane fxRootContainer;
    @FXML
    private HBox bottomHBox;
    @FXML
    private ScrollPane fxScrollPane;
    @FXML
    private VBox fxMessagesContainer;
    @FXML
    private TextArea fxWrittingTextArea;
    @FXML
    private Button fxSendMessageButton;
    @FXML
    private Button fxAddFileButton;

    private boolean isLeft = true;

    private File fileToSend;

    private static final int IMAGE_PREVIEW_HEIGHT = 150;
    private static final int MESSAGE_PANE_PADDING_VALUE = 5;
    private static final double MESSAGE_FRAME_CORNER_RADIUS_VALUE = 5.0;
    private static final String HYPERLINK_FONT = "System Bold Italic";

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        setMessageAutoScroll();

        fxSendMessageButton.setOnAction(event -> {
            String text = fxWrittingTextArea.getText();
            /*try {
                ClientCore.sendDataToServer(text);
                addMessageOnBoard();
            } catch (IOException e) {
                log.info("can't establish connect with server");
            }*/
            addMessageToMessagesContainer();
        });

        fxAddFileButton.setOnAction(event -> {
            fileToSend = chooseFileToSend(((Node) event.getSource()).getScene().getWindow());
            if (fileToSend != null) {
                log.info(fileToSend.getAbsolutePath());
                addMessageToMessagesContainer();
            }
        });
    }

    public BorderPane createMessageBlock() {
        BorderPane messageBlock = new BorderPane();
        messageBlock.setPadding(new Insets(MESSAGE_PANE_PADDING_VALUE));

        /*if (isLeft) {
            messageBlock.setLeft(createMessageLabel(text));
            isLeft = false;
        } else {
            messageBlock.setRight(createMessageLabel(text));
            isLeft = true;
        }*/
        messageBlock.setRight(createMessageFrame(fxWrittingTextArea.getText()));
        fxWrittingTextArea.clear();
        return messageBlock;
    }

    public Label createMessageFrame(String text) {
        Label messageFrame = new Label(text);
        messageFrame.setMaxWidth(fxRootContainer.getWidth() / 2);
        messageFrame.setAlignment(Pos.TOP_LEFT);
        messageFrame.setWrapText(true);
        messageFrame.setBackground(new Background(new BackgroundFill(Color.LIGHTBLUE, new CornerRadii(MESSAGE_FRAME_CORNER_RADIUS_VALUE), null)));
        messageFrame.setPadding(new Insets(MESSAGE_PANE_PADDING_VALUE));
        setAutoResizableMessageFrame(messageFrame);

        if (fileToSend != null)
            addFileToMessageFrame(fileToSend, messageFrame);

        return messageFrame;
    }

    public void addFileToMessageFrame(File file, Label messageFrame) {
        if (file.getName().endsWith(".jpg") || file.getName().endsWith(".png")) {
            ImageView imageView = new ImageView(new Image(file.getAbsolutePath()));
            imageView.setFitHeight(calculatePreviewImageHeight());
            imageView.setPreserveRatio(true);
            messageFrame.setGraphic(imageView);
            setAutoResizableImageInMessageFrame(imageView, messageFrame);

        } else {
            messageFrame.setText(file.getName());
            messageFrame.setUnderline(true);
            messageFrame.setFont(new Font(HYPERLINK_FONT, messageFrame.getFont().getSize()));
            messageFrame.setOnMouseEntered(mouseEvent -> {
                messageFrame.setFont(new Font(HYPERLINK_FONT, messageFrame.getFont().getSize()));
                messageFrame.setTextFill(Color.MEDIUMBLUE);
            });
            messageFrame.setOnMouseExited(mouseEvent -> {
                messageFrame.setFont(new Font(HYPERLINK_FONT, messageFrame.getFont().getSize()));
                messageFrame.setTextFill(Color.BLACK);
            });
        }
        messageFrame.setOnMouseClicked(mouseEvent -> showFileDownloadDialog(file));
        fileToSend = null;
    }

    public void showFileDownloadDialog(File file){
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setContentText("Do tou want to download this file?");
        alert.setHeaderText(file.getName());
        alert.show();
    }

    public void addMessageToMessagesContainer() {
        if (!"".equals(fxWrittingTextArea.getText()) || fileToSend != null) {
            fxMessagesContainer.getChildren().add(createMessageBlock());
        }
    }

    public void setMessageAutoScroll() {
        fxMessagesContainer.heightProperty().addListener(observable -> fxScrollPane.setVvalue(1.0));
        //scrollPane.vvalueProperty().bind(vBox.heightProperty());
    }

    public void setAutoResizableMessageFrame(Label messageFrame) {
        fxRootContainer.widthProperty().addListener(observable -> messageFrame.setMaxWidth(fxRootContainer.getWidth() / 2));
        //messageLabel.maxWidthProperty().bind(Bindings.createDoubleBinding(() -> fxRootBorderPane.getWidth()/2, fxRootBorderPane.widthProperty()));
    }

    public void setAutoResizableImageInMessageFrame(ImageView image, Label messageFrame){
        messageFrame.graphicProperty().bind(Bindings.createObjectBinding(() -> {
            image.setFitHeight(calculatePreviewImageHeight());
            return image;
        }, fxRootContainer.widthProperty()));
    }

    public File chooseFileToSend(Window window) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("file chooser");
        fileChooser.setInitialDirectory(new File("C:\\Users\\Kwasheniak\\Documents\\Różne\\zrózne\\Obrazy"));
        return fileChooser.showOpenDialog(window);
    }

    public double calculatePreviewImageHeight() {
        return fxRootContainer.getWidth() / 5;
    }
}