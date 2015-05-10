/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package maaj.lang;

import maaj.term.Symbol;

/**
 *
 * @author maartyl
 */
public class EmptyLoader extends Namespace.Loader {

  @Override
  public Namespace loadNamespaceFor(Symbol nsName, Context cxt) {
    return createEmptyWithName(nsName);
  }

}
