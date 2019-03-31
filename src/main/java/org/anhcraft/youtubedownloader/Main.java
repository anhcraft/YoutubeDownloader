package org.anhcraft.youtubedownloader;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import org.anhcraft.youtubedownloader.controllers.HomeController;
import org.anhcraft.youtubedownloader.utils.Storage;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class Main extends Application {
    public static Stage stage;
    private static Scene scene;
    private static boolean stopped;
    private static final ExecutorService asyncPool = Executors.newFixedThreadPool(3);

    public static void runSync(Runnable runnable){
        Platform.runLater(runnable);
    }

    public static void runAsync(Runnable runnable){
        asyncPool.submit(runnable);
    }

    public static void ask(String message, Consumer<Boolean> cancelled){
        Alert a = new Alert(Alert.AlertType.CONFIRMATION, message);
        a.setHeaderText(null);
        a.setTitle("YOUTUBE DOWNLOADER");
        a.showAndWait().ifPresent(buttonType -> cancelled.accept(buttonType.getButtonData().isDefaultButton()));
    }

    public static void info(String message){
        Alert a = new Alert(Alert.AlertType.INFORMATION, message);
        a.setHeaderText(null);
        a.setTitle("YOUTUBE DOWNLOADER");
        a.show();
    }

    public static void err(String message){
        Alert a = new Alert(Alert.AlertType.ERROR, message);
        a.setHeaderText(null);
        a.setTitle("YOUTUBE DOWNLOADER");
        a.show();
    }

    public static void loadFXML(String name, Object controller){
        try {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("/"+ name + ".fxml"));
            loader.setController(controller);
            scene.setRoot(loader.load());
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        Storage.init();

        stage = primaryStage;
        primaryStage.setTitle("YOUTUBE DOWNLOADER");

        scene = new Scene(new Group(), 600, 400);
        loadFXML("home", new HomeController());
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
        primaryStage.setOnCloseRequest(event -> {
            stopped = true;
            asyncPool.shutdownNow();
            Storage.destroy();
            System.exit(0);
        });
    }
}
