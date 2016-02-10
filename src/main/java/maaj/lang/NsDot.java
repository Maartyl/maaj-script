/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package maaj.lang;

import java.util.Collection;
import java.util.Collections;
import maaj.term.Symbol;
import maaj.term.Var;
import maaj.util.Sym;

/**
 * Namespace './' - used for shortcuts to interop (#jvm) functions.
 * Contains dynamically created macros.
 * <p>
 * @author maartyl
 */
public final class NsDot implements Namespace.ReadOnly {

  @Override
  public Collection<Var> getAllOwn() {
    return Collections.emptyList();
  }

  @Override
  public Symbol getName() {
    return Sym.dotSym;
  }

  @Override
  public Var getOwn(Symbol name) {
    throw new UnsupportedOperationException("Not supported yet."); //TODO: implement
  }

}
