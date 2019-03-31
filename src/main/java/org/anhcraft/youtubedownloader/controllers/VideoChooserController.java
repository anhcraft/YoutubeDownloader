package org.anhcraft.youtubedownloader.controllers;

import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import org.anhcraft.spaciouslib.builders.ArrayBuilder;
import org.anhcraft.spaciouslib.utils.Paginator;
import org.anhcraft.youtubedownloader.Main;
import org.anhcraft.youtubedownloader.utils.EntryType;
import org.anhcraft.youtubedownloader.utils.MimeUtils;
import org.anhcraft.youtubedownloader.utils.Storage;
import org.anhcraft.youtubedownloader.utils.VideoEntry;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

public class VideoChooserController implements Initializable {
    private Paginator<VideoEntry> paginator;
    @FXML
    private Pagination pagination;
    @FXML
    private ChoiceBox<EntryType> filter;
    private final VideoEntry[] GENERATED_ENTRIES = VideoEntry.generate();
    private boolean[] checkAllStatus;

    private void initPaginator(){
        ArrayBuilder array = new ArrayBuilder(VideoEntry.class);
        for(VideoEntry entry : GENERATED_ENTRIES){
            if(filter.getValue() == EntryType.ALL_A){
                if(!entry.isAudioOnly()){
                    continue;
                }
            } else if(filter.getValue() == EntryType.ALL_V){
                if(entry.isAudioOnly()){
                    continue;
                }
            } else if(filter.getValue() != EntryType.ALL) {
                if(!filter.getValue().getId().equals(entry.getQuality())) {
                    continue;
                }
            }
            array.append(entry);
        }
        paginator = new Paginator<>((VideoEntry[]) array.build(), 10);
        pagination.setPageCount(paginator.pages());
        checkAllStatus = new boolean[paginator.pages()];
        pagination.setPageFactory(page -> {
            TableView<VideoEntry> tableView = new TableView<>();

            TableColumn<VideoEntry, CheckBox> entryCheckbox = new TableColumn<>();
            entryCheckbox.setCellValueFactory(ent -> {
                CheckBox checkBox = new CheckBox();
                checkBox.setCursor(Cursor.HAND);
                checkBox.selectedProperty().bindBidirectional(ent.getValue().selectedProperty());
                return new SimpleObjectProperty<>(checkBox);
            });
            entryCheckbox.setPrefWidth(30);
            CheckBox checkAll = new CheckBox();
            checkAll.setCursor(Cursor.HAND);
            checkAll.selectedProperty().set(checkAllStatus[page]);
            checkAll.setOnAction(e -> {
                checkAllStatus[page] = checkAll.isSelected();
                tableView.getItems()
                    .forEach(p -> p.selectedProperty().set(checkAll.isSelected()));
            });
            entryCheckbox.setGraphic(checkAll);

            TableColumn<VideoEntry, String> videoTitle = new TableColumn<>("Title");
            videoTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
            videoTitle.setPrefWidth(300);

            TableColumn<VideoEntry, String> entryQuality = new TableColumn<>("Quality");
            entryQuality.setCellValueFactory(new PropertyValueFactory<>("quality"));
            entryQuality.setPrefWidth(120);

            TableColumn<VideoEntry, String> mimeType = new TableColumn<>("Mime Type");
            mimeType.setCellValueFactory(new PropertyValueFactory<>("mimeType"));
            mimeType.setPrefWidth(120);

            tableView.getColumns().addAll(entryCheckbox, videoTitle, entryQuality, mimeType);

            if(page < paginator.pages()) {
                tableView.setItems(FXCollections.observableArrayList(paginator.go(page).get()));
            }
            return new BorderPane(tableView);
        });
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        filter.setValue(EntryType.valueOf(Storage.getOption("video-chooser-filter", "ALL")));
        initPaginator();
        filter.getItems().addAll(EntryType.values());
        filter.setOnAction(event -> {
            Storage.setOption("video-chooser-filter", filter.getValue().name());
            initPaginator();
            pagination.setCurrentPageIndex(0);
        });
    }

    @FXML
    private void start(MouseEvent event){
        VideoDownloaderController c = new VideoDownloaderController();
        for(VideoEntry e : GENERATED_ENTRIES){
           if(e.isSelected()){
               c.getDownloadMap().put(e.getUrl(), new File(Storage.DOWNLOADS, e.getTitle()+MimeUtils.getExt(e.getMimeType().split(";")[0])));
           }
        }
        Main.loadFXML("videoDownloader", c);
    }

    @FXML
    private void back(MouseEvent event){
        Main.loadFXML("home", new HomeController());
    }
}
