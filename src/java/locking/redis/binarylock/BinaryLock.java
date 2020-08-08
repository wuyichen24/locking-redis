package locking.redis.binarylock;

import java.util.List;
import java.util.UUID;

import locking.redis.Lock;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;

/**
 * Implement binary locking protocol with Redis.
 * 
 * @author  Wuyi Chen
 * @date    08/05/2020
 * @version 1.0
 * @since   1.0
 */
public class BinaryLock implements Lock {
	@Override
	public String acquireLock(Jedis conn, String dataType, String id, long acquireTimeout) {
		String identifier = UUID.randomUUID().toString();                      // Generate a 128-bit identifier.
		String lockKey    = getLockKey(dataType, id);                          // Concatenate the key for the lock.
		long   end        = System.currentTimeMillis() + acquireTimeout;       // Calculate the final time point before giving up acquiring the lock.
		
		while (System.currentTimeMillis() < end) {
			if (conn.setnx(lockKey, identifier) == 1) {                        // Set the lock if the lock is not existing.
				return identifier;
			}

			try {
				Thread.sleep(1);                                               // Wait 1 milliseconds to try to acquire the lock again if the previous lock on the same record is not released.
			} catch (InterruptedException ie) {
				Thread.currentThread().interrupt();
			}
		}

		return null;                                                           // null indicates that the lock was not acquired
	}

	@Override
	public boolean releaseLock(Jedis conn, String dataType, String id, String identifier) {
		String lockKey = getLockKey(dataType, id);                             // Concatenate the key for the lock.

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
	
	/**
	 * Get the lock key based on data type and id.
	 * 
	 * @param  dataType
	 *         The type of the record. It can be the table name in the relational database.
	 *         
	 * @param  id
	 *         The unique identifier of the record. It can be primary keys in the relational database.
	 *         
	 * @return  The unique identifier of the lock.
	 */
	private String getLockKey(String dataType, String id) {
		return "lock:" + dataType + ":" + id;
	}
}
