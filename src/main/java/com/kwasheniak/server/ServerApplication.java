package com.kwasheniak.server;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class ServerApplication extends Application {
    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage stage) throws Exception {

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/server/Server.fxml"));
        Scene scene = new Scene(loader.load());
        stage.setScene(scene);
        stage.show();

        stage.setOnCloseRequest(windowEvent -> ServerService.closeAllConnection());
    }
}
