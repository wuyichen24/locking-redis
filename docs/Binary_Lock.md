# Binary Lock

## Logic
- A lock on a data record can be in two states; it is either locked or unlocked.
- A transcation must acquire a lock before processing the operations on a data record.
- A transcation must release a lock after finishing processing the operations on a data record.
- If a transcation has locked a data item, other transcations cannot acquire the lock on the same data record, they must wait until the original transcation release the lock on that data record. 
