package com.mydiary.diaryappclient.controller;

import com.mydiary.diaryappclient.controller.interfaces.IClosable;
import com.mydiary.diaryappclient.model.domain.Entry;
import com.mydiary.diaryappclient.model.dto.EntryRequest;
import com.mydiary.diaryappclient.service.ApiClient;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class NewEntryController implements IClosable {

    @FXML private TextField titleField;
    @FXML private TextArea contentArea;
    @FXML private Button cancelButton;
    @FXML private Button saveButton;

    private Runnable onClose;
    private Entry entryToEdit; // Biến để lưu bài viết đang được sửa, sẽ là null nếu là bài viết mới

    @Override
    public void setOnClose(Runnable action) {
        this.onClose = action;
    }

    /**
     * Phương thức này sẽ được gọi từ MainViewController để truyền dữ liệu vào.
     * @param entry Bài viết cần chỉnh sửa. Nếu là null, đây là chế độ tạo mới.
     */
    public void initData(Entry entry) {
        this.entryToEdit = entry;
        if (entryToEdit != null) {
            // Chế độ chỉnh sửa: điền dữ liệu vào các ô
            titleField.setText(entryToEdit.getTitle());
            contentArea.setText(entryToEdit.getContent());
        }
        // Nếu entryToEdit là null, các ô sẽ trống (chế độ tạo mới)
    }

    @FXML
    void handleCancelButtonAction(ActionEvent event) {
        if (onClose != null) {
            onClose.run();
        }
    }

    @FXML
    void handleSaveButtonAction(ActionEvent event) {
        String title = titleField.getText();
        String content = contentArea.getText();

        if (title.isBlank()) {
            // Có thể thêm Alert báo lỗi ở đây
            System.err.println("Tiêu đề không được để trống");
            return;
        }

        EntryRequest request = new EntryRequest(title, content);

        // Tạo một Task chung để xử lý cả tạo mới và cập nhật
        Task<Entry> saveTask = new Task<>() {
            @Override
            protected Entry call() throws Exception {
                if (entryToEdit == null) {
                    // Chế độ tạo mới
                    return ApiClient.getInstance().createEntry(request);
                } else {
                    // Chế độ cập nhật
                    return ApiClient.getInstance().updateEntry(entryToEdit.getId(), request);
                }
            }
        };

        saveTask.setOnSucceeded(e -> {
            if (onClose != null) {
                Platform.runLater(onClose);
            }
        });

        saveTask.setOnFailed(e -> {
            Platform.runLater(() -> {
                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                errorAlert.setTitle("Lỗi");
                errorAlert.setHeaderText("Không thể lưu bài viết.");
                errorAlert.setContentText(saveTask.getException().getMessage());
                errorAlert.showAndWait();
            });
            saveTask.getException().printStackTrace();
        });

        new Thread(saveTask).start();
    }
}