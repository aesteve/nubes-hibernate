package com.github.aesteve.nubes.hibernate.factories;

import com.github.aesteve.nubes.hibernate.annotations.Create;
import com.github.aesteve.nubes.hibernate.handlers.impl.SavesAndReturnProcessor;
import com.github.aesteve.nubes.hibernate.services.HibernateService;
import com.github.aesteve.vertx.nubes.handlers.AnnotationProcessor;
import com.github.aesteve.vertx.nubes.reflections.factories.AnnotationProcessorFactory;

public class SavesAndReturnProcessorFactory implements AnnotationProcessorFactory<Create> {

	private HibernateService hibernate;
	
	public SavesAndReturnProcessorFactory(HibernateService hibernate) {
		this.hibernate = hibernate;
	}
	
	@Override
	public AnnotationProcessor<Create> create(Create arg0) {
		return new SavesAndReturnProcessor(hibernate);
	}
	
}
