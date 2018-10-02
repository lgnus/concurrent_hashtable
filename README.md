# Concurrent article repository through fine grained locks
Concurrent computing is a form of computing in which several computations are executed during overlapping time periods concurrently, instead of sequentially. Which has the advantage of exploiting the increasing number of cpu cores that todays modern machines have at their disposal. The design of concurrent-multi-threaded programs is not trivial tough, as there is a need to preserve consistency between shared resources while trying to minimize performance costs.

This solution implements a shared hash table with fine-grained locks to protect and control accessses of threads in each bucket.
