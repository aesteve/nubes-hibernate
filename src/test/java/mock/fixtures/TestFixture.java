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
import mock.domains.Master;

public class TestFixture extends Fixture {

	public static List<Dog> dogs;
	public static List<Master> masters;
	
	@Service(HibernateNubes.HIBERNATE_SERVICE_NAME)
	private HibernateService service;
	
	@Override
	public int executionOrder() {
		return 1;
	}

	@Override
	public void startUp(Vertx vertx, Future<Void> future) {
		dogs = new ArrayList<>();
		masters = new ArrayList<>();
		dogs.add(new Dog("Snoopy", "Beagle"));
		dogs.add(new Dog("Bill", "Cocker"));
		dogs.add(new Dog("Rantanplan", "German_shepherd"));
		Master tintin = new Master("Tintin", "DestinationMoon");
		masters.add(tintin);
		Dog milou = new Dog("Milou", "Fox_terrier");
		milou.setMaster(tintin);
		dogs.add(milou);
		dogs.add(new Dog("Idefix", "Westy"));
		dogs.add(new Dog("Pluto", "Mutt"));
		List<Object> models = new ArrayList<>(dogs.size() + masters.size());
		models.addAll(masters); models.addAll(dogs);
		service.withEntityManager((entityManager, fut) -> {
			service.saveWithinTransaction(entityManager, models, AsyncUtils.ignoreResult(fut));
		}, AsyncUtils.ignoreResult(future));
	}

	@Override
	public void tearDown(Vertx vertx, Future<Void> future) {
		future.complete();
	}

}
