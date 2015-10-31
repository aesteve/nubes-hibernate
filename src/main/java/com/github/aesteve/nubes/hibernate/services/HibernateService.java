package com.github.aesteve.nubes.hibernate.services;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Function;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.aesteve.nubes.hibernate.queries.FindById;
import com.github.aesteve.vertx.nubes.services.Service;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

public class HibernateService implements Service {

	protected final static Logger log = LoggerFactory.getLogger(HibernateService.class);
	
	private JsonObject config;
	public Vertx vertx;
	private EntityManagerFactory entityManagerFactory;
	private Map<String, EntityManager> managers;
	private Random rand;
	
	public HibernateService(JsonObject config) {
		this.config = config;
		managers = new HashMap<>();
		rand = new Random();
	}
	
	@Override
	public void init(Vertx vertx) {
		this.vertx = vertx;
		
	}

	@Override
	public void start(Future<Void> startFuture) {
		log.info("----- Startup Hibernate service");
		String persistenceUnit = config.getString("persistence-unit");
		if (persistenceUnit == null) {
			startFuture.fail("No persistence-unit specified in config");
			return;
		}
        vertx.executeBlocking(future -> {
        	try {
        		entityManagerFactory = Persistence.createEntityManagerFactory(persistenceUnit);
        		future.complete();
        	} catch(Exception e) {
        		future.fail(e);
        	}
        }, res -> {
        	if (res.succeeded()) {
        		log.info("----- Init done");
            	startFuture.complete();
        	} else {
        		log.info("----- Init failed");
            	startFuture.fail(res.cause());
        	}
        });
	}
	
	
	// Utility functions
	
	public<T> void withEntityManager(Function<EntityManager, T> blockingHandler, Handler<AsyncResult<T>> resultHandler) {
		vertx.executeBlocking(handler -> {
			EntityManager em = null;
			try {
				em = entityManagerFactory.createEntityManager();
				T result = blockingHandler.apply(em);
				handler.complete(result);
			} catch(Throwable t) {
				handler.fail(t);
			} finally {
				closeSilently(em);
			}
		}, resultHandler);
	}
	
	public<T> void withinTransaction(Function<EntityManager, T> blockingHandler, Handler<AsyncResult<T>> resultHandler) {
		vertx.executeBlocking(handler -> {
			EntityManager em = null;
			try {
				em = entityManagerFactory.createEntityManager();
				EntityTransaction tx = em.getTransaction();
				tx.begin();
				T result =  blockingHandler.apply(em);
				tx.commit();
				handler.complete(result);
			} catch(Throwable t) {
				handler.fail(t);
			} finally {
				closeSilently(em);
			}
		}, resultHandler);
	}
	
	public void createSession(Handler<AsyncResult<String>> handler) {
		vertx.executeBlocking(future -> {
			try {
				String sessionId = generateSessionId();
				EntityManager em = entityManagerFactory.createEntityManager();
				getMap().put(sessionId, em);
				future.complete(sessionId);
			} catch(Exception e) {
				future.fail(e);
			}
		}, handler);
	}

	public void beginTransaction(String sessionId, Handler<AsyncResult<Void>> handler) {
		EntityManager manager = getManager(sessionId, handler);
		if (manager == null) {
			return;
		} 
		vertx.executeBlocking(future -> {
			try {
				manager.getTransaction().begin();
				future.complete();
			} catch(Exception he) {
				future.fail(he);
			}
		}, handler);
	}
	
	public void flushSession(String sessionId, Handler<AsyncResult<Void>> handler) {
		EntityManager entityManager = getManager(sessionId, handler);
		if (entityManager == null) {
			return;
		} 
		vertx.executeBlocking(future -> {
			try {
				entityManager.flush();
				future.complete();
			} catch(Exception e) {
				future.fail(e);
			}
		}, handler);
	}
	
	public void clearSession(String sessionId, Handler<AsyncResult<Void>> handler) {
		EntityManager entityManager = getManager(sessionId, handler);
		if (entityManager == null) {
			return;
		} 
		vertx.executeBlocking(future -> {
			try {
				entityManager.clear();
				future.complete();
			} catch(Exception e) {
				future.fail(e);
			}
		}, handler);
	}
	
	public void flushAndClose(String sessionId, Handler<AsyncResult<Void>> handler) {
		EntityManager entityManager = getManager(sessionId, handler);
		if (entityManager == null) {
			return;
		} 
		vertx.executeBlocking(future -> {
			try {
				getMap().remove(sessionId);
				entityManager.flush();
				if (entityManager.getTransaction() != null) {
					entityManager.getTransaction().commit();
				}
				entityManager.close();
				future.complete();
			} catch(Exception e) {
				future.fail(e);
			}
		}, handler);
	}
	
	public<T> void saveWithinTransaction(String sessionId, T model, Handler<AsyncResult<T>> handler) {
		EntityManager entityManager = getManager(sessionId, handler);
		if (entityManager == null) {
			return;
		} 
		vertx.executeBlocking(future -> {
			try {
				EntityTransaction tx = entityManager.getTransaction();
				tx.begin();
				entityManager.persist(model);
				tx.commit();
				future.complete(model);
			} catch (Exception e) {
				future.fail(e);
			}
		}, handler);
	}
	
	public<T> void find(String sessionId, FindById<T> findById, Handler<AsyncResult<T>> handler) {
		EntityManager entityManager = getManager(sessionId, handler);
		if (entityManager == null) {
			return;
		} 
		vertx.executeBlocking(future -> {
			try {
				T result = entityManager.find(findById.clazz, findById.id);
				future.complete(result);
			} catch(Exception e) {
				future.fail(e);
			}
		}, handler);		
	}
	
	
	public CriteriaBuilder getCriteriaBuilder(String sessionId) {
		EntityManager entityManager = getManager(sessionId, res -> {});
		if (entityManager == null) {
			return null;
		}
		return entityManager.getCriteriaBuilder();
	}
	
	public void persist(String sessionId, Object model, Handler<AsyncResult<Void>> handler) {
		EntityManager entityManager = getManager(sessionId, handler);
		if (entityManager == null) {
			return;
		} 
		vertx.executeBlocking(future -> {
			try {
				entityManager.persist(model);
				future.complete();
			} catch (Exception e) {
				future.fail(e);
			}
		}, handler);
	}
	
	@SuppressWarnings("unchecked")
	public<T> void list(String sessionId, CriteriaQuery<T> criteria, Integer firstItem, Integer lastItem, Handler<AsyncResult<List<T>>> handler) {
		EntityManager entityManager = getManager(sessionId, handler);
		if (entityManager == null) {
			return;
		} 
		vertx.executeBlocking(future -> {
			try {
				Query query = entityManager.createQuery(criteria);
				if (firstItem != null) {
					query.setFirstResult(firstItem);
					if (lastItem != null) {
						query.setMaxResults(lastItem - firstItem);
					}
				}
				List<T> list = query.getResultList();
				future.complete(list);
			} catch (Exception e) {
				future.fail(e);
			}
		}, handler);		
	}
	
	@SuppressWarnings({ "unchecked" })
	public<T> void singleResult(String sessionId, CriteriaQuery<T> criteria, Handler<AsyncResult<T>> handler) {
		EntityManager entityManager = getManager(sessionId, handler);
		if (entityManager == null) {
			handler.handle(Future.failedFuture("No entity manager found with id : "+sessionId));
			return;
		} 
		vertx.executeBlocking(future -> {
			try {
				Query query = entityManager.createQuery(criteria);
				future.complete((T)query.getSingleResult());
			} catch (Exception e) {
				future.fail(e);
			}
		}, handler);			
	}
	
	// TODO : update, delete, merge, persist, createQuery, ...
	
	
	public void stop(Future<Void> future) {
		future.complete();
	}
	
	private String generateSessionId() {
		return "HibernateSession-" + System.currentTimeMillis() + "-" + rand.nextInt();
	}
	
	private<T> EntityManager getManager(String sessionId, Handler<AsyncResult<T>> handler) {
		EntityManager mgr = getMap().get(sessionId);
		if (mgr == null) {
			handler.handle(Future.failedFuture("No entity manager found for sessionId " + sessionId));
		}
		return mgr;
	}
	
	private Map<String, EntityManager> getMap() {
		return managers;
	}
	
	private static void closeSilently(EntityManager em) {
		try {
			if (em != null && em.isOpen()) {
				em.close();
			}
		} catch(RuntimeException re) {
			log.error("Could'nt close entitymanager", re);
		}

	}
}
