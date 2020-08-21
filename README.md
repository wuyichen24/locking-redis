# locking-redis

Use Redis (in-memory key–value database) to implement different locking algorithms for distributed concurrency control.

## Locking Algorithms
| Algorithm | Implementation | Demo |
|----|----|----|
| [**Binary Lock**](docs/Binary_Lock.md) | [BinaryLock.java](src/java/locking/redis/binarylock/BinaryLock.java) | [BinaryLockTest.java](test/unit/locking/redis/binarylock/BinaryLockTest.java) |
| [**Read-Write Lock**](docs/Read_Write_Lock.md) | <li>[ReadLock.java](src/java/locking/redis/readwritelock/ReadLock.java)<li>[WriteLock.java](src/java/locking/redis/readwritelock/WriteLock.java) | [ReadWriteLockTest.java](test/unit/locking/redis/readwritelock/ReadWriteLockTest.java) |
