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


;; @@PLEAC@@_8.2 Counting Lines (or Paragraphs or Records) in a File
;;-----------------------------
(require '[clojure.java.shell :as shell]
         '[clojure.string :as str])

;; shell-run was introduced in Section 4.1.  This is a modified
;; version that returns everything shell/sh does, including the exit
;; status as well as the output.
(defn shell-run [cmd]
  (let [shell (or (get (System/getenv) "SHELL") "bash")]
    (shell/sh shell "-c" cmd)))


(defn die [msg exit-status]
  (binding [*out* *err*] 
    (printf "%s" msg)
    (flush))
  (let [shifted-status (bit-shift-right exit-status 8)]
    (System/exit (if (zero? shifted-status) 255 shifted-status))))


(let [{:keys [out exit]} (shell-run (str "wc -l < " file))]
  (when (not= exit 0)
    (die (format "wc failed: %d" exit) exit))
  (let [count (Long/parseLong (str/trim (str/trim-newline out)))]
    (printf "count=%d\n" count)
    ;; ...
    ))
;;-----------------------------
(require '[clojure.java.io :as io])

(let [count (with-open [rdr (io/reader file)]
              (count (line-seq rdr)))]
  ;; count now holds the number of lines read
  )
;;-----------------------------
;; TBD: How to do unbuffered reading of file and counting of newline
;; characters in Clojure/Java?
;;-----------------------------
;; Repeat of two examples above.
;;-----------------------------
;; We can make another version with an explicit loop if we really want
;; to.
(let [count (with-open [rdr (io/reader file)]
              (binding [*in* rdr]
                (loop [count 0
                       line (read-line)]
                  (if line
                    (recur (inc count) (read-line))
                    count))))]
  ;; ...
  )
;;-----------------------------
;; Clojure doesn't have anything quite like Perl's $. built in.
;; Something similar could be created, but I won't do so here.
;;-----------------------------
;; paragraph-seq was introduced in example headerfy.clj in Section
;; 6.6.  We will not repeat its definition here.
(let [para-count (with-open [rdr (io/reader file)]
                   (count (paragraph-seq rdr)))]
  ;; para-count now holds the number of paragraphs read
  )
;;-----------------------------
