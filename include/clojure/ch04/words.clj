#! /usr/bin/env clj

(require '[clojure.java.io :as io])
(require '[clojure.string :as str])

;; words - gather lines, present in columns

;; TBD: I don't know if Clojure or the JVM have a way of doing an
;; ioctl call on Linux without using native calls from Java.

(def cols 80)

;; Note: Trying to write code like the following, except without the
;; doall, that returns a line-seq from inside of (with-open ...)
;; causes an exception about the stream being closed, because we do
;; not try to access the elements of the line-seq sequence until after
;; the io/reader stream is already closed.

;; With doall, it forces the sequence to be evaluated while the file
;; is open.

(defn file-lines [fname]
  (with-open [rdr (io/reader fname)]
    (doall (line-seq rdr))))

;; First gather up every line of input, remembering the longest line
;; length seen.
(let [data (->> *command-line-args*
                 (map file-lines)
                 (apply concat)
                 (map #(str/replace-first % #"\s+$" ""))
                 vec)
      maxlen (apply max (map count data))
      maxlen (inc maxlen)

      ;; determine boundaries of screen
      cols (max 1 (quot cols maxlen))
      rows (quot (+ (dec (count data)) cols) cols)

      ;; pre-create mask for faster computation
      mask (format "%%-%ds " (dec maxlen))

      ;; function to check whether at last item on line
      EOL (fn [item] (== (mod (inc item) cols) 0))]

  ;; now process each item, picking out proper piece for this position
  (dotimes [item (* rows cols)]
    (let [target (+ (* (mod item cols) rows) (quot item cols))
          piece (format mask (if (< target (count data)) (data target) ""))
          piece (if (EOL item) (str/replace piece #"\s+$" "") piece)  ; don't blank-pad to EOL
          ]
      (print piece))
    (when (EOL item)
      (println)))

  ;; finish up if needed
  (when (EOL (* rows cols))
    (println)))
