/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package maaj.lang;

import maaj.coll.traits.Lookup;
import maaj.term.Term;

/**
 *
 * @author maartyl
 */
public class Context implements Lookup {

  /*
   what I contain:
   reference to 'global' scope :: Symbol/Term -> ? / Var? / Term
   - all other lisps have Vars - why? - to change stuff? can't I just change the map?
   -- so closures can remember mutable values? - I will look it up repeatedly, no? ... maybe not, then it could help...

   local scope variables map :: Map // my or IPersistentMap<Symbol, Term>
   - using MyMap makes no sense: I wil never need it and it would only be slower

   bindings: in future: only layer before accessing global... ?
   - how different from local vars? - visible inside Vars ... !!!
   -- must be INSIDE VARS not here - ok

   and some reference to truly global scope
   - way to access other objects in JVM

   */

  @Override
  public Term valAt(Term key) {
    throw new UnsupportedOperationException("Not supported yet."); //TODO: implement
  }

  @Override
  public Term valAt(Term key, Term dflt) {
    throw new UnsupportedOperationException("Not supported yet."); //TODO: implement
  }

}
