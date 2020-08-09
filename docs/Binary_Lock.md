# Binary Lock

## Rules
- A lock on a data record can be in two states; it is either locked or unlocked.
- A thread/client must acquire a lock before processing the operations on a data record.
- A thread/client must release a lock after finishing processing the operations on a data record.
- If a thread/client has locked a data item, other threads/clients cannot acquire the lock on the same data record, they must wait until the original thread/client release the lock on that data record.

## Design
### Data Model
- A lock on a data record is represented as a key-value pair in Redis, value is String:
    - The format of key: `lock:<datatype>:<id>`
       - `<datatype>`: The type of the data record.
       - `<id>`: The unique identifier of the data record.
    - The format of value: `<identifier>`
       - `<identifier>`: The identifier of the lock. This identifier will be verified when releasing the lock.

### Logic
#### Acquire Lock
- If there is no lock on the data record, the thread/client can acquire a lock on the data record.
- If there is a lock the data record, the thread/client will wait to acquire a lock until the acquiring lock timeout is reached.

#### Release Lock
