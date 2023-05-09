package org.jinghe.com.util;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;

/**
 * @autor wwl
 * @date 2023/5/8-17:49
 * 下载工具类 TODO 可以综合考虑各种下载方式
 */
public class DownLoadUtil {
    public static void downloadFile(String fileURL, String saveDir) {
        try {
            URL url = new URL(fileURL);
            HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
            int responseCode = httpConn.getResponseCode();

            // 判断响应状态码是否为OK
            if (responseCode == HttpURLConnection.HTTP_OK) {
                // 打开输入流和输出流
                InputStream inputStream = httpConn.getInputStream();
                FileOutputStream outputStream = new FileOutputStream(saveDir + getFileNameFromUrl(fileURL));

                // 将输入流中的数据写入输出流
                int bytesRead = -1;
                byte[] buffer = new byte[1024];
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }

                // 关闭输入输出流
                outputStream.close();
                inputStream.close();

                System.out.println("文件下载成功。");
            } else {
                System.out.println("无法下载文件。");
            }
            httpConn.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getFileNameFromUrl(String fileURL) {
        int index = fileURL.lastIndexOf("/");
        if (index == fileURL.length() - 1) {
            fileURL = fileURL.substring(0, fileURL.length() - 1);
            index = fileURL.lastIndexOf("/");
        }
        return fileURL.substring(index + 1);
    }
}
