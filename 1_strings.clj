;; @@PLEAC@@_1.0 Strings
;; @@PLEAC@@_1.1 Introduction

; ---------------------------
(def string "\\n")                 ; two characters, \ and an n
(def string "Jon 'Maddog' Orwant") ; literal single quotes
; ---------------------------
(def string "\n")                    ; "newline" character
(def string "Jon \"Maddog\" Orwant") ; literal double quotes

(def a "
    This is a multiline here document
    terminated by one double quote.
    ")

;; @@PLEAC@@_1.2 Accessing Substrings
(def value (.substring string offset (+ offset count)))
(def value (.substring string offset (.length string)))
 
; or
(def value (sub_end string offset))
; using
(defn sub_end [string offset]
  (.substring string offset (.length string)))


; -----------------------------
; get a 5-byte string, skip 3, then grab 2 8-byte strings, then the rest

; split at 'sz' byte boundaries
(defn split-every-n-chars [sz string]
  (if (= string "")
    ()
    (try
      (let [beg (.substring string 0 sz)
            rest (sub_end string sz)]
        (cons beg (split-every-n-chars sz rest)))
      (catch Exception _e string))))
            
(def fivers (split-every-n-chars 5 string)

; chop string into individual characters
(def chars (map (fn [x] (.charAt x 0)) (split-every-n-chars 1 string)))

; -----------------------------
(def string "This is what you have")
; Indexes are left to right. There is no possibility to index
; directly from right to left
; "T"
(def first (.substring string 0 1))
; "is"
(def start (.substring string 5 7))
; "you have"
(def rest (.substring string 13 (.length string)))
; "e" *)
(def last (let [len (.length string)]
            (.substring string (- len 1) len)))
; "have"
(def theend (let [len (.length string)]
              (.substring string (- len 4) len)))
; "you"
(def piece (let [len (.length string)]
             (.substring string (- len 8) (- len 5))))

; -----------------------------
(def string "This is what you have")
(format "%s" "string")

; Change "is" to "wasn't"
(def string (str
             (.substring string 0 4)
             " wasn't "
             (sub_end string 8)))
; This wasn't what you have

; This wasn't wonderous
(def string
  (str (.substring string 0 (- (.length string) 12)) "ondrous"))

; delete first character
(def string (.substring string 1 (.length string)))
; his wasn't wondrous

; delete last 10 characters
(def string (.substring string 0 (- (.length string) 10)))
; his wasn'
; -----------------------------

;; @@PLEAC@@_1.3 Establishing a Default Value

; -----------------------------
; use b if b is true, else c
(def a (or b c))

(when-not x (def x y))

; use b if b is defined, otherwise c
(def a (if (find (ns-interns *ns*) 'b) b c))
; -----------------------------
(def foo (or bar "DEFAULT VALUE"))

(def dir
  (if (> (count *command-line-args*) 1)
    (subvec *command-line-args 1)
    "/tmp"))

;; @@PLEAC@@_1.4 Exchanging Values Without Using Temporary Variables
; -----------------------------
(let [var1 var2
      var2 var1])
; -----------------------------
(def temp a)
(def a b)
(def b temp)
; -----------------------------
(let [a "alpha"
      b "omega"]
  (let [a b
        b a]
  ; the first shall be last -- and versa vice
    ))

; -----------------------------
(let [alpha "January"
      beta "March"
      production "August"]
; move beta to alpha
; move production to beta
; move alpha to production
  (let [alpha beta
        beta production
        production alpha]

    ))

;; @@PLEAC@@_1.5 Converting Between ASCII Characters and Values
