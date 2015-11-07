package com.github.aesteve.nubes.orm.hibernate.queries;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import com.github.aesteve.nubes.orm.queries.FindBy;

public class CriteriaQueryBuilder<T> {
	
	private FindBy<T> findBy;
	
	
	public CriteriaQueryBuilder(FindBy<T> findBy) {
		this.findBy = findBy;
	}
	
	public CriteriaQuery<T> toCriteriaQuery(CriteriaBuilder builder) {
		Class<T> clazz = findBy.getType();
		CriteriaQuery<T> crit = builder.createQuery(clazz);
		Root<T> root = crit.from(clazz);
		findBy.getRestrictions().forEach((key, value) -> {
			crit.where(builder.equal(root.get(key), value));
		});
		return crit;
	}

}
