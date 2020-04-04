package com.apple.sdk.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

/**
 * Created by lipeishen on 2020/02/20.
 */
public class FileUtils {

    /**
     * 读取文件内容
     *
     * @param fileName
     * @return
     */
    public static String readFileContent(String fileName) {
        File file = new File(fileName);
        BufferedReader reader = null;
        StringBuffer sbf = new StringBuffer();
        try {
            reader = new BufferedReader(new FileReader(file));
            String tempStr;
            while ((tempStr = reader.readLine()) != null) {
                sbf.append(tempStr);
            }
            return sbf.toString();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            closeReader(reader);
        }
        return sbf.toString();
    }

    /**
     * 关闭文件读取流
     *
     * @param reader
     */
    private static void closeReader(Reader reader) {
        if (reader != null) {
            try {
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
