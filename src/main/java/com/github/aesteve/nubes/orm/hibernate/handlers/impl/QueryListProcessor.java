package com.github.aesteve.nubes.orm.hibernate.handlers.impl;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;

import com.github.aesteve.nubes.orm.annotations.RetrieveByQuery;
import com.github.aesteve.nubes.orm.hibernate.queries.CriteriaQueryBuilder;
import com.github.aesteve.nubes.orm.hibernate.services.HibernateService;
import com.github.aesteve.nubes.orm.queries.FindBy;
import com.github.aesteve.vertx.nubes.context.PaginationContext;
import com.github.aesteve.vertx.nubes.handlers.AnnotationProcessor;
import com.github.aesteve.vertx.nubes.marshallers.Payload;

import io.vertx.ext.web.RoutingContext;

public class QueryListProcessor extends OpenAndCloseProcessor implements AnnotationProcessor<RetrieveByQuery> {
	
	public QueryListProcessor(HibernateService hibernate) {
		super(hibernate);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void postHandle(RoutingContext context) {
		Payload<?> payload = context.get(Payload.DATA_ATTR);
		Class<?> payloadClass = payload.getType();
		CriteriaQuery<?> crit;
		String sessionId = context.get(HibernateService.SESSION_ID_CTX);
		if (CriteriaQuery.class.isAssignableFrom(payloadClass)) {
			crit = (CriteriaQuery)payload.get();
		} else if (payloadClass.equals(FindBy.class)) {
			CriteriaBuilder builder = hibernate.getCriteriaBuilder(sessionId);
			FindBy findBy = (FindBy)payload.get();
			crit = new CriteriaQueryBuilder(findBy).toCriteriaQuery(builder);
		} else {
			context.fail(new Exception("Unknown type of payload, cannot create query"));
			return;
		}
		PaginationContext pageContext = context.get(PaginationContext.DATA_ATTR);
		hibernate.listAndCount(sessionId, crit, pageContext.firstItemInPage(), pageContext.lastItemInPage(), res -> {
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

}
