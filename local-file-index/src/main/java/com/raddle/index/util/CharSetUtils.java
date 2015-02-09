package com.raddle.index.util;

import java.io.ByteArrayInputStream;

import org.mozilla.universalchardet.UniversalDetector;

public class CharSetUtils {

    private static UniversalDetector charsetDetector = new UniversalDetector(null);

    public synchronized static String detectCharset(byte[] bytes) {
        byte[] buf = new byte[4096];
        ByteArrayInputStream byteIn = new ByteArrayInputStream(bytes);

        int nread;
        try {
            while ((nread = byteIn.read(buf)) > 0 && !charsetDetector.isDone()) {
                charsetDetector.handleData(buf, 0, nread);
            }
            charsetDetector.dataEnd();
            String charsetName = charsetDetector.getDetectedCharset();
            charsetDetector.reset();
            return charsetName;
        } catch (Exception e) {
            e.printStackTrace();
            try {
                charsetDetector.reset();
            } catch (Exception e1) {
            }
            // 不知道当前是什么状态，直接换一个
            charsetDetector = new UniversalDetector(null);
            return null;
        }
    }
}
