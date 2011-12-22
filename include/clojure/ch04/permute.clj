#! /usr/bin/env clj
;; tsc_permute: permute each word of input

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

(defn permute [items perms]
  (if (== (count items) 0)
    (printf "%s\n" (str/join " " perms))
    (dotimes [i (count items)]
      (permute (remove-elem items i) (cons (items i) perms)))))

(while-<> [file line]
  (permute (perl-split-on-space line) '()))
