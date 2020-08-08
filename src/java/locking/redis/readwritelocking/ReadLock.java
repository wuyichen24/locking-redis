package locking.redis.readwritelocking;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import locking.redis.Lock;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;

public class ReadLock implements Lock {
	@Override
	public String acquireLock(Jedis conn, String dataType, String id, long acquireTimeout) {
		String identifier   = UUID.randomUUID().toString();                              // Generate a 128-bit identifier.
		String readLockKey  = getLockKey(dataType, id);                                  // Concatenate the key for the read lock.
		String writeLockKey = WriteLock.getLockKey(dataType, id);
		
		long end = System.currentTimeMillis() + acquireTimeout;                          // Calculate the final time point before giving up acquiring the lock.
		
		while (System.currentTimeMillis() < end) {
			// Use Lua script to implement the conditional update with isolation guarantee.
			conn.watch(readLockKey, writeLockKey);
			
			Transaction trans = conn.multi(); 
			String script = conn.scriptLoad("if redis.call('exists', KEYS[1]) == 0 then\n"         // If there is no write lock, add this read lock into the read lock set.
					+                       "    redis.call('sadd', KEYS[2], ARGV[1])\n"
					+                       "    return 1\n"
					+                       "end\n"
					+                       "return 0");
			
			List<String> keys = Arrays.asList(writeLockKey, readLockKey);
	        List<String> args = Arrays.asList(identifier);
			trans.evalsha(script, keys, args);
	        
			List<Object> results = trans.exec();
			
			conn.unwatch();
			
			if (results != null && (Long) results.get(0) == 1L) {                        // If there is no other threads breaks the transaction and the read lock has been added successfully,
				return identifier;                                                       // return the identifier.
			}
			
			try {
				Thread.sleep(1);                                                         // Wait 1 milliseconds to try to acquire the lock again if there is a write lock or the transaction failed.
			} catch (InterruptedException ie) {
				Thread.currentThread().interrupt();
			}
		}
		
		return null;
	}

	@Override
	public boolean releaseLock(Jedis conn, String dataType, String id, String identifier) {
		String lockKey = getLockKey(dataType, id);                                       // Concatenate the key for the lock.

		while (true) {
			conn.watch(lockKey);                                                         // Watch the read lock to make sure there is no write on the read lock during the transaction.
			if (conn.sismember(lockKey, identifier)) {                                   // Check the set of read locks has this read lock.
				Transaction trans = conn.multi();                                        // Start the transaction which could execute multiple operations.
				trans.srem(lockKey, identifier);                                         // Add the operation of deleting the read lock from the read lock set.
				List<Object> results = trans.exec();                                     // Execute the transaction.
				if (results == null) {                                                   // If there is an error, try again.
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
	protected static String getLockKey(String dataType, String id) {
		return "readlock:" + dataType + ":" + id;
	}
}
