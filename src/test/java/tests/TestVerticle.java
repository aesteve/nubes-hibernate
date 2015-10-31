package tests;

import static com.github.aesteve.vertx.nubes.utils.async.AsyncUtils.completeOrFail;
import static com.github.aesteve.vertx.nubes.utils.async.AsyncUtils.onSuccessOnly;

import com.github.aesteve.nubes.hibernate.HibernateNubes;
import com.github.aesteve.vertx.nubes.VertxNubes;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;

public class TestVerticle extends AbstractVerticle {
	private static final Logger log = LoggerFactory.getLogger(TestVerticle.class);

	public static final String HOST = "localhost";
	public static final int PORT = 8000;
	
	private VertxNubes mvc;

	@Override
	public void start(Future<Void> startFuture) throws Exception {
		HttpServerOptions options = new HttpServerOptions();
		options.setPort(PORT);
		options.setHost(HOST);
		HttpServer server = vertx.createHttpServer(options);
		JsonObject config = createTestConfig();
		
		mvc = new HibernateNubes(vertx, config);
		mvc.bootstrap(onSuccessOnly(startFuture, router -> {
			server.requestHandler(router::accept);
			server.listen();
			log.info("Server listening on port : " + PORT);
			startFuture.complete();
		}));
		
	}

	@Override
	public void stop(Future<Void> stopFuture) throws Exception {
		mvc.stop(completeOrFail(stopFuture));
	}

	private static JsonObject createTestConfig() {
		JsonObject config = new JsonObject();
		JsonArray controllerPackages = new JsonArray();
		controllerPackages.add("mock.controllers");
		config.put("controller-packages", controllerPackages);
		config.put("domain-package", "mock.domains");
		config.put("verticle-package", "mock.verticles");
		JsonArray fixturePackages = new JsonArray();
		fixturePackages.add("mock.fixtures");
		config.put("fixture-packages", fixturePackages);
		config.put("persistence-unit", "vertx-hibernate-tests");
		log.info("Config : " + config.toString());
		return config;
	}


}
