package com.kwasheniak;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class ClientApplication extends Application {

    public static void main(String[] args) {
        launch();
    }

    private ClientCore clientCore;
    @Override
    public void start(Stage stage) throws Exception {

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/ClientActivity.fxml"));
        Scene scene = new Scene(loader.load());
        stage.setScene(scene);
        //stage.initStyle(StageStyle.UNDECORATED);

        clientCore = new ClientCore();
        stage.setOnCloseRequest(windowEvent -> clientCore.closeConnection());

        ClientActivityController controller = loader.getController();
        controller.setClientCore(clientCore);
        controller.listenForMessages();

        stage.show();

        setStageMinSize(stage);
    }

    private void setStageMinSize(Stage stage){
        stage.setHeight(stage.getScene().getHeight());
        stage.setWidth(stage.getScene().getWidth());
        stage.setMinHeight(stage.getScene().getHeight());
        stage.setMinWidth(stage.getScene().getWidth());
    }
}