package com.mydiary.diaryappclient.controller;

import com.mydiary.diaryappclient.MainApplication;
import com.mydiary.diaryappclient.controller.interfaces.IClosable;
import com.mydiary.diaryappclient.model.domain.Entry;
import com.mydiary.diaryappclient.model.dto.EntryRequest;
import com.mydiary.diaryappclient.service.ApiClient;
import com.mydiary.diaryappclient.service.AuthService;
import com.mydiary.diaryappclient.service.CredentialManager;
import com.mydiary.diaryappclient.util.SceneManager;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.CacheHint;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.web.HTMLEditor;
import javafx.scene.web.WebView;
import javafx.util.Duration;
import org.kordamp.ikonli.javafx.FontIcon;

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
    @FXML private VBox sidebar;
    @FXML private FontIcon themeIcon;

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
    @FXML private HTMLEditor editorContentArea;
    @FXML private Button saveEntryButton;
    @FXML private Button cancelEditButton;

    private final ObservableList<Entry> entryList = FXCollections.observableArrayList();
    private Entry currentlyEditingEntry;
    private boolean isSidebarVisible = true;
    private boolean isDarkTheme = false;

    private final double sidebarPrefWidth = 320.0;

    @FXML
    public void initialize() {
        usernameLabel.setText(AuthService.getInstance().getUsername());
        switchContentView(ContentViewMode.PLACEHOLDER, false); // Bắt đầu với placeholder, không có animation
        setupEntriesListView();
        loadEntries();
    }

    @FXML
    private void toggleSidebar(ActionEvent event) {
        isSidebarVisible = !isSidebarVisible;

        // Thiết lập thông số animation
        final double DURATION = 150; // milliseconds
        final double START_OPACITY = isSidebarVisible ? 0 : 1;
        final double END_OPACITY = isSidebarVisible ? 1 : 0;
        final double START_TRANSLATE = isSidebarVisible ? -30 : 0;
        final double END_TRANSLATE = isSidebarVisible ? 0 : -30;

        // Kích hoạt hiển thị trước khi animation bắt đầu (nếu đang mở)
        if (isSidebarVisible) {
            sidebar.setVisible(true);
            sidebar.setManaged(true);
            sidebar.setOpacity(START_OPACITY);
            sidebar.setTranslateX(START_TRANSLATE);
        }

        // Tạo hiệu ứng song song
        ParallelTransition parallelTransition = new ParallelTransition();

        // Hiệu ứng fade (mờ dần)
        FadeTransition fadeTransition = new FadeTransition(Duration.millis(DURATION), sidebar);
        fadeTransition.setFromValue(START_OPACITY);
        fadeTransition.setToValue(END_OPACITY);

        // Hiệu ứng slide (trượt)
        TranslateTransition translateTransition = new TranslateTransition(Duration.millis(DURATION), sidebar);
        translateTransition.setFromX(START_TRANSLATE);
        translateTransition.setToX(END_TRANSLATE);
        translateTransition.setInterpolator(Interpolator.EASE_OUT);

        parallelTransition.getChildren().addAll(fadeTransition, translateTransition);

        // Xử lý sau khi animation hoàn thành
        parallelTransition.setOnFinished(e -> {
            if (!isSidebarVisible) {
                sidebar.setVisible(false);
                sidebar.setManaged(false);
            }
            sidebar.setTranslateX(0); // Reset vị trí
        });

        // Tối ưu hiệu năng
        sidebar.setCache(true);
        sidebar.setCacheHint(CacheHint.SPEED);

        parallelTransition.play();
    }

    @FXML
    private void toggleTheme(ActionEvent event) {
        isDarkTheme = !isDarkTheme;
        if (isDarkTheme) {
            rootStackPane.getStyleClass().add("dark");
            themeIcon.setIconLiteral("fas-sun");
        } else {
            rootStackPane.getStyleClass().remove("dark");
            themeIcon.setIconLiteral("fas-moon");
        }
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
            // Thêm lại snippet Label
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

                // Luôn reset trạng thái của cell
                getStyleClass().remove("primary-entry-cell");
                title.getStyleClass().remove("primary-entry-title");

                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    // Chỉ thực hiện các thao tác khi chắc chắn 'item' không null
                    String titleText = item.getTitle();
                    if (Boolean.TRUE.equals(item.isPrimary())) {
                        getStyleClass().add("primary-entry-cell");
                        title.setText("★ " + titleText);
                        if (!title.getStyleClass().contains("primary-entry-title")) {
                            title.getStyleClass().add("primary-entry-title");
                        }
                    } else {
                        title.setText(titleText);
                    }

                    if (item.getEntryDate() != null) {
                        date.setText(item.getEntryDate().format(DateTimeFormatter.ofPattern("dd MMMM, yyyy")));
                    }

                    // Cập nhật lại snippet
                    String plainContent = item.getContent().replaceAll("<[^>]*>", "");
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
            switchContentView(ContentViewMode.PLACEHOLDER, true);
            return;
        }
        entryTitleLabel.setText(entry.getTitle());
        if (entry.getEntryDate() != null) {
            entryDateLabel.setText(entry.getEntryDate().format(DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy")));
        }
        String contentWithLineBreaks = "<html><body style='white-space: pre-wrap; word-wrap: break-word;'>"
                + entry.getContent()
                + "</body></html>";
        entryContentView.getEngine().loadContent(contentWithLineBreaks);
        switchContentView(ContentViewMode.DETAIL, true);
    }

    private enum ContentViewMode { PLACEHOLDER, DETAIL, EDITOR }
    private void switchContentView(ContentViewMode mode, boolean animate) {
        Node targetNode = null;
        if (mode == ContentViewMode.PLACEHOLDER) targetNode = contentPlaceholder;
        if (mode == ContentViewMode.DETAIL) targetNode = entryDetailView;
        if (mode == ContentViewMode.EDITOR) targetNode = entryEditorView;

        for (Node child : contentPlaceholder.getParent().getChildrenUnmodifiable()) {
            child.setVisible(child.equals(targetNode));
            child.setManaged(child.equals(targetNode));
        }

        if (animate && targetNode != null) {
            FadeTransition ft = new FadeTransition(Duration.millis(300), targetNode);
            ft.setFromValue(0.0);
            ft.setToValue(1.0);
            ft.setInterpolator(Interpolator.EASE_IN); // Thêm gia tốc
            ft.play();
        }
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
        editorContentArea.setHtmlText("");
        switchContentView(ContentViewMode.EDITOR, false);
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

            // Thêm hiệu ứng fade-in cho modal
            FadeTransition fadeIn = new FadeTransition(Duration.millis(250), modalPane);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);
            fadeIn.setInterpolator(Interpolator.EASE_IN);
            fadeIn.play();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void closeModal() {
        // Thêm hiệu ứng fade-out cho modal
        FadeTransition fadeOut = new FadeTransition(Duration.millis(250), modalPane);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        fadeOut.setInterpolator(Interpolator.EASE_OUT);
        fadeOut.setOnFinished(e -> {
            modalPane.setVisible(false);
            modalPane.getChildren().clear();
        });
        fadeOut.play();
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
        editorContentArea.setHtmlText(selectedEntry.getContent());
        switchContentView(ContentViewMode.EDITOR, false);
    }

    @FXML
    void handleCancelEditButtonAction(ActionEvent event) {
        displayEntryDetails(entriesListView.getSelectionModel().getSelectedItem());
    }
    @FXML
    void handleSaveEntryButtonAction(ActionEvent event) {
        String title = editorTitleField.getText();
        String content = editorContentArea.getHtmlText();
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
            switchContentView(ContentViewMode.PLACEHOLDER, false); // Quay về placeholder
        }));

        saveTask.setOnFailed(e -> {
            saveEntryButton.setDisable(false);
            // ... Xử lý lỗi
        });

        new Thread(saveTask).start();
    }
}