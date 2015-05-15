/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package maaj.reader;

import java.io.Reader;
import maaj.exceptions.ReaderException;
import maaj.term.Char;
import maaj.term.Dbl;
import maaj.term.Int;
import maaj.term.Invocable0;
import maaj.term.Keyword;
import maaj.term.Map;
import maaj.term.MapT;
import maaj.term.Num;
import maaj.term.Seq;
import maaj.term.Str;
import maaj.term.Symbol;
import maaj.term.Term;
import maaj.term.Unquote;
import maaj.term.Vec;
import maaj.term.VecT;
import maaj.util.H;
import maaj.util.MapH;
import maaj.util.SeqH;
import maaj.util.Sym;
import maaj.util.VecH;

/**
 *
 * @author maartyl
 */
public class MaajReader {

  private final PosReader reader;
  private final ReaderContext context;

  private MaajReader(PosReader r, ReaderContext context) {
    this.reader = r;
    this.context = context;
  }

  public Seq readAll() {
    if (nextSkipWhitespace() < 0)
      return H.END;
    return H.lazy(read0Cur(), this::readAll);
  }

  private int next() {
    return reader.readNextChar();
  }

  private int cur() {
    return reader.getCurrentChar();
  }

  private void unread() {
    reader.unread();
  }

  private int peek() {
    return reader.peek();
  }

  private int nextSkipWhitespace() {
    while (isWhitespace(next())) {
    }
    return cur();
  }

  private <T> T fail(String message) {
    throw new ReaderException(context, reader.getRow(), reader.getColumn(), message);
  }

  private Term read0SkipWhitespace() {
    return read0(nextSkipWhitespace());
  }

  private Term read0Cur() {
    return read0(cur());
  }

  private Term read0Inner(int c) {
    switch (c) {
    case '(': return readList();
    case '[': return readVec();
    case '{': return readMap();
    case '"': return readStr();
    case '#': return readHash();
    case '-': return readMinus();
    case ';': return readComment(this::read0SkipWhitespace);
    case '/': return readSlash();
    case '^': return readMeta();
    case '~': return readUnquote();
    case '\\': return readEscape();
    case '`': return H.list(Sym.quoteQualifiedSymC, read0SkipWhitespace());
    case '\'': return H.list(Sym.quoteSymC, read0SkipWhitespace());
    case '@': return H.list(Sym.derefSymCore, read0SkipWhitespace());
    case ')': return fail("unmatched: )");
    case ']': return fail("unmatched: ]");
    case '}': return fail("unmatched: }");
    }
    if (c < 0) return fail("unexpected EOF");
    if (isNumericStart(c))
      return readNum();
    if (isSymbolicStart(c))
      return readSymbol();

    return fail("read0: " + (char) c + " /:" + c);
  }
  /**
   * read top level term (can be neseted inside others, but also can exist by itself)
   * starts at @c@ position : can be gotten to differently based on previous content (i.e. : a(5 2) vs. (1 2) (5 2))
   */
  private Term read0(int c) {
    Map m = H.map(Sym.fileRowSymK, H.wrap(reader.getRow()), Sym.fileColSymK, H.wrap(reader.getColumn()));
    return read0Inner(c).addMeta(m);
  }
  /**
   * called after '#' symbol : extends dispatch table
   */
  private Term readHash() {
    int c = nextSkipWhitespace();
    switch (c) {
    case '(': return fail("not implemented yet: fn syntax");
    case '[': return fail("not implemented yet: array? something...");
    case '{': return fail("not implemented yet: set");
    case '"': return fail("not implemented yet: regexp");
    case '_': return readIgnoreOne();
    case '#': return fail("reserved: hash inside hash");
    //case '-': return readMinus(); - always interpret as symbol
    case ';': return readComment(this::readHash);
    case '/': return readSymbol().withNamespace("#");
    case '^': return fail("not implemented yet: I can pick anything...");
    case '~': return fail("not implemented yet: I can pick anything...");
    case '\\': return fail("not implemented yet: I can pick anything... (var)");
    case '`': return fail("not implemented yet: I can pick anything...");
    case '\'': return fail("not implemented yet: I can pick anything...");
    case '@': return fail("not implemented yet: I can pick anything...");
    case ')': return fail("unmatched: )");
    case ']': return fail("unmatched: ]");
    case '}': return fail("unmatched: }");
    }
    if (c < 0) return fail("unexpected EOF");
    if (isNumericStart(c))
      return fail("not implemented yet: hash number ... ?");
    if (isSymbolicStart(c))
      return readHashSymbol(readSymbol());

    return fail("readHash: " + (char) cur() + " /:" + cur());
  }
  /**
   * used if readHash reads symbol, i.e.:
   * #test ~> read0 # -> readHash test -> readHashSymbol(test)
   * most of these variants are not decided on meaning yet
   * if symbol is qualified, prepends '#' to namespace of it and returns
   */
  private Term readHashSymbol(Symbol s) {
    if (s.isQualified()) {
      //core functions with namespaces
      return H.symbol('#' + s.getNs(), s.getNm());
    }
    int c = nextSkipWhitespace();
    switch (c) {
    case '(': return fail("not implemented yet: ???");
    case '[': return fail("not implemented yet: ???");
    case '{': return fail("not implemented yet: ???");
    case '"': return fail("not implemented yet: ???");
    case '_': return fail("not implemented yet: ???");
    case '#': return fail("maybe, this should work normally?");
    // case '-': return readMinus(); // for now, interpret as symbol...
    case ';': return readComment(() -> readHashSymbol(s));
    case '/': return fail("not implemented yet: core fns with ns"); //not really, there must have had been space or something...
    case '^': return fail("not implemented yet: work normally? ... applying to rest: not syntax based...");
    case '~': return fail("not implemented yet: ???");
    case '\\': return fail("not implemented yet: ???");
    case '`': return fail("not implemented yet: ???");
    case '\'': return fail("not implemented yet: ???");
    case '@': return fail("not implemented yet: ???");
    case ')': return fail("unmatched: )");
    case ']': return fail("unmatched: ]");
    case '}': return fail("unmatched: }");
    }
    if (c < 0) return fail("unexpected EOF");
    if (isNumericStart(c))
      return fail("not implemented yet: ???");
    if (isSymbolicStart(c))
      return fail("not implemented yet: ???");

    return fail("readHashSymbol: " + (char) cur() + " /:" + cur());
  }
  /**
   * used for commenting out terms: (func asd #_ not-good-but-keep good-instead ...)
   */
  private Term readIgnoreOne() {
    read0SkipWhitespace();
    return read0SkipWhitespace();
  }
  /**
   * / is a valid symbol name: for division
   * also used for namespace separation, so (for now) no other symbol starting with / is allowed
   */
  private Term readSlash() {
    if (!isSymbolic(peek()))
      return H.symbol("/");
    //symbols cannot start with /, unless / - DECIDE what to do with this
    //clojure: just returns, never extending... unless 3... ?
    return fail("symbol cannot start with '/'");
  }
  /**
   * ^ :hello term
   * ^ {:meta-key meta-value} term
   */
  private Term readMeta() {
    Map meta = normalizeMeta(read0SkipWhitespace());
    return read0SkipWhitespace().addMeta(meta);
  }
  /**
   * variants of meta, that can be read; meta syntax is always a term itself
   * if there is meta on meta, and is not map; keeps it + adds it to the entire meta map
   */
  private Map normalizeMeta(Term m) {
    Map metaOnMeta = m.getMeta();
    Term mu = m.unwrap();
    if (mu instanceof Map)
      return MapH.update(metaOnMeta, (Map) mu);
    if (mu instanceof Keyword)
      return MapH.update(metaOnMeta, H.map(m, m));
    if (mu instanceof Symbol)
      return MapH.update(metaOnMeta, H.map(Sym.tagSymK, m));
    if (mu instanceof Str)
      return MapH.update(metaOnMeta, H.map(Sym.infoSymK, m));
    if (mu instanceof Num)
      return MapH.update(metaOnMeta, H.map(Sym.numSymK, m));

    return fail("unexpected meta term type: " + mu.getType().getName());
  }
  /**
   * either returns next char from reader only wrapped or attempts to read complex characters:
   * like: \\_newline, \\u74A0 ...
   * //can't do these yet... (not that important : I might do it in future)
   */
  private Char readEscape() {
    if (isMulticharEscape(next())) 
      return readMulticharEscape();
    else
      return Char.of((char) cur());
  }

  private boolean isMulticharEscape(int cur) {
    if (cur == 'u' && isNumeric16(peek()))
      return true;
    return cur == '_';
  }

  private Char readMulticharEscape() {
    switch (cur()) {
    case 'u': return Char.of((char) read4Num16Int());
    default:
      return fail("not implemented: textual char literals");
    }
  }

  private Seq readList() {
    switch (nextSkipWhitespace()) {
    case ')': return H.END;
    default:
      return SeqH.sexp(read0Cur(), readList());
    }
  }

  private Vec readVec() {
    VecT v = VecH.emptyTransient();
    while (nextSkipWhitespace() != ']')
      v.doConj(read0Cur());
    return VecH.preferTuple(v.asPersistent());
  }

  private Map readMap() {
    MapT m = MapH.emptyTransient();
    while (nextSkipWhitespace() != '}') {
      Term key = read0Cur();
      if (nextSkipWhitespace() == '}')
        return fail("map: Requires even number of terms.");
      Term val = read0Cur();
      m.doAssoc(key, val);
    }
    return m.asPersistent();
  }

  private Str readStr() {
    StringBuilder sb = new StringBuilder();
    while (next() != '"') {
      if (cur() < 0)
        return fail("EOF while reading string");
      if (cur() == '\\')
        sb.append(readStrEscape());
      else
        sb.append((char) cur());
    }
    return Str.of(sb.toString());
  }

  private char readStrEscape() {
    switch (next()) {
    case 'n': return '\n';
    case 't': return '\t';
    case '\\': return '\\';
    case '"': return '"';
    case 'r': return '\r';
    case 'b': return '\b';
    case 'f': return '\f';
    case '\'': return '\'';
    case 'u': return (char) read4Num16Int();
    }
    return fail("unrecognized escape sequence: \\" + (char) cur());
  }
  /**
   * read 4 characters (0-f) and return them read into integer using radix 16
   */
  private int read4Num16Int() {
    StringBuilder sb = new StringBuilder(4);
    for (int i = 0; i < 4; ++i)
      if (isNumeric16(next()))
        sb.append((char) cur());
      else fail("Invalid hexadecimal char: " + cur());
    return Integer.parseInt(sb.toString(), 16);
  }

  private Num readNum() {
    //precodition: isNumericStart(cur())
    boolean metDot = false;
    StringBuilder sb = new StringBuilder();
    do {
      if (cur() == '.') //readNum is never called when cur() is '.'
        metDot = true;
      sb.append((char) cur());
    } while (isNumeric(next()));
    unread(); //last read char is not part of number
    if (metDot) 
      return Dbl.of(Double.parseDouble(sb.toString()));
     else 
      return Int.of(Long.parseLong(sb.toString()));
  }

  private Symbol readSymbol() {
    //precodition: isSymbolicStart(cur())
    StringBuilder sb = new StringBuilder();
    do sb.append((char) cur());
    while (isSymbolic(next()));
    unread(); //last read char is not part of symbol
    return qualifyKeywordIfShould(H.symbol(sb.toString()));
  }

  private Symbol qualifyKeywordIfShould(Symbol s) {
    //is not qualified and is keyword
    if (s.getType() == Keyword.class && s.getNm().startsWith(":"))
      return Keyword.qualified(context.getCurrentNamespaceName(), s.getNm().substring(1));
    return s;
  }

  private Term readUnquote() {
    if (peek() == '@') {
      next();
      return Unquote.createSplicing(read0SkipWhitespace());
    }
    return Unquote.createSimple(read0SkipWhitespace());
  }

  private Term readComment(Invocable0 continuation) {
    while (next() != '\n') {
    }
    return continuation.invoke();
  }

  private Term readMinus() {
    if (isNumericStart(peek())) {
      next();
      return readNum().neg();
    }
    return readSymbol();
  }

  private static boolean isWhitespace(int c) {
    if (c < 0) return false;
    return Character.isWhitespace(c) || c == ',' || c < 32 || Character.isIdentifierIgnorable(c);
  }

  private static boolean isNumericStart(int c) {
    //should't accept \-
    return c > 47 && c < 58;//Character.isDigit(c);
  }

  private static boolean isNumeric(int c) {
    return isNumericStart(c) || c == '.';
  }

  private static boolean isNumeric16(int c) {
    //0-9,a-f,A-F
    return (c > 47 && c < 58) || (c > 64 && c < 71) || (c > 96 && c < 103);
  }

  private static boolean isSymbolicStart(int c) {
    return Character.isAlphabetic(c) 
           || c == '!' || c == '$' || c == '%' || c == '&' || c == '*'
           || c == '*' || c == '-' || c == '+' || c == '?' || c == '.'
           || c == '<' || c == '>' || c == ':' || c == '_' || c == '='
           || c == '\\' || c == '|'
           || Character.isIdeographic(c);
  }

  private static boolean isSymbolic(int c) {
    return c == '/' || c == '#' || c == '\'' || isSymbolicStart(c) || Character.isDigit(c);
  }
  /**
   * main reader entry point
   * @param r   Reader to read terms from
   * @param cxt Contains what could be potentailly needed when reading : file name, namespace, ...
   * @return sequence of read terms - ends with reader
   * @throws ReaderException + any call to rest() throws too
   */
  public static Seq read(Reader r, ReaderContext cxt) {
    return H.lazy(() -> new MaajReader(new PosReader(r), cxt).readAll());
  }
}
