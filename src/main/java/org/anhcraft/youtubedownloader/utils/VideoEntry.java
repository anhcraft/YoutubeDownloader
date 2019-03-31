package org.anhcraft.youtubedownloader.utils;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import org.anhcraft.spaciouslib.builders.ArrayBuilder;

public class VideoEntry {
    public static VideoEntry[] generate() {
        ArrayBuilder builder = new ArrayBuilder(VideoEntry.class);
        Storage.getVideos().forEach(info -> info.getUrls().forEach(url -> {
            VideoEntry entry = new VideoEntry();
            entry.audioOnly = new SimpleBooleanProperty(url.isAudioOnly());
            entry.quality = new SimpleStringProperty(url.getQuality());
            entry.mimeType = new SimpleStringProperty(url.getMimeType());
            entry.url = new SimpleStringProperty(url.getUrl());
            entry.title = new SimpleStringProperty(info.getTitle());
            builder.append(entry);
        }));
        return (VideoEntry[]) builder.build();
    }

    private SimpleStringProperty title;
    private SimpleStringProperty url;
    private SimpleStringProperty quality;
    private SimpleStringProperty mimeType;
    private SimpleBooleanProperty audioOnly;
    private SimpleBooleanProperty selected = new SimpleBooleanProperty();

    public String getTitle() {
        return title.get();
    }

    public SimpleStringProperty titleProperty() {
        return title;
    }

    public String getUrl() {
        return url.get();
    }

    public SimpleStringProperty urlProperty() {
        return url;
    }

    public String getQuality() {
        return quality.get();
    }

    public SimpleStringProperty qualityProperty() {
        return quality;
    }

    public String getMimeType() {
        return mimeType.get();
    }

    public SimpleStringProperty mimeTypeProperty() {
        return mimeType;
    }

    public boolean isAudioOnly() {
        return audioOnly.get();
    }

    public SimpleBooleanProperty audioOnlyProperty() {
        return audioOnly;
    }

    public boolean isSelected() {
        return selected.get();
    }

    public SimpleBooleanProperty selectedProperty() {
        return selected;
    }
}
