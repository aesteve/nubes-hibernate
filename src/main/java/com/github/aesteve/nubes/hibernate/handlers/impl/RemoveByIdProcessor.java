package com.github.aesteve.nubes.hibernate.handlers.impl;

import com.github.aesteve.nubes.hibernate.annotations.RemoveById;
import com.github.aesteve.nubes.hibernate.queries.FindById;
import com.github.aesteve.nubes.hibernate.services.HibernateService;
import com.github.aesteve.vertx.nubes.handlers.AnnotationProcessor;
import com.github.aesteve.vertx.nubes.marshallers.Payload;

import io.vertx.ext.web.RoutingContext;

public class RemoveByIdProcessor extends OpenAndCloseProcessor implements AnnotationProcessor<RemoveById> {
	
	public RemoveByIdProcessor(HibernateService hibernate) {
		super(hibernate);
	}
	
	@Override
	public void postHandle(RoutingContext context) {
		Payload<FindById<?>> payload = context.get(Payload.DATA_ATTR);
		String sessionId = context.get(HibernateService.SESSION_ID_CTX);
		hibernate.removeWithinTransaction(sessionId, payload.get(), res -> {
			context.put(Payload.DATA_ATTR, new Payload<>());
			if (res.failed()) {
				context.fail(res.cause());
			} else {
				context.next();
			}
		});
	}

	@Override
	public Class<? extends RemoveById> getAnnotationType() {
		return RemoveById.class;
	}

}
