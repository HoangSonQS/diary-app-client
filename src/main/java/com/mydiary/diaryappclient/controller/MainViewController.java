package com.mydiary.diaryappclient.controller;

import com.mydiary.diaryappclient.MainApplication;
import com.mydiary.diaryappclient.controller.interfaces.IClosable;
import com.mydiary.diaryappclient.model.domain.Entry;
import com.mydiary.diaryappclient.model.dto.EntryRequest;
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
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class MainViewController {

    // --- FXML Components ---
    @FXML private StackPane rootStackPane;
    @FXML private StackPane modalPane;
    @FXML private Label usernameLabel;
    @FXML private ListView<Entry> entriesListView;

    // Panels
    @FXML private VBox contentPlaceholder;
    @FXML private VBox entryDetailView;
    @FXML private VBox entryEditorView;

    // Detail View Components
    @FXML private Label entryTitleLabel;
    @FXML private Label entryDateLabel;
    @FXML private WebView entryContentView;
    @FXML private Button editButton;
    @FXML private Button deleteButton;

    // Editor View Components
    @FXML private Label editorModeLabel;
    @FXML private TextField editorTitleField;
    @FXML private TextArea editorContentArea;
    @FXML private Button saveEntryButton;
    @FXML private Button cancelEditButton;

    private final ObservableList<Entry> entryList = FXCollections.observableArrayList();
    private Entry currentlyEditingEntry;

    @FXML
    public void initialize() {
        usernameLabel.setText(AuthService.getInstance().getUsername());
        switchContentView(ContentViewMode.PLACEHOLDER); // Bắt đầu với placeholder
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
            private final VBox vbox = new VBox(title, date);

            {
                title.getStyleClass().add("entry-cell-title");
                date.getStyleClass().add("entry-cell-date");
                VBox.setMargin(date, new Insets(2, 0, 2, 0));

                title.setWrapText(true);
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
        if (entry == null) {
            switchContentView(ContentViewMode.PLACEHOLDER);
            return;
        }
        entryTitleLabel.setText(entry.getTitle());
        if (entry.getEntryDate() != null) {
            entryDateLabel.setText(entry.getEntryDate().format(DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy")));
        }
        entryContentView.getEngine().loadContent(entry.getContent());
        switchContentView(ContentViewMode.DETAIL);
    }

    private enum ContentViewMode { PLACEHOLDER, DETAIL, EDITOR }

    private void switchContentView(ContentViewMode mode) {
        contentPlaceholder.setVisible(mode == ContentViewMode.PLACEHOLDER);
        contentPlaceholder.setManaged(mode == ContentViewMode.PLACEHOLDER);
        entryDetailView.setVisible(mode == ContentViewMode.DETAIL);
        entryDetailView.setManaged(mode == ContentViewMode.DETAIL);
        entryEditorView.setVisible(mode == ContentViewMode.EDITOR);
        entryEditorView.setManaged(mode == ContentViewMode.EDITOR);
    }

    private void showPlaceholder(boolean show) {
        setEntryActionButtonsVisible(!show);
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
    void handleNewEntryButtonAction(ActionEvent event) {
        currentlyEditingEntry = null;
        editorModeLabel.setText("Tạo bài viết mới");
        editorTitleField.clear();
        editorContentArea.clear();
        switchContentView(ContentViewMode.EDITOR);
    }

    @FXML
    void handleSetPinButtonAction(ActionEvent event) {
        loadModal("create-pin-view.fxml", IClosable.class, null);
    }

    private <T> void loadModal(String fxmlFile, Class<T> controllerType, Consumer<T> initializer) {
        try {
            FXMLLoader loader = new FXMLLoader(MainApplication.class.getResource("/views/" + fxmlFile));
            Node modalContent = loader.load();
            T controller = loader.getController();

            if (initializer != null) {
                initializer.accept(controller);
            }
            if (controller instanceof IClosable) {
                ((IClosable) controller).setOnClose(this::closeModal);
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
    }

    @FXML
    void handleDeleteButtonAction(ActionEvent event) {
        Entry selectedEntry = entriesListView.getSelectionModel().getSelectedItem();
        if (selectedEntry == null) {
            return; // Không có gì được chọn, không làm gì cả
        }

        // Tạo hộp thoại xác nhận
        Alert confirmationDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmationDialog.setTitle("Xác nhận Xóa");
        confirmationDialog.setHeaderText("Bạn có chắc chắn muốn xóa bài viết này không?");
        confirmationDialog.setContentText("Hành động này không thể hoàn tác.\n\n\"" + selectedEntry.getTitle() + "\"");

        // Hiển thị hộp thoại và chờ người dùng phản hồi
        Optional<ButtonType> result = confirmationDialog.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            // Nếu người dùng nhấn OK, thực hiện xóa
            deleteSelectedEntry(selectedEntry);
        }
    }

    private void deleteSelectedEntry(Entry entry) {
        Task<Void> deleteTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                ApiClient.getInstance().deleteEntry(entry.getId());
                return null;
            }
        };

        deleteTask.setOnSucceeded(e -> Platform.runLater(() -> {
            // Xóa bài viết khỏi danh sách trên giao diện và tải lại
            entryList.remove(entry);
            if(entryList.isEmpty()){
                showPlaceholder(true);
            }
        }));

        deleteTask.setOnFailed(e -> {
            // Xử lý lỗi (ví dụ: hiển thị Alert)
            Platform.runLater(() -> {
                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                errorAlert.setTitle("Lỗi");
                errorAlert.setHeaderText("Không thể xóa bài viết.");
                errorAlert.setContentText(deleteTask.getException().getMessage());
                errorAlert.showAndWait();
            });
            deleteTask.getException().printStackTrace();
        });

        new Thread(deleteTask).start();
    }

    // Phương thức mới để ẩn/hiện các nút Chỉnh sửa và Xóa
    private void setEntryActionButtonsVisible(boolean visible) {
        if (editButton != null && deleteButton != null) {
            editButton.setVisible(visible);
            editButton.setManaged(visible);
            deleteButton.setVisible(visible);
            deleteButton.setManaged(visible);
        }
    }

    @FXML
    void handleEditButtonAction(ActionEvent event) {
        Entry selectedEntry = entriesListView.getSelectionModel().getSelectedItem();
        if (selectedEntry == null) return;
        currentlyEditingEntry = selectedEntry;
        editorModeLabel.setText("Chỉnh sửa bài viết");
        editorTitleField.setText(selectedEntry.getTitle());
        editorContentArea.setText(selectedEntry.getContent());
        switchContentView(ContentViewMode.EDITOR);
    }

    @FXML
    void handleCancelEditButtonAction(ActionEvent event) {
        displayEntryDetails(entriesListView.getSelectionModel().getSelectedItem());
    }
    @FXML
    void handleSaveEntryButtonAction(ActionEvent event) {
        String title = editorTitleField.getText();
        String content = editorContentArea.getText();
        EntryRequest request = new EntryRequest(title, content);
        saveEntryButton.setDisable(true);

        Task<Entry> saveTask = new Task<>() {
            @Override
            protected Entry call() throws Exception {
                if (currentlyEditingEntry == null) {
                    return ApiClient.getInstance().createEntry(request);
                } else {
                    return ApiClient.getInstance().updateEntry(currentlyEditingEntry.getId(), request);
                }
            }
        };

        saveTask.setOnSucceeded(e -> Platform.runLater(() -> {
            saveEntryButton.setDisable(false);
            loadEntries(); // Tải lại danh sách
            switchContentView(ContentViewMode.PLACEHOLDER); // Quay về placeholder
        }));

        saveTask.setOnFailed(e -> {
            saveEntryButton.setDisable(false);
            // ... Xử lý lỗi
        });

        new Thread(saveTask).start();
    }
}