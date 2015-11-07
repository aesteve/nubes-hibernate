package com.github.aesteve.nubes.orm.hibernate.factories;

import com.github.aesteve.nubes.orm.annotations.RetrieveById;
import com.github.aesteve.nubes.orm.hibernate.handlers.impl.GetByIdProcessor;
import com.github.aesteve.nubes.orm.hibernate.services.HibernateService;
import com.github.aesteve.vertx.nubes.handlers.AnnotationProcessor;
import com.github.aesteve.vertx.nubes.reflections.factories.AnnotationProcessorFactory;

public class GetByIdProcessorFactory implements AnnotationProcessorFactory<RetrieveById> {

	private HibernateService hibernate;
	
	public GetByIdProcessorFactory(HibernateService hibernate) {
		this.hibernate = hibernate;
	}
	
	@Override
	public AnnotationProcessor<RetrieveById> create(RetrieveById annnot) {
		return new GetByIdProcessor(hibernate);
	}
	
}
