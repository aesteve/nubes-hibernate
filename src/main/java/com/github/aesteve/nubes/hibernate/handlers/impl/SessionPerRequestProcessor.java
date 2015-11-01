package com.github.aesteve.nubes.hibernate.handlers.impl;

import com.github.aesteve.nubes.hibernate.annotations.SessionPerRequest;
import com.github.aesteve.nubes.hibernate.services.HibernateService;
import com.github.aesteve.vertx.nubes.handlers.AnnotationProcessor;

import io.vertx.ext.web.RoutingContext;

public class SessionPerRequestProcessor extends OpenAndCloseProcessor implements AnnotationProcessor<SessionPerRequest> {
	
	public SessionPerRequestProcessor(HibernateService hibernate) {
		super(hibernate);
	}
	
	@Override
	public void postHandle(RoutingContext context) {
		context.next();
	}

	@Override
	public Class<? extends SessionPerRequest> getAnnotationType() {
		return SessionPerRequest.class;
	}

}
