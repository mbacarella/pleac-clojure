;; @@PLEAC@@_1.0 Strings
;; @@PLEAC@@_1.1 Introduction

;; ---------------------------
(def string "\\n")                 ; two characters, \ and an n
(def string "Jon 'Maddog' Orwant") ; literal single quotes
;; ---------------------------
(def string "\n")                    ; "newline" character
(def string "Jon \"Maddog\" Orwant") ; literal double quotes

(def a "
    This is a multiline here document
    terminated by one double quote.
    ")

;; @@PLEAC@@_1.2 Accessing Substrings
(def value (subs string offset (+ offset count)))
(def value (subs string offset (count string)))

;; or
(def value (subs string offset))


;; -----------------------------
;; get a 5-byte string, skip 3, then grab 2 8-byte strings, then the rest

;; split at 'sz' byte boundaries
;; jli for mbac: partition is the bomb for this
;; mbac for jli: hell yeah!
;; jli for mbac: I meant, "partition" is old and tired. all the cool
;;               kids are using "partition-all". see commify-hipster.
(defn split-every-n-chars [sz string]
  (if (empty? string)
    ()
    (try
      (let [beg (subs string 0 sz)
            rest (subs string sz)]
        (cons beg (split-every-n-chars sz rest)))
      (catch Exception _e [string]))))

;; or the more idiomatic version
(defn split-every-n-chars [sz string]
  ;; the map turns vector of char vector into vector of string
  (map (fn [x] (apply str x))
       (partition 5 5 nil string)))

(def fivers (split-every-n-chars 5 string))

;; chop string into individual characters
(def chars (seq string))

;; -----------------------------
(def string "This is what you have")
;; Indexes are left to right. There is no possibility to index
;; directly from right to left
;; "T"
(def first (subs string 0 1))
;; "is"
(def start (subs string 5 7))
;; "you have"
(def rest (subs string 13 (count string)))
;; "e" *)
(def last (let [len (count string)]
            (subs string (- len 1) len)))
;; "have"
(def theend (let [len (count string)]
              (subs string (- len 4) len)))
;; "you"
(def piece (let [len (count string)]
             (subs string (- len 8) (- len 5))))

;; -----------------------------
(def string "This is what you have")
(format "%s" "string")

;; Change "is" to "wasn't"
(def string (str
             (subs string 0 4)
             " wasn't "
             (subs string 8)))
;; This wasn't what you have

;; This wasn't wonderous
(def string
     (str (subs string 0 (- (count string) 12)) "ondrous"))

;; delete first character
(def string (subs string 1 (count string)))
;; his wasn't wondrous

;; delete last 10 characters
(def string (subs string 0 (- (count string) 10)))
;; his wasn'
;; -----------------------------

;; @@PLEAC@@_1.3 Establishing a Default Value
;; -----------------------------
;; use b if b is true, else c
(def a (or b c))

;; re-define x with the value y, unless x is already true
(def x (when-not x y))

;; use b if b is defined, otherwise c
(def a (if (find (ns-interns *ns*) 'b) b c))
;; -----------------------------
(def foo (or bar "DEFAULT VALUE"))

(def dir
  (if (> (count *command-line-args*) 1)
    (subvec *command-line-args 1)
    "/tmp"))

;; @@PLEAC@@_1.4 Exchanging Values Without Using Temporary Variables
;; -----------------------------
(let [var1 var2
      var2 var1])

;; -----------------------------
(def temp a)
(def a b)
(def b temp)
;; -----------------------------
(let [a "alpha"
      b "omega"]
  (let [a b
        b a]
  ; the first shall be last -- and versa vice
    ))

;; -----------------------------
(let [alpha "January"
      beta "March"
      production "August"]
;; move beta to alpha
;; move production to beta
;; move alpha to production
  (let [alpha beta
        beta production
        production alpha]

    ))

;; @@PLEAC@@_1.5 Converting Between ASCII Characters and Values

;; -----------------------------
(def num (int \a))     ; => ASCII code 97
(def char (char 97))   ; => \a
;; -----------------------------

(defn print-ascii-code-for-char [c]
  (printf "Number %d is character '%c'\n" (int c) c))

;; (print-ascii-code-for-char \a)
;; Number 97 is the ASCII character a

;; @@PLEAC@@_1.6 Processing a String One Character at a Time
;; strings are char sequences; map, reduce, and other
;; sequencing functions are available for use on strings
(def string'
  (map (fn [b]
         ;; do something with b
         )
       string))

(defn print-uniq-chars [string]
  (printf "unique chars are: %s\n"
          (sort (set string))))

;; => (print-uniq-chars "an apple a day")
;; unique chars are: (\space \a \d \e \l \n \p \y)
(defn print-ascii-value-sum [string]
  (printf "sum is %s\n" (apply + (map int string))))
;; => (print-ascii-value-sum "abc")
;; sum is 294

;; @@PLEAC@@_1.7 Reversing a String by Word or by Character
(defn reverse-by-char [str] (reverse str))

;; XXX: split on whitespace, then reverse

;; @@PLEAC@@_1.8 Expanding and Compressing Tabs

;; @@PLEAC@@_1.9 Expanding Variables in User Input

;; @PLEAC@@_1.10 Controlling Cas
(.toUpperCase "foo") ;; -> "FOO"
(.toLowerCase "FOO") ;; -> "foo"

;; @@PLEAC@@_1.11 Interpolating Functions and Expressions Within Strings

;; @@PLEAC@@_1.12 Indenting Here Documents

;; @@PLEAC@@_1.13 Reformatting Paragraphs

;; @@PLEAC@@_1.14 Escaping Characters

;; @@PLEAC@@_1.15 Trimming Blanks from the Ends of a String

(.trim string)
;; (.trim "  foo  ") => "foo"

;; @@PLEAC@@_1.16 Parsing Comma-Separated Data

