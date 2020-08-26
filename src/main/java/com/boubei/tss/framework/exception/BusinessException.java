/* ==================================================================   
 * Created [2009-7-7] by Jon.King 
 * ==================================================================  
 * TSS 
 * ================================================================== 
 * mailTo:boubei@163.com
 * Copyright (c) boubei.com, 2015-2018 
 * ================================================================== 
 */

package com.boubei.tss.framework.exception;

/**
 * 业务逻辑异常。
 *
 */
public class BusinessException extends RuntimeException implements IBusinessException {

    private static final long serialVersionUID = 1759438185530697479L;
 
    public BusinessException(String msg) {
        this(msg, false);
    }
    
    public BusinessException(String msg, Object code) {
        this(msg, false);
        this.errorCode = code;
    }
    
    public BusinessException(String msg, boolean neddPrint) {
        super(msg);
        this.neddPrint = neddPrint;
    }
 
    public BusinessException(String msg, Throwable t) {
        super(msg, t);
    }
 
    /**
     * 是否打印异常stack
     */
    private boolean neddPrint = true;
    
    /**
     * 错误编码
     */
    private Object errorCode;

	public boolean needPrint() {
		return this.neddPrint;
	}
	
    public boolean needRelogin() {
        return false;
    }

	public Object errorCode() {
		return errorCode;
	}
}
