package org.anhcraft.youtubedownloader.utils;

import org.anhcraft.spaciouslib.annotations.DataField;
import org.anhcraft.spaciouslib.annotations.Serializable;
import org.anhcraft.spaciouslib.io.FileManager;
import org.anhcraft.spaciouslib.scheduler.TimerTask;
import org.anhcraft.spaciouslib.serialization.DataSerialization;
import org.anhcraft.spaciouslib.utils.GZipUtils;
import org.anhcraft.spaciouslib.utils.InitialisationValidator;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class Storage {
    @Serializable
    public static class Settings{
        @DataField
        public HashMap<String, String> options = new HashMap<>();
    }

    private static final File ROOT = new File(System.getProperty("user.home") +
            File.separator + "AppData" + File.separator + "Roaming" +
            File.separator + "YoutubeDownloader" + File.separator);
    private static final File SETTINGS = new File(ROOT, "settings");
    private static final File CACHE = new File(ROOT, "cache" + File.separator);
    public static final File DOWNLOADS = new File(ROOT, "downloads" + File.separator);
    private static final InitialisationValidator validator = new InitialisationValidator();
    private static final HashMap<String, VideoInfo> videos = new HashMap<>();
    private static final AtomicBoolean needSaveConfig = new AtomicBoolean();
    private static final ConcurrentLinkedQueue<String> videoSaver = new ConcurrentLinkedQueue<>();
    private static Settings settings;
    private static TimerTask task;

    public static void setVideo(VideoInfo info) {
        videos.put(info.getId(), info);
        if(!videoSaver.contains(info.getId())) {
            videoSaver.add(info.getId());
        }
    }

    public static boolean hasVideo(String videoId) {
        return videos.containsKey(videoId);
    }

    public static Collection<VideoInfo> getVideos() {
        return Collections.unmodifiableCollection(videos.values());
    }

    public static void setOption(String k, String v) {
        settings.options.put(k, v);
        synchronized(needSaveConfig) {
            needSaveConfig.set(true);
        }
    }

    public static String getOption(String k){
        return getOption(k, "");
    }

    public static String getOption(String k, String dv){
        return settings.options.getOrDefault(k, dv);
    }

    public static void init(){
        try {
            validator.validate();
        } catch(Exception e) {
            e.printStackTrace();
        }
        DOWNLOADS.mkdirs();
        CACHE.mkdir();
        if(SETTINGS.exists()) {
            try {
                settings = (Settings) DataSerialization.deserialize(
                        GZipUtils.decompress(new FileManager(SETTINGS).read()));
            } catch(Exception e) {
                e.printStackTrace();
            }
        } else {
            settings = new Settings();
        }
        File[] files = CACHE.listFiles();
        if(files != null) {
            try {
                for(File f : files) {
                    VideoInfo vif = (VideoInfo) DataSerialization.deserialize(
                            GZipUtils.decompress(new FileManager(f).read()));
                    videos.put(vif.getId(), vif);
                }
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
        task = new TimerTask(() -> {
            synchronized(needSaveConfig) {
                if(needSaveConfig.get()) {
                    try {
                        new FileManager(SETTINGS).create().write(
                                GZipUtils.compress(DataSerialization.serialize(settings).getA()));
                    } catch(IOException e) {
                        e.printStackTrace();
                    }
                    needSaveConfig.set(false);
                }
            }
            if(!videoSaver.isEmpty()){
                int sz = Math.min(videoSaver.size(), 5);
                for(int i = 0; i < sz; i++){
                    String vid = videoSaver.poll();
                    try {
                        assert vid != null;
                        new FileManager(new File(CACHE, vid)).create().write(
                                GZipUtils.compress(DataSerialization.serialize(videos.get(vid)).getA()));
                    } catch(IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }, 0, 5000);
        task.run();
    }

    public static void destroy() {
        task.stop();
    }
}
