/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package maaj.term;

import maaj.lang.Context;

/**
 * A Fn that doesn't evaluate it's args and macro expands it's result.
 * <p>
 * @author maartyl
 */
public class Macro extends Fn {

  @Override
  public Term apply(Context cxt, Seq args) {
    throw new UnsupportedOperationException("Not supported yet."); //TODO: implement
  }


}
