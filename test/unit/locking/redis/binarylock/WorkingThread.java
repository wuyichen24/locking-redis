package locking.redis.binarylock;

import locking.redis.Lock;
import locking.redis.binarylock.BinaryLock;
import redis.clients.jedis.Jedis;

/**
 * A single working thread for testing binary locking protocol.
 * 
 * <p>Each single thread is processing the cycle of acquiring the lock, 
 * process the operation and release the lock.
 * 
 * <p>The time of the operation is random, which is between the upper boundary 
 * and lower boundary of the operation time.
 * 
 * @author  Wuyi Chen
 * @date    08/06/2020
 * @version 1.0
 * @since   1.0
 */
public class WorkingThread implements Runnable {
	private Jedis                             conn;
	private Lock                              lock;
	private long operationTimeLowerBoundary = 4000L;
	private long operationTimeUpperBoundary = 10000L;
	
	public void run() {
		conn = new Jedis("localhost");
        conn.select(15);
        
        lock = new BinaryLock();
		
		while (true) {
			// acquire the lock.
			System.out.println(Thread.currentThread().getId() + " is acquiring the lock.");
			String identifier = lock.acquireLock(conn, "Student", "001", 10000);
			if (identifier == null) {
				System.out.println(Thread.currentThread().getId() + " is timeout for acquiring the lock.");
			} else {
				System.out.println(Thread.currentThread().getId() + " has acquired the lock.");
			
				// process the operation.
				try {
					long randomOperationTime = operationTimeLowerBoundary + (long) (Math.random() * (operationTimeUpperBoundary - operationTimeLowerBoundary));
					System.out.println(Thread.currentThread().getId() + " is start working on the operation: " + randomOperationTime/1000 + " seconds.");
					Thread.sleep(randomOperationTime);
					System.out.println(Thread.currentThread().getId() + " has finished the operation: " + randomOperationTime/1000 + " seconds.");
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
				// release the lock.
			    System.out.println(Thread.currentThread().getId() + " is releasing the lock.");
			    boolean isSuccess = lock.releaseLock(conn, "Student", "001", identifier);
			    if (isSuccess) {
			    	System.out.println(Thread.currentThread().getId() + " has releasd the lock.");
			    } else {
			    	System.out.println(Thread.currentThread().getId() + " cannot release the lock. ");
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
