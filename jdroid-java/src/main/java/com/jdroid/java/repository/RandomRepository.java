package com.jdroid.java.repository;

import java.util.List;
import com.jdroid.java.domain.Identifiable;

/**
 * {@link Repository} with the capability of getting random objects.
 * 
 * @param <T> Type of object this repository deals with.
 */
public interface RandomRepository<T extends Identifiable> extends Repository<T> {
	
	/**
	 * Gets a random list of objects from the repository.
	 * 
	 * @param maxResults Maximum quantity of objects to get.
	 * @return The random list.
	 */
	public List<T> getRandomList(Integer maxResults);
	
	/**
	 * Gets a random object from the repository.
	 * 
	 * @return The random object.
	 */
	public T getRandom();
	
	/**
	 * Returns a list of random IDs.
	 * 
	 * @param maxResults Maximum amount of IDs to return.
	 * @return The random list.
	 */
	public List<Long> getRandomIds(Integer maxResults);
}
