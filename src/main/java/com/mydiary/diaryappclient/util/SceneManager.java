package com.mydiary.diaryappclient.util;

import com.mydiary.diaryappclient.MainApplication;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import lombok.Setter;

import java.io.IOException;
import java.util.Objects;

public class SceneManager {

    /**
     * -- SETTER --
     *  Phương thức này phải được gọi một lần duy nhất trong lớp MainApplication
     *  để lưu lại cửa sổ chính (Stage).
     */
    @Setter
    private static Stage primaryStage;

    /**
     * Tải và chuyển sang một Scene mới từ một file FXML.
     *
     * @param fxmlFile Tên của file FXML (ví dụ: "login-view.fxml").
     * @param title    Tiêu đề mới cho cửa sổ.
     * @throws IOException Nếu không thể tải file FXML.
     */
    public static void switchScene(String fxmlFile, String title) throws IOException {
        // Tải file FXML từ thư mục /views/
        Parent root = FXMLLoader.load(Objects.requireNonNull(MainApplication.class.getResource("/views/" + fxmlFile)));

        // Tạo một Scene mới và đặt nó vào cửa sổ chính
        Scene scene = new Scene(root);
        primaryStage.setTitle(title);
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}