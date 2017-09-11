package client.ui;

import client.network.BanchoClient;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class uitest extends Application {

    public static Runnable client;
    public static ExecutorService executorService;
    public static void main(String[] args) throws IOException {
        client = BanchoClient.Create();
        executorService = Executors.newSingleThreadExecutor();
        executorService.execute(client);
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("/ui/loginUI.fxml"));
        primaryStage.setTitle("Hello World");
        primaryStage.setScene(new Scene(root, 300, 275));
        primaryStage.show();
    }
}
