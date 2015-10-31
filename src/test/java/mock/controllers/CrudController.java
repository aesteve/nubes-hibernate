package mock.controllers;

import com.github.aesteve.nubes.hibernate.annotations.GetById;
import com.github.aesteve.nubes.hibernate.annotations.Create;
import com.github.aesteve.nubes.hibernate.queries.FindById;
import com.github.aesteve.nubes.hibernate.services.HibernateService;
import com.github.aesteve.vertx.nubes.annotations.Controller;
import com.github.aesteve.vertx.nubes.annotations.mixins.ContentType;
import com.github.aesteve.vertx.nubes.annotations.params.Param;
import com.github.aesteve.vertx.nubes.annotations.params.RequestBody;
import com.github.aesteve.vertx.nubes.annotations.routing.http.GET;
import com.github.aesteve.vertx.nubes.annotations.routing.http.POST;

import mock.domains.Dog;

@Controller("/api/dogs")
@ContentType("application/json")
public class CrudController {

	HibernateService hibernate;
	
	@POST
	@Create
	public Dog createDog(@RequestBody Dog dog) {
		return dog;
	}
	
	@GET("/:name/")
	@GetById
	public FindById<Dog> getDogByName(@Param String name) {
		return new FindById<>(Dog.class, name);
	}
}
