package locking.redis.readwritelock;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import locking.redis.Lock;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;

/**
 * Implement the write lock of the read-write lock protocol with Redis.
 * 
 * <p>This write lock class use the Lua script and Redis transaction to 
 * implement check-and-set (CAS) when acquiring the lock.
 * 
 * @author  Wuyi Chen
 * @date    08/08/2020
 * @version 1.0
 * @since   1.0
 */
public class WriteLock implements Lock {
    @Override
    public String acquireLock(Jedis conn, String dataType, String id, long acquireTimeout) {
        String identifier   = UUID.randomUUID().toString();                              // Generate a 128-bit identifier.
        String writeLockKey = getLockKey(dataType, id);                                  // Concatenate the key for the write lock.
        String readLockKey  = ReadLock.getLockKey(dataType, id);                         // Concatenate the key for the read lock.
        long   end          = System.currentTimeMillis() + acquireTimeout;               // Calculate the final time point before giving up acquiring the lock.
		
        while (System.currentTimeMillis() < end) {
            // Use Lua script to implement the conditional update with isolation guarantee.
            String script = conn.scriptLoad("if redis.call('exists', KEYS[1]) == 0 and redis.call('scard', KEYS[2]) == 0 then\n"  // If there is no write lock and read lock, set the write lock.
                    +                       "    redis.call('set', KEYS[1], ARGV[1])\n"
                    +                       "    return 1\n"
                    +                       "end\n"
                    +                       "return 0");
						
            List<String> keys = Arrays.asList(writeLockKey, readLockKey);
            List<String> args = Arrays.asList(identifier);
            Object result = conn.evalsha(script, keys, args);
			
            if (result != null && (Long) result == 1L) {                                 // If there is no other threads breaks the transaction and the write lock has been set successfully,
                return identifier;                                                       // return the identifier.
            }
			
            try {
                Thread.sleep(1);                                                         // Wait 1 milliseconds to try to acquire the lock again if there has a write lock or read locks.
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
        }
		
        return null;
    }

    @Override
    public boolean releaseLock(Jedis conn, String dataType, String id, String identifier) {
        String lockKey = getLockKey(dataType, id);                             // Concatenate the key for the lock.

        while (true) {
            conn.watch(lockKey);                                               // Watch the write lock to make sure there is no write on the write lock during the transaction.
            if (identifier.equals(conn.get(lockKey))) {                        // Check there is no change for the identifier of the write lock.
                Transaction trans = conn.multi();                              // Start the transaction which could execute multiple operations.
                trans.del(lockKey);                                            // Add the operation of deleting the write lock.
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
    protected static String getLockKey(String dataType, String id) {
        return "writelock:" + dataType + ":" + id;
    }
}
