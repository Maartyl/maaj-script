/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package maaj.term;

import java.io.IOException;
import java.io.Writer;
import maaj.lang.Context;

/**
 * very simple lightweight box for arguments;
 * can be returned from a function and all fn results are tested on isRecur()
 * if so, the function is repeated with the new argument list
 * <p>
 * Maybe not the most efficient implementation, but allows loops with minimal mutation...
 * <p>
 * @author maartyl
 */
public class Recur implements Term {

  private final Seq args;

  private Recur(Seq args) {
    this.args = args;
  }

  @Override
  public boolean isRecur() {
    return true;
  }

  public Seq getArgs() {
    return args;
  }

  @Override
  public Term unwrap() {
    //this might get called, because it can get wrapped
    //this is (sadly) necessary, to wrok well with rest of the app
    return this;
    //throw new UnsupportedOperationException("Recur outside looping context");
  }

  @Override
  public Term addMeta(Map meta) {
    //some transformations require wrapping... (even of just fn results...)
    return this;
    //throw new UnsupportedOperationException("Recur outside looping context");
  }

  @Override
  public Term eval(Context c) {
    throw new UnsupportedOperationException("Recur outside looping context");
  }

  @Override
  public Term evalMacros(Context c) {
    throw new UnsupportedOperationException("Recur outside looping context");
  }

  @Override
  public Term apply(Context cxt, Seq args) {
    throw new UnsupportedOperationException("Recur outside looping context");
  }

  @Override
  public void show(Writer w) throws IOException {
    throw new UnsupportedOperationException("Recur outside looping context");
  }

  @Override
  public Monad unquoteTraverse(Context c) {
    throw new UnsupportedOperationException("Recur outside looping context");
  }

  @Override
  public int hashCode() {
    throw new UnsupportedOperationException("Recur outside looping context");
  }

  @Override
  public boolean equals(Object obj) {
    throw new UnsupportedOperationException("Recur outside looping context");
  }

  @Override
  public String toString() {
    throw new UnsupportedOperationException("Recur outside looping context");
  }

  @Override
  public void serialize(Writer w) throws IOException {
    throw new UnsupportedOperationException("Recur outside looping context");
  }

  @Override
  public String print() {
    throw new UnsupportedOperationException("Recur outside looping context");
  }

  @Override
  public Str show() {
    throw new UnsupportedOperationException("Recur outside looping context");
  }

  @Override
  public Term transform(Invocable transformer) {
    throw new UnsupportedOperationException("Recur outside looping context");
  }

  @Override
  public Object getContent() {
    throw new UnsupportedOperationException("Recur outside looping context");
  }

  @Override
  public Class getType() {
    throw new UnsupportedOperationException("Recur outside looping context");
  }

  @Override
  public Term invokeMethod(Str methodName, Seq args) {
    throw new UnsupportedOperationException("Recur outside looping context");
  }

  @Override
  public Term applyMacro(Context cxt, Seq args) {
    throw new UnsupportedOperationException("Recur outside looping context");
  }

  @Override
  public Term addMeta(Term key, Term val) {
    throw new UnsupportedOperationException("Recur outside looping context");
  }

  @Override
  public Term getMeta(Term key) {
    throw new UnsupportedOperationException("Recur outside looping context");
  }

  @Override
  public Map getMeta() {
    throw new UnsupportedOperationException("Recur outside looping context");
  }

  @Override
  public boolean isNil() {
    throw new UnsupportedOperationException("Recur outside looping context");
  }

  public static Recur ofArgs(Seq args) {
    return new Recur(args);
  }
}
