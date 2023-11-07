package com.kwasheniak.client;

import com.kwasheniak.data.ChatMessage;
import com.kwasheniak.data.Requests;
import javafx.application.Platform;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
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
    private VBox fxUsersContainer;
    private TreeMap<String, ClientStatus> usernameList;
    private final HashMap<String, ChatController> conversations = new HashMap<>();

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

    public static TreeMap<String, ClientStatus> sortMapByValues(final Map<String, ClientStatus> map) {
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
                            usernameList = sortMapByValues((TreeMap<String, ClientStatus>) list);
                            updateUsernameList(usernameList);
                        }
                        continue;
                    }
                    if (Requests.UPDATE_USER_STATUS.equals(response)) {
                        Object user = ClientUtils.receiveData();
                        if (user instanceof HashMap<?, ?>) {
                            usernameList.putAll((HashMap<String, ClientStatus>) user);
                            usernameList = sortMapByValues(usernameList);
                            updateUsernameList(usernameList);
                        }
                        continue;
                    }
                    if (Requests.CHAT_MESSAGE.equals(response)) {
                        String messageFrom = (String) ClientUtils.receiveData();
                        ChatMessage message = (ChatMessage) ClientUtils.receiveData();
                        Platform.runLater(() -> {
                            ChatController controller = conversations.get(messageFrom);
                            if (controller == null) {
                                try {
                                    Tab tab = createConversationTab(messageFrom);
                                    tab.setStyle("-fx-font-weight: bold");
                                    fxTabChats.getTabs().add(tab);
                                } catch (IOException e) {
                                    log.error(e);
                                }
                                controller = conversations.get(messageFrom);
                            }
                            if (!fxTabChats.getSelectionModel().getSelectedItem().getText().equals(messageFrom)) {
                                Tab newMessageTab = fxTabChats.getTabs().stream()
                                        .filter(tab -> tab.getText().equals(messageFrom))
                                        .findFirst().get();
                                newMessageTab.setStyle("-fx-font-weight: bold");
                            }

                            controller.addMessageToMessageBoard(ChatController.RECEIVED, message);
                        });
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
        menuUserLabelController.setOnClick(mouseEvent -> openConversationTab(username));
        return borderPane;
    }

    private void openConversationTab(String conversationName) {
        Optional<Tab> conversationTab = fxTabChats.getTabs().stream()
                .filter(tab -> tab.getText().equals(conversationName))
                .findFirst();

        if (conversationTab.isPresent()) {
            fxTabChats.getSelectionModel().select(conversationTab.get());
            return;
        }
        try {
            Tab tab = createConversationTab(conversationName);
            fxTabChats.getTabs().add(tab);
            fxTabChats.getSelectionModel().select(tab);
        } catch (IOException e) {
            log.error(e);
        }
    }

    private Tab createConversationTab(String conversationName) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(CHAT_FXML));
        Parent chatPane = loader.load();
        ChatController controller = loader.getController();
        controller.setMessageReceiver(conversationName);
        conversations.put(conversationName, controller);
        Tab tab = new Tab(conversationName);
        tab.setContent(chatPane);
        tab.setOnClosed(event -> conversations.remove(conversationName));
        tab.setOnSelectionChanged(event -> {
            if (tab.isSelected()) {
                tab.setStyle("-fx-font-weight: regular");
            }
        });
        return tab;
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
}
