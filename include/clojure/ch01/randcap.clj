#! /usr/bin/env clj
;; randcap: filter to randomly capitalize 20% of the letters

(require '[clojure.string :as str])
(require '[clojure.java.io :as io])

(defn randcase [s]
  (if (< (rand 100) 20)
    (str/upper-case s)
    (str/lower-case s)))

;; This only reads from standard input.  If you want to be able to
;; also handle a list of input file names on the command line like
;; Perl's <>, see Section 1.6 macro while-<>
(doseq [line (line-seq (io/reader *in*))]
  (let [line (str/replace line #"\w" randcase)]
    (println line)))
