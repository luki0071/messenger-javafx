package com.kwasheniak;

import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;

@Log4j2
public class ServerApplication extends Application {

    private ServerCore serverCore;
    public static void main(String[] args) {
        launch();
    }
    @Override
    public void start(Stage stage) throws Exception {

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/ServerActivity.fxml"));
        Scene scene = new Scene(loader.load());
        stage.setScene(scene);
        stage.show();

        stage.setOnCloseRequest(windowEvent -> {
            serverCore.closeServer();
        });

        final ServerActivityController controller = loader.getController();

        new Thread(new Task<Void>() {
            @Override
            protected Void call() {
                serverCore = new ServerCore(1234, controller);
                serverCore.createServer();
                return null;
            }
        }).start();
    }
}
