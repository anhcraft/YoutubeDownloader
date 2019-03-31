package org.anhcraft.youtubedownloader.controllers;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;
import org.anhcraft.spaciouslib.io.FileManager;
import org.anhcraft.spaciouslib.utils.Group;
import org.anhcraft.spaciouslib.utils.IOUtils;
import org.anhcraft.youtubedownloader.Main;
import org.anhcraft.youtubedownloader.utils.Storage;
import org.anhcraft.youtubedownloader.utils.Utils;
import org.anhcraft.youtubedownloader.utils.VideoInfo;
import org.anhcraft.youtubedownloader.utils.VideoURL;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HomeController implements Initializable {
    @FXML
    private TextArea videoIds;
    @FXML
    private ProgressBar progressBar;
    private boolean isRunning = false;

    private Group<String, List<VideoURL>> getVideoInfo(String id) {
        try {
            List<VideoURL> result = new ArrayList<>();
            String data = Utils.decodeBase64(Utils.decodeBase64(IOUtils.toString(new URL("https://www.youtube.com/get_video_info?html5=1&video_id=" + id + "&el=detailpage"))));
            if(data != null) {
                if(data.contains("\"status\":\"LOGIN_REQUIRED\"")) {
                    String embedPage = IOUtils.toString(new URL("https://www.youtube.com/embed/" + id));
                    Pattern p = Pattern.compile("\"sts\"\\s*:\\s*(\\d+)");
                    Matcher m = p.matcher(embedPage);
                    if(m.find()) {
                        data = Utils.decodeBase64(Utils.decodeBase64(IOUtils.toString(new URL("https://www.youtube.com/get_video_info?html5=1&video_id=" + id + "&eurl=https://youtube.googleapis.com/v/" + id + "&sts="+m.group(1)))));
                        if(data == null) {
                            return null;
                        }
                    }
                }
                String title = data.split("\"title\":\"")[1].split("\"")[0].replaceAll("[\\\\/:*?\"<>|]", "_");
                data = data.split("\"formats\":")[1];
                data = data.split(",\"playbackTracking\":")[0];
                data = "{\"a\":" + data;
                JsonObject jo = new Gson().fromJson(data, JsonObject.class);
                JsonArray ja = jo.getAsJsonArray(jo.has("adaptiveFormats") ? "adaptiveFormats" : "a");
                ja.forEach(jsonElement -> {
                    JsonObject e = jsonElement.getAsJsonObject();
                    String url = e.getAsJsonPrimitive("url").getAsString();
                    String mimeType = e.getAsJsonPrimitive("mimeType").getAsString();
                    if(mimeType.contains("video")) {
                        String quality = e.getAsJsonPrimitive("qualityLabel").getAsString();
                        result.add(new VideoURL(url, quality, mimeType, false));
                    } else {
                        String quality = e.getAsJsonPrimitive("audioQuality").getAsString();
                        result.add(new VideoURL(url, quality, mimeType, true));
                    }
                });
                return new Group<>(title, result);
            }
        } catch(Exception e) {
            e.printStackTrace();
            Main.runSync(() -> Main.err("Error: "+e.getCause().getMessage()));
        }
        return null;
    }

    @FXML
    private void discoverPlaylist(MouseEvent event){
        if(isRunning){
            Main.err("Please wait until the process is done!");
            return;
        }
        Storage.setOption("videoIds", videoIds.getText());
        Main.loadFXML("playlistDiscover", new PlaylistDiscoverController());
    }

    @FXML
    private void settings(MouseEvent event){
        if(isRunning){
            Main.err("Please wait until the process is done!");
            return;
        }
        Storage.setOption("videoIds", videoIds.getText());
        Main.loadFXML("settings", new SettingsController());
    }

    @FXML
    private void discoverVideos(MouseEvent event){
        if(isRunning) {
            Main.err("Another process is running. Please wait!");
            return;
        }
        progressBar.setProgress(0);
        isRunning = true;
        Storage.setOption("videoIds", videoIds.getText());
        Main.ask("Should we ignore discovered videos?", accepted -> Main.runAsync(() -> new Thread(() -> {
            String[] videoIds = Storage.getOption("videoIds").split("\n");
            for(int i = 0; i < videoIds.length; i++){
                String id = videoIds[i];
                if(id.matches("^[A-Za-z0-9-_]+$")){
                    if(!Storage.hasVideo(id) || !accepted){
                        Group<String, List<VideoURL>> info = getVideoInfo(id);
                        if(info != null){
                            VideoInfo vif = new VideoInfo(id, info.getA());
                            vif.getUrls().addAll(info.getB());
                            Storage.setVideo(vif);
                        }
                    }
                }
                progressBar.setProgress(1d/videoIds.length*(i+1));
            }
            isRunning = false;
            Main.runSync(() -> Main.info("Done!"));
        }).start()));
    }

    @FXML
    private void downloadVideos(MouseEvent event){
        if(isRunning) {
            Main.err("Another process is running. Please wait!");
            return;
        }
        Storage.setOption("videoIds", videoIds.getText());
        Main.loadFXML("videoChooser", new VideoChooserController());
    }

    @FXML
    private void loadList(MouseEvent event){
        FileChooser chooser = new FileChooser();
        String p = Storage.getOption("load_path");
        if(!p.isEmpty()){
            chooser.setInitialDirectory(new File(p));
        }
        File f = chooser.showOpenDialog(Main.stage);
        if(f != null){
            Main.info("Loaded the list!");
            try {
                String x = new FileManager(f).readAsString();
                videoIds.setText(x);
            } catch(IOException e) {
                e.printStackTrace();
            }
            Storage.setOption("load_path", f.getParentFile().getPath());
        }
    }

    @FXML
    private void saveList(MouseEvent event){
        FileChooser chooser = new FileChooser();
        String p = Storage.getOption("save_path");
        if(!p.isEmpty()){
            chooser.setInitialDirectory(new File(p));
        }
        File f = chooser.showSaveDialog(Main.stage);
        if(f != null){
            Main.info("Saved the list!");
            try {
                new FileManager(f).write(videoIds.getText());
            } catch(IOException e) {
                e.printStackTrace();
            }
            Storage.setOption("save_path", f.getParentFile().getPath());
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        videoIds.setText(Storage.getOption("videoIds"));
    }
}
