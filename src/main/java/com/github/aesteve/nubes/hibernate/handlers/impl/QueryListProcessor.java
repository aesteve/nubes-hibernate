package com.github.aesteve.nubes.hibernate.handlers.impl;

import javax.persistence.criteria.CriteriaQuery;

import com.github.aesteve.nubes.hibernate.annotations.QueryList;
import com.github.aesteve.nubes.hibernate.services.HibernateService;
import com.github.aesteve.vertx.nubes.context.PaginationContext;
import com.github.aesteve.vertx.nubes.handlers.AnnotationProcessor;
import com.github.aesteve.vertx.nubes.marshallers.Payload;

import io.vertx.ext.web.RoutingContext;

public class QueryListProcessor implements AnnotationProcessor<QueryList> {
	
	private HibernateService hibernate;
	
	public QueryListProcessor(HibernateService hibernate) {
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
		Payload<CriteriaQuery<?>> payload = context.get(Payload.DATA_ATTR);
		String sessionId = context.get(HibernateService.SESSION_ID_CTX);
		PaginationContext pageContext = context.get(PaginationContext.DATA_ATTR);
		hibernate.listAndCount(sessionId, payload.get(), pageContext.firstItemInPage(), pageContext.lastItemInPage(), res -> {
			if (res.failed()) {
				context.put(Payload.DATA_ATTR, null);
				context.fail(res.cause());
			} else {
				Payload newPayload = new Payload<>();
				newPayload.set(res.result().list);
				pageContext.setNbItems(res.result().count);
				context.put(Payload.DATA_ATTR, newPayload);
				context.next();
			}
		});
	}

	@Override
	public Class<? extends QueryList> getAnnotationType() {
		return QueryList.class;
	}

}
