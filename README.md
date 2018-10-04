# Concurrent article repository through fine grained locks
Concurrent computing is a form of computing in which several computations are executed during overlapping time periods concurrently, instead of sequentially. Which has the advantage of exploiting the increasing number of cpu cores that todays modern machines have at their disposal. The design of concurrent-multi-threaded programs is not trivial tough, as there is a need to preserve consistency between shared resources while trying to minimize performance costs.

This solution implements a shared hash table with fine-grained locks to protect and control accessses of threads in each bucket.

**Info:**

We considered three different versions of the article repository, isolated in the following three versions of the file _Repository_:
* _RepositorySyncOperationGrained_ - Insert and remove operations are blocking so there is little concurrency (coarse grained).
* _RepositorySyncStructuresGrained_ - Insert and remove operations completely block access to the data structures (coarse grained)
* _Repository_ (**default**) - Operations on the hashtable are concurrent through the use of bucket level locks (middle grained), operations at the repository level only block the buckets required.
