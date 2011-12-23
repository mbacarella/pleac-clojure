#! /usr/bin/env clj
;; paragrep - trivial paragraph grepper

(ns paragrep
  (:require [clojure.string :as str]
            [clojure.java.io :as io]))

(defn read-paragraph
  "Reads and returns a string containing the next 'paragraph' from the
  BufferedReader argument.  Paragraphs are taken to be consecutive
  sequences of non-empty lines separated by one or more empty lines."
  [^java.io.BufferedReader rdr]
  (loop [lines nil
         line (.readLine rdr)]
    (cond
     ;; If we reach end of file, return the lines we have found so
     ;; far, if any, otherwise nil.
     (nil? line) (if lines (apply str lines) nil)
     ;; Skip over empty lines before the paragraph begins
     (and (= line "") (nil? lines)) (recur nil (.readLine rdr))
     ;; Otherwise an empty line is a sign that we reached the end of
     ;; the paragraph we have been reading.
     (= line "") (apply str (conj lines "\n"))
     ;; We found a non-empty line.  Append it to the list of lines in
     ;; the paragraph.
     :else (recur (conj (or lines []) line "\n")
                  (.readLine rdr)))))


(defn paragraph-seq
  "Returns paragraphs of text from rdr as a lazy sequence of strings,
  where a paragraph is defined to be a sequence of non-empty lines
  separated by one or more blank lines.  rdr must implement
  java.io.BufferedReader."
  [^java.io.BufferedReader rdr]
  (when-let [pgraph (read-paragraph rdr)]
    (cons pgraph (lazy-seq (paragraph-seq rdr)))))

;; An extended version of while-<> compared to the one that appeared
;; in Section 1.6, that also optionally tracks line numbers, or
;; actually paragraph numbers in this case..

(defmacro while-<>-pgraph
  [[args file line & opt-args] & body]
  (let [linenum (first opt-args)]
    (if linenum
      `(let [~linenum (atom 0)]
         (doseq [~file (or ~args [*in*])]
           (with-open [rdr# (clojure.java.io/reader ~file)]
             (doseq [~line (paragraph-seq rdr#)]
               (swap! ~linenum inc)
               ~@body))))
      `(doseq [~file (or ~args [*in*])]
         (with-open [rdr# (clojure.java.io/reader ~file)]
           (doseq [~line (paragraph-seq rdr#)]
             ~@body))))))

(when (= (count *command-line-args*) 0)
  (printf "usage: %s pat [files]\n" *file*)
  (flush)
  (System/exit 1))
(let [[pat-str & args] *command-line-args*]
  (try
    (re-pattern pat-str)
    (catch java.util.regex.PatternSyntaxException e
      (printf "%s: Bad pattern %s\n%s\n" *file* pat-str e)
      (flush)
      (System/exit 1)))
  (let [pat (re-pattern pat-str)]
    (while-<>-pgraph [args file pgraph pgraphnum]
      (if (re-find pat pgraph)
        (printf "%s %d: %s" file @pgraphnum pgraph)))))
