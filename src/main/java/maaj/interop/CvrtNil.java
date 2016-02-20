/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package maaj.interop;

import maaj.term.Nil;

/**
 * For passing null to non-primitive slots.
 * Should be last in test chain.
 * <p>
 * @author maartyl
 */
public final class CvrtNil implements Conversion, Converter {

  private CvrtNil() {
  }

  @Override
  public Conversion lookup(Class<?> source, Class<?> target) {
    return (source == Nil.class && !target.isPrimitive()) ? this : null;
  }

  @Override
  public int cost() {
    return 0; //null is any ref type ...
  }

  @Override
  public Object convert(Object obj) {
    if (obj != null)
      throw new IllegalArgumentException("Expected null but got: " + obj + " :: " + obj.getClass());
    return obj;
  }

  private static final CvrtNil SINGLETON = new CvrtNil();

  public static CvrtNil singleton() {
    return SINGLETON;
  }

}
