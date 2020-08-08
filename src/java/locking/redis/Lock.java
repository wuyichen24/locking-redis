package locking.redis;

import redis.clients.jedis.Jedis;

/**
 * @author wuyichen
 *
 */
public interface Lock {
	/**
	 * Acquire a lock on a record with timeout.
	 * 
	 * @param  conn
	 *         The Redis connection.
	 *         
	 * @param  dataType
	 *         The type of the record. It can be the table name in the relational database.
	 * 
	 * @param  id
	 *         The unique identifier of the record. It can be primary keys in the relational database.
	 *         
	 * @param  acquireTimeout
	 *         The timeout of acquiring the lock in milliseconds.
	 *         
	 * @return  The identifier of the lock.
	 */
	public String acquireLock(Jedis conn, String dataType, String id, long acquireTimeout);
	
	/**
	 * Release a lock.
	 * 
	 * @param  conn
	 *         The Redis connection.
	 *         
	 * @param  dataType
	 *         The type of the record. It can be the table name in the relational database.
	 *         
	 * @param  id
	 *         The unique identifier of the record. It can be primary keys in the relational database.
	 *         
	 * @param  identifier
	 *         The identifier of the lock. This identifier will be used to check there is no change when releasing the lock.
	 *         
	 * @return  {@code true} if the lock has been release successfully, {@code false} otherwise.
	 */
	public boolean releaseLock(Jedis conn, String dataType, String id, String identifier);
}
