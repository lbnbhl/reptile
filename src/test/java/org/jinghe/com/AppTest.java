package org.jinghe.com;

import static org.junit.Assert.assertTrue;

import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.io.RandomAccessFile;
import org.apache.pdfbox.multipdf.Splitter;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.PDFTextStripperByArea;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.jinghe.com.pojo.PunishObj;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Whitelist;
import org.jsoup.select.Elements;
import org.junit.Test;

import javax.imageio.ImageIO;
//import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Unit test for simple App.
 */
public class AppTest 
{
    /**
     * Rigorous Test :-)
     */
    @Test
    public void shouldAnswerWithTrue()
    {
        assertTrue( true );
    }


//    正则表达式测试
    @Test
    public void regularTest(){
        String str = "上的das号  所测试领受到惩罚手段：2034年12月5日";
//        String regex = "(?<=罚款).+?(?=元)";
        String regex = "^.*?(?=号)";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(str);
        if (matcher.find()) {
            System.out.println("匹配成功：" + matcher.group());
        } else {
            System.out.println("匹配失败");
        }

        regex = "(?<=我是).+?(?=你爹)";
        pattern = Pattern.compile(regex);
        matcher = pattern.matcher(str);
        if (matcher.find()) {
            System.out.println("匹配成功：" + matcher.group());
        } else {
            System.out.println("匹配失败");
        }
    }

    @Test
    public void resolveStringTest(){
        String str = "nihaohttp://www.gaotang.gov.cn/gtxxgk/szfbmxxgk/gtxhjbhj/202303/t20230315_4264748.htmlhfsrfrghhttp://www.guanxian.gov.cn/gxxxgk/xzfbmxxgk/gxhjbhfj/202304/t20230427_4302753.htmlhtmlheihei";
        String regex = "http[^\\s]+?\\.html";
        Pattern pattern;
        Matcher matcher;
        pattern = Pattern.compile(regex);
        matcher = pattern.matcher(str);
        while (matcher.find()){
            System.out.println(matcher.group());
        }
    }

//    解析view-source:http://sthjj.liaocheng.gov.cn/xxgk/wryhjjgxxgk/xzcf/201302/t20130221_1674288.html测试
    @Test
    public void resolveTest(){
        PunishObj punishObj = new PunishObj();
        String url = "http://sthjj.liaocheng.gov.cn/xxgk/wryhjjgxxgk/xzcf/201303/t20130321_1674290.html";
        Document document = null;
        try {
            document = Jsoup.connect(url)
                    .userAgent("Mozilla")
                    .timeout(3000)
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
                punishObj.setDate(LocalDate.parse(content));
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
        System.out.println(punishObj);
    }

//    爬取网站上所有.资源，看看都有什么格式
    @Test
    public void pdfdownLoadTest(){

    }

//    解析pdf测试
    @Test
    public void resolvePdfTest() throws IOException {

        String fileName = "D:\\hhh.pdf";
        PDDocument document = PDDocument.load(new File(fileName));
        PDFTextStripper stripper = new PDFTextStripper();

        String content = stripper.getText(document);
        System.out.println(content);
        document.close();

    }

//    解析docx测试
    @Test
    public  void readDocx() {
        String filePath = "D:\\JavaTest\\reptile\\src\\main\\resources\\山东省\\聊城市\\P020230428348400607067.docx";
        StringBuilder result = new StringBuilder();

        try (FileInputStream fis = new FileInputStream(new File(filePath))) {
            XWPFDocument document = new XWPFDocument(fis);
            XWPFWordExtractor extractor = new XWPFWordExtractor(document);
            result.append(extractor.getText());
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(result.toString());
    }


//    java解析docces
    @Test
    public void readDoc() {
        String filePath = "D:\\JavaTest\\reptile\\src\\main\\resources\\山东省\\聊城市\\P020230428348400501221.doc";
        StringBuilder result = new StringBuilder();

        try (FileInputStream fis = new FileInputStream(new File(filePath))) {
            HWPFDocument document = new HWPFDocument(fis);
            WordExtractor extractor = new WordExtractor(document);
            result.append(extractor.getText());
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println(result.toString());
    }

//    java解析xls格式文件
    @Test
    public void resolveXls(){
        String filePath = "D:\\JavaTest\\reptile\\src\\main\\resources\\山东省\\聊城市\\P020221011336622159119.xls";
//        StringBuilder result = new StringBuilder();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

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
                    if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)){
                        // 处理日期
                        Date date = sdf.parse(row.getCell(i).toString());
                        row.getCell(i).setCellValue(date);
                        row.getCell(i).setCellStyle(dateStyle);
                        System.out.println(date.toString());
                    } if (cell.getCellType() == CellType.NUMERIC) {
                        System.out.println(cell.getNumericCellValue());
//                        result.append(cell.getNumericCellValue());
                    } else if (cell.getCellType() == CellType.STRING) {
                        System.out.println(cell.getStringCellValue());
//                        result.append(cell.getStringCellValue());
                    } else if (cell.getCellType() == CellType.BOOLEAN) {
                        System.out.println(cell.getBooleanCellValue());
//                        result.append(cell.getBooleanCellValue());
                    }
                }
                System.out.println();
            }
            workbook.close();
        }
        catch (IOException | ParseException e) {
            e.printStackTrace();
        }
    }

//    Java解析xlsx格式文件
    @Test
    public void resolveXlsx(){

//        System.out.println(result.toString());
    }
}
