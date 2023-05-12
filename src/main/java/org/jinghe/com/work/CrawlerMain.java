package org.jinghe.com.work;

import org.apache.http.HttpEntity;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.jinghe.com.core.Crawler;
import org.jinghe.com.pojo.PunishObj;
import org.jinghe.com.util.DownLoadUtil;
import org.jinghe.com.util.ExcelUtil;
import org.jinghe.com.util.SSLUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Whitelist;
import org.jsoup.select.Elements;
import sun.net.www.http.HttpClient;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @autor wwl
 * @date 2023/5/8-11:28
 * 爬取主类
 */
public class CrawlerMain {

//    存要下载的文件名和下载地址
    private Map<String,String> fileMap;
//    存要扫描的地址,这里面的都是合格的资源
    private ArrayDeque<String> urlQueue;
//    和处罚相关的链接
    private ArrayDeque<String> punishQueue;
//    数据结果集合
    private List<PunishObj> punishObjList;

    public CrawlerMain() {
        fileMap = new HashMap<>();
        urlQueue = new ArrayDeque<>();
        punishQueue = new ArrayDeque<>();
        punishObjList = new ArrayList<>();
    }


    public static void main(String[] args) {
        Crawler crawler = new Crawler();

        String a = "http://sthjj.liaocheng.gov.cn/xxgk/wryhjjgxxgk/xzcf/index.html";
        crawler.crawling(a);
        String b = "http://sthjj.liaocheng.gov.cn/xxgk/wryhjjgxxgk/xzcf/index_";
        for (int i = 1; i < 31; i++) {
            crawler.crawling(b+i+".html");
        }
//        加载资源
//        loadResources(url...);
//        //处理资源
//        resolveResources();
//        //得到结果
//        getResult();
//        System.setProperty("javax.net.ssl.keyStore", "D:/Secure/Program_Base/jdk/jre/lib/security/mykeystore");
//        System.setProperty("javax.net.ssl.keyStorePassword", "changeit");
//        CrawlerMain crawlerMain = new CrawlerMain();
////        crawlerMain.setPublishUrl();
//        crawlerMain.setPublishUrl();
//        crawlerMain.excuteUrl();
//        Set<String> fileNames = crawlerMain.fileMap.keySet();
//        for (String fileName : fileNames) {
//            DownLoadUtil.downloadFile(crawlerMain.fileMap.get(fileName),"./src/main/resources/山东省/聊城市/");
//        }
//        ExcelUtil.getExcel(crawlerMain.punishObjList, "/hhhhh.xls");
    }

//    爬取黑龙江大庆市的处罚结果 TODO 先看逻辑，再封装方法
    public String heiLongJiangDaQing() {
//        TODO url的传入及构建
//        String url = "http://sthjj.liaocheng.gov.cn/xxgk/wryhjjgxxgk/xzcf/201305/t20130515_1674299.html";
        String url = "https://www.baidu.com";

//        建立连接，并传输网页数据
        //默认运行一次，当连接不上链接时（也就是出现SSLException异常时），runTime会+1，也就是再运行一次
        int runTimes = 1;
        for (int runtime = 0; runtime < runTimes; runtime++) {
            try {
                URL u = new URL(url);
//                if("https".equalsIgnoreCase(u.getProtocol())){
//                    SSLUtils.ignoreSsl();
//                }
                //打开连接
                URLConnection urlConnection = u.openConnection();
                //建立Http的连接
                HttpsURLConnection connection = (HttpsURLConnection) urlConnection;
                //新建流
                InputStreamReader isr = new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8);
                BufferedReader br = new BufferedReader(isr);
                //现在网页中的内容已经读进了BufferedReader中。
                System.out.println("连接成功！");
                StringBuilder builder = new StringBuilder();
                String text;
                while ((text = br.readLine()) != null) builder.append(text);
                System.out.println("hhhhh");
                //            解析html
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }


    public void httpClientTest(){
//        获得http客户端
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();

//        创建get请求
        HttpGet httpGet = new HttpGet("http://sthjj.liaocheng.gov.cn/xxgk/wryhjjgxxgk/xzcf/");

        // 响应模型
        CloseableHttpResponse response = null;
        try {
            // 由客户端执行(发送)Get请求
            response = httpClient.execute(httpGet);
            // 从响应模型中获取响应实体
            HttpEntity responseEntity = response.getEntity();
            System.out.println("响应状态为:" + response.getStatusLine());
            if (responseEntity != null) {
                System.out.println("响应内容长度为:" + responseEntity.getContentLength());
                System.out.println("响应内容为:" + EntityUtils.toString(responseEntity));
            }
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        } finally {
            try {
                // 释放资源
                if (httpClient != null) {
                    httpClient.close();
                }
                if (response != null) {
                    response.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }



    public void setPublishUrl() {
        String s = "http://sthjj.liaocheng.gov.cn/xxgk/wryhjjgxxgk/xzcf/index_";
        urlQueue.add("http://sthjj.liaocheng.gov.cn/xxgk/wryhjjgxxgk/xzcf/index.html");
//        从index.html到index_30.html
        for (int i = 1; i < 31; i++) {
            String str = s + i + ".html";
            urlQueue.addLast(str);
        }

        while (!urlQueue.isEmpty()){
            Document document = null;
            String url = urlQueue.pollFirst();
            try {
                document = Jsoup.connect(url)
                        .userAgent("Mozilla")
//                        .timeout(3000)
                        .get();
                assert document != null;
//                获取body的内容
                Elements body = document.getElementsByTag("body");
//                获取所有a标签中href的内容和a标签的值
                Elements contents  = body.first().getElementsByTag("a");
                for (Element content : contents) {
                    String linkText = content.text();
                    String linkHref = content.attr("href");
                    //TODO 后面必须要对链接用正则做
                    String regex = "\\.html$";
                    Pattern pattern = Pattern.compile(regex);
                    Matcher matcher = pattern.matcher(linkHref);
                    if (linkText.contains("处罚") && matcher.find()){
                        linkHref = linkHref.replace("./","http://sthjj.liaocheng.gov.cn/xxgk/wryhjjgxxgk/xzcf/");
                        punishQueue.addLast(linkHref);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

//
    public void excuteUrl(){
        while (!punishQueue.isEmpty()){
            boolean flag = false;
            String url = punishQueue.pollFirst();
            Document document = null;
            try {
                document = Jsoup.connect(url)
                        .userAgent("Mozilla")
//                        .timeout(3000)
                        .get();
                assert document != null;
//                获取body的内容
                Elements body = document.getElementsByTag("body");
//                获取所有a标签中href的内容和a标签的值
                Elements contents  = body.first().getElementsByTag("a");
                for (Element content : contents) {
                    String linkText = content.text();
                    String linkHref = content.attr("href");
                    //TODO 后面必须要对链接用正则做，看有无pdf，doc这些可下载的文件 TODO 也可通过其他方法区别
                    if (linkHref.contains(".pdf") || linkHref.contains(".doc") || linkHref.contains(".docx") || linkHref.contains(".xls") || linkHref.contains(".xlsx") || linkHref.contains(".pptx")){
                        flag = true;
                        fileMap.put(DownLoadUtil.getFileNameFromUrl(linkHref),linkHref);
                    }
                }
//                没有可下载资源说明内容再html上，TODO 这是以html和可下载资源只能存在一个为基础的
                if (!flag){
                    resolveHtml(url);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void resolveHtml(String url1){
        PunishObj punishObj = new PunishObj();
        String url = url1;
        Document document = null;
        try {
            document = Jsoup.connect(url)
                    .userAgent("Mozilla")
//                    .timeout(3000)
                    .get();
        } catch (IOException e) {
            e.printStackTrace();
        }
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


//    解析pdf文件
    public void resolvePDF(){

    }

//    解析doc文件
    public void resolveDOC(){

    }

    //    解析doc文件
    public void resolveDOCX(){

    }

    public void resolveXls(){

    }

    //    解析doc文件
    public void resolveXlsx(){

    }

//    处理字符串类型
    public void resolveString(String str){
        String regex = "http[^\\s]+?\\.html";
        Pattern pattern;
        Matcher matcher;
        pattern = Pattern.compile(regex);
        matcher = pattern.matcher(str);
        while (matcher.find()){
            System.out.println(matcher.group());
        }
    }
}
