/* ==================================================================   
 * Created [2016-3-5] by Jon.King 
 * ==================================================================  
 * TSS 
 * ================================================================== 
 * mailTo:boubei@163.com
 * Copyright (c) boubei.com, 2015-2018 
 * ================================================================== 
 */

package com.boubei.tss.dm.ddl;

import java.util.Map;

import com.boubei.tss.dm.dml.SQLExcutor;
import com.boubei.tss.dm.record.Record;

public class _PostgreSQL extends _Database {
	
	public _PostgreSQL(Record record) {
		super(record);
	}
	
	protected Map<String, String> getDBFiledTypes(int length) {
		Map<String, String> m = super.getDBFiledTypes(length);
		m.put(_Field.TYPE_NUMBER, "numeric(19,3)");
		m.put(_Field.TYPE_INT, "bigint");
		m.put(_Field.TYPE_DATETIME, "timestamp");
		
		return m;
	}
	
	protected String[] getSQLs(String field) {
		String[] sqls = super.getSQLs(field);
		sqls[4] = "alter table " +this.table+ " alter " +field+ " TYPE ";  
		
		return sqls;
	}
	
	public void createTable() {
		StringBuffer createDDL = new StringBuffer("create table if not exists " + table + " ( ");
		createDDL.append("id SERIAL primary key, ");  // PostgresSQL需要将ID放在第一栏位，否则insert时返回的getGeneratedKeys不是ID字段
		
   		for(Map<Object, Object> fDefs : fields) {
			createDDL.append( getFiledDef(fDefs, false) ).append( ", " );
   		}
   		
   		createDDL.append("domain varchar(50), ");
   		createDDL.append("createtime timestamp NULL, ");
		createDDL.append("creator varchar(50) NOT NULL, ");
		createDDL.append("updatetime timestamp NULL, ");
		createDDL.append("updator varchar(50), ");
		createDDL.append("version integer");
   		createDDL.append(")");
   		
   		SQLExcutor.excute(createDDL.toString(), datasource);		
	}
 
	public String toPageQuery(String sql, int page, int pagesize) {
		int fromRow = pagesize * (page - 1);
		return sql + "\n LIMIT " + (pagesize) + " OFFSET " + fromRow;
	}
}
