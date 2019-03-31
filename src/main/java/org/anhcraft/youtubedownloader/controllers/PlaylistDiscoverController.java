package org.anhcraft.youtubedownloader.controllers;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import org.anhcraft.spaciouslib.utils.IOUtils;
import org.anhcraft.youtubedownloader.Main;
import org.anhcraft.youtubedownloader.utils.Storage;

import java.net.URL;
import java.net.URLConnection;
import java.util.ResourceBundle;

public class PlaylistDiscoverController implements Initializable {
    private boolean isRunning = false;
    @FXML
    private TextField playlistId;
    @FXML
    private TextArea logs;

    private void indexPlaylist(String id, String nextPageToken, Runnable callback){
        try {
            URLConnection conn = new URL("https://www.googleapis.com/youtube/v3/playlistItems?playlistId="+id+"&maxResults=25&part=contentDetails"+(nextPageToken == null ? "" : "&pageToken="+nextPageToken)).openConnection();
            conn.setRequestProperty("Connection", "keep-alive");
            conn.setRequestProperty("Pragma", "no-cache");
            conn.setRequestProperty("Cache-Control", "no-cache");
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/69.0.3497.102 Safari/537.36");
            conn.setRequestProperty("Accept", "*/*");
            conn.setRequestProperty("Accept-Encoding", "gzip, deflate, br");
            conn.setRequestProperty("Accept-Language", "en-US,en;q=0.9");
            conn.setRequestProperty("Authorization", "Bearer "+ Storage.getOption("accessToken"));
            conn.setDoInput(true);
            conn.connect();
            String data = IOUtils.toString(conn.getInputStream());
            JsonObject obj = new Gson().fromJson(data, JsonObject.class);
            if(obj.has("items")){
                obj.getAsJsonArray("items").forEach(itemElem -> {
                    JsonObject item = itemElem.getAsJsonObject();
                    if(item.has("contentDetails")){
                        JsonObject contentDetails = item.getAsJsonObject("contentDetails");
                        if(contentDetails.has("videoId")){
                            String vid = contentDetails.getAsJsonPrimitive("videoId").getAsString();
                            logs.appendText(vid+"\n");
                            Storage.setOption("playlist-discover-logs", logs.getText());
                        }
                    }
                });
            }
            if(obj.has("nextPageToken")){
                indexPlaylist(id, obj.getAsJsonPrimitive("nextPageToken").getAsString(), callback);
            } else {
                callback.run();
            }
        } catch(Exception e) {
            Main.runSync(() -> Main.err("Error: "+e.getCause().getMessage()));
            Storage.setOption("playlist-discover-logs", logs.getText());
        }
    }

    @FXML
    private void startDiscover(MouseEvent event) {
        if(isRunning) {
            Main.err("Another process is running. Please wait!");
            return;
        }
        isRunning = true;
        Storage.setOption("playlist-discover-playlistId", playlistId.getText());
        Storage.setOption("playlist-discover-logs", logs.getText());
        logs.appendText("\n");
        Main.runAsync(() -> indexPlaylist(playlistId.getText(), null, () -> isRunning = false));
    }

    @FXML
    private void back(MouseEvent event){
        if(isRunning){
            Main.err("Please wait until the process is done!");
            return;
        }
        Storage.setOption("playlist-discover-playlistId", playlistId.getText());
        Storage.setOption("playlist-discover-logs", logs.getText());
        Main.loadFXML("home", new HomeController());
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        playlistId.setText(Storage.getOption("playlist-discover-playlistId"));
        logs.setText(Storage.getOption("playlist-discover-logs"));
    }
}
