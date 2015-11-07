package mock.controllers;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import com.github.aesteve.nubes.orm.annotations.Create;
import com.github.aesteve.nubes.orm.annotations.RemoveById;
import com.github.aesteve.nubes.orm.annotations.RetrieveById;
import com.github.aesteve.nubes.orm.annotations.RetrieveByQuery;
import com.github.aesteve.nubes.orm.annotations.Update;
import com.github.aesteve.nubes.orm.hibernate.queries.FindById;
import com.github.aesteve.vertx.nubes.annotations.Controller;
import com.github.aesteve.vertx.nubes.annotations.mixins.ContentType;
import com.github.aesteve.vertx.nubes.annotations.params.Param;
import com.github.aesteve.vertx.nubes.annotations.params.RequestBody;
import com.github.aesteve.vertx.nubes.annotations.routing.http.DELETE;
import com.github.aesteve.vertx.nubes.annotations.routing.http.GET;
import com.github.aesteve.vertx.nubes.annotations.routing.http.PATCH;
import com.github.aesteve.vertx.nubes.annotations.routing.http.POST;
import com.github.aesteve.vertx.nubes.annotations.routing.http.PUT;
import com.github.aesteve.vertx.nubes.context.PaginationContext;

import mock.domains.Dog;

@Controller("/api/dogs")
@ContentType("application/json")
public class SimpleCrudController {
	
	@POST
	@Create
	public Dog createDog(@RequestBody Dog dog) {
		return dog;
	}

	@PUT("/:name/")
	@PATCH("/:name/")
	@Update
	public Dog updateDog(@RequestBody Dog dog, @Param String name) {
		dog.setName(name);
		return dog;
	}
	
	@GET("/:name/")
	@RetrieveById
	public FindById<Dog> getDogByName(@Param String name) {
		return new FindById<>(Dog.class, name);
	}
	
	@GET
	@RetrieveByQuery
	public CriteriaQuery<Dog> getDogs(@Param String breed, PaginationContext pageContext, CriteriaBuilder builder) {
		CriteriaQuery<Dog> crit = builder.createQuery(Dog.class);
		Root<Dog> root = crit.from(Dog.class);
		if (breed != null) {
			crit.where(builder.equal(root.get("breed"), breed));
		}
		return crit;
	}
	
	@DELETE("/:name/")
	@RemoveById
	public FindById<Dog> removeDog(@Param String name) {
		return new FindById<>(Dog.class, name);
	}
}
