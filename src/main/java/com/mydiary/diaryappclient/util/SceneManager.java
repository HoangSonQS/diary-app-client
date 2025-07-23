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

    @Setter
    private static Stage primaryStage;

    public static Object switchScene(String fxmlFile, String title) throws IOException {
        return switchScene(fxmlFile, title, false);
    }

    public static Object switchScene(String fxmlFile, String title, boolean maximize) throws IOException {
        // Luôn luôn phóng to nếu file được yêu cầu là màn hình chính
        if ("main-view.fxml".equals(fxmlFile)) {
            maximize = true;
        }
        FXMLLoader loader = new FXMLLoader(MainApplication.class.getResource("/views/" + fxmlFile));
        Parent root = loader.load();

        if (primaryStage.getScene() == null) {
            primaryStage.setScene(new Scene(root));
        } else {
            primaryStage.getScene().setRoot(root);
        }

        primaryStage.setTitle(title);

        primaryStage.setMaximized(maximize);
        primaryStage.show();

        if (!maximize) {
            primaryStage.sizeToScene();
            primaryStage.centerOnScreen();
        }

        return loader.getController();
    }
}