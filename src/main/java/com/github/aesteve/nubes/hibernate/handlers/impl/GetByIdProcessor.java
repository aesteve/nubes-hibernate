package com.github.aesteve.nubes.hibernate.handlers.impl;

import com.github.aesteve.nubes.hibernate.annotations.RetrieveById;
import com.github.aesteve.nubes.hibernate.queries.FindById;
import com.github.aesteve.nubes.hibernate.services.HibernateService;
import com.github.aesteve.vertx.nubes.handlers.AnnotationProcessor;
import com.github.aesteve.vertx.nubes.marshallers.Payload;

import io.vertx.ext.web.RoutingContext;

public class GetByIdProcessor extends OpenAndCloseProcessor implements AnnotationProcessor<RetrieveById> {

	public GetByIdProcessor(HibernateService hibernate) {
		super(hibernate);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void postHandle(RoutingContext context) {
		Payload<FindById<?>> payload = context.get(Payload.DATA_ATTR);
		String sessionId = context.get(HibernateService.SESSION_ID_CTX);
		hibernate.findById(sessionId, payload.get(), res -> {
			if (res.failed()) {
				context.put(Payload.DATA_ATTR, null);
				context.fail(res.cause());
			} else {
				Payload newPayload = new Payload<>();
				newPayload.set(res.result());
				if (res.result() == null) {
					context.fail(404);
					return;
				}
				context.put(Payload.DATA_ATTR, newPayload);
				context.next();
			}
		});
	}

	@Override
	public Class<? extends RetrieveById> getAnnotationType() {
		return RetrieveById.class;
	}

}
