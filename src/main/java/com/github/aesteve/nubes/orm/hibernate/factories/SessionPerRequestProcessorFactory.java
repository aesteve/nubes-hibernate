package com.github.aesteve.nubes.orm.hibernate.factories;

import com.github.aesteve.nubes.orm.annotations.SessionPerRequest;
import com.github.aesteve.nubes.orm.hibernate.handlers.impl.SessionPerRequestProcessor;
import com.github.aesteve.nubes.orm.hibernate.services.HibernateService;
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
