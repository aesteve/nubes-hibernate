package com.github.aesteve.nubes.orm.hibernate.handlers.impl;

import com.github.aesteve.nubes.orm.annotations.Create;
import com.github.aesteve.nubes.orm.hibernate.services.HibernateService;
import com.github.aesteve.vertx.nubes.handlers.AnnotationProcessor;
import com.github.aesteve.vertx.nubes.marshallers.Payload;

import io.vertx.ext.web.RoutingContext;

public class SavesAndReturnProcessor extends OpenAndCloseProcessor implements AnnotationProcessor<Create> {
	
	public SavesAndReturnProcessor(HibernateService hibernate) {
		super(hibernate);
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

}
