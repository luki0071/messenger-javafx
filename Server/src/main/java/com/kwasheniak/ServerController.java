package com.kwasheniak;


import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
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
import java.util.Optional;
import java.util.ResourceBundle;

@Log4j2
public class ServerController implements Initializable {

    @FXML
    private BorderPane fxRootContainer;
    @FXML
    private ScrollPane fxScrollPane;
    @FXML
    private VBox fxMessageBoard;

    private static final int DEFAULT_PADDING_VALUE = 5;
    private static final double MESSAGE_LABEL_CORNER_RADIUS_VALUE = 5.0;
    private static final String HYPERLINK_FONT = "System Bold Italic";

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setAutoScrollMessageBoard();
    }

    public void showMessageOnMessageBoard(byte[] title, byte[] data) {
        BorderPane messageBlock = createMessageBlock(createMessageLabel(title, data));
        fxMessageBoard.getChildren().add(messageBlock);
    }

    private BorderPane createMessageBlock(Label messageLabel) {
        BorderPane messageBlock = new BorderPane();
        messageBlock.setPadding(new Insets(DEFAULT_PADDING_VALUE));
        messageLabel.setBackground(new Background(new BackgroundFill(Color.LIGHTGRAY, new CornerRadii(MESSAGE_LABEL_CORNER_RADIUS_VALUE), null)));
        messageBlock.setLeft(messageLabel);
        return messageBlock;
    }

    private Label createMessageLabel(byte[] title, byte[] data) {
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
        imageView.setFitHeight(calculatePreviewImageHeight());
        imageView.setPreserveRatio(true);
        return imageView;
    }

    private void showFileDownloadDialog(String fileName, byte[] data) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setContentText("Do tou want to download this file?");
        alert.setHeaderText(fileName);
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent()) {
            if (result.get() == ButtonType.OK) {
                showFileSaveDialog(alert.getOwner(), fileName, data);
            } else if (result.get() == ButtonType.CANCEL) {
                alert.close();
            }
        }
    }

    private void showFileSaveDialog(Window window, String fileName, byte[] data) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(new File("C:\\Users\\Kwasheniak\\Desktop"));
        fileChooser.setInitialFileName(fileName);
        File saveLocation = fileChooser.showSaveDialog(window);

        if (saveLocation != null) {
            try (FileOutputStream fileOutputStream = new FileOutputStream(saveLocation)) {
                fileOutputStream.write(data);
            } catch (IOException error) {
                log.error(error);
            }
        }
    }

    public void setAutoScrollMessageBoard() {
        fxMessageBoard.heightProperty().addListener(observable -> fxScrollPane.setVvalue(1.0));
        //scrollPane.vvalueProperty().bind(vBox.heightProperty());
    }

    public void setAutoResizableMessageLabel(Label messageFrame) {
        fxRootContainer.widthProperty().addListener(observable -> messageFrame.setMaxWidth(calculateMessageLabelMaxWidth()));
        //messageLabel.maxWidthProperty().bind(Bindings.createDoubleBinding(() -> fxRootBorderPane.getWidth()/2, fxRootBorderPane.widthProperty()));
    }

    public void setAutoResizableImageInMessageFrame(ImageView image, Label messageFrame) {
        messageFrame.graphicProperty().bind(Bindings.createObjectBinding(() -> {
            image.setFitHeight(calculatePreviewImageHeight());
            return image;
        }, fxRootContainer.widthProperty()));
    }

    public double calculatePreviewImageHeight() {
        return fxRootContainer.getWidth() / 5;
    }

    public double calculateMessageLabelMaxWidth() {
        return (fxRootContainer.getWidth() / 2) - 10.0;
    }

}
