package org.ironrhino.security.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
public class SimpleLogoutEventListener implements
		ApplicationListener<LogoutEvent> {

	private Logger logger = LoggerFactory.getLogger(getClass());

	@Override
	public void onApplicationEvent(LogoutEvent event) {
		logger.info(event.getUsername() + " logout from {}",
				event.getRemoteAddr());
	}

}