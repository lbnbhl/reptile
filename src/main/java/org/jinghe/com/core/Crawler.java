package org.jinghe.com.core;

import org.jinghe.com.resources.Resource;

import java.util.ArrayDeque;
import java.util.Map;

/**
 * @autor wwl
 * @date 2023/5/9-15:14
 * 爬虫类，提供爬取方法,最后获取到的是html页面或者资源，本质上都是链接,使用模板方法模式
 */
public abstract class Crawler {

    //    存要下载的文件名和下载地址
    private Map<String,String> fileMap;
    //    存要扫描的地址,这里面的都是合格的资源
    private ArrayDeque<String> urlQueue;
    //    和处罚相关的链接
    private ArrayDeque<String> punishQueue;

    public void crawling(String[] urls){
        for (String url : urls) {
            crawling(url);
        }
    }

    public <T> T crawling(String url){
//        连接url
//        不同的需求对url的内容进行不同的处理，resolveResource
        Resource resource = getResource(url);
//        处理后得到资源 html或其他可下载资源
        //        根据资源的不同类型选择不同的处理方式
        return resolveResource(resource);
    }
//    对资源进行筛选过滤
    protected abstract Resource getResource(String url);

    protected abstract <T> T resolveResource(Resource resource);

}
