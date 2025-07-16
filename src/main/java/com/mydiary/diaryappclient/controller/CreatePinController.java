package com.mydiary.diaryappclient.controller;

import com.mydiary.diaryappclient.controller.components.PinFieldController;
import com.mydiary.diaryappclient.service.ApiClient;
import com.mydiary.diaryappclient.service.AuthService;
import com.mydiary.diaryappclient.service.CredentialManager;
import com.mydiary.diaryappclient.util.SceneManager;
import com.mydiary.diaryappclient.util.SecurityUtil;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import java.io.IOException;

public class CreatePinController {

    @FXML private Label errorLabel;
    @FXML private Button createPinButton;


    @FXML
    private PinFieldController pinFieldComponentController;

    @FXML
    public void initialize() {
        // Đăng ký hành động tự động gửi
        if (pinFieldComponentController != null) {
            pinFieldComponentController.setOnPinComplete(() -> createPinButton.fire());
        }
    }


    @FXML
    void handleCreatePinAction(ActionEvent event) {
        String pin = pinFieldComponentController.getPin();

        if (pin.isEmpty() || !pin.matches("\\d{6}")) {
            showError("Mã PIN phải là 6 chữ số.");
            return;
        }

        createPinButton.setDisable(true);
        createPinButton.setText("Đang lưu...");
        errorLabel.setVisible(false);

        // Tạo Task để gọi API trên luồng nền
        Task<Void> setPinTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                ApiClient.getInstance().setPin(pin);
                return null;
            }
        };

        setPinTask.setOnSucceeded(e -> {
            Platform.runLater(() -> {
                try {
                    // Sau khi set PIN thành công, lưu lại thông tin local và vào màn hình chính
                    String username = AuthService.getInstance().getUsername();
                    CredentialManager.saveLastUser(username); // Lưu lại để lần sau mở app sẽ hỏi PIN
                    SceneManager.switchScene("main-view.fxml", "Nhật ký của bạn");
                } catch (Exception ex) {
                    ex.printStackTrace();
                    showError("Lỗi nghiêm trọng khi chuyển màn hình.");
                }
            });
        });

        setPinTask.setOnFailed(e -> {
            Platform.runLater(() -> {
                showError(setPinTask.getException().getMessage());
                createPinButton.setDisable(false);
                createPinButton.setText("Xác nhận và Lưu");
            });
        });

        new Thread(setPinTask).start();
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }
}