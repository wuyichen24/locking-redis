package locking.redis.readwritelock;

import redis.clients.jedis.Jedis;

/**
 * Test read-write lock with Redis.
 * 
 * <p>This test class use 2 reader threads and 2 writer threads to simulate 
 * the race condition for reading and writing on the same record.
 * 
 * @author  Wuyi Chen
 * @date    08/08/2020
 * @version 1.0
 * @since   1.0
 */
public class ReadWriteLockTest {
	public static void main(String args[]) throws InterruptedException {
		// remove all the keys to avoid to cause confusion during the testing.
		Jedis conn = new Jedis("localhost");
        conn.select(15);
        conn.flushAll();
        conn.close();
		
		Thread rt1 = new Thread(new ReaderThread());
		Thread rt2 = new Thread(new ReaderThread());
		Thread wt1 = new Thread(new WriterThread());
		Thread wt2 = new Thread(new WriterThread());

		rt1.start();
		Thread.sleep(1000);
		rt2.start();
		Thread.sleep(1000);
		wt1.start();
		Thread.sleep(1000);
		wt2.start();
	}
}
