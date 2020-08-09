# Binary Lock

## Logic
- A lock on a data record can be in two states; it is either locked or unlocked.
- A thread/client must acquire a lock before processing the operations on a data record.
- A thread/client must release a lock after finishing processing the operations on a data record.
- If a thread/client has locked a data item, other threads/clients cannot acquire the lock on the same data record, they must wait until the original thread/client release the lock on that data record.
