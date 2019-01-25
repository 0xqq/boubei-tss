package com.boubei.tss.modules.cloud.product;

import java.util.Date;

import com.boubei.tss.modules.cloud.entity.Account;
import com.boubei.tss.modules.cloud.entity.AccountFlow;
import com.boubei.tss.modules.cloud.entity.CloudOrder;
import com.boubei.tss.modules.sn.SerialNOer;

/**
 * @author hank 充值成功后续操作
 */
public class RechargeOrderHandler extends AbstractAfterPay {

	public RechargeOrderHandler(CloudOrder co) {
		super(co);
	}

	public Boolean handle() {
		// 获取账户
		Account account = getAccount();
		Long account_id = account.getId();
		// 创建流水
		AccountFlow flow = new AccountFlow();
		flow.setAccount_id(account_id);
		flow.setMoney(co.getMoney_real());
		flow.setOrder_no(co.getOrder_no());
		flow.setPay_man(this.trade_map.get("buyer_id").toString());
		flow.setPay_time(new Date());
		flow.setPayment(this.payType);
		flow.setSn(SerialNOer.get("AF"));
		flow.setType(co.getType());
		Double balance = account.getBalance() + flow.getMoney();
		flow.setBalance(balance);
		commonDao.create(flow);

		account.setBalance(balance);
		return true;
	}
}
