;; @@PLEAC@@_4.0 Arrays

;;-----------------------------
;; vectors
(def simple ["this" "that" "the" "other"])
(def nested ["this" "that" ["the" "other"]])

(assert (= (count simple) 4))
(assert (= (count nested) 3))
(assert (= (count (nth nested 2)) 2))

;;-----------------------------
(def tune ["The" "Star-Spangled" "Banner"])
;;-----------------------------

;; @@PLEAC@@_4.1
(def a ["quick" "brown" "fox"])
(defn qw
  "Split string on whitespace. Returns a seq."
  [s] (seq (.split s "\\s")))
(def a2 (qw "Why are you teasing me?"))
(def lines
  (.replaceAll "    The boy stood on the burning deck,
    It was as hot as glass."
               "\\ +" ""))
;;-----------------------------
(ns bigvector
  (:require [clojure.string :as str]
            [clojure.java.io :as io]))

(try
  (let [bigvector (vec (line-seq (io/reader "mydatafile")))]
    ;; rest of code to do something with bigvector
    )
  (catch java.io.FileNotFoundException e
    (printf "%s\n" e)
    (flush)
    (. System (exit 1))))
;;-----------------------------


;; @@PLEAC@@_4.2
;;-----------------------------
(defn commify-series [coll]
  (case (count coll)
        0 ""
        1 (first coll)
        2 (str/join " and " coll)
        (str/join ", " (concat (butlast coll)
                               (list (str "and " (last coll)))))))
;;-----------------------------
(def array ["red" "yellow" "green"])
(print "I have" array "marbles.\n")
;; Clojure does not have string interpolation.
(printf "I have %s marbles.\n" (str/join " " array))
I have [red yellow green] marbles.

I have red yellow green marbles.
;;-----------------------------

;; @@PLEAC@@_4.3
;;-----------------------------
;; Clojure vectors cannot be modified, but we can create new vectors
;; from existing ones, with differences between the existing and new
;; ones.

;; create smaller array that is a subset of an existing one.  Unlike
;; Perl's $#ARRAY = $NEW_LAST_ELEMENT_INDEX_NUMBER, you must use the
;; new number of elements with subvec, which is one larger than the
;; new last element index number.
(def newv (subvec v 0 newv-number-of-elements))
;; In general you can give an arbitrary start (inclusive) and end
;; (exclusive) index to subvec.  It only takes O(1) time.  The new
;; vector's index i has the same value as the original vector's index
;; (start+i).
;;-----------------------------
;; We can create a new Clojure vector one larger in size than an
;; existing one using assoc or conj.
(def newv (assoc v (count v) value))
(def newv (conj v value))
;; I believe there is no way to expand a vector by an arbitrary amount
;; using a single Clojure built-in function.  One could achieve that
;; effect by repeatedly using conj to add individual elements to the
;; end, one at a time, until the desired vector size was reached.
;; However, if you want a sparsely populated array with elements
;; indexed by integer, you are likely to be more satisfied using a map
;; with integer keys than a vector.
;;-----------------------------
(defn what-about-that-vector [v]
  (printf "The vector now has %d elements.\n" (count v))
  (printf "The index of the last element is %d.\n" (dec (count v)))
  (printf "Element #3 is `%s'.\n" (v 3)))
;; Note that qw returns a sequence of elements that is not a Clojure
;; vector.  Here we use vec to create a vector containing the same
;; elements as the sequence.
(def people (vec (qw "Crosby Stills Nash Young")))
(what-about-that-vector people)
;;-----------------------------
The vector now has 4 elements.
The index of the last element is 3.
Element #3 is `Young'.
;;-----------------------------
(def people (pop people))
;; The following has equivalent behavior for vectors to pop, but not
;; sure if the efficiency is the same.
;;(def people (subvec people 0 (dec (count people))))
(what-about-that-vector people)
;;-----------------------------
IndexOutOfBoundsException   clojure.lang.PersistentVector.arrayFor (PersistentVector.java:106)
The vector now has 3 elements.
The index of the last element is 2.
;;-----------------------------
;; As mentioned above, there is no single builtin function to extend a
;; vector by an arbitrarily large number of elements.  We'll do it
;; here with a loop.
(def people
     (loop [people people]
       (if (< (count people) 10001)
         (recur (conj people nil))
         ;; else
         people)))
(what-about-that-vector people)
;;-----------------------------
The vector now has 10001 elements.
The index of the last element is 10000.
Element #3 is `null'.
;;-----------------------------
;; Assigning a value to element 10000 of vector people will not change
;; its size, even if that new value is nil.  To make a vector with a
;; smaller size, use subvec or pop as described above.

;; @@PLEAC@@_4.4
;;-----------------------------

;; Clojure is often written in a functional style, meaning that you
;; calculate output value from input values.  So Clojure's 'for' is
;; actually a way to take one or more input sequences and produce an
;; output sequence, and in fact this is done in a lazy fashion,
;; meaning that no actual computation occurs unless some other code
;; _uses_ elements of the output sequence.

;; If you use Clojure's REPL to try out code before using it in a
;; program, this can easily confuse you, because at the REPL, every
;; expression you enter is read, executed, and the result is printed.
;; The fact that the result is printed often forces lazy expressions
;; to calculate their entire result, but if you use that lazy
;; expression as part of a larger expression or program, it won't be.

;; Here are a couple of quick examples:

user=> (range 0 5)
(0 1 2 3 4)

;; i iterates over the elements of the sequence (range 0 5).  The for
;; expression as a whole returns a sequence containing (inc i) for
;; each input sequence element.  It does this lazily, but because we
;; are typing it at the REPL, the output value is used in order to
;; print it.
user=> (for [i (range 0 5)] (inc i))
(1 2 3 4 5)

;; Here we add some debug print statements, and its output gets
;; mingled with the printed output value.
user=> (for [i (range 0 5)] (do (printf "i=%d\n" i) (inc i)))
(i=0
i=1
i=2
i=3
i=4
1 2 3 4 5)
;; Here we assign the value of the for expression to a var.  Note that
;; the only output printed is the output value of the def statement,
;; which is #'user/a1.  Why don't the printf's get executed?  Because
;; the for is lazy, and nothing has used any part of its output value
;; yet.
user=> (def a1 (for [i (range 0 5)] (do (printf "i=%d\n" i) (inc i))))
#'user/a1

;; When we ask for the value of a1, then the output value of the for
;; expression is required, and so its body is executed now.
user=> a1
(i=0
i=1
i=2
i=3
i=4
1 2 3 4 5)

;; If you want to force the iteration to occur when it is evaluated,
;; use doseq instead.  It does not return any useful value (only nil),
;; and is intended to be used when the body contains side effects.
;; Here the (inc i) is superfluous, since it simply returns a value
;; that is ignored by the rest of the expression around it.
user=> (def a1 (doseq [i (range 0 5)] (printf "i=%d\n" i) (inc i)))
i=0
i=1
i=2
i=3
i=4
#'user/a1

;; As mentioned above, doseq always returns nil.
user=> a1
nil
;;-----------------------------
(doseq [user bad-users]
  (comlain user))
;;-----------------------------
(doseq [var (sort (keys (System/getenv)))]
  (printf "%s=%s\n" var (get (System/getenv) var)))
;;-----------------------------
(doseq [user all-users]
  (let [disk-space (get-usage user)]
    (if (> disk-space MAX-QUOTA)
      (complain user))))
;;-----------------------------
(require '[clojure.java.shell :as shell])

(doseq [line (str/split (:out (shell/sh "who")) #"\n")]
  (if (re-find #"tchrist" line)
    (printf "%s\n" line)))
;;-----------------------------
;; Unlike in Perl, there is nothing in Clojure similar to the $_ and
;; @_ default variables for iteratin over lines of an input file or
;; elements of a list.

;; rdr implements interface java.io.BufferedReader in this example,
;; and so can be used with line-seq.
(doseq [line (line-seq rdr)]
  ;; line-seq is a sequence of strings, one for each line in the input
  ;; file, and they never have a trailing \n
  (doseq [word (str/split line #"\s+")]
    (printf "%s" (str/reverse word))))
;;-----------------------------
;; In Clojure, every for or doseq has variables that are like Perl's
;; "my", i.e. their scope is local to the body of the loop, and any
;; value the symbol had outside the loop is not visible inside, and
;; any change made inside has no affect on the symbol's value outside
;; the loop.
(doseq [item array]
  (printf "i = %s\n" item))
;;-----------------------------
;; Clojure's native vectors are immutable, so there is no way to
;; modify their elements, although it is easy to create new vectors
;; that are the same as old ones except that a single element has been
;; replaced with a new one.

;; This is a clunky way to do it that loops over the elements of the
;; array explicitly.  Note that we first bind the symbol array to the
;; value [1 2 3], then to the value returned by the loop expression.
;; The first value of array is then lost.
(let [array [1 2 3]
      array (loop [a array
                   i 0]
              (if (< i (count a))
                (recur (assoc a i (dec (a i))) (inc i))
                a))]
  (println array))
[0 1 2]

;; A much more functional style for doing this is to use map.  Here we
;; use vec on the result of map to convert the list that is returned
;; by map, which is different than a vector in Clojure, to a vector
;; with the same elements.
(let [array [1 2 3]
      array (vec (map dec array))]
  (println array))

;; Again, this example achieves a similar effect as the Perl code, but
;; it does not modify a and b in place -- it creates new values and
;; assigns those new values to a and b.
(let [a [0.5 3]
      b [0 1]
      a (vec (map #(* % 7) a))
      b (vec (map #(* % 7) b))]
  (printf "%s %s\n" a b))
[3.5 21] [0 7]

;; Note that you can use native Java arrays in a straightforward way
;; from Clojure, and these are mutable data structures, like Java's
;; and Perl's mutable arrays.  Java arrays cannot be grown or shrunk
;; after creation, except by copying their contents into a new array
;; and abandoning the old one.

;; into-array creates a Java array with values initialized to the
;; elements of a sequence.  If you don't give an explicit type for the
;; array elements, they default to the type of the first element of
;; the sequence.  I'll create Java arrays of java.lang.Object's below,
;; so that the elements can be a mix of different subclasses of
;; Object.

;; You can use loop, doseq, or dotimes to iterate over the indices of
;; the array.

(let [a (into-array Object [0.5 3])
      b (into-array Object [0 1])]
  (dotimes [i (alength a)]
    (aset a i (* (aget a i) 7)))
  (dotimes [i (alength b)]
    (aset b i (* (aget b i) 7)))
  (printf "%s %s\n" (seq a) (seq b)))  ; seq used to create sequence
                                       ; of values in arrays a and b
(3.5 21) (0 7)

;; Clojure's amap creates new Java arrays, by copying the given one,
;; then iterating over its elements and replacing each one with the
;; result of evaluating a given expression.

(let [a (into-array Object [0.5 3])
      b (into-array Object [0 1])
      a (amap a i temp (* (aget a i) 7))
      b (amap b i temp (* (aget b i) 7))]
  (printf "%s %s\n" (seq a) (seq b)))
(3.5 21) (0 7)
;;-----------------------------

;; Because Clojure doesn't provide a way to modify its collections in
;; place, but instead encourages you to create new versions of
;; existing data structures, it doesn't really provide a way to
;; iterate over several different collections in a single loop.

;; clojure.string/trim returns a string the same as the string you
;; give it, except with white space at the beginning and end removed.

;; do-to-map was written by Brian Carper, and he also wrote the
;; explanation for how it works that I have copied below.  Original
;; source is at the following URL:

;; http://stackoverflow.com/questions/1638854/clojure-how-do-i-apply-a-function-to-a-subset-of-the-entries-in-a-hash-map

;; It helps to look at it inside-out.  In Clojure, hash-maps act like
;; functions; if you call them like a function with a key as an
;; argument, the value associated with that key is returned.  So given
;; a single key, the current value for that key can be obtained via:

;; (some-map some-key)

;; We want to take old values, and change them to new values by
;; calling some function f on them.  So given a single key, the new
;; value will be:

;; (f (some-map some-key))

;; We want to associate this new value with this key in our hash-map,
;; "replacing" the old value.  This is what assoc does:

;; (assoc some-map some-key (f (some-map some-key)))

;; ("Replace" is in scare-quotes because we're not mutating a single
;; hash-map object; we're returning new, immutable, altered hash-map
;; objects each time we call assoc.  This is still fast and efficient
;; in Clojure because hash-maps are persistent and share structure
;; when you assoc them.)

;; We need to repeatedly assoc new values onto our map, one key at a
;; time.  So we need some kind of looping construct.  What we want is
;; to start with our original hash-map and a single key, and then
;; "update" the value for that key.  Then we take that new hash-map
;; and the next key, and "update" the value for that next key.  And we
;; repeat this for every key, one at a time, and finally return the
;; hash-map we've "accumulated".  This is what reduce does.

;; * The first argument to reduce is a function that takes two
;;   arguments: an "accumulator" value, which is the value we keep
;;   "updating" over and over; and a single argument used in one
;;   iteration to do some of the accumulating.
;; * The second argument to reduce is the initial value passed as the
;;   first argument to this fn.
;; * The third argument to reduce is a collection of arguments to be
;;   passed as the second argument to this fn, one at a time.

;; So:

;; (reduce fn-to-update-values-in-our-map 
;;         initial-value-of-our-map 
;;         collection-of-keys)

;; fn-to-update-values-in-our-map is just the assoc statement from
;; above, wrapped in an anonymous function:

;; (fn [map-so-far some-key]
;;   (assoc map-so-far some-key (f (map-so-far some-key))))

;; So plugging it into reduce:

;; (reduce (fn [map-so-far some-key]
;;           (assoc map-so-far some-key (f (map-so-far some-key))))
;;         amap
;;         keyseq)

;; In Clojure, there's a shorthand for writing anonymous functions:
;; #(...) is an anonymous fn consisting of a single form, in which %1
;; is bound to the first argument to the anonymous function, %2 to the
;; second, etc.  So our fn from above can be written equivalently as:

;; #(assoc %1 %2 (f (%1 %2)))

;; This gives us:

;; (reduce #(assoc %1 %2 (f (%1 %2))) amap keyseq)

(defn do-to-map [amap keyseq f]
  (reduce #(assoc %1 %2 (f (%1 %2))) amap keyseq))

(let [scalar (str/trim scalar)
      array (vec (map str/trim array)) ; skip the vec if a list result is OK
      hash (do-to-map hash (keys hash) str/trim)]
  )

;;-----------------------------
;; No foreach/for synonym in Clojure.  I believe the existing common
;; alternatives are all mentioned above.
;;-----------------------------

;; @@PLEAC@@_4.5
;;-----------------------------
;; Clojure does not have Perl's distinction between an array and an
;; array ref.  Clojure lists, vectors, maps, etc. can all contain
;; instances of each other as values, and in maps any of these data
;; structures can be used as keys, too.
;;-----------------------------
(def fruits [ "Apple" "Blackberry" ])
(doseq [fruit fruits]
  (printf "%s tastes good in a pie.\n" fruit))
Apple tastes good in a pie.
Blackberry tastes good in a pie.
;;-----------------------------
(dotimes [i (count fruits)]
  (printf "%s tastes good in a pie.\n" (fruits i)))
;;-----------------------------
(def namelist { })
(def rogue-cats [ "YellowFang" "BrokenTail" "Clawface" ])
(let [namelist (assoc namelist :felines rogue-cats)]
  (doseq [cat (namelist :felines)]   ; (:felines namelist) gives same result
    (printf "%s purrs hypnotically..\n" cat)))
(printf "--More--\nYou are controlled.\n")
;;-----------------------------
(let [namelist (assoc namelist :felines rogue-cats)]
  (dotimes [i (count (namelist :felines))]
    (printf "%s purrs hypnotically..\n" ((namelist :felines) i))))
;;-----------------------------

;; @@PLEAC@@_4.6
;;-----------------------------
;; Iterative style -- requires a fair amount of verbiage.
(loop [seen {}
       uniq []
       l (seq list)]
  (if-let [item (first l)]
    (if (not (seen item))   ;; (seen item) is nil if item is not a key
                            ;; in the map seen, and nil is treated as false
      (recur (assoc seen item 1)
             (conj uniq item)
             (rest l))
      ;; else
      (recur seen uniq (rest l)))
    ;; return a final value from loop statement here, perhaps seen and
    ;; uniq
    ))
  
;; Functional style.  If reduce call is confusing, try first reading
;; explanation of do-to-map above.  This is a bit simpler than that.
(let [seen (reduce #(assoc %1 %2 1) {} list)
      uniq (vec (keys seen))]   ; leave out vec if a list is good enough
  ;; use seen and/or uniq here
  )
;;-----------------------------
;; This is nearly the same as functional style above, except this time
;; we want to count occurrences of items.
(let [seen (reduce #(assoc %1 %2 (if-let [n (%1 %2)] (inc n) 1)) {} list)
      uniq (vec (keys seen))]   ; leave out vec if a list is good enough
  ;; ...
  )

;; Note that while the following is simpler, it crashes because it
;; tries to do (inc nil) when it finds that (seen
;; "first-list-element") is nil.
(let [seen (reduce #(assoc %1 %2 (inc (%1 %2))) {} list)
      uniq (vec (keys seen))]   ; leave out vec if a list is good enough
  ;; ...
  )

;; fnil can make the first version a bit simpler.  As used here, (fnil
;; inc 0), it says "do an inc, but if the argument is nil, replace it
;; with a 0 before doing the inc".
(let [seen (reduce #(assoc %1 %2 ((fnil inc 0) (%1 %2))) {} list)
      uniq (vec (keys seen))]   ; leave out vec if a list is good enough
  ;; ...
  )
;;-----------------------------
;; Here we call function (some-func item) the first time a new item is
;; encountered in the sequence 'list', but never if it is seen a 2nd
;; or larger time later in the list.  This function is presumably
;; called for its side effects, since there is no return value being
;; used.
(let [seen (reduce #(assoc %1 %2 (if-let [n (%1 %2)]
                                   (inc n)
                                   (do (some-func %2) 1)))
                   {} list)]
  ;; ...
  )
;;-----------------------------
;; Here the Perl version is closer to the functional style examples
;; given above.  No reason to repeat the Clojure code for them here.
;;-----------------------------
;; The Perl code here is very much like a functional style, except its
;; condition mutates the hash 'seen'.  I'm not going to try to write a
;; Clojure version that emulates this, since to match its behavior
;; closely would require using a mutable Java hash table.

;; %seen = ();
;; @uniqu = grep { ! $seen{$_} ++ } @list;
;;-----------------------------
;; Here is a functional style version of the Perl code.  Let's make a
;; function 'tally' to create a map of occurrence counts of items in a
;; collection.
(require '[clojure.string :as str])
(require '[clojure.java.shell :as shell])

(defn tally [coll]
  (reduce #(assoc %1 %2 ((fnil inc 0) (%1 %2)))
          {} coll))

(let [lines (str/split (:out (shell/sh "who")) #"\n")
      usernames (map #(str/replace-first % #"\s.*$" "") lines)
      ucnt (tally usernames)
      users (sort (keys ucnt))]
  (printf "users logged in: %s\n" (str/join " "users)))
;;-----------------------------
