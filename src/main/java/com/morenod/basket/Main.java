package com.morenod.basket;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.net.URL;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/ui/dashboard.fxml"));
        
        Scene scene = new Scene(root, 1000, 650);
        
        URL cssResource = getClass().getResource("/ui/style.css");
        if (cssResource != null) {
            scene.getStylesheets().add(cssResource.toExternalForm());
        } else {
            System.out.println("WARNING: Could not find style.css inside src/main/resources/ui/");
        }

        primaryStage.setTitle("BASKET - We organize your food donations.");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}