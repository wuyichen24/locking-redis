package locking.redis.binarylocking;

import java.util.List;
import java.util.UUID;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;


/**
 * Implement binary locking protocol with Redis
 * 
 * @author  Wuyi Chen
 * @date    08/05/2020
 * @version 1.0
 * @since   1.0
 */
public class BinaryLock {
	/**
	 * Acquire a binary lock on a record with timeout.
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
	 *         The timeout of acquiring the lock.
	 * 
	 * @param  lockTimeout
	 *         The timeout of the lock.
	 *         
	 * @return  The identifier of the lock.
	 */
	public String acquireLockWithTimeout(Jedis conn, String dataType, String id, long acquireTimeout, long lockTimeout) {
		String identifier = UUID.randomUUID().toString();                      // Generate a 128-bit identifier.
		String lockKey = "lock:" + dataType + ":" + id;                        // Concatenate the key for the lock.
		int lockExpire = (int) (lockTimeout / 1000);                           // Calculate the lock timeout in seconds.

		long end = System.currentTimeMillis() + acquireTimeout;                // Calculate the final time point before giving up acquiring the lock.
		while (System.currentTimeMillis() < end) {
			if (conn.setnx(lockKey, identifier) == 1) {                        // Set the lock if the lock is not existing.
				conn.expire(lockKey, lockExpire);                              // Set the expiration time of the lock.
				return identifier;
			}
			if (conn.ttl(lockKey) == -1) {                                     // If there is no expiration time of the lock, set the expiration time of the lock.
				conn.expire(lockKey, lockExpire);
			}

			try {
				Thread.sleep(1);                                               // Wait 1 milliseconds to try to acquire the lock again if the previous lock on the same record is not released.
			} catch (InterruptedException ie) {
				Thread.currentThread().interrupt();
			}
		}

		return null;                                                           // null indicates that the lock was not acquired
	}

	/**
	 * Release a binary lock.
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
	public boolean releaseLock(Jedis conn, String dataType, String id, String identifier) {
		String lockKey = "lock:" + dataType + ":" + id;                        // Concatenate the key for the lock.

		while (true) {
			conn.watch(lockKey);                                               // Watch the lock to make sure there is no write on the lock during the transaction.
			if (identifier.equals(conn.get(lockKey))) {                        // Check there is no change for the identifier of the lock.
				Transaction trans = conn.multi();                              // Start the transaction which could execute multiple operations.
				trans.del(lockKey);                                            // Add the operation of deleting the lock.
				List<Object> results = trans.exec();                           // Execute the transaction.
				if (results == null) {                                         // If there is an error, try again.
					continue;
				}
				return true;
			}

			conn.unwatch();
			break;
		}

		return false;
	}
}
