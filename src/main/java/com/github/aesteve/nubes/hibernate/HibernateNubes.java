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

	private JsonObject jsonConfig;

	public HibernateNubes(Vertx vertx, JsonObject config) {
		super(vertx, config);
		this.jsonConfig = config;
	}

	@Override
	public void bootstrap(Handler<AsyncResult<Router>> handler) {
		HibernateService service = new HibernateService(jsonConfig);
		service.init(vertx, jsonConfig);
		registerService(HIBERNATE_SERVICE_NAME, service);
		registerAnnotationProcessor(Create.class, new SavesAndReturnProcessorFactory(service));
		registerAnnotationProcessor(Update.class, new UpdateAndReturnProcessorFactory(service));
		registerAnnotationProcessor(RetrieveById.class, new GetByIdProcessorFactory(service));
		registerAnnotationProcessor(RetrieveByQuery.class, new QueryListProcessorFactory(service));
		registerAnnotationProcessor(RemoveById.class, new RemoveByIdProcessorFactory(service));
		registerAnnotationProcessor(SessionPerRequest.class, new SessionPerRequestProcessorFactory(service));
		registerAnnotationProcessor(Transactional.class, new TransactionalProcessorFactory(service));
		registerTypeParamInjector(CriteriaBuilder.class, new CriteriaBuilderParamInjector(service));
		registerTypeParamInjector(EntityManager.class, new EntityManagerInjector(service));
		super.bootstrap(handler);
	}

}
