;; @@PLEAC@@_4.0 Arrays

;; @@PLEAC@@_4.1 Introduction
;;-----------------------------
;; vectors
(def simple ["this" "that" "the" "other"])
(def nested [["this"] ["that"] ["the" "other"]])

(assert (= (count simple) 4))
(assert (= (count nested) 3))
(assert (= (count (nth nested 2)) 2))

;;-----------------------------
(def tune ["The" "Star-Spangled" "Banner"])
;;-----------------------------

;; @@PLEAC@@_4.2 Specifying a List In Your Program
(def a ["quick" "brown" "fox"])
(defn qw
  "Split string on whitespace. Returns a seq."
  [s] (seq (.split s "\\s")))
(def a2 (qw "Why are you teasing me?"))
(def lines
  (.replaceAll "    The boy stood on the burning deck,
    It was as hot as glass."
               "\\ +" ""))
