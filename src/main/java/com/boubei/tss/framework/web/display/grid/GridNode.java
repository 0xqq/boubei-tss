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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.boubei.tss.util.DateUtil;
import com.boubei.tss.util.MathUtil;
import com.boubei.tss.util.XmlUtil;

/** 
 * 网格Grid行节点对象
 */
public class GridNode {
 
	private GridColumn[] columns; //节点属性名称数组

	private Object[] values; //节点属性值数组

	private List<GridNode> children = new ArrayList<GridNode>(); //子节点：GridNode对象

    public GridNode() { }
 
	public GridNode(Object item, GridColumn[] columns) {
		this.columns = columns;
		
		// 解析数据
		IGridNode node = (IGridNode) item;
		GridAttributesMap attributesMap = new GridAttributesMap(getColumnNames());
		values = node.getAttributes(attributesMap).getValues();
	}
	
	private boolean isRoot() {
		return this.columns == null;
	}

	/**
	 * 增加子节点。
	 * 
	 * @param child
	 */
	public void addChild(GridNode child) {
		this.children.add(child);
	}

	/**
	 * 树型节点转换成xml
	 */
	public String toXml(String nodeName) {
		StringBuffer sb = new StringBuffer();
		if( isRoot() ) {
			for (GridNode childNode : children) {
				sb.append(childNode.toXml(nodeName));
			}
		}
		else {
			sb.append("<").append(nodeName);
			for (int index = 0; index < columns.length; index++) {
				sb.append(" ")
				  .append( columns[index].getName() )
				  .append("=\"")
				  .append( getFormatedValue(index) )
				  .append("\"");
			}
			sb.append("/>");
		}
		
		return sb.toString();
	}

	/**
	 * 获取格式化的数据
	 * 
	 * @param index
	 * @return
	 */
	private String getFormatedValue(int index) {
		GridColumn column = columns[index];
		String pattern = column.getPattern();
		String mode = column.getMode();
		
        Object value = values[index];
		if ( GridColumn.GRID_COLUMN_MODE_DATE.equals(mode)
				&& value instanceof Date 
				&& pattern != null && !"null".equals(pattern)) {
			
			return DateUtil.format((Date) value, pattern);
		}
        
		if (GridColumn.GRID_COLUMN_MODE_NUMBER.equals(mode)) {
		    return MathUtil.formatNumber(value, pattern);
		}
		
		return XmlUtil.toFormXml(value).replaceAll("&quot;", "^"); 
		/* Grid 显示时值不能有双引号，前台将^转回双引号: content="<a onclick=^parent.openUrl('more/bi_nav.html...')^ " */
	}

	/**
	 * 获取column.name 数组
	 */
	private String[] getColumnNames() {
		String[] names = new String[columns.length];
		for (int i = 0; i < columns.length; i++) {
			names[i] = columns[i].getName();
		}
		return names;
	}
}
