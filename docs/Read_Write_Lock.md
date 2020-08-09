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
- A read lock on a data record is represented as a key-value pair in Redis, value is Set:
   - The format of key: `readlock:<datatype>:<id>`
      - `<datatype>`: The type of the data record.
      - `<id>`: The unique identifier of the data record.
   - The format of value (Set): `<identifier1><identifier2>....<identifierN>`
      - The set is consists of multiple identifiers, each identifier is for one read lock.
- A write lock on a data record is represented as a key-value pair in Redis, value is String:
   - The format of key: `writelock:<datatype>:<id>`
      - `<datatype>`: The type of the data record.
      - `<id>`: The unique identifier of the data record.
   - The format of value: <identifier>
      - <identifier>: The identifier of the write lock. This identifier will be verified when releasing the write lock.
