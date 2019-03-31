package org.anhcraft.youtubedownloader.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import org.anhcraft.youtubedownloader.Main;
import org.anhcraft.youtubedownloader.utils.Storage;

import java.net.URL;
import java.util.ResourceBundle;

public class SettingsController implements Initializable {
    @FXML
    private TextField accessToken;

    @FXML
    private void back(MouseEvent event){
        Storage.setOption("accessToken", accessToken.getText());
        Main.loadFXML("home", new HomeController());
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        accessToken.setText(Storage.getOption("accessToken"));
    }
}
