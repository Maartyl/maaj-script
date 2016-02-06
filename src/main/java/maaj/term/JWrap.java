/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package maaj.term;

import java.io.IOException;
import java.io.Writer;
import maaj.lang.Context;

/**
 *
 * @author maartyl
 */
public class JWrap implements JObj, Ground {

  Object obj;

  protected JWrap(Object obj) {
    this.obj = obj;
  }

  @Override
  public Object getContent() {
    return obj;
  }

  @Override
  public Term apply(Context cxt, Seq args) {
    throw new UnsupportedOperationException("Not supported yet."); // wrong?
  }

  public static JWrap of(Object obj) {
    if (obj == null)
      throw new IllegalArgumentException("wrapped object cannot be null");
    return new JWrap(obj);
  }

}
