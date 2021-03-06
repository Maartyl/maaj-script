/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package maaj.term;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.logging.Level;
import java.util.logging.Logger;
import maaj.lang.Context;
import maaj.term.visitor.Visitor;
import maaj.util.H;
import maaj.util.MapH;
import maaj.util.SeqH;

/**
 * Base term: This interface is required for participating in interpretation of Maaj program.
 * Java objects need to be wrapped.
 * <p>
 * It is never assumed that any term is null. For null values there is Nil implementing Term.
 * All nulls from elsewhere are wrapped in Nil before they get into interpret.
 * <p>
 * @author maartyl
 */
public interface Term extends Quotable {

  default boolean isNil() {
    return false;
  }

  Term eval(Context c);

  Term evalMacros(Context c);

  default Map getMeta() {
    return MapH.emptyPersistent();
  }

  default Term getMeta(Term key) {
    return getMeta().valAt(key);
  }

  default Term addMeta(Map meta) {
    if (meta.isEmpty()) return this;
    return Meta.of(this, meta);
  }

  default Term addMeta(Term key, Term val) {
    return addMeta(H.map(key, val));
  }

  <TR, TA> TR visit(Visitor<TR, TA> v, TA arg);



  /**
   * application of function on arguments:
   * (fn a b c) -> fn.apply(a,b,c)
   * //actually, more like: fn.eval().apply(map(::eval,list(a,b,c)));
   * //fn.eval() is needed, for otherwise it would be called just on a symbol
   * //where evaluation of symbol get the associated function
   * @param cxt  context of computation (to access global and scope vars)
   * @param args
   * @return result of application (return value)
   */
  Term apply(Context cxt, Seq args);

  default Term applyMacro(Context cxt, Seq args) {
    //when applying to symbol: must ask if represents macro or something... - addressed in Var.applyMacro
    //macros will reimplement
    //it's ok to call on things that cannot be applied ... probably makes little sense, but won't break anything
    return SeqH.cons(this, args.fmap((Invocable1) x -> x.evalMacros(cxt)));
  }

  /**
   * Must match type of Content
   * //getContent().getClass()
   * <p>
   * @return type of what returns getContent
   */
  default Class getType() {
    return getContent().getClass(); //TODO: check it's not the interface only
  }

 
  /**
   * Must be of type returned by getType
   * @return underlying object (most often this)
   */
  default Object getContent() {
    return this;
  }

  /**
   * Recur is a special construct (Term) that allows tail call optimization.
   * It costs creating an object for each iteration but prevents StackOverflow
   * - If I were to compile or properly analyze and rebuild my AST that could be ommited.
   * - I'm trying to not overcomplicate the evaluation model. This implementation is slow anyway.
   * <p>
   * @return
   */
  default boolean isRecur() {
    return false;
  }

  /**
   * Method for updating Terms
   * instead of: f(t) call t.transform(f)
   * For most Terms works the same but wrappers only apply this on contents and then rewrap the term
   * (- current wrappers : Meta)
   * Does not mutate current Term.
   * <p>
   * Also solves problem of checking type of wrapped Terms.
   * <p>
   * @param transformer (ThisTerm -> NewTerm)
   * @return NewTerm
   */
  default Term transform(Invocable transformer) {
    return transformer.invoke(this);
  }

  /**
   * Returns this, unless this term is only a wrapper.
   * <p>
   * @return Actually represented term by this term.
   */
  default Term unwrap() {
    return this;
  }

  /**
   * Creates textual representation of term, that is human readable.
   * This string is not necessarily readable by computer.
   * Uses print() in default
   * <p>
   * @return
   */
  default Str show() {
    return Str.of(print());
  }

  /**
   * Writes textual representation of term, that is human readable.
   * This string is not necessarily readable by computer.
   * Equivalent of: w.Write(term.show().asString());
   * Uses .toString() in default
   * <p>
   * @param w write into
   * @throws java.io.IOException
   */
  /*default*/ void show(Writer w) throws IOException;/*{ w.append(toString()); }*/


  /**
   * The same as show() but returns normal String.
   * Equivalent of show().asString();
   * Uses show(StringWriter) in default.
   * <p>
   * @return
   */
  default String print() {
    try {
      StringWriter sw = new StringWriter();
      show(sw);
      return sw.toString();
    } catch (IOException ex) {
      Logger.getLogger(Term.class.getName()).log(Level.SEVERE, null, ex);
      throw H.sneakyThrow(ex); //I don't expect this to ever happen
    }
  }

  /**
   * Creates textual representation of term, that can be read again later.
   * Generally is human readable, but it is not a requirement.
   * Uses show(Writer) in default
   * <p>
   * @param w the writer to serialize into
   * @throws java.io.IOException
   */
  default void serialize(java.io.Writer w) throws IOException {
    show(w);
  }

}
