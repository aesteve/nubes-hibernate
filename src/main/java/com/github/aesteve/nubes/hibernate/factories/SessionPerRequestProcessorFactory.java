package com.github.aesteve.nubes.hibernate.factories;

import com.github.aesteve.nubes.hibernate.annotations.SessionPerRequest;
import com.github.aesteve.nubes.hibernate.handlers.impl.SessionPerRequestProcessor;
import com.github.aesteve.nubes.hibernate.services.HibernateService;
import com.github.aesteve.vertx.nubes.handlers.AnnotationProcessor;
import com.github.aesteve.vertx.nubes.reflections.factories.AnnotationProcessorFactory;

public class SessionPerRequestProcessorFactory implements AnnotationProcessorFactory<SessionPerRequest> {

	private HibernateService hibernate;
	
	public SessionPerRequestProcessorFactory(HibernateService hibernate) {
		this.hibernate = hibernate;
	}
	
	@Override
	public AnnotationProcessor<SessionPerRequest> create(SessionPerRequest annnot) {
		return new SessionPerRequestProcessor(hibernate);
	}
	
}
