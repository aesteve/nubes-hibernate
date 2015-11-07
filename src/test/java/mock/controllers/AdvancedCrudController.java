package mock.controllers;

import javax.persistence.EntityManager;

import com.github.aesteve.nubes.orm.annotations.RetrieveByQuery;
import com.github.aesteve.nubes.orm.hibernate.HibernateNubes;
import com.github.aesteve.nubes.orm.hibernate.services.HibernateService;
import com.github.aesteve.nubes.orm.queries.FindBy;
import com.github.aesteve.vertx.nubes.annotations.Controller;
import com.github.aesteve.vertx.nubes.annotations.mixins.ContentType;
import com.github.aesteve.vertx.nubes.annotations.params.Param;
import com.github.aesteve.vertx.nubes.annotations.routing.http.GET;
import com.github.aesteve.vertx.nubes.annotations.services.Service;
import com.github.aesteve.vertx.nubes.context.PaginationContext;
import com.github.aesteve.vertx.nubes.marshallers.Payload;

import io.vertx.ext.web.RoutingContext;
import mock.domains.Dog;
import mock.domains.Master;

@Controller("/api/myDogs")
@ContentType("application/json")
public class AdvancedCrudController {
	
	@Service(HibernateNubes.HIBERNATE_SERVICE_NAME)
	HibernateService hibernate;
	
	@GET
	@RetrieveByQuery
	public void listMyDogs(
			@Param String token,
			EntityManager entityManager, 
			RoutingContext context, 
			Payload<FindBy<Dog>> payload,
			PaginationContext pageContext) {
		
		if (token == null) {
			context.fail(401);
			return;
		}
		hibernate.findBy(entityManager, new FindBy<>(Master.class, "token", token), res -> {
			if (res.failed()) {
				context.fail(res.cause());
				return;
			} 
			Master master = res.result();
			if (master == null) {
				context.fail(403);
				return;
			}
			payload.set(new FindBy<>(Dog.class, "master", master));
			context.next();
		});
	}
}
