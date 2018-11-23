package de.mpg.mpi_inf.ambiversenlu.nlu.tools.javatools.datatypes;

import de.mpg.mpi_inf.ambiversenlu.nlu.tools.javatools.administrative.D;

import java.io.Closeable;
import java.util.*;

/**
 This class is part of the Java Tools (see http://mpii.de/yago-naga/javatools).
 It is licensed under the Creative Commons Attribution License
 (see http://creativecommons.org/licenses/by/3.0) by
 the YAGO-NAGA team (see http://mpii.de/yago-naga).





 This class provides an Iterator that can look ahead. With the method peek(), you
 can retrieve the next element without advancing the iterator.<BR>
 Example:
 <PRE>
 PeekIterator i=new SimplePeekIterator(1,2,3,4);
 i.peek();
 ---> 1
 i.peek();
 ---> 1
 i.next();
 ---> 1
 i.peek();
 ---> 2

 </PRE>
 The class is also suited to create an Interator by overriding. The only method that
 needs top be overwritten is "internalNext()".<BR>
 Example:
 <PRE>
 // An iterator over the numbers 0,1,2
 PeekIterator it=new PeekIterator() {
 int counter=0;
 // Returns null if there are no more elements
 protected Integer internalNext() throws Exception {
 if(counter==3) return(null);
 return(counter++);
 }
 };

 for(Integer i : it) D.p(i);

 --->
 0
 1
 2
 </PRE>
 */
public abstract class PeekIterator<T> implements Iterator<T>, Iterable<T>, Closeable {

  /** Holds the next element (to be peeked)*/
  public T next = null;

  /** TRUE if next has received its first value */
  public boolean initialized = false;

  /** TRUE if the iterator has been closed*/
  public boolean closed = false;

  /** TRUE if there are more elements to get with getNext */
  public final boolean hasNext() {
    if (!initialized) next = internalSilentNext();
    if (next == null && !closed) {
      close();
      closed = true;
    }
    return (next != null);
  }

  /** Wraps the Exceptions of internalNext into RuntimeExceptions */
  protected final T internalSilentNext() {
    try {
      T next = internalNext();
      initialized = true;
      return (next);
    } catch (Exception e) {
      if (e instanceof RuntimeException) throw (RuntimeException) e;
      else throw new RuntimeException(e);
    }
  }

  /** Returns the next or NULL if no next element is available*/
  protected abstract T internalNext() throws Exception;

  /** Returns the next element and advances. Overwrite internalNext instead! */
  public final T next() {
    if (hasNext()) {
      T returnMe = next;
      next = internalSilentNext();
      return (returnMe);
    }
    throw new NoSuchElementException();
  }

  /** Returns the next element and advances. Overwrite internalNext instead! */
  public final T nextOrNull() {
    if (hasNext()) return (next());
    return (null);
  }

  /** Removes the current element, if supported by the underlying iterator*/
  public void remove() {
    throw new UnsupportedOperationException();
  }

  /** returns the next element without advancing*/
  public final T peek() {
    if (hasNext()) return (next);
    throw new NoSuchElementException();
  }

  /** returns this*/
  public Iterator<T> iterator() {
    return this;
  }

  /** Closes the underlying resource */
  public void close() {
  }

  /** Returns an arraylist of an iterator (killing the iterator)*/
  public static <T> List<T> asList(Iterator<T> i) {
    ArrayList<T> l = new ArrayList<T>();
    while (i.hasNext()) l.add(i.next());
    return (l);
  }

  /** Returns an arraylist of this iterator (killing this iterator)*/
  public List<T> asList() {
    return (asList(this));
  }

  /** Fills the elements of an iterator into a given set (killing the iterator)*/
  public static <T> Set<T> asSet(Iterator<T> i, Set<T> set) {
    while (i.hasNext()) set.add(i.next());
    return (set);
  }

  /** Returns a hashset of an iterator (killing the iterator)*/
  public static <T> Set<T> asSet(Iterator<T> i) {
    return asSet(i, new HashSet<T>());
  }

  /** Returns a hashset of this iterator (killing this iterator)*/
  public Set<T> asSet() {
    return (asSet(this));
  }

  @Override public String toString() {
    return "PeekIterator[initialized=" + initialized + ", closed=" + closed + ", next=" + next + "]";
  }

  /** A PeekIterator that can iterate over another iterator or over a list of elements*/
  public static class SimplePeekIterator<T> extends PeekIterator<T> {

    /** Wrapped iterator */
    public Iterator<T> iterator;

    /** Returns the next or NULL if no next element is available. To be overwritten */
    protected T internalNext() throws Exception {
      if (!iterator.hasNext()) return (null);
      return (iterator.next());
    }

    /** Constructs a PeekIterator from another Iterator */
    public SimplePeekIterator(Iterator<T> i) {
      this.iterator = i;
    }

    /** Constructs a PeekIterator from an Iteratable (e.g. a list)*/
    public SimplePeekIterator(Iterable<T> i) {
      this(i.iterator());
    }

    /** Constructs a PeekIterator for a given list of elements */
    public SimplePeekIterator(T... elements) {
      this(Arrays.asList(elements));
    }

    /** Constructs a PeekIterator for a given list of elements */
    public SimplePeekIterator(T element) {
      this(Arrays.asList(element));
    }

    /** Removes the current element, if supported by the underlying iterator*/
    public void remove() {
      iterator.remove();
    }

    @Override public String toString() {
      return "Simple:" + super.toString();
    }
  }

  /** A Peek iterator with one single element */
  public static class ElementaryPeekIterator<T> extends PeekIterator<T> {

    /** The element to return or NULL */
    protected T element;

    public ElementaryPeekIterator(T element) {
      this.element = element;
    }

    @Override protected T internalNext() throws Exception {
      T e = element;
      element = null;
      return e;
    }

    public String toString() {
      return "ElementaryPeekIterator[next=" + element + "]";
    }

  }

  /** An empty PeekIterator*/
  protected static PeekIterator<Object> EMPTY = new PeekIterator<Object>() {

    {
      closed = true;
      initialized = true;
      next = null;
    }

    @Override protected Object internalNext() throws Exception {
      return null;
    }

    @Override public String toString() {
      return "EmptyPeekIterator";
    }
  };

  /** returns a constant empty iterator */
  @SuppressWarnings("unchecked") public static <K> PeekIterator<K> emptyIterator() {
    return ((PeekIterator<K>) EMPTY);
  }

  /** Counts the number of elements in an iterator (and destroys it)*/
  public static <S> int numElements(Iterator<S> it) {
    int num = 0;
    while (it.hasNext()) {
      it.next();
      num++;
    }
    return (num);
  }

  /** Counts the number of elements in an iterable*/
  public static <S> int numElements(Iterable<S> it) {
    return (numElements(it.iterator()));
  }

  /** Lists the elements in an iterable*/
  public static <S> StringBuilder toString(Iterable<S> it) {
    return (toString(it.iterator()));
  }

  /** Lists the elements in an iterator (and destroys it)*/
  public static <S> StringBuilder toString(Iterator<S> it) {
    StringBuilder res = new StringBuilder();
    while (it.hasNext()) {
      res.append(", ").append(it.next());
    }
    if (res.length() == 0) return (res.append("[]"));
    res.setCharAt(0, '[');
    return (res.append("]"));
  }

  /** Lists the elements in an iterable*/
  public static <S> List<S> list(Iterable<S> it) {
    return (list(it.iterator()));
  }

  /** Lists the elements in an iterator (and destroys it)*/
  public static <S> List<S> list(Iterator<S> it) {
    List<S> res = new ArrayList<S>();
    while (it.hasNext()) {
      res.add(it.next());
    }
    return (res);
  }

  /** test routine*/
  public static void main(String[] args) throws Exception {
    PeekIterator<Integer> it = new SimplePeekIterator<Integer>(1, 2, 3, 4);
    D.p(it.peek());
    D.p(it.peek());
    D.p(it.next());
    D.p(it.peek());
    D.p(it.peek());
  }

}
