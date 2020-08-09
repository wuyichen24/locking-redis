# Read-Write Lock

## Rules
- There are 2 types of locks: Read lock and write lock.
- Read lock
   - A thread/client must acquire a read lock before processing the read operations on a data record.
   - Multiple threads/clients can acquire multiple read locks on the same data record, if there is no write lock on that data record.
- Write lock
   - A thread/client must acquire a write lock before processing the write operations on a data record.
   - Only one thread/client can acquire a write lock on a data record, if there is no write lock or read lock on that data record.

## Design
### Data Model
