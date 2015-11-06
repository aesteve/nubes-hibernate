package tests.specs.primitives;

import org.junit.Before;
import org.junit.runner.RunWith;

import com.github.aesteve.nubes.hibernate.services.HibernateService;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

@RunWith(VertxUnitRunner.class)
public abstract class TestBase {

	protected HibernateService hibernate;

	@Before
	public void setUp(TestContext context) {
		hibernate = new HibernateService(config());
		hibernate.init(Vertx.vertx(), config());
		Future<Void> f = Future.future();
		f.setHandler(context.asyncAssertSuccess());
		hibernate.start(f);
	}

	protected JsonObject config() {
		JsonObject conf = new JsonObject();
		conf.put("persistence-unit", "vertx-hibernate-tests");
		return conf;
	}
}
