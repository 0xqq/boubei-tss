/* ==================================================================   
 * Created [2016-3-5] by Jon.King 
 * ==================================================================  
 * TSS 
 * ================================================================== 
 * mailTo:boubei@163.com
 * Copyright (c) boubei.com, 2015-2018 
 * ================================================================== 
 */

package com.boubei.tss.dm.dml;

import java.sql.Connection;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.boubei.tss.EX;
import com.boubei.tss.cache.Cacheable;
import com.boubei.tss.cache.JCache;
import com.boubei.tss.cache.Pool;
import com.boubei.tss.dm.DMConstants;
import com.boubei.tss.dm.DMUtil;
import com.boubei.tss.dm.record.RecordService;
import com.boubei.tss.framework.Global;
import com.boubei.tss.framework.exception.BusinessException;
import com.boubei.tss.framework.persistence.ICommonService;
import com.boubei.tss.modules.log.BusinessLogger;
import com.boubei.tss.modules.log.Log;
import com.boubei.tss.util.EasyUtils;

/**
 * 
 * 1、在SQL自定义表（SQLDef，dm_sql_def）里定义以下四个SQL语句：
	s1: insert into tbl_jx(name,score,day,createtime,creator,version) values ('${name}', ${score}, '${day}', '${day}', 'Admin', 0)
	s2: select IFNULL(max(id), 1) as maxid from tbl_jx
	s3: update tbl_jx t set t.score = ${score} where t.id = ${maxid}
	s4: delete from tbl_jx  where id = ${maxid} - 1
	
   2、在页面通过AJAX发起以下请求：
	var json = [];
	json.push({ "sqlCode": "s1", data: {"name": "JK", "score": 59, "day": "2017-01-01"} });
	json.push({ "sqlCode": "s1", data: {"name": "Jon", "score": 58, "day": "2017-01-02"} });
	json.push({ "sqlCode": "s2", data: {} });
	json.push({ "sqlCode": "s3", data: {"score": 100} });
	json.push({ "sqlCode": "s4", data: {} });
	
	var params = {};
	params.ds = "connectionpool";
	params.json = JSON.stringify(json);
	tssJS.post("/tss/api/dml/multi", params, function(result) { console.log(result); } );
	
	返回结果： {result: "Success", maxid: 2, step1: 1, step2: 1, step4: 1, step5: 0}
 */
@Controller
@RequestMapping( {"/auth/dml/", "/api/dml/"})
public class MultiSQLExcutor {
	
	Logger log = Logger.getLogger(this.getClass());
	
	public static final int PAGE_SIZE = 50;
	
	@Autowired public RecordService recordService;
	  
    /**
     * 一个事务内执行新增、修改、删除、查询等多条SQL语句，All in one，并记录日志；要求都在一个数据源内执行。
     * 注：需严格控制数据行级权限； 在
     */
	@RequestMapping(value = "/multi", method = RequestMethod.POST)
    @ResponseBody
    public Object exeMultiSQLs(HttpServletRequest request, String ds, String json) throws Exception {
		String queryString = request.getQueryString();
		if( queryString != null && queryString.indexOf("json") >= 0 ) { // wx call
			 json = DMUtil.parseRequestParams(request, true).get("json");
		}
		
    	return _exeMultiSQLs(json, ds, new HashMap<String, Object>());
    }
    	
	@SuppressWarnings("unchecked")
    public Object _exeMultiSQLs(String json, String ds, Map<String, Object> data) throws Exception {
    	// 此方法调用时，不在Spring的IOC内
    	ICommonService commonService = Global.getCommonService();
 
    	ds = (String) EasyUtils.checkNull(ds, DMConstants.LOCAL_CONN_POOL);
    	Pool connpool = getDSPool(ds);
        Cacheable connItem = connpool.checkOut(0);
        Connection conn = (Connection) connItem.getValue();
        
        Map<String, String> sqlDefMap = new HashMap<String, String>();
        List<SQLDef> sqlDefList = (List<SQLDef>) commonService.getList("from SQLDef");
        for(SQLDef sd : sqlDefList) {
        	sqlDefMap.put(sd.getCode(), sd.getScript());
        }
        
        boolean autoCommit = true;
    	String sql = null;
    	List<Statement> statements = new ArrayList<Statement>();
    	
    	Map<String, Object> stepResults = new LinkedHashMap<String, Object>();
    	List<Log> logs = new ArrayList<Log>();
        try {
        	autoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            
	    	List<Map<String, Object>> list = EasyUtils.json2List(json);
	    	int index = 1;
	    	for(Map<String, Object> item : list) {
	    		String sqlCode = (String) item.get("sqlCode");
    			sql = sqlDefMap.get(sqlCode);
	    		if(sql == null) {
	    			throw new BusinessException("code = " +sqlCode+ " SQLDef not exsit.");
	    		}
	    		
	    		Map<String, Object> params = (Map<String, Object>) item.get("data");
	    		if( params == null ) {
	    			params = new HashMap<String, Object>();
	    		}
	    		params.putAll(stepResults);
	    		params.putAll(data);
	    		
	    		String _sql = DMUtil.fmParse(sql, params, true).trim(); // MacrocodeCompiler.run(sql, params, true);
	    		if(_sql.startsWith("FM-parse-error")) {
	    			throw new BusinessException("SQL freemarker parse error" + _sql.replaceAll("FM-parse-error:", ""));
	    		}
	    		sql = _sql;
	    		log.debug(" excute  sql: " + sql);
	    		
	    		if( sql.toLowerCase().startsWith("select") ) {
	    			List<Map<String, Object>> rows = SQLExcutor.query(ds, sql);
	    			if( !EasyUtils.isNullOrEmpty(rows) ) {
	    				stepResults.putAll( rows.get(0) );
	    			}
	    		} 
	    		else {
	    			Statement statement = conn.createStatement();
	    			statements.add(statement);
                    statement.execute(sql);
                    stepResults.put("step" + index, statement.getUpdateCount());
	    		}
	    		
	    		Log excuteLog = new Log( "SQL_" + sqlCode, sqlCode, sql ); // params.toString()
    			logs.add(excuteLog);
    			
    			index++;
	    	}
	    	
	    	// 提交事务
	    	conn.commit();
	    	
	    	try { BusinessLogger.log(logs.toArray(new Log[logs.size()])); } catch(Exception e) { }
        } 
        catch (Exception e) {
        	try { conn.rollback(); } catch (Exception e2) { }
        	
        	throw new BusinessException(e.getMessage() + " SQL= " + sql);
        } 
        finally {
        	try { conn.setAutoCommit(autoCommit); } catch (Exception e) { }
			try { 
				for(Statement statement : statements) {
					statement.close();
				}
			} catch (Exception e) { }
			
        	connpool.checkIn(connItem);
        }
	        
    	stepResults.put("result", "Success");
    	return stepResults;
    }
    
    private static Pool getDSPool(String datasource) {
    	Pool connpool = JCache.getInstance().getPool(datasource);
        if(connpool == null) {
        	throw new BusinessException( EX.parse(EX.DM_02, datasource) );
        }
        return connpool;
    }
}
