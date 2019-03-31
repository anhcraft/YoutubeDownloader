package org.anhcraft.youtubedownloader.utils;

import java.util.HashMap;

public class MimeUtils {
    private static final HashMap<String, String> map = new HashMap<>();

    static {
        map.put("video/webm", ".webm");
        map.put("audio/webm", ".webm");
        map.put("video/mp4", ".mp4");
        map.put("audio/mp4", ".m4a");
    }

    public static String getExt(String mime){
        return map.getOrDefault(mime.toLowerCase(), "");
    }
}
