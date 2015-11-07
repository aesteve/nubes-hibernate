package com.github.aesteve.nubes.orm.hibernate.queries;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

public class FindBy<T> {
	
	private Class<T> clazz;
	private Map<String, Object> restrictions;
	
	public FindBy(Class<T> clazz) {
		this.clazz = clazz;
		restrictions = new HashMap<>();
	}
	
	public FindBy(Class<T> clazz, String propertyName, Object propertyValue) {
		this(clazz);
		restrictions.put(propertyName, propertyValue);
	}
	
	public void addRestriction(String propertyName, Object propertyValue) {
		restrictions.put(propertyName, propertyValue);
	}
	
	public CriteriaQuery<T> toCriteriaQuery(CriteriaBuilder builder) {
		CriteriaQuery<T> crit = builder.createQuery(clazz);
		Root<T> root = crit.from(clazz);
		restrictions.forEach((key, value) -> {
			crit.where(builder.equal(root.get(key), value));
		});
		return crit;
	}
}
