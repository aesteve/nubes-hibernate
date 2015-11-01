package mock.fixtures;

import java.util.ArrayList;
import java.util.List;

import com.github.aesteve.nubes.hibernate.HibernateNubes;
import com.github.aesteve.nubes.hibernate.services.HibernateService;
import com.github.aesteve.vertx.nubes.annotations.services.Service;
import com.github.aesteve.vertx.nubes.fixtures.Fixture;
import com.github.aesteve.vertx.nubes.utils.async.AsyncUtils;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import mock.domains.Dog;

public class DogFixture extends Fixture {

	public static List<Dog> models;
	
	@Service(HibernateNubes.HIBERNATE_SERVICE_NAME)
	private HibernateService service;
	
	@Override
	public int executionOrder() {
		return 1;
	}

	@Override
	public void startUp(Vertx vertx, Future<Void> future) {
		models = new ArrayList<>();
		models.add(new Dog("Snoopy", "Beagle"));
		models.add(new Dog("Bill", "Cocker"));
		models.add(new Dog("Rantanplan", "German_shepherd"));
		models.add(new Dog("Milou", "Fox_terrier"));
		models.add(new Dog("Idefix", "Westy"));
		models.add(new Dog("Pluto", "Mutt"));
		service.withEntityManager((entityManager, fut) -> {
			service.saveWithinTransaction(entityManager, models, AsyncUtils.ignoreResult(fut));
		}, AsyncUtils.ignoreResult(future));
	}

	@Override
	public void tearDown(Vertx vertx, Future<Void> future) {
		future.complete();
	}

}
