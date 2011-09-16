;; vectors
(def nested1 ["this" "that" "the" "other"])
(def nested2 ["this" "that" ["the" "other"]])
(def tune ["The" "Star-Spangled" "Banner"])

(def a ["quick" "brown" "fox"])
(defn qw
  "Split string on whitespace. Returns a seq."
  [s] (seq (.split s "\\s")))
(def a2 (qw "Why are you teasing me?"))
;; (def lines
;;   (.replaceAll "    The boy stood on the burning deck,
;;     It was as hot as glass."
;;                "\\ \+" ""))
