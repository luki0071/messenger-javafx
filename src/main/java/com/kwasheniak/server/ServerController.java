package com.kwasheniak.server;

import javafx.fxml.Initializable;

import java.net.URL;
import java.util.ResourceBundle;

public class ServerController implements Initializable {

    private ServerService serverService;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        serverService = new ServerService(1234, this);
        serverService.createServer();
    }
}
