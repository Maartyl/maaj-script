/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package maaj.interop;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * static implicit conversions 
 * <p>
 * @author maartyl
 */
//(dynamic could be added as another, composable converter)
public final class ImplicitConversions implements Converter {

  private final Map<Key, Conversion> impls = new HashMap<>();

  private ImplicitConversions() {
    //all source types are necessarily boxed
    // targets are from method definitions: could be anything
    add2T(Long.class, Integer.class, Integer.TYPE, Long::intValue); //needed: all my Ints are Longs

    add2T(Byte.class, Short.class, Short.TYPE, Byte::shortValue);
    add2T(Byte.class, Integer.class, Integer.TYPE, Byte::intValue);
    add2T(Byte.class, Long.class, Long.TYPE, Byte::longValue);
    add2T(Byte.class, Float.class, Float.TYPE, Byte::floatValue);
    add2T(Byte.class, Double.class, Double.TYPE, Byte::doubleValue);

    add2T(Short.class, Integer.class, Integer.TYPE, Short::intValue);
    add2T(Short.class, Long.class, Long.TYPE, Short::longValue);
    add2T(Short.class, Float.class, Float.TYPE, Short::floatValue);
    add2T(Short.class, Double.class, Double.TYPE, Short::doubleValue);

    add2T(Integer.class, Long.class, Long.TYPE, Integer::longValue);
    add2T(Integer.class, Float.class, Float.TYPE, Integer::floatValue);
    add2T(Integer.class, Double.class, Double.TYPE, Integer::doubleValue);

    add2T(Long.class, Float.class, Float.TYPE, Long::floatValue);
    add2T(Long.class, Double.class, Double.TYPE, Long::doubleValue);

    add2T(Float.class, Double.class, Double.TYPE, Float::doubleValue);
  }

  @Override
  public Conversion lookup(Class<?> source, Class<?> target) {
    return impls.get(new Key(source, target));
  }

  private void add(Class<?> source, Class<?> target, CvrtSimple cvrt) {
    impls.put(new Key(source, target), cvrt);
  }

  @SuppressWarnings("unchecked")
  private <T> void addT(Class<T> source, Class<?> target, Cvrt<T> cvrt) {
    add(source, target, x -> cvrt.k((T) x));
  }

  private void add2(Class<?> source, Class<?> target1, Class<?> target2, CvrtSimple cvrt) {
    add(source, target1, cvrt);
    add(source, target2, cvrt);
  }

  private <T> void add2T(Class<T> source, Class<?> target1, Class<?> target2, Cvrt<T> cvrt) {
    addT(source, target1, cvrt);
    addT(source, target2, cvrt);
  }

  private static final ImplicitConversions SINGLETON = new ImplicitConversions();

  public static ImplicitConversions singleton() {
    return SINGLETON;
  }

  private static interface Cvrt<T> {

    public Object k(T kk);
  }

  private static final class Key {
    private final Class<?> source;
    private final Class<?> target;

    public Key(Class<?> source, Class<?> target) {
      this.source = source;
      this.target = target;
    }

    @Override
    public boolean equals(Object obj) {
      if (obj == null) return false;
      if (getClass() != obj.getClass()) return false;
      final Key other = (Key) obj;
      if (!Objects.equals(this.source, other.source)) return false;
      return Objects.equals(this.target, other.target);
    }

    @Override
    public int hashCode() {
      int hash = 5;
      hash = 23 * hash + Objects.hashCode(this.source);
      hash = 23 * hash + Objects.hashCode(this.target);
      return hash;
    }
  }

}
