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

public class _MySQL extends _Database {
	
	public _MySQL(Record record) {
		super(record);
	}
	
	protected Map<String, String> getDBFiledTypes(int length) {
		Map<String, String> m = super.getDBFiledTypes(length);
		m.put(_Field.TYPE_NUMBER, "DOUBLE");
		m.put(_Field.TYPE_INT, "BIGINT");
		
		return m;
	}
	
	public void createTable() {
		if(this.fields.isEmpty()) return;
		
		StringBuffer createDDL = new StringBuffer("create table if not exists " + table + " ( ");
   		for(Map<String, Object> fDefs : fields) {
			createDDL.append( getFiledDef(fDefs, false) ).append( ", " );
   		}
   		
   		createDDL.append("`domain` varchar(50), ");
   		createDDL.append("`createtime` TIMESTAMP NULL, ");
		createDDL.append("`creator` varchar(50) NOT NULL, ");
		createDDL.append("`updatetime` TIMESTAMP NULL, ");
		createDDL.append("`updator` varchar(50), ");
		createDDL.append("`version` int(5), ");
   		createDDL.append("`id` bigint(20) NOT NULL AUTO_INCREMENT, ");
   		createDDL.append( "primary key (id))" );
   		
   		SQLExcutor.excute(createDDL.toString(), datasource);		
	}

	public void dropTable(String table, String datasource) {
		SQLExcutor.excute("drop table if exists " + table, datasource);
	}
	
	public String toPageQuery(String sql, int page, int pagesize) {
		int fromRow = pagesize * (page - 1);
		return sql + "\n LIMIT " + (fromRow) + ", " + pagesize;
	}
	
	protected String[] getSQLs(String field) {
		String[] names = createNames(field);
		String[] sqls = super.getSQLs(field);
		
		sqls[0] = "alter table " +this.table+ " add UNIQUE (" +field+ ", domain)";
		sqls[1] = "alter table " +this.table+ " drop index " +field;
		sqls[2] = "create index " +names[1]+ " on " +this.table+ " (" +field+ ")";
		sqls[3] = "drop   index " +names[1]+ " on " +this.table;
		return sqls;
	}
}
