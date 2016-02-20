/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package maaj.util;

import maaj.interop.Converter;
import maaj.interop.ConverterCombiner;

/**
 *
 * @author maartyl
 */
public class CvrtH {

  private CvrtH() {
  }

  public static Converter combine(Converter c) {
    return c;
  }

  public static Converter combine(Converter c1, Converter c2) {
    return new ConverterCombiner(c1, c2);
  }

  public static Converter combine(Converter c1, Converter c2, Converter c3) {
    return combine(c1, combine(c2, c3));
  }

  public static Converter combine(Converter c1, Converter c2, Converter c3, Converter c4) {
    return combine(combine(c1, c2), combine(c3, c4));
  }

  public static Converter combine(Converter c1, Converter c2, Converter c3, Converter c4, Converter c5) {
    return combine(combine(c1, c2), combine(c3, c4, c5));
  }

}
