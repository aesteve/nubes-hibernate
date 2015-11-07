package com.github.aesteve.nubes.orm.hibernate.factories;

import com.github.aesteve.nubes.orm.annotations.Transactional;
import com.github.aesteve.nubes.orm.hibernate.handlers.impl.TransactionalProcessor;
import com.github.aesteve.nubes.orm.hibernate.services.HibernateService;
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
