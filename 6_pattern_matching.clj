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

;; That is equivalent to the following two examples.  The first uses
;; Clojure's re-pattern, and the second explicitly calls the compile()
;; method of java.util.regex.Pattern class.
(def pat1b (re-pattern "\\d+"))   ; equivalent
(def pat1c (java.util.regex.Pattern/compile "\\d+"))

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
(def pat2b (re-pattern "\\\\\\d+\\s+\\S+"))  ; ugh!

;; Another advantage of the #"pattern" syntax is that even if it
;; occurs in a loop, or a function called many times, the regex is
;; 'compiled' only one time, when the file or function containing it
;; is compiled.  This is as opposed to (re-pattern "\\d+"), which
;; causes the regex to be compiled every time the expression is
;; evaluated.

;; If you have a pattern that must change from one invocation of a
;; function to the next, e.g. because the pattern is an argument to
;; the function, and there is a loop that uses the pattern many times,
;; it is more efficient to call re-pattern once, bind the result to a
;; symbol (e.g. using let), and then use that result many times.

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

;; /o Compile pattern only once.   Clojure does this differently.
;;                                 If you use the #"pattern" syntax, Clojure
;;                                 always compiles the regex only once,
;;                                 but as there is no string interpolation,
;;                                 the regex must be constant at compile time.
;;                                 re-pattern causes the regex to be compiled
;;                                 every time it is called.  While there is
;;                                 nothing like Perl's /o, you can move a call
;;                                 to re-pattern before a loop if you know the
;;                                 regex will not change during the loop's
;;                                 execution.

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
;; Note: *file* is the name of the current file in Clojure.  In an
;; executable Clojure file that begins with the following line (see
;; Section 1.6):

;; #! /usr/bin/env clj

;; you can use *file* the way you would use $0 in Perl.

;; strip to basename
(let [progname (str/replace-first *file* #"^.*/" "")]
  ;; ...
  )

;; Make All Words Title-Cased
(let [capword (str/replace word #"\w+" str/capitalize)]
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
;; This is like the Perl "copy b, and then change a", except it
;; doesn't actually modify any strings in place like Perl does.
(let [a (str/replace b #"x" "y")]
  ;; ...
  )

;; Here is one way to achieve the behavior of the Perl code, first
;; finding all matches and counting them, and then doing the
;; replacements.  If you want a faster way, you probably need to write
;; your own using Java calls.  See clojure.string/replace-by for a
;; starting point.
(let [a (count (re-seq #"x" b))    ; count of pattern occurrences goes in a
      b (str/replace b #"x" "y")]  ; b's value replaced with new string
  ;; ...
  )
;;-----------------------------

;; @@PLEAC@@_6.2 Matching Letters
;;-----------------------------
(if (re-find #"^[A-Za-z]+$" var)
  ;; it is purely alphabetic
  )
;;-----------------------------
;; TBD: Does the JVM implement the use of POSIX locale settings?  How?

;; There is the java.util.Locale/setDefault method:
;; http://docs.oracle.com/javase/1.5.0/docs/api/java/util/Locale.html

;; TBD: However, does setting the Locale in that way affect the \W
;; character class in Java regex matching?  Does it affect any of the
;; POSIX \p character classes?
;;-----------------------------

;; @@PLEAC@@_6.3 Matching Words
;;-----------------------------
;; #"\S+"          ; as many non-whitespace characters as possible
;; #"[A-Za-z'-]+"  ; as many letters, apostrophes, and hyphens
;;-----------------------------
;; #"\b([A-Za-z]+)\b"    ; usually best
;; #"\s([A-Za-z]+)\s"    ; fails at ends or w/ punctuation
;;-----------------------------

;; @@PLEAC@@_6.4 Commenting Regular Expressions
;;-----------------------------
;; The Perl Cookbook lists 4 methods for Perl: comments outside the
;; pattern, comments inside the pattern with the /x modifier, comments
;; inside the replacement part of s///, and alternate delimeters.
;;
;; Clojure has comments outside the pattern, and comments inside the
;; pattern as long as they are preceded by (?x).  If the replacement
;; part of a call to str/replace is a function, then that function can
;; have normal Clojure comments in it.  Clojure does not have
;; alternate delimeters.
;; @@INCLUDE@@ include/clojure/ch06/resname.clj
;;-----------------------------
(str/replace s
             #"(?x)       # replace
               \#         #   a pound sign
               (\w+)      #   the variable name
               \#         #   another pound sign
              "
             (fn [[whole-match var-name]]
               (str (eval (read-string var-name)))))
;;-----------------------------
;; You can nest calls to eval as much as you like in Clojure.

;; TBD: I'm not sure why the previous Perl example allows getting only
;; the values of global variables, whereas the second example can use
;; the value of any variables, and whether the Clojure example above
;; could be written differently to access only global, only local, or
;; both kinds of symbols.
;;-----------------------------

;; @@PLEAC@@_6.5 Finding the Nth Occurrence of a Match
;;-----------------------------
;; We'll do this in the Perl explicit loop style below, but first let
;; us do the shortest way, using re-seq to generate a sequence of all
;; matches, and picking out the 3rd item (items are indexed starting
;; at 0, so number 2):
(def s "One fish two fish red fish blue fish")

(def WANT 3)
(let [matches (re-seq #"(?i)(\w+)\s+fish\b" s)]
  ;; Ignore the string that matches the whole pattern by binding it to _
  (if-let [[_ adjective] (nth matches (dec WANT) nil)]
    (printf "The third fish is a %s one.\n" adjective)))

;; Note that re-seq generates a lazy sequence.  It will not match any
;; more than 3 times in our use above, because we never use any
;; element past the third one.

;; If we had left out the last argument nil in the nth call, and there
;; were fewer than WANT matches, nth would throw an exception when
;; asked to find an item past the end of the list.

;; Now here is a way using loop
(def s "One fish two fish red fish blue fish")

(def WANT 3)
(loop [count 0
       matcher (re-matcher #"(?i)(\w+)\s+fish\b" s)]
  (if-let [[_ adjective] (re-find matcher)]
    (if (== (inc count) WANT)
      (printf "The third fish is a %s one.\n" adjective)
      ;; count was not modified by (inc count) above, but matcher is a
      ;; Java object that _is_ modified in place by calling re-find on
      ;; it.
      (recur (inc count) matcher))))

;; Unlike the Perl version, the code above will stop after finding the
;; WANT-th match, because there is no recur executed after the printf
;; is done.

;; If we wanted to be wasteful of compute time and iterate through all
;; of the later matches, even though we are not going to do anything
;; with them, we can ensure that we always recur whether a match is
;; found or not, like so:

(loop [count 0
       matcher (re-matcher #"(?i)(\w+)\s+fish\b" s)]
  (if-let [[_ adjective] (re-find matcher)]
    (do              ; use do two group the following two statements together
      (if (== (inc count) WANT)
        (printf "The third fish is a %s one.\n" adjective))
      (recur (inc count) matcher))))

;; Here is finding the third match by using a repeat count in the
;; pattern.

(re-find #"(?i)(?:\w+\s+fish\s+){2}(\w+)\s+fish" s)
;;-----------------------------
;; using loop
(loop [count 0
       matcher (re-matcher #"PAT" s)]
  (if-let [[_ adjective] (re-find matcher)]
    (do
      ;; whatever you want to do here.  When finished with the loop,
      ;; don't call recur.  To go around again, you must call recur.
      (recur (inc count) matcher))))

;; Clojure has no trailing while built in.  You could write your own
;; with a macro if you like.

;; The loop above is equivalent to the Perl for loop example.

;; To count overlapping matches, change #"PAT" to #"(?=PAT)"
;;-----------------------------
;; The next Perl examples are more like the first Clojure example
;; in this section.
(def pond "One fish two fish red fish blue fish")

;; This time we map the function 'second' over the result of re-seq,
;; to extract out the second item of each match, which is the string
;; that matches the parenthesized group (\w+)

;; using a temporary
(let [colors (map second (re-seq #"(?i)(\w+)\s+fish\b" pond)) ; LAZILY get matches
      color (nth colors 2 nil)]                    ; then the one we want
  ;; ...
  )

;; or without a temporary list
(let [color (nth (map second (re-seq #"(?i)(\w+)\s+fish\b" pond))
                 2 nil)]
  (printf "The third fish in the pond is %s.\n" color))
;;-----------------------------
;; The idx value passed to the fn that is the first argument of
;; keep-indexed starts at 0 for the first item in the sequence, so in
;; this example we need to keep the odd idx valued items, even though
;; we call it evens.
(def s "One fish two fish red fish blue fish")
(let [evens (keep-indexed (fn [idx item] (if (odd? idx) item))
                          (map second (re-seq #"(?i)(\w+)\s+fish\b" s)))]
  (printf "Even numbered fish are %s.\n" (str/join " " evens)))
;; Even numbered fish are two blue.
;;-----------------------------
(def c (atom 0))
(str/replace s #"(?x)
                 \b             # makes next \w more efficient (TBD?)
                 ( \w+ )        # this is what we'll be changing
                 (
                   \s+ fish \b
                 )
                "
             (fn [[all-match g1 g2]]
               (swap! c inc)
               (if (== @c 4)
                 (str "sushi" g2)
                 (str g1 g2))))
;; One fish two fish red fish sushi fish
;;-----------------------------
(def pond "One fish two fish red fish blue fish swim here.")
(let [color (second (last (re-seq #"(?i)\b(\w+)\s+fish\b" pond)))]
  (printf "Last fish is %s.\n" color))
;; Last fish is blue.
;;-----------------------------
;; This seems to me a very strange part of the Perl Cookbook.

;; In an earlier paragraph they give a perfectly reasonable and
;; working way to get the last match of a pattern PAT using a single
;; regex, like this: /.*PAT/.  That will match the last occurrence of
;; PAT, because the .* is greedy and will match as much of the string
;; as possible, including any earlier occurrences of strings that
;; match PAT before the last one.

;; So why do they go to the trouble of showing a much more complex way
;; of doing it with a single regex later?  I know Perl culture
;; celebrates "there is more than one way to do it", but if they are
;; going to show us this way, why not show us how to do it by
;; implementing our own regex matching library?  Yes, I know I am
;; exaggerating the extra complexity they went to here, but it is
;; still weird.
;;-----------------------------

;; @@PLEAC@@_6.6 Matching Multiple Lines
;;-----------------------------
;; In Java and therefore Clojure regex patterns, (?m) at the beginning
;; of the pattern works like Perl's /m at the end of a pattern, and
;; (?s) works like Perl's /s.

;; @@INCLUDE@@ include/clojure/ch06/killtags.clj
;;-----------------------------
;; @@INCLUDE@@ include/clojure/ch06/headerfy.clj
;;-----------------------------
;; There is no Clojure one-liner for this, unless you stretch the
;; definition of one line to include a significantly longer line.
;;-----------------------------
;; This example reuses function paragraph-seq defined in headerfy.clj
;; above.

;; Also, I believe the Perl example has a bug in it, since it goes
;; into an infinite loop if it every finds a paragraph that matches.
;; It should probably be changed so that in addition to the modifiers
;; 'sm' after the regexp #^START(.*?)^END#sm, it should also have 'g'
;; added to them, so that the inner while loop iterates over multiple
;; matches in the same paragraph.  Either that or perhaps the intent
;; was that the inner while should be an if instead of a while.  I've
;; written the Clojure version to match the behavior of a while with
;; the 'g' option added to the regexp.
(def chunk-num (atom 0))

(doseq [file (or *command-line-args* [*in*])]
  (with-open [rdr (io/reader file)]
    (doseq [pgraph (paragraph-seq rdr)]
      ;; (?s) makes . span line boundaries
      ;; (?m) makes ^ match near newlines
      (swap! chunk-num inc)
      (doseq [[whole-match g1] (re-seq #"(?sm)^START(.*?)^END" pgraph)]
        (printf "chunk %d in %s has <<%s>>\n"
                @chunk-num file g1)))))
;;-----------------------------


;; @@PLEAC@@_6.7 Reading Records with a Pattern Separator
;;-----------------------------
(require '[clojure.string :as str])

(let [chunks (str/split (slurp "filename-or-uri") #"pattern")]
  ;; use chunks here
  )
;;-----------------------------

;; Clojure's built-in split does not behave like Perl's split when
;; there are parenthesized capture groups in the regex pattern.
;; Perl's behavior of including these captured strings in the list
;; returned by split can be useful, so implementing that behavior in
;; Clojure would be nice.

;; Here is one implementation, called split-with-capture, that should
;; work like Perl's split, including for arbitrary values of the limit
;; parameter (-1, 0, and any positive value).  It is built on top of
;; re-groups+ first introduced in Section 1.7, and includes an
;; re-find+ function that can be useful on its own, because it returns
;; not only the string matched, but also the part of the string before
;; and after the match, like Perl's $` and $' special variables.

(require '[clojure.string :as str])

(defn re-groups+ [^java.util.regex.Matcher m s]
  (let [gc (. m (groupCount))
        pre (subs s 0 (. m (start)))
        post (subs s (. m (end)))]
    (loop [v [pre] c 0]
      (if (<= c gc)
        (recur (conj v (. m (group c))) (inc c))
        (conj v post)))))

(defn re-find+
  "Returns the next regex match, if any, of string to pattern, using
  java.util.regex.Matcher.find().  Uses re-groups+ to return the
  groups if a match was found, meaning that on a match the return
  value will always be a vector consisting of these strings:

  [ before-match match capture1 capture2 ... after-match ]

  Where capture1, capture2, etc. are strings that matched
  parenthesized capture groups in the pattern, if any."
  [^java.util.regex.Pattern re s]
  (let [m (re-matcher re s)]
    (when (. m (find))
      (re-groups+ m s))))

(defn drop-trailing-empty-strings [result]
  (loop [max (count result)]
    (if (zero? max)
      []
      (if (= "" (result (dec max)))
        (recur (dec max))
        (subvec result 0 max)))))

(defn split-with-capture-core [s re limit]
  (loop [result []
         s s
         c 1]
    (if (or (= s "") (= c limit))
      (conj result s)
      (if-let [matches (re-find+ re s)]
        (let [pre (matches 0)
              capture-groups (subvec matches 2 (dec (count matches)))
              post (peek matches)]
          (recur (apply conj result pre capture-groups) post (inc c)))
        ;; else we are done, and s is the last string to be returned
        (conj result s)))))

(defn split-with-capture
  ([s re]
     (drop-trailing-empty-strings (split-with-capture-core s re nil)))
  ([s re limit]
     (if (zero? limit)
       (split-with-capture s re)
       (split-with-capture-core s re (if (pos? limit) limit nil)))))

;; Assuming all of the above is in a library somewhere, here is the
;; "new code" to get the job at hand done.

(let [chunks (split-with-capture (slurp "filename-or-uri")
                                 #"(?m)^\.(Ch|Se|Ss)$")]
  (printf "I read %d chunks.\n" (count chunks)))
;;-----------------------------


;; @@PLEAC@@_6.8 Extracting a Range of Lines
;;-----------------------------

;; TBD: Consider writing Clojure functions range-inc and range-exc
;; that take the contents of a line/record string as an argument,
;; and/or a function of two arguments (the string and the line/record
;; number) returning a logical true or false value, and behaves like
;; Perl's .. or ... operators.  The syntax would be more verbose than
;; Perl's, but it should work.


;; @@PLEAC@@_6.9 Matching Shell Globs as Regular Expressions
;;-----------------------------
(import '(java.util.regex Pattern Matcher))
(require '[clojure.string :as str])

;; (java.util.regex.Pattern/quote s) is like Perl's "\Q$s" for quoting
;; a string's special characters so that they will match literally.

;; In addition, str/replace when using a pattern to match against
;; gives special meanings to backslash and dollar sign characters in
;; the replacement string.  We use (re-qr replacement-str) to make the
;; replacement string be replaced literally.

(defn re-qr [replacement]
  (Matcher/quoteReplacement replacement))

(defn glob2pat [globstr]
  (let [patmap { "*" ".*",
                 "?" ".",
                 "[" "[",
                 "]" "]" }
        globstr (str/replace globstr #"."
                             (fn [s] (re-qr (or (patmap s)
                                                (Pattern/quote s)))))]
    (str "^" globstr "$")))
;;-----------------------------

;; @@PLEAC@@_6.10 Speeding Up Interpolated Matches
;;-----------------------------
;; Clojure doesn't have interpolated strings or regexes, but you can
;; have regex patterns that vary from one run to the next that are
;; compiled with re-pattern.

;; If you know that a particular regex pattern can vary at run time,
;; but cannot change within a loop, there is nothing like Perl's /o
;; regex modifier, but you can explicitly choose when to call
;; re-pattern, save the result, and use that saved result to avoid
;; unnecessarily recompiling the regex pattern.
(require '[clojure.java.io :as io])

(let [compiled-pat (re-pattern pattern-str)]
  (doseq [line (io/reader *in*)]
    (if (re-find compiled-pat line)
      ;; do something
      )))
;;-----------------------------
;; @@INCLUDE@@ include/clojure/ch06/popgrep1.clj
;;-----------------------------
;; Several of the methods given for Perl to do this seem like kludges
;; to me.  I'm not going to attempt to write Clojure versions of the
;; Perl programs popgrep2, popgrep3, or grepauth, preferring instead
;; for the straightforward Clojure code that is similar to Perl's
;; popgrep4.
;;-----------------------------
;; @@INCLUDE@@ include/clojure/ch06/popgrep4.clj
;;-----------------------------


;; @@PLEAC@@_6.11 Testing for a Valid Pattern
;;-----------------------------
(let [pat (loop []
            (printf "Pattern? ")
            (flush)
            (let [pat-str (read-line)]
              (if-let [pat (try
                             (re-pattern pat-str)
                             (catch java.util.regex.PatternSyntaxException e
                               (printf "INVALID PATTERN\n%s\n" e)
                               false))]
                pat
                (recur))))]
  (printf "(class pat)=%s pat=%s\n" (class pat) pat))
;;-----------------------------
(defn is-valid-pattern [pat-str]
  (try
    (re-pattern pat-str)
    true
    (catch java.util.regex.PatternSyntaxException e
      false)))
;;-----------------------------
;; @@INCLUDE@@ include/clojure/ch06/paragrep.clj
;;-----------------------------
;; I am not aware of any big security holes in allowing the user to
;; specify a pattern that is then given to the re-pattern function.
;; It might take a huge amount of time or memory to compile, but it
;; should not be able to break out of the Java security model without
;; some very exotic and subtle bug in the regex pattern compiling
;; code, or the JVM itself.
;;-----------------------------
;; You could do something similar to the Perl example, but again, if
;; you want to find out if a literal string appears as a substring of
;; another one, Java's java.lang.String indexOf(String str) method
;; should be more efficient than using regex patterns.
(let [safe-pat-str (java.util.regex.Pattern/quote pat-str)]
  (if (re-find (re-pattern safe-pat-str) s)
    (something)))

(let [idx (.indexOf s pat)]
  (if (not= idx -1)
    (something)))
;;-----------------------------
(if (re-find (re-pattern (str "\\Q" pat-str)) str)
  (something))
;;-----------------------------

;; @@PLEAC@@_6.14 Matching from Where the Last Pattern Left Off
;;-----------------------------
;; Iterate through all matches of the pattern \d+ in the string s
(doseq [number (re-seq #"\d+" s)]
  (printf "Found %s\n" number))
;;-----------------------------
(let [n "   49 here"
      n (str/replace n #"\G " "0")]
  (printf "%s\n" n))
00049 here
;;-----------------------------
(doseq [number (re-seq #"\G,?(\d+)" s)]
  (printf "Found number %s\n" number))
;;-----------------------------
;; There is no built-in way in Clojure to cause the regex matching
;; functions to remember where they were on a failed match attempt.
;; It can be implemented by calling the Java functions
;; java.util.regex.Matcher/find(), and remember the end position of
;; each match by calling end().  On a failed match attempt, start the
;; next find() at the last remembered end position by calling find
;; with an integer argument.
;; TBD
;;-----------------------------
;; If you call re-find with a matcher object m created by calling
;; re-matcher, then you can call the java.util.regex.Matcher start()
;; and end() methods on m to get the start and end index of the last
;; successful match, but there are no Clojure wrappers provided for
;; this.  If you use re-find with a pattern and string argument, the
;; matcher is created but not returned, so it is not available to make
;; such calls on it.

;; TBD: I believe there is no API to set the position.  You can
;; achieve a similar effect by searching in a string that is a suffix
;; of the original string you want to search, starting at the desired
;; position.
(let [a "The year 1752 lost 10 days on the 3rd of September"
      m (re-matcher #"\d+" a)]
  (if-let [s (re-find m)]
    (printf "The position in a is %s\n" (. m start))
    (printf "No match found\n")))
;;-----------------------------


;; @@PLEAC@@_6.15 Greedy and Non-Greedy Matches
;;-----------------------------
(require '[clojure.string :as str])

;; greedy pattern
(str/replace s #"(?s)<.*>" "")     ; try to remove tags, very badly

;; non-greedy pattern
(str/replace s #"(?s)<.*?>" "")    ; try to remove tags, still rather badly
;;-----------------------------
;;<b><i>this</i> and <i>that</i> are important</b> Oh, <b><i>me too!</i></b>
;;-----------------------------
(re-find #"(?sx) <b><i>(.*?)</i></b> " s)
;;-----------------------------
#"BEGIN((?:(?!BEGIN).)*)END"
;;-----------------------------
(re-find #"(?sx) <b><i>(  (?: (?!</b>|</i>). )*  ) </i></b> " s)
;;-----------------------------
(re-find #"(?sx) <b><i>(  (?: (?!</[ib]>). )*  ) </i></b> " s)
;;-----------------------------
(re-find #"(?sx)
    <b><i> 
    [^<]*  # stuff not possibly bad, and not possibly the end.
    (?:
 # at this point, we can have '<' if not part of something bad
     (?!  </?[ib]>  )   # what we can't have
     <                  # okay, so match the '<'
     [^<]*              # and continue with more safe stuff
    ) *
    </i></b>
 " s)
;;-----------------------------


;; @@PLEAC@@_6.16 Detecting Duplicate Words
;;-----------------------------
;; TBD: Make a namespace in which read-paragraph, paragraph-seq, and
;; while-<>-graph from example include/clojure/ch06/paragrep.clj are
;; all defined, and can be used from both places.
(while-<>-pgraph [*command-line-args* file pgraph pgraphnum]
  (doseq [[match word] (re-seq
                        #"(?xi)
                \b            # start at a word boundary (begin letters)
                (\S+)         # find chunk of non-whitespace
                \b            # until another word boundary (end letters)
                (
                    \s+       # separated by some whitespace
                    \1        # and that very same chunk again
                    \b        # until another word boundary
                ) +           # one or more sets of those
                         "
                        pgraph)]
    (printf "dup word '%s' at paragraph %d\n" word pgraphnum)))
;;-----------------------------
This is a test
test of the duplicate word finder.
;;-----------------------------
(let [a "nobody"
      b "bodysnatcher"]
  (if-let [[whole-match g1 g2 g3]
           (re-find #"^(\w+)(\w+) \2(\w+)$" (str a " " b))]
    (printf "%s overlaps in %s-%s-%s\n" g2 g1 g2 g3)))
body overlaps in no-body-snatcher
;;-----------------------------
#"^(\w+?)(\w+) \2(\w+)$"
;;-----------------------------
;; @@INCLUDE@@ include/clojure/ch06/prime-pattern.clj
;;-----------------------------
;; solve for 12x + 15y + 16z = 281, maximizing x
(if-let [[whole-match X Y Z] (re-find #"^(o*)\1{11}(o*)\2{14}(o*)\3{15}$"
                                      (apply str (repeat 281 "o")))]
  (let [x (count X) y (count Y) z (count Z)]
    (printf "One solution is: x=%d; y=%d; z=%d.\n" x y z))
  (printf "No solution.\n"))
;; One solution is: x=17; y=3; z=2.
;;-----------------------------
#"^(o+)\1{11}(o+)\2{14}(o+)\3{15}$"
;; One solution is: x=17; y=3; z=2

#"^(o*?)\1{11}(o*)\2{14}(o*)\3{15}$"
;; One solution is: x=0; y=7; z=11.

#"^(o+?)\1{11}(o*)\2{14}(o*)\3{15}$"
;; One solution is: x=1; y=3; z=14.
;;-----------------------------


;; @@PLEAC@@_6.17 Expressing AND, OR and NOT in a Single Pattern
;;-----------------------------
(let [pattern (read-line)]
  (if [re-find (re-pattern pattern) data]
    ;; ...
    ))
;;-----------------------------
#"ALPHA|BETA"
;;-----------------------------
#"(?s)^(?=.*ALPHA)(?=.*BETA)"
;;-----------------------------
#"(?s)ALPHA.*BETA|BETA.*ALPHA"
;;-----------------------------
#"(?s)^(?:(?!PAT).)*$"
;;-----------------------------
#"(?s)(?=^(?:(?!BAD).)*$)GOOD"
;;-----------------------------
(if (not (re-find #"pattern" s))     ; 'ugly' way is only way in Clojure
  (something))
;;-----------------------------
(if (and (re-find #"pat1" s) (re-find #"pat2" s))
  (something))
;;-----------------------------
(if (or (re-find #"pat1" s) (re-find #"pat2" s))
  (something))
;;-----------------------------
;; @@INCLUDE@@ include/clojure/ch06/minigrep.clj
;;-----------------------------
(re-find #"(?s)^(?=.*bell)(?=.*lab)" "labelled")
;;-----------------------------
(and (re-find #"bell" s) (re-find #"lab" s))
;;-----------------------------
(if (re-find #"(?sx)
             ^              # start of string
            (?=             # zero-width lookahead
                .*          # any amount of intervening stuff
                bell        # the desired bell string
            )               # rewind, since we were only looking
            (?=             # and do the same thing
                .*          # any amount of intervening stuff
                lab         # and the lab part
            )
         "                  ; /s means . can match newline
         murray-hill)
  (printf "Looks like Bell Labs might be in Murray Hill!\n"))
;;-----------------------------
(re-find #"(?:^.*bell.*lab)|(?:^.*lab.*bell)" "labelled")
;;-----------------------------
(let [brand "labelled"]
  (if (re-find #"(?sx)
        (?:                 # non-capturing grouper
            ^ .*?           # any amount of stuff at the front
              bell          # look for a bell
              .*?           # followed by any amount of anything
              lab           # look for a lab
          )                 # end grouper
    |                       # otherwise, try the other direction
        (?:                 # non-capturing grouper
            ^ .*?           # any amount of stuff at the front
              lab           # look for a lab
              .*?           # followed by any amount of anything
              bell          # followed by a bell
          )                 # end grouper
    "                       ; /s means . can match newline
               brand)
    (printf "Our brand has bell and lab separate.\n")))
;;-----------------------------
(re-find #"(?s)^(?:(?!waldo).)*$" map)
;;-----------------------------
(if (re-find #"(?sx)
        ^                   # start of string
        (?:                 # non-capturing grouper
            (?!             # look ahead negation
                waldo       # is he ahead of us now?
            )               # is so, the negation failed
            .               # any character (cuzza /s)
        ) *                 # repeat that grouping 0 or more
        $                   # through the end of the string
    "                       ; /s means . can match newline
             map)
  (printf "There's no waldo here!\n"))
;;-----------------------------
 7:15am  up 206 days, 13:30,  4 users,  load average: 1.04, 1.07, 1.04

USER     TTY      FROM              LOGIN@  IDLE   JCPU   PCPU  WHAT

tchrist  tty1                       5:16pm 36days 24:43   0.03s  xinit

tchrist  tty2                       5:19pm  6days  0.43s  0.43s  -tcsh

tchrist  ttyp0    chthon            7:58am  3days 23.44s  0.44s  -tcsh

gnat     ttyS4    coprolith         2:01pm 13:36m  0.30s  0.30s  -tcsh
;;-----------------------------
;; % w | minigrep.clj '^(?!.*ttyp).*tchrist'
;;-----------------------------
#"(?x)
    ^                       # anchored to the start
    (?!                     # zero-width look-ahead assertion
        .*                  # any amount of anything (faster than .*?)
        ttyp                # the string you don't want to find
    )                       # end look-ahead negation; rewind to start
    .*                      # any amount of anything (faster than .*?)
    tchrist                 # now try to find Tom
"
;;-----------------------------
;; % w | grep tchrist | grep -v ttyp
;;-----------------------------
;; % grep -i 'pattern' files
;; % minigrep.clj '(?i)pattern' files
;;-----------------------------


;; @@PLEAC@@_6.20 Matching Abbreviations
;;-----------------------------
(let [answer (read-line)
      answer-pat (re-pattern (str "(?i)^\\Q" answer))]
  (cond (re-find answer-pat "SEND")  (printf "Action is send\n")
        (re-find answer-pat "STOP")  (printf "Action is stop\n")
        (re-find answer-pat "ABORT") (printf "Action is abort\n")
        (re-find answer-pat "LIST")  (printf "Action is list\n")
        (re-find answer-pat "EDIT")  (printf "Action is edit\n")))
;;-----------------------------
(defn proper-prefixes [s]
  (map #(subs s 0 %) (range 1 (count s))))

(defn remove-nil-vals [m]
  (select-keys m (filter #(m %) (keys m))))

(defn abbrev
  [str-coll]
  (let [separate-prefix-maps (map #(zipmap (proper-prefixes %) (repeat %))
                                  str-coll)
        ;; Merge the separate maps.  If two maps have the same key
        ;; (prefix) then make that key map to nil in the combined map,
        ;; so we can know to remove it later.  Such a prefix is a
        ;; prefix of more than one string in str-coll.
        merged-prefix-maps (apply merge-with (fn [a b] nil) separate-prefix-maps)]
    (merge (remove-nil-vals merged-prefix-maps)
           ;; Non-abbreviations always get entered, even if they
           ;; aren't unique.
           (zipmap str-coll str-coll))))

(require '[clojure.string :as str])
;; See Section 4.1 for definition of qw

(let [abbrevs (abbrev (qw "send abort list edit"))]
  (loop []
    (if-let [line (do (printf "Action: ") (flush) (read-line))]
      (let [action (abbrevs (str/lower-case line))]
        (printf "Action is %s\n" action)
        (recur)))))
;;-----------------------------
(let [name "abbrev"
      fn (resolve name)]
  (fn (qw "send abort list edit")))
;;-----------------------------
;; assumes that invoke_editor, deliver_message, file and PAGER are
;; defined somewhere else.

;; TBD: It is not clear in the original Perl code how the function
;; that prints "Unknown command" gets the value of $cmd to print.
;; Here it is declared as a function argument, but the argument isn't
;; actually passed in.  Should fix that.
(require '[clojure.java.shell :as sh])
(let [actions { "edit"  invoke_editor,
                "send"  deliver_message,
                "list"  (fn [] (sh/sh PAGER file)),
                "abort" (fn []
                          (printf "See ya!\n") (flush) (System/exit 0)),
                ""      (fn [cmd]
                          (printf "Unknown command: %s\n" cmd)
                          (swap! errors inc))
                }
      abbrevs (abbrev (keys actions))]
  (loop []
    (if-let [line (do (printf "Action: ") (flush) (read-line))]
      (let [action (str/trim line)]
        (if (= action "")
          (recur)
          (do
            ((actions (abbrevs (str/lower-case action))))
            (recur)))))))
;;-----------------------------
(let [abbrevation (str/lower-case action)
      expansion (abbrevs abbreviation)
      fn (actions expansion)]
  (fn))
;;-----------------------------


;; @@PLEAC@@_6.21 Program: urlify
;;-----------------------------
;; % gunzip -c ~/mail/archive.gz | urlify.clj > archive.urlified
;;-----------------------------
;; % urlify.clj ~/mail/*.inbox > ~/allmail.urlified
;;-----------------------------
;; @@INCLUDE@@ include/clojure/ch06/urlify.clj
;;-----------------------------


;; @@PLEAC@@_6.23 Regular Expression Grabbag
;;-----------------------------
;; Most Perl regexes can be used verbatim in the Java regex
;; implementation.  The most common changes requires are to replace
;; the /imsx suffixes with the corresponding (?imsx) prefix at the
;; beginning of the pattern.  There is no /o in Java or Clojure, since
;; pattern compilation is explicit (either by use of re-pattern, or by
;; the #"pattern" syntax).  There is no /g in Java or Clojure -- in
;; Clojure use re-seq, clojure.string/replace, or an explicit loop for
;; repeated matching.
(re-find #"(?i)^m*(d?c{0,3}|c[dm])(l?x{0,3}|x[lc])(v?i{0,3}|i[vx])$" s)
;;-----------------------------
(require '[clojure.string :as str])
(str/replace-first s #"(\S+)(\s+)(\S+)" "$3$2$1")
;;-----------------------------
(let [m (re-find #"(\w+)\s*=\s*(.*)\s*$" s)]
  (when m
    ;; entire match is (m 0), keyword is (m 1), value is (m 2)
    ))
;;-----------------------------
(re-find #".{80,}" s)
;;-----------------------------
(re-find #"(\d+)/(\d+)/(\d+) (\d+):(\d+):(\d+)" s)
;;-----------------------------
(str/replace s #"/usr/bin" "/usr/local/bin")
;;-----------------------------
(str/replace s #"%([0-9A-Fa-f][0-9A-Fa-f])"
             (fn [[whole-match hex]] (str (char (Long/parseLong hex 16)))))
;;-----------------------------
(str/replace s #"(?sx)
    /\*                    # Match the opening delimiter
    .*?                    # Match a minimal number of characters
    \*/                    # Match the closing delimiter
    " "")
;;-----------------------------
(let [s (str/replace-first s #"^\s+" "")
      s (str/replace-first s #"\s+$" "")]
  ;; ...
  )
;; or more succinctly in Clojure
(trim s)
;;-----------------------------
(str/replace s #"\\n" "\n")
;;-----------------------------
(str/replace-first s #"^.*::" "")
;;-----------------------------
(re-find #"^([01]?\d\d|2[0-4]\d|25[0-5])\.([01]?\d\d|2[0-4]\d|25[0-5])\.([01]?\d\d|2[0-4]\d|25[0-5])\.([01]?\d\d|2[0-4]\d|25[0-5])$" s)
;;-----------------------------
(str/replace-first s #"^.*/" "")
;;-----------------------------
(def cols (if-let [[whole-match cols-str]
                   (re-find #":co#(\d+):" 
                            (or (get (System/getenv) "TERMCAP") " "))]
            (Long/parseLong cols-str) 80))
;;-----------------------------
(let [name (str " " *file* " " (str/join " " *command-line-args*))
      name (str/replace name #" /\S+/" " ")]
  ;; ...
  )
;;-----------------------------
;; This appears to be somewhat like Perl's $^O, or $Config{"osname'}
;;(get (System/getProperties) "os.name")
(if (not (re-find #"(?i)linux" (get (System/getProperties) "os.name")))
  (printf "This isn't Linux\n")
  (flush)
  (System/exit 1))
;;-----------------------------
(str/replace s #"\n\s+" " ")
;;-----------------------------
(def nums (re-seq #"\d+\.?\d*|\.\d+" s))
;;-----------------------------
(def capword (re-seq #"\b[^\Wa-z0-9_]+\b" s))
;;-----------------------------
(def lowords (re-seq #"\b[^\WA-Z0-9_]+\b" s))
;;-----------------------------
(def icwords (re-seq #"\b[^\Wa-z0-9_][^\WA-Z0-9_]*\b" s))
;;-----------------------------
(def links (map second (re-seq #"(?si)<A[^>]+?HREF\s*=\s*[\"']?([^'\" >]+?)[ '\"]?>" s)))
;;-----------------------------
(def initial (if-let [[whole-match init] (re-find #"^\S+\s+(\S)\S*\s+\S" s)]
               init ""))
;;-----------------------------
(str/replace s #"\"([^\"]*)\"" "``$1''")
;;-----------------------------
;; while-<>-pgraph was defined in paragrep.clj in Section 6.11
(def sentences (atom []))
(while-<>-pgraph [*command-line-args* file pgraph pgraphnum]
  (let [s (str/replace pgraph #"\n" " ")
        s (str/replace s #" {3,}" "  ")
        newsents (vec (map first (re-seq #"(\S.*?[!?.])(?=  |\Z)" s)))]
    ;; (apply conj [1 2] [3 4]) => [1 2 3 4]
    ;; but (apply conj [1 2] []) => ArityException
    ;; so don't try to do the (appy swap! ...) call if newsents is an
    ;; empty vector.
    (if (not= (count newsents) 0)
      (apply swap! sentences conj newsents))))
;;-----------------------------
(if-let [[whole-match yyyy mm dd] (re-find #"(\d{4})-(\d\d)-(\d\d)" s)]
  ;; ...
  )
;;-----------------------------
(re-find #"(?x)
      ^
      (?:
       1 \s (?: \d\d\d \s)?            # 1, or 1 and area code
       |                               # ... or ...
       \(\d\d\d\) \s                   # area code with parens
       |                               # ... or ...
       (?: \+\d\d?\d? \s)?             # optional +country code
       \d\d\d ([\s\-])                 # and area code
      )
      \d\d\d (\s|\1)                   # prefix (and area code separator)
      \d\d\d\d                         # exchange
        $"
         s)
;;-----------------------------
(re-find #"(?i)\boh\s+my\s+gh?o(d(dess(es)?|s?)|odness|sh)\b" s)
;;-----------------------------
;; The 'take-while identity' part causes lines to stop on the first
;; element of input-line-seq for which re-find does not find a match.
(let [lines (take-while identity
                        (map #(second
                               (re-find #"^([^\012\015]*)(\012\015?|\015\012?)"
                                        %))
                             input-line-seq))]
  ;; ...
  )
;;-----------------------------
