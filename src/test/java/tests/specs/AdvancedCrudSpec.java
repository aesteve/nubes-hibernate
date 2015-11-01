package tests.specs;

import org.junit.Test;

import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.Async;
import tests.HibernateNubesTestBase;

public class AdvancedCrudSpec extends HibernateNubesTestBase {

	@Test
	public void testMyDogs(TestContext context) {
		Async async = context.async();
		getJSON("/api/myDogs?token=DestinationMoon", response -> {
			context.assertEquals(200, response.statusCode());
			response.bodyHandler(buff -> {
				context.assertTrue(buff.toString("UTF-8").contains("Milou"));
				async.complete();
			});
		});
	}
	
	@Test
	public void testAccessRefused(TestContext context) {
		Async async = context.async();
		getJSON("/api/myDogs?token=BashiBazouks", response -> {
			context.assertEquals(403, response.statusCode());
			async.complete();
		});
	}
}
