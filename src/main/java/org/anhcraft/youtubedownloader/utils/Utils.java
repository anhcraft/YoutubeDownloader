package org.anhcraft.youtubedownloader.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

public class Utils {
    public static String decodeBase64(String str){
        try {
            return URLDecoder.decode(str.replaceAll("%(?![0-9a-fA-F]{2})", "%25"), StandardCharsets.UTF_8.name());
        } catch(UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return str;
    }
}
