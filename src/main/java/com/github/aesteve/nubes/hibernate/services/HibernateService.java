package com.github.aesteve.nubes.hibernate.services;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.BiConsumer;
import java.util.function.Function;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.NoResultException;
import javax.persistence.Persistence;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.aesteve.nubes.hibernate.queries.FindBy;
import com.github.aesteve.nubes.hibernate.queries.FindById;
import com.github.aesteve.nubes.hibernate.queries.ListAndCount;
import com.github.aesteve.vertx.nubes.services.Service;

public class HibernateService implements Service {

	public static String SESSION_ID_CTX = "hibernate-session-id";

	protected final static Logger log = LoggerFactory.getLogger(HibernateService.class);

	protected JsonObject config;
	protected Vertx vertx;

	private EntityManagerFactory entityManagerFactory;
	private Map<String, EntityManager> managers;
	private Random rand;

	public HibernateService(JsonObject config) {
		managers = new HashMap<>();
		rand = new Random();
	}

	@Override
	public void init(Vertx vertx, JsonObject config) {
		this.vertx = vertx;
		this.config = config;
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
			} catch (Exception e) {
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
	public <T> void withEntityManager(BiConsumer<EntityManager, Future<T>> consumer, Handler<AsyncResult<T>> resultHandler) {
		vertx.executeBlocking(future -> {
			EntityManager em = entityManagerFactory.createEntityManager();
			Future<T> handlerFuture = Future.future();
			handlerFuture.setHandler(res -> {
				closeSilently(em);
				if (res.failed()) {
					future.fail(res.cause());
				} else {
					future.complete(res.result());
				}
			});
			consumer.accept(em, handlerFuture);
		}, resultHandler);
	}

	public <T> void withEntityManager(Function<EntityManager, T> blockingHandler, Handler<AsyncResult<T>> resultHandler) {
		vertx.executeBlocking(handler -> {
			EntityManager em = null;
			try {
				em = entityManagerFactory.createEntityManager();
				T result = blockingHandler.apply(em);
				handler.complete(result);
			} catch (Throwable t) {
				handler.fail(t);
			} finally {
				closeSilently(em);
			}
		}, resultHandler);
	}

	public <T> void withinTransaction(Function<EntityManager, T> blockingHandler, Handler<AsyncResult<T>> resultHandler) {
		vertx.executeBlocking(handler -> {
			EntityManager em = null;
			try {
				em = entityManagerFactory.createEntityManager();
				EntityTransaction tx = em.getTransaction();
				tx.begin();
				T result = blockingHandler.apply(em);
				tx.commit();
				handler.complete(result);
			} catch (Throwable t) {
				handler.fail(t);
			} finally {
				closeSilently(em);
			}
		}, resultHandler);
	}

	public void withinTransactionDo(BiConsumer<EntityManager, Future<Object>> actions, Handler<AsyncResult<Object>> handler) {
		vertx.executeBlocking(future -> {
			try {
				final EntityManager em = entityManagerFactory.createEntityManager();
				final EntityTransaction tx = em.getTransaction();
				future.setHandler(res -> {
					tx.commit();
					closeSilently(em);
					handler.handle(res);
				});
				tx.begin();
				actions.accept(em, future);
			} catch (Throwable t) {
				future.fail(t);
			}
		}, res -> {
			handler.handle(res);
		});
	}

	public void createSession(Handler<AsyncResult<String>> handler) {
		vertx.executeBlocking(future -> {
			try {
				String sessionId = generateSessionId();
				EntityManager em = entityManagerFactory.createEntityManager();
				getMap().put(sessionId, em);
				future.complete(sessionId);
			} catch (Exception e) {
				future.fail(e);
			}
		}, handler);
	}

	public void beginTransaction(String sessionId, Handler<AsyncResult<Void>> handler) {
		EntityManager manager = getManager(sessionId, handler);
		if (manager == null) {
			handler.handle(Future.failedFuture("No entity manager found"));
			return;
		}
		vertx.executeBlocking(future -> {
			try {
				manager.getTransaction().begin();
				future.complete();
			} catch (Exception he) {
				future.fail(he);
			}
		}, handler);
	}

	public void flushSession(String sessionId, Handler<AsyncResult<Void>> handler) {
		EntityManager entityManager = getManager(sessionId, handler);
		if (entityManager == null) {
			handler.handle(Future.failedFuture("No entity manager found"));
			return;
		}
		vertx.executeBlocking(future -> {
			try {
				entityManager.flush();
				future.complete();
			} catch (Exception e) {
				future.fail(e);
			}
		}, handler);
	}

	public void clearSession(String sessionId, Handler<AsyncResult<Void>> handler) {
		EntityManager entityManager = getManager(sessionId, handler);
		if (entityManager == null) {
			handler.handle(Future.failedFuture("No entity manager found"));
			return;
		}
		vertx.executeBlocking(future -> {
			try {
				entityManager.clear();
				future.complete();
			} catch (Exception e) {
				future.fail(e);
			}
		}, handler);
	}

	public void flushAndClose(String sessionId, Handler<AsyncResult<Void>> handler) {
		EntityManager entityManager = getManager(sessionId, handler);
		if (entityManager == null) {
			handler.handle(Future.failedFuture("No entity manager found"));
			return;
		}
		vertx.executeBlocking(future -> {
			try {
				getMap().remove(sessionId);
				EntityTransaction tx = entityManager.getTransaction();
				if (tx != null) {
					if (tx.isActive()) {
						entityManager.flush();
					}
					tx.commit();
				}
				entityManager.close();
				future.complete();
			} catch (Exception e) {
				future.fail(e);
			}
		}, handler);
	}

	public <T> void removeWithinTransaction(String sessionId, FindById<T> findById, Handler<AsyncResult<Void>> handler) {
		EntityManager entityManager = getManager(sessionId, handler);
		if (entityManager == null) {
			handler.handle(Future.failedFuture("No entity manager found"));
			return;
		}
		vertx.executeBlocking(future -> {
			try {
				EntityTransaction tx = entityManager.getTransaction();
				tx.begin();
				T model = entityManager.find(findById.clazz, findById.id);
				if (model == null) {
					future.fail("Could not find model");
					return;
				}
				entityManager.remove(model);
				tx.commit();
				future.complete();
			} catch (Exception e) {
				future.fail(e);
			}
		}, handler);
	}

	public <T> void saveWithinTransaction(String sessionId, T model, Handler<AsyncResult<T>> handler) {
		EntityManager entityManager = getManager(sessionId, handler);
		if (entityManager == null) {
			handler.handle(Future.failedFuture("No entity manager found"));
			return;
		}
		saveWithinTransaction(entityManager, model, handler);
	}

	public <T> void saveWithinTransaction(EntityManager entityManager, T model, Handler<AsyncResult<T>> handler) {
		List<T> models = new ArrayList<>(1);
		models.add(model);
		saveWithinTransaction(entityManager, models, res -> {
			if (res.failed()) {
				handler.handle(Future.failedFuture(res.cause()));
			} else {
				handler.handle(Future.succeededFuture(res.result().get(0)));
			}
		});
	}

	public <T> void saveWithinTransaction(String sessionId, List<T> models, Handler<AsyncResult<List<T>>> handler) {
		EntityManager entityManager = getManager(sessionId, handler);
		if (entityManager == null) {
			handler.handle(Future.failedFuture("No entity manager found"));
			return;
		}
		saveWithinTransaction(entityManager, models, handler);
	}

	public <T> void saveWithinTransaction(EntityManager entityManager, List<T> models, Handler<AsyncResult<List<T>>> handler) {
		vertx.executeBlocking(future -> {
			try {
				EntityTransaction tx = entityManager.getTransaction();
				tx.begin();
				models.forEach(model -> {
					entityManager.persist(model);
				});
				tx.commit();
				future.complete(models);
			} catch (Exception e) {
				future.fail(e);
			}
		}, handler);
	}

	public <T> void updateWithinTransaction(String sessionId, T model, Handler<AsyncResult<T>> handler) {
		EntityManager entityManager = getManager(sessionId, handler);
		if (entityManager == null) {
			handler.handle(Future.failedFuture("No entity manager found"));
			return;
		}
		updateWithinTransaction(entityManager, model, handler);
	}

	public <T> void updateWithinTransaction(EntityManager entityManager, T model, Handler<AsyncResult<T>> handler) {
		vertx.executeBlocking(future -> {
			try {
				EntityTransaction tx = entityManager.getTransaction();
				tx.begin();
				entityManager.merge(model);
				tx.commit();
				future.complete(model);
			} catch (Exception e) {
				future.fail(e);
			}
		}, handler);
	}

	public <T> void findBy(String sessionId, FindBy<T> findBy, Handler<AsyncResult<T>> handler) {
		EntityManager entityManager = getManager(sessionId, handler);
		if (entityManager == null) {
			handler.handle(Future.failedFuture("No entity manager found"));
			return;
		}
		findBy(entityManager, findBy, handler);
	}

	@SuppressWarnings("unchecked")
	public <T> void findBy(EntityManager entityManager, FindBy<T> findBy, Handler<AsyncResult<T>> handler) {
		vertx.executeBlocking(future -> {
			CriteriaBuilder builder = entityManager.getCriteriaBuilder();
			CriteriaQuery<T> crit = findBy.toCriteriaQuery(builder);
			Query q = entityManager.createQuery(crit);
			try {
				T result = (T) q.getSingleResult();
				future.complete(result);
			} catch (NoResultException nre) {
				future.complete(null);
			} catch (Exception e) {
				future.fail(e);
			}
		}, handler);
	}

	public <T> void findById(String sessionId, FindById<T> findById, Handler<AsyncResult<T>> handler) {
		EntityManager entityManager = getManager(sessionId, handler);
		if (entityManager == null) {
			handler.handle(Future.failedFuture("No entity manager found"));
			return;
		}
		findById(entityManager, findById, handler);
	}

	public <T> void findById(EntityManager entityManager, FindById<T> findById, Handler<AsyncResult<T>> handler) {
		vertx.executeBlocking(future -> {
			try {
				T result = entityManager.find(findById.clazz, findById.id);
				future.complete(result);
			} catch (Exception e) {
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
			handler.handle(Future.failedFuture("No entity manager found"));
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
	public <T> void list(String sessionId, CriteriaQuery<T> criteria, Integer firstItem, Integer lastItem, Handler<AsyncResult<List<T>>> handler) {
		EntityManager entityManager = getManager(sessionId, handler);
		if (entityManager == null) {
			handler.handle(Future.failedFuture("No entity manager found"));
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

	@SuppressWarnings("unchecked")
	public <T> void listAndCount(String sessionId, CriteriaQuery<T> criteria, Integer firstItem, Integer lastItem, Handler<AsyncResult<ListAndCount<T>>> handler) {
		EntityManager entityManager = getManager(sessionId, handler);
		if (entityManager == null) {
			handler.handle(Future.failedFuture("No entity manager found"));
			return;
		}
		vertx.executeBlocking(future -> {
			try {
				ListAndCount<T> listAndCount = new ListAndCount<>();
				Query query = entityManager.createQuery(criteria);
				if (firstItem != null) {
					query.setFirstResult(firstItem);
					if (lastItem != null) {
						query.setMaxResults(lastItem - firstItem);
					}
				}
				listAndCount.list = query.getResultList();
				CriteriaBuilder builder = entityManager.getCriteriaBuilder();
				CriteriaQuery<Long> countCritQuery = builder.createQuery(Long.class);

				countCritQuery.select(builder.count(countCritQuery.from(criteria.getResultType())));
				if (criteria.getRestriction() != null) {
					countCritQuery.where(criteria.getRestriction());
				}
				listAndCount.count = entityManager.createQuery(countCritQuery).getSingleResult();
				future.complete(listAndCount);
			} catch (Exception e) {
				future.fail(e);
			}
		}, handler);
	}

	@SuppressWarnings({ "unchecked" })
	public <T> void singleResult(String sessionId, CriteriaQuery<T> criteria, Handler<AsyncResult<T>> handler) {
		EntityManager entityManager = getManager(sessionId, handler);
		if (entityManager == null) {
			handler.handle(Future.failedFuture("No entity manager found with id : " + sessionId));
		}
		vertx.executeBlocking(future -> {
			try {
				Query query = entityManager.createQuery(criteria);
				future.complete((T) query.getSingleResult());
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

	public <T> EntityManager getManager(String sessionId, Handler<AsyncResult<T>> handler) {
		EntityManager mgr = getMap().get(sessionId);
		if (mgr == null && handler != null) {
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
		} catch (RuntimeException re) {
			log.error("Could'nt close entitymanager", re);
		}

	}
}
