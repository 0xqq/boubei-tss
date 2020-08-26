/* ==================================================================   
 * Created [2015-8-3] by Jon.King 
 * ==================================================================  
 * TSS 
 * ================================================================== 
 * mailTo:boubei@163.com
 * Copyright (c) boubei.com, 2015-2018 
 * ================================================================== 
 */

package com.boubei.tss.cache.aop;

import java.lang.reflect.Method;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.boubei.tss.cache.Cacheable;
import com.boubei.tss.cache.JCache;
import com.boubei.tss.cache.Pool;
import com.boubei.tss.cache.extension.CacheLife;
import com.boubei.tss.util.EasyUtils;
 
@Component("cacheInterceptor")
public class CacheInterceptor implements MethodInterceptor {

    protected Logger log = Logger.getLogger(this.getClass());
    
    public static String cacheKey(Method targetMethod, Object[] args) {
    	Class<?> declaringClass = targetMethod.getDeclaringClass();
		String key = declaringClass.getName() + "." + targetMethod.getName();
        key += "(";
        if(args != null && args.length > 0) {
        	int index = 0;
        	for(Object arg : args) {
        		if( index++ > 0) {
        			key += ", ";
        		}
        		key += arg;
        	}
        }
        key += ")";
        
        return key;
    }

    public Object invoke(MethodInvocation invocation) throws Throwable {
        Method targetMethod = invocation.getMethod(); /* 获取目标方法 */
        Object[] args = invocation.getArguments();    /* 获取目标方法的参数 */
        
    	Cached annotation = targetMethod.getAnnotation(Cached.class); // 取得注释对象
        if ( annotation == null || hasTimestampArg(args) ) {
        	return invocation.proceed(); /* 如果没有配置缓存，则直接执行方法并返回结果 */
        }
 
		CacheLife life = annotation.cyclelife();
		Pool dataCache = JCache.getInstance().getPool(life.toString());
		
		String key = cacheKey(targetMethod, args);
		Cacheable cacheItem = dataCache.getObject(key);
		
		Object returnVal;
		if (cacheItem != null) {
			returnVal = cacheItem.getValue();
		} else {
			/* 执行方法，如果非空则Cache结果  */
			returnVal = invocation.proceed();
			if(returnVal != null) {
				dataCache.putObject(key, returnVal); 
			}
		}
 
        return returnVal;
    }

	public static boolean hasTimestampArg(Object[] args) {
		boolean hasTimestampArg = false;
        for( Object arg : args ) {
        	if ( arg instanceof Long && EasyUtils.isTimestamp((Long) arg) ) {
        		hasTimestampArg = true;
        		break;
        	}
        }
		return hasTimestampArg;
	}
}