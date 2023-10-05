package com.kwasheniak;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class ServerApplication extends Application {
    private ServerService serverService;

    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage stage) throws Exception {

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Server.fxml"));
        Scene scene = new Scene(loader.load());
        stage.setScene(scene);
        stage.show();

        //closes connection with server on window closing
        stage.setOnCloseRequest(windowEvent -> {
            serverService.closeAllConnection();
        });

        //server starts
        new Thread(() -> {
            serverService = new ServerService(1234, loader.getController());
            serverService.createServer();
        }).start();
    }
}
