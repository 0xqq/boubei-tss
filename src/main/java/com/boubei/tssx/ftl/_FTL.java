package com.boubei.tssx.ftl;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Table;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.boubei.tss.dm.ddl._Database;
import com.boubei.tss.dm.ddl._Field;
import com.boubei.tss.dm.record.RecordService;
import com.boubei.tss.framework.persistence.IEntity;
import com.boubei.tss.util.BeanUtil;
import com.boubei.tss.util.EasyUtils;
import com.boubei.tss.util.FileHelper;
import com.boubei.tss.util.URLUtil;

/**
 * 生成各类模板
 */
@Controller
@RequestMapping("/_ftl")
public class _FTL {
	
	@Autowired RecordService recordService;
	
	@RequestMapping(value = "/table/{id}")
	@ResponseBody
	public Object recordDef2Def(@PathVariable("id") Long id) {
		_Database _db = recordService.getDB(id);
        List<Map<Object, Object>> fields = _db.getFields();
        
        String java = FileHelper.readFile(URLUtil.getResourceFileUrl("freemarker/entity.ftl").getPath());
        Map<String, Object> dataMap = new HashMap<String, Object>();
        dataMap.put("tableName", _db.table);
        
        // def fields ---> java fileds
        Map<String, String> m = new HashMap<String, String>();
        m.put(_Field.TYPE_DATE, "Date");
        m.put(_Field.TYPE_DATETIME, "Date");
        m.put(_Field.TYPE_INT, "Long");
        m.put(_Field.TYPE_NUMBER, "Double");
        
        List<String> list = new ArrayList<String>();
        for(Map<Object, Object> f : fields) {
        	String type = (String) f.get("type");
        	type = (String) EasyUtils.checkNull( m.get(type), "String");
        	
        	list.add( "    private " + " "  + f.get("code"));
        }
        dataMap.put("fields", EasyUtils.list2Str(list, ";\n"));
		
        return EasyUtils.fmParse(java, dataMap);
	}

	@RequestMapping(value = "/model/{cn}")
	@ResponseBody
	public Object model2RecordDef(@PathVariable("cn") String className) {

		String defJson = "[";
		Class<?> clazz = BeanUtil.createClassByName(className);
		Field[] fields = clazz.getDeclaredFields();
		for(Field f : fields) {
			List<String> list = new ArrayList<String>();
			
			String name = f.getName();
			if("id".equals(name)) continue;
			
			Class<?> type = f.getType();
			if( IEntity.class.isAssignableFrom(type) ) {
				list.add("'code':'" + name + "_id'");
				list.add("'type':'int'");
				list.add("'cwidth':'0'"); 
				list.add("'jsonUrl':'/tss/xdata/json/" + type.getAnnotation(Table.class).name() + "'");
			} 
			else {
				list.add("'code':'" + name + "'");
				if(type.equals(Integer.class) || type.equals(Long.class)) {
					list.add("'type':'int'");
				}
				if(type.equals(Float.class) || type.equals(Double.class)) {
					list.add("'type':'number'");
				}
				if(type.equals(Date.class)) {
					list.add("'type':'datetime'");
				}
			}
			
			Column column = f.getAnnotation(Column.class); // 取得注释对象
			if( column != null ) {
				if( !column.nullable() ) {
					list.add("'nullable':'false'");
				}
				if( !column.unique() ) {
					list.add("'unique':'true'");
				}
				if( column.length() >= 500) {
					list.add("'width':'300px'");
					list.add("'height':'80px'");
				}
			}
			TssColumn _column = f.getAnnotation(TssColumn.class);
			String label = name;
			if( _column != null ) {
				label = _column.label();
				if( _column.isParam() ) {
					list.add("'isParam':'true'");
				}
				if( !EasyUtils.isNullOrEmpty(_column.defaultVal()) ) {
					list.add("'defaultVal':'" +_column.defaultVal()+ "'");
				}
				if( !EasyUtils.isNullOrEmpty(_column.checkReg()) ) {
					list.add("'checkReg':'" +_column.checkReg()+ "'");
				}
				if( !EasyUtils.isNullOrEmpty(_column.errorMsg()) ) {
					list.add("'errorMsg':'" +_column.errorMsg()+ "'");
				}

				if( !EasyUtils.isNullOrEmpty(_column.calign()) ) {
					list.add("'calign':'" +_column.calign()+ "'");
				}
				if( !EasyUtils.isNullOrEmpty(_column.cwidth()) ) {
					list.add("'cwidth':'" +_column.cwidth()+ "'");
				}
				if( !EasyUtils.isNullOrEmpty(_column.width()) ) {
					list.add("'width':'" +_column.width()+ "'");
				}
				if( !EasyUtils.isNullOrEmpty(_column.height()) ) {
					list.add("'height':'" +_column.height()+ "'");
				}
				if( !EasyUtils.isNullOrEmpty(_column.options()) ) {
					list.add("'options':'" +_column.options()+ "'");
				}
				if( !EasyUtils.isNullOrEmpty(_column.jsonUrl()) ) {
					list.add("'jsonUrl':'" +_column.jsonUrl()+ "'");
				}
			}
			list.add(0, "'label':'" + label + "'");
				
			defJson += "\n  {" +EasyUtils.list2Str(list)+ "},";
		}
		
		return defJson.substring(0, defJson.length() - 1) + "\n]";
	}

}
