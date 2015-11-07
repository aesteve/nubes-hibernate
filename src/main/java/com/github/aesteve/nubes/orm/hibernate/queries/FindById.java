package com.github.aesteve.nubes.orm.hibernate.queries;

import java.io.Serializable;

public class FindById<T> {
	public Class<T> clazz;
	public Serializable id;
	
	
	public FindById(Class<T> clazz, Serializable id) {
		this.clazz = clazz;
		this.id = id;
	}
	
	@Override
	public String toString() {
		return "Find " + clazz + " by id : " + id;
	}
}
