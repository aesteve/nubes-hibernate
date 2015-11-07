package com.github.aesteve.nubes.orm.hibernate;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;

import com.github.aesteve.nubes.orm.annotations.Create;
import com.github.aesteve.nubes.orm.annotations.RemoveById;
import com.github.aesteve.nubes.orm.annotations.RetrieveById;
import com.github.aesteve.nubes.orm.annotations.RetrieveByQuery;
import com.github.aesteve.nubes.orm.annotations.SessionPerRequest;
import com.github.aesteve.nubes.orm.annotations.Transactional;
import com.github.aesteve.nubes.orm.annotations.Update;
import com.github.aesteve.nubes.orm.hibernate.factories.GetByIdProcessorFactory;
import com.github.aesteve.nubes.orm.hibernate.factories.QueryListProcessorFactory;
import com.github.aesteve.nubes.orm.hibernate.factories.RemoveByIdProcessorFactory;
import com.github.aesteve.nubes.orm.hibernate.factories.SavesAndReturnProcessorFactory;
import com.github.aesteve.nubes.orm.hibernate.factories.SessionPerRequestProcessorFactory;
import com.github.aesteve.nubes.orm.hibernate.factories.TransactionalProcessorFactory;
import com.github.aesteve.nubes.orm.hibernate.factories.UpdateAndReturnProcessorFactory;
import com.github.aesteve.nubes.orm.hibernate.reflections.injectors.typed.CriteriaBuilderParamInjector;
import com.github.aesteve.nubes.orm.hibernate.reflections.injectors.typed.EntityManagerInjector;
import com.github.aesteve.nubes.orm.hibernate.services.HibernateService;
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
		registerService(HIBERNATE_SERVICE_NAME, hibernate);
	}

	@Override
	public void bootstrap(Handler<AsyncResult<Router>> handler) {
		hibernate.init(vertx, jsonConfig);
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
