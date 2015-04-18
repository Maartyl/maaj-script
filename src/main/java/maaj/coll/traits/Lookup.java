/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package maaj.coll.traits;

import maaj.term.Term;

/**
 *
 * @author maartyl
 */
public interface Lookup {
  Term valAt(Term key);

  Term valAt(Term key, Term dflt);
}
