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
