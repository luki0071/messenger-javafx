package com.kwasheniak;

import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;

@Log4j2
public class ServerApplication extends Application{

    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage stage) throws Exception {

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/ServerActivity.fxml"));
        Scene scene = new Scene(loader.load());
        stage.setScene(scene);
        stage.show();

        final ServerActivityController controller = loader.getController();


        //Task allows to run a long task outside the GUI thread
        // (to avoid freezing application)
        new Thread(new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                try {
                    ServerCore.createServer(1234, controller);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                return null;
            }
        }).start();
        /*new Thread(() -> {
            try {
                ServerCore.createServer(1234, controller);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).start();*/
    }
}
