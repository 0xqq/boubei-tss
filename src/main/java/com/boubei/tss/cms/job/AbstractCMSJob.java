/* ==================================================================   
 * Created [2006-12-28] by Jon.King 
 * ==================================================================  
 * TSS 
 * ================================================================== 
 * mailTo:boubei@163.com
 * Copyright (c) boubei.com, 2015-2018 
 * ================================================================== 
 */

package com.boubei.tss.cms.job;

import java.util.ArrayList;
import java.util.List;

import com.boubei.tss.cms.service.IArticleService;
import com.boubei.tss.cms.service.IChannelService;
import com.boubei.tss.framework.Global;
import com.boubei.tss.modules.timer.AbstractJob;
import com.boubei.tss.util.EasyUtils;

public abstract class AbstractCMSJob extends AbstractJob {
	
	// jobConfig 为 siteIds
	protected String excuteJob(String jobConfig, Long jobID) {
		
		JobStrategy strategy = getJobStrategy();
        log.info("开始执行定时策略【" + strategy.name + "】");
        
        List<String> msgList = new ArrayList<>(); 
    	String[] jobConfigs = EasyUtils.split(jobConfig, ","); 
    	for(int i = 0; i < jobConfigs.length; i++) {
    		String msg = excuteCMSJob(jobConfigs[i]);
    		msgList.add(msg);
		}
    	
    	return EasyUtils.list2Str(msgList);
	}
 
	protected IChannelService getChannelService() {
		return (IChannelService) Global.getBean("ChannelService");
	}
	
	protected IArticleService getArticleService() {
		return (IArticleService) Global.getBean("ArticleService");
	}
	
	protected abstract String excuteCMSJob(String jobConfig);
	
	protected abstract JobStrategy getJobStrategy();
}
