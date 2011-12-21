;; @@PLEAC@@_4.0 Introduction

;;-----------------------------
;; Vectors.  Unlike Perl, the effect of these two lines is not
;; equivalent.  The first creates a vector of 4 items, all strings.
;; The second creates a vector of 3 items, where the first 2 are
;; strings, and the 3rd is a vector containing 2 strings.
(def simple ["this" "that" "the" "other"])
(def nested ["this" "that" ["the" "other"]])

(assert (= (count simple) 4))
(assert (= (count nested) 3))
(assert (= (count (nth nested 2)) 2))

;;-----------------------------
(def tune ["The" "Star-Spangled" "Banner"])
;;-----------------------------

;; @@PLEAC@@_4.1 Specifying a List in Your Program
;;-----------------------------
(def a ["quick" "brown" "fox"])
;;-----------------------------
;; almost-qw is close to Perl's qw, but it treats leading whitespace
;; differently.
(defn almost-qw [s]
  (str/split s #"\s+"))

(almost-qw "  Leading whitespace behaves differently than Perl's qw  ")
;; ["" "Leading" "whitespace" "behaves" "differently" "than" "Perl's" "qw"]

;; perl-split-on-space was introduced in Section 1.6
(defn perl-split-on-space [s]
  (str/split (str/triml s) #"\s+"))

(defn qw
  "Split string on whitespace. Returns a seq."
  [s] (perl-split-on-space s))

(def a2 (qw "Why are you teasing me?"))
;;-----------------------------
;; The 'map second' part is because each element returned by the
;; re-seq is a vector with 2 elements: the first is the string that
;; matched the entire pattern, the second is the string that matched
;; the parenthesized group (.+).  (?m) at the beginning of the pattern
;; allows ^ to match the beginning of a line inside the string, just
;; after a newline, like Perl's /m at the end of a pattern.
(def lines (map second (re-seq #"(?m)\s*(.+)"
"    The boy stood on the burning deck,
    It was as hot as glass.
")))
;;-----------------------------
(ns bigvector
  (:require [clojure.string :as str]
            [clojure.java.io :as io]))

(try
  (with-open [rdr (io/reader "mydatafile")]
    (let [bigvector (vec (line-seq rdr))]
      ;; rest of code to do something with bigvector
      ))
  (catch java.io.FileNotFoundException e
    (printf "%s\n" e)
    (flush)
    (System/exit 1)))
;;-----------------------------
;; There is no distinction in Clojure similar to Perl's single-quoting
;; and double-quoting of strings, because there is no string
;; interpolation in Clojure.
(def banner "The Mines of Moria")
;;-----------------------------
;; Probably the most similar thing in Clojure to Perl's string
;; interpolation is using format, which is like sprintf in Perl.
(def name "Gandalf")
(def banner (format "Speak, %s, and enter!" name))
;;-----------------------------
;; Note that Clojure's clojure.java.shell/sh takes as arguments a
;; separate string for each 'word' on the command line, not a single
;; string that is then parsed to separate it into words.
(require '[clojure.java.shell :as shell])
(def his-host "www.perl.com")
(def host-info (:out (shell/sh "nslookup" his-host)))

;; I haven't found a simpler way to get the JVM's process ID.  Here
;; are two web pages describing several possibilities:
;; http://blog.igorminar.com/2007/03/how-java-application-can-discover-its.html
;; http://stackoverflow.com/questions/35842/process-id-in-java
(defn getpid []
  (let [pid-at-host-str
        (. (java.lang.management.ManagementFactory/getRuntimeMXBean) getName)]
    (if-let [match (re-find #"^(\d+)@" pid-at-host-str)]
      (read-string (second match)))))

(def clojure-info (:out (shell/sh "ps" (str (getpid)))))
;; If you want something equivalent to the following in Perl:

;; $shell_info = qx'ps $$';        # that's the new shell's $$

;; then the following is a good first try, but it does not work as
;; above, because it does not start a shell process that can interpret
;; and replace the "$$".  Instead it directly starts the process ps
;; without invoking a shell process first, so the "ps" command sees
;; the first argument as the string "$$", which it most likely prints
;; an error message about.
(def shell-info (:out (shell/sh "ps" "$$")))

;; This is closer, but it hard-codes the use of bash as the shell.
;; Note that in this case bash expects the entire command to parse as
;; a single string.  That is why "ps $$" is all one string.
(def shell-info (:out (shell/sh "bash" "-c" "ps $$")))

;; If you do this frequently, and you want to use the value of the
;; SHELL environment variable to decide which shell to run, defaulting
;; to bash only if SHELL is not defined, a function like this is
;; recommended:
(defn shell-run [cmd]
  (let [shell (or (get (System/getenv) "SHELL") "bash")]
    (:out (shell/sh shell "-c" cmd))))

(def shell-info (shell-run "ps $$"))

;; Note that at least on my Mac, both the Perl and Clojure versions
;; above result in a string the one below, showing the ps command
;; instead of bash.  This is because the bash process is doing an exec
;; call to replace itself with the ps process, having the same process
;; ID.

;;  PID   TT  STAT      TIME COMMAND
;; 3606 s002  R+     0:00.01 ps 3606
;;-----------------------------
(def banner ["Costs" "only" "$4.95"])
(def banner (qw "Costs only $4.95"))
;; Note the use of perl-split-on-space here instead of Clojure's
;; str/split.
(def banner (perl-split-on-space "Costs only $4.95"))
;;-----------------------------
;; Clojure's strings can be multiline, but they can only be delimited
;; with "double quote characters", not a large variety of characters
;; as Perl allows.  You must escape double-quote characters that you
;; want to be included as part of the string.
(def brax   (qw " ( ) < > { } [ ] "))
(def rings  (qw "Nenya Narya Vilya"))
(def tags   (qw "LI TABLE TR TD A IMG H1 P"))
(def sample (qw "The vertical bar (|) looks and behaves like a pipe."))
;;-----------------------------
;; As mentioned above, Clojure cannot use anything except double-quote
;; characters to delimit strings.
(def banner (qw "The vertical bar (|) looks and behaves like a pipe."))
;;-----------------------------
(def ships (qw "Niña Pinta Santa María"))   ; WRONG
(def ships ["Niña" "Pinta" "Santa María"])  ; right
;;-----------------------------


;; @@PLEAC@@_4.2 Printing a List with Commas
;;-----------------------------
;; Note that to use concat to append a single item to the end of a
;; sequence, we have to put that single item into its own 1-item
;; sequence, using (list item), (vec item), etc.
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
;; @@INCLUDE@@ include/clojure/ch04/commify_series.clj
;;-----------------------------

;; @@PLEAC@@_4.3 Changing Array Size
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

;; @@PLEAC@@_4.4 Doing Something with Every Element in a List
;;-----------------------------

;; Clojure is often written in a functional style, meaning that you
;; calculate an output value from input values.  So Clojure's 'for' is
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

;; If you want to force the iteration of 'for' to occur when it is
;; evaluated, you can wrap it, or any other expression that returns a
;; lazy result, in a call to doall, which forces the entire sequence
;; to be evaluated.
user=> (def a1 (doall (for [i (range 0 5)] (do (printf "i=%d\n" i) (inc i)))))
i=0
i=1
i=2
i=3
i=4
#'user/a1

;; Since the for has already been evaluated, it is not evaluated again
;; when we ask to show the value of a1 this time.
user=> a1
(1 2 3 4 5)

;; Another way to iterate similar to for, and force the iteration to
;; occur when the expression is evaluated, is to use doseq.  It does
;; not return any useful value (only nil), and is intended to be used
;; when the body contains side effects.  Here the (inc i) is
;; superfluous, since it simply returns a value that is ignored by the
;; rest of the expression around it.
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
  (complain user))
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
;; the loop (if the symbol had a value before the loop was
;; encountered).
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

;; @@PLEAC@@_4.5 Iterating Over an Array by Reference 
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

;; @@PLEAC@@_4.6 Extracting Unique Elements from a List
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

;; @@PLEAC@@_4.7 Finding Elements in One Array but Not Another
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
;; Another way:
(let [hash (merge hash {"key1" 1, "key2" 2})]
  ;; ...
  )
;;-----------------------------
;; The behavior of this Perl code:

;; @seen{@B} = ();

;; is to add all of the values in array @B as keys in the map %seen,
;; but with "no value", i.e. they keys are associated with the value
;; undef.

;; This is pretty much equivalent in Clojure, with the usual
;; distinction that Clojure is returning a new map, not modifying the
;; original one in place as Perl does.
(def seen (merge seen (zipmap B (repeat nil))))
;;-----------------------------
(def seen (merge seen (zipmap B (repeat 1))))
;;-----------------------------

;; @@PLEAC@@_4.8 Computing Union, Intersection, or Difference of Unique Lists
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
          (var-set union (conj @union e))
          (let [target (if (== (count e) 2) isect diff)]
            (var-set target (conj @target e))))
        [@union @isect @diff])]
  (printf "union=%s\n" (str/join " " union))
  (printf "isect=%s\n" (str/join " " isect))
  (printf "diff=%s\n" (str/join " " diff)))
union=9 2 8 7 6 5 3 1
isect=7 5 3
diff=9 2 8 6 1
;;-----------------------------

;; @@PLEAC@@_4.9 Appending One Array to Another
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
;; splice that will return a new data structure that is similar in its
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
;; of positive, 0, or negative arguments to Perl's splice for offset
;; and length.  It should also work as a helper for implementing a
;; Clojure function that works like Perl's substr, based on Clojure's
;; subs, which is why ps-start-end is written as a separate function.

;; This helper function converts Perl offset and length arguments to
;; Clojure start and end arguments for subvec (and also subs).  It is
;; a bit of a mess because of all of the conditions to check.  There
;; is likely code much like this buried inside of Perl's
;; implementation of substr and splice.

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

;; @@PLEAC@@_4.10 Reversing an Array
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

;; @@PLEAC@@_4.11 Processing Multiple Elements of an Array
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

;; @@PLEAC@@_4.12 Finding the First List Element That Passes a Test
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
;; Assume that for each employee in employees, (category employee) and
;; (name employee) will return what $employee->category() and
;; $employee->name() do in the Perl example.

;; Note that the original Perl example will never assign a value to
;; $highest_engineer if no employee has the category "engineer", and
;; the code below will assign a value of nil to highest-engineer in
;; that case.  Both examples should really check for that case, unless
;; there is some reason you believe that it can never happen.
(let [highest-engineer (first (filter #(= (category %) "engineer") employees))]
  (printf "Highest paid engineer is: %s\n" (name highest-engineer)))
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

;; @@PLEAC@@_4.13 Finding All Elements in an Array Matching Certain Criteria
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

;; @@PLEAC@@_4.14 Sorting an Array Numerically
;;-----------------------------
;; Clojure does not auto-convert between numbers and strings depending
;; upon how they are used.  There is no built-in comparison function
;; like Perl's <=> operator that takes two strings, tries to convert
;; them to numbers, and compares them numerically.  If you have
;; strings containing numbers in Clojure, and want to compare them
;; numerically, you must convert the strings to numbers explicitly.
;; If they are integers that fit into a Java long, Java's
;; Long/parseLong might be what you want.  Bigger integers can be
;; parsed with BigInteger's constructor like this:
;; (BigInteger. "577777").  Double/parseDouble tries to parse and
;; return a double value.

;; (compare x y) can be used like Perl's cmp or <=> to compare two
;; values, as long as their types are the same, or 'similar enough'.
;; Different numeric types can be compared with compare, for example.
(def sorted (sort unsorted))
;; Or if you want to do an explicit comparison function:
(def sorted (sort #(compare %1 %2) unsorted))
;;-----------------------------
(require '[clojure.java.shell :as shell])

;; pids is an unsorted sequence of numeric process IDs
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
  ;; the system, so probably won't work on Windows, whereas I believe
  ;; Perl's subroutine kill would.
  (shell/sh "kill" "-TERM" (str pid))
  (Thread/sleep 2000)  ; units are millisec
  (shell/sh "kill" "-KILL" (str pid)))
(System/exit 0)
;;-----------------------------
(def descending (sort #(compare %2 %1) unsorted))
;;-----------------------------
;; You can use your own function of two arguments as a comparison
;; function.  Like for Perl, it should return consistent results, and
;; represent a total order on the items being sorted.  Unlike Perl,
;; there are no special calling conventions with named parameters like
;; Perl's $a and $b -- just a normal function of two arguments.  For
;; this reason, there is no problem calling a comparison function
;; defined in a different namespace than the namespace where sort is
;; called.
(ns sort.subs)
(defn revnum [a b] (compare b a))

(ns other.namespace)
(sort sort.subs/revnum [4 19 8 3])
;;-----------------------------
(def all (sort #(compare %2 %1) [4 19 8 3]))
;;-----------------------------

;; @@PLEAC@@_4.15 Sorting a List by Computable Field
;;-----------------------------
(def ordered (sort #(compare %1 %2) unordered))
;;-----------------------------
(let [precomputed (map (fn [x] [(compute x) x]) unordered)
      ordered-precomputed (sort #(compare (%1 0) (%2 0)) precomputed)
      ordered (map #(% 1) ordered-precomputed)]
  ;; ...
  )
;;-----------------------------
(def ordered (map #(% 1)
                  (sort #(compare (%1 0) (%2 0))
                        (map (fn [x] [(compute x) x])
                             unordered))))
;; Since each sequence returned by one function always becomes the
;; last argument of the next, you can also use ->> to make code that
;; looks more like the example with 'let' above.  The macro ->>
;; transforms the following expression into code like the previous
;; example.
(def ordered (->> unordered
                  (map (fn [x] [(compute x) x]))
                  (sort #(compare (%1 0) (%2 0)))
                  (map #(% 1))))
;;-----------------------------
(def ordered (sort #(compare (name %1) (name %2)) employees))
;;-----------------------------
(doseq [employee (sort #(compare (name %1) (name %2)) employees)]
  (printf "%s earns $%s\n" (name employee) (salary employee)))
;;-----------------------------
(let [sorted-employees (sort #(compare (name %1) (name %2)) employees)]
  (doseq [employee sorted-employees]
    (printf "%s earns $%s\n" (name employee) (salary employee)))
  ;; load bonus
  (doseq [employee sorted-employees]
    (if (bonus (ssn employee))
      (printf "%s got a bonus!\n" (name employee)))))
;;-----------------------------
;; Unlike Perl, Clojure treats the numerical value 0 as true when it
;; is used as a condition in if/when/and/or.  Clojure only treats the
;; values nil and false as false.

;; You can of course write a multi-key comparison function like this,
;; as you can in Perl:

(def sorted (sort #(let [cmp1 (compare (name %1) (name %2))]
                     (if (not= cmp1 0)
                       cmp1
                       (let [cmp2 (compare (age %2) (age %1))]
                         (if (not= cmp2 0)
                           cmp2
                           (compare (compare (salary %1) (salary %2)))))))
                  employees))

;; But we'd like something more compact than that.

;; It is straightforward to write a function that takes a sequence of
;; results of comparison operators, each of which is negative, 0, or
;; positive, and returns the first one that is non-0, or 0 if all of
;; them are 0.
(defn multicmp [s]
  (if-let [first-non-0 (first (filter #(not= % 0) s))]
    first-non-0
    0))

(def sorted (sort #(multicmp [ (compare (name %1) (name %2))
                               (compare (age %2) (age %1))
                               (compare (salary %1) (salary %2)) ]) employees))

;; The downside of this approach is that it always evaluates all of
;; the (compare ...) calls every time it compares two items, even if
;; the first one decides the issue.  It would be nicer if it did a
;; short-circuit evaluation like Perl's || or Clojure's or.

;; Fortunately, if there is a short-circuit evaluation that you wish
;; were built into Clojure, but it isn't yet, you can make your own
;; using macros.

(defmacro multicmp
  ([x] x)
  ([x & next]
     `(let [cmp# ~x]
        (if (not= cmp# 0)
          cmp#
          (multicmp ~@next)))))

;; We wouldn't normally write comparison functions as shown below.
;; I'm doing it only for testing that the multicmp works as expected,
;; including doing short-circuit evaluation.  See further below for a
;; more typical example.

(defn cmp1 [x y]
  (println "Doing (cmp1" x y")")
  (compare (x :name) (y :name)))

(defn cmp2 [x y]
  (println "Doing (cmp2" x y")")
  (compare (x :age) (y :age)))

(defn cmp3 [x y]
  (println "Doing (cmp3" x y")")
  (compare (x :salary) (y :salary)))

(defn fancycmp [a b]
  (multicmp (cmp1 a b)
            (cmp2 a b)
            (cmp3 a b)))

(def john1 {:name "John", :age 28, :salary 35000.00 })
(def mary  {:name "Mary", :age 25, :salary 35000.00 })
(def john2 {:name "John", :age 37, :salary 40000.00 })
(def john3 {:name "John", :age 28, :salary 30000.00 })

(fancycmp john1 mary)
Doing (cmp1 {:age 28, :name John, :salary 35000.0} {:age 25, :name Mary, :salary 35000.0} )
-3

(fancycmp john1 john2)
Doing (cmp1 {:age 28, :name John, :salary 35000.0} {:age 37, :name John, :salary 40000.0} )
Doing (cmp2 {:age 28, :name John, :salary 35000.0} {:age 37, :name John, :salary 40000.0} )
-1

(fancycmp john1 john3)
Doing (cmp1 {:age 28, :name John, :salary 35000.0} {:age 28, :name John, :salary 30000.0} )
Doing (cmp2 {:age 28, :name John, :salary 35000.0} {:age 28, :name John, :salary 30000.0} )
Doing (cmp3 {:age 28, :name John, :salary 35000.0} {:age 28, :name John, :salary 30000.0} )
1

;; Here is the more typical example.  Note that unlike the multicmp
;; function, with the multicmp macro we do not need to put the compare
;; expressions in [ ].
(def sorted (sort #(multicmp (compare (:name %1) (:name %2))
                             (compare (:age %2) (:age %1))
                             (compare (:salary %1) (:salary %2))) employees))

;; Here is a different way to do it, from Alan Malloy.  It achieves
;; short-circuit evaluation because map and remove are lazy, so they
;; will not evaluating more of the sequence of keys than is needed in
;; order to find the first compare result that is non-0.  It also
;; achieves this because it passes in the functions like name, age,
;; and salary, instead of the result of comparing the return values of
;; those functions called on two items.
(defn multicmp [& keys]
  (fn [a b]
    (or (first (remove zero? (map #(compare (% a) (% b))
                                  keys)))
        0)))

(def sorted (sort (multicmp :name :age :salary) employees))

;; The only down side is that it is restricted to sorting keys in
;; ascending order.  If the keys have numeric values, like :age does,
;; you can negate the ages before comparing them to get a descending
;; order like so:
(def sorted (sort (multicmp :name (comp - :age) :salary) employees))

;; But this does not work if you wish to sort a field with a string
;; value like :name in descending order, because it tries to do (-
;; "string value").

;; Below is a small modification to the previous multicmp that takes a
;; vector [- keyfn] in place of a keyfn in order to specify that a key
;; should be compared in the opposite order.  This function does not
;; do any extensive error checking, but it allows you to explicitly
;; specify ascending order by using any symbol besides - in the first
;; vector element, e.g. [+ keyfn].
(defn multicmp [& keys]
  (fn [a b]
    (or (first (remove zero? (map #(if (vector? %)
                                     (let [[order keyfn] %]
                                       (if (= order -)
                                         (compare (keyfn b) (keyfn a))
                                         (compare (keyfn a) (keyfn b))))
                                     (compare (% a) (% b)))
                                  keys)))
        0)))

(def sorted (sort (multicmp :name [- :age] :salary) employees))
;;-----------------------------
;; There may be a POSIX library for Clojure or Java that contains
;; getpwent, but for this example we will read /etc/passwd to get the
;; desired info.
(require '[clojure.string :as str])
(let [lines (str/split (slurp "/etc/passwd") #"\n")
      usernames (map #(first (str/split % #":")) lines)
      usernames (sort usernames)]
  (doseq [user usernames]
    (printf "%s\n" user)))
;;-----------------------------
(def sorted (sort #(compare (subs %1 1 2) (subs %2 1 2)) names))
;;-----------------------------
(def sorted (sort #(compare (count %1) (count %2)) strings))
;;-----------------------------
(let [temp (map (fn [x] [(count x) x]) strings)
      temp (sort #(compare (%1 0) (%2 0)) temp)
      sorted (map #(% 1) temp)]
  ;; ...
  )
;;-----------------------------
(def ordered (->> strings
                  (map (fn [x] [(count x) x]))
                  (sort #(compare (%1 0) (%2 0)))
                  (map #(% 1))))
;;-----------------------------
;; To compare numerically rather than by string comparison, we convert
;; the decimal strings found to numbers.  Treat a string with no
;; numbers as if it had a 0, for sorting purposes.
(let [temp (map (fn [x] [(if-let [y (re-find #"\d+" x)]
                           (read-string y) 0)
                         x]) fields)
      sorted-temp (sort #(compare (%1 0) (%2 0)) temp)
      sorted-fields (map #(% 1) sorted-temp)]
  ;; ...
  )
;;-----------------------------
(def sorted-fields (->> fields
                        (map (fn [x] [(if-let [y (re-find #"\d+" x)]
                                        (read-string y) 0)
                                      x]))
                        (sort #(compare (%1 0) (%2 0)))
                        (map #(% 1))))
;;-----------------------------
;; Note: To get this to work on my Mac, where /etc/passwd has some
;; comment lines beginning with #, add this as the second line:
;;     (filter #(not (str/blank? (str/replace % #"#.*$" ""))))

;; Clojure does not have Perl's auto-conversion between string and
;; numeric types, so we will convert strings representing decimal
;; numbers to numbers using read-string.  Java's Long/parseLong would
;; also work.
(->> (str/split (slurp "/etc/passwd") #"\n")
     (map (fn [ln] (let [[login _ uid-str gid-str] (str/split ln #":")]
                     [ln (read-string gid-str) (read-string uid-str) login])))
     (sort #(multicmp (compare (%1 1) (%2 1))  ; gid
                      (compare (%1 2) (%2 2))  ; uid
                      (compare (%1 3) (%2 3))  ; login
                      ))
     (map #(% 0))
     (str/join "\n")
     (print))
;;-----------------------------

;; @@PLEAC@@_4.16 Implementing a Circular List
;;-----------------------------
;; Two method will be given, one using Clojure vectors, the other for
;; Clojure lists/queues.  These data structures have different
;; performance characteristics for operations at the beginning or end.

;; Finger trees are another data structure that make all of these
;; operations O(log n), including some other operations not listed
;; here, like splitting a list at an arbitrary item in the middle, or
;; concatenating two lists together.
;;
;; See http://github.com/clojure/data.finger-tree

;; "O(1)" means O(log n), and the base of the logarithm is 32.

;; Abstract     Function on               Function on
;; operation    vector                    list/queue
;; -----------  ------------------------  ------------------

;; Remove from  (v 0) or (first v)        (first l)
;; beginning    returns first item
;;              "O(1)"                    O(1)
;;
;;              (subvec v 1) return vec   (rest l)
;;              with first item removed
;;              O(1)                      O(1)

;; Add x to     (vec (cons x (seq v)))    (conj x l)
;; beginning    O(n)                      O(1)

;; Remove from  (peek v)                  (last l)
;; end          returns last item
;;              "O(1)"                    O(n)
;;
;;              (pop v) return vec        (butlast l)
;;              with last item removed
;;              "O(1)"?                   O(n)

;; Add x to     (conj v x)                (concat l (list x))
;; end          "O(1)"                    O(n)

;; Using vectors
(def circular [1 2 3 4 5])
(let [new-circular (vec (cons (peek circular)  ; the last shall be first
                              (seq (pop circular))))]   ; O(n) total
  (printf "%s\n" (str/join " " new-circular)))

(let [new-circular (conj (subvec circular 1)   ; and vice versa
                         (first circular))]    ; "O(1)" total
  (printf "%s\n" (str/join " " new-circular)))

;; Using lists
(def circular '(1 2 3 4 5))
(let [new-circular (cons (last circular) (butlast circular))]
  (printf "%s\n" (str/join " " new-circular)))   ; the last shall be first
                                                 ; O(n) total

(let [new-circular (concat (rest circular) (list (first circular)))]
  (printf "%s\n" (str/join " " new-circular)))   ; and vice versa
                                                 ; O(n) total
;;-----------------------------
;; I'll use vectors here.

;; Since we cannot mutate v, we'll return the first item, and a new
;; vector that is rotated left from the input vector.
(defn grab-and-rotate [v]
  [(v 0) (conj (subvec v 1) (v 0))])
  
(loop [processes [1 2 3 4 5]]
  (let [[process next-processes] (grab-and-rotate processes)]
    (printf "Handling process %d\n" process)
    (flush)    ; printf does not automatically flush its output
    (Thread/sleep 1000)  ; units are millisec
    (recur next-processes)))
;;-----------------------------

;; @@PLEAC@@_4.17 Randomizing An Array
;;-----------------------------
;; Clojure standard library has shuffle built right in
(let [shuffled-vec (shuffle collection)]
  ;; ...
  )

;; If you want to implement the Fisher-Yates shuffle yourself, using a
;; mutable Java array is a straightforward way.  There might not be a
;; way to do it in linear time without using a mutable array.
(defn fisher-yates-shuffle [coll]
  (let [a (into-array Object coll)]
    (loop [i (dec (alength a))]
      (if (zero? i)
        (vec a)    ; copy Java array contents to Clojure vector and return that
        (let [j (rand-int (inc i))]
          (if (not= i j)
            (let [temp (aget a i)]
              (aset a i (aget a j))
              (aset a j temp)))
          (recur (dec i)))))))

;; Minor point: While the Java array used above is mutable, and we
;; mutate it in place for efficiency, function fisher-yates-shuffle
;; "as a whole" is purely functional (well, except for the minor
;; detail about using a pseudo-random number generator inside and so
;; rarely returning the same result for the same input :-), because it
;; takes an immutable collection, and returns an immutable collection.
;; The mutable thing it uses temporarily is a local variable allocated
;; inside the call, and becomes inaccessible garbage when the function
;; returns.
;;-----------------------------
(let [permutations (factorial (count array))
      shuffle (map #(array %) (n2perm (inc (rand-int permutation))
                                      (count array)))]
  ;; TBD: Test this
  )
;;-----------------------------
(defn naive-shuffle [coll]               ; don't do this
  (let [a (into-array Object coll)]
    (loop [i 0]
      (if (< i (alength a))
        (let [j (rand-int (alength a))]  ; pick random element
          (let [temp (aget a i)]         ; swap 'em
            (aset a i (aget a j))
            (aset a j temp))
          (recur (inc i)))
        (vec a)))))
;;-----------------------------

;; @@PLEAC@@_4.18 Program: words
;;-----------------------------
awk      cp       ed       login    mount    rmdir    sum
basename csh      egrep    ls       mt       sed      sync
cat      date     fgrep    mail     mv       sh       tar
chgrp    dd       grep     mkdir    ps       sort     touch
chmod    df       kill     mknod    pwd      stty     vi
chown    echo     ln       more     rm       su
;;-----------------------------
;; @@INCLUDE@@ include/clojure/ch04/words.clj
;;-----------------------------
;;Wrong       Right
;;-----       -----
;;1 2 3       1 4 7
;;4 5 6       2 5 8
;;7 8 9       3 6 9
;;-----------------------------

;; @@PLEAC@@_4.19 Program: permute
;;-----------------------------
;; If you use 1 instead of 1N below, Clojure 1.3 will use 64-bit Java
;; longs for arithmetic, and the result will overflow for n >= 21.
;; The N in 1N signifies that the constant 1 is of type
;; clojure.lang.BigInt, which can grow up to the available memory.
(defn factorial [n]
  (apply * (range 1N (inc n))))

(print (factorial 500))
1220136... (1135 digits total)

;; TBD: Finish this section.
