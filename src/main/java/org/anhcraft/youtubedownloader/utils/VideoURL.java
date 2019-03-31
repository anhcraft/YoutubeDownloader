package org.anhcraft.youtubedownloader.utils;

import org.anhcraft.spaciouslib.annotations.DataField;
import org.anhcraft.spaciouslib.annotations.Serializable;

@Serializable
public class VideoURL {
    @DataField
    private String url;
    @DataField
    private String quality;
    @DataField
    private String mimeType;
    @DataField
    private boolean audioOnly;

    public VideoURL() {

    }

    public VideoURL(String url, String quality, String mimeType, boolean audioOnly) {
        this.url = url;
        this.mimeType = mimeType;
        this.quality = quality;
        this.audioOnly = audioOnly;
    }

    public String getUrl() {
        return url;
    }

    public String getQuality() {
        return quality;
    }

    public boolean isAudioOnly() {
        return audioOnly;
    }

    public String getMimeType() {
        return mimeType;
    }
}
