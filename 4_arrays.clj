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
