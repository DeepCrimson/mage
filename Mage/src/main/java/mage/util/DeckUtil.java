package mage.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * @author LevelX2
 */
public final class DeckUtil {

    public static long fixedHash(String string) {
        long h = 1125899906842597L; // prime
        int len = string.length();

        for (int i = 0; i < len; i++) {
            h = 31 * h + string.charAt(i);
        }
        return h;
    }

    public static String writeTextToTempFile(String text) {
        return writeTextToTempFile("cbimportdeck", ".txt", text);
    }

    public static String writeTextToTempFile(String filePrefix, String fileSuffix, String text) {
        BufferedWriter bw = null;
        try {
            File temp = File.createTempFile(filePrefix, fileSuffix);
            bw = new BufferedWriter(new FileWriter(temp));
            bw.write(text);
            return temp.getPath();
        } catch (IOException e) {
        } finally {
            StreamUtils.closeQuietly(bw);
        }
        return null;
    }
}
