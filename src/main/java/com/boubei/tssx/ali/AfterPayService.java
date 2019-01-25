package com.boubei.tssx.ali;

import java.util.Map;

public interface AfterPayService {
	/**
	 * @param map
	 * @param payType 支付类型：支付宝|微信|eg
	 */
	Object handle(Map<?, ?> map, String payType);
}