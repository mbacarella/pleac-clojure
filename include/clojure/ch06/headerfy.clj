#! /usr/bin/env clj
;; headerfy: change certain chapter headers to html

(ns headerfy
  (:require [clojure.string :as str]
            [clojure.java.io :as io]))

;; I don't know of any Clojure equivalent to Perl's changing the
;; record separator variable $/ to '' that changes the behavior of
;; reading to get a whole paragraph at a time (if we define a
;; paragraph to be bounded by one or more completely blank lines).

;; I'll just define a helper function to return a lazy seq of
;; paragraphs, similar to how line-seq returns a lazy seq of lines.

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
  

(doseq [file (or *command-line-args* [*in*])]
  (with-open [rdr (io/reader file)]
    (doseq [pgraph (paragraph-seq rdr)]
      (printf "%s" (str/replace pgraph
                                #"(?x)
                                  \A          # start of string
                                  (           # capture
                                     Chapter  # text string
                                     \s+      # mandatory whitespace
                                     \d+      # decimal number
                                     \s*      # optional whitespace
                                     :        # a real colon
                                     . *      # anything not a newline till end of line
                                  )"
                                (fn [[_ g1]] (str "<H1>" g1 "</H1>")))))))
