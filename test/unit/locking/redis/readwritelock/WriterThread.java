package locking.redis.readwritelock;

import locking.redis.Lock;
import locking.redis.readwritelock.WriteLock;
import redis.clients.jedis.Jedis;

/**
 * A single writer thread for testing read-write lock.
 * 
 * <p>Each single thread is processing the cycle of acquiring the lock, 
 * process the write operation and release the lock.
 * 
 * <p>The time of the write operation is random, which is between the upper boundary 
 * and lower boundary of the operation time.
 * 
 * @author  Wuyi Chen
 * @date    08/08/2020
 * @version 1.0
 * @since   1.0
 */
public class WriterThread implements Runnable {
	private Jedis                             conn;
	private Lock                              lock;
	private long operationTimeLowerBoundary = 4000L;
	private long operationTimeUpperBoundary = 10000L;
	
	public void run() {
		conn = new Jedis("localhost");
        conn.select(15);
        
        lock = new WriteLock();
		
		while (true) {
			// acquire the lock.
			System.out.println(Thread.currentThread().getId() + " writer is acquiring the write lock.");
			String identifier = lock.acquireLock(conn, "Student", "001", 10000);
			if (identifier == null) {
				System.out.println(Thread.currentThread().getId() + " writer is timeout for acquiring the write lock.");
			} else {
				System.out.println(Thread.currentThread().getId() + " writer has acquired the write lock.");
			
				// process the operation.
				try {
					long randomOperationTime = operationTimeLowerBoundary + (long) (Math.random() * (operationTimeUpperBoundary - operationTimeLowerBoundary));
					System.out.println(Thread.currentThread().getId() + " writer starts the write operation: " + randomOperationTime/1000 + " seconds.");
					Thread.sleep(randomOperationTime);
					System.out.println(Thread.currentThread().getId() + " writer finished the write operation: " + randomOperationTime/1000 + " seconds.");
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
				// release the lock.
			    System.out.println(Thread.currentThread().getId() + " writer is releasing the write lock.");
			    boolean isSuccess = lock.releaseLock(conn, "Student", "001", identifier);
			    if (isSuccess) {
			    	System.out.println(Thread.currentThread().getId() + " writer has releasd the write lock.");
			    } else {
			    	System.out.println(Thread.currentThread().getId() + " writer cannot release the write lock. ");
			    }
			}
			try {
				Thread.sleep(4000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
