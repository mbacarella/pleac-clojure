;; @@PLEAC@@_2.1 Checking Whether a String Is a Valid Number

(import '(java.text NumberFormat ParseException)
        '(java.util Locale))

(def locale Locale/US)

(defn nb [s]
  (let [nf (NumberFormat/getInstance locale)]
    (.parse nf s)))

;; user=> (nb "100")
;; 100
;; user=> (nb "not a number")
;; java.text.ParseException: Unparseable number: "not a number"

;; (def s1 "100")
;; (def s1 "not a number")
(try
  (Integer/parseInt s1)
  (catch NumberFormatException _ex
    (println (str s1 " is not an integer"))))

;; (def s2 3.14)
;; (def s2 "foo")
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

;; @@PLEAC@@_2.2 Comparing Floating-Point Numbers
;;----------------------------------------------------------------------------------
;; (equal NUM1 NUM2 ACCURACY) returns true if NUM1 and NUM2 are
;; equal to ACCURACY number of decimal places
;; jli for mbac: not sure if you can use with-precision for this:
;; http://clojure.github.com/clojure/clojure.core-api.html#clojure.core/with-precision
(defn equal [num1 num2 accuracy]
  (letfn [(bignum [num]
            (.setScale (BigDecimal. num)
                       accuracy
                       BigDecimal/ROUND_DOWN))]
    (= 0 (.compareTo (bignum num1) (bignum num2)))))

;;----------------------------------------------------------------------------------
;; with a scaling factor
;; use "M" suffix for BigDecimal literals
(def wage 5.36M)
(def hours 40M)
(def week (.multiply wage hours))

(println (str "One week's wage is: $" week))
;; One week's wage is: $214.40
;;----------------------------------------------------------------------------------

;; @@PLEAC@@_2.3 Rounding Floating-Point Numbers
;;----------------------------------------------------------------------------------
;; (def unrounded ...)
;; (def scale ...)
;; (def roundingMode ...)
(def rounded (.setScale unrounded scale roundingMode))
;;----------------------------------------------------------------------------------
(def a 0.255M)
(def b (.setScale a 2 BigDecimal/ROUND_HALF_UP))
(println (str "Unrounded: " a))
(println (str "Rounded: " b))
;=> Unrounded: 0.255
;=> Rounded: 0.26
;;----------------------------------------------------------------------------------
;; caution, Math.rint() rounds to the nearest integer!
(def a [3.3 3.5 3.7 -3.3])
(println "number\tint\tfloor\ceil")
(map (fn [x] (println (str (Math/rint x) "\t" (Math/floor x) "\t" (Math/ceil x)))) a)
;;  3.0     3.0     4.0
;;  4.0     3.0     4.0
;;  4.0     3.0     4.0
;; -3.0    -4.0    -3.0

;; @@PLEAC@@_2.4 Converting Between Binary and Decimal
;;----------------------------------------------------------------------------------
(def i (Integer/parseInt s 2))
;;----------------------------------------------------------------------------------
(def s (Integer/toString i 2))
;;----------------------------------------------------------------------------------
;; reader supports "<radix>r<number>"
(def i 2r0110110) ; i = 54
;;----------------------------------------------------------------------------------
(def s (Integer/toString 54 2)) ; s = 110110
;;----------------------------------------------------------------------------------

;; @@PLEAC@@_2.5 Operating on a Series of Integers
;;----------------------------------------------------------------------------------
(let [x 1 y 10]
  (doseq [i (range x (inc y))]
    ;; i is set to every integer from X to Y inclusive
    ))

(let [x 1 y 10]
  (doseq [i (range x (+ y 1) 7)]
    ;; i is set to every integer from X to Y, stepsize = 7
    ))

;;----------------------------------------------------------------------------------
(apply println (cons "Infancy is:" (range 0 3)))
(apply println (cons "Toddling is:" (range 3 5)))
(apply println (cons "Childhood is:" (range 5 13)))
;; Infancy is: 0 1 2
;; Toddling is: 3 4
;; Childhood is: 5 6 7 8 9 10 11 12
;;----------------------------------------------------------------------------------

;; @@PLEAC@@_2.6 Working with Roman Numerals
;;----------------------------------------------------------------------------------
;; no roman module available
;;----------------------------------------------------------------------------------

;; @@PLEAC@@_2.7 Generating Random Numbers
;;----------------------------------------------------------------------------------
(import '(java.util Random))
(def random (Random. ))
(def i (+ (.nextInt random (- y (+ x 1))) x))
;;----------------------------------------------------------------------------------
(def i (+ (.nextInt random 51) 25))
(println i)
;;----------------------------------------------------------------------------------

;; @@PLEAC@@_2.8 Generating Different Random Numbers
;;----------------------------------------------------------------------------------
;; Seed the generator with an integer
(Random. 5)

;; Use SecureRandom instead to seed with bytes from stdin
(import '(java.security SecureRandom))
;; jli for mbac: unqualified "use" is almost always ungood. use :only
;; or :rename.
;; mbac: fixed!
(use '[clojure.contrib.io :only (to-byte-array)])
(SecureRandom. (to-byte-array System/in))

;; @@PLEAC@@_2.9 Making Numbers Even More Random
(let [srng (SecureRandom.)
      buf (byte-array 10)]
  (do
    (.nextBytes srng buf)
    buf))

;; @@PLEAC@@_2.10 Generating Biased Random Numbers
(def prng (Random.))

(defn gaussian-rand []
  (let [[w u1 u2] (loop []
                    (let [u1 (- (* 2 (.nextDouble prng)) 1)
                          u2 (- (* 2 (.nextDouble prng)) 1)
                          w (+ (* u1 u1) (* u2 u2))]
                      (if (>= w 1)
                        (recur)
                        [w u1 u2])))
        w (Math/sqrt (* -2 (/ (Math/log w) w)))
        g2 (* u1 w)
        g1 (* u2 w)]
    g1))

;; -----------------------------
;; weight-to-dist: takes a list of pairs mapping key to weight and
;; returns a list of pairs mapping key to probability
(defn weight-to-dist [weights]
  (let [total (apply + (map (fn [[_key weight]] weight) weights))]
    (map (fn [[key weight]] [key (/ weight total)])
         weights)))

;; weighted-rand: takes a list of pairs mapping key to probability
;; and returns the corresponding key
(defn weighted-rand [dist]
  ;; accumulate without mutation
  (let [go (fn cont [p lst]
             (if-let [[key weight] (first lst)]
               (let [pp (- p weight)]
                 (if (< pp 0)
                   [pp key]
                   (recur pp (rest lst))))))
        result (go (.nextDouble (Random.)) dist)]
    ;; to avoid floating point inaccuracies
    (if (nil? result)
      (recur dist)
      (let [[_p key] result]
        key))))

(def mean 25)
(def stddev 2)
(def salary (+ (* (gaussian-rand) stddev) mean))
(printf "You have been hired at $%.2f\n" salary)

;; @@PLEAC@@_2.11 Doing Trigonometry in Degrees, not Radians
(defn radians [deg] (Math/toRadians deg))
(defn degrees [rad] (Math/toDegrees rad))

(defn sin-deg [deg] (Math/sin (radians deg)))

;; @@PLEAC@@_2.12 Calculating More Trigonometric Functions
(defn tan [theta] (/ (Math/sin theta) (Math/cos theta)))
;; or use Math/tan

(def y
  (try
    (tan (/ Math/PI 2))
    (catch Exception e nil)))

;; @@PLEAC@@_2.13 Taking Logarithms
(defn log_e [x] (Math/log x))

(defn log_10 [x] (Math/log10 x))

(defn log-base [base value] (/ (log_e value) (log_e base)))

(def answer (log-base 10 10000))
(printf "log10(10,000) = %f" answer)

;; @@PLEAC@@_2.14 Multiplying Matrices
;; This is a very academic, purely functional implementation of
;; multiply-matrix.  A performance critical implementation would
;; likely not use Clojure's immutable vectors.
(defn multiply-matrix [m1 m2]
  (let [dim (fn [m] [(count m) (count (first m))])
        [r1 c1] (dim m1)
        [r2 c2] (dim m2)]
    (if (not (= c1 r2))
      nil ; matrix dimensions don't match
      (let [dot-product (fn [v1 v2]
                          (reduce (fn [a i] (+ a (* (nth v1 i) (nth v2 i))))
                                  0
                                  (range 0 (count v1))))
            row (fn [m i] (nth m i))
            col (fn [m i] (map (fn [r] (nth (nth m r) i))
                               (range (count m))))]
        (map (fn [r]
               (map (fn [c] (dot-product (row m1 r) (col m2 c)))
                    (range 0 c2)))
             (range 0 r1))))))

;; @@PLEAC@@_2.15 Using Complex Numbers
;; c = a * b manually
(defrecord Complex [real imag])
(defn mul [a b]
  (Complex. (* (.real a) (.real b))
            (* (.imag a) (.imag b))))

;; c = a * b using the complex-numbers module
(use '(clojure.contrib complex-numbers)
     '(clojure.contrib.generic [arithmetic :only [+ *]]
                               [math-functions :only [sqrt]]))

(def a (complex 3 5))
(def b (complex 2 -2))
(def c (* a b))

(def c (* (complex 3 5) (complex 2 -2))) ; or on one line

(defn print-complex-sqrt [x]
  (let [as-string (fn [c] (format "%s+%si"
                                  (real c)
                                  (if (= (imag c) 1) "" (imag c))))]
    (printf "sqrt(%s) = %s\n"
           (as-string x)
           (as-string (sqrt x)))))
(def d (complex 3 4))
(print-complex-sqrt d)

;; @@PLEAC@@_2.16 Converting Between Octal and Hexadecimal
;; hex and octal should not have leading 0x or 0 characters
(defn hex-and-oct-as-decs [hex octal]
  (let [dec-of-hex (Integer/parseInt hex 16)
        dec-of-oct (Integer/parseInt octal 8)]
    ;; use dec-of-hex and dec-of-oct here   
    ))

(let [num (->
           (do
             (print "Gimme a number in decimal, octal, or hex: ")
             (read-line))
           .trim
           Integer/parseInt)]
  (printf "%s %s %s\n"
          (Integer/toString num)
          (Integer/toOctalString num)
          (Integer/toHexString num)))

;; @@PLEAC@@_2.17 Putting Commas in Numbers
(import '(java.text NumberFormat)
        '(java.util Locale))
(def locale Locale/US)
(defn commify-localized [num]
  (let [nf (NumberFormat/getInstance locale)]
    (.format nf num)))

;; deck version
(defn commify-hipster [numstr]
  (->> (.toString numstr)
       reverse
       (partition-all 3)
       (interpose \,)
       flatten
       reverse
       (apply str)))

;; @@PLEAC@@_2.18 Printing Correct Plurals
(defn print-plurals [hours centuries]
  (do 
    (printf "It took %d hour%s\n" hours (if (= hours 1) "" "s"))
    (printf "%d hour%s %s enough.\n"
            hours
            (if (= hours 1) "" "s")
            (if (= hours 1) "is" "are"))
    (printf "It took %d centur%s\n" centuries (if (= centuries 1) "y" "ies"))))

(def noun-rules
  [[#"ss$" "sses"]
   [#"ph$" "phes"]
   [#"sh$" "shes"]
   [#"ch$" "ches"]
   [#"z$"  "zes"]
   [#"ff$" "ffs"]
   [#"f$"  "ves"]
   [#"ey$" "eys"]
   [#"y$"  "ies"]
   [#"ix$" "ices"]
   [#"s$"  "ses"]
   [#"x$"  "xes"]
   [#"$"   "s"]])

(require '(clojure.contrib [str-utils2 :as s]))
(defn noun_plural [word]
  (some (fn [[re ending]]
          (if (re-find re word) (s/replace word re ending)))
        noun-rules))
(def verb_singular noun_plural) ; make function alias

;; Note: there's no perl Lingua::EN::Inflect equivalent module

;; @@PLEAC@@_2.19 Program: Calculating Prime Factors
;; jli for mbac: shouldn't this return {orig 1} for prime numbers?
;; mbac for jli: maybe, but the perl version doesn't do this either
(defn get-factors [orig]
  (letfn [(factor-out [n factors i]
            (if (zero? (mod n i))
              (recur (/ n i)
                     (assoc factors i (inc (factors i 0)))
                     i)
              [n factors]))]
    (loop [i 2
           sqi 4
           [n factors] [orig {}]]
      (cond
       (<= sqi n) (recur (inc i)
                         (+ sqi (* 2 i) 1)
                         (factor-out n factors i))
       (and (not= n 1)
            (not= n orig)) (assoc factors n (inc (factors n 0)))
       :default factors))))

(defn print-factors [orig factors]
  (let [head (format "%-10d" orig)
        lines (if (zero? (count factors))
                ["PRIME"]
                (map (fn [[f e]] (format "%d^%d" f e)) factors))
        s (apply str (interpose "\n" (cons head lines)))]
    (println s)))

(loop [i 0]
  (if (< i (count *command-line-args*))
    (let [n (Integer/parseInt (.get argv i))]
      (do
        (print-factors n (factorize n))
        (recur (inc i))))))
