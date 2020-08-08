package locking.redis.readwritelock;

import locking.redis.Lock;
import locking.redis.readwritelock.ReadLock;
import redis.clients.jedis.Jedis;

/**
 * A single reader thread for testing read-write lock.
 * 
 * <p>Each single thread is processing the cycle of acquiring the lock, 
 * process the read operation and release the lock.
 * 
 * <p>The time of the read operation is random, which is between the upper boundary 
 * and lower boundary of the operation time.
 * 
 * @author  Wuyi Chen
 * @date    08/08/2020
 * @version 1.0
 * @since   1.0
 */
public class ReaderThread implements Runnable {
	private Jedis                             conn;
	private Lock                              lock;
	private long operationTimeLowerBoundary = 4000L;
	private long operationTimeUpperBoundary = 10000L;
	
	public void run() {
		conn = new Jedis("localhost");
        conn.select(15);
        
        lock = new ReadLock();
		
		while (true) {
			// acquire the lock.
			System.out.println(Thread.currentThread().getId() + " reader is acquiring the read lock.");
			String identifier = lock.acquireLock(conn, "Student", "001", 10000);
			if (identifier == null) {
				System.out.println(Thread.currentThread().getId() + " reader is timeout for acquiring the read lock.");
			} else {
				System.out.println(Thread.currentThread().getId() + " reader has acquired the read lock.");
			
				// process the operation.
				try {
					long randomOperationTime = operationTimeLowerBoundary + (long) (Math.random() * (operationTimeUpperBoundary - operationTimeLowerBoundary));
					System.out.println(Thread.currentThread().getId() + " reader starts the read operation: " + randomOperationTime/1000 + " seconds.");
					Thread.sleep(randomOperationTime);
					System.out.println(Thread.currentThread().getId() + " reader finished the read operation: " + randomOperationTime/1000 + " seconds.");
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
				// release the lock.
			    System.out.println(Thread.currentThread().getId() + " reader is releasing the read lock.");
			    boolean isSuccess = lock.releaseLock(conn, "Student", "001", identifier);
			    if (isSuccess) {
			    	System.out.println(Thread.currentThread().getId() + " reader has released the read lock.");
			    } else {
			    	System.out.println(Thread.currentThread().getId() + " reader cannot release the read lock. ");
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
