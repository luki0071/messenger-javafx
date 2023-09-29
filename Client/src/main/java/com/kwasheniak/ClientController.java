package com.kwasheniak;

import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
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

import java.io.*;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

@Log4j2
public class ClientController implements Initializable {

    @FXML
    private BorderPane fxRootContainer;
    @FXML
    private HBox bottomHBox;
    @FXML
    private ScrollPane fxScrollPane;
    @FXML
    private VBox fxMessageBoard;
    @FXML
    private TextArea fxWrittingTextArea;
    @FXML
    private Button fxSendMessageButton;
    @FXML
    private Button fxAddFileButton;

    private File fileToSend;
    private static final int DEFAULT_PADDING_VALUE = 5;
    private static final double MESSAGE_LABEL_CORNER_RADIUS_VALUE = 5.0;
    private static final String HYPERLINK_FONT = "System Bold Italic";

    private ClientCore clientCore;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        setAutoScrollMessageBoard();

        fxSendMessageButton.setOnAction(event -> {
            String text = fxWrittingTextArea.getText();
            fxWrittingTextArea.clear();
            if (!text.isEmpty()) {
                try {
                    byte[][] data = clientCore.sendMessage(text);
                    addMessageToMessageBoard(MessageStatus.SENT, data);
                } catch (IOException e) {
                    log.info("can't establish connect with server");
                }
            }
        });

        fxAddFileButton.setOnAction(event -> {
            fileToSend = chooseFileToSend(((Node) event.getSource()).getScene().getWindow());
            if (fileToSend != null) {
                log.info(fileToSend.getAbsolutePath());
                try {
                    byte[][] data = clientCore.sendMessage(fileToSend);
                    addMessageToMessageBoard(MessageStatus.SENT, data);
                    fileToSend = null;
                } catch (IOException e) {
                    log.info("can't establish connect with server");
                }
            }
        });
    }

    public void setClientCore(ClientCore clientCore) {
        this.clientCore = clientCore;
    }

    public File chooseFileToSend(Window window) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("file chooser");
        fileChooser.setInitialDirectory(new File("C:\\Users\\Kwasheniak\\Documents\\Różne\\zrózne\\Obrazy"));
        return fileChooser.showOpenDialog(window);
    }

    public void addMessageToMessageBoard(MessageStatus status, byte[][] messageData) {
        BorderPane messageBlock = createMessageBlock(status, createMessageLabel(messageData[0], messageData[1]));
        fxMessageBoard.getChildren().add(messageBlock);
    }

    private BorderPane createMessageBlock(MessageStatus status, TextFlow messageLabel) {
        BorderPane messageBlock = new BorderPane();
        messageBlock.setPadding(new Insets(DEFAULT_PADDING_VALUE));
        if (MessageStatus.RECEIVED.equals(status)) {
            messageLabel.setBackground(new Background(new BackgroundFill(Color.LIGHTGRAY, new CornerRadii(MESSAGE_LABEL_CORNER_RADIUS_VALUE), null)));
            messageBlock.setLeft(messageLabel);
        } else {
            messageLabel.setBackground(new Background(new BackgroundFill(Color.LIGHTBLUE, new CornerRadii(MESSAGE_LABEL_CORNER_RADIUS_VALUE), null)));
            messageBlock.setRight(messageLabel);
        }
        return messageBlock;
    }

    /*private Label createMessageLabel(byte[] title, byte[] data) {
        String fileName = new String(title);
        Label messageLabel = new Label();
        messageLabel.setMaxWidth(calculateMessageLabelMaxWidth());
        messageLabel.setAlignment(Pos.TOP_LEFT);
        messageLabel.setWrapText(true);
        messageLabel.setPadding(new Insets(DEFAULT_PADDING_VALUE));
        setAutoResizableMessageLabel(messageLabel);
        if (fileName.isEmpty()) {
            String textMessage = new String(data);
            messageLabel.setText(textMessage);
        } else {
            if (fileName.endsWith(".jpg") || fileName.endsWith(".png")) {
                Image image = getImageFromData(data);
                ImageView imageView = getImageViewOfImage(image);
                messageLabel.setGraphic(imageView);
                setAutoResizableImageInMessageFrame(imageView, messageLabel);
            } else {
                messageLabel.setText(fileName);
                messageLabel.setUnderline(true);
                messageLabel.setFont(new Font(HYPERLINK_FONT, messageLabel.getFont().getSize()));
                messageLabel.setOnMouseEntered(mouseEvent -> messageLabel.setTextFill(Color.MEDIUMBLUE));
                messageLabel.setOnMouseExited(mouseEvent -> messageLabel.setTextFill(Color.BLACK));
            }
            messageLabel.setOnMouseClicked(mouseEvent -> showFileDownloadDialog(fileName, data));
        }
        return messageLabel;
    }*/

    private TextFlow createMessageLabel(byte[] name, byte[] data) {
        TextFlow messageLabel = new TextFlow();
        messageLabel.setMaxWidth(calculateMessageLabelMaxWidth());
        messageLabel.setTextAlignment(TextAlignment.LEFT);
        messageLabel.setPadding(new Insets(DEFAULT_PADDING_VALUE));
        setAutoResizableMessageLabel(messageLabel);
        String fileName = new String(name);
        if (fileName.isEmpty()) {
            addTextToMessageLabel(data, messageLabel);
        } else {
            addDownloadableContentToMessageLabel(fileName, data, messageLabel);
        }
        return messageLabel;
    }

    private void addTextToMessageLabel(byte[] data, TextFlow messageLabel){
        String textMessage = new String(data);
        messageLabel.getChildren().add(new Text(textMessage));
    }
    private void addDownloadableContentToMessageLabel(String fileName, byte[] data, TextFlow messageLabel){
        if (fileName.endsWith(".jpg") || fileName.endsWith(".png")) {
            Image image = getImageFromData(data);
            ImageView imageView = getImageViewOfImage(image);
            messageLabel.getChildren().add(imageView);
            setAutoResizableImageInMessageFrame(imageView);
        } else {
            Text text = new Text(fileName);
            text.setUnderline(true);
            text.setFont(new Font(HYPERLINK_FONT, text.getFont().getSize()));
            messageLabel.setOnMouseEntered(mouseEvent -> text.setFill(Color.MEDIUMBLUE));
            messageLabel.setOnMouseExited(mouseEvent -> text.setFill(Color.BLACK));
            messageLabel.getChildren().add(text);
        }
        messageLabel.setOnMouseClicked(mouseEvent -> showFileDownloadDialog(fileName, data));
    }

    private Image getImageFromData(byte[] data) {
        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(data)) {
            return new Image(byteArrayInputStream);
        } catch (IOException e) {
            log.error(e);
        }
        return null;
    }

    private ImageView getImageViewOfImage(Image image) {
        ImageView imageView = new ImageView(image);
        imageView.setFitHeight(calculateImageViewHeight());
        imageView.setPreserveRatio(true);
        return imageView;
    }

    public void showFileDownloadDialog(String fileName, byte[] data) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setContentText("Do tou want to download this file?");
        alert.setHeaderText(fileName);
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isEmpty())
            return;
        if (result.get() == ButtonType.OK) {
            showFileSaveDialog(alert.getOwner(), fileName, data);
            return;
        }
        if (result.get() == ButtonType.CANCEL) {
            alert.close();
        }
    }

    public void showFileSaveDialog(Window window, String fileName, byte[] data){
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(new File("C:\\Users\\Kwasheniak\\Desktop"));
        fileChooser.setInitialFileName(fileName);
        File saveLocation = fileChooser.showSaveDialog(window);

        if (saveLocation != null) {
            try (FileOutputStream fileOutputStream = new FileOutputStream(saveLocation)) {
                fileOutputStream.write(data);
            } catch (IOException error) {
                log.error("can't write data into file: " + error);
            }
        }
    }

    public void setAutoScrollMessageBoard() {
        fxMessageBoard.heightProperty().addListener((observableValue, oldValue, newValue) -> fxScrollPane.setVvalue((Double) newValue));
    }

    public void setAutoResizableMessageLabel(TextFlow messageFrame) {
        fxRootContainer.widthProperty().addListener(observable -> messageFrame.setMaxWidth(calculateMessageLabelMaxWidth()));
    }

    public void setAutoResizableImageInMessageFrame(ImageView image) {
        image.fitHeightProperty().bind(Bindings.createDoubleBinding(this::calculateImageViewHeight, fxRootContainer.heightProperty()));

        /*messageFrame.graphicProperty().bind(Bindings.createObjectBinding(() -> {
            image.setFitHeight(calculatePreviewImageHeight());
            return image;
        }, fxRootContainer.widthProperty()));*/
    }

    public double calculateImageViewHeight() {
        //return fxRootContainer.getWidth() / 5;
        return fxRootContainer.getHeight() / 5;
    }

    public double calculateMessageLabelMaxWidth() {
        return fxRootContainer.getWidth() - 20;
    }
}
