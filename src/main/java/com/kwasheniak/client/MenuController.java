package com.kwasheniak.client;

import javafx.application.Platform;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.net.URL;
import java.util.Comparator;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.TreeMap;

@Log4j2
public class MenuController implements Initializable {

    public static final String CLIENT_FXML = "/Chat.fxml";
    public static final String LOGIN_FXML = "/Login.fxml";
    public Button fxLogoutButton;
    @FXML
    private VBox fxChatContainer;
    @FXML
    private VBox fxUsersContainer;
    private TreeMap<String, Boolean> usernameList;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        fxLogoutButton.setOnAction(event -> {
            ClientService.closeConnection();
            if (!ClientService.isConnectedToServer()) {
                switchToLoginWindow(event);
            }
        });
        /*usernameList = new TreeMap<>((o1, o2) -> {
            int value = usernameList.get(o2).compareTo(usernameList.get(o1));
            if(value == 0){
                return o1.compareTo(o2);
            }
            return value;
        });*/
        getUsersStatus();
        listenFromServer();
    }

    private void getUsersStatus() {
        try {
            ClientUtils.sendUsernameListRequest();
        } catch (IOException e) {
            log.error("couldn't send request");
        }
    }

    @SuppressWarnings("unchecked")
    public void listenFromServer() {
        Thread thread = new Thread(() -> {
            try {
                while (ClientService.isConnectedToServer()) {
                    String response = ClientUtils.receiveResponse();
                    if (response == null)
                        continue;
                    if (ClientRequests.USERNAMES_LIST.toString().equals(response)) {
                        Object list = ClientUtils.receiveData();
                        if (list instanceof TreeMap<?, ?>) {
                            //usernameList.putAll((Map<String, Boolean>) list);
                            /*HashMap<String, Boolean> map = usernameList.entrySet().stream().sorted(Map.Entry.comparingByValue())
                                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, HashMap::new));*/
                            updateUsernameList(sortByValues((Map<String, Boolean>) list));
                        }
                        continue;
                    }
                    if (ClientRequests.START_CHAT.toString().equals(response)) {

                    }
                }
            } catch (IOException e) {
                log.error(e);
            }
        });
        thread.setName("Menu Thread");
        thread.start();
    }

   /* public void startListenServer(){
        listenForUsernames = new Thread(() -> {
            try{
                while(ClientService.isConnectedToServer()){
                    String response = new String(ClientService.getObjectInputStream().readNBytes(ClientService.getObjectInputStream().readInt()));
                    if(ClientRequests.USERNAMES_LIST.toString().equals(response)){
                        int usernamesCount = ClientService.getObjectInputStream().readInt();
                        for (int i = 0; i < usernamesCount; i++) {
                            String username = new String(ClientService.getObjectInputStream().readNBytes(ClientService.getObjectInputStream().readInt()));
                            String status = new String(ClientService.getObjectInputStream().readNBytes(ClientService.getObjectInputStream().readInt()));
                            users.put(username, status);
                        }
                        updateUsernameList();
                        log.info("username list updated");
                        continue;
                    }
                    if(ClientRequests.STOP_LISTEN.toString().equals(response)){
                        break;
                    }
                }
            }catch (IOException e){
                log.error(e);
            }


        });
        listenForUsernames.start();
    }*/

    private void updateUsernameList(Map<String, Boolean> map) {
        Platform.runLater(() -> fxUsersContainer.getChildren().clear());
        map.forEach((username, status) -> Platform.runLater(() -> fxUsersContainer.getChildren().add(addUserPanel(username, status))));
    }

    /*private Task<Boolean> getUsernamesListTask() {

        Task<Boolean> task = new Task<>() {
            @Override
            protected Boolean call() throws IOException {
                int usernamesCount = ClientService.getDataInputStream().readInt();
                for (int i = 0; i < usernamesCount; i++) {
                    String username = new String(ClientService.getDataInputStream().readNBytes(ClientService.getDataInputStream().readInt()));
                    String status = new String(ClientService.getDataInputStream().readNBytes(ClientService.getDataInputStream().readInt()));
                    users.put(username, status);
                }
                return true;
            }
        };

        task.setOnSucceeded(workerStateEvent -> {
            users.forEach((username, status) -> Platform.runLater(() -> fxUsersContainer.getChildren().add(addUserPanel(username, status))));
        });
        return task;
    }*/

    public HBox addUserPanel(String username, Boolean status) {
        ImageView imageView = new ImageView(new Image("images/user-image.png"));
        imageView.setFitHeight(30);
        imageView.setPickOnBounds(true);
        imageView.setPreserveRatio(true);

        BorderPane imagePanel = new BorderPane();
        imagePanel.setPadding(new Insets(5));
        String style = "-fx-background-radius: 90; -fx-background-color: ";
        style = status ? style + "lightgreen;" : style + "grey;";
        imagePanel.setStyle(style);
        imagePanel.setCenter(imageView);

        Label usernameLabel = new Label(username);
        usernameLabel.setAlignment(Pos.TOP_LEFT);
        usernameLabel.setContentDisplay(ContentDisplay.CENTER);
        usernameLabel.setFont(new Font(17));
        usernameLabel.setPadding(new Insets(0, 5, 5, 0));

        HBox panel = new HBox();
        panel.setAlignment(Pos.CENTER_LEFT);
        panel.getChildren().addAll(imagePanel, usernameLabel);

        return panel;
    }

    public void switchToClientChatWindow(Event event, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(CLIENT_FXML));
            Scene scene = new Scene(loader.load());
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Conversation with " + title);
            stage.show();
            stage.setOnCloseRequest(windowEvent -> ClientService.closeConnection());
        } catch (IOException e) {
            log.error(e);
        }
    }

    public void switchToLoginWindow(Event event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(LOGIN_FXML));
            Scene scene = new Scene(loader.load());
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            log.error(e);
        }
    }

    /*public static <K, V extends Comparable<V>> Map<K, V> sortByValues(final Map<K, V> map) {
        Comparator<K> valueComparator = (k1, k2) -> {
            int value = map.get(k2).compareTo(map.get(k1));
            if(value == 0){
                return k1.toString().compareTo(k2.toString());
            }
            return value;
        };

        Map<K, V> sortedByValues = new TreeMap<>(valueComparator);
        sortedByValues.putAll(map);
        return sortedByValues;
    }*/

    public static Map<String, Boolean> sortByValues(final Map<String, Boolean> map) {
        Comparator<String> valueComparator = (k1, k2) -> {
            int value = map.get(k2).compareTo(map.get(k1));
            if (value == 0) {
                return k1.compareTo(k2);
            }
            return value;
        };

        Map<String, Boolean> sortedByValues = new TreeMap<>(valueComparator);
        sortedByValues.putAll(map);
        return sortedByValues;
    }

}
