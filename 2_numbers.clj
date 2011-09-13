;; 2. Numbers
;; Checking Whether a String Is a Valid Number

(import '(java.text NumberFormat ParseException)
        '(java.util Locale))

(def locale Locale/US)

(defn nb [s]
  (let [nf (NumberFormat/getInstance locale)]
    (.parse nf s)))

; user=> (nb "100")
; 100
; user=> (nb "not a number")
; java.text.ParseException: Unparseable number: "not a number"

; (def s1 100)
; (def s1 "not a number")
(try
  (Integer/parseInt s1)
  (catch NumberFormatException _ex
    (println (str s1 " is not an integer"))))

; (def s2 3.14)
; (def s2 "foo")
(try
  (Float/parseFloat s2)
  (catch NumberFormatException _ex
    (println (str s2 " is not a float"))))

(defn isNumeric [s]
  (let [nf (NumberFormat/getInstance locale)]
    (try
      (do
        (.parse nf s)
        true)
      (catch ParseException _ex false))))

;; Comparing Floating-Point Numbers
; (equal NUM1 NUM2 ACCURACY) returns true if NUM1 and NUM2 are
; equal to ACCURACY number of decimal places
(defn equal [num1 num2 accuracy]
  (let [bigNum (fn [num]
                 (.setScale
                  (BigDecimal. num)
                  accuracy
                  BigDecimal/ROUND_DOWN))]
    (= (.compareTo
        (bigNum num1)
        (bigNum num2)) 0)))

; with a scaling factor
(def wage (BigDecimal. "5.36"))
(def hours (BigDecimal. "40"))
(def week (.multiply wage hours))

(println (str "One week's wage is: $" week))
; One week's wage is: $214.40

;; Rounding Floating-Point Numbers
