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

  /*
   name transformation:
   any ...x-y... gets translated to ...xY...

   (./xxx obj arg1 arg2 ...) -> method call with name xxx
   (./xxx 'Type arg1 arg2 ...) -> static method call

   (./xx: obj) -> (./getXx obj)
   (./xx? obj) -> (./isXx obj)
   (./xx! obj val) -> (./setXx obj val)

   (./xx- obj) -> field getter
   (./xx- obj val) -> field setter
   (./xx- 'Type) -> access static field //set if adds second arg


   (./!! ex) -> throw ex
   (./:Xxx arg1 arg2) -> .ctor of type Xxx  //??

   */

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
