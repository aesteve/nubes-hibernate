package com.github.aesteve.nubes.hibernate;

import com.github.aesteve.vertx.nubes.NubesServer;

import io.vertx.core.Context;
import io.vertx.core.Vertx;

public class HibernateNubesServer extends NubesServer {

	@Override
	public void init(Vertx vertx, Context context) {
		super.init(vertx, context);
		nubes = new HibernateNubes(vertx, context.config());
	}
}
