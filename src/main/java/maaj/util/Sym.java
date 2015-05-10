/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package maaj.util;

import maaj.term.Symbol;

/**
 *
 * @author maartyl
 */
public class Sym {

  private Sym() {
  }

  public static final Symbol nameSym = H.symbol("name");
  public static final Symbol doSymC = H.symbol("#", "do");
  public static final Symbol docSymK = H.symbol(":doc");
  public static final Symbol ignoreSym = H.symbol("_");
  public static final Symbol firstSym = H.symbol("first");
  public static final Symbol restSym = H.symbol("rest");


}
