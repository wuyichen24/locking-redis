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
   - The format of value: `<identifier>`
      - `<identifier>`: The identifier of the write lock. This identifier will be verified when releasing the write lock.

### Logic
#### Acquire Read Lock
- If there is no write lock on the data record, the thread/client can acquire a read lock on the data record.
   - Add the lock identifier into the Set of the identifiers for the read lock on the same data record (This check-and-set operation is implemented in Lua script, to avoid other threads/clients to intervene the resources during the operation).
   - Return the lock identifer to the thread/client.
   
#### Release Read Lock
- Check the lock identifier, which is provided by the thread/client, is existing in the Set of the identifiers for the read locks on the same data record.
- Remove the lock identifier from the Set.

#### Acquire Write Lock
- If there is no write lock and read lock on the data record, the thread/client can acquire a write lock on the data record.
   - Add a new key-value pair in Redis: { key=`writelock:<datatype>:<id>`, value=`<identifier>` }
   - Return the lock identifer to the thread/client.
   
#### Release Write Lock
- Check there is no change on the lock identifier (Compare the lock identifer provided by the thread/client with the identifier of the write lock in Redis).
- Delete the key-value pair for the write lock.
