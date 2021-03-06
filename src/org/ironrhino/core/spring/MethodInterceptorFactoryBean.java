package org.ironrhino.core.spring;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.ironrhino.core.util.NameableThreadFactory;
import org.ironrhino.core.util.ReflectionUtils;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureTask;

import lombok.Getter;
import lombok.Setter;

public abstract class MethodInterceptorFactoryBean
		implements MethodInterceptor, FactoryBean<Object>, DisposableBean, ApplicationContextAware {

	public static final int EXECUTOR_POOL_SIZE_DEFAULT = 5;

	public static final String EXECUTOR_POOL_SIZE_SUFFIX = ".executor.pool.size";

	@Setter
	private volatile ExecutorService executorService;

	private boolean executorServiceCreated;

	@Getter
	@Setter
	private ApplicationContext applicationContext;

	@Override
	public Object invoke(final MethodInvocation methodInvocation) throws Throwable {
		Method method = methodInvocation.getMethod();
		if (AopUtils.isToStringMethod(methodInvocation.getMethod())) {
			Class<?> objectType = getObjectType();
			return "Dynamic proxy for [" + (objectType != null ? objectType.getName() : "Unknown") + "]";
		}
		if (method.isDefault())
			return ReflectionUtils.invokeDefaultMethod(getObject(), method, methodInvocation.getArguments());
		Class<?> returnType = method.getReturnType();
		if (returnType == Callable.class || returnType == ListenableFuture.class || returnType == Future.class) {
			Callable<Object> callable = new Callable<Object>() {
				@Override
				public Object call() throws Exception {
					try {
						return doInvoke(methodInvocation);
					} catch (Exception e) {
						throw e;
					} catch (Throwable e) {
						throw new InvocationTargetException(e);
					}
				}
			};
			if (returnType == Callable.class) {
				return callable;
			}
			if (returnType == ListenableFuture.class) {
				ListenableFutureTask<Object> future = new ListenableFutureTask<>(callable);
				getExecutorService().execute(future);
				return future;
			}
			if (returnType == Future.class) {
				return getExecutorService().submit(callable);
			}
		}
		return doInvoke(methodInvocation);
	}

	protected abstract Object doInvoke(MethodInvocation methodInvocation) throws Throwable;

	private ExecutorService getExecutorService() {
		ExecutorService es = executorService;
		if (es == null) {
			synchronized (this) {
				es = executorService;
				if (es == null) {
					Class<?> objectType = getObjectType();
					String poolName = objectType != null ? objectType.getSimpleName() : "Unknown";
					ApplicationContext ctx = getApplicationContext();
					if (ctx != null) {
						int threads = ctx.getEnvironment().getProperty(
								objectType != null ? objectType.getName() + EXECUTOR_POOL_SIZE_SUFFIX : "", int.class,
								EXECUTOR_POOL_SIZE_DEFAULT);
						executorService = es = Executors.newFixedThreadPool(threads,
								new NameableThreadFactory(poolName));
					} else {
						executorService = es = Executors.newCachedThreadPool(new NameableThreadFactory(poolName));
					}
					executorServiceCreated = true;
				}
			}
		}
		return es;
	}

	@Override
	public void destroy() {
		if (executorServiceCreated) {
			executorService.shutdown();
			executorServiceCreated = false;
		}
	}

}
