/* ==================================================================   
 * Created [2009-7-7] by Jon.King 
 * ==================================================================  
 * TSS 
 * ================================================================== 
 * mailTo:boubei@163.com
 * Copyright (c) boubei.com, 2015-2018 
 * ================================================================== 
 */

package com.boubei.tss.framework.web.listener;

import java.util.Date;

import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.apache.log4j.Logger;

import com.boubei.tss.PX;
import com.boubei.tss.framework.sso.context.Context;
import com.boubei.tss.framework.sso.online.IOnlineUserManager;
import com.boubei.tss.framework.sso.online.OnlineUserManagerFactory;
import com.boubei.tss.modules.param.ParamConfig;

/**
 * <p> Session超时监听程序 </p>
 *
 * 相关Session超时时，注销在线用户库中对应记录信息
 *
 */
@WebListener
public class SessionDestroyedListener implements HttpSessionListener {

    private Logger log = Logger.getLogger(this.getClass());
 
    public void sessionCreated(HttpSessionEvent event) {
        HttpSession session = event.getSession();
        
        // 设置 session 的过期时间
        try {
        	String configValue = ParamConfig.getAttribute(PX.SESSION_CYCLELIFE_CONFIG);
        	int cycleLife = Integer.parseInt(configValue);
			session.setMaxInactiveInterval(cycleLife); // 以秒为单位
        } 
        catch(Exception e) { }
        
        String sessionId = session.getId();
        String appCode = Context.getApplicationContext().getCurrentAppCode();
        log.debug("应用【" + appCode + "】里 sessionId为：" + sessionId
                + " 的session创建完成，有效期为：" + session.getMaxInactiveInterval() + " 秒 ");
        
        session.setAttribute("sessionCTime", new Date());
        Context.sessionMap.put(sessionId, session);
    }
 
    public void sessionDestroyed(HttpSessionEvent event) {
        String sessionId = event.getSession().getId();
        String appCode = Context.getApplicationContext().getCurrentAppCode();
        
        log.debug("应用【" + appCode + "】里 sessionId为：" + sessionId + " 的session已经过期，" +
                "有效期为：" + event.getSession().getMaxInactiveInterval() + " 秒 ");
        
        // 注销在线用户库中对应记录信息，去除登陆用户身份证card信息
    	IOnlineUserManager ouManager = OnlineUserManagerFactory.getManager();
    	String token = ouManager.logout(appCode, sessionId);
		if(token != null) {
        	Context.destroyIdentityCard(token);
        }
		
		Context.sessionMap.remove(sessionId);
    }
}
