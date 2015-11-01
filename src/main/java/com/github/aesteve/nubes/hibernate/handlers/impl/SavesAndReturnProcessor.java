package com.github.aesteve.nubes.hibernate.handlers.impl;

import com.github.aesteve.nubes.hibernate.annotations.Create;
import com.github.aesteve.nubes.hibernate.services.HibernateService;
import com.github.aesteve.vertx.nubes.handlers.AnnotationProcessor;
import com.github.aesteve.vertx.nubes.marshallers.Payload;

import io.vertx.ext.web.RoutingContext;

public class SavesAndReturnProcessor implements AnnotationProcessor<Create> {
	
	private HibernateService hibernate;
	
	public SavesAndReturnProcessor(HibernateService hibernate) {
		this.hibernate = hibernate;
	}
	
	@Override
	public void preHandle(RoutingContext context) {
		hibernate.createSession(result -> {
			if (result.failed()) {
				context.fail(result.cause());
			} else {
				context.put(HibernateService.SESSION_ID_CTX, result.result());
				context.next();
			}
		});
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void postHandle(RoutingContext context) {
		Payload payload = context.get(Payload.DATA_ATTR);
		String sessionId = context.get(HibernateService.SESSION_ID_CTX); 
		hibernate.saveWithinTransaction(sessionId, payload.get(), res -> {
			if (res.failed()) {
				context.fail(res.cause());
			} else {
				payload.set(res.result());
				context.put(Payload.DATA_ATTR, payload);
				context.next();
			}
		});
	}

	@Override
	public Class<? extends Create> getAnnotationType() {
		return Create.class;
	}

}
