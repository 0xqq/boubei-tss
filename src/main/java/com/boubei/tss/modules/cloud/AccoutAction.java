package com.boubei.tss.modules.cloud;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.boubei.tss.framework.persistence.ICommonService;
import com.boubei.tss.framework.sso.Environment;
import com.boubei.tss.modules.cloud.entity.Account;

@Controller
@RequestMapping("/auth/module/account")
public class AccoutAction {

	@Autowired
	private ICommonService commonService;

	// 充值

	// 消费扣款

	// 查看余额
	@RequestMapping(method = RequestMethod.GET)
	@ResponseBody
	public Account queryAccount() {
		@SuppressWarnings("unchecked")
		List<Account> accounts = (List<Account>) commonService.getList(" from Account where belong_user_id = ?", Environment.getUserId());
		if (accounts.size() > 0) {
			return accounts.get(0);
		}
		Account account = new Account();
		account.setBalance(0D);
		return account;
	}
	// 提现

}
