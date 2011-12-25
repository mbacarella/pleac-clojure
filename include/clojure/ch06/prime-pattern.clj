#! /usr/bin/env clj
;; prime_pattern -- find prime factors of argument using pattern matching

(ns prime-pattern
  (:require [clojure.string :as str]))


(let [num (Long/parseLong (nth *command-line-args* 0))
      N (loop [N (apply str (repeat num "o"))]
          (if-let [[whole-match unary-factor]
                   (re-find #"^(oo+?)\1+$" N)]
            (do
              (printf "%d " (count unary-factor))
              (recur (str/replace N unary-factor "o")))
            N))]
  (printf "%d\n" (count N)))
