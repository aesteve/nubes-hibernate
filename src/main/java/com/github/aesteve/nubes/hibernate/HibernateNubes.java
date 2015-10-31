package com.github.aesteve.nubes.hibernate;

import com.github.aesteve.nubes.hibernate.annotations.Create;
import com.github.aesteve.nubes.hibernate.annotations.GetById;
import com.github.aesteve.nubes.hibernate.factories.GetByIdProcessorFactory;
import com.github.aesteve.nubes.hibernate.factories.SavesAndReturnProcessorFactory;
import com.github.aesteve.nubes.hibernate.services.HibernateService;
import com.github.aesteve.vertx.nubes.VertxNubes;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;

public class HibernateNubes extends VertxNubes {

	private JsonObject jsonConfig;
	
	public HibernateNubes(Vertx vertx, JsonObject config) {
		super(vertx, config);
		this.jsonConfig = config;
	}
	
	@Override
	public void bootstrap(Handler<AsyncResult<Router>> handler) {
		HibernateService service = new HibernateService(jsonConfig);
		service.init(vertx);
		registerService("hibernate", service);
		registerAnnotationProcessor(Create.class, new SavesAndReturnProcessorFactory(service));
		registerAnnotationProcessor(GetById.class, new GetByIdProcessorFactory(service));
		super.bootstrap(handler);
	}
	
}
