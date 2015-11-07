package com.github.aesteve.nubes.orm.hibernate.factories;

import com.github.aesteve.nubes.orm.annotations.Update;
import com.github.aesteve.nubes.orm.hibernate.handlers.impl.UpdateAndReturnProcessor;
import com.github.aesteve.nubes.orm.hibernate.services.HibernateService;
import com.github.aesteve.vertx.nubes.handlers.AnnotationProcessor;
import com.github.aesteve.vertx.nubes.reflections.factories.AnnotationProcessorFactory;

public class UpdateAndReturnProcessorFactory implements AnnotationProcessorFactory<Update> {

	private HibernateService hibernate;
	
	public UpdateAndReturnProcessorFactory(HibernateService hibernate) {
		this.hibernate = hibernate;
	}
	
	@Override
	public AnnotationProcessor<Update> create(Update update) {
		return new UpdateAndReturnProcessor(hibernate);
	}
	
}
