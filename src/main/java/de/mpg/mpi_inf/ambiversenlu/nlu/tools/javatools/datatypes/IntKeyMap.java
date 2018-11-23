package de.mpg.mpi_inf.ambiversenlu.nlu.tools.javatools.datatypes;

import de.mpg.mpi_inf.ambiversenlu.nlu.tools.javatools.administrative.D;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 *
 * This class is part of the Java Tools (see
 * http://mpii.de/yago-naga/javatools). It is licensed under the Creative
 * Commons Attribution License (see http://creativecommons.org/licenses/by/3.0)
 * by the YAGO-NAGA team (see http://mpii.de/yago-naga).
 *
 * This class implements a HashMap with integer keys.
 *
 * @author Fabian M. Suchanek
 *
 * @param <K>
 */
public class IntKeyMap<K> {

  /** Holds the keys */
  protected int[] keys;

  /** Holds the values */
  protected K[] values;

  /** Holds size */
  protected int size;

  /** Constructor */
  public IntKeyMap() {
    clear();
  }

  /** Creates an intHashMap from a list that contains keys and values in alternation*/
  public IntKeyMap(Object... keyValuePairs) {
    this(Arrays.asList(keyValuePairs));
  }

  /** Creates an intHashMap from a list that contains keys and values in alternation*/
  @SuppressWarnings("unchecked") public IntKeyMap(List<Object> keyValuePairs) {
    this();
    for (int i = 0; i < keyValuePairs.size(); i += 2) {
      Object key = keyValuePairs.get(i);
      if (key instanceof Integer) put((Integer) key, (K) keyValuePairs.get(i + 1));
      else if (key instanceof Character) put((int) ((Character) key).charValue(), (K) keyValuePairs.get(i + 1));
      else if (key instanceof Byte) put(((Byte) key).intValue(), (K) keyValuePairs.get(i + 1));
      else if (key instanceof Short) put(((Short) key).intValue(), (K) keyValuePairs.get(i + 1));
      else throw new RuntimeException("Keys have to be integers");
    }
  }

  /** Returns an index where to store the object */
  protected int index(int key, int len) {
    return (Math.abs(key) % len);
  }

  /** Returns an index where to store the object */
  protected int index(int key) {
    return (index(key, keys.length));
  }

  /** Retrieves a value */
  public K get(int key) {
    return (get(key, null));
  }

  /** Finds a key, keys[find] will be Integer.MAX_VALUE if non-existent */
  protected int find(int key) {
    int i = index(key);
    while (true) {
      if (keys[i] == Integer.MAX_VALUE) return (i);
      if (keys[i] == key) return (i);
      i++;
      if (i == keys.length) i = 0;
    }
  }

  /** Retrieves a value */
  public K get(int key, K defaultValue) {
    int pos = find(key);
    if (keys[pos] == Integer.MAX_VALUE) return (defaultValue);
    else return ((K) values[pos]);
  }

  /** True if value is there */
  public boolean containsKey(int key) {
    return (keys[find(key)] != Integer.MAX_VALUE);
  }

  /** Returns keys. Can be used only once. */
  public PeekIterator<Integer> keys() {
    final int[] e = keys;
    return (new PeekIterator<Integer>() {

      int pos = -1;

      @Override protected Integer internalNext() throws Exception {
        pos++;
        for (; pos < keys.length; pos++) {
          if (e[pos] != Integer.MAX_VALUE) {
            return (e[pos]);
          }
        }
        return (null);
      }

    });
  }

  /**
   * Adds a key, true for 'added the key as new', false for 'overwrote
   * existing value'
   */
  public boolean put(int key, K value) {
    if (key == Integer.MAX_VALUE) throw new RuntimeException("Integer.MAX_VALUE cannot be stored as key. Sorry...");
    if (put(keys, values, key, value)) {
      size++;
      if (size > keys.length * 3 / 4) rehash();
      return (true);
    }
    return (false);
  }

  /**
   * Adds a key, true for 'added the key as new', false for 'overwrote
   * existing value'
   */
  protected boolean put(int[] keys, K[] values, int key, K value) {
    int i = index(key, keys.length);
    while (true) {
      if (keys[i] == Integer.MAX_VALUE) {
        keys[i] = key;
        values[i] = value;
        return (true);
      }
      if (keys[i] == key) {
        values[i] = value;
        return (false);
      }
      i++;
      if (i == keys.length) i = 0;
    }
  }

  /** Rehashes */
  protected void rehash() {
    int[] newKeys = new int[keys.length * 2];
    Arrays.fill(newKeys, Integer.MAX_VALUE);
    @SuppressWarnings("unchecked") K[] newValues = (K[]) new Object[keys.length * 2];
    for (int i = 0; i < keys.length; i++) {
      if (keys[i] != Integer.MAX_VALUE) put(newKeys, newValues, keys[i], values[i]);
    }
    keys = newKeys;
    values = newValues;
  }

  public Iterator<Integer> iterator() {
    return keys().iterator();
  }

  public int size() {
    return size;
  }

  @SuppressWarnings("unchecked") public void clear() {
    size = 0;
    keys = new int[10];
    Arrays.fill(keys, Integer.MAX_VALUE);
    values = (K[]) new Object[10];
  }

  public boolean contains(int o) {
    return containsKey(o);
  }

  @Override public String toString() {
    if (isEmpty()) return ("{}");
    StringBuilder b = new StringBuilder("{");
    int counter = 30;
    for (int key : keys()) {
      if (counter-- == 0) {
        b.append("..., ");
        break;
      }
      b.append(key).append('=').append(get(key)).append(", ");
    }
    b.setLength(b.length() - 2);
    return (b.append("}").toString());
  }

  /** TRUE if there is no mapping*/
  public boolean isEmpty() {
    return size == 0;
  }

  @Override public boolean equals(Object o) {
    if (!(o instanceof IntKeyMap<?>)) return (false);
    IntKeyMap<?> other = (IntKeyMap<?>) o;
    if (other.size() != this.size()) return (false);
    for (int i = 0; i < keys.length; i++) {
      if (keys[i] == Integer.MAX_VALUE && values[i] != other.get(keys[i])) return (false);
    }
    return (true);
  }

  @Override public int hashCode() {
    return Arrays.hashCode(values);
  }

  /** Test */
  public static void main(String[] args) throws Exception {
    IntKeyMap<String> m = new IntKeyMap<String>();
    for (int i = 1; i < 3000; i *= 2) {
      m.put(i, "#" + i);
      D.p("Added", i, m.size());
    }
    D.p(m.keys);
    m.put(8, "#0");
    for (int key : m.keys())
      D.p(key, m.get(key));
  }
}
