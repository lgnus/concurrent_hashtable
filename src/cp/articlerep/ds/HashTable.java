package cp.articlerep.ds;

import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author José Carneiro, Ricardo Dias, Ruben Ramalho
 */
public class HashTable<K extends Comparable<K>, V> implements Map<K, V> {

	private static class Node {
		public Object key;
		public Object value;
		public Node next;

		public Node(Object key, Object value, Node next) {
			this.key = key;
			this.value = value;
			this.next = next;
		}
	}

	/**
	 * 
	 */
	private static class Bucket extends ReentrantReadWriteLock {
		public Node next;

		public Bucket() {
			super();
		}
	}
	
	private Bucket[] table;

	public HashTable() {
		this(1000);
	}

	public HashTable(int size) {
		this.table = new Bucket[size];
		for (int i = 0; i < size; i++) {
			this.table[i] = new Bucket();
		}
	}

	private int calcTablePos(K key) {
		return Math.abs(key.hashCode()) % this.table.length;
	}

	@SuppressWarnings("unchecked")
	@Override
	public V put(K key, V value) {

		// Get the bucket where the key is
		int pos = this.calcTablePos(key);

		this.table[pos].writeLock().lock();
		try { // lock the bucket

			// Skip the bucket
			Node n = this.table[pos].next;

			// while there's a next node whose key
			// doesn't match ours, fetch next node
			while (n != null && !n.key.equals(key)) {
				n = n.next;
			}

			// if it already exists
			if (n != null) {
				V oldValue = (V) n.value;
				n.value = value;
				return oldValue;
			}

			// else create a new one (while keeping the bucket)
			Node nn = new Node(key, value, this.table[pos].next);
			this.table[pos].next = nn;

			return null;

		} finally { // release the lock on this bucket
			this.table[pos].writeLock().unlock();
		}

	}

	@SuppressWarnings("unchecked")
	@Override
	public V remove(K key) {
		int pos = this.calcTablePos(key);

		this.table[pos].writeLock().lock();
		try { // lock the bucket

			Node p = this.table[pos].next; // find first node

			// if it doesn't exist, exit
			if (p == null) {
				return null;
			}

			// delete if key matches
			if (p.key.equals(key)) {
				this.table[pos].next = p.next;
				return (V) p.value;
			}

			// if key doesn't match, get the next
			Node n = p.next;
			while (n != null && !n.key.equals(key)) {
				p = n;
				n = n.next;
			}

			// exit if key isn't in the bucket
			if (n == null) {
				return null;
			}

			// if the key matches
			p.next = n.next;
			return (V) n.value;

		} finally { // release the lock on this bucket
			this.table[pos].writeLock().unlock();
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public V get(K key) {
		int pos = this.calcTablePos(key);

		this.table[pos].readLock().lock();
		try { // read lock this bucket
			Node n = this.table[pos].next;
			while (n != null && !n.key.equals(key)) {
				n = n.next;
			}
			return (V) (n != null ? n.value : null);

		} finally { // release the lock on this bucket
			this.table[pos].readLock().unlock();
		}
	}

	@Override
	public boolean contains(K key) {
		return get(key) != null;
	}

	/**
	 * No need to protect this method from concurrent interactions
	 */
	@Override
	public Iterator<V> values() {
		return new Iterator<V>() {

			private int pos = -1;
			private Node nextBucket = advanceToNextBucket();

			private Node advanceToNextBucket() {
				pos++;
				while (pos < HashTable.this.table.length && HashTable.this.table[pos].next == null) {
					pos++;
				}
				if (pos < HashTable.this.table.length)
					return HashTable.this.table[pos].next;

				return null;
			}

			@Override
			public boolean hasNext() {
				return nextBucket != null;
			}

			@SuppressWarnings("unchecked")
			@Override
			public V next() {
				V result = (V) nextBucket.value;

				nextBucket = nextBucket.next != null ? nextBucket.next : advanceToNextBucket();

				return result;
			}
		};
	}

	@Override
	public Iterator<K> keys() {
		return new Iterator<K>() {

			private int pos = -1;
			private Node nextBucket = advanceToNextBucket();

			private Node advanceToNextBucket() {
				pos++;
				while (pos < HashTable.this.table.length && HashTable.this.table[pos].next == null) {
					pos++;
				}
				if (pos < HashTable.this.table.length)
					return HashTable.this.table[pos].next;

				return null;
			}

			@Override
			public boolean hasNext() {
				return nextBucket != null;
			}

			@SuppressWarnings("unchecked")
			@Override
			public K next() {
				K result = (K) nextBucket.key;

				nextBucket = nextBucket.next != null ? nextBucket.next : advanceToNextBucket();

				return result;
			}
		};
	}
	
	// Used for validation, no need to protect for concurrent access
	// counts the total number of elements in the table
	public int getSize(){
		int count = 0;
		Iterator<V> values = this.values();
		while(values.hasNext()){
			values.next();
			count++;
		}
		return count;
	}

	/**
	 * Write locks the bucket of the corresponding key or keys.
	 * 
	 * @param key
	 *            : key whose bucket should be locked
	 */
	public void writeLock(K key) {
		int bucketPos = calcTablePos(key);
		this.table[bucketPos].writeLock().lock();
	}

	/**
	 * Write unlocks the bucket of the corresponding key or keys.
	 * 
	 * @param key
	 *            : key whose bucket should be unlocked
	 */
	public void writeUnlock(K key) {
		int bucketPos = calcTablePos(key);
		this.table[bucketPos].writeLock().unlock();
	}
	
	/**
	 * Read locks the bucket of the corresponding key or keys.
	 * 
	 * @param key
	 *            : key whose bucket should be locked
	 */
	public void readLock(K key) {
		int bucketPos = calcTablePos(key);
		this.table[bucketPos].readLock().lock();
	}
	
	/**
	 * Read locks the bucket of the corresponding key or keys.
	 * 
	 * @param key
	 *            : key whose bucket should be unlocked
	 */
	public void readUnlock(K key) {
		int bucketPos = calcTablePos(key);
		this.table[bucketPos].readLock().unlock();
	}
	

}
