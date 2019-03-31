package org.anhcraft.youtubedownloader.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;
import javafx.scene.input.MouseEvent;
import org.anhcraft.spaciouslib.io.FileManager;
import org.anhcraft.spaciouslib.utils.IOUtils;
import org.anhcraft.spaciouslib.utils.MathUtils;
import org.anhcraft.youtubedownloader.Main;
import org.anhcraft.youtubedownloader.utils.SpeedClock;
import org.anhcraft.youtubedownloader.utils.Storage;

import java.awt.*;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class VideoDownloaderController implements Initializable {
    @FXML
    private TextArea logs;
    @FXML
    private Label status;
    @FXML
    private ProgressBar progressBar1;
    @FXML
    private ProgressBar progressBar2;
    private static final HashMap<String, File> downloadMap = new HashMap<>();
    private static final SpeedClock CLOCK = new SpeedClock();
    private boolean isRunning = false;

    @FXML
    private void openFolder(MouseEvent event){
        try {
            Desktop.getDesktop().open(Storage.DOWNLOADS);
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void back(MouseEvent event){
        if(isRunning){
            Main.err("Please wait until the process is done!");
            return;
        }
        Main.loadFXML("videoChooser", new VideoChooserController());
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Main.ask("Should we ignore downloaded video?", ignored -> Main.runAsync(() -> {
            isRunning = true;
            Object[] data = downloadMap.entrySet().toArray();
            for(int i = 0; i < data.length; i++){
                Map.Entry<String, File> e = (Map.Entry<String, File>) data[i];
                if(!e.getValue().exists() || !ignored) {
                    logs.appendText("Source: " + e.getKey() + "\n");
                    logs.appendText("Destination: " + e.getValue().getPath() + "\n");
                    try {
                        HttpURLConnection conn = (HttpURLConnection) new URL(e.getKey()).openConnection();
                        conn.setRequestProperty("Access-Control-Allow-Origin", "*");
                        conn.setRequestProperty("Connection", "keep-alive");
                        conn.setRequestProperty("Pragma", "no-cache");
                        conn.setRequestProperty("Cache-Control", "no-cache");
                        conn.setRequestProperty("Origin", "https://www.youtube.com");
                        conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/69.0.3497.102 Safari/537.36");
                        conn.setRequestProperty("Accept", "*/*");
                        conn.setRequestProperty("Referer", "https://www.youtube.com/");
                        conn.setRequestProperty("Accept-Encoding", "gzip, deflate, br");
                        conn.setRequestProperty("Accept-Language", "en-US,en;q=0.9");
                        conn.setRequestProperty("Authorization", "Bearer "+ Storage.getOption("accessToken"));
                        conn.setDoInput(true);
                        conn.connect();

                        int length = conn.getContentLength();
                        double lengthKB = MathUtils.round(length / 1024d);
                        CLOCK.refresh();
                        byte[] byteArray = IOUtils.toByteArray(new BufferedInputStream(conn.getInputStream(), 1024), a -> {
                            progressBar1.setProgress(1d / length * a);
                            double kbPerMs = CLOCK.updateSpeed(a/1000d);
                            Main.runSync(() -> status.setText("Downloaded " + MathUtils.round(a / 1024d) + "/" +
                                    lengthKB + " KB (" +
                                    MathUtils.round(100d / length * a) + "%) | " +
                                    MathUtils.round(kbPerMs) + " KB/MS"));
                        });
                        new FileManager(e.getValue()).create().write(byteArray);
                    } catch(Exception ex) {
                        Main.runSync(() -> Main.err("Error: " + ex.getCause().getMessage()));
                        logs.appendText("Error: " + ex.getCause().getMessage());
                        ex.printStackTrace();
                        break;
                    }
                    logs.appendText("\n");
                }
                i++;
                progressBar2.setProgress(1d/downloadMap.size()*i);
            }
            isRunning = false;
        }));
    }

    public HashMap<String, File> getDownloadMap() {
        return downloadMap;
    }
}
