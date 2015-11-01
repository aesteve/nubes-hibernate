package com.github.aesteve.nubes.hibernate.handlers.impl;

import com.github.aesteve.nubes.hibernate.annotations.Update;
import com.github.aesteve.nubes.hibernate.services.HibernateService;
import com.github.aesteve.vertx.nubes.handlers.AnnotationProcessor;
import com.github.aesteve.vertx.nubes.marshallers.Payload;

import io.vertx.ext.web.RoutingContext;

public class UpdateAndReturnProcessor extends OpenAndCloseProcessor implements AnnotationProcessor<Update> {
	
	public UpdateAndReturnProcessor(HibernateService hibernate) {
		super(hibernate);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void postHandle(RoutingContext context) {
		Payload payload = context.get(Payload.DATA_ATTR);
		String sessionId = context.get(HibernateService.SESSION_ID_CTX); 
		hibernate.updateWithinTransaction(sessionId, payload.get(), res -> {
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
	public Class<? extends Update> getAnnotationType() {
		return Update.class;
	}

}
