/* ==================================================================   
 * Created [2016-06-22] by Jon.King 
 * ==================================================================  
 * TSS 
 * ================================================================== 
 * mailTo:boubei@163.com
 * Copyright (c) boubei.com, 2015-2018 
 * ================================================================== 
 */

package com.boubei.tss.modules.log;

import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.boubei.tss.framework.persistence.pagequery.PageInfo;
import com.boubei.tss.framework.web.display.grid.GridDataEncoder;
import com.boubei.tss.framework.web.display.xform.XFormEncoder;
import com.boubei.tss.framework.web.mvc.BaseActionSupport;
import com.boubei.tss.util.XMLDocUtil;

@Controller
@RequestMapping("/log")
public class LogAction extends BaseActionSupport {

    /** 日志展示模板路径 */
	public static final String LOG_XFORM_TEMPLET_PATH = "template/log/Log_xform.xml";
	public static final String LOG_GRID_TEMPLET_PATH  = "template/log/Log_grid.xml";
    
    @Autowired private LogService service;

    @RequestMapping("/objects")
    public void getAllApps4Tree(HttpServletResponse response) {
        List<?> data = service.getAllOperateObjects();
        
        StringBuffer sb = new StringBuffer("<actionSet><treeNode name=\"全部\" id=\"_root\">");
        for(Iterator<?> it = data.iterator(); it.hasNext();){
            String operateObject = (String) it.next();
            operateObject = operateObject.replaceAll("&", "");
            sb.append("<treeNode id=\"" + operateObject + "\" name=\"" + operateObject + "\" icon=\"../../tools/tssJS/img/tree/folder.gif\"/>");
        }
        print("ObjectTree", sb.append("</treeNode></actionSet>"));
    }
    
    @RequestMapping("/{page}")
    public void queryLogs4Grid(HttpServletResponse response, LogQueryCondition condition, @PathVariable int page) {
        condition.getPage().setPageNum(page);
        PageInfo pi = service.getLogsByCondition(condition);
        
        GridDataEncoder gridEncoder = new GridDataEncoder(pi.getItems(), XMLDocUtil.createDoc(LOG_GRID_TEMPLET_PATH));
        print(new String[]{"LogList", "PageInfo"}, new Object[]{gridEncoder, pi});
    }
    
    /**
     * http://localhost:9000/tss/log/json/1?operationCode=update, 04
     * TODO 为数据表定制一个适合业务人员查看的日志界面，在每个数据表的全局JS里定制定义展示方法
     */
    @RequestMapping("/json")
    @ResponseBody
    public List<?> queryLogs4Json(LogQueryCondition condition) {
        PageInfo pi = service.getLogsByCondition(condition);
        return pi.getItems();
    }
    
    @RequestMapping("/item/{id}")
    public void getLogInfo(HttpServletResponse response, @PathVariable long id) {
        Log log = service.getLogById(id);
        print("LogInfo", new XFormEncoder(LOG_XFORM_TEMPLET_PATH, log));
    }
}

