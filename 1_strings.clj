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
;; test whether a pattern matches in a substring
(if (re-find #"pattern" (subs string 0 (- (count string) 10)))
  (printf "Pattern matches in last 10 characters.\n"))

;; Substitute "at" for "is", restricted to first five characters.
;; Clojure doesn't have mutable strings, so it doesn't have mutable
;; substrings, either.  We build up a new string from the ones we
;; have.
(let [string (str (str/replace (subs string 0 5) #"is" "at")
                  (subs string 5))]
  ;; ...
  )
;; -----------------------------
;; exchange the first and last letters in a string
(let [a "make a hat"
      len-1 (dec (count a))
      a (str (subs a len-1)
             (subs a 1 len-1)
             (subs a 0 1))]
  (printf "%s" a))
;; take a ham
;; -----------------------------
;; TBD: extract column like Perl's unpack does
;; -----------------------------
;; We'll show how to implement cut2fmt in Clojure, but since the
;; return value in the original code is intended as an input string to
;; unpack, which Clojure does not have built in, it would be better to
;; write different code for the intended purpose of splitting up lines
;; at particular column numbers.
(defn cut2fmt [& positions]
  (let [positions-with-1-first (cons 1 (seq positions))
        pairs (partition 2 1 positions-with-1-first)
        deltas (map (fn [[lastpos place]] (- place lastpos)) pairs)
        template-parts (map #(format "A%d " %) deltas)]
    (str (apply str template-parts) "A*")))

(let [fmt (cut2fmt 8 14 20 26 30)]
  (printf "%s\n" fmt))
;; A7 A6 A6 A6 A4 A*

;; Here is variation on cut2fmt, using ->> to shorten it.  Each of the
;; let symbols above becomes the last argument to the next expression.
;; Sometimes you want it shorter like this, but sometimes the
;; intermediate names are useful for understanding how the code works.
(defn cut2fmt [& positions]
  (let [template-parts (->> (cons 1 (seq positions))
                            (partition 2 1)
                            (map (fn [[lastpos place]] (- place lastpos)))
                            (map #(format "A%d " %)))]
    (str (apply str template-parts) "A*")))

;; Here is a Clojure function to split a string at the specified list
;; of column numbers.  It does not implement the full functionality of
;; Perl's unpack.
(defn split-at-cols [& positions]
  (let [positions-0-first (cons 0 (map dec positions))
        last-pos (last positions-0-first)
        pairs (partition 2 1 positions-0-first)
        subs-args (concat pairs (list (list last-pos)))]
    (fn [s]
      (map #(apply subs s %) subs-args))))

(def s "12345678901234567890123456789012345678901234567890")
(def splitter (split-at-cols 8 14 20 26 30))
(splitter s)
;; ("1234567" "890123" "456789" "012345" "6789" "012345678901234567890")
;; -----------------------------

;; @@PLEAC@@_1.2 Establishing a Default Value
;; -----------------------------
;; While Perl treats undef, 0, and "" as false, Clojure treats the
;; values false and nil as false, but 0 and "" as true.

;; use b if b is true, else c
;; Note that if b has never been defined or had a value bound to it,
;; then unlike Perl this will give an error that the value is
;; undefined.
(def a (or b c))

;; re-define x with the value y, unless x is already true
(def x (when-not x y))
;; -----------------------------
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
;; -----------------------------
;; Clojure data structures are immutable.  The code below does not
;; change the value of *command-line-args*, whereas Perl
;; 'shift(@ARGV)' does modify @ARGV by removing its first element.
(def dir (or (first *command-line-args*) "/tmp"))
;; -----------------------------
;; The previous Clojure example is quite close to the Perl example's
;; behavior here.  One way in which the Clojure example behaves better
;; is that even if the first command line argument is the string "0",
;; that is treated as logically true by Clojure, since it is neither
;; nil nor false, so dir will become "0" as intended if that is the
;; first command line argument, not "/tmp".
;; -----------------------------
;; This Perl version works the same as the previous example, except
;; the Perl version modifies @ARGV, but the Clojure version leaves
;; *command-line-args* unmodified.
;; -----------------------------
;; The Clojure code above is completely identical to the behavior of
;; this Perl code: neither modifies the list of command line args, and
;; both use "0" if that is the value of the first arg.
;; -----------------------------
;; See Section 4.6 for more explanation of update-in and fnil.
(let [count (update-in count [(or shell "/bin/sh")] (fnil inc 0))]
  ;; ...
  )
;; -----------------------------
;; TBD: What would Clojure version of Perl's getlogin() and getpwuid()
;; be?  For $< to get the real uid of this process?
(let [user (or (get (System/getenv) "USER")
               (get (System/getenv) "LOGNAME")
               "Unknown uid number")]
  ;; ...
  )
;; -----------------------------
(let [starting-point (or starting-point "Greenwich")]
  ;; ...
  )
;; -----------------------------
(let [a (if (or (nil? a) (== 0 (count a))) b a)]  ; assign if a was nil or empty
  ;; ...
  )
(let [a (if (or (nil? b) (== 0 (count b))) c b)]  ; assign b if nonempty, else c
  ;; ...
  )
;; -----------------------------


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
;; Clojure has a type for an individual character, unlike Perl which
;; has strings of characters, but not a separate type for an
;; individual character.
(def num (int \a))     ; => ASCII code 97
(def c (char 97))      ; => \a
;; -----------------------------

(defn print-ascii-code-for-char [c]
  (printf "Number %d is character '%c'\n" (int c) c))

;; (print-ascii-code-for-char \a)
;; Number 97 is the ASCII character a
;; -----------------------------
(def ascii (map int string))
(def string (apply str (map char ascii)))
;; -----------------------------
(def ascii-value (int \e))  ; now 101
(def character (char 101))  ; now character \e
;; -----------------------------
(def ascii-character-numbers (map int "sample"))
(printf "%s\n" (str/join " " ascii-character-numbers))
115 97 109 112 108 101

(def word (apply str (map char ascii-character-numbers)))
(def word (apply str (map char [ 115 97 109 112 108 101 ])))
(printf "%s\n" word)
sample
;; -----------------------------
(let [hal "HAL"
      ascii (map int hal)
      ascii (map inc ascii)  ; add one to each ASCII value
      ibm (apply str (map char ascii))]
  (printf "%s\n" ibm))       ; prints "IBM"
;; -----------------------------

;; @@PLEAC@@_1.5 Processing a String One Character at a Time
;; -----------------------------
;; I'm not sure why, but this:
(def array (str/split string #""))
;; does not work the same as the Perl split(//, $string).  The Clojure
;; version returns an empty string as the first item in the result,
;; whereas the Perl does not.

;; This will split up a string into one string per 16-bit Java
;; character.  Note that it does not try to keep together UTF-16
;; surrogate pairs as a single character.
(def sequence (map str (seq string)))

;; As mentioned in previous section, this will get their ASCII values,
;; if only ASCII values are in the string, or in general get 16-bit
;; code points, treating surrogate pairs as two consecutive 16-bit
;; values.
(def sequence (map int string))

;; TBD: Consider writing a version that works with UTF-16 surrogate
;; pairs in the string, converting them into a single string, or a
;; single integer, when they are found.
;; -----------------------------
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
          (apply str (sort (set string)))))
;; => (print-uniq-chars "an apple a day")
;; unique chars are:  adelnpy
;; -----------------------------
;; (re-seq #"." string) returns a sequence of length 1 strings, as
;; opposed to (set string) above, which returns a sequence of
;; characters, which are different objects than length 1 strings in
;; Java and Clojure.
(defn print-uniq-chars [string]
  (printf "unique chars are: %s\n"
          (apply str (sort (set (re-seq #"." string))))))
;; => (print-uniq-chars "an apple a day")
;; unique chars are:  adelnpy
;; -----------------------------
(defn print-ascii-value-sum [string]
  (printf "sum is %s\n" (apply + (map int string))))
;; => (print-ascii-value-sum "an apple a day")
;; sum is 1248
;; -----------------------------
;; TBD: Clojure version of Perl's $sum = unpack("%32C*", $string); ?
;; -----------------------------
;; TBD: Clojure version of include/perl/ch01/sum
;; -----------------------------
;; @@INCLUDE@@ include/clojure/ch01/slowcat.clj
;; -----------------------------

;; @@PLEAC@@_1.6 Reversing a String by Word or Character
;; -----------------------------
;; Make namespace clojure.string usable with the abbreviated name
;; 'str'.
(require '[clojure.string :as str])

(def revbytes (str/reverse string))
;; -----------------------------
;; Clojure's (str/split str #"\s+") is almost the same behavior as
;; Perl's split(" ", $str), except if $str has leading whitespace, in
;; which case the former will return a list where the first string is
;; empty, but the latter will not.  perl-split-on-space handles this
;; the same as Perl does, even for that case, by first removing any
;; leading whitespace before doing the split.
(defn perl-split-on-space [s]
  (str/split (str/triml s) #"\s+"))

(str/join " " (reverse (perl-split-on-space str)))
;; -----------------------------
(def gnirts (str/reverse string))    ; str/reverse reverses letters in string

(def sdrow (reverse words))          ; reverse reverses elements in sequence

(def confused (str/reverse (str/join "" words)))
;; -----------------------------
;; reverse word order
(def string "Yoda said, \"can you see this?\"")
(def allwords (perl-split-on-space string))
(def revwords (str/join " " (reverse allwords)))
(printf "%s\n" revwords)
this?" see you "can said, Yoda
;; -----------------------------
(def revwords (str/join " " (reverse (perl-split-on-space str))))
;; -----------------------------
;; Perl's split, when given a regex containing a parenthesized group,
;; returns strings in the resulting list that match that group, but
;; Java and Clojure's split do not do this.  We can write something
;; similar as follows.

;; This function requires that the regex is of the form:
;; #"^(.*?)(your desired split pattern here)(.*)$"
(defn split-with-capture [s re]
  (loop [result []
         s s]
    (if (= s "")
      result
      (if-let [[all pre middle post] (re-find re s)]
        (if (= pre "")    ; Ignore a 0-length match of ^(.*?)
          (recur (conj result middle) post)
          (recur (conj result pre middle) post))
        ;; else we are done, and s is the last string to be returned
        (conj result s)))))

(def revwords (str/join "" (reverse (split-with-capture str
                                      #"^(.*?)(\s+)(.*)$"))))

;; We can write a version that does not require the ^(.*?) and (.*)$
;; in the regex if we use re-groups+ from Section 1.7 and a modified
;; re-find that always returns the part of the string before and after
;; a match.  See Section 6.7 for that.
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

;; Note: (or *command-line-args* [*in*]) returns *command-line-args*
;; if one or more command line args were specified, otherwise it
;; returns a vector containing one element, the value of *in*.  This
;; makes the following code reasonably close to the behavior of Perl's
;; "while (<>) ...".
(doseq [file (or *command-line-args* [*in*])]
  (with-open [rdr (io/reader file)]
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


;; If you often want to write Clojure code that operates like Perl's
;; "while (<>) ...", or some other control structure that doesn't
;; already exist in Clojure, you can make a new one with defmacro.
;; For example, here is a macro while-<> and an example of its use
;; that works like the above.
(defmacro while-<>
  [[file line] & body]
  `(doseq [~file (or *command-line-args* [*in*])]
     (with-open [rdr# (clojure.java.io/reader ~file)]
       (doseq [~line (line-seq rdr#)]
         ~@body))))

(while-<> [file line]
  (when (and (= line (str/reverse line))
             (>= (count line) 5))
    (printf "%s\n" line)))
;; -----------------------------


;; @@PLEAC@@_1.7 Expanding and Compressing Tabs
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
;; each time through the loop (TBD: true?).  If you want it to be
;; compiled only once, wrap the function body in a (let [pat #"\t+"]
;; ...) and use pat in place of #"\t+" in the body.

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
    (if (.find m)
      (let [buffer (StringBuffer. (.length s))]
        (let [groups (re-groups+ m s)
              rep (f groups)]
          (.appendReplacement m buffer rep)
          (.appendTail m buffer)
          [(second groups) (str buffer)]))
      [nil s])))

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

;; Use your preferred version of expand here.  See Section 1.6 for
;; definition of while-<>.

(while-<> [file line]
  (printf "%s\n" (expand line)))
          
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

(while-<> [file line]
  (printf "%s\n" (unexpand line)))
;; -----------------------------

;; @@PLEAC@@_1.8 Expanding Variables in User Input
;; -----------------------------
;; -----------------------------

;; @@PLEAC@@_1.9 Controlling Case
;; -----------------------------
(use '[clojure.string :only (upper-case lower-case capitalize replace)])

(def big (upper-case little))     ; "bo peep" -> "BO PEEP"
(def little (lower-case big))     ; "JOHN"    -> "john"
;; I know of no way in Clojure to change case similar to Perl's \U and
;; \L inside of interpolated strings.
;; -----------------------------
;; Clojure has no ucfirst or lcfirst, but we can write them easily
;; enough if we want them.  This lcfirst is patterned after Clojure's
;; clojure.string/capitalize.  ucfirst is nearly identical.
(defn lcfirst [^CharSequence s]
  (let [s (.toString s)]
    (if (< (count s) 2)
      (lower-case s)
      (str (lower-case (subs s 0 1)) (subs s 1)))))

(def little (lcfirst big))        ; "BoPeep"    -> "boPeep" 

;; Clojure's capitalize is like Perl's ucfirst, except it upper-cases
;; the first character, and lower-cases the rest.
(def big (capitalize little))     ; "bO"      -> "Bo"
;; -----------------------------
;; Clojure's upper-case is based on Java's
;; java.lang.String/toUpperCase, which respects the current default
;; Locale.  Similarly for lower-case and capitalize.
(def beast "dromedary")
;; capitalize various parts of beast
(def capit (capitalize beast))     ; Dromedary
(def capall (upper-case beast))    ; DROMEDARY
(def caprest (lcfirst (upper-case beast)))  ; dROMEDARY
;; -----------------------------
;; capitalize each word's first character, downcase the rest
(let [text "thIS is a loNG liNE"
      text (replace text #"\w+" capitalize)]
  (printf "%s\n" text))
This Is A Long Line
;; -----------------------------
;; @@INCLUDE@@ include/clojure/ch01/randcap.clj
;; -----------------------------

;; 1.10 Interpolating Functions and Expressions Within Strings

;; 1.11 Indenting Here Documents

;; 1.12 Reformatting Paragraphs

;; 1.13 Escaping Characters

;; 1.14 Trimming Blanks from the Ends of a String

(.trim string)
;; (.trim "  foo  ") => "foo"

;; 1.15 Parsing Comma-Separated Data

;; 1.16 Soundex Matching

;; 1.17 Program: fixstyle

;; 1.18 Program: psgrep
