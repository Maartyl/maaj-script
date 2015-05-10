/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package maaj.lang;

import maaj.exceptions.InvalidOperationException;
import maaj.term.Sf;
import maaj.term.Str;
import maaj.term.Symbol;
import maaj.util.H;

/**
 *
 * @author maartyl
 */
public class CoreLoader extends Namespace.Loader {

  @Override
  public Namespace loadNamespaceFor(Symbol nsName, Context cxt) {
    Namespace ns = createEmptyWithName(nsName);
    Context c = cxt.withNamespace(ns);
    switch (nsName.getNm()) {
    case "#": loadSf(c, ns);
      break;
    case "#core": loadCore(c, ns);
      break;
    case "#macro": loadMacro(c, ns);
      break;
    }
    return ns;
  }

  private void loadSf(Context cxt, Namespace core) {
    def(core, "if", "", (c, a) -> {
      int len = a.boundLength(3);
      if (len == 2)
        return a.first().isNil() ? H.NIL : a.rest().first().eval(c);
      if (len == 3)
        return !a.first().isNil() ? a.rest().first().eval(c) : a.rest().rest().first().eval(c);
      throw new InvalidOperationException("Wrong number of args passed to #/if: " + a.boundLength(30));
    });

  }

  private void loadCore(Context c, Namespace ns) {
    //throw new UnsupportedOperationException("Not supported yet."); //TODO: implement
  }

  private void loadMacro(Context c, Namespace ns) {
    //throw new UnsupportedOperationException("Not supported yet."); //TODO: implement
  }

  private static final Symbol docSym = H.symbol(":doc");

  private static void def(Namespace ns, String name, String doc, Sf sf) {
    ns.def(H.symbol(name), sf, H.map(docSym, Str.of(doc)));
  }
}
