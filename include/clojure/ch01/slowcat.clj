#! /usr/bin/env clj
;; slowcat - emulate a   s l o w   line printer
;; usage: slowcat.clj [-DELAY] [files ...]
(require '[clojure.java.io :as io])

(let [args *command-line-args*
      [args delay] (if-let [match (re-find #"^-([.\d]+)" (or (first args) ""))]
                     [(rest args) (read-string (second match))]
                     [args 1])]
  (doseq [file (if (not= 0 (count args)) args [*in*])]
    (with-open [rdr (io/reader file)]
      (doseq [line (line-seq rdr)]
        (doseq [s (re-seq #"." line)]
          (print s)
          (flush)
          (Thread/sleep (long (* 5 delay))))  ; delay is multiple of 5 millisec
        (println)
        (Thread/sleep (long (* 5 delay)))))))
