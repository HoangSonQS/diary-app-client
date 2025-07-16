package com.mydiary.diaryappclient;

import com.mydiary.diaryappclient.controller.EnterPinController;
import com.mydiary.diaryappclient.service.ApiClient;
import com.mydiary.diaryappclient.service.CredentialManager;
import com.mydiary.diaryappclient.util.SceneManager;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.stage.Stage;
import java.io.IOException;

public class MainApplication extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        SceneManager.setPrimaryStage(stage);
        // Hiển thị màn hình "Đang tải..." ngay lập tức
        SceneManager.switchScene("loading-view.fxml", "Đang tải...");

        // Tạo một Task để chạy logic kiểm tra trên luồng nền
        Task<Void> startupTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                String lastUser = CredentialManager.loadLastUser();

                if (lastUser != null) {
                    // Nếu có người dùng cũ, gọi API để kiểm tra PIN
                    boolean hasPin = ApiClient.getInstance().hasPin(lastUser);

                    // Cập nhật giao diện trên luồng JavaFX
                    Platform.runLater(() -> {
                        try {
                            if (hasPin) {
                                EnterPinController controller = (EnterPinController) SceneManager.switchScene("enter-pin-view.fxml", "Mở khóa bằng PIN");
                                controller.initData(lastUser);
                            } else {
                                // Nếu người dùng cũ chưa có PIN, vẫn vào màn hình đăng nhập
                                SceneManager.switchScene("login-view.fxml", "Ultimate Diary - Đăng nhập");
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });

                } else {
                    // Nếu không có người dùng cũ, vào thẳng màn hình đăng nhập
                    Platform.runLater(() -> {
                        try {
                            SceneManager.switchScene("login-view.fxml", "Ultimate Diary - Đăng nhập");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
                }
                return null;
            }
        };

        // Bắt đầu chạy Task
        new Thread(startupTask).start();
    }

    public static void main(String[] args) {
        launch();
    }
}