package locking.redis.experiment;

import java.util.Arrays;
import java.util.List;

import redis.clients.jedis.Jedis;

public class Test {

	public static void main(String[] args) {
		Jedis conn = new Jedis("localhost");
        conn.select(15);
        
        System.out.println(conn.scard("bb"));
        
		String script = conn.scriptLoad("if redis.call('exists', KEYS[1]) == 0 then\n"
				+                       "    redis.call('sadd', KEYS[2], ARGV[1])\n"
				+                       "    redis.call('set',  'qqqq', 'pppp')\n"
				+                       "    return 1\n"
				+                       "end\n"
				+                       "return 0");
        
        List<String> keys = Arrays.asList("aa", "bb");
        List<String> arg = Arrays.asList("yy");
        Object x = conn.evalsha(script, keys, arg);
        
        System.out.println(conn.scard("bb"));
        System.out.println((Long) x);
        System.out.println(conn.smembers("bb"));
        System.out.println(conn.get("qqqq"));
	}

}
