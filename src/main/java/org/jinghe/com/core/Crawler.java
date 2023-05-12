package org.jinghe.com.core;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.jinghe.com.pojo.PunishObj;
import org.jinghe.com.resources.Resource;
import org.jinghe.com.util.DownLoadUtil;
import org.jinghe.com.util.ExcelUtil;
import org.jinghe.com.work.RunApplication;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @autor wwl
 * @date 2023/5/9-15:14
 * 爬虫类，提供爬取方法
 */
public class Crawler {

    //    存要下载的文件名和下载地址
    private Map<String,String> fileMap;
    //    存要扫描的地址,这里面的都是合格的资源
    private ArrayDeque<String> urlQueue;
    //    和处罚相关的链接
    private ArrayDeque<String> punishQueue;

    private ThreadPoolExecutor threadPoolExecutor;

    //    数据结果集合
    private List punishObjList;

    private final String saveDir = "D:\\JavaTest\\reptile\\src\\main\\resources\\";

    public Crawler() {
        this.fileMap = new HashMap<>();
        this.urlQueue = new ArrayDeque<>();
        this.punishQueue = new ArrayDeque<>();
        this.threadPoolExecutor = new ThreadPoolExecutor(5,10,3, TimeUnit.SECONDS,new LinkedBlockingQueue<>());
        this.punishObjList = RunApplication.punishObjList;
    }


    //    将url放进队列进行处理
    public void crawling(String[] urls){
        for (String url : urls) {
            crawling(url);
        }
    }


    public void crawling(String url){
        putUrlToQueue(url);
        try {
            resolveUrl(url);
        } catch (InterruptedException e) {
//            e.printStackTrace();
        }
        removeUrlFromQueue();
//        threadPoolExecutor.execute(()->{
//            try {
//                resolveUrl(url);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//            removeUrlFromQueue();
//        });
//        loadUrl();
    }

    public void removeUrlFromQueue(){
        urlQueue.pollFirst();
    }

    public void loadUrl(){
        String url = getUrlFromQueue();
    }

    private void putUrlToQueue(String url) {
        urlQueue.addLast(url);
    }

    private String getUrlFromQueue(){
        return urlQueue.getFirst();
    }

    //    对资源进行筛选过滤
//    protected abstract Resource getUrl(String url);

    public void resolveUrl(String url) throws InterruptedException {
//        true表示str是文本
        boolean flag = false;
        String str = url;
        if (isDownloadable(url)) {
            if (!isLocalUrl(url)){
                fileMap.put(url,url);
//                TODO 下载是需要时间的
                DownLoadUtil.downloadFile(url,saveDir);
                fileMap.remove(url);
//                threadPoolExecutor.execute(()->{
//                    DownLoadUtil.downloadFile(url,saveDir);
//                    fileMap.remove(url);
//                });
            }
            String fileName =saveDir + DownLoadUtil.getFileNameFromUrl(url);
            str = transferToString(fileName);
            flag = true;

            deleteFile(fileName);
        }
        if (!getUrlsAndResolve(str,flag)){
            putPojoToList(str,flag);
        }
    }

//    解析文档，封装成pojo，放入list中
    private void putPojoToList(String str, Boolean flag) {
        if (!flag){
//            根据html解析pojo
            createPojoAndPutListForHtml(str);
        }else {
//            纯文本解析pojo
            createPojoAndPutListForText(str);
        }
    }

    private void createPojoAndPutListForText(String str) {
        String regex = "";
        Pattern pattern;
        Matcher matcher;
        PunishObj punishObj = new PunishObj();
        punishObj.setProvince("山东省");
        punishObj.setCity("聊城市");
//        处罚对象
        regex = "(?<=号).+?(?=：)";
        pattern = Pattern.compile(regex);
        matcher = pattern.matcher(str);
        if (matcher.find()){
            punishObj.setTarget(matcher.group());
        }

//        处罚日期
        regex = "\\d{4}年\\d{1,2}月\\d{1,2}日(?:(?!.*\\b\\d{4}年\\d{1,2}月\\d{1,2}日).)*$";
        pattern = Pattern.compile(regex);
        matcher = pattern.matcher(str);
        if (matcher.find()){
            punishObj.setDate(matcher.group());
        }

//        处理机构
        punishObj.setInstitution("聊城市生态环境局");

        regex = "^.*?(?=号)";
        pattern = Pattern.compile(regex);
        matcher = pattern.matcher(str);
        if (matcher.find()){
            punishObj.setTitle(matcher.group());
        }

        //        构建提取处罚原因的正则表达式
        String[] begins = {"发现你（单位）实施了以下环境违法行为","发现你公司实施了以下环境违法行为","你单位存在以下行为","你公司存在以下行为"};
        String[] ends = {"以上事实","你公司以上行为违反了","上述行为违反了","上述事实，由以下证据证明"};
        for (String begin : begins) {
            for (String end : ends) {
                regex = "(?<=" + begin + ").+?(?=" + end + ")";
                pattern = Pattern.compile(regex);
                matcher = pattern.matcher(str);
                if (matcher.find()) {
                    punishObj.setReason(matcher.group());
                    break;
                }
            }
        }
//        构建处罚内容的正则表达式
        begins = new String[]{"决定对你（单位）作出如下行政处罚", "决定对你单位作出如下行政处罚","我局对你（单位）实施以下行政处罚",
                "我局对你（单位）作出如下行政处罚","我局决定对你(单位)处以如下行政处罚","我局拟对你（单位）处以行政罚款人民币",
                "我局决定对你（单位）罚款", "我局决定对你公司处以如下行政处罚","我局决定对你公司作出如下行政处罚","现责令","我局决定","情节","对你公司作出如下处罚"};
        ends = new String[]{"行政处罚决定的履行方式和期限","限于接到本处罚决定书之日起十五日内","处罚决定的履行方式和期限限于接到本处罚决定之日起15日内缴至指定银行和账号",
                "并于接到本决定书之日起十五日内","限你（单位）自收到本处罚决定之日起十五日内","限于接到本处罚决定之日起十五日内持","你(单位)如不服本处罚决定",
                "责令改正的履行以及未改正"};
        for (String begin : begins) {
            for (String end : ends) {
                regex = "(?<=" + begin + ").+?(?=" + end + ")";
                pattern = Pattern.compile(regex);
                matcher = pattern.matcher(str);
                if (matcher.find()) {
                    punishObj.setContent(matcher.group());
                    punishObj.setConcreteContent(matcher.group());
                    break;
                }
            }
        }
//        构建处罚金额正则表达式
        regex = "(?<=罚款).+?(?=元)";
        pattern = Pattern.compile(regex);
        matcher = pattern.matcher(str);
        if (matcher.find()){
            punishObj.setAmount(matcher.group());
        }
        punishObjList.add(punishObj);
    }

    private boolean getUrlsAndResolve(String str, Boolean flag) {
        if (flag){
            return resolveString(str);
        }else {
            return resolveHtml(str);
        }
    }

//    从页面中获取有用的url并惊醒处理
    private Boolean resolveHtml(String url) {
        Document document = null;
        boolean flag = false;
        try {
            document = Jsoup.connect(url)
                    .userAgent("Mozilla")
    //                .timeout(3000)
                    .get();
            assert document != null;
//                获取body的内容
            Elements body = document.getElementsByTag("body");
//                获取所有a标签中href的内容和a标签的值
            Elements contents  = body.first().getElementsByTag("a");
            for (Element content : contents) {
                String linkText = content.text();
                String linkHref = content.attr("href");
                String regex = "\\.(html|pdf|doc|docx|xls|xlsx)$";
                Pattern pattern = Pattern.compile(regex);
                Matcher matcher = pattern.matcher(linkHref);
                if (linkText.contains("处罚") && matcher.find()){
                    flag = true;
//                    TODO 不一定这么替换的
                    String patternString = "^(.*)/[^/]+$";

                    pattern = Pattern.compile(patternString);
                    matcher = pattern.matcher(url);
                    String str = "";
                    if (matcher.find()) {
                        str = matcher.group(1)+"/";
                    }
                    linkHref = linkHref.replace("./",str);
                    crawling(linkHref);
                }
            }
        } catch (Exception e) {
//            e.printStackTrace();
        }
        return flag;
    }

    //    获取所有链接并爬取
    private Boolean resolveString(String str) {
        boolean flag = false;
        String regex = "http[^\\s]+?\\.html";
        Pattern pattern;
        Matcher matcher;
        pattern = Pattern.compile(regex);
        matcher = pattern.matcher(str);
        //获取所有链接并爬取
        while (matcher.find()){
            flag = true;
            crawling(matcher.group());
        }
        return flag;
    }

    private void deleteFile(String fileName) {
        File file = new File(fileName);
        boolean flag = file.delete();
        for (int i = 0; i < 5; i++) {
            flag = file.delete();
            if (flag = true)
                break;
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
//                e.printStackTrace();
            }
        }
        if (flag)
            System.out.println(file.getName() + " 文件已被删除！");
        else
            System.out.println("文件删除失败");
    }

    private String transferToString(String fileName) {
        if (fileName.contains(".pdf"))
            return resolvePdf();
        else if (fileName.contains(".docx"))
            return resolveDocx(fileName);
        else if (fileName.contains(".doc"))
            return resolveDoc(fileName);
        else if (fileName.contains(".xlsx"))
            return resolveXlsx(fileName);
        else if (fileName.contains(".xls"))
            return resolveXls(fileName);
        else return "";
    }

    private String resolveXlsx(String filePath) {
        StringBuilder result = new StringBuilder();
        try (FileInputStream fileInputStream = new FileInputStream(new File(filePath));
            XSSFWorkbook workbook = new XSSFWorkbook(fileInputStream)) {
            Sheet sheet = workbook.getSheetAt(0);

            for (Row row: sheet) {
                for (Cell cell : row) {
                    if (cell.getCellType() == CellType.NUMERIC) {
//                        System.out.println(cell.getNumericCellValue());
                        result.append(cell.getNumericCellValue());
                    } else if (cell.getCellType() == CellType.STRING) {
//                        System.out.println(cell.getStringCellValue());
                        result.append(cell.getStringCellValue());
                    } else if (cell.getCellType() == CellType.BOOLEAN) {
//                        System.out.println(cell.getBooleanCellValue());
                        result.append(cell.getBooleanCellValue());
                    }
                }
//                System.out.println();
            }
        } catch (IOException e) {
//            e.printStackTrace();
        }
        return String.valueOf(result);
    }

    private String resolveXls(String filePath) {
//        StringBuilder result = new StringBuilder();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        StringBuilder result = new StringBuilder();

        try (FileInputStream fis = new FileInputStream(new File(filePath))) {
            HSSFWorkbook workbook = new HSSFWorkbook(fis);
            Sheet sheet = workbook.getSheetAt(0);
            // 获取日期时间格式
            DataFormat dataFormat = workbook.createDataFormat();
            short dateTimeFormat = dataFormat.getFormat("yyyy-mm-dd hh:mm:ss");
            CellStyle dateTimeStyle = workbook.createCellStyle();
            dateTimeStyle.setDataFormat(dateTimeFormat);

            // 获取日期格式
            short dateFormat = dataFormat.getFormat("yyyy-mm-dd");
            CellStyle dateStyle = workbook.createCellStyle();
            dateStyle.setDataFormat(dateFormat);
            for (int i = 0; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                for (int j = 0; j < row.getLastCellNum(); j++) {
                    Cell cell = row.getCell(j);
                    if (cell == null) break;
//                    if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)){
//                        // 处理日期
//                        Date date = sdf.parse(row.getCell(i).toString());
//                        row.getCell(i).setCellValue(date);
//                        row.getCell(i).setCellStyle(dateStyle);
//                        result.append(date.toString());
//                    }
                    if (cell.getCellType() == CellType.NUMERIC) {
//                        System.out.println(cell.getNumericCellValue());
                        result.append(cell.getNumericCellValue());
                    } else if (cell.getCellType() == CellType.STRING) {
//                        System.out.println(cell.getStringCellValue());
                        result.append(cell.getStringCellValue());
                    } else if (cell.getCellType() == CellType.BOOLEAN) {
//                        System.out.println(cell.getBooleanCellValue());
                        result.append(cell.getBooleanCellValue());
                    }
                }
            }
            workbook.close();
        }
        catch (Exception e) {
//            e.printStackTrace();
        }
        return String.valueOf(result);
    }

    private String resolveDocx(String filePath) {
        String text = "";
        try (FileInputStream fileInputStream = new FileInputStream(new File(filePath));
             XWPFDocument document = new XWPFDocument(fileInputStream);
             XWPFWordExtractor extractor = new XWPFWordExtractor(document)) {
             text = extractor.getText();
        } catch (IOException e) {
//            e.printStackTrace();
        }
        return text;
    }

    private String resolveDoc(String filePath) {
        StringBuilder result = new StringBuilder();
        try (FileInputStream fis = new FileInputStream(new File(filePath))) {
            HWPFDocument document = new HWPFDocument(fis);
            WordExtractor extractor = new WordExtractor(document);
            result.append(extractor.getText());
        } catch (IOException e) {
//            e.printStackTrace();
        }
        return result.toString();
    }

    private String resolvePdf() {

        return "";
    }

    private boolean isLocalUrl(String url) {
        return !url.contains("http");
    }

    private boolean isDownloadable(String url) {
        return url.contains(".pdf") || url.contains(".doc") || url.contains(".docx") || url.contains(".xls") || url.contains(".xlsx");
    }

//TODO 这里得优化
    private void createPojoAndPutListForHtml(String url){
        PunishObj punishObj = new PunishObj();
        Document document = null;
//        是否出现处理不了的url
        boolean flag = false;
        try {
            document = Jsoup.connect(url)
                    .userAgent("Mozilla")
//                    .timeout(3000)
                    .get();
        } catch (Exception e) {
            flag = true;
//            e.printStackTrace();
        } finally {
            if (!flag){
                assert document != null;
//                获取head的内容
                Elements head = document.getElementsByTag("head");
                Elements elements = head.first().getElementsByTag("meta");
                for (Element element : elements) {
                    String name = element.attr("name");
                    String content = element.attr("content");;
                    if (name.equals("PubDate")){
                        punishObj.setDate(content);
                    }if (name.equals("ArticleTitle")){
                        punishObj.setTitle(content);
                    }else if (name.equals("SiteName")){
                        punishObj.setInstitution(content);
                    }
                }
                punishObj.setProvince("山东省");
                punishObj.setCity("聊城市");
                punishObj.setUrl(url);
//       原因，内容，具体内容，金额
                Elements contents = document.getElementsByClass("MsoNormal");
//        title标题的一个括号里 聊环罚[2013]11号（高唐县人和街道社区卫生服务中心）-聊城市生态环境局

                String str,regex;
                str = punishObj.getTitle();
                regex = "\\（(.*?)\\）";
                Pattern pattern = Pattern.compile(regex);
                Matcher matcher = pattern.matcher(str);
                while (matcher.find()) {
                    punishObj.setTarget(matcher.group(1));
                }
                //将html转化成纯文本，再进行正则匹配
                str = document.text();
//        构建提取处罚原因的正则表达式
                String[] begins = {"发现你（单位）实施了以下环境违法行为","发现你公司实施了以下环境违法行为","你单位存在以下行为","你公司存在以下行为"};
                String[] ends = {"以上事实","你公司以上行为违反了","上述行为违反了","上述事实，由以下证据证明"};
                for (String begin : begins) {
                    for (String end : ends) {
                        regex = "(?<=" + begin + ").+?(?=" + end + ")";
                        pattern = Pattern.compile(regex);
                        matcher = pattern.matcher(str);
                        if (matcher.find()) {
                            punishObj.setReason(matcher.group());
                            break;
                        }
                    }
                }
//        构建处罚内容的正则表达式
                begins = new String[]{"决定对你（单位）作出如下行政处罚", "决定对你单位作出如下行政处罚","我局对你（单位）实施以下行政处罚",
                        "我局对你（单位）作出如下行政处罚","我局决定对你(单位)处以如下行政处罚","我局拟对你（单位）处以行政罚款人民币",
                        "我局决定对你（单位）罚款", "我局决定对你公司处以如下行政处罚","我局决定对你公司作出如下行政处罚","现责令","我局决定","情节","对你公司作出如下处罚"};
                ends = new String[]{"行政处罚决定的履行方式和期限","限于接到本处罚决定书之日起十五日内","处罚决定的履行方式和期限限于接到本处罚决定之日起15日内缴至指定银行和账号",
                        "并于接到本决定书之日起十五日内","限你（单位）自收到本处罚决定之日起十五日内","限于接到本处罚决定之日起十五日内持","你(单位)如不服本处罚决定",
                        "责令改正的履行以及未改正"};
                for (String begin : begins) {
                    for (String end : ends) {
                        regex = "(?<=" + begin + ").+?(?=" + end + ")";
                        pattern = Pattern.compile(regex);
                        matcher = pattern.matcher(str);
                        if (matcher.find()) {
                            punishObj.setContent(matcher.group());
                            punishObj.setConcreteContent(matcher.group());
                            break;
                        }
                    }
                }
//        构建处罚金额正则表达式
                regex = "(?<=罚款).+?(?=元)";
                pattern = Pattern.compile(regex);
                matcher = pattern.matcher(str);
                if (matcher.find()){
                    punishObj.setAmount(matcher.group());
                }
                punishObjList.add(punishObj);
            }
        }

    }

}
