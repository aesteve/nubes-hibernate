package com.github.aesteve.nubes.hibernate.reflections.injectors.typed;

import javax.persistence.criteria.CriteriaBuilder;

import com.github.aesteve.nubes.hibernate.services.HibernateService;
import com.github.aesteve.vertx.nubes.reflections.injectors.typed.ParamInjector;

import io.vertx.ext.web.RoutingContext;

public class CriteriaBuilderParamInjector implements ParamInjector<CriteriaBuilder> {

	private HibernateService hibernate;
	
	public CriteriaBuilderParamInjector(HibernateService hibernate) {
		this.hibernate = hibernate;
	}
	
	@Override
	public CriteriaBuilder resolve(RoutingContext context) {
		return hibernate.getCriteriaBuilder(context.get(HibernateService.SESSION_ID_CTX));
	}

}
