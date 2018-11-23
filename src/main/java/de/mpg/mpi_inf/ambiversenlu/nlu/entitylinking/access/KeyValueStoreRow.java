package de.mpg.mpi_inf.ambiversenlu.nlu.entitylinking.access;

import java.util.Arrays;

public class KeyValueStoreRow {

  public Object[] values; //for serialization purposes more efficient if public

  public KeyValueStoreRow() {
    // Bean.
  }

  ;

  public KeyValueStoreRow(Object[] values) {
    this.values = values;
  }

  public String getString(int i) {
    return String.class.cast(values[i]);
  }

  public int getInt(int i) {
    return Integer.class.cast(values[i]);
  }

  public double getDouble(int i) {
    return Double.class.cast(values[i]);
  }

  public Object getObject(int i) {
    return values[i];
  }

  public int size() {
    return values.length;
  }

  @Override public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    KeyValueStoreRow that = (KeyValueStoreRow) o;

    // Probably incorrect - comparing Object[] arrays with Arrays.equals
    return Arrays.equals(values, that.values);

  }

  @Override public int hashCode() {
    return values != null ? Arrays.hashCode(values) : 0;
  }

}
