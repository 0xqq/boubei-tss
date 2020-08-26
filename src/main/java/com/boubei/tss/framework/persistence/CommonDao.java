/* ==================================================================   
 * Created [2009-7-7] by Jon.King 
 * ==================================================================  
 * TSS 
 * ================================================================== 
 * mailTo:boubei@163.com
 * Copyright (c) boubei.com, 2015-2018 
 * ================================================================== 
 */

package com.boubei.tss.framework.persistence;

import org.springframework.stereotype.Repository;

/** 
 * <p> 常用Dao类 </p> 
 * <pre>
 * 通过继承BaseDao抽象超类，基本已经可以满足普通数据库操作的要求。 
 * 对于一些数据库操作简单的业务service可以直接调用本对象，而不必再去单独实现自己的Dao对象。
 * </pre>
 */
@Repository("CommonDao")
public class CommonDao extends BaseDao<IEntity> implements ICommonDao{

    public CommonDao() {
        super(IEntity.class);
    }

	public Object createWithLog(Object entity) {
		return super.createObject(entity);
	}
	
	public Object updateWithLog(Object entity) {
		return super.update(entity);
	}

	public Object deleteWithLog(Class<?> entityClass, Long id) {
		return super.delete(entityClass, id);
	}
}

