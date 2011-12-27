#! /usr/bin/env clj

(ns urlify
  (:require [clojure.string :as str]))

(defmacro while-<>
  [[file line] & body]
  `(doseq [~file (or *command-line-args* [*in*])]
     (with-open [rdr# (clojure.java.io/reader ~file)]
       (doseq [~line (line-seq rdr#)]
         ~@body))))

;; Note that since we don't have Perl's string interpolation, we can't
;; use the #"pattern" syntax in Clojure to combine multiple
;; sub-patterns, and so any backslashes or special characters need an
;; extra \ to escape them in a Clojure string.

(def urls "(http|telnet|gopher|file|wais|ftp)")
(def ltrs "\\w")
;; Note: Unlike Perl, the # character needs to be escaped here,
;; because it will be used as part of a regex with the (?x) modifier,
;; in which # denotes the beginning of a comment.
(def gunk "/\\#~:.?+=&%@!\\-")
(def punc ".:?\\-")
(def any  (str ltrs gunk punc))

(def pat
     (re-pattern
      (str "(?ix)
      \\b                   # start at word boundary
      (                     # begin $1  {
       " urls "  :          # need resource and a colon
       [" any "] +?         # followed by one or more
                            #  of any valid character, but
                            #  be conservative and take only
                            #  what you need to....
      )                     # end   $1  }
      (?=                   # look-ahead non-consumptive assertion
       [" punc "]*          # either 0 or more punctuation
       [^" any "]           #   followed by a non-url char
       |                    # or else
       $                    #   the end of the string
      )
    ")))

(while-<> [file line]
  (printf "%s\n"
          (str/replace line pat
                       (fn [[whole-match url]]
                         (str "<A HREF=\"" url "\">" url "</A>")))))
