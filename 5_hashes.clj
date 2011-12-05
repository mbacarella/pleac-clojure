;; @@PLEAC@@_5.0 Introduction

(ns pleac-section-5
  (:require [clojure.java.io :as io]
            [clojure.string :as string]))

;; The commas are whitespace and optional. Maps are printed with them
;; at the REPL for readability.
(def age {"Nat" 24, "Jules" 25, "Josh" 17})

(def age (assoc age "Nat" 24))
(def age (assoc age "Jules" 25))
(def age (assoc age "Josh" 17))

;; Commas omitted here.
(def food-color {"Apple"  "red"
                 "Banana" "yellow"
                 "Lemon"  "yellow"
                 "Carrot" "orange"})

;; @@PLEAC@@_5.1 Adding an Element to a Hash

;; Maps, like all core Clojure data structures, are immutable.
;; Functions for "changing" maps just return new maps.
(def food-color (assoc food-color "Raspberry" "pink"))
(println "Known foods:")
(doseq [food (keys food-color)]
  (println food))
;; Known foods:
;; Carrot
;; Banana
;; Raspberry
;; Lemon
;; Apple

;; @@PLEAC@@_5.2 Testing for the Presence of a Key in a Hash

(if (contains? food-color "key")
  "exists"
  "doesn't exist")

(doseq [name ["Banana" "Martini"]]
  (if (contains? food-color name)
    (println name "is a food.")
    (println name "is a drink.")))

;; Banana is a food.
;; Martini is a drink.

(def age {})
(def age (assoc age "Toddler" 3))
(def age (assoc age "Unborn" 0))
(def age (assoc age "Phantasm" nil))

;; Maps are functions from keys to values, and can be called exactly
;; like functions, taking a key as an argument. The function call
;; returns nil if the key doesn't exist. This works just like using
;; the function "get".

(doseq [thing ["Toddler" "Unborn" "Phantasm" "Relic"]]
  (printf "%s: " thing)
  (when (contains? age thing) (print "Exists "))
  ;; get returns nil when the key isn't in the map, and when the key
  ;; does exist and the value is nil.
  (when (get age thing) (print "Defined "))
  ;; This works just like the above. Output differs from Perl because
  ;; 0 is not falsy.
  (when (age thing) (print "True "))
  (newline))

;; Toddler: Exists Defined True 
;; Unborn: Exists Defined True 
;; Phantasm: Exists 
;; Relic: 

(defn file-sizes [files]
  (reduce (fn [map file]
            (let [file (.trim file)]
              (if (contains? map file)
                map
                (assoc map file (.length (java.io.File. file))))))
          {}
          files))

(file-sizes (line-seq (io/reader *in*)))

;; @@PLEAC@@_5.3 Deleting from a Hash

;; dissoc is used to "remove" (return a new map without the key)

(defn print-foods []
  (println "Keys:" (string/join " " (keys food-color)))
  (print "Values: ")
  (doseq [food (keys food-color)]
    (if (food-color food)
      (printf "%s " (food-color food))
      (print "(undef) ")))
  (newline))

(println "Initially:")
(print-foods)

(println "\nWith Banana undef")
(def food-color (assoc food-color "Banana" nil))
(print-foods)

(println "\nWith Banana deleted")
(def food-color (dissoc food-color "Banana"))
(print-foods)

;; Initially:
;; Keys: Carrot Banana Lemon Apple
;; Values: orange yellow yellow red 

;; With Banana undef
;; Keys: Carrot Banana Lemon Apple
;; Values: orange (undef) yellow red 

;; With Banana deleted
;; Keys: Carrot Lemon Apple
;; Values: orange yellow red 

(def food-color (dissoc food-color "Banana" "Apple" "Cabbage"))

;; @@PLEAC@@_5.4 Traversing a Hash

(doseq [[key value] food-color]
  ;; do something
  )

(doseq [key (keys food-color)]
  (let [value (food-color key)]
    ;; do something
    ))

;; food-color per the introduction
(doseq [[food color] food-color]
  (printf "%s is %s.\n" food color))
;; Carrot is orange.
;; Banana is yellow.
;; Lemon is yellow.
;; Apple is red.

(doseq [food (keys food-color)]
  (let [color (food-color food)]
    (printf "%s is %s.\n" food color)))
;; Carrot is orange.
;; Banana is yellow.
;; Lemon is yellow.
;; Apple is red.

(doseq [food (sort (keys food-color))]
  (let [color (food-color food)]
    (printf "%s is %s.\n" food color)))
;; Apple is red.
;; Banana is yellow.
;; Carrot is orange.
;; Lemon is yellow.

;; There isn't an idiomatic way to reset an iteration through a
;; collection in Clojure.

(defn countfrom [file]
  (with-open [rdr (io/reader file)]
    (let [lines (line-seq rdr)
          match-sender (fn [line]
                         (second (re-matches #"^From: (.*)" line)))
          from (reduce (fn [map line]
                         (let [sender (match-sender line)
                               cur (get map sender 0)]
                           (assoc map sender (inc cur))))
                       {}
                       lines)]
      (doseq [[person n] (sort from)]
        (printf "%s: %d\n" person n)))))
