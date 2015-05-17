Maaj Script
===========

Maaj Script is a dynamic interpreted language based on Clojure running on JVM with interpret written in Java.

## Overview

Maaj Script is a functional, dynamic scripting language with Lisp syntax. Prefers immutability and, as is now, does not allow mutable local variables. It supports tail recursion, macro expansion, lazy lists, module importing... Is easy to use from Java. Jar with all dependencies (mainly Clojure data structures) is "only" ~400 KiB.

There are many constructs familiar to people who used lisp, especially Clojure, like: 

- `def`,`defn`,`defmacro`,`fn`
- `cons`, `first`,`rest`,`car`,`cdr`,`cadr` 
- `do`,`let`,`recur`,`eval`,`apply`
- ...

### Application

Some ideas are:

- Loading settings
    - This can be potentially unsafe.
- Dynamic testing of rest of the system.
- not yet:
    - Dnamic testing without requiring special namespace loader
    - Direct Java interop (boxed)
    - Reloading of scripts from files (for changing settings / ...)
        - Only reloading is missing, rest is ready.
-  General purpose scripting language.

 
### Name
 Coming up with good names is hard...

- maa :  from Maartyl
- j :   JVM, Java
- script :  ...

So essentially, a script by Maartyl for JVM / in Java...

### Why?
I am writing MaajScript as a school project for Java class. I chose an interpreter because I always wanted to write one and because I like Clojure, of which I wanted to learn more in the process. Not to mention, every programmer has written their own lisp... 

### Requirements / Dependencies

- Java 8
- https://github.com/krukow/clj-ds
    - This library is currently copied into project for the sake of development (fast goToSource etc.)
    - It will be made into a normal dependency at some later phase.

### Coming
- Java interop (through seamless reflection)
- Bean accessors
- Integration with Java Scripting API
- Possibly some synchronization primitives.

---

## Syntax

Maaj Script is a Lisp with extended syntax for vectors and maps. Very similar to Clojure, syntactically. 
There is a good chance that if a feature from Clojure is implemented, it has the same or similar syntax.

### Character Encoding

Maaj Script is fully Unicode compliant up to char 65535. (as far as reading characters goes)
Which means all common Unicode strings are valid strings in MaajScript, and if they meet requirements* for identifiers (no parenthesis, ...) they are valid identifiers.
Example of valid symbols: `čeřící`, `葛城`, `Ꙭൽↈⴱ`
For requirements see `maaj.reader.MaajReader#isSymbolic(int)`.
Default loader assumes files to be in UTF-8 format.


### Basic syntax examples


```Clojure
; this is a comment
,,,,,,,;commas are considered whitespace and ignored

; this invokes foo with arguments in variables* bar a baz
; (* all normal variables are immutable)
(foo bar baz) 

; lambda creation
(fn args-bind body-here)
(fn [] 5) ; takes no arguments and always returns 5

; this function returns a function that returns the argument passed to the outer function
(fn [x] (fn [] x)) 
(((fn [x] (fn [] x)) 5)) ;=> 5
```
See [let](#let) for details on argument binding.
Body can be multiple terms, evaluated in an implicit [do](#do) block.

```lisp
; named functions
(defn name-of-fn args-bind body-here)
(defn add [a b] (+ a b)) ; adds 2 numeric arguments

```
## Terms
Any expression in Maaj Script is a term. Some terms can nest other terms, creating expression trees.

### Symbol
<small>~Classical Lisp symbol.</small>
Used as identifier. 
Essentially any number of characters that don't start with number, colon or other special character (whitespace, parenthesis...)
Examples: `do` ,`count'`, `->`,`_auto_#45`, `.1`
Evaluated as value lookup.

Symbols and keywords can have a namespace: it is separated from name by a `/`, becoming:  `namespace/name` - Symbols cannot begin with `/`. (Special case is `/` only : it is a valid name)
Examples: `#core/reduce`, `some.namespace/foo`, `#core//`

### Keyword
Special variant of symbol that evaluates to itself. Starts with a colon.
Used for keys / tags...
Examples: `:col`, `:height`, `:*`

### Number
2 variants of numbers are supported: 
Evaluates to itself.

- int : wrapper over java.lang.Long
    - `5`, `-74567`
- double :
    - `0.5`, `32.`, `752.1563`

#### Char
Also a numeric term, but not a number. Can be used in numerical operations.
Is written as escape character. Some characters have* special literals like `\newline`.
Another way to specify character is using a unicode literal: `\u` and 4 hexadecimal digits.
Example: `\a`, `\n`, `\u771F`
Evaluates to itself.
(* multisymbol character literals other then unicode i.e. \u00A0 are not supported yet)

### Str
String. Wrapper over native java.lang.String .
Example: `"hello world!"`
Evaluates to itself.

### List / Sexp
Immutable linked list. Or limited S-expression: tail must always be a seq*.
(* Some Lisps allow: (5 . 8) pairs. There is no dot syntax in MaajScript ind if there were, the second argument would have to be a seq.)
Example: `(foo bar baz)`, `(count' 5 coll)`
Evaluated as function application.

### Seq
Sequence. Variant of list that is potentially lazy*. Does not necessarily know it's own length.
(* Only partially evaluated. The rest is determined but unevaluated.)
Can be created from any immutable collection or through other operations
Example: `(seq "hello") ;=> (\h \e \l \l \o)`
Evaluated as function application.

### Nil
A special value, can be used as: false, end of a sequence, .... - similar to `null` in Java, but is a valid term in that methods can be invoked on it.
Example: `()` - There is no other syntax for this value.

### Vector / Vec
Immutable array. - Supports O(1) [add to] and [remove from] end. Can hold any number of terms.
(wrapped Clojure data structure)
Example: `[1 2 3 :kw 4.78 \a]`
Evaluation evaluates all elements, returning resulting vector. (fmap)

### Map
Associative collection of key-value pairs. Both keys and values can by any* term.
Example: `{:key :value, :key2 5, :key3 ()}`
Evaluation evaluates all elements, returning resulting map. (fmap)
(* maps over 8 pairs require hashable keys. For now, all terms have correctly defined .hashCore() and .equals() - This might no longer be the case after wrapper for any JVM object is implemented. - With less then 8 pairs, only .equals() is required. )

---

### Invocable, Fn, Macro, Sf
Can by invoked. There is a variety of types (some evaluate arguments, some are treated differently in Java (different functional interfaces), ...)
Evaluates to itself.

### Seqable
Anything that can be coerced into a seq. - seq and collections. (and string, which is a special collection of chars)


## Special Forms
All following special forms are defined in namespace '#'. This namespace cannot be directly referenced and is loaded by default, imported into every other created namespace. In case of name clash: qualified form (i.e. `#/if`) can be used instead.

### `def`
Creates (or updates) a global mutable cell (Var) in the current namespace. Vars can only* be changed from the namespaces they were defined in. (* through `def` special form)
Syntax: `(def name value)`. Any metadata on `name` will be added to the created var itself. (Not if this only changes.)

###  `let`
Binds local scope variables with provided values. A binding is a pair of pattern binder and expression to evaluate. One let can contain multiple such pairs.
Variables are no longer bound after leaving let construct, in which they were created.
Syntax: `(let [pattern expression ...] body)`
Binding: Currently only 3 version of binding are supported:

- simple: just a symbol : binds
    - Syntax: any symbol, that from now on can be used to access bound value.
- seq: captures positional elements in a seq
    - Syntax: `[a b c]` - This will bind first 3 terms in seq
    - `[a b & r]` - The r will capture the rest of the sequence.
        - & is a special symbol and cannot be bound to in this context.
    - If seq contains less terms then required: rest will be bound to nil.
- ignore: like simple, but ignore 
    - Syntax: `_`
    - The value will still be computed, only not captured.

Example: `(let [a (+ 7 5)] (* a (inc a))) ;=> 156`
`(let [[a b c] (50000)] (+ a b c)) ;=> 3`
- Explantion: `(50000)` creates a lazy seqence of numbers from 0 to 49999. Only first 3 will be actually realized and bound to `a`, `b` and `c`, so expression will become `(+ 0 1 2)` and `+` can sum any number of arguments.

Binding are recursive, so something like this is possible too: `[[a b] [c d] [_ [q w r & l]]]`.

//Note: I say 'bound' but associated might be a better term... Either way, they are immutable and expressions in let are evaluated eagerly. (i.e. not "bound" to potential results if needed, but actually computed and "assigned")

Known problem: Scope retains captured values even if they are no longer needed. - Can cause memory leaks: especially if context is captured in a closure: The same problem: Closure captures the entire scope, not just what is used inside.
- This might not seem like a problem, but imagine: if scope retains head of a lazy seq, that is then being reduced: under normal circumstances, only the necessary, little, part of seq is created at a time, and already consumed beginning of the seq is garbage collected... If the head is still retained in scope, although unneeded, it will never be GCed and will cause (potentially) huge space leaks.

### `do`
Do special form takes any number of expressions and evaluates them in turn, returning result of last. Other then last can be executed for side effects, as their result will never be captured anywhere.

### `if`
Takes 2 or 3 arguments. Evaluates first argument and based on it's returned value* evaluates and returns second argument or [if third argument is present, evaluates and returns that, otherwise returns nil].
Syntax: `(if test then else)` or `(if test then)` ~= `(if test then ())`

### `eval`
Evaluates it's arguments like any other function. (can only take 1) But, then evaluates the result of that. Which allows evaluation of any (possibly computed / otherwise obtained) term. As other lisps: Program is data, and possibly vice versa, so it does not take a string or anything special, but normal data structures.
Syntax: `(eval '(+ 7 8))`

### `apply`
Transforms a seq into argument list. Last argument of apply must be seqable. It replaces the last argument with all the arguments in the seq. This operation works even if last argument is infinite.
Syntax: `(apply fn possible other args seq-arg)`
Example: `(apply + [1 2 3])` - the same as `(+ 1 2 3)`.
`(apply + -15 128 (100))` - sums numbers 0 to 99 and -15 and 128

### `recur`
Allows tail recursion. Every Fn* tests it's result for being a recur. If it is, it repeats itself with arguments captured in the recur. - Recur itself is just a lightweight box for arguments. This special form can be used anywhere, but invoking virtually any other method then getArgs() results in "Recur outside looping context" exception.
This implies that it can only be used in a tail position and only inside Fn.
Syntax: `(recur arg1 arg2 ...)`
Example:  A function that will reverse a list.
```Clojure
(fn ([l] (recur () l))
    ([acc l] (if l 
                (recur (cons (first l) acc) (rest l)) 
                acc)))
``` 


(* essentially all normal functions: not macros or special forms)
### `var`
Normal symbol evaluation becomes lookup of that symbol in scope. Global scope is composed of Vars. i.e. Evaluation of a symbol accesses value of a Var. If I want to access the Var itself, rather then value stored in it, I have to use this special form. This special form completely ignores any scope variables.
Syntax: `(var symbol)`
There exist special reader macro that is translated into this special form: `#'symbol`

### `require'`
Loads and imports a file. Uses default Loader, that searches on CLASSPATH. Splits symbol name by `.` and replaces with `/`, adding `.maaj`. - Loader then evaluates the file and returns created namespace. `require'` then imports based on it's options
For details on options see: `(meta :doc #'require')`

- `:*` - loads not qualified
- nothing - fully qualified
- `:as`- qualified with another namespace
- there will also be an option to import as not qualified only specified symbols

Everything will be also fully qualified.

### `fnseq`
Creates a function (or a lambda; not different) that captures current context* / scope. (* Function will be evaluated in the namespace they were created in.) - The body of this expression will become the body of the function. It will not be evaluated now, only stored. The body can be potentially infinite.
Evaluating the function causes the body to be evaluated in the original context, with one* difference: there is special local variable injected: `$args`. - This variable shouldn't be used directly and is prone to change name. This special form generally shouldn't be used directly, use macro [`fn`](#fn) instead.
Syntax: `(fnseq "Hello World!")`

(* There is potentially more differences : if provided, name of this function will be in scope as well to allow recursion. There might be other changes. Argument list is the most important, though.)
### `macroseq`
The same as [`fnseq`](#fnseq), only has different evaluation semantics. See [Macro expansion](#macro-expansion). Shouldn't be used directly, use macro [`macro`](#macro) instead.

## Macro expansion
Macros are function, that upon evaluation generate code. In most Lisps, this is done at compilation time. I'm trying to do it once (while creating a function, essentially). - On the other hand, I can pass macros around and generally threat them as dynamically as functions. - At least some advantage of not having a compiled language.

//Note: At this point, macros sometimes work incorrectly, mainly: expand more then they should... (some combinations of macro expansion and quotation, unquotation...) - And ignores unquote - I will probaly have to make that a special term too...
// I know about it and I will address it when there is time. At this point, works mostly fine and macros are not the most import part of this project.

### Evaluation
Macros are evaluated *from outside in*. 
Macro expansion algorithm: 

 - on seq: Is this a macro application?
     - yes: apply and expand result
     - no : expand all arguments
 - on other then seq 
     - collection: just fmap (expand all terms)
     - ground: return itself

### `#macro` namespace
This namespace is imported by default, but kept fully qualified. Contains primitives mainly for manipulating macros.

All these special forms / terms have associated reader macros.

#### `quote`
On evaluation simply returns it's body (i.e. first argumet) without evaluating it.
Reader macro: `'x` -> `(#macro/quote x)`.
Example: `(#macro/quote (1 2 3)) ;=> (1 2 3)`

##### `quote-qualified`
Similar to `quote`, but traverses it's argument and : changes unqualified symbols to qualified (either by Var they represent, or current namespace) and runs `unquote` and `unquote-splicing`.
Reader macro: ``` `x ```. (backtick)
Example: ``` `(if a test/b) ;=> (#/if current-namespace/a test/b) ```

#### `expand`
Expects single argument. Returns it unevaluated, but macro expanded.
Example: `(#macro/expand (cadr [4 5 6])) ;=> (first (rest [4 5 6]))`

#### `unquote`
Unquote special terms (not forms) are terms, because they exist inside quoted terms. These are not evaluated and thus, they would never be found. - Instead a special operation unquoteTraverse is used in quote-qualified. If it encounters unquote, it evaluates it's body instead. Reader macro is the only way to create this.
Reader macro: `~x`generates something like `(#macro/unquote x)`
Example: ```(let [a 5] `[a ~a 5]) ;=> [current-namespace/a 5 5]``` 

Note: unquote only works inside `quote-qualified`

#### `unquote-splicing`
Works like [`unquote`](#unquote), only instead of injecting 1 value from evaluating it;'s body, expects body to result in something seqable. It then becomes all of the terms in the evaluated seqable, and replaces itself with that.
Reader macro: `~@x` generates something like `(#macro/unquote-splicing x)`
Example: ```(let [a [1 2 3]] `[a ~a ~@a 42] ) ;=> [cur-ns/a [1 2 3] 1 2 3 42]```


## Core functions and macros

### `fn`
Adds arity checks, arity overloads and argument list pattern binding to [`fnseq`](#fnseq).
Basic form is: `(fn pattern-bind  body in an implicit do block)`.
Arity check: arity is computed from pattern-bind, that must be a seq bind or simple bind ... or ignore. 

- A simple bind represents an overload with variadic arity and no minimal number of arguments required.
    - Is functionally equivalent with `[& r]`.
- A seq bind (i.e. vec):
    - if does not capture / ignore rest: has fixed arity.
        - example: `[a b c]` : has arity 3
    - if captures rest : is considered variadic, but might define minimal arity : number of terms specified
        - example: `[a b & r]` - this will match if `arity >= 2`. And will bind first 2 elements.
- There cannot be multiple overloads with the same arity or multiple variadic overloads.

Arity overloading syntax: args-bind-pattern and it's body are in a seq. - For all overloads.
Example: `(fn ([](recur 0)) ([x](inc x)))`- A function that increments it's argument, or calls itself with the argument 0. - Any other arity would throw an arity-exception.

### `macro`
The same as [`fn`](#fn) but uses [`macroseq`](#macroseq) instead.

### `defn`
Combines [`def`](#def) and [`fn`](#fn). You can specify meta data after name.
Form: same as `fn`, only first argument is name for def.
Example: `(defn name ^"the same as using just :name" [x] (:name x))`

### `defmacro`
The same as [`defn`](#defn) but uses [`macro`](#macro) instead.

---

### `first`
Returns first element (head) of a seqable argument.
### `rest`
Returns the rest (tail) of a seqable arument.
### `cons`
takes 2 args, second must be seqable; returns second argument with prepended first
### `seq`
Retuns a seqable as a seq.
### `reduce`
takes: 

- fn : invocable, that takes 2 arguments: accumulator and term, producing new accumulator
- acc : starting value of accumulator
- coll : Reducible (essentially: collection / seq)

Applies fn on acc and first term in coll, applies fn on this new accumulator and second term in coll ... until no more left. - If coll was from the start empty, returns starting accumulator.

### `lazy`
Captures 1 argument body, that must return a seq and returns a seq thunk, with a promise of evaluating the captured body when needed. The returned value can be used as a seq.

If given 2 arguments is essentially `(cons arg1 (lazy arg2))`.

### `require`
Calls `require'` for each argument.

### `cond`
Takes even number of args. eacch "pair" is interpreted as : test, body

- evaluates and returns body after first succesful test
    - if nothing matches returns nil
- tests are evaluated in order

Example: `(cond (0 < v) :pos, (0 > v) :neg, :else :zero)`

### `case`
Similar to Java switch construct.
Takes an expression and pairs. Tests each match on equality with computed value of first expression.
Tests sequentially.
Can take a special test match `_` that will match anything.

Example: `(case (get-value), 0 :ok, 1 :warn, 4 :err, _ :unknown)`

### `count`
Counts collection. Most times O(1), counting a seq can be O(n).

### `count'`
O(1) bound count : takes 2 args : bound and seq
- if counts a seq and gets over specified bound, returns Int.MAX_VALUE

### `gensym`
returns new unique symbol, with possible preffix specified as first argumetn

### `meta`
access meta data of a term

### Arithmetics

- `+, *` : work on arity 0-* (0 argumets : identity in associated monoid)
- `-, /` : work on arity 1-* (single argument : inverse in associated algebraic group)
- `min, max` : works on arity 1-*
- `<, >, <=, >=, ==` : works on arity 2
- `inc, dec, neg`: works on arity 1

### `=`
.equals() : works on arity 1-*

### bool

- `and, or`: works on arity 0-*
- `not`: works on arity 1

-----

... Some other functions are: `take, conj, assoc`...

- Informations about them can be found thorough `(meta :doc #'fn-name)`. Insead of `:doc` could be `:info`.

## IO
There is no IO yet. It will be implemented together with other Java interop. - I could implement it now as something like: having readln, writeln on some reader and writer (... possibly streams) stored in Context, but it would be mostly just a stub implementation. 

Thanks to Repl, it's easy to explore and can be used from Java...

## Repl
Interactive read-evaluate-print loop.
run: `maaj.Repl`.
What it does: It will:

- Read a term. 
- Evaluate it. 
- Print what it's become. 
- Repeat this in a loop, until the reader encounters EOF.

You can also look at implementation of this class (it's very short) on how Maaj Script can be used from Java.
I did not provide any special API because I plan to use Java Scripting API, when I get to do it.

## Implemetation concepts

Term is an interface, with methods like `.eval(Context):Term`, `.apply(Context, Arguments):Term`, `.Show(Writer):Void`...
- Each term then knows how to perform these actions specifically to it's details. Possibly invoking the more methods on other Terms it contains...

Scope is stored in special type `Context` that consists of mainly 2 things: reference to global variables and local scope, represented as a immutable map.

Reading mainly works as a (single pass) push-down automaton, where the entire state is position in code. Only exception to this are "terminated" constructs like `(...),[...],{...},"..."` which are read in a loop, but read the insides normally.

Reader has a lot unused "slots" (special characters; switching to different dispatch table). Some are decided, but not yet implemented (like sets or regexps), some are reserved, or otherwise unused. - When these are encountered, reading fails. (as with any syntax error; the difference is : these can in future change to something meaningful)

Complex data structures (vectors, maps) are just wrappers over Clojure data structures.

Let (and fn) argument binding works such that from the bind expression is created a function that can create a bindings map from it's argument.
