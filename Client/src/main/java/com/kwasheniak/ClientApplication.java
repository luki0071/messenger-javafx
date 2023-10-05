package com.kwasheniak;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class ClientApplication extends Application {

    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage stage) throws Exception {

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Login.fxml"));
        Scene scene = new Scene(loader.load());
        stage.setScene(scene);
        stage.show();

        setStageMinSize(stage);
    }

    /**
     * sets min size of window
     *
     * @param stage object of window which displays content
     */
    private void setStageMinSize(Stage stage) {
        stage.setHeight(stage.getScene().getHeight());
        stage.setWidth(stage.getScene().getWidth());
        stage.setMinHeight(stage.getScene().getHeight());
        stage.setMinWidth(stage.getScene().getWidth());
    }
}