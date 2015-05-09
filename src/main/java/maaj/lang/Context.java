/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package maaj.lang;

import maaj.coll.traits.Lookup;
import maaj.term.Map;
import maaj.term.Term;
import maaj.util.MapH;

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
   -- EDIT: nope: vars are saved inside fns / stuff and only content changes

   local scope variables map :: Map // my or IPersistentMap<Symbol, Term>
   - using MyMap makes no sense: I wil never need it and it would only be slower

   bindings: in future: only layer before accessing global... ?
   - how different from local vars? - visible inside Vars ... !!!
   -- must be INSIDE VARS not here - ok

   and some reference to truly global scope
   - way to access other objects in JVM

   EDIT:
   - I will need namespaces:
   current namespace + name
   lang namespace: #
   special namespaces that are referenced through # starting names
   - these are lang features / ...
   required namespaces
   - namespace should be more then just a map
   -- in case I need to access ... complicated stuff

   */
  private final Glob glob;

  private Namespace curNs;

  private Map scope;

  public Context(Glob glob, Namespace curNs, Map scope) {
    this.glob = glob;
    this.curNs = curNs;
    this.scope = scope;
  }

  public Context(Context c, Map scope) {
    this(c.glob, c.curNs, scope);
  }

  public Context(Glob glob, Namespace curNs) {
    this(glob, curNs, MapH.emptyPersistent());
  }

  public Context(Context c, Namespace curNs) {
    this(c.glob, curNs, c.scope);
  }

  @Override
  public Term valAt(Term key) {
    throw new UnsupportedOperationException("Not supported yet."); //TODO: implement
  }

  @Override
  public Term valAt(Term key, Term dflt) {
    throw new UnsupportedOperationException("Not supported yet."); //TODO: implement
  }

  public String getCurrentNamespaceName() {
    throw new UnsupportedOperationException("Not supported yet."); //TODO: implement
  }

  public Context withNamespace(Namespace ns) {
    return new Context(this, ns);
  }
}
