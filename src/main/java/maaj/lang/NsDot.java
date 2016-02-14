/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package maaj.lang;

import java.util.Collection;
import java.util.Collections;
import maaj.term.Macro;
import maaj.term.Seq;
import maaj.term.Symbol;
import maaj.term.Term;
import maaj.term.Var;
import maaj.util.H;
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
    return Var.of(matchName(name.getNm()), Sym.macroMapTag);
  }

  //---
  private Macro matchName(String name) {
    switch (name) {
    case "": throw new UnsupportedOperationException("CANNOT HAPPEN: empty name");
    case "!!": throw new UnsupportedOperationException("Not supported yet: throw"); //TODO: implement
    //other special cases here
    default: return extractName(name);
    }
  }

  private Macro extractName(String name) {
    //name is nonempty
    switch (name.charAt(0)) { //on first char
    case ':': return ctor(name.substring(1));
    default: //continue
    }

    int lastIndex = name.length() - 1;

    if (lastIndex != 0) //name is longer than 1 char
      switch (name.charAt(lastIndex)) { //on last char
      case ':': return getter(name.substring(0, lastIndex));
      case '?': return getterBool(name.substring(0, lastIndex));
      case '!': return setter(name.substring(0, lastIndex));
      case '-': return field(name.substring(0, lastIndex));
      default: //continue
      }
    
    //ok, nothing special: just normal method invoke: replace all x-y with xY etc.
    return escapeAndInvoke(name);
  }

  private Macro getter(String name) {
    return tter("get", name);
  }

  private Macro getterBool(String name) {
    return tter("is", name);
  }

  private Macro setter(String name) {
    return tter("set", name);
  }

  private Macro tter(String prefix, String name) {
    return escapeAndInvoke(prefix + Character.toUpperCase(name.charAt(0)) + name.substring(1));
  }

  private Macro ctor(String substring) {
    throw new UnsupportedOperationException("Not supported yet."); //TODO: implement
  }

  private Macro field(String substring) {
    throw new UnsupportedOperationException("Not supported yet."); //TODO: implement
  }

  private Macro escapeAndInvoke(String name) {
    String nm = checkAndEscape(name);

    return args -> {
      if (args.isNil())
        throw (new IllegalArgumentException("Cannot invoke '" + nm + " without target argument."));

      Term target = args.first();
      return H.list(isQuotedSymbol(target) ? Sym.invokeStaticSymInterop : Sym.invokeVirtualSymInterop,
                    target,
                    H.list(Sym.quoteSymC, H.symbol(nm)),
                    H.cons(Sym.listSymCore, args.rest()));
    };
  }

  private static String checkAndEscape(String name) {
    int len = name.length();
    int last = len - 1;
    int pos;
    while ((pos = name.indexOf('-')) >= 0) { //TODO: make more efficient
      if (pos == last)
        throw new IllegalArgumentException("Illegal method name: \"" + name + "\" (ends with '-')");
      name = name.substring(0, pos) + Character.toUpperCase(name.charAt(pos + 1)) + name.substring(pos + 2);
    }
    return name; //TODO
  }

  private static boolean isQuotedSymbol(Term t) {
    // code ala haskell, to make this function clear
    // let isQuotedSymbol Seq (Sym.quoteSymC : (Symbol   _)   : []) = true
    //     isQuotedSymbol Seq (Sym.quoteSymC : (SymbolNs _ _) : []) = true
    //     isQuotedSymbol _ = false

    t = t.unwrap();
    if (!(t instanceof Seq)) return false;

    Seq s = (Seq) t;
    if (s.boundLength(2) != 2) return false;

    Term f1 = s.first().unwrap();
    if (!Sym.quoteSymC.equals(f1)) return false;

    Term f2 = s.rest().first().unwrap();
    return (f2 instanceof Symbol) && !((Symbol) f2).isKeyword();
  }

}
