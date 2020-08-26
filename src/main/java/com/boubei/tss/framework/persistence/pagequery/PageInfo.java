/* ==================================================================   
 * Created [2009-7-7] by Jon.King 
 * ==================================================================  
 * TSS 
 * ================================================================== 
 * mailTo:boubei@163.com
 * Copyright (c) boubei.com, 2015-2018 
 * ================================================================== 
 */

package com.boubei.tss.framework.persistence.pagequery;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.boubei.tss.util.EasyUtils;

/**
 * 分页信息结果
 */
public class PageInfo {
	
    public static final int DEFAULT_PAGESIZE = 100;

    /** 当前页码 */
    private int pageNum;

    /** 每页记录数  */
    private int pageSize;

    /** 总记录数 */
    private int totalRows;

    /** 当前页记录集 */
    private List<?> items;

    /** 获取记录集 */
    public List<?> getItems() {
        return  (List<?>) EasyUtils.checkNull(items, new ArrayList<Object>());
    }

    /** 设置记录集 */
    public void setItems(List<?> items) {
        this.items = items;
    }

    /** 获取下一页页码 */
    public int getNextPageNum() {
        return getTotalPages() <= getPageNum() ? 0 : getPageNum() + 1;
    }
    
    /** 获取最后一页页码 */
    public int getLastPageNum(){
    	return getTotalPages();
   }

    /** 获取当前页页码 */
    public int getPageNum() {
		if (  pageNum <= 0 ) {
			return 1;
		}  
		return pageNum;
	}
    
    /**
	 * 获取当前页首条记录顺序数
	 * @return int 记录序号，如果为 -N, 表示没有记录
	 */
    public int getFirstResult() {
        return (getPageNum() - 1) * getPageSize();
    }

    /** 获取首页页码 */
    public int getFirstPageNum() {
        return 1;
    }

    /** 设置当前页页码 */
    public void setPageNum(int pageNum) {
        this.pageNum = pageNum;
    }

    /** 获取每页记录数 */
    public int getPageSize() {
        return pageSize == 0 ? DEFAULT_PAGESIZE : pageSize;
    }

    /** 设置每页记录数 */
    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    /** 获取前一页页码 */
    public int getPrePageNum() {
        return pageNum > 1 ? pageNum - 1 : 0;
    }

    /** 获取总页数 */
    public int getTotalPages() {
        return calTotalPages(getTotalRows(), getPageSize());
    }
    
    public static int calTotalPages(int total, int pagesize) {
    	int totalPages = total / pagesize;
        if( total % pagesize > 0) {
        	totalPages = totalPages + 1;
        }
        return totalPages;
    }

    /** 获取总记录数 */
    public int getTotalRows() {
        return totalRows;
    }

    /** 设置总记录数 */
    public void setTotalRows(int totalRows) {
        this.totalRows = totalRows;
    }

    /** 获取当前页记录数 */
    public int getPageRows() {
        int pageSize = getPageSize();
        if (getTotalPages() <= getPageNum()) { // 最后一页
            return getTotalRows() % pageSize;
        }
        return pageSize;
    }

    /** 对象与XML之间的转换 */
    public String toString(){
        StringBuffer sb = new StringBuffer();
        sb.append("<pagelist totalpages=\"").append(getTotalPages());
        sb.append("\" totalrecords=\"").append(getTotalRows());
        sb.append("\" currentpage=\"").append(getPageNum());
        sb.append("\" pagesize=\"").append(getPageSize());
        sb.append("\" firstpage=\"").append(getFirstPageNum());
        sb.append("\" prepage=\"").append(getPrePageNum());
        sb.append("\" nextpage=\"").append(getNextPageNum());
        sb.append("\" lastpage=\"").append(getLastPageNum());
        sb.append("\" pagerecords=\"").append(getPageRows());
        sb.append("\"/>");
        return sb.toString();
    }
    
    public Map<String, Integer> toJson() {
    	Map<String, Integer> map = new HashMap<String, Integer>();
    	map.put("page", getPageNum());
    	map.put("pagesize", getPageSize());
    	map.put("total", getTotalRows());
        return map;
    }
    
    public Map<String,Object> toEasyUIDataGrid(){
    	Map<String,Object> map = new HashMap<String,Object>();
    	map.put("total", this.getTotalRows());
    	map.put("rows", this.getItems());
    	return map;
    }
}
