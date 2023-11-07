package com.kwasheniak.client;

import com.kwasheniak.data.ChatMessage;
import com.kwasheniak.data.MessageType;
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
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

@Log4j2
public class ChatController implements Initializable {

    public static final String SENT = "sent";
    public static final String RECEIVED = "received";
    private static final String INITIAL_DIRECTORY = "C:\\Users\\Kwasheniak\\Desktop";
    public static final String MENU_FXML = "/client/Menu.fxml";
    private static final int DEFAULT_PADDING_VALUE = 5;
    private static final double MESSAGE_LABEL_CORNER_RADIUS_VALUE = 5.0;
    @FXML
    private BorderPane fxRootContainer;
    @FXML
    private HBox bottomHBox;
    @FXML
    private ScrollPane fxScrollPane;
    @FXML
    private VBox fxMessageBoard;
    @FXML
    private TextArea fxWritingTextArea;
    @FXML
    private Button fxSendMessageButton;
    @FXML
    private Button fxAddFileButton;
    private File fileToSend;

    @Setter
    private String messageReceiver;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        setAutoScrollMessageBoard();

        fxSendMessageButton.setOnAction(event -> {
            String text = fxWritingTextArea.getText();
            fxWritingTextArea.clear();
            if (text.isEmpty() || messageReceiver == null)
                return;
            log.info("messageReceiver " + messageReceiver);
            ChatMessage message = new ChatMessage(text);
            ClientUtils.sendMessage(messageReceiver, message);
            addMessageToMessageBoard(SENT, message);
        });

        fxAddFileButton.setOnAction(event -> {
            fileToSend = chooseFileToSend(((Node) event.getSource()).getScene().getWindow());
            if (fileToSend == null || messageReceiver == null)
                return;
            try {
                ChatMessage message = new ChatMessage(fileToSend);
                ClientUtils.sendMessage(messageReceiver, message);
                addMessageToMessageBoard(SENT, message);
            } catch (IOException e) {
                log.error(e);
            }
        });
    }

    public File chooseFileToSend(Window window) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("file chooser");
        fileChooser.setInitialDirectory(new File("C:\\Users\\Kwasheniak\\Documents\\Różne\\zrózne\\Obrazy"));
        return fileChooser.showOpenDialog(window);
    }

    public void addMessageToMessageBoard(String status, ChatMessage message) {
        BorderPane messageBlock = createMessageBlock(status, createMessageLabel(message));
        fxMessageBoard.getChildren().add(messageBlock);
    }

    private BorderPane createMessageBlock(String status, TextFlow messageLabel) {
        BorderPane messageBlock = new BorderPane();
        messageBlock.setPadding(new Insets(DEFAULT_PADDING_VALUE));
        if (RECEIVED.equals(status)) {
            messageLabel.setBackground(new Background(new BackgroundFill(Color.LIGHTGRAY, new CornerRadii(MESSAGE_LABEL_CORNER_RADIUS_VALUE), null)));
            messageBlock.setLeft(messageLabel);
        } else {
            messageLabel.setBackground(new Background(new BackgroundFill(Color.LIGHTBLUE, new CornerRadii(MESSAGE_LABEL_CORNER_RADIUS_VALUE), null)));
            messageBlock.setRight(messageLabel);
        }
        return messageBlock;
    }

    private TextFlow createMessageLabel(ChatMessage message) {
        TextFlow messageLabel = new TextFlow();
        messageLabel.setMaxWidth(calculateMessageLabelMaxWidth());
        messageLabel.setTextAlignment(TextAlignment.LEFT);
        messageLabel.setPadding(new Insets(DEFAULT_PADDING_VALUE));
        setAutoResizableMessageLabel(messageLabel);
        if (MessageType.TEXT.equals(message.getMessageType())) {
            messageLabel.getChildren().add(getTextNode(message.getData()));
            return messageLabel;
        }
        String filename = message.getFilename();
        if (filename.endsWith(".jpg") || filename.endsWith(".png")) {
            messageLabel.getChildren().add(getImageViewNode(filename, message.getData()));
        } else {
            messageLabel.getChildren().add(getHyperlinkNode(filename, message.getData()));
        }
        return messageLabel;
    }

    private Text getTextNode(byte[] data) {
        String textMessage = new String(data);
        return new Text(textMessage);
    }

    private ImageView getImageViewNode(String fileName, byte[] data) {
        Image image = getImageFromData(data);
        ImageView imageView = getImageViewOfImage(image);
        imageView.setOnMouseClicked(mouseEvent -> showFileDownloadDialog(fileName, data));
        setAutoResizableImageInMessageFrame(imageView);
        return imageView;
    }

    private Hyperlink getHyperlinkNode(String fileName, byte[] data) {
        Hyperlink hyperlink = new Hyperlink(fileName);
        hyperlink.setOnAction(event -> showFileDownloadDialog(fileName, data));
        return hyperlink;
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

    public void showFileSaveDialog(Window window, String fileName, byte[] data) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(new File(INITIAL_DIRECTORY));
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
        fxMessageBoard.heightProperty().addListener(
                (observableValue, oldValue, newValue) -> fxScrollPane.setVvalue((Double) newValue));
    }

    public void setAutoResizableMessageLabel(TextFlow messageFrame) {
        fxRootContainer.widthProperty().addListener(
                observable -> messageFrame.setMaxWidth(calculateMessageLabelMaxWidth()));
    }

    public void setAutoResizableImageInMessageFrame(ImageView image) {
        image.fitHeightProperty().bind(
                Bindings.createDoubleBinding(this::calculateImageViewHeight, fxRootContainer.heightProperty()));
    }

    public double calculateImageViewHeight() {
        return fxRootContainer.getHeight() / 5;
    }

    public double calculateMessageLabelMaxWidth() {
        return fxRootContainer.getWidth() - 20;
    }
}
