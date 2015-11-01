package com.github.aesteve.nubes.hibernate.factories;

import com.github.aesteve.nubes.hibernate.annotations.RemoveById;
import com.github.aesteve.nubes.hibernate.handlers.impl.RemoveByIdProcessor;
import com.github.aesteve.nubes.hibernate.services.HibernateService;
import com.github.aesteve.vertx.nubes.handlers.AnnotationProcessor;
import com.github.aesteve.vertx.nubes.reflections.factories.AnnotationProcessorFactory;

public class RemoveByIdProcessorFactory implements AnnotationProcessorFactory<RemoveById> {

	private HibernateService hibernate;
	
	public RemoveByIdProcessorFactory(HibernateService hibernate) {
		this.hibernate = hibernate;
	}
	
	@Override
	public AnnotationProcessor<RemoveById> create(RemoveById annnot) {
		return new RemoveByIdProcessor(hibernate);
	}
	
}
