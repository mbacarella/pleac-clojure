#! /usr/bin/env clj
;; killtags - very bad html tag killer

(require '[clojure.string :as str])

(doseq [file (or *command-line-args* *in*)]
  (let [lines (slurp file)]                    ; slurp reads whole file
    (printf "%s" (str/replace lines #"(?s)<.*?>" ""))))  ; strip tags (terribly)
