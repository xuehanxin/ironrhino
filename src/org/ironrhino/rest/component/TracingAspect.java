package org.ironrhino.rest.component;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.ironrhino.core.aop.BaseAspect;
import org.ironrhino.core.tracing.Tracing;
import org.ironrhino.core.util.ReflectionUtils;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.RestController;

@Aspect
@ControllerAdvice
public class TracingAspect extends BaseAspect {

	@Around("execution(public * *(..)) and @within(restController)")
	public Object trace(ProceedingJoinPoint pjp, RestController restController) throws Throwable {
		return Tracing.executeThrowableCallable(
				ReflectionUtils.stringify(((MethodSignature) pjp.getSignature()).getMethod()), pjp::proceed,
				"span.kind", "server", "component", "rest");
	}

}
