#! /usr/bin/env clj

;; resname - change all "foo.bar.com" style names in the input stream
;; into "foo.bar.com [204.148.40.9]" (or whatever) instead

;; TBD: How to do gethostbyname or inet_ntoa in the JVM?

(require '[clojure.java.io :as io]
         '[clojure.string :as str])

(doseq [line (line-seq (io/reader *in*))]
  (printf "%s\n"
          (str/replace
           line
           #"(?x)
    (                       # capture the hostname
        (?:                 # these parens for grouping only
            (?! [-_]  )     # lookahead for neither underscore nor dash
            [\w-] +         # hostname component
            \.              # and the domain dot
        ) +                 # now repeat that whole thing a bunch of times
        [A-Za-z]            # next must be a letter
        [\w-] +             # now trailing domain part
    )                       # end of hostname capture
            "
           (fn [[_ hostname]]
             (str hostname " "
                  (let [addrs (java.net.InetAddress/getAllByName hostname)]
                    (if (not= (count addrs) 0)    ; if we get at least 1 addr
                      (format "[%s]" (str/join ", "    ; format them all
                                               (map (fn [addr]
                                                      (. addr getHostAddress))
                                                    addrs)))
                      "[???]")))))))              ; else mark dubious
