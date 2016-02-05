package de.uniheidelberg.cl.reiter.util;



public class Pair<S extends Comparable<S>, T extends Comparable<T>> implements Comparable<Pair<S, T>> {
	S element1;
	T element2;
	
	public Pair(S e1, T e2) {
		this.element1 = e1;
		this.element2 = e2;
	}
	
	public int compareTo(Pair<S, T> other) {
		int c1 = this.getElement1().compareTo(other.getElement1());
		if (c1 != 0)
			return c1;
		return this.getElement2().compareTo(other.getElement2());
	}

	public boolean equals(Pair<S, T> other) {
		return this.compareTo(other) == 0;
	}
	
	public S getElement1() {
		return element1;
	}

	public T getElement2() {
		return element2;
	}
	
	public String toString() {
		return element1.toString() + "-" + element2.toString();
	}
}
