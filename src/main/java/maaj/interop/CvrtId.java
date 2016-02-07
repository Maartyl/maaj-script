/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package maaj.interop;

/**
 * For types that don't need converting.
 * <p>
 * @author maartyl
 */
public final class CvrtId implements Conversion, Converter {

  private CvrtId() {
  }

  @Override
  public Conversion lookup(Class<?> source, Class<?> target) {
    return (target.isAssignableFrom(source)) ? this : null;
  }

  @Override
  public int cost() {
    return 0;
  }


  @Override
  public Object convert(Object obj) {
    return obj;
  }


  private static final CvrtId SINGLETON = new CvrtId();

  public static CvrtId singleton() {
    return SINGLETON;
  }

}
