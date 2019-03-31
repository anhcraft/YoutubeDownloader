package org.anhcraft.youtubedownloader.utils;

import org.anhcraft.spaciouslib.annotations.DataField;
import org.anhcraft.spaciouslib.annotations.Serializable;

import java.util.LinkedList;

@Serializable
public class VideoInfo {
    @DataField
    private String id;
    @DataField
    private String title;
    @DataField
    private LinkedList<VideoURL> urls = new LinkedList<>();

    public VideoInfo() {
    }

    public VideoInfo(String id, String title) {
        this.id = id;
        this.title = title;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public LinkedList<VideoURL> getUrls() {
        return urls;
    }
}
