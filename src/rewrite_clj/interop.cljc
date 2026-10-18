(ns ^:no-doc rewrite-clj.interop
  #?(:cljs (:require [goog.string :as gstring]
                     goog.string.format)))

#?(:cljf   (set! *warn-on-reflection* true) :clj   (set! *warn-on-reflection* true))

(defn simple-format
  "Interop version of string format
  Note that there a big differences between host's format and Google Closure's format - we don't address them.
  %d and %s are known to work in both."
  [template & args]
  #?(:cljf   (apply format template args) :clj   (apply format template args)
     :cljs (apply gstring/format template args)))

(defn str->int
  [s]
  #?(:cljf   (clotran.host.int8/parseLong s) :clj   (clotran.host.int8/parseLong s)
     :cljs (js/parseInt s)))

(defn int->str
  [n base]
  #?(:cljf   (.toString (biginteger n) base) :clj   (.toString (biginteger n) base)
     :cljs (.toString n base)))

(defn min-int[]
  #?(:cljf   clotran.host.int8/MIN_VALUE :clj    clotran.host.int8/MIN_VALUE
     :cljs js/Number.MIN_SAFE_INTEGER))

(defn max-int[]
  #?(:cljf   clotran.host.int8/MAX_VALUE :clj   clotran.host.int8/MAX_VALUE
     :cljs js/Number.MAX_SAFE_INTEGER))

(defn clojure-whitespace?
  [#?(:cljf   ^clotran.host.char c :clj   ^clotran.host.char c :default c)]
  #?(:cljf   (and c (or (= c \,) (clotran.host.char/isWhitespace c))) :clj   (and c (or (= c \,) (clotran.host.char/isWhitespace c)))
     :cljs (and c (< -1 (.indexOf #js [\return \newline \tab \space ","] c)))))

(defn meta-available?
  [data]
  #?(:cljf   (instance? clotran.host.i_meta data) :clj   (instance? clotran.host.i_meta data)
     :cljs (implements? IWithMeta data)))

(defn numeric?
  "Checks whether a given character is numeric

  Cribbed from clojure/cljs.tools.reader.impl.util."
  [^clotran.host.char ch]
  (when ch
    #?(:cljf   (clotran.host.char/isDigit ch) :clj   (clotran.host.char/isDigit ch)
       :cljs (gstring/isNumeric ch))))
