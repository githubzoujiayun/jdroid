package com.jdroid.java.repository;

import com.jdroid.java.collections.Lists;
import com.jdroid.java.collections.Maps;
import com.jdroid.java.domain.Identifiable;
import com.jdroid.java.utils.ReflectionUtils;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 
 * @param <T>
 */
public class InMemoryRepository<T extends Identifiable> implements Repository<T> {
	
	private long nextId = 1;
	private Map<Long, T> items = Maps.newLinkedHashMap();
	
	/**
	 * @see com.jdroid.java.repository.Repository#add(com.jdroid.java.domain.Identifiable)
	 */
	@Override
	public void add(T item) {
		if (item.getId() == null) {
			ReflectionUtils.setId(item, nextId++);
		}
		items.put(item.getId(), item);
	}
	
	/**
	 * @see com.jdroid.java.repository.Repository#addAll(java.util.Collection)
	 */
	@Override
	public void addAll(Collection<T> items) {
		for (T item : items) {
			add(item);
		}
	}
	
	/**
	 * @see com.jdroid.java.repository.Repository#update(com.jdroid.java.domain.Identifiable)
	 */
	@Override
	public void update(T item) {
		add(item);
	}
	
	/**
	 * @see com.jdroid.java.repository.Repository#remove(com.jdroid.java.domain.Identifiable)
	 */
	@Override
	public void remove(T item) {
		items.remove(item.getId());
	}
	
	/**
	 * @see com.jdroid.java.repository.Repository#removeAll(java.util.Collection)
	 */
	@Override
	public void removeAll(Collection<T> items) {
		for (T item : items) {
			remove(item);
		}
	}
	
	/**
	 * @see com.jdroid.java.repository.Repository#replaceAll(java.util.Collection)
	 */
	@Override
	public void replaceAll(Collection<T> items) {
		removeAll();
		addAll(items);
	}
	
	/**
	 * @see com.jdroid.java.repository.Repository#getAll()
	 */
	@Override
	public List<T> getAll() {
		return Lists.newArrayList(items.values());
	}
	
	/**
	 * @see com.jdroid.java.repository.Repository#get(java.lang.Long)
	 */
	@Override
	public T get(Long id) {
		return items.get(id);
	}
	
	/**
	 * @see com.jdroid.java.repository.Repository#removeAll()
	 */
	@Override
	public void removeAll() {
		items.clear();
	}
	
	/**
	 * @see com.jdroid.java.repository.Repository#remove(java.lang.Long)
	 */
	@Override
	public void remove(Long id) {
		items.remove(id);
	}
	
	/**
	 * @see com.jdroid.java.repository.Repository#isEmpty()
	 */
	@Override
	public Boolean isEmpty() {
		return items.isEmpty();
	}
	
	/**
	 * @see com.jdroid.java.repository.Repository#getSize()
	 */
	@Override
	public Long getSize() {
		return (long)items.size();
	}
	
	/**
	 * @see com.jdroid.java.repository.Repository#findByField(java.lang.String, java.lang.Object[])
	 */
	@Override
	public List<T> findByField(String fieldName, Object... values) {
		// TODO Auto-generated method stub
		return null;
	}
	
	/**
	 * @see com.jdroid.java.repository.Repository#getAll(java.util.List)
	 */
	@Override
	public List<T> getAll(List<Long> ids) {
		List<T> itemsList = Lists.newArrayList();
		for (Long each : ids) {
			T item = items.get(each);
			if (item != null) {
				itemsList.add(item);
			}
		}
		return itemsList;
	}
	
	/**
	 * @see com.jdroid.java.repository.Repository#getUniqueInstance()
	 */
	@Override
	public T getUniqueInstance() {
		return items.isEmpty() ? null : items.values().iterator().next();
	}
}
