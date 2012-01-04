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


;; @@PLEAC@@_8.3 Processing Every Word in a File
;;-----------------------------
;; while-<> and perl-split-on-space were defined in Section 1.6
(while-<> [file line]
  (doseq [chunk (perl-split-on-space line)]
    ;; do something with chunk
    ))
;;-----------------------------
(while-<> [file line]
  (doseq [w (re-seq #"\w[\w'-]*" line)]
    ;; do something with w
    ))
;;-----------------------------
;; Let us write this in two different styles.  First, keep it as close
;; in form as possible to the original Perl code.
(def seen (atom {}))

(while-<> [file line]
  (doseq [w (re-seq #"\w[\w'-]*" line)]
    (swap! seen update-in [(str/lower-case w)] (fnil inc 0))))

;; output map in a descending numeric sort of its values
(doseq [word (sort #(compare (@seen %2) (@seen %1)) (keys @seen))]
  (printf "%5d %s\n" (@seen word) word))


;; Second, in a more "sequencey" style.  Here is an overall view of
;; the data flow:
;;   list of files or *in*
;;   -> each gives a sequence of lines, and we want to concatenate
;;      them
;;   -> each produces 0 or more words, and we want to concatenate
;;      those word lists
;;   -> lowercase
;;   -> tally

;; tally was introduced in Section 4.6
(defn tally [coll]
  (reduce #(update-in %1 [%2] (fnil inc 0))
          {} coll))

(let [files (or *command-line-args* [*in*])
      lines (mapcat #(with-open [rdr (io/reader %)]
                       (doall (line-seq rdr)))
                    files)
      words (mapcat #(re-seq #"\w[\w'-]*" %) lines)
      lc-words (map str/lower-case words)
      seen (tally lc-words)]
  ;; output map in a descending numeric sort of its values
  (doseq [word (sort #(compare (seen %2) (seen %1)) (keys seen))]
    (printf "%5d %s\n" (seen word) word)))


;; Note: In the expression for lines above, if you leave out the doall
;; then the expression after that for words will give a 'Stream
;; closed' exception.  This is because line-seq returns a lazy
;; sequence, and when attempting to use its result after the with-open
;; expression has returned will cause line-seq to attempt to read the
;; file after it has already been closed.  This is a commonly
;; experienced tension between lazy computation and resources (like
;; files) that must be closed or cleaned up after.

;; The solution above using doall has the advantage of being simple,
;; but the disadvantage of requiring all lines in a file to be stored
;; in memory at one time, even though we might wish that only one line
;; at a time was kept in memory and then it becomes garbage.

;; If you are curious, see the following Clojure Google group thread,
;; and/or the proposed work on resource scopes in Clojure:
;; http://groups.google.com/group/clojure/browse_thread/thread/d890cb17d13ddf8a
;; http://dev.clojure.org/display/design/Resource+Scopes


;; If you don't care for all of the intermediate names in the let
;; above, then since each one is the last argument in the next
;; expression, ->> is useful.
(let [seen (->> (or *command-line-args* [*in*])
                (mapcat #(with-open [rdr (io/reader %)]
                           (doall (line-seq rdr))))
                (mapcat #(re-seq #"\w[\w'-]*" %))
                (map str/lower-case)
                (tally))]
  ;; output map in a descending numeric sort of its values
  (doseq [word (sort #(compare (seen %2) (seen %1)) (keys seen))]
    (printf "%5d %s\n" (seen word) word)))


;;-----------------------------
;; Line frequency count
(def seen (atom {}))

(while-<> [file line]
  (swap! seen update-in [(str/lower-case line)] (fnil inc 0)))

(doseq [line (sort #(compare (@seen %2) (@seen %1)) (keys @seen))]
  (printf "%5d %s\n" (@seen line) line))


;; And again in sequence style:
(let [seen (->> (or *command-line-args* [*in*])
                (mapcat #(with-open [rdr (io/reader %)]
                           (doall (line-seq rdr))))
                (map str/lower-case)
                (tally))]
  (doseq [line (sort #(compare (seen %2) (seen %1)) (keys seen))]
    (printf "%5d %s\n" (seen line) line)))
;;-----------------------------


;; @@PLEAC@@_8.4 Reading a File Backwards by Line or Paragraph
;;-----------------------------
(loop [lines (vec (line-seq reader))]
  (when-let [line (peek lines)]
    ;; do something with line
    (recur (pop lines))))
;;-----------------------------
(doseq [line (reverse (line-seq reader))]
  ;; do something with line
  )
;;-----------------------------
(let [lines (vec (line-seq reader))]
  (doseq [i (range (dec (count lines)) -1 -1)]
    (let [line (lines i)]
      ;; do something with line
      )))
;;-----------------------------
;; paragraph-seq was introduced in example headerfy.clj in Section
;; 6.6.  We will not repeat its definition here.
(doseq [paragraph (reverse (paragraph-seq reader))]
  ;; do something with paragraph
  )
;;-----------------------------


;; @@PLEAC@@_8.6 Picking a Random Line from a File
;;-----------------------------
;; Imperative-style solution.
;; while-<> was defined in Section 1.6
(def rand-line (atom nil))
(def linenum (atom 0))
(while-<> [file line]
  (swap! linenum inc)
  (when (zero? (rand-int linenum))
    (reset! rand-line line)))
;; rand-line is the random line

;; Below is a sequence style solution.
(let [[linenum rand-line] (->> (or *command-line-args* [*in*])
                               (mapcat #(with-open [rdr (io/reader %)]
                                          (doall (line-seq rdr))))
                               (keep-indexed (fn [idx line]
                                               (if (zero? (rand-int (inc idx)))
                                                 [(inc idx) line])))
                               (last))]
  ;; rand-line is the random line.  We also kept its line number in
  ;; linenum, but you could leave that out if you didn't want it.
  )
;;-----------------------------
;; Here we will mercilessly throw a bunch of Clojure code at you, but
;; most of it consists of a couple of general-purpose Clojure
;; functions for reading records separated by any separator value that
;; can be assigned to Perl's $/ variable ($INPUT_RECORD_SEPARATOR if
;; you use the English module).  See below for "NON-LIBRARY CODE
;; BEGINS HERE" for the part of the code that assumes these things are
;; included as part of a library.

(require '[clojure.string :as str]
         '[clojure.java.io :as io])


(def ^:dynamic *auto-chomp* true)

(defn read-paragraph-helper
  "Reads and returns a string containing the next 'paragraph' from the
  BufferedReader argument.  Paragraphs are taken to be consecutive
  sequences of non-empty lines separated by one or more empty lines."
  [^java.io.BufferedReader rdr]
  (loop [lines nil
         line (.readLine rdr)]
    (cond
     ;; If we reach end of file, return the lines we have found so
     ;; far, if any, otherwise nil.
     (nil? line) (if lines (apply str lines) nil)
     ;; Skip over empty lines before the paragraph begins
     (and (= line "") (nil? lines)) (recur nil (.readLine rdr))
     ;; Otherwise an empty line is a sign that we reached the end of
     ;; the paragraph we have been reading.
     (= line "") (apply str (conj lines "\n"))
     ;; We found a non-empty line.  Append it to the list of lines in
     ;; the paragraph.
     :else (recur (conj (or lines []) line "\n")
                  (.readLine rdr)))))


(defn read-paragraph
  [^java.io.BufferedReader rdr]
  (if-let [s (read-paragraph-helper rdr)]
    (if *auto-chomp*
      (str/trim-newline s)
      s)))


;; TBD: Can I use *in* as shown below, with fn read-record defined in
;; a separate namespace from where it is called, and *in* will refer
;; to the 'correct' *in*?  If so, why will that always work?  Because
;; *in* is dynamic and per-thread-bound?

;; TBD: Can I call this fn with sep=nil, or will ^CharSequence type
;; hint cause an error in that case?

(defn read-record
  "Like read-line, except it reads a 'record' from the stream that is
  the current value of *in*, where a record ends with the string sep.
  Like Perl, sep is an exact-match string, and the values \"\" and nil
  work like Perl's $/ equal to \"\" or undef, respectively, i.e. sep
  equal to \"\" treats two or more consecutive newlines as the
  separator, and sep equal to nil causes read-record to read the
  entire stream all at once.

  Returns nil if there are no more records to read.

  If the variable *auto-chomp* is true, the separator string will be
  removed from the end of the strings returned, otherwise it will be
  left on."
  ([^CharSequence sep]
     (read-record *in* sep))
  ([^java.io.BufferedReader rdr ^CharSequence sep]
     (cond
      (nil? sep) (slurp rdr)
      (= sep "") (read-paragraph rdr)
     ;; Note: The efficiency of this way of matching the sep string is
     ;; poor if it has repeated characters in it.  Creating a DFA to
     ;; match the string sep would be more efficient in the worst
     ;; case, but this should be efficient enough for the expected
     ;; common cases where characters in sep do not appear often in
     ;; the input stream.
      :else
      (let [sb (StringBuilder.)
            n-1 (dec (count sep))
            sep-last (int (.charAt sep n-1))
            sep-butlast (subs (str sep) 0 n-1)]
        (loop [c (.read rdr)]
          (if (neg? c)
            (if (zero? (.length sb))
              nil
              (str sb))
            (if (and (== c sep-last)
                     (>= (.length sb) n-1)
                     ;; Check whether other n-1 chars at end of sb
                     ;; match sep-butlast
                     (loop [i (int (dec n-1))
                            j (int (dec (.length sb)))]
                       (if (neg? i)
                         true
                         (if (= (.charAt sep-butlast i)
                                (.charAt sb j))
                           (recur (dec i) (dec j))
                           false))))
              (if *auto-chomp*
                ;; return all but sep at the end of sb
                (.substring sb 0 (- (.length sb) n-1))
                (do
                  (.append sb (char c))
                  (.toString sb)))
              (do
                (.append sb (char c))
                (recur (.read rdr))))))))))


(defn record-seq
  "Like line-seq, except it returns a lazy sequence of records
  separated by the string sep.  See read-record documentation for
  special case values of sep.  record-seq is affected by *auto-chomp*
  as read-record is."
  [^java.io.BufferedReader rdr ^CharSequence sep]
  (when-let [rec (read-record rdr sep)]
    (cons rec (lazy-seq (record-seq rdr sep)))))


;; NON-LIBRARY CODE BEGINS HERE
(let [adage (->> [ "/usr/share/games/fortunes" ]
                 (mapcat #(with-open [rdr (io/reader %)]
                            (doall (record-seq rdr "%%\n"))))
                 (keep-indexed (fn [idx line]
                                 (if (zero? (rand-int (inc idx)))
                                   line)))
                 (last))]
  (printf "%s" adage))
;;-----------------------------


;; @@PLEAC@@_8.7 Randomizing All Lines
;;-----------------------------
;; You can use the shuffle from Chapter 4 if you want, but Clojure's
;; built-in one is just fine.
(doseq [line (shuffle (line-seq input))]
  (printf "%s\n" line))
;;-----------------------------
