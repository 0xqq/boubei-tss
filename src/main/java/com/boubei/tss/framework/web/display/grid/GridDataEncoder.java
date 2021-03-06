/* ==================================================================   
 * Created [2009-7-7] by Jon.King 
 * ==================================================================  
 * TSS 
 * ================================================================== 
 * mailTo:boubei@163.com
 * Copyright (c) boubei.com, 2015-2018 
 * ================================================================== 
 */

package com.boubei.tss.framework.web.display.grid;

import org.dom4j.Document;

import com.boubei.tss.framework.web.display.IDataEncoder;
import com.boubei.tss.framework.web.display.XmlPrintWriter;

/** 
 * Grid对象：生成Grid控件所需要的xml数据
 * 
 */
public class GridDataEncoder implements IDataEncoder {
    
    private static final String GRID_DATA_ROW_NODE_NAME = "row";

    private GridTemplet templet; //模板对象

    private GridParser parser; //解析器

    private Object data; //源数据
 
    public GridDataEncoder(Object data, String uri) {
        this(data, uri, new SimpleGridParser());
    }
 
    public GridDataEncoder(Object data, Document doc) {
        this(data, doc, new SimpleGridParser());
    }
 
    public GridDataEncoder(Object data, Document doc, GridParser parser) {
        templet = new GridTemplet(doc);
        this.data = data;
        this.parser = parser;
        parser.setColumns(templet.getColumns());
    }
 
    public GridDataEncoder(Object data, String uri, GridParser parser) {
        templet = new GridTemplet(uri);
        this.data = data;
        this.parser = parser;
        parser.setColumns(templet.getColumns());
    }

    public String toXml() {
        StringBuffer sb = new StringBuffer();
        sb.append( templet.getHeader() );
        
        GridNode root = parser.parse(data);
        sb.append( root.toXml(GRID_DATA_ROW_NODE_NAME) );
        
        sb.append( templet.getFooter() );
        return sb.toString();
    }

    public void print(XmlPrintWriter out) {
        out.append( toXml() );
    }
    
    public GridTemplet getTemplete() {
    	return this.templet;
    }
}
