package tests.specs;

import org.junit.Test;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import tests.HibernateNubesTestBase;

public class SaveSpec extends HibernateNubesTestBase {
	
	@Test
	public void testSave(TestContext context) {
		Async async = context.async();
		JsonObject json = new JsonObject();
		String name = "Snoopy";
		String breed = "Beagle";
		json.put("name", name);
		json.put("breed", breed);
		sendJSON("/api/dogs", json, response -> {
			response.bodyHandler(buff -> {
				String body = buff.toString("UTF-8");
				context.assertTrue(body.contains(name) && body.contains(breed));
				getJSON("/api/dogs/" + name + "/", getResponse -> {
					context.assertEquals(200, getResponse.statusCode());
					getResponse.bodyHandler(getBuff -> {
						context.assertEquals(body, getBuff.toString("UTF-8"));
						async.complete();
					});
				});
			});
		});
	}
}
