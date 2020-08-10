# locking-redis

Use Redis (in-memory key–value database) to implement different locking algorithms for distributed concurrency control.

## Locking Algorithms
- [**Binary Lock**](docs/Binary_Lock.md) - [BinaryLock.java](src/java/locking/redis/binarylock/BinaryLock.java)
- [**Read-Write Lock**](docs/Read_Write_Lock.md) - [ReadLock.java](src/java/locking/redis/readwritelock/ReadLock.java), [WriteLock.java](src/java/locking/redis/readwritelock/WriteLock.java)
