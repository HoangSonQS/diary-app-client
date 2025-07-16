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

        System.out.println("DEBUG: Nút Tạo PIN đã nhấn. Chuẩn bị tạo Task.");


        // Tạo Task để gọi API trên luồng nền
        Task<Void> setPinTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                // Gọi API để backend lưu PIN
                System.out.println("DEBUG: Task setPin đang chạy...");
                ApiClient.getInstance().setPin(pin);
                System.out.println("DEBUG: Đã gọi API setPin xong.");
                // Sau khi set PIN thành công, lưu lại username ở local
                // để lần sau mở app sẽ vào thẳng màn hình nhập PIN
                String username = AuthService.getInstance().getUsername();
                CredentialManager.saveLastUser(username);
                System.out.println("DEBUG: Đã lưu user vào CredentialManager.");

                return null;
            }
        };

        // Xử lý khi Task thành công
        setPinTask.setOnSucceeded(e -> {
            System.out.println("DEBUG: Task BÁO THÀNH CÔNG (setOnSucceeded).");

            Platform.runLater(() -> {
                try {
                    System.out.println("DEBUG: Chuẩn bị chuyển sang main-view.fxml...");
                    SceneManager.switchScene("main-view.fxml", "Nhật ký của bạn");
                    System.out.println("DEBUG: Đã gọi lệnh chuyển màn hình.");
                } catch (Exception ex) {
                    System.out.println("DEBUG: LỖI BÊN TRONG setOnSucceeded!");
                    ex.printStackTrace();
                    showError("Lỗi nghiêm trọng khi chuyển màn hình.");
                }
            });
        });

        // Xử lý khi Task thất bại
        setPinTask.setOnFailed(e -> {
            System.out.println("DEBUG: Task BÁO THẤT BẠI (setOnFailed).");
            Throwable exception = setPinTask.getException();
            System.err.println("Lỗi trong Task: " + exception.getMessage());
            exception.printStackTrace(); // In ra toàn bộ lỗi
            Platform.runLater(() -> {
                showError(exception.getMessage());
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