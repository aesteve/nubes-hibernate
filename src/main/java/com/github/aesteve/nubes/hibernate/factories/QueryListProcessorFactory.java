package com.github.aesteve.nubes.hibernate.factories;

import com.github.aesteve.nubes.hibernate.annotations.RetrieveByQuery;
import com.github.aesteve.nubes.hibernate.handlers.impl.QueryListProcessor;
import com.github.aesteve.nubes.hibernate.services.HibernateService;
import com.github.aesteve.vertx.nubes.handlers.AnnotationProcessor;
import com.github.aesteve.vertx.nubes.reflections.factories.AnnotationProcessorFactory;

public class QueryListProcessorFactory implements AnnotationProcessorFactory<RetrieveByQuery> {

	private HibernateService hibernate;
	
	public QueryListProcessorFactory(HibernateService hibernate) {
		this.hibernate = hibernate;
	}
	
	@Override
	public AnnotationProcessor<RetrieveByQuery> create(RetrieveByQuery annnot) {
		return new QueryListProcessor(hibernate);
	}
	
}
