/* ==================================================================   
 * Created [2016-06-22] by Jon.King 
 * ==================================================================  
 * TSS 
 * ================================================================== 
 * mailTo:boubei@163.com
 * Copyright (c) boubei.com, 2015-2018 
 * ================================================================== 
 */

package com.boubei.tss.modules.param;

import org.apache.log4j.Logger;

import com.boubei.tss.framework.Config;
import com.boubei.tss.util.EasyUtils;

/** 
 * <p> 参数管理对象 </p> 
 * 
 * 可同时从参数管理模块和系统参数配置文件（application.properties）读取参数。
 * 先从参数管理模块取值，如果取不到再去Config里取，都取不到则抛出异常。
 * 
 */
public class ParamConfig {
    
    static Logger log = Logger.getLogger(ParamConfig.class);
    
    /**
     * 获取配置参数。先从参数管理模块取值，如果取不到再去Config里取，都取不到则返回NULL。
     * @param code
     * @return
     */
    public static String getAttribute(String code) {
        String value = null;
        try {
            value = ParamManager.getValue(code);
        } 
        catch(Exception e) { }
 
        return (String) EasyUtils.checkNull(value, Config.getAttribute(code));
    }
    
    public static String getAttribute(String code, String defaultVal) {
    	Object value = null;
        try {
            value = ParamManager.getValue(code);
        } catch(Exception e) { }
        
        return (String) EasyUtils.checkNullI(value, Config.getAttribute(code), defaultVal);
    }
}

