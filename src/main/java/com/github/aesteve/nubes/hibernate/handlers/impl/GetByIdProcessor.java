package com.github.aesteve.nubes.hibernate.handlers.impl;

import com.github.aesteve.nubes.hibernate.annotations.GetById;
import com.github.aesteve.nubes.hibernate.queries.FindById;
import com.github.aesteve.nubes.hibernate.services.HibernateService;
import com.github.aesteve.vertx.nubes.handlers.AnnotationProcessor;
import com.github.aesteve.vertx.nubes.marshallers.Payload;

import io.vertx.ext.web.RoutingContext;

public class GetByIdProcessor implements AnnotationProcessor<GetById> {

	public static final String SESSION_ID = "hibernate-session-id";
	
	private HibernateService hibernate;
	
	public GetByIdProcessor(HibernateService hibernate) {
		this.hibernate = hibernate;
	}
	
	@Override
	public void preHandle(RoutingContext context) {
		hibernate.createSession(result -> {
			if (result.failed()) {
				context.fail(result.cause());
			} else {
				context.put(SESSION_ID, result.result());
				context.next();
			}
		});
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void postHandle(RoutingContext context) {
		Payload<FindById<?>> payload = context.get(Payload.DATA_ATTR);
		String sessionId = context.get(SESSION_ID);
		hibernate.find(sessionId, payload.get(), res -> {
			if (res.failed()) {
				context.put(Payload.DATA_ATTR, null);
				context.fail(res.cause());
			} else {
				Payload newPayload = new Payload<>();
				newPayload.set(res.result());
				context.put(Payload.DATA_ATTR, newPayload);
				context.next();
			}
		});
	}

	@Override
	public Class<? extends GetById> getAnnotationType() {
		return GetById.class;
	}

}
