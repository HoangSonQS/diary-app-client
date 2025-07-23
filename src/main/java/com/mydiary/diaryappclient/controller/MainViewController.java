package com.mydiary.diaryappclient.controller;

import com.mydiary.diaryappclient.MainApplication;
import com.mydiary.diaryappclient.controller.interfaces.IClosable;
import com.mydiary.diaryappclient.model.domain.Entry;
import com.mydiary.diaryappclient.service.ApiClient;
import com.mydiary.diaryappclient.service.AuthService;
import com.mydiary.diaryappclient.service.CredentialManager;
import com.mydiary.diaryappclient.util.SceneManager;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class MainViewController {

    // FXML Components
    @FXML private StackPane rootStackPane;
    @FXML private StackPane modalPane;
    @FXML private Label usernameLabel;
    @FXML private ListView<Entry> entriesListView;
    @FXML private VBox contentPlaceholder;
    @FXML private VBox entryDetailView;
    @FXML private Label entryTitleLabel;
    @FXML private Label entryDateLabel;
    @FXML private WebView entryContentView;

    private final ObservableList<Entry> entryList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        usernameLabel.setText(AuthService.getInstance().getUsername());
        showPlaceholder(true);
        setupEntriesListView();
        loadEntries();
    }

    private void loadEntries() {
        Task<List<Entry>> loadEntriesTask = new Task<>() {
            @Override
            protected List<Entry> call() throws Exception {
                return ApiClient.getInstance().getEntries();
            }
        };

        loadEntriesTask.setOnSucceeded(e -> {
            Platform.runLater(() -> {
                entryList.setAll(loadEntriesTask.getValue());
                if (!entryList.isEmpty()) {
                    entriesListView.getSelectionModel().selectFirst();
                } else {
                    showPlaceholder(true);
                }
            });
        });

        loadEntriesTask.setOnFailed(e -> loadEntriesTask.getException().printStackTrace());
        new Thread(loadEntriesTask).start();
    }

    private void setupEntriesListView() {
        entriesListView.setItems(entryList);
        entriesListView.setCellFactory(lv -> new ListCell<>() {
            private final Label title = new Label();
            private final Label date = new Label();
            private final Label snippet = new Label();
            private final VBox vbox = new VBox(title, date, snippet);

            {
                title.getStyleClass().add("entry-cell-title");
                date.getStyleClass().add("entry-cell-date");
                snippet.getStyleClass().add("entry-cell-snippet");
                VBox.setMargin(date, new Insets(2, 0, 2, 0));
            }

            @Override
            protected void updateItem(Entry item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    title.setText(item.getTitle());
                    if (item.getEntryDate() != null) {
                        date.setText(item.getEntryDate().format(DateTimeFormatter.ofPattern("dd MMMM, yyyy")));
                    }                    String plainContent = item.getContent().replaceAll("<[^>]*>", "");
                    snippet.setText(plainContent.substring(0, Math.min(plainContent.length(), 100)) + "...");
                    setGraphic(vbox);
                }
            }
        });

        entriesListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                displayEntryDetails(newVal);
            }
        });
    }

    private void displayEntryDetails(Entry entry) {
        showPlaceholder(false);
        entryTitleLabel.setText(entry.getTitle());
        if (entry.getEntryDate() != null) {
            entryDateLabel.setText(entry.getEntryDate().format(DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy")));
        }        entryContentView.getEngine().loadContent(entry.getContent());
    }

    private void showPlaceholder(boolean show) {
        contentPlaceholder.setVisible(show);
        contentPlaceholder.setManaged(show);
        entryDetailView.setVisible(!show);
        entryDetailView.setManaged(!show);
    }

    @FXML
    void handleLogoutButtonAction(ActionEvent event) throws IOException {
        AuthService.getInstance().clearAuthToken();
        CredentialManager.deleteLastUser();
        SceneManager.switchScene("login-view.fxml", "Ultimate Diary - Đăng nhập", false);
    }

    @FXML
    void handleSetPinButtonAction(ActionEvent event) {
        loadModal("create-pin-view.fxml");
    }

    @FXML
    void handleNewEntryButtonAction(ActionEvent event) {
        loadModal("new-entry-view.fxml");
    }

    private void loadModal(String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(MainApplication.class.getResource("/views/" + fxmlFile));
            Node modalContent = loader.load();

            if (loader.getController() instanceof IClosable) {
                ((IClosable) loader.getController()).setOnClose(this::closeModal);
            }

            modalPane.getChildren().setAll(modalContent);
            modalPane.setVisible(true);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void closeModal() {
        modalPane.setVisible(false);
        modalPane.getChildren().clear();
        // Tùy chọn: Tải lại danh sách entry sau khi thêm/sửa
        loadEntries();
    }
}