#! /usr/bin/env clj
;; mjd_permute: permute each word of input

(require '[clojure.string :as str]
         '[clojure.java.io :as io])

;; perl-split-on-space and while-<> from Section 1.6

(defn perl-split-on-space [s]
  (str/split (str/triml s) #"\s+"))

(defmacro while-<>
  [[file line] & body]
  `(doseq [~file (or *command-line-args* [*in*])]
     (with-open [rdr# (io/reader ~file)]
       (doseq [~line (line-seq rdr#)]
         ~@body))))

(defn remove-elem [v i]
  ;; (apply conj <vector1> <vector2>) gives an error if <vector2> has
  ;; no elements, so check for that case separately.
  (if (== (inc i) (count v))
    (pop v)
    (apply conj (subvec v 0 i) (subvec v (inc i)))))

(defn unmemoized-factorial [n]
  (if (zero? n)
    1N
    (* n (unmemoized-factorial (dec n)))))

(def factorial (memoize unmemoized-factorial))

;; n2pat: produce the n-th pattern of length len
(defn n2pat [n len]
  (loop [i 1
         n n
         pat []]
    (if (< i (inc len))
      (recur (inc i)
             (quot n i)
             (conj pat (mod n i)))
      pat)))


;; pat2perm: turn pattern returned by n2pat into permutation of
;; integers.  XXX: splice is already O(N)
(defn pat2perm [pat]
  (loop [perm []
         source (vec (range (count pat)))
         pat pat]
    (flush)
    (if (zero? (count pat))
      perm
      (recur (conj perm (source (peek pat)))
             (remove-elem source (peek pat))
             (pop pat)))))

(defn n2perm [n len]
  (pat2perm (n2pat n len)))

(while-<> [file line]
  (let [data (perl-split-on-space line)
        num-permutations (factorial (count data))]
    (dotimes [i num-permutations]
      (printf "%s\n" (str/join " " (map (fn [x] (data x))
                                        (n2perm i (count data))))))))
