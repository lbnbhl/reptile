package org.jinghe.com.pojo;

import lombok.Data;

import java.time.LocalDate;

/**
 * @autor wwl
 * @date 2023/5/8-10:31
 * 处罚对象类
 */
@Data
public class PunishObj {

//    省份
    private String province;

//    城市
    private String city;

//    披露日期
    private String date;

//    处罚对象
    private String target;

//    处罚原因
    private String reason;

//    处罚内容
    private String content;

//    罚没金额
    private String amount;

//    处罚具体内容
    private String concreteContent;

//    处理机构
    private String institution;

//    url
    private String url;

//    文档标题
    private String title;

}


