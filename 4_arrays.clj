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
    (System/exit 1)))
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
;; @_ default variables for iterating over lines of an input file or
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
;; TBD: My use of seq/first/rest might be nonstandard here.  Is it
;; correct for all cases?  If not, what case causes it to break?
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

;; Clojure also has sets as a built-in data structure.  They make it
;; easy to find unique items in a collection.
(let [uniq (set list)]    ; use (vec (set list)) if you want a vector instead
  ;; use uniq here
  )

;;-----------------------------
;; This is nearly the same as functional style above, except this time
;; we want to count occurrences of items.

;; First we'll define a tiny helper function to increment the entry.
;; In Perl, if you increment an undefined entry in a hash, it treats
;; it as a 0 and increments it to 1.  In Clojure, trying to do (inc
;; nil) throws an exception.  What we want is a function that when
;; given nil, returns 1, and when given a number, increments it.
;; We'll call it incn.  Clojure evaluates all values except false and
;; nil as true, when the value is used as the test in an if
;; expression.
(defn incn [x]
  (if x
    (inc x)
    1))

(let [seen (reduce #(assoc %1 %2 (incn (%1 %2)))
                   {} list)
      uniq (vec (keys seen))]   ; leave out vec if a list is good enough
  ;; ...
  )

;; fnil can help us in cases like the above, when we want a function
;; like inc, except it doesn't work when passed nil.  (fnil f
;; default-input) returns a function that works just like f does,
;; except when it is given an argument of nil, it evaluates (f
;; default-input) instead.  So incn above is the same as (fnil inc 0).
(let [seen (reduce #(assoc %1 %2 ((fnil inc 0) (%1 %2)))
                   {} list)
      uniq (vec (keys seen))]   ; leave out vec if a list is good enough
  ;; ...
  )

;; This expression (assoc map key (f (map key))) is so common that
;; there is a function update-in that can shorten it a bit, as
;; (update-in map [key] f).  It can also help update nested maps
;; within maps, but we won't use it for that until later.  This
;; generality is the reason that it takes a vector of key values,
;; instead of only a single key value, and that is why the [] are
;; there around key in the call to update-in.
(let [seen (reduce #(update-in %1 [%2] (fnil inc 0))
                   {} list)
      uniq (vec (keys seen))]   ; leave out vec if a list is good enough
  (printf "seen='%s'\n" seen)
  )

;;-----------------------------
;; Here we call function (some-func item) the first time a new item is
;; encountered in the sequence 'list', but never if it is seen a 2nd
;; or larger time later in the list.  This function is presumably
;; called for its side effects, since there is no return value being
;; used.
(let [seen (reduce #(assoc %1 %2 (if-let [n (%1 %2)]
                                   (inc n)
                                   (do
                                     (some-func %2)
                                     1)))
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
  (reduce #(update-in %1 [%2] (fnil inc 0))
          {} coll))

;; Note that we use the regex #"\s.*$" as opposed to the one #"\s.*\n"
;; in Perl, because the strings in the sequence lines do not have \n
;; at the end of each one.
(let [lines (str/split (:out (shell/sh "who")) #"\n")
      usernames (map #(str/replace-first % #"\s.*$" "") lines)
      ucnt (tally usernames)
      users (sort (keys ucnt))]
  (printf "users logged in: %s\n" (str/join " " users)))

;; If the count of how many times each username occurred is not
;; important, just the unique ones, then Clojure sets are more
;; straightforward.
(let [lines (str/split (:out (shell/sh "who")) #"\n")
      usernames (map #(str/replace-first % #"\s.*$" "") lines)
      users (sort (set usernames))]
  (printf "users logged in: %s\n" (str/join " " users)))
;;-----------------------------

;; @@PLEAC@@_4.7
;;-----------------------------
;; First we'll do it in a similar style to the Perl version.
;; seen is a map, and we produce a vector aonly.
(def A ["a" "b" "c" "d" "c" "b" "a" "e"])
(def B ["b" "c" "d" "c" "b"])
(let [seen (reduce #(assoc %1 %2 1) {} B)
      aonly (vec (filter #(not (seen %)) A))]  ; no vec, if vector not needed
 (printf "%s\n" (str/join " " aonly)))
a a e

;; Then we'll use Clojure sets to simplify it.
(require '[clojure.set :as set])

(let [seen (set B)
      aonly (filter #(not (seen %)) A)]
  (printf "%s\n" (str/join " " aonly)))
a a e

;; We can simplify even further if aonly can be a set of unique
;; elements in A that are not also in B, and the order of the elements
;; does not matter.  Note that the original Perl code contains
;; elements of A not also in B in the same order as they occur in A,
;; and if there are duplicates of such elements in A, they will also
;; be duplicated in aonly.
(let [aonly (set/difference (set A) (set B))]
  (printf "%s\n" (str/join " " aonly)))
a e
;;-----------------------------
;; I can't think of any direct correspondence in Clojure of the Perl
;; techniques used in this code.  The Clojure set examples above are
;; quite concise.
;;-----------------------------
;; This version has a different behavior than the previous one.  It
;; adds an item to @aonly at most one time, without duplicates, but
;; the order is the same as the order the items appear in A.

;; If order does not matter, then the set/difference example above is
;; shorter and clearer.

;; If order in aonly does matter, then here is one way to do it.
(let [aonly (loop [aonly []
                   s (seq A)
                   seen #{}]  ; If we want to use a previously calculated
                              ; set in seen, replace #{} with seen.
              (if-let [item (first s)]
                (if (seen item)
                  (recur aonly (rest s) seen)
                  (recur (conj aonly item) (rest s) (conj seen item)))
                aonly))]
  (printf "%s\n" (str/join " " aonly)))
;;-----------------------------
(let [hash (assoc hash "key1" 1)
      hash (assoc hash "key2" 2)]
  ;; ...
  )
;;-----------------------------
(let [hash (assoc hash "key1" 1 "key2" 2)]
  ;; ...
  )
;;-----------------------------
;; TBD: What does this Perl code do?
;;-----------------------------
;; TBD: What does this Perl code do?
;;-----------------------------

;; @@PLEAC@@_4.8
;;-----------------------------
(def a [1 3 5 6 7 8])
(def b [2 3 5 7 9])
;; All initializations of union, isect, diff will be done in each
;; example below.
;;-----------------------------
;; This time I'll do it using Clojure sets first.
(require '[clojure.set :as set])

(let [set-a (set a)   ; if a is a non-set collection, create one
      set-b (set b)
      union (set/union set-a set-b)
      isect (set/intersection set-a set-b)]
  (printf "union=%s\n" (str/join " " union))
  (printf "isect=%s\n" (str/join " " isect)))
union=1 2 3 5 6 7 8 9
isect=3 5 7

;; Now a way more like the style of the Perl examples.
;;
;; Warning: The Perl code has a bug, if the input array b contains
;; duplicates.  In this case, the duplicate elements in b will become
;; part of isect, even if the elements are not also in a.  The code
;; below emulates this behavior of the Perl examples.  The code using
;; Clojure sets above does not.
;;
;; To see this behavior, try out the code below with these values for
;; a and b:
;;
;; (def a [1 3 5 6 7 8])
;; (def b [2 3 5 7 9 2])  ; 2 is duplicated, and will appear in isect
(let [union (reduce #(assoc %1 %2 1) {} a)
      [union isect] (loop [union union
                           isect {}
                           s (seq b)]
                      (if-let [e (first s)]
                        (recur (assoc union e 1)
                               (if (union e) (assoc isect e 1) isect)
                               (rest s))
                        ;; return a vector of 2 values from the loop
                        ;; expression, which will be bound to union
                        ;; and isect in the outer let expression.
                        [union isect]))
      ;; Note that unlike Perl, the map called union becomes
      ;; inaccessible after the following line.  Give the map and
      ;; sequence different names if you want them both accessible
      ;; later.
      union (keys union)
      isect (keys isect)]
  (printf "union=%s\n" (str/join " " union))
  (printf "isect=%s\n" (str/join " " isect)))
union=9 2 8 7 6 5 3 1
isect=7 5 3
;;-----------------------------
;; The only way I know to write Clojure code that closely emulates
;; this Perl example is to use thread-local mutable variables,
;; introduced using with-local-vars.  These require using var-set to
;; change the value, and var-get to examine the value, which is a bit
;; clunky.  (var-get union) can be abbreviated @union, which helps
;; somewhat.  If you really want to write something in imperative
;; style, with-local-vars may be your best bet.
(let [[union isect]
      (with-local-vars [union {}
                        isect {}]
        (doseq [e (seq (concat a b))]
          ;; The next let statement behaves as Perl's $union{$e}++,
          ;; incrementing $union{$e}, but returning the value of
          ;; $union{$e} before the increment occurs.  This is nil in
          ;; Clojure rather than Perl's undef, but both evaluate to
          ;; false by Clojure 'and' or Perl &&.
          (and (let [in-union (@union e)]
                 (var-set union (update-in @union [e] (fnil inc 0)))
                 in-union)
               (var-set isect (update-in @isect [e] (fnil inc 0)))))
        [@union @isect])
      union (keys union)
      isect (keys isect)]
  (printf "union=%s\n" (str/join " " union))
  (printf "isect=%s\n" (str/join " " isect)))
union=9 2 8 7 6 5 3 1
isect=7 5 3

;; The example using set/union and set/difference is really the best
;; way to go in Clojure, though.
;;-----------------------------
;; First the clojure.set way:
(let [set-a (set a)
      set-b (set b)
      union (set/union set-a set-b)
      isect (set/intersection set-a set-b)
      diff (set/difference union isect)
      ;; Or, if you want to find the 'symmetric difference' without
      ;; explicitly calculation the union and intersection first,
      ;; another way is:
      ;; diff (set/union (set/difference set-a set-b)
      ;;                 (set/difference set-b set-a))
      ]
  (printf "union=%s\n" (str/join " " union))
  (printf "isect=%s\n" (str/join " " isect))
  (printf "diff=%s\n" (str/join " " diff)))
union=1 2 3 5 6 7 8 9
isect=3 5 7
diff=1 2 6 8 9

;; Next a way closer to the Perl code, but without with-local-vars.
;; Function tally copied from an earlier example, repeated for easier
;; reference.
(defn tally [coll]
  (reduce #(update-in %1 [%2] (fnil inc 0))
          {} coll))

(let [count (tally (concat a b))
      [union isect diff]
      (loop [union []
             isect []
             diff []
             s (keys count)]
        (if-let [e (first s)]
          (if (== (count e) 2)
            (recur (conj union e) (conj isect e) diff          (rest s))
            (recur (conj union e) isect          (conj diff e) (rest s)))
          [union isect diff]))]
  (printf "union=%s\n" (str/join " " union))
  (printf "isect=%s\n" (str/join " " isect))
  (printf "diff=%s\n" (str/join " " diff)))
union=9 2 8 7 6 5 3 1
isect=7 5 3
diff=9 2 8 6 1
;;-----------------------------
;; A similar trick as used in the Perl example can be made to work
;; with Clojure local mutable vars, if you really want to do it.
(let [count (tally (concat a b))
      [union isect diff]
      (with-local-vars [union []
                        isect []
                        diff []]
        (doseq [e (keys count)]
          (var-set union (conj (var-get union) e))
          (let [target (if (== (count e) 2) isect diff)]
            (var-set target (conj (var-get target) e))))
        [(var-get union) (var-get isect) (var-get diff)])]
  (printf "union=%s\n" (str/join " " union))
  (printf "isect=%s\n" (str/join " " isect))
  (printf "diff=%s\n" (str/join " " diff)))
union=9 2 8 7 6 5 3 1
isect=7 5 3
diff=9 2 8 6 1
;;-----------------------------

;; @@PLEAC@@_4.9
;;-----------------------------
;; No vec call needed if the result can be a list.
(let [ARRAY1 (vec (concat ARRAY1 ARRAY2))]
  ;; ...
  )
;;-----------------------------
;; I don't know of any Clojure code that looks like that in the Perl
;; example for combining two lists.  The example above using concat
;; will do the job.
;;-----------------------------
(def members ["Time" "Flies"])
(def initiates ["An" "Arrow"])
(let [members (vec (concat members initiates))]
  ;; members is now ["Time" "Flies" "An" "Arrow"]
  )
;;-----------------------------
;; Clojure data structures are immutable, so we can't write a splice
;; function that modifies these data structures, but we can write a
;; split that will return a new data structure that is similar in its
;; value to the Perl one, after splice modifies it.

;; First, we'll write a simpler version that only works with an offset
;; in the range [0, n] where n is the length of the vector, and a
;; non-negative length such that offset+length is also in the range
;; [0,n].
(defn splice [v offset length coll-to-insert]
  (vec (concat (subvec v 0 offset)
               coll-to-insert
               (subvec v (+ offset length)))))

;; Since the example below uses a splice with negative offset, I'll go
;; ahead and give what I think is a full implementation of all cases
;; of positive, 0, or negaitve arguments to Perl's splice (and also
;; substr) for offset and length.  This helper function converts Perl
;; offset and length arguments to Clojure start and end arguments for
;; subvec (and also subs).  It is a bit of a mess because of all of
;; the conditions to check.  There is likely code much like this
;; buried inside of Perl's implementation of subs and splice.

(defn ps-start-end
  ([n offset]
     (cond (neg? offset) [(max 0 (+ n offset)) n]
           (> offset n) nil
           :else [offset n]))
  ([n offset c]
     (let [start (if (neg? offset)
                   (+ n offset)
                   offset)
           end (if (neg? c)
                 (+ n c)
                 (+ start c))]
       (cond (neg? start) (if (neg? end)
                            nil
                            [0 (min n end)])
             (> start n) nil
             :else [start (min n (max start end))]))))

;; Here we implement splice that takes a vector and either just an
;; offset, an offset and a length, or offset, length, and collection
;; of items to splice in.
(defn splice-helper [v start end coll-to-insert]
  (vec (concat (subvec v 0 start)
               coll-to-insert
               (subvec v end))))

(defn splice
  ([v offset]
     (when-let [[start end] (ps-start-end (count v) offset)]
       (splice-helper v start end [])))
  ([v offset length]
     (splice v offset length []))
  ([v offset length coll-to-insert]
     (when-let [[start end] (ps-start-end (count v) offset length)]
       (splice-helper v start end coll-to-insert))))


(let [members (splice members 2 0 (cons "Like" (seq initiates)))
      _ (printf "%s\n" (str/join " " members))
      members (splice members 0 1 ["Fruit"])
      members (splice members -2 2 ["A" "Banana"])
      ]
  (printf "%s\n" (str/join " " members)))
;;-----------------------------
Time Flies Like An Arrow
Fruit Flies Like A Banana
;;-----------------------------

;; @@PLEAC@@_4.10
;;-----------------------------
(def reversed (vec (reverse array)))  ; remove vec call if sequence result OK
;;-----------------------------
(loop [i (dec (count ARRAY))]
  (when (>= i 0)
    ;; do something with (ARRAY i)
    (recur (dec i))))

;; alternate version
(doseq [i (range (dec (count ARRAY)) -1 -1)]
  ;; do something with (ARRAY i)
  )
;;-----------------------------
(def ascending (sort users))

;; If you want to make the comparison function explicit, you can use
;; this, which is equivalent to the above.
(def ascending (sort #(compare %1 %2) users))
(def descending (reverse ascending))

;; one-step: sort with reverse comparison
(def descending (sort #(compare %2 %1) users))
;;-----------------------------

;; @@PLEAC@@_4.11
;;-----------------------------
;; The Perl code @FRONT = splice(@ARRAY, 0, $N); has the side effect
;; of modifying @ARRAY, removing the first $N elements from it, and
;; simultaneously assigning an array of those first $N elements to
;; @FRONT.

;; To get a similar effect in Clojure can be done as follows:
(let [FRONT (subvec array 0 n)
      array (subvec array n)]
  ;; ...
  )

;; The Perl code @END = splice(@ARRAY, -$N); also has two side
;; effects: removing the last $N elements from @ARRAY, and assigning
;; an array containing those last $N elements to @END.
(let [END (subvec array (- (count array) n))
      array (subvec array 0 (- (count array) n))]
  ;; ...
  )

;; or if you want to use the Clojure splice function defined earlier:
(def array [1 2 3 4 5 6 7 8 9])
(def n 4)
(let [END (splice array 0 (- n))
      array (splice array (- n))]
  (printf "array=%s   END=%s\n" (str/join " " array) (str/join " " END))
  ;; ...
  )
;;-----------------------------
;; It is not really possible to write a Clojure function that mutates
;; an immutable data structure, like Perl's shift2 and pop2 do.

;; We could write a function that takes a vector v and return a vector
;; of the two things: (1) a vector of the first two elements of v, and
;; (2) a vector of all but the first two elements of v.  The caller of
;; that function could then choose to use one or both of those values
;; and bind them to symbols of its choice.
(defn shift2 [v]
  [(subvec v 0 2) (subvec v 2)])

(defn pop2 [v]
  (let [i (- (count v) 2)]
    [(subvec v i) (subvec v 0 i)]))
;;-----------------------------
;; Clojure qw defined far above.  Reuse it here.
(def friends (vec (qw "Peter Paul Mary Jim Tim")))

(let [[[this that] friends] (shift2 friends)]
  ;; this contains "Peter", that has "Paul", and friends has "Mary",
  ;; "Jim", and "Tim".
  (printf "this=%s  that=%s  friends=%s\n" this that (str/join " " friends)))

(def beverages (vec (qw "Dew Jolt Cola Sprite Fresca")))
(let [[pair beverages] (pop2 beverages)]
  ;; (pair 0) contains Sprite, (pair 1) has Fresca, and beverages has
  ;; ["Dew" "Jolt" "Cola"]
  (printf "(pair 0)=%s  (pair 1)=%s  beverages=%s\n"
          (pair 0) (pair 1) (str/join " " beverages)))
;;-----------------------------
;; Clojure doesn't have references exactly like Perl's.  It does have
;; something called refs, but in Clojure they are intended primarily
;; for handling coordinated concurrent changes to multiple refs with
;; transactions.
;;
;; TBD: It isn't clear to me whether Clojure has a way to implement
;; the behavior in this Perl code.
;;
;; $line[5] = \@list;
;; @got = pop2( @{ $line[5] } );
;;-----------------------------

;; @@PLEAC@@_4.12
;;-----------------------------
;; If we know that the matching item cannot possibly have a value that
;; Clojure would evaluate as false, i.e. false or nil, then we can
;; write the following.  Note that filter is lazy, so since we only
;; ask for the first element, the rest of the elements after the
;; first, if any, will not be computed.  (Exception: If array is a
;; chunked sequence, the predicate function will be evaluated on all
;; elements of array in the same chunk as the first one that returns a
;; logical true value.)
(def array [1 3 5 7 9 11 12 13 15])
(def array [1 3 5 7 9 11 13 15])
(if-let [match (first (filter even? array))]
  (printf "Found matching item %s\n" match)
  (printf "No matching item found\n"))

;; Be careful!  That code will do the wrong thing if the value false
;; or nil is in the array, and the criterion we are checking for is
;; true for such a value.  For example:
(def array [1 3 5 7 9 11 nil 13 15])
(if-let [match (first (filter nil? array))]
  (printf "Found matching item %s\n" match)
  (printf "No matching item found\n"))
No matching item found

;; In this case, nil? returned true for the value nil in array, so
;; filter did include nil in its output sequence.  However, this
;; causes first to return the first element of the sequence, nil.  if
;; and if-let treat nil as false, and thus does the else case.

;; If we want to avoid that possibility, we can use the function some,
;; and have a predicate function that returns something besides
;; nil/false in the matching case.  One way to do that is to bundle up
;; the matching value in a vector, because if and if-let treat [nil]
;; and [false] as true.  After all, they are not the same as the
;; values nil or false.
(def array [1 3 5 7 9 11 nil 13 15])
(if-let [[match] (some #(if (nil? %) [%]) array)]
  (printf "Found matching item %s\n" match)
  (printf "No matching item found\n"))
Found matching item null
;;-----------------------------
;; The previous examples will work, of course.  If you want the index
;; of the matching element, though, they won't do.  We could do it
;; with loop.  Note that this works with vectors, but not with other
;; collections, whereas earlier examples work with all kinds of
;; collections.
(let [match-idx (loop [i 0]
                  (if (< i (count array))
                    (if (even? (array i))
                      i
                      (recur (inc i)))
                    nil))]
  (if match-idx
    (printf "Found matching item %s\n" (array match-idx))
    (printf "No matching item found\n")))
;;-----------------------------
;; TBD: Read more about what this example is intended to do.  Should
;; Clojure example create a new class?  Seems like overkill.
;;-----------------------------
;; Clojure loops like loop, dotimes, doseq all bind symbols locally,
;; i.e. within their body, only.  They do not have any accessible
;; value after the body of the loop is complete.  If you really want
;; to do it in the way the Perl code is written, it seems you must
;; write imperative style code, like this.
(with-local-vars [i 0]
  (loop []
    (if (< @i (count array))
      (if (even? (array @i))
        true   ; stop the loop
        (do (var-set i (inc @i))
            (recur)))))
  (if (< @i (count array))
    (printf "Found matching item %s\n" (array @i))
    (printf "No matching item found\n")))

;; Another way is almost exactly like one of the examples above, where
;; you return the loop index as the value of the loop expression.
(let [i (loop [i 0]
          (if (< i (count array))
            (if (even? (array i))
              i
              (recur (inc i)))
            i))]
  (if (< i (count array))
    (printf "Found matching item %s\n" (array i))
    (printf "No matching item found\n")))
;;-----------------------------

;; @@PLEAC@@_4.13
;;-----------------------------
(def matching (filter #(test %) collection))
;;-----------------------------
;; You can write Clojure code that works much like this Perl code, but
;; filter is shorter to write in Clojure, much like grep is in Perl.
(let [matching (loop [matching []
                      s collection]
                 (if-let [s (seq s)]
                   (if (test (first s))
                     (recur (conj matching (first s)) (next s))
                     (recur matching (next s)))
                   matching))]
  (printf "matching=%s\n" (str/join " " matching)))
;;-----------------------------
(def nums [5 1000000 1000001 -2])
(def bigs (filter #(> % 1000000) nums))
(def bigs (filter #(> (users %) 10000000) (keys users)))
;;-----------------------------
(def matching (filter #(re-find #"^gnat " %)
                      (str/split (:out (shell/sh "who")) #"\n")))
;;-----------------------------
;; Just calling function position on elements of employees, not
;; treating them as objects.
(def engineers (filter #(= (position %) "Engineer") employees))
;;-----------------------------
(def secondary-assistance (filter #(and (>= (income %) 26000)
                                        (< (income %) 30000))
                                  applicants))
;;-----------------------------

;; @@PLEAC@@_4.14
;;-----------------------------
(def sorted (sort unsorted))
;; Or if you want to do an explicit comparison function:
(def sorted (sort #(compare. %1 %2) unsorted))
;; Note that Clojure does not have the distinction between Perl's <=>
;; for comparing scalars as numbers vs. cmp for comparing scalars as
;; strings, because Clojure does not auto-convert value between types
;; the way Perl does.
;;-----------------------------
(require '[clojure.java.shell :as shell])

(doseq [pid (sort pids)]
  (printf "%d\n" pid))
(printf "Select a process ID to kill:\n")
(flush)  ; println does an automatic flush, but printf does not
(let [pid (read-line)]
  ;; Note that re-matches only returns true if the whole string
  ;; matches the regexp.  It is the same as (re-find #"^\d+$" pid).
  (when (not (re-matches #"\d+" pid))
    (printf "Exiting...\n")  ; prints to *out*, which is likely not stderr
    (flush)
    (System/exit 1))
  ;; TBD: Is there a 'platform-independent' API for killing a process
  ;; available from Clojure or Java?  If so, use it here.  The
  ;; following depends upon a process named "kill" being available on
  ;; the system, so probably won't work on Windows, whereas Perl's
  ;; subroutine kill would.
  (shell/sh "kill" "-TERM" (str pid))
  ;; TBD: Similar question for sleep.  I'm almost sure Java must have
  ;; something here.
  (shell/sh "sleep" "2")
  (shell/sh "kill" "-KILL" (str pid)))
(System/exit 0)
;;-----------------------------
(def descending (sort #(compare. %2 %1) unsorted))
;;-----------------------------
;; TBD: Put sort function in separate Clojure namespace
;;-----------------------------
(def all (sort #(compare. %2 %1) [4 19 8 3]))
;;-----------------------------
