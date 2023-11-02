package com.kwasheniak.client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ClientApplication extends Application {
    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage stage) throws Exception {

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/client/Login.fxml"));
        Scene scene = new Scene(loader.load());
        stage.setScene(scene);
        stage.show();

    }
}
