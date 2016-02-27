/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package maaj.interop;


/**
 * provides access to conversions
 * also tests if types are compatible at all (assignable types are convertible too, by id)
 * <p>
 * @author maartyl
 */
@FunctionalInterface
public interface Converter {

  /**
   *
   * @param source type of object to be converted
   * @param target needed type
   * @return NULL if no conversion possible
   */
  //@Nullable
  Conversion lookup(Class<?> source, Class<?> target);
}
