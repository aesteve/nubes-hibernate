package com.github.aesteve.nubes.hibernate.reflections.injectors.typed;

import javax.persistence.EntityManager;

import com.github.aesteve.nubes.hibernate.services.HibernateService;
import com.github.aesteve.vertx.nubes.reflections.injectors.typed.ParamInjector;

import io.vertx.ext.web.RoutingContext;

public class EntityManagerInjector implements ParamInjector<EntityManager> {

	private HibernateService hibernate;
	
	public EntityManagerInjector(HibernateService hibernate) {
		this.hibernate = hibernate;
	}
	
	@Override
	public EntityManager resolve(RoutingContext context) {
		return hibernate.getManager(context.get(HibernateService.SESSION_ID_CTX), null);
	}

}
