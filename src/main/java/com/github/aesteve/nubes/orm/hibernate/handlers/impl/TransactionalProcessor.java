package com.github.aesteve.nubes.orm.hibernate.handlers.impl;

import com.github.aesteve.nubes.orm.annotations.Transactional;
import com.github.aesteve.nubes.orm.hibernate.services.HibernateService;
import com.github.aesteve.vertx.nubes.handlers.AnnotationProcessor;
import com.github.aesteve.vertx.nubes.utils.async.AsyncUtils;

import io.vertx.ext.web.RoutingContext;

public class TransactionalProcessor extends OpenAndCloseProcessor implements AnnotationProcessor<Transactional> {
	
	public TransactionalProcessor(HibernateService hibernate) {
		super(hibernate);
	}

	@Override
	public void postHandle(RoutingContext context) {
		String sessionId = context.get(HibernateService.SESSION_ID_CTX);
		hibernate.flushAndClose(sessionId, AsyncUtils.nextOrFail(context));
	}

}
