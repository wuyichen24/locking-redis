package locking.redis.experiment;

import java.util.Arrays;
import java.util.List;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Response;
import redis.clients.jedis.Transaction;

public class Test2 {

	public static void main(String[] args) {
		Jedis conn = new Jedis("localhost");
        conn.select(15);
        
        String readLockKey = "readlockzz";
        String writeLockKey = "writelockzz";
        String identifier = "abcdef";
        
        String script = conn.scriptLoad("if redis.call('exists', KEYS[1]) == 0 and redis.call('scard', KEYS[2]) == 0 then\n"  // If there is no write lock and read lock, set the write lock.
                +                       "    redis.call('set', KEYS[1], ARGV[1])\n"
                +                       "    return 1\n"
                +                       "end\n"
                +                       "return 0");
		
        List<String> keys = Arrays.asList(writeLockKey, readLockKey);
        List<String> arg  = Arrays.asList(identifier);
        Object response   = conn.evalsha(script, keys, arg);
        
        System.out.println(response);
		
        conn.unwatch();
	}

}
