package tests.specs.primitives;

import io.vertx.ext.unit.TestContext;
import mock.domains.Dog;
import mock.domains.Master;

import org.junit.Test;

import com.github.aesteve.nubes.hibernate.queries.FindById;

public class TestInsert extends TestBase {

	@Test
	public void simpleWithinTransactionDo(TestContext context) {
		hibernate.withinTransactionDo((em, future) -> {
			Dog d = new Dog("test-dog", "test-breed");
			em.persist(d);
			future.complete();
		}, context.asyncAssertSuccess());
	}

	@Test
	public void nestedWithinTransactionDo(TestContext context) {
		String masterName = "ImTheMaster";
		final Master master = new Master(masterName, "secret-pwd");
		hibernate.withinTransactionDo((em, future) -> {
			em.persist(master);
			future.complete();
		}, res -> {
			if (res.failed()) {
				context.fail(res.cause());
				return;
			}
			hibernate.withinTransactionDo((em, future) -> {
				Dog d = new Dog("test-dog", "test-breed");
				hibernate.findById(em, new FindById<>(Master.class, masterName), masterRes -> {
					if (masterRes.failed()) {
						context.fail(masterRes.cause());
						return;
					}
					context.assertNotNull(masterRes);
					d.setMaster(master);
					em.persist(d);
				});
			}, context.asyncAssertSuccess());
		});
	}
}
