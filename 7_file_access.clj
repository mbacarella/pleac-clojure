;; FILE ACCESS
;; @@PLEAC@@_7.0 Introduction
;; -----------------------------
(ns pleac-section-7.0
  (:require [clojure.java.io :as io])
  (:import [java.io BufferedReader FileReader]))

;; The perl version contains an or die "Couldn't open filename: $!"
;; after the file open, but this isn't quite as necessary
;; in languages with exceptions, such as Clojure.
(defn print-blue-lines-in-file [filename]
  (with-open [rdr (io/reader filename)]
    (doseq [line (line-seq rdr)]
      (if (.contains line "blue")
        (println line)))))

;; => (print-blue-lines-in-file "/usr/local/widgets/data")
;; blue
;; blue's
;; bluebell
;; ...
;; -----------------------------
;; The Perl code shows how to use * to cast the STDIN file handle
;; to a scalar variable.  This is unnecessary in Clojure, the
;; stdin reader object is already bound to the name *in*, which
;; can be manipulated like any other symbol.
(def stdin-var *in*)
(mysub stdin-var logfile)
;; -----------------------------

;; Here's another way to do the blue line iterator.
(defn print-blue-lines-in-file [filename]
  (let [compiled-regex #"blue"]
    (with-open [rdr (io/BufferedReader. (io/FileReader. filename))]
      (doseq [line (line-seq rdr)]
        (when-not (re-find compiled-regex line)
          (printf "%s\n" line))))))

(defn print-digital-lines-from-stdin []
  (let [digit-regex "#\d"]
    (doseq [line (line-seq (io/reader *in*))]
      (if (re-find digit-regex)
        (printf "Read: %s\n" line)
        (println *err* "No digit found.")))))
;; -----------------------------
;; The Perl code shows how to assign a file handle to LOGFILE...
(def log-file-handle (io/writer "/tmp/log" :append true))
;; -----------------------------
(.close log-file-handle)
;; -----------------------------
;; ... but because unlike Perl, flow-control in Clojure can be
;; interupted with conditions and exceptions, it's a good idea to
;; use the with-open macro to ensure the file handle is closed.
(with-open [log-file-handle (io/writer "/tmp/log" :append true)]
  ;; do stuff with log-file-handle.  Log-file-handle wlll be closed when
  ;; evaluation of this expression terminates.
  )
;; -----------------------------

;; The with-out-str macro redirects everything written to *out*
;; into a string.
;; mbac: rewrite as a macro so calling is less awkward.
(defn append-stdout-to-file [f filename]
  (with-open [fh (io/writer filename :append true)]
    (.write fh (with-out-str (f)))))
;; => (append-stdout-to-file (fn [] (printf "foo bar baz\n")) "/tmp/log.txt")

;; @@PLEAC@@_7.1 Opening a File
;; -----------------------------
(ns pleac-section-7.1
  (:require [clojure.java.io :as io])
  (:import [java.io BufferedReader FileReader]))

;; open PATH for reading
(def source (io/reader path))

;; open PATH for writing
(def sink (io/writer path))

;; ----------------------------

;; The Perl code makes POSIX open for read and open for write calls
;; which aren't directly accessible from Clojure.
;; mbac: maybe find a UNIX/POSIX module?

;; -----------------------------

;; The Perl code shows how to open files through an object oriented
;; interface to contrast the file handle interface.  Clojure already uses
;; objects.

;; mbac: maybe we can show interesting file openers from contrib instead?

;; -----------------------------
;; sysopen(FILEHANDLE, $name, $flags)         or die "Can't open $name : $!";
;; sysopen(FILEHANDLE, $name, $flags, $perms) or die "Can't open $name : $!";
;; #-----------------------------
;; open(FH, "< $path")                                 or die $!;
;; sysopen(FH, $path, O_RDONLY)                        or die $!;
;; #-----------------------------
;; open(FH, "> $path")                                 or die $!;
;; sysopen(FH, $path, O_WRONLY|O_TRUNC|O_CREAT)        or die $!;
;; sysopen(FH, $path, O_WRONLY|O_TRUNC|O_CREAT, 0600)  or die $!;
;; #-----------------------------
;; sysopen(FH, $path, O_WRONLY|O_EXCL|O_CREAT)         or die $!;
;; sysopen(FH, $path, O_WRONLY|O_EXCL|O_CREAT, 0600)   or die $!;
;; #-----------------------------
;; open(FH, ">> $path")                                or die $!;
;; sysopen(FH, $path, O_WRONLY|O_APPEND|O_CREAT)       or die $!;
;; sysopen(FH, $path, O_WRONLY|O_APPEND|O_CREAT, 0600) or die $!;
;; #-----------------------------
;; sysopen(FH, $path, O_WRONLY|O_APPEND)               or die $!;
;; #-----------------------------
;; open(FH, "+< $path")                                or die $!;
;; sysopen(FH, $path, O_RDWR)                          or die $!;
;; #-----------------------------
;; sysopen(FH, $path, O_RDWR|O_CREAT)                  or die $!;
;; sysopen(FH, $path, O_RDWR|O_CREAT, 0600)            or die $!;
;; #-----------------------------
;; sysopen(FH, $path, O_RDWR|O_EXCL|O_CREAT)           or die $!;
;; sysopen(FH, $path, O_RDWR|O_EXCL|O_CREAT, 0600)     or die $!;
;; #-----------------------------
@@INCOMPLETE@@
