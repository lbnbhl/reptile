package org.jinghe.com.work;

import org.jinghe.com.core.Crawler;
import org.jinghe.com.pojo.PunishObj;
import org.jinghe.com.util.ExcelUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * @autor wwl
 * @date 2023/5/11-9:12
 */
public class RunApplication {

    public static List punishObjList = new ArrayList();

    public static final String saveDir = "D:\\JavaTest\\reptile\\src\\main\\resources\\";

    public static void main(String[] args) {
        Crawler crawler = new Crawler();
        String a = "http://sthjj.liaocheng.gov.cn/xxgk/wryhjjgxxgk/xzcf/index.html";
        crawler.crawling(a);
        String b = "http://sthjj.liaocheng.gov.cn/xxgk/wryhjjgxxgk/xzcf/index_";
        for (int i = 1; i < 31; i++) {
            crawler.crawling(b+i+".html");
        }
        ExcelUtil.getExcel(punishObjList, saveDir+"heihei.xls");
    }
}
