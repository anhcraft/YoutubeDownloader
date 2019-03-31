package org.anhcraft.youtubedownloader.utils;

public enum EntryType {
    ALL(null, "View all entries"),
    ALL_A(null, "View all audio entries"),
    ALL_V(null, "View all video entries"),
    A_MEDIUM("AUDIO_QUALITY_MEDIUM", "View all audio entries with medium quality"),
    A_LOW("AUDIO_QUALITY_LOW", "View all audio entries with low quality"),
    V_1080P("1080p", "View all video entries with quality 1080p"),
    V_720P("720p", "View all video entries with quality 720p"),
    V_480P("480p", "View all video entries with quality 480p"),
    V_360P("360p", "View all video entries with quality 360p"),
    V_240P("240p", "View all video entries with quality 240p");

    private String id;
    private String text;

    EntryType(String id, String text) {
        this.id = id;
        this.text = text;
    }

    @Override
    public String toString() {
        return text;
    }

    public String getId() {
        return id;
    }
}
