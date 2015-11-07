package com.github.aesteve.nubes.orm.hibernate.queries;

import java.util.ArrayList;
import java.util.List;

public class ListAndCount<T> {
	public List<T> list;
	public Long count;
	
	public ListAndCount() {
		list = new ArrayList<>();
		count = 0l;
	}
}
