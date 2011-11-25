;; @@PLEAC@@_1.0 Strings
;; @@PLEAC@@_1.1 Introduction

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

;; @@PLEAC@@_1.2 Accessing Substrings
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

;; @@PLEAC@@_1.3 Establishing a Default Value

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

;; @@PLEAC@@_1.4 Exchanging Values Without Using Temporary Variables
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

;; @@PLEAC@@_1.5 Converting Between ASCII Characters and Values

;; @@PLEAC@@_1.6
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

(print revwords "\n")
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
  (:import (java.io BufferedReader FileReader))
  (:require [clojure.string :as str]))

(doseq [filename *command-line-args*]
  (doseq [line (line-seq (BufferedReader. (FileReader. filename)))]
    (when (and (= line (str/reverse line))
               (>= (count line) 5))
      (print line "\n"))))

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
