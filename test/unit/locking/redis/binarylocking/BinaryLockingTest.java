package locking.redis.binarylocking;

/**
 * Test binary locking protocol implementation with Redis.
 * 
 * <p>This test class use 3 threads to simulate the race condition for updating 
 * the same record.
 * 
 * @author  Wuyi Chen
 * @date    08/06/2020
 * @version 1.0
 * @since   1.0
 */
public class BinaryLockingTest {
	public static void main(String args[]) throws InterruptedException {
		Thread t1 = new Thread(new WorkingThread());
		Thread t2 = new Thread(new WorkingThread());
		Thread t3 = new Thread(new WorkingThread());

		t1.start();
		Thread.sleep(2000);
		t2.start();
		Thread.sleep(2000);
		t3.start();
	}
}
