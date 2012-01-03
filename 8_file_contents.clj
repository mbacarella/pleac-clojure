;; @@PLEAC@@_8.0 Introduction
;;-----------------------------
;;-----------------------------

;; @@PLEAC@@_8.1 Reading Lines with Continuation Characters
;;-----------------------------
;; I'll show a few different ways to do this in Clojure.

;; First, a function combine-continuation-lines that takes a
;; collection of individual lines, and returns a lazy sequence of
;; combined lines.  It is a 'filter' that can take in a lazy sequence
;; and produce a lazy sequence with the same number or fewer strings
;; as the input sequence.

;; Some general-purpose utility functions.  take-until is similar to
;; take-while, partition-when is similar to partition-by, and chomp is
;; like Perl's chomp.

(defn take-until
  "Returns a lazy sequence of successive items from coll until, and
   including, the first item where (pred item) returns true.  If there
   are no such items, then return all items in coll.  pred must be
   free of side-effects."
  [pred coll]
  (lazy-seq
   (when-let [s (seq coll)]
     (if (pred (first s))
       (list (first s))
       (cons (first s) (take-until pred (rest s)))))))


(defn partition-when
  "Applies f to each value in coll, splitting it each time f returns
   logical true, i.e. the last item i in each partition has (f i)
   true, and only the last item.  The exception is if the last item of
   coll has (f i) false.  In that case the last partition consists of
   one or more items that all have (f i) false.  Returns a lazy seq of
   partitions."
  [f coll]
  (lazy-seq
   (when-let [s (seq coll)]
     (let [run (take-until f s)]
       (cons run (partition-when f (seq (drop (count run) s))))))))


(defn chomp
  "If the last character of s is c, return a string with the last
   character removed.  Otherwise return s."
  [^CharSequence s c]
  (let [n (count s)]
    (if (zero? n)
      s
      (if (= c (.charAt s (dec n)))
        (subs s 0 (dec n))
        s))))


;; Now functions specific to the problem at hand.

(defn continuation-line?
  [^CharSequence s]
  (let [n (count s)]
    (if (zero? n)
      false
      (= (.charAt s (dec n)) \\))))


(defn combine-continuation-lines
  [line-coll]
  (map (fn [lines] (apply str (map #(chomp % \\) lines)))
       (partition-when #(not (continuation-line? %)) line-coll)))


(doseq [joined-line (combine-continuation-lines (line-seq fh))]
  ;; process full record in joined-line here
  )


;; Second, a function combined-line-seq, similar to line-seq, that
;; takes a BufferedReader, and returns a lazy sequence of combined
;; lines.

(defn combined-line-seq2
  [accum ^java.io.BufferedReader rdr]
  (if-let [line (.readLine rdr)]
    (if (continuation-line? line)
      (recur (conj accum (subs line 0 (dec (count line)))) rdr)
      (cons (apply str (conj accum line))
            (lazy-seq (combined-line-seq2 [] rdr))))
    ;; else
    (if (= accum [])  ; (apply str []) returns "".  We want an empty sequence
      nil
      (list (apply str accum)))))


(defn combined-line-seq
  [^java.io.BufferedReader rdr]
  (combined-line-seq2 [] rdr))


(doseq [joined-line (combined-line-seq fh)]
  ;; process full record in joined-line here
  )


;; Finally, a version most like the Perl version
(require '[clojure.string :as str])

(binding [*in* fh]
  (loop [line (read-line)]
    (when line
      (let [line2 (str/replace-first line #"\\$" "")]
        (if (= line line2)
          (do
            ;; process full record in line here
            (recur (read-line)))
          (recur (str line2 (read-line))))))))
;;-----------------------------
;; DISTFILES = $(DIST_COMMON) $(SOURCES) $(HEADERS) \
;;         $(TEXINFOS) $(INFOS) $(MANS) $(DATA)
;; DEP_DISTFILES = $(DIST_COMMON) $(SOURCES) $(HEADERS) \
;;         $(TEXINFOS) $(INFO_DEPS) $(MANS) $(DATA) \
;;         $(EXTRA_DIST)
;;-----------------------------
;; The last example above takes only a one line change to make
;; continuation lines able to have spaces after the backslash.

(let [line2 (str/replace-first line #"\\\s*$" "")]
  ;; as before
  )

;; The first two examples require a few more changes than one line,
;; e.g. change the definition of continuation-line? to permit spaces
;; after the backslash, and replace (chomp % \\) with
;; (str/replace-first % #"\\\s*$" "").
;;-----------------------------
