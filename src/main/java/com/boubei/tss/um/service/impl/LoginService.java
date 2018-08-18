/* ==================================================================   
 * Created [2009-08-29] by Jon.King 
 * ==================================================================  
 * TSS 
 * ================================================================== 
 * mailTo:boubei@163.com
 * Copyright (c) boubei.com, 2015-2018 
 * ================================================================== 
 */

package com.boubei.tss.um.service.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.boubei.tss.EX;
import com.boubei.tss.PX;
import com.boubei.tss.framework.exception.BusinessException;
import com.boubei.tss.framework.sso.Environment;
import com.boubei.tss.framework.sso.IOperator;
import com.boubei.tss.framework.sso.context.Context;
import com.boubei.tss.modules.param.Param;
import com.boubei.tss.modules.param.ParamManager;
import com.boubei.tss.um.UMConstants;
import com.boubei.tss.um.dao.IGroupDao;
import com.boubei.tss.um.dao.IRoleDao;
import com.boubei.tss.um.dao.IUserDao;
import com.boubei.tss.um.entity.Group;
import com.boubei.tss.um.entity.User;
import com.boubei.tss.um.entity.permission.RoleUserMapping;
import com.boubei.tss.um.entity.permission.RoleUserMappingId;
import com.boubei.tss.um.helper.PasswordRule;
import com.boubei.tss.um.helper.dto.GroupDTO;
import com.boubei.tss.um.helper.dto.OperatorDTO;
import com.boubei.tss.um.permission.PermissionHelper;
import com.boubei.tss.um.service.ILoginService;
import com.boubei.tss.util.DateUtil;
import com.boubei.tss.util.EasyUtils;
import com.boubei.tss.util.InfoEncoder;
import com.boubei.tss.util.MacrocodeCompiler;
import com.boubei.tss.util.MathUtil;

/**
 * <p>
 * 用户登录系统相关业务逻辑处理接口：
 * <li>根据用户登录名获取用户名及认证方式信息等；
 * <li>根据用户ID获取用户信息；
 * <li>根据用户登录名获取用户信息；
 * </p>
 */
@Service("LoginService")
@SuppressWarnings("unchecked")
public class LoginService implements ILoginService {
	
	protected Logger log = Logger.getLogger(this.getClass());

	@Autowired private IUserDao userDao;
	@Autowired private IGroupDao groupDao;
	@Autowired private IRoleDao roleDao;
	@Autowired private PermissionHelper pHelper;
	
	public int checkPwdErrorCount(String loginName) {
		User user = getUserByLoginName(loginName);
		int count = user.getPwdErrorCount();
	    
	    // 离最后一次输错密码已经超过十分钟了，则统计次数重新清零
		Date lastPwdErrorTime = user.getLastPwdErrorTime();
	    if(lastPwdErrorTime == null 
	    		|| System.currentTimeMillis() - lastPwdErrorTime.getTime() > 10*60*1000) {
	    	count = 0;
	    }
	    
		if( count >= 10) {
			throw new BusinessException(EX.U_25);
		}
		return count;
	}
	
	public void recordPwdErrorCount(String loginName, int currCount) {
		User user = getUserByLoginName(loginName);

		currCount ++;
	    if(currCount >= 10) {
	    	log.info("【" + loginName + "】已连续【" +currCount+ "】次输错密码。");
	    }
	    
	    user.setLastPwdErrorTime(new Date());
    	user.setPwdErrorCount(currCount);
    	userDao.refreshEntity(user);
	}
	
	public void setLastLoginTime(Long userId) {
		User user = userDao.getEntity(userId);
		user.setLastLogonTime(new Date());
		user.setLogonCount( EasyUtils.obj2Int(user.getLogonCount()) + 1 );
		
		userDao.refreshEntity(user);
	}
	
	public Object resetPassword(Long userId, String passwd) {
		User user = userDao.getEntity(userId);
		String token = InfoEncoder.simpleEncode(userId.toString(), MathUtil.randomInt(12));
    	user.setOrignPassword( passwd );
    	
    	if(Context.isOnline()) {
    		IOperator operator = Context.getIdentityCard().getOperator();
    		operator.getAttributesMap().put("passwordStrength", user.getPasswordStrength());
    	}
    	
    	userDao.refreshEntity(user);
    	return token;
	}

	public String[] getLoginInfoByLoginName(String loginName) {
		User user = getUserByLoginName(loginName);
		return new String[] { user.getUserName(), user.getAuthMethod(), user.getAuthToken() };
	}
	
	private User getUserByLoginName(String loginName) {
        User user = userDao.getUserByAccount(loginName, true);
        userDao.evict(user);
        return user;
	}
	
	/* 检查用户的密码强度，太弱的话强制要求修改密码 */
	public int checkPwdSecurity(Long userId) {
    	Object strengthLevel = null;
    	try {
			IOperator operator = getOperatorDTOByID(userId);
			strengthLevel = operator.getAttributesMap().get("passwordStrength");
    	} 
    	catch(Exception e) { }
    	
    	if(EasyUtils.obj2Int(strengthLevel) <= PasswordRule.LOW_LEVEL ) {
    		return 0;
		}
    	
    	// 检查用户上次修改密码时间，如果超过了180天，则将安全等级降低为0
		User user = userDao.getEntity(userId);
		Date lastPwdChangeTime = user.getLastPwdChangeTime();
		lastPwdChangeTime = (Date) EasyUtils.checkNull(lastPwdChangeTime, user.getLastLogonTime(), new Date());
		if( DateUtil.addDays(lastPwdChangeTime, 180).before( new Date() ) ) {
			return -1;
		}
    	
    	return 1;
	}

	public OperatorDTO getOperatorDTOByID(Long userId) {
		User user = userDao.getEntity(userId);
		return new OperatorDTO(user);
	}

	public OperatorDTO getOperatorDTOByLoginName(String loginName) {
	    User user = getUserByLoginName(loginName);
	    return new OperatorDTO(user);
	}
    
    public List<Long> saveUserRolesAfterLogin(Long logonUserId) {
    	List<Long> roleIds = getRoleIdsByUserId( logonUserId );
        return saveUserRolesAfterLogin(logonUserId, roleIds);
	}
    
    public List<Long> saveUserRolesAfterLogin(Long logonUserId, List<Long> roleIds) {
    	
    	userDao.executeHQL("delete RoleUserMapping o where o.id.userId = ?",  logonUserId);
        
        // 默认插入一条【匿名角色】给每一个登录用户
    	if( !roleIds.contains(UMConstants.ANONYMOUS_ROLE_ID) ) {
    		roleIds.add(0, UMConstants.ANONYMOUS_ROLE_ID );
    	}
        Set<Long> roleSet = new HashSet<Long>( roleIds ); // 去重
        
        for(Long roleId : roleSet) {
			RoleUserMappingId id = new RoleUserMappingId();
			id.setUserId(logonUserId);
			id.setRoleId(roleId);
			
			RoleUserMapping entity = new RoleUserMapping();
			entity.setId(id);
			
			userDao.createObject(entity);
		}
        
        return roleIds;
	}

	public List<Long> getRoleIdsByUserId(Long userId) {
		String hql = "select distinct o.id.roleId from ViewRoleUser o where o.id.userId = ? order by o.id.roleId ";
        return (List<Long>) userDao.getEntities(hql, userId);
    }
    
    public List<String> getRoleNames(Collection<Long> roleIds) {
    	if( roleIds.isEmpty() ) {
    		return new ArrayList<String>();
    	}
    	List<?> names = roleDao.getEntities("select name from Role where id in (" +EasyUtils.list2Str(roleIds)+ ") order by id");
        return (List<String>) names;
    }
    
	public List<Object[]> getAssistGroupIdsByUserId(Long userId) {
        String hql = "select distinct g.id, g.name from Group g, GroupUser gu " +
        		" where g.id = gu.groupId and gu.userId = ? and g.groupType = ?";
        return (List<Object[]>) userDao.getEntities(hql, userId, Group.ASSISTANT_GROUP_TYPE);
	}

	public List<Object[]> getGroupsByUserId(Long userId) {
		List<?> list = groupDao.getFatherGroupsByUserId(userId);
		List<Object[]> result = new ArrayList<Object[]>();
		for (int i = 1; i < list.size() + 1; i++) {
			Group group = (Group) list.get(i - 1);
			result.add(new Object[] {group.getId(), group.getName(), group.getDomain()});
		}
		return result;
	}

    public List<GroupDTO> getGroupTreeByGroupId(Long groupId) {
        List<Group> groups = groupDao.getChildrenById(groupId);
        
        List<GroupDTO> returnList = new ArrayList<GroupDTO>();
        for( Group group : groups ){
            GroupDTO dto = new GroupDTO();
            dto.setId(group.getId().toString());
            dto.setName(group.getName());
            dto.setParentId(group.getParentId().toString());
            returnList.add(dto);
        }
        
        return returnList;
    }
    
    public List<OperatorDTO> getUsersByGroupId(Long groupId) {
        List<User> users = groupDao.getUsersByGroupId(groupId);
        return translateUserList2DTO(users);
    }
 
    public List<OperatorDTO> getUsersByRoleId(Long roleId) {
    	String domain = Environment.getDomainOrign();
        String hql = "select distinct u from ViewRoleUser ru, User u, GroupUser gu, Group g" +
                " where ru.id.userId = u.id and ru.id.roleId = ? " +
                " 	and u.id = gu.userId and gu.groupId = g.id and g.groupType = 1 " +
                "  and g.domain " + (domain != null ? " = '"+ domain + "'" : " is null" ) +
                " order by u.id desc ";
        
		List<User> data = (List<User>) groupDao.getEntities( hql, roleId);
        return translateUserList2DTO(data);
    }
    
    public List<?> getUsersByDomain(String domain, String field, Long logonUserId) {
    	domain = (String) EasyUtils.checkNull(domain, UMConstants.DEFAULT_DOMAIN);
    	String selfRegDomain = groupDao.getEntity(UMConstants.SELF_REGISTER_GROUP_ID).getDomain();
    	String devDomain = groupDao.getEntity(UMConstants.DEV_GROUP_ID).getDomain();
    	
    	// 如果当前用户属于开发者域或自注册域，则只返回自己个人账号
    	if(domain.equals(selfRegDomain) || domain.equals(devDomain)) {
    		return userDao.getEntities( "select distinct u." +field+ " from User u where u.id=?", logonUserId );
    	}
    	
        String hql = "select distinct u." +field+ " from Group g, GroupUser gu, User u" +
                " where gu.id.userId = u.id and gu.id.groupId = g.id and groupType = 1 " +
                "	and g.domain = ? order by u." + field;
       
        return userDao.getEntities( hql, domain );
    }
    
    private List<OperatorDTO> translateUserList2DTO(List<User> users){
        List<OperatorDTO> returnList = new ArrayList<OperatorDTO>();
        for( User user : users ){
            returnList.add(new OperatorDTO(user));
        }
        return returnList;
    }
    
	public String[] getContactInfos(String receiverStr, boolean justID) {
    	if(receiverStr == null) return null;
    	
    	Map<String, Object> fmDataMap = new HashMap<String, Object>();
		List<Param> macroParams = ParamManager.getComboParam(PX.EMAIL_MACRO);
		macroParams = (List<Param>) EasyUtils.checkNull(macroParams, new ArrayList<Param>());
		for(Param p : macroParams) {
			fmDataMap.put(p.getText(), p.getValue());
		}
		
		receiverStr = MacrocodeCompiler.runLoop(receiverStr, fmDataMap, true);
		String[] receiver = receiverStr.split(",");
		
		// 将登陆账号转换成该用户的邮箱
		Set<String> emails = new HashSet<String>();
		Set<Long> ids = new HashSet<Long>();
		for(int j = 0; j < receiver.length; j++) {
			String temp = receiver[j];
			
			// 判断配置的是否已经是email，如不是，作为loginName处理
			if(temp.endsWith("@tssRole")) { // 角色
				List<OperatorDTO> list = getUsersByRoleId(parseID(temp));
				for(OperatorDTO user : list) {
					addUserEmail2List(user, emails, ids);
				}
			} 
			else if(temp.endsWith("@tssGroup")) { // 用户组
				List<OperatorDTO> list = getUsersByGroupId(parseID(temp));
				for(OperatorDTO user : list) {
					addUserEmail2List(user, emails, ids);
				}
			} 
			else if(temp.indexOf("@") < 0) { // LoginName
				try {
					OperatorDTO user = getOperatorDTOByLoginName(temp);
					addUserEmail2List(user, emails, ids);
				} 
				catch(Exception e) {
				}
			}
			else if(temp.indexOf("@") > 0 && temp.indexOf(".") > 0) { // email地址
				emails.add(temp);
			}
		}
		
		if(justID) {
			return ids.isEmpty() ? new String[]{} : EasyUtils.list2Str(ids).split(",");
		}
		
		receiver = new String[emails.size()];
		receiver = emails.toArray(receiver);
		
		return receiver;
	}
	
	private Long parseID(String temp) {
		try {
			return EasyUtils.obj2Long( temp.split("@")[0] );
		} catch(Exception e) {
			return 0L;
		}
	}
 
	private void addUserEmail2List(OperatorDTO user, Set<String> emails, Set<Long> ids) {
		String email = (String) user.getAttribute("email");
		if( !EasyUtils.isNullOrEmpty(email) ) {
			emails.add( email );
		}
		ids.add(user.getId());
	}
}