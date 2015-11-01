package com.github.aesteve.nubes.hibernate;

import javax.persistence.criteria.CriteriaBuilder;

import com.github.aesteve.nubes.hibernate.annotations.Create;
import com.github.aesteve.nubes.hibernate.annotations.GetById;
import com.github.aesteve.nubes.hibernate.annotations.QueryList;
import com.github.aesteve.nubes.hibernate.factories.GetByIdProcessorFactory;
import com.github.aesteve.nubes.hibernate.factories.QueryListProcessorFactory;
import com.github.aesteve.nubes.hibernate.factories.SavesAndReturnProcessorFactory;
import com.github.aesteve.nubes.hibernate.reflections.injectors.typed.CriteriaBuilderParamInjector;
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
		service.init(vertx);
		registerService(HIBERNATE_SERVICE_NAME, service);
		registerAnnotationProcessor(Create.class, new SavesAndReturnProcessorFactory(service));
		registerAnnotationProcessor(GetById.class, new GetByIdProcessorFactory(service));
		registerAnnotationProcessor(QueryList.class, new QueryListProcessorFactory(service));
		registerTypeParamInjector(CriteriaBuilder.class, new CriteriaBuilderParamInjector(service));
		super.bootstrap(handler);
	}
	
}
