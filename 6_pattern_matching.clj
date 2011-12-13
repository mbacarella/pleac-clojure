;; @@PLEAC@@_6.0 Introduction
;;-----------------------------
;; Clojure does have functions for matching in strings, and replacing
;; matches in strings.
(require '[clojure.string :as str])

(re-find pattern string)
(str/replace-first string pattern replacement)
;;-----------------------------
;; Clojure does not have special syntax for re-find and str/replace
;; above, but it does have a little bit of special syntax for creating
;; regexes (i.e. regular expressions).

;; You can create a java.util.regex.Pattern by putting it in
;; double-quotes and preceding that with the character #, like this:
(def pat1a #"\d+")

;; This is equivalent to the following, which explicitly calls the
;; compile() method of java.util.regex.Pattern class:
(def pat1b (java.util.regex.Pattern/compile "\\d+"))   ; equivalent

;; Note that it is a bit nicer to use #"pattern", because you don't
;; need to escape \ characters by preceding them with another \.  This
;; is good, because \ characters are fairly common in regexes, and
;; they get tedious to write, and read, with extra occurrences.
;; Compare these, for example:

;; Pattern that matches:
;; \\    a single backslash,
;; \d+   followed by one or more digits,
;; \s+   followed by one or more whitespace characters,
;; \S+   followed by one or more non-whitespace characters
(def pat2a #"\\\d+\s+\S+")

;; Compare that to the following mess if you don't use #"pattern".
;; Every \ in the pattern must be escaped with its own preceding \.
(def pat2b (java.util.regex.Pattern/compile "\\\\\\d+\\s+\\S+"))  ; ugh!

;; On to Clojure code that match the behavior of the Perl examples.

;; Perl: $meadow =~ m/sheep/;   # True if $meadow contains "sheep"
(re-find #"sheep" meadow)   ; true if meadow contains "sheep"

;; Perl: $meadow !~ m/sheep/;   # True if $meadow doesn't contain "sheep"
(not (re-find #"sheep" meadow))  ; true if meadow doesn't contain "sheep"

;; Perl: $meadow =~ s/old/new/; # Replace "old" with "new" in $meadow
;; The Perl example modifies the string meadow in place.  The Clojure
;; example below does not modify meadow, because its value is an
;; immutable string.  It returns a new string that is the same as
;; meadow, except with the first occurrence of "old" replaced with
;; "new", if "old" occurs in meadow, otherwise it returns the original
;; string that is the value of meadow.
(str/replace-first meadow #"old" "new")
;;-----------------------------
;; There is a lot of similarity between special character sequences
;; that can be used in Perl regexes and java.util.regex.Pattern
;; regexes, but they are not identical.  See java.util.regex.Pattern
;; documentation for details.  If you are familiar with Perl 5
;; regexes, see especially the section of the Java docs titled
;; "Comparison to Perl 5".

;; Note that (?i) should be at the beginning to make the whole pattern
;; match case-insensitively.  If you put it in the middle, everything
;; in the pattern from that point onward will match
;; case-insensitively, but nothing before that will.  You can also use
;; (?-i) to turn off case-insensitive matching in the middle, after it
;; has been turned on earlier.
(if (re-find #"(?i)\bovines?\b" meadow)
  (printf "Here be sheep!"))

(def lines1 [ "Fine bovines demand fine toreadors"
              "FINE BOVINES DEMAND FINE TOREADORS"
              "Muskoxen are a polar ovibovine species."
              "Harold, the smart ovine."
              "OVINES!  Ovines!  Wait ...  ovines?  What are they?"
              "Grooviness went out of fashion decades ago." ])

(def patterns1 [ #"(?i)\bovines?\b"
                 #"\bovines?\b(?i)"
                 #"ovine"
                 #"ovine(?i)"
                 #"(?i)ovine"
                 #"o(?i)vine"
                 #"(?i)o(?-i)vine" ])
                
(doseq [pat patterns1
        line lines1]
  (let [found (re-find pat line)]
    (printf "\"%s\" pat %s -> %s\n" line pat found)))
;;-----------------------------
(let [string "good food"
      string (str/replace-first string #"o*" "e")]
  (printf "string='%s'\n" string))
;;-----------------------------
(let [string "ababacaca"
      match (re-find #"(a|ba|b)+(a|ac)+" string)]
  (if match
    (println (match 0))))
;; ababa
;;-----------------------------
;; Here are correspondences between Perl regex modifiers and Java
;; regex modifiers:

;; /i Ignore alphabetic case       (?i) at *beginning* of pattern (or
;;                                 wherever you want ignoring case to
;;                                 begin in the pattern).  Same comment
;;                                 applies for other Java regex modifiers.
;;                                 This is for ignoring case of US-ASCII
;;                                 characters only.  (?u) is for Unicode.

;; /x Ignore most whitespace       (?x)
;;    in pattern and permit
;;    comments

;; /g Global - match/substitute    There is no corresponding modifier
;;    as often as possible         for this in a Java regex.  A different
;;                                 call must be made, e.g. re-seq instead
;;                                 of re-find, or clojure.string/replace
;;                                 instead of clojure.string/replace-first

;; /gc Don't reset search position  TBD: Is there something that
;;     on failed match.             corresponds to this with Java regexes?

;; /s Let . match newline.         (?s)

;; /m Let ^ and $ match next to    (?m)
;;    embedded \n

;; /o Compile pattern only once.   Java and Clojure do this differently,
;;                                 with explicit calls that perform
;;                                 compilation.  TBD: Give Clojure examples
;;                                 with repeated vs. once-only compilation.

;; /e Righthand side of a s///     Clojure clojure.string/replace and
;;    is code to eval              clojure.string/replace-all can take a function
;;                                 as the last argument.  The function
;;                                 takes the matching string as an argument,
;;                                 and returns a string that is the replacement.

;; /ee Righthand side of a s///    TBD: Look for Perl examples, and see if/how
;;     is a string to eval, then   they can be written in Clojure.
;;     run as code, and its return
;;     value eval'led again.
;;-----------------------------
;; Note: If you use the pattern #"(\d+)" here, each element of the
;; sequence returned by re-seq will have a vector of 2 strings.  The
;; first is the entire matching string, the second is the one that
;; matches the first parenthesized group in the pattern.  Since it is
;; good enough in this case to get the strings matched by the entire
;; pattern, don't bother putting it in parentheses.

;; The Perl example implicitly searches for matches within the string
;; $_.  In the example below, we explicitly name the string to search
;; 'string'.
(let [string "Testing 1 time how many 2 numbers 9384592 I can find in the -92 middle."]
  (doseq [num-str (re-seq #"\d+" string)]
    (printf "Found number %s\n" num-str)))
;;-----------------------------
(def numbers (re-seq #"\d+" string))
;;-----------------------------
(let [digits "123456789"
      nonlap (map second (re-seq #"(\d\d\d)" digits))
      yeslap (map second (re-seq #"(?=(\d\d\d))" digits))]
  (printf "Non-overlapping:  %s\n" (str/join " " nonlap))
  (printf "Overlapping:      %s\n" (str/join " " yeslap)))
;;-----------------------------
;; Java and Clojure regexes do not have anything exactly corresponding
;; to Perl's $` and $'.

;; The most straigtforward way to get both of them is to put
;; ^(?s)(.*?)  before a pattern, and (.*)$ after it, and use the
;; strings matched by those parenthesized expressions.  This will work
;; as long as it is OK for . in the original pattern to match newline
;; characters -- otherwise leave out the (?s) and use something like
;; ^((.|\n)*?)  before and ((.|\n)*)$ after.  Or if you happen to know
;; that the string you are matching against won't contain newlines,
;; ^(.*?) and (.*)$ are good enough.

;; If you leave out the ? in ^(.*?), then that pattern will greedily
;; match, possibly causing the original pattern to match starting
;; later in the string than it would if you used only it.

(let [string "And little lambs eat ivy"
      match (re-find #"^(.*?)(l[^s]*s)(.*)$" string)]
  (printf "(%s) (%s) (%s)\n" (match 1) (match 2) (match 3)))

;; Another way is to write a modified version of re-find that uses the
;; re-groups+ defined in Section 1.8.

;; @@PLEAC@@_6.1 Copying and Substituting Simultaneously
;;-----------------------------
;; Because Clojure strings are immutable, there is effectively no
;; choice to edit a string in place, vs. copy it and then edit it.
;; You use clojure.string/replace-first or clojure.string/replace to
;; create a new string with replacements made in the original one, and
;; the original one always remains unchanged.

;; You could write this:
(let [dst src
      dst (str/replace-first dst #"this" "that")]
  ;; ...
  )
;;-----------------------------
;; But you may as well write it this way, unless you prefer the version above:
(let [dst (str/replace-first src #"this" "that")]
  ;; ...
  )
;;-----------------------------
;; Note: *file* is the name of the current file in Clojure.  In
;; executable Clojure file that begin with the line:

;; #! /usr/bin/env clj

;; as described earlier, you can use it the way you would use $0 in
;; Perl.

;; strip to basename
(let [progname (str/replace-first *file* #"^.*/" "")]
  ;; ...
  )

;; Make All Words Title-Cased
(defn title-case [word]
  (str (str/upper-case (subs word 0 1)) (str/lower-case (subs word 1))))

(let [capword (str/replace word #"\w+" title-case)]
  ;; ...
  )

;; /usr/man/man3/foo.1 changes to /usr/man/cat3/foo.1
(let [catpage (str/replace-first manpage #"man(?=\d)" "cat")]
  ;; ...
  )
;;-----------------------------
;; Clojure qw function was defined in Section 4.0
(let [bindirs (qw "/usr/bin /bin /usr/local/bin")
      libdirs (map #(str/replace-first % #"bin" "lib") bindirs)]
  (printf "%s\n" (str/join " " libdirs)))
;;-----------------------------
;; There aren't two syntactically close examples like these that do
;; such different things in Clojure as the original Perl examples.
;;-----------------------------

;; @@PLEAC@@_6.2 Matching Letters
;;-----------------------------
(if (re-find #"^[A-Za-z]+$" var)
  ;; it is purely alphabetic
  )
;;-----------------------------
;; TBD: Does the JVM implement the use of POSIX locale settings?  How?
