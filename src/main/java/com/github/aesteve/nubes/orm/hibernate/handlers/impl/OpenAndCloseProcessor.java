package com.github.aesteve.nubes.orm.hibernate.handlers.impl;

import com.github.aesteve.nubes.orm.hibernate.services.HibernateService;
import com.github.aesteve.vertx.nubes.handlers.Processor;
import com.github.aesteve.vertx.nubes.utils.async.AsyncUtils;

import io.vertx.ext.web.RoutingContext;

public abstract class OpenAndCloseProcessor implements Processor {
	
	protected HibernateService hibernate;
	
	public OpenAndCloseProcessor(HibernateService hibernate) {
		this.hibernate = hibernate;
	}
	
	@Override
	public void preHandle(RoutingContext context) {
		hibernate.createSession(result -> {
			if (result.failed()) {
				context.fail(result.cause());
			} else {
				context.put(HibernateService.SESSION_ID_CTX, result.result());
				context.next();
			}
		}, context);
	}
	
	@Override
	public void afterAll(RoutingContext context) {
		String sessionId = context.get(HibernateService.SESSION_ID_CTX);
		hibernate.flushAndClose(sessionId, AsyncUtils.nextOrFail(context));
	}

	
	
}
