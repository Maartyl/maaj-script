/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package maaj.term;

import java.io.IOException;
import java.io.Writer;

/**
 * Used to represent wrappers of native JVM objects.
 * <p>
 * @author maartyl
 */
public interface JObj extends Term {

  @Override
  Object getContent();

  @Override
  public default void show(Writer w) throws IOException {
    w.append("#!{"); //some way to show it's something 'weird'
    w.append(getContent().toString());
    w.append("}");
  }

  @Override
  default void serialize(java.io.Writer w) throws IOException {
    throw new UnsupportedOperationException("JObj: cannot serialize wrapped Java object");
  }

}
