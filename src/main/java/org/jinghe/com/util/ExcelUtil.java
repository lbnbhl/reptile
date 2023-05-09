package org.jinghe.com.util;

import java.io.FileOutputStream;
import java.util.List;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jinghe.com.pojo.PunishObj;

/**
 * @autor wwl
 * @date 2023/5/9-17:10
 */
public class ExcelUtil {

//    将list转化成excel表格
    public static void getExcel(List<PunishObj> punishObjs, String filePath) {

        try (FileOutputStream outputStream = new FileOutputStream(filePath)) {
            XSSFWorkbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("PunishObjs");

            int rowNumber = 0;
            Row row = sheet.createRow(rowNumber++);

            Cell cell = row.createCell(0);
            cell.setCellValue("省份名称");

            cell = row.createCell(1);
            cell.setCellValue("城市名称");

            cell = row.createCell(2);
            cell.setCellValue("披露日期");

            cell = row.createCell(3);
            cell.setCellValue("处罚对象");

            cell = row.createCell(4);
            cell.setCellValue("处罚原因");

            cell = row.createCell(5);
            cell.setCellValue("处罚内容");

            cell = row.createCell(6);
            cell.setCellValue("罚没金额");

            cell = row.createCell(7);
            cell.setCellValue("处罚具体内容");

            cell = row.createCell(8);
            cell.setCellValue("处理机构");

            cell = row.createCell(9);
            cell.setCellValue("url");

            cell = row.createCell(10);
            cell.setCellValue("文档标题");

            for(PunishObj punishObj : punishObjs) {
                row = sheet.createRow(rowNumber++);
                row.createCell(0).setCellValue(punishObj.getProvince());
                row.createCell(1).setCellValue(punishObj.getCity());
                row.createCell(2).setCellValue(punishObj.getDate().toString());
                row.createCell(3).setCellValue(punishObj.getTarget());
                row.createCell(4).setCellValue(punishObj.getReason());
                row.createCell(5).setCellValue(punishObj.getContent());
                row.createCell(6).setCellValue(punishObj.getAmount());
                row.createCell(7).setCellValue(punishObj.getContent());
                row.createCell(8).setCellValue(punishObj.getInstitution());
                row.createCell(9).setCellValue(punishObj.getUrl());
                row.createCell(10).setCellValue(punishObj.getTitle());
            }

            workbook.write(outputStream);
            workbook.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}