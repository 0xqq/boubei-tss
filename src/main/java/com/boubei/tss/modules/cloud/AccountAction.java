package com.boubei.tss.modules.cloud;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.boubei.tss.PX;
import com.boubei.tss.dm.DMUtil;
import com.boubei.tss.framework.persistence.ICommonService;
import com.boubei.tss.framework.sso.Environment;
import com.boubei.tss.modules.cloud.entity.Account;
import com.boubei.tss.modules.param.ParamConfig;
import com.boubei.tss.um.entity.SubAuthorize;
import com.boubei.tss.util.DateUtil;
import com.boubei.tss.util.EasyUtils;

@Controller
@RequestMapping({ "/auth/account", "/api/account" })
public class AccountAction {

	@Autowired
	private ICommonService commService;
	@Autowired
	private CloudService service;
	@Autowired
	private CloudDao cloudDao;

	// 查看余额
	@RequestMapping(method = RequestMethod.GET)
	@ResponseBody
	public Account queryAccount() {
		Long userID = Environment.getUserId();
		List<?> accounts = commService.getList(" from Account where belong_user_id = ?", userID);
		if (accounts.size() > 0) {
			return (Account) accounts.get(0);
		}

		Account account = new Account();
		account.setBalance(0D);
		account.setId(-999L);
		return account;
	}

	// 查看账户流水
	@RequestMapping(value = "/flow", method = RequestMethod.GET)
	@ResponseBody
	public Object queryAccountFlow(HttpServletRequest request, Integer page, Integer rows) {

		Map<String, String> params = DMUtil.getRequestMap(request, true);
		boolean ignoreZeroFlow = EasyUtils.obj2String(params.get("ignoreZero")).equals("true");

		Long account_id = queryAccount().getId();
		String hql = " from AccountFlow where account_id = " + account_id;
		if (ignoreZeroFlow) {
			hql += " and money != 0 ";
		}
		hql += " order by id desc";

		if (page == null && rows == null) {
			return cloudDao.getEntities(hql);
		} else {
			return cloudDao.getPaginationEntities(hql, page, rows);
		}
	}

	@RequestMapping(value = "/subauthorize", method = RequestMethod.GET)
	@ResponseBody
	public List<?> querySubAuthorize() {
		return commService.getList(" from SubAuthorize where buyerId = ? order by id desc", Environment.getUserId());
	}

	@RequestMapping(value = "/subauthorize/role", method = RequestMethod.GET)
	@ResponseBody
	public List<?> querySubAuthorizeRoles(Long strategyId) {
		String hql = "select ru, r.name, r.description from RoleUser ru, Role r "
				+ " where ru.strategyId = ? and ru.roleId = r.id and ru.moduleId is not null " + " order by r.decode desc";
		return commService.getList(hql, strategyId);
	}

	@RequestMapping(value = "/subauthorize/role", method = RequestMethod.POST)
	@ResponseBody
	public Boolean setSubAuthorizeRoles(Long userId, String ruIds, Long strategyId) {
		service.setSubAuthorizeRoles(userId, ruIds, strategyId);
		return true;
	}

	/**
	 * $.post("/tss/auth/account/subauthorize", {"strategyId": 12, "startDay": "2019-01-01", "endDay": "2019-12-31"}
	 */
	@RequestMapping(value = "/subauthorize", method = RequestMethod.POST)
	@ResponseBody
	public void setSubAuthorizeExpire(Long strategyId, String startDay, String endDay) {
		if (!Environment.isAdmin()) return;

		SubAuthorize sa = (SubAuthorize) commService.getEntity(SubAuthorize.class, strategyId);
		sa.setStartDate(DateUtil.parse(startDay));
		sa.setEndDate(DateUtil.parse(endDay));
		commService.update(sa);
	}

	@RequestMapping("/subauthorize/check")
	@ResponseBody
	public Object[] checkSubAuthorizeExpire() {
		String hql = "select count(id), min(endDate) from SubAuthorize where ? in (buyerId, ownerId) and endDate between ? and ?";
		Long userId = Environment.getUserId();
		int preDays = EasyUtils.obj2Int(ParamConfig.getAttribute(PX.SA_EXPIRE_NOTIFY_DAYS, "15"));
		return (Object[]) commService.getList(hql, userId, DateUtil.addDays(-3), DateUtil.addDays(preDays)).get(0);
	}
}
