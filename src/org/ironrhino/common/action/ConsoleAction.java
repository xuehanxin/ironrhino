package org.ironrhino.common.action;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;

import org.apache.commons.lang3.StringUtils;
import org.ironrhino.core.metadata.Authorize;
import org.ironrhino.core.metadata.AutoConfig;
import org.ironrhino.core.metadata.JsonConfig;
import org.ironrhino.core.metadata.Scope;
import org.ironrhino.core.security.role.UserRole;
import org.ironrhino.core.spring.ApplicationContextConsole;
import org.ironrhino.core.struts.BaseAction;
import org.ironrhino.core.util.JsonUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.interceptor.annotations.InputConfig;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@AutoConfig
@Authorize(ifAnyGranted = UserRole.ROLE_ADMINISTRATOR)
@Slf4j
public class ConsoleAction extends BaseAction {

	private static final long serialVersionUID = 8180265410790553918L;

	@NotEmpty
	@Getter
	@Setter
	private String expression;

	@Getter
	@Setter
	private Scope scope = Scope.LOCAL;

	@Getter
	private Object result;

	@Autowired
	private ApplicationContextConsole applicationContextConsole;

	@Override
	@Valid
	@InputConfig(resultName = SUCCESS)
	public String execute() throws Exception {
		try {
			result = applicationContextConsole.execute(expression, scope);
			addActionMessage(getText("operate.success") + (result != null ? (":" + JsonUtils.toJson(result)) : ""));
			return SUCCESS;
		} catch (Throwable throwable) {
			if (throwable instanceof InvocationTargetException)
				throwable = ((InvocationTargetException) throwable).getTargetException();
			if (throwable.getCause() instanceof InvocationTargetException)
				throwable = ((InvocationTargetException) throwable.getCause()).getTargetException();
			String msg = throwable.getLocalizedMessage();
			log.error(msg);
			addActionError(getText("error") + (StringUtils.isNotBlank(msg) ? (": " + msg) : ""));
			return ERROR;

		}
	}

	@Valid
	@InputConfig(resultName = SUCCESS)
	@JsonConfig(root = "result")
	public String executeJson() {
		try {
			result = applicationContextConsole.execute(expression, scope);
		} catch (Throwable throwable) {
			if (throwable instanceof InvocationTargetException)
				throwable = ((InvocationTargetException) throwable).getTargetException();
			String msg = throwable.getLocalizedMessage();
			log.error(msg);
			addActionError(getText("error") + (StringUtils.isNotBlank(msg) ? (": " + msg) : ""));
			Map<String, Collection<String>> map = new HashMap<>();
			map.put("actionErrors", getActionErrors());
			result = map;
		}
		return JSON;
	}
}
