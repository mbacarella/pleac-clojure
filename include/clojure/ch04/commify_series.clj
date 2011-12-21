#! /usr/bin/env clj

;; commify_series.clj - show proper comma insertion in list output

(require '[clojure.string :as str])

(defn perl-split
  "Split string as Perl's split would, including how it has a special
  case for the pattern ' ' that ignores leading whitespace in the
  string to be split."
  ([s pat-or-space]
     (if (= pat-or-space " ")
       ;; Eliminate leading whitespace before using Clojure's split
       (str/split (str/replace-first s #"^\s+" "") #"\s+")
       (str/split s pat-or-space)))
  ([s pat-or-space limit]
     (if (= pat-or-space " ")
       (str/split (str/replace-first s #"^\s+" "") #"\s+" limit)
       (str/split s pat-or-space limit))))

(defn qw
  "Split string on whitespace. Returns a seq."
  [s] (perl-split s " "))

(def lists [
    [ "just one thing" ]
    (qw "Mutt Jeff")
    (qw "Peter Paul Mary")
    [ "To our parents" "Mother Theresa" "God" ]
    [ "pastrami" "ham and cheese" "peanut butter and jelly" "tuna" ]
    [ "recycle tired, old phrases" "ponder big, happy thoughts" ]
    [ "recycle tired, old phrases" 
      "ponder big, happy thoughts" 
      "sleep and dream peacefully" ]
    ])

(defn commify-series [coll]
  (let [sepchar (if (first (filter #(re-find #"," %) coll)) ";" ",")]
    (case (count coll)
          0 ""
          1 (first coll)
          2 (str/join " and " coll)
          (str/join (str sepchar " ")
                    (concat (butlast coll)
                            (list (str "and " (last coll))))))))

(doseq [l lists]
  (printf "The list is: %s.\n" (str/join "" (commify-series l))))
