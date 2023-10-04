package com.kwasheniak;


import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import lombok.extern.log4j.Log4j2;

import java.net.URL;
import java.util.ResourceBundle;

@Log4j2
public class ServerController implements Initializable {

    @FXML
    private BorderPane fxRootContainer;
    @FXML
    private ScrollPane fxScrollPane;
    @FXML
    public TextArea fxLogsBoard;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setAutoScrollMessageBoard();
    }
    public void setAutoScrollMessageBoard() {
        fxScrollPane.vvalueProperty().bind(fxLogsBoard.heightProperty());
    }

    public void addLogOnLogsBoard(String log){
        fxLogsBoard.appendText(log + "\n");
    }

}
