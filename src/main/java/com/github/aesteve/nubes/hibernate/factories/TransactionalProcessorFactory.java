package com.github.aesteve.nubes.hibernate.factories;

import com.github.aesteve.nubes.hibernate.annotations.Transactional;
import com.github.aesteve.nubes.hibernate.handlers.impl.TransactionalProcessor;
import com.github.aesteve.nubes.hibernate.services.HibernateService;
import com.github.aesteve.vertx.nubes.handlers.AnnotationProcessor;
import com.github.aesteve.vertx.nubes.reflections.factories.AnnotationProcessorFactory;

public class TransactionalProcessorFactory implements AnnotationProcessorFactory<Transactional> {

	private HibernateService hibernate;
	
	public TransactionalProcessorFactory(HibernateService hibernate) {
		this.hibernate = hibernate;
	}
	
	@Override
	public AnnotationProcessor<Transactional> create(Transactional annnot) {
		return new TransactionalProcessor(hibernate);
	}
	
}
