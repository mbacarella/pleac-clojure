#! /usr/bin/env clj
;; popgrep4.clj - grep for abbreviations of places that say "pop"
;; version 4: use re-pattern to compile regex patterns and save the results

(ns popgrep4
  (:require [clojure.string :as str]
            [clojure.java.io :as io]))

;; perl-split-on-space and while-<> were introduced in Section 1.6
(defn perl-split-on-space [s]
  (str/split (str/triml s) #"\s+"))

(defn qw
  "Split string on whitespace. Returns a seq."
  [s] (perl-split-on-space s))

(defmacro while-<>
  [[file line] & body]
  `(doseq [~file (or *command-line-args* [*in*])]
     (with-open [rdr# (clojure.java.io/reader ~file)]
       (doseq [~line (line-seq rdr#)]
         ~@body))))

(def popstates (qw "CO ON MI WI MN"))

(def poppats (map #(re-pattern (str "\\b" % "\\b")) popstates))

(while-<> [file line]
  (if (some #(re-find % line) poppats)
    (printf "%s\n" line)))
