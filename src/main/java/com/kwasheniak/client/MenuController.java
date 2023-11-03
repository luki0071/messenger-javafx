package com.kwasheniak.client;

import com.kwasheniak.data.Requests;
import javafx.application.Platform;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.net.URL;
import java.util.*;

@Log4j2
public class MenuController implements Initializable {

    public static final String CHAT_FXML = "/client/Chat.fxml";
    public static final String LOGIN_FXML = "/client/Login.fxml";
    public static final String MENU_USER_LABEL_FXML = "/client/MenuUserLabel.fxml";
    public Button fxLogoutButton;
    @FXML
    public TabPane fxTabChats;
    @FXML
    private VBox fxChatContainer;
    @FXML
    private VBox fxUsersContainer;
    private TreeMap<String, ClientStatus> usernameList;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        fxLogoutButton.setOnAction(event -> {
            ClientService.closeConnection();
            if (!ClientService.isConnectedToServer()) {
                switchToLoginWindow(event);
            }
        });

        getUsersStatus();
        listenFromServer();
    }

    public static TreeMap<String, ClientStatus> sortByValues(final Map<String, ClientStatus> map) {
        Comparator<String> valueComparator = (k1, k2) -> {
            int value = map.get(k1).compareTo(map.get(k2));
            if (value == 0) {
                return k1.compareTo(k2);
            }
            return value;
        };

        TreeMap<String, ClientStatus> sortedByValues = new TreeMap<>(valueComparator);
        sortedByValues.putAll(map);
        return sortedByValues;
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
                    Requests response = ClientUtils.receiveResponse();
                    if (response == null)
                        continue;
                    if (Requests.USERNAMES_LIST.equals(response)) {
                        Object list = ClientUtils.receiveData();
                        if (list instanceof TreeMap<?, ?>) {
                            //usernameList.putAll((Map<String, Boolean>) list);
                            /*HashMap<String, Boolean> map = usernameList.entrySet().stream().sorted(Map.Entry.comparingByValue())
                                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, HashMap::new));*/
                            usernameList = sortByValues((TreeMap<String, ClientStatus>) list);
                            updateUsernameList(usernameList);
                        }
                        continue;
                    }
                    if (Requests.UPDATE_USER_STATUS.equals(response)) {
                        Object user = ClientUtils.receiveData();
                        if (user instanceof HashMap<?, ?>) {
                            usernameList.putAll((HashMap<String, ClientStatus>) user);
                            usernameList = sortByValues(usernameList);
                            updateUsernameList(usernameList);
                        }
                        continue;
                    }
                    if (Requests.START_CHAT.equals(response)) {

                    }
                }
            } catch (IOException e) {
                log.error(e);
            }
        });
        thread.setName("Menu Thread");
        thread.start();
    }

    private void updateUsernameList(Map<String, ClientStatus> map) {
        Platform.runLater(() -> fxUsersContainer.getChildren().clear());
        map.forEach((username, status) -> Platform.runLater(() -> {
            try {
                fxUsersContainer.getChildren().add(addUserPanel(username, status));
            } catch (IOException e) {
                log.error(e);
            }
        }));
    }

    public Parent addUserPanel(String username, ClientStatus status) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(MENU_USER_LABEL_FXML));
        Parent borderPane = loader.load();
        MenuUserLabelController menuUserLabelController = loader.getController();

        String color = ClientStatus.ONLINE.equals(status) ? "lightgreen;" : "grey;";
        menuUserLabelController.setImageFrameColor(color);
        menuUserLabelController.setUsername(username);
        menuUserLabelController.setOnClick(new EventHandler<>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource(CHAT_FXML));
                    Parent chatPane = loader.load();
                    Tab tab = new Tab(username);
                    tab.setContent(chatPane);
                    fxTabChats.getTabs().add(tab);
                } catch (IOException e) {
                    log.error(e);
                }
            }
        });

        return borderPane;
    }

    public void switchToClientChatWindow(Event event, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(CHAT_FXML));
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

}
