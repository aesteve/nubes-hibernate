package tests.specs.web;

import org.junit.Test;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import mock.fixtures.TestFixture;
import tests.HibernateNubesTestBase;

public class SimpleCrudSpec extends HibernateNubesTestBase {

	@Test
	public void testSave(TestContext context) {
		Async async = context.async();
		JsonObject json = new JsonObject();
		String name = "NotADog";
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
				context.assertEquals(TestFixture.dogs.size(), array.size());
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
	
	@Test
	public void removeDog(TestContext context) {
		Async async = context.async();
		deleteJSON("/api/dogs/Snoopy/", response -> {
			context.assertEquals(204, response.statusCode());
			getJSON("/api/dogs", getResponse -> {
				context.assertEquals(200, getResponse.statusCode());
				getResponse.bodyHandler(buff -> {
					String json = buff.toString("UTF-8");
					JsonArray array = new JsonArray(json);
					context.assertEquals(TestFixture.dogs.size() - 1, array.size());
					async.complete();
				});
			});
			
		});
		
	}

	@Test
	public void updateDog(TestContext context) {
		Async async = context.async();
		String newBreed = "notABeagle";
		JsonObject sent = new JsonObject().put("breed", newBreed);
		putJSON("/api/dogs/Snoopy/", sent, response -> {
			context.assertEquals(200, response.statusCode());
			getJSON("/api/dogs/Snoopy/", getResponse -> {
				context.assertEquals(200, getResponse.statusCode());
				getResponse.bodyHandler(buff -> {
					String json = buff.toString("UTF-8");
					JsonObject obj = new JsonObject(json);
					context.assertEquals("Snoopy", obj.getString("name"));
					context.assertEquals(newBreed, obj.getString("breed"));
					async.complete();
				});
			});
			
		});
		
	}

	
}
