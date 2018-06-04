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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.boubei.tss.cms.lucene.ArticleContent;
import com.boubei.tss.cms.lucene.IndexHelper;
import com.boubei.tss.modules.progress.Progress;
import com.boubei.tss.modules.progress.ProgressManager;
import com.boubei.tss.modules.progress.Progressable;
import com.boubei.tss.util.EasyUtils;

/**
 * 创建文章索引的定时JOB & 带进度信息的实现类（共手动建索引时action调用）
 * 
 * com.boubei.tss.cms.job.IndexJob | 0 07 * * * ? | 12
 */
public class IndexJob extends AbstractCMSJob implements Progressable {

	protected String excuteCMSJob(String jobConfig) {
        Long siteId = EasyUtils.obj2Long(jobConfig.trim());
		Set<ArticleContent> data = getData(siteId, false); // 重建索引 而非增量
                
		JobStrategy strategy = getJobStrategy();
		strategy.isIncrement = false;
		strategy.site = getChannelService().getChannelById(siteId);
		IndexHelper.createIndex(strategy, data, new Progress(data.size())); 
		
		return strategy.site.getName() + "里共有" + data.size() + "篇文章被创建了索引";
	}
	
    /**
     * 需要建索引的所有文章地址列表
     * @param siteId
     * @param isIncrement 是否增量
     * @return
     */
    private Set<ArticleContent> getData(Long siteId, boolean isIncrement) {
    	List<Long> channelIds = getChannelService().getAllEnabledChannelIds(siteId);
    	return getChannelService().getIndexableArticles(channelIds, isIncrement);
    }

	protected JobStrategy getJobStrategy() {
		return JobStrategy.getIndexStrategy();
	}
    
    public String createIndex(Long siteId, boolean isIncrement) {
    	Set<ArticleContent> data = getData(siteId, isIncrement);
    	
    	Map<String, Object> paramsMap = new HashMap<String, Object>();
        paramsMap.put("data", data); // 需要建索引的所有文章地址列表
        paramsMap.put("siteId", siteId);
        getJobStrategy().isIncrement = isIncrement;
    	
    	String progressCode = new ProgressManager(this, data.size(), paramsMap).execute();
    	return progressCode;
    }
    
    /* 
     * 执行定时任务时启用进度条。
     */
    @SuppressWarnings("unchecked")
    public String execute(Map<String, Object> params, final Progress progress) {
	    JobStrategy strategy = getJobStrategy();
	    strategy.site = getChannelService().getChannelById((Long) params.get("siteId"));
	    
	    Set<ArticleContent> data = (Set<ArticleContent>)params.get("data");
	    IndexHelper.createIndex(strategy, data, progress); 
	    
	    return data.size() + " articles create index done.";
    }
}
