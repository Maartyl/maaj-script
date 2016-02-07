/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package maaj.interop;


public final class ConverterCombiner implements Converter {

  Converter c1;
  Converter c2;

  /**
   * @param c1 tried first, if not found, tries second
   * @param c2
   */
  public ConverterCombiner(Converter c1, Converter c2) {
    this.c1 = c1;
    this.c2 = c2;
  }

  @Override
  public Conversion lookup(Class<?> source, Class<?> target) {
    Conversion l1 = c1.lookup(source, target);
    return l1 != null ? l1 : c2.lookup(source, target);
  }

}
