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
    add(Long.class, Integer.class, x -> ((Long) x).intValue());
  }

  @Override
  public Conversion lookup(Class<?> source, Class<?> target) {
    return impls.get(new Key(source, target));
  }

  private void add(Class<?> source, Class<?> target, CvrtSimple cvrt) {
    impls.put(new Key(source, target), cvrt);
  }

  private static final ImplicitConversions SINGLETON = new ImplicitConversions();

  public static ImplicitConversions singleton() {
    return SINGLETON;
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
