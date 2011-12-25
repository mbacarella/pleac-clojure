#! /usr/bin/env clj
;; minigrep - trivial grep

;; TBD: Use a namespace that defines while-<> and takes a list of
;; command line args as the first argument.
(let [pat-str (first *command-line-args*)
      pat (re-pattern pat-str)]
  (while-<> [(rest *command-line-args*) file line]
    (if (re-find pat line)
      (printf "%s\n" line))))
