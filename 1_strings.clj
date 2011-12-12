;; @@PLEAC@@_1.0 Introduction

;; ---------------------------
(def string "\\n")                 ; two characters, \ and an n
(def string "Jon 'Maddog' Orwant") ; literal single quotes
;; ---------------------------
(def string "\n")                    ; "newline" character
(def string "Jon \"Maddog\" Orwant") ; literal double quotes

(def a "
    This is a multiline here document
    terminated by one double quote.
    ")

;; @@PLEAC@@_1.1 Accessing Substrings
(def value (subs string offset (+ offset count)))
(def value (subs string offset (count string)))

;; or
(def value (subs string offset))

;; Clojure strings are immutable Java strings, so while you cannot
;; modify an existing string, you can build a new one with part of it
;; replaced by another.

(def string (str (subs string 0 offset) newstring
                 (subs string (+ offset count))))
(def string (str (subs string 0 offset) newtail))

;; -----------------------------
;; get a 5-byte string, skip 3, then grab 2 8-byte strings, then the rest

;; split at 'sz' byte boundaries
;; jli for mbac: partition is the bomb for this
;; mbac for jli: hell yeah!
;; jli for mbac: I meant, "partition" is old and tired. all the cool
;;               kids are using "partition-all". see commify-hipster.
(defn split-every-n-chars [sz string]
  (if (empty? string)
    ()
    (try
      (let [beg (subs string 0 sz)
            rest (subs string sz)]
        (cons beg (split-every-n-chars sz rest)))
      (catch Exception _e [string]))))

;; or the more idiomatic version
(defn split-every-n-chars [sz string]
  ;; the map turns vector of char vector into vector of string
  (map (fn [x] (apply str x))
       (partition 5 5 nil string)))

(def fivers (split-every-n-chars 5 string))

;; chop string into individual characters
(def chars (seq string))

;; -----------------------------
(def string "This is what you have")
;; Indexes are left to right. There is no possibility to index
;; directly from right to left
;; "T"
(def first (subs string 0 1))
;; "is"
(def start (subs string 5 7))
;; "you have"
(def rest (subs string 13))
;; "e" *)
(def last (let [len (count string)]
            (subs string (- len 1))))
;; "have"
(def theend (let [len (count string)]
              (subs string (- len 4))))
;; "you"
(def piece (let [len (count string)]
             (subs string (- len 8) (- len 5))))

;; -----------------------------
(def string "This is what you have")
(printf "%s" "string")

;; Change "is" to "wasn't"
(def string (str
             (subs string 0 5)
             "wasn't"
             (subs string 7)))
;; This wasn't what you have

;; This wasn't wonderous
(def string
     (str (subs string 0 (- (count string) 12)) "ondrous"))

;; delete first character
(def string (subs string 1))
;; his wasn't wondrous

;; delete last 10 characters
(def string (subs string 0 (- (count string) 10)))
;; his wasn'
;; -----------------------------

;; @@PLEAC@@_1.2 Establishing a Default Value

;; While Perl treats undef, 0, and "" as false, Clojure treats the
;; values false and nil as false, but 0 and "" as true.

;; -----------------------------
;; use b if b is true, else c
;; Note that if b has never been defined or had a value bound to it,
;; then unlike Perl this will give an error that the value is
;; undefined.
(def a (or b c))

;; re-define x with the value y, unless x is already true
(def x (when-not x y))

;; use b if b is defined, otherwise c
;; This correctly tests whether b is bound to a value or not, but
;; if it is not, then it throws an exception because of the last
;; occurrence of b not having a value.
(def a (if (find (ns-interns *ns*) 'b) b c))
;; This is closer:
(def a (if (find (ns-interns *ns*) 'b) (eval 'b) c))

;; But note that if b is only bound in a let, or as a function
;; argument, but not at the top level with def or something similar,
;; then this code will go with the value of c.
(let [c "c-value"
      b "b-value"]
  (let [a (if (find (ns-interns *ns*) 'b) (eval 'b) c)]
    (printf "a=%s" a)))
;; a=c-value

;; -----------------------------
(def foo (or bar "DEFAULT VALUE"))

;; Clojure data structures are immutable.  The code below does not
;; change the value of *command-line-args*, whereas Perl
;; 'shift(@ARGV)' does modify @ARGV by removing its first element.
(def dir
  (if (>= (count *command-line-args*) 1)
    (nth *command-line-args* 0)
    "/tmp"))

;; @@PLEAC@@_1.3 Exchanging Values Without Using Temporary Variables
;; -----------------------------

;; This Clojure code does _not_ exchange values of var1 and var2
;; without a temporary.  It binds var1 to the value of var2, then
;; binds var2 to the new value of var1, so they both end up with the
;; original value of var2.

(let [var1 var2
      var2 var1])

;; This will achieve the desired effect.  It creates a vector of the
;; values of var2 and var1, then binds them using a technique called
;; 'destructuring' to var1 and var2.

(let [[var1 var2] [var2 var1]])


;; -----------------------------
(def temp a)
(def a b)
(def b temp)
;; -----------------------------
(let [a "alpha"
      b "omega"]
  (let [[a b] [b a]]
    ;; the first shall be last -- and versa vice
    ))

;; -----------------------------
(let [alpha "January"
      beta "March"
      production "August"]
;; move beta to alpha
;; move production to beta
;; move alpha to production
  (let [[alpha beta production] [beta production alpha]]

    ))

;; @@PLEAC@@_1.4 Converting Between ASCII Characters and Values

;; -----------------------------
(def num (int \a))     ; => ASCII code 97
(def char (char 97))   ; => \a
;; -----------------------------

(defn print-ascii-code-for-char [c]
  (printf "Number %d is character '%c'\n" (int c) c))

;; (print-ascii-code-for-char \a)
;; Number 97 is the ASCII character a

;; @@PLEAC@@_1.5 Processing a String One Character at a Time
;; Strings in Clojure can be treated as sequences, so the usual
;; map, reduce, doseq functions apply.
(defn one-char-at-a-time [f string] (doseq [b string] (f b)))

;; => (one-char-at-a-time
;;       (fn [b] (printf "do something with: %c\n" b))
;;       "abc")
;; do something with: a
;; do something with: b
;; do something with: c
;; ----------------------------

(defn print-uniq-chars [string]
  (printf "unique chars are: %s\n"
          (sort (set string))))
;; => (print-uniq-chars "an apple a day")
;; unique chars are: (\space \a \d \e \l \n \p \y)
;; -----------------------------
(defn print-ascii-value-sum [string]
  (printf "sum is %s\n" (apply + (map int string))))
;; => (print-ascii-value-sum "an apple a day")
;; sum is 1248
;; -----------------------------

;; @@PLEAC@@_1.6 Reversing a String by Word or Character
;; -----------------------------
;; Make namespace clojure.string usable with the abbreviated name
;; 'str'.
(require '[clojure.string :as str])

(def revbytes (str/reverse string))
;; -----------------------------
;; TBD: Verify whether the split call below matches the behavior of
;; Perl split with a " " as first arg.  Should we use the regular
;; expression #"\s+" to match Perl behavior more closely?  Does that
;; even match exactly?  What about white space before first word or
;; after last word in the string to be split?
(str/join " " (reverse (str/split str #"\s+")))
;; -----------------------------
(def gnirts (str/reverse string))    ; str/reverse reverses letters in string

(def sdrow (reverse words))          ; reverse reverses elements in sequence

;; TBD: What corresponds to following Perl?
;; $confused = reverse(@words);        # reverse letters in join("", @words)
;; -----------------------------
;; reverse word order
(def string "Yoda said, \"can you see this?\"")

(def allwords (str/split string #"\s+"))

(def revwords (str/join " " (reverse allwords)))

(printf "%s\n" revwords)
;; -----------------------------
;; There is no shortcut in Clojure like in Perl for the last arg of
;; str/split equal to " " meaning the same thing as matching on the
;; regular expression #"\s+"
;; -----------------------------
(def revwords (str/join " " (reverse (str/split str #"\s+"))))
;; -----------------------------
(def word "reviver")
(def is-palindrome (= word (str/reverse word)))
;; -----------------------------
;; No Clojure program I know of equivalent to Perl's for finding
;; palindromes of length 5 or larger conveniently fits into a single
;; line.  Better to create a file with this program.

(ns print-palindromes
  (:require [clojure.string :as str]
            [clojure.java.io :as io]))

(doseq [filename *command-line-args*]
  (with-open [rdr (io/reader filename)]
    (doseq [line (line-seq rdr)]
      (when (and (= line (str/reverse line))
                 (>= (count line) 5))
        (printf "%s\n" line)))))

;; Save the above in a file print-palindromes.clj, then run from
;; command prompt (replace path to wherever your clojure-1.3.0.jar
;; file is located):

;; % java -cp /Users/jafinger/lein/clj-1.3.0/lib/clojure-1.3.0.jar clojure.main print-palindromes.clj /usr/share/dict/words

;; Alternately, on Linux/*BSD/Mac OS X, create a shell script like
;; this:

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; #! /bin/sh
;; 
;; # Replace the path below to refer to the Clojure jar file on your system.
;; CLJ_JAR=$HOME/lein/clj-1.3.0/lib/clojure-1.3.0.jar
;; scriptname="$1"
;; shift
;; java -cp $CLJ_JAR:. clojure.main "$scriptname" "$@"
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

;; If you save that as the file 'clj' somewhere in your command path
;; and make it executable (remove the ';; ' before each line), and add
;; this line to the beginning of print-palindromes.clj:
;;
;; #! /usr/bin/env clj
;;
;; Then you can use the command line:
;;
;; % ./print-palindromes.clj /usr/share/dict/words
;;
;; or if print-palindromes.clj is in your command path (perhaps
;; because . is in your command path, although I wouldn't recommend it
;; for security reasons):
;;
;; % print-palindromes.clj /usr/share/dict/words

;; @@PLEAC@@_1.7 Reversing a String by Word or by Character

;; -----------------------------
;; Clojure's built-in regexp matching functions have something like
;; Perl's $& that returns everything that a regexp matched within a
;; string, and also like Perl's $1, $2, $3, etc. that match
;; parenthesized groups.  However, it seems to require calls to Java
;; methods to get something like Perl's $` and $' that return the
;; strings that are before and after the regexp match.

;; The Clojure function expand-str below is most closely analagous to
;; the following Perl code:

;; sub expand_str {
;;     my $s = shift;
;;     while (1) {
;;         if ($s =~ /\t+/) {
;;             $s = $` . (' ' x (length($&) * 8 - length($`) % 8)) . $';
;;         } else {
;;             return $s;
;;         }
;;     }
;; }

(defn expand-str [s]
  (loop [s s]
    (let [m (re-matcher #"\t+" s)
          tabs (re-find m)]         ; Like Perl's $&
      (if tabs
        (let [before-tabs (subs s 0 (. m (start)))  ; Like Perl's $`
              after-tabs (subs s (. m (end)))]      ; $'
          (recur (str before-tabs
                      (apply str (repeat (- (* (count tabs) 8)
                                            (mod (count before-tabs) 8))
                                         " "))
                      after-tabs)))
        s))))

;; Performance note: The code above will recompile the regexp #"\t+"
;; each time through the loop.  If you want it to be compiled only
;; once, wrap the function body in a (let [pat #"\t+"] ...) and use
;; pat in place of #"\t+" in the body.

;; Another way is to use the regexp "^([^\t]*)(\t+)" instead of simply
;; "\t+".  The ([^\t]*) will explicitly match everything before the
;; first tabs.  Warning: using (.*) instead would greedily match as
;; much of the beginning of the string as possible, including tabs, so
;; would not correctly cause (\t+) to match the _first_ tabs in the
;; string.

;; According to http://dev.clojure.org/jira/browse/CLJ-753 there is a
;; bug in str/replace-first where it returns nil instead of the
;; unmodified string s if the regexp pattern is not found to match
;; anywhere in s.

;; replace-first-fixed is a modified version of str/replace-first that
;; behaves as the corrected version should.

(defn replace-first-fixed [s pat fn]
  (if-let [new-s (str/replace-first s pat fn)]
    new-s
    s))

;; The last argument to str/replace-first is a fn that takes a vector
;; of strings as an argument.  The first of these strings is
;; everything that was matched by the regexp pattern.  The rest are
;; the strings matched by parenthesized groups inside the regexp.  We
;; use Clojure's destructuring on function arguments to break up the
;; vector argument to replace-tabs and give names to its elements.

(defn replace-tabs [[all-matched before-tabs tabs]]
  (str before-tabs
       (apply str (repeat (- (* (count tabs) 8)
                             (mod (count before-tabs) 8))
                          " "))))

;; Repeatedly call replace-first-fixed until the string does not
;; change, indicating that no match was found.

(defn expand-str [s]
  (loop [s s]
    (let [next-s (replace-first-fixed s #"^([^\t]*)(\t+)" replace-tabs)]
      (if (= s next-s)
        s
        (recur next-s)))))

;; Performance note: Same as above about the regexp being recompiled
;; every time through the loop.  Bind the regexp to a symbol using
;; let, outside of the loop, to compile it only once.
  
;; My favorite version of this requires defining slightly modified
;; versions of clojure.core/re-groups and clojure.string/replace-first

;; The modified re-groups+ returns a vector like (re-groups) does,
;; except it always returns a vector, even if there are no
;; parenthesized subexpressions in the regexp, and it always returns
;; the part of the string before the match (Perl's $`) as the first
;; element, and the part of the string after the match (Perl's $') as
;; the last element.

(defn re-groups+ [^java.util.regex.Matcher m s]
  (let [gc (. m (groupCount))
        pre (subs s 0 (. m (start)))
        post (subs s (. m (end)))]
    (loop [v [pre] c 0]
      (if (<= c gc)
        (recur (conj v (. m (group c))) (inc c))
        (conj v post)))))

;; replace-first+ is based on Clojure's hidden internal function
;; replace-first-by, except that it calls the user-supplied fn f for
;; calculating the replacement string with the return value of
;; re-groups+ instead of re-groups, so f can use those additional
;; strings to calculate the replacement.

;; The other difference is that it returns a vector of two elements:
;; the first is the string matched, or nil if there was no match.  The
;; second is the string after replacement on a match, or the original
;; string if no match.

(defn replace-first+
  [^CharSequence s ^java.util.regex.Pattern re f]
  (let [m (re-matcher re s)]
    (let [buffer (StringBuffer. (.length s))]
      (if (.find m)
        (let [groups (re-groups+ m s)
              rep (f groups)]
          (.appendReplacement m buffer rep)
          (.appendTail m buffer)
          [(second groups) (str buffer)])
        [nil s]))))

;; Assuming the above are added to Clojure, or some user-defined
;; library of commonly-used utilities, the "new code" is as follows:

(defn expand-str [s]
  (loop [[found-match s] [true s]]
    (if found-match
      (recur (replace-first+ s #"\t+"
                             (fn [[pre tabs post]]
                               (apply str (repeat (- (* (count tabs) 8)
                                                     (mod (count pre) 8))
                                                  " ")))))
      s)))

;; Performance note: As before, assign the regexp #"\t+" to a symbol
;; using let, outside of the loop, to compile it only once, instead of
;; every time through the loop.

;; Test cases:

;; (let [t1 (= "No tabs here" (expand-str "No tabs here"))
;;       t2 (= "Expand          this" (expand-str "Expand\t\tthis"))
;;       t3 (= "Expand          this    please" (expand-str "Expand\t\tthis\tplease"))]
;;   [t1 t2 t3])
;; -----------------------------
;; I am not aware of any Clojure library similar to Perl's Text::Tabs

;; The expand-str Clojure functions above work on individual strings.
;; This works on a string or a collection of strings, similar to how
;; Perl's does:

(defn expand [x]
  (cond
   (instance? String x) (expand-str x)
   :else (map expand-str x)))


(defn unexpand-line [s]
  (let [s (expand s)
        len (count s)
        tabstop *tabstop*
        sections (map #(subs s % (min len (+ % tabstop)))
                      (range 0 len tabstop))
        ;; last section must be handled differently than earlier ones
        lastbit (last sections)
        sections (butlast sections)
        lastbit (if (and (= (count lastbit) tabstop)
                         (str/blank? lastbit))
                  "\t"
                  lastbit)
        sections (map #(str/replace % #"  +$" "\t") sections)
        sections (conj (vec sections) lastbit)]
    (str/join "" sections)))

(defn unexpand-str [s]
  (str/join "\n" (map unexpand-line (str/split s #"\n" -1))))

(defn unexpand [x]
  (cond
   (instance? String x) (unexpand-str x)
   :else (map unexpand-str x)))
;; -----------------------------
(ns expand
  (:require [clojure.string :as str]
            [clojure.java.io :as io]))

;; Use your preferred version of expand here.

(doseq [filename *command-line-args*]
  (with-open [rdr (io/reader filename)]
    (doseq [line (line-seq rdr)]
      (printf "%s\n" (expand line)))))
;; -----------------------------
;; Below is a version of expand-str that takes an optional argument
;; tabstop.  It is based upon the last version of expand-str given
;; above, but the others could easily be generalized in a similar way.

(defn expand-str
  ([s tabstop]
     (loop [[found-match s] [true s]]
       (if found-match
         (recur (replace-first+
                 s #"\t+"
                 (fn [[pre tabs post]]
                   (apply str (repeat (- (* (count tabs) tabstop)
                                         (mod (count pre) tabstop))
                                      " ")))))
         s)))
  ([s] (expand-str s 8)))

;; If one wished for a version of expand-str that could use a tabstop
;; supplied by a "global variable", then a dynamic var named *tabstop*
;; that was used inside of expand-str would be a good way to do it.

(def ^:dynamic *tabstop* 8)

(defn expand-str [s]
  (loop [[found-match s] [true s]]
    (if found-match
      (recur (replace-first+
              s #"\t+"
              (fn [[pre tabs post]]
                (apply str (repeat (- (* (count tabs) *tabstop*)
                                      (mod (count pre) *tabstop*))
                                   " ")))))
      s)))

(expand-str "Expand\t\tthis") ; expands to tabstop 8
(def ^:dynamic *tabstop* 4)
(expand-str "Expand\t\tthis") ; expands to tabstop 4 this time

;; Performance note: Besides the repeated one about avoiding
;; recompilation of the regexp, a new performance issue here is that
;; accessing dynamic vars like *tabstop* is slower than accessing a
;; local binding like those introduced via let or loop.  Wrapping the
;; entire function body in something like (let [tabstop *tabstop*]
;; ... ) and using tabstop in place of *tabstop* inside the body
;; incurs this cost only once, instead of every time through the loop.
;; -----------------------------
(ns unexpand
  (:require [clojure.string :as str]
            [clojure.java.io :as io]))

;; Use your preferred version of expand and unexpand here.

(doseq [filename *command-line-args*]
  (with-open [rdr (io/reader filename)]
    (doseq [line (line-seq rdr)]
      (printf "%s\n" (unexpand line)))))
;; -----------------------------

;; 1.8 Expanding and Compressing Tabs

;; 1.9 Expanding Variables in User Input

;; @PLEAC@@_1.10 Controlling Case
(.toUpperCase "foo") ;; -> "FOO"
(.toLowerCase "FOO") ;; -> "foo"

;; 1.11 Interpolating Functions and Expressions Within Strings

;; 1.12 Indenting Here Documents

;; 1.13 Reformatting Paragraphs

;; 1.14 Escaping Characters

;; 1.15 Trimming Blanks from the Ends of a String

(.trim string)
;; (.trim "  foo  ") => "foo"

;; 1.16 Parsing Comma-Separated Data
