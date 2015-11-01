package tests.specs;

import org.junit.Test;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import mock.fixtures.DogFixture;
import tests.HibernateNubesTestBase;

public class SaveSpec extends HibernateNubesTestBase {

	@Test
	public void testSave(TestContext context) {
		Async async = context.async();
		JsonObject json = new JsonObject();
		String name = "Milou";
		String breed = "Fox";
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
	
	
	@Test
	public void getAllDogs(TestContext context) {
		Async async = context.async();
		getJSON("/api/dogs", response -> {
			context.assertEquals(200, response.statusCode());
			response.bodyHandler(buff -> {
				String json = buff.toString("UTF-8");
				JsonArray array = new JsonArray(json);
				context.assertEquals(DogFixture.models.size(), array.size());
				async.complete();
			});
		});
	}
	
	@Test
	public void getBeagles(TestContext context) {
		Async async = context.async();
		getJSON("/api/dogs?breed=Beagle", response -> {
			context.assertEquals(200, response.statusCode());
			response.bodyHandler(buff -> {
				String json = buff.toString("UTF-8");
				JsonArray array = new JsonArray(json);
				context.assertEquals(1, array.size());
				async.complete();
			});
		});
	}
	
	
}
