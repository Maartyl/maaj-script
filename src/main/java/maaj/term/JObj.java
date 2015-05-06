/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package maaj.term;

/**
 * Used to represent wrappers of native JVM objects.
 * Used in: Str, JWrap
 * <p>
 * @author maartyl
 */
public interface JObj extends Term {

  @Override
  Object getContent();

  @Override
  default void serialize(java.io.Writer w) {
    throw new UnsupportedOperationException("JObj: cannot serialize wrapped Java object");
  }

}
