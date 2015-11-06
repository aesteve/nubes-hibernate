package tests;

import static io.vertx.core.http.HttpHeaders.ACCEPT;
import static io.vertx.core.http.HttpHeaders.CONTENT_TYPE;

import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;

import com.github.aesteve.nubes.hibernate.services.HibernateService;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;

@RunWith(VertxUnitRunner.class)
public abstract class HibernateNubesTestBase {

	protected JsonObject config;
	protected HibernateService service;
	protected Vertx vertx;

	@Before
	public void setUp(TestContext context) throws Exception {
		vertx = Vertx.vertx();
		DeploymentOptions options = new DeploymentOptions();
		vertx.deployVerticle("tests.TestVerticle", options, context.asyncAssertSuccess());
	}

	@After
	public void tearDown(TestContext context) throws Exception {
		if (vertx != null) {
			vertx.close(context.asyncAssertSuccess());
		}
	}

	protected void deleteJSON(String path, Handler<HttpClientResponse> responseHandler) {
		client().delete(path, responseHandler).putHeader(ACCEPT, "application/json").putHeader(CONTENT_TYPE, "application/json").end();
	}

	protected void sendJSON(String path, Object payload, Handler<HttpClientResponse> responseHandler) {
		client().post(path, responseHandler).putHeader(ACCEPT, "application/json").putHeader(CONTENT_TYPE, "application/json").end(payload.toString());
	}

	protected void putJSON(String path, Object payload, Handler<HttpClientResponse> responseHandler) {
		client().put(path, responseHandler).putHeader(ACCEPT, "application/json").putHeader(CONTENT_TYPE, "application/json").end(payload.toString());
	}

	protected void getJSON(String path, Handler<HttpClientResponse> responseHandler) {
		client().get(path, responseHandler).putHeader(ACCEPT, "application/json").end();
	}

	protected HttpClient client() {
		return vertx.createHttpClient(options());
	}

	private static HttpClientOptions options() {
		HttpClientOptions options = new HttpClientOptions();
		options.setDefaultHost(TestVerticle.HOST);
		options.setDefaultPort(TestVerticle.PORT);
		options.setKeepAlive(false);
		return options;
	}

}