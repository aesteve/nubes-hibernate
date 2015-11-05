package com.github.aesteve.nubes.hibernate;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;

import com.github.aesteve.nubes.hibernate.annotations.Create;
import com.github.aesteve.nubes.hibernate.annotations.RemoveById;
import com.github.aesteve.nubes.hibernate.annotations.RetrieveById;
import com.github.aesteve.nubes.hibernate.annotations.RetrieveByQuery;
import com.github.aesteve.nubes.hibernate.annotations.SessionPerRequest;
import com.github.aesteve.nubes.hibernate.annotations.Transactional;
import com.github.aesteve.nubes.hibernate.annotations.Update;
import com.github.aesteve.nubes.hibernate.factories.GetByIdProcessorFactory;
import com.github.aesteve.nubes.hibernate.factories.QueryListProcessorFactory;
import com.github.aesteve.nubes.hibernate.factories.RemoveByIdProcessorFactory;
import com.github.aesteve.nubes.hibernate.factories.SavesAndReturnProcessorFactory;
import com.github.aesteve.nubes.hibernate.factories.SessionPerRequestProcessorFactory;
import com.github.aesteve.nubes.hibernate.factories.TransactionalProcessorFactory;
import com.github.aesteve.nubes.hibernate.factories.UpdateAndReturnProcessorFactory;
import com.github.aesteve.nubes.hibernate.reflections.injectors.typed.CriteriaBuilderParamInjector;
import com.github.aesteve.nubes.hibernate.reflections.injectors.typed.EntityManagerInjector;
import com.github.aesteve.nubes.hibernate.services.HibernateService;
import com.github.aesteve.vertx.nubes.VertxNubes;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;

public class HibernateNubes extends VertxNubes {

	public static final String HIBERNATE_SERVICE_NAME = "hibernate";

	protected HibernateService hibernate;

	private JsonObject jsonConfig;

	public HibernateNubes(Vertx vertx, JsonObject config) {
		super(vertx, config);
		this.jsonConfig = config;
		hibernate = new HibernateService(jsonConfig);
	}

	@Override
	public void bootstrap(Handler<AsyncResult<Router>> handler) {
		hibernate.init(vertx, jsonConfig);
		registerService(HIBERNATE_SERVICE_NAME, hibernate);
		registerAnnotationProcessor(Create.class, new SavesAndReturnProcessorFactory(hibernate));
		registerAnnotationProcessor(Update.class, new UpdateAndReturnProcessorFactory(hibernate));
		registerAnnotationProcessor(RetrieveById.class, new GetByIdProcessorFactory(hibernate));
		registerAnnotationProcessor(RetrieveByQuery.class, new QueryListProcessorFactory(hibernate));
		registerAnnotationProcessor(RemoveById.class, new RemoveByIdProcessorFactory(hibernate));
		registerAnnotationProcessor(SessionPerRequest.class, new SessionPerRequestProcessorFactory(hibernate));
		registerAnnotationProcessor(Transactional.class, new TransactionalProcessorFactory(hibernate));
		registerTypeParamInjector(CriteriaBuilder.class, new CriteriaBuilderParamInjector(hibernate));
		registerTypeParamInjector(EntityManager.class, new EntityManagerInjector(hibernate));
		super.bootstrap(handler);
	}

}
