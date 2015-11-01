package com.github.aesteve.nubes.hibernate.factories;

import com.github.aesteve.nubes.hibernate.annotations.QueryList;
import com.github.aesteve.nubes.hibernate.handlers.impl.QueryListProcessor;
import com.github.aesteve.nubes.hibernate.services.HibernateService;
import com.github.aesteve.vertx.nubes.handlers.AnnotationProcessor;
import com.github.aesteve.vertx.nubes.reflections.factories.AnnotationProcessorFactory;

public class QueryListProcessorFactory implements AnnotationProcessorFactory<QueryList> {

	private HibernateService hibernate;
	
	public QueryListProcessorFactory(HibernateService hibernate) {
		this.hibernate = hibernate;
	}
	
	@Override
	public AnnotationProcessor<QueryList> create(QueryList annnot) {
		return new QueryListProcessor(hibernate);
	}
	
}
