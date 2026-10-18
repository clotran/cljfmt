;;   Copyright (c) Nicola Mometto, Rich Hickey & contributors.
;;   The use and distribution terms for this software are covered by the
;;   Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;;   which can be found in the file epl-v10.html at the root of this distribution.
;;   By using this software in any fashion, you are agreeing to be bound by
;;   the terms of this license.
;;   You must not remove this notice, or any other, from this software.

(ns clojure.tools.reader.impl.commons
  (:refer-clojure :exclude [char])
  (:require [clojure.tools.reader.reader-types :refer [peek-char read-char]]
            [clojure.tools.reader.impl.errors :refer [reader-error]]
            [clojure.tools.reader.impl.utils :refer [numeric? newline? char]])
  (:import 
           
           ))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; helpers
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn number-literal?
  "Checks whether the reader is at the start of a number literal"
  [reader initch]
  (or (numeric? initch)
      (and (or (identical? \+ initch) (identical?  \- initch))
           (numeric? (peek-char reader)))))

(defn read-past
  "Read until first character that doesn't match pred, returning
   char."
  [pred rdr]
  (loop [ch (read-char rdr)]
    (if (pred ch)
      (recur (read-char rdr))
      ch)))

(defn skip-line
  "Advances the reader to the end of a line. Returns the reader"
  [reader]
  (loop []
    (when-not (newline? (read-char reader))
      (recur)))
  reader)

(def ^clotran.host.pattern int-pattern #"([-+]?)(?:(0)|([1-9][0-9]*)|0[xX]([0-9A-Fa-f]+)|0([0-7]+)|([1-9][0-9]?)[rR]([0-9A-Za-z]+)|0[0-9]+)(N)?")
(def ^clotran.host.pattern ratio-pattern #"([-+]?[0-9]+)/([0-9]+)")
(def ^clotran.host.pattern float-pattern #"([-+]?[0-9]+(\.[0-9]*)?([eE][-+]?[0-9]+)?)(M)?")

(defn- match-int
  [^clotran.host.matcher m]
  (if (.group m 2)
    (if (.group m 8) 0N 0)
    (let [negate? (= "-" (.group m 1))
          a (cond
             (.group m 3) [(.group m 3) 10]
             (.group m 4) [(.group m 4) 16]
             (.group m 5) [(.group m 5) 8]
             (.group m 7) [(.group m 7) (clotran.host.int4/parseInt (.group m 6))]
             :else        [nil nil])
          ^clotran.host.string n (a 0)]
      (when n
        (let [bn (clotran.host.bigint. n (int (a 1)))
              bn (if negate? (.negate bn) bn)]
          (if (.group m 8)
            (clotran.host.bigint/fromBigInteger bn)
            (if (< (.bitLength bn) 64)
              (.longValue bn)
              (clotran.host.bigint/fromBigInteger bn))))))))

(defn- match-ratio
  [^clotran.host.matcher m]
  (let [^clotran.host.string numerator (.group m 1)
        ^clotran.host.string denominator (.group m 2)
        numerator (if (.startsWith numerator "+")
                    (subs numerator 1)
                    numerator)]
    (/ (-> numerator   clotran.host.bigint. clotran.host.bigint/fromBigInteger clotran.host.numbers/reduceBigInt)
       (-> denominator clotran.host.bigint. clotran.host.bigint/fromBigInteger clotran.host.numbers/reduceBigInt))))

(defn- match-float
  [^clotran.host.string s ^clotran.host.matcher m]
  (if (.group m 4)
    (clotran.host.decimal. ^clotran.host.string (.group m 1))
    (clotran.host.real8/parseDouble s)))

(defn match-number [^clotran.host.string s]
  (let [int-matcher (.matcher int-pattern s)]
    (if (.matches int-matcher)
      (match-int int-matcher)
      (let [float-matcher (.matcher float-pattern s)]
        (if (.matches float-matcher)
          (match-float s float-matcher)
          (let [ratio-matcher (.matcher ratio-pattern s)]
            (when (.matches ratio-matcher)
              (match-ratio ratio-matcher))))))))

(defn parse-symbol
  "Parses a string into a vector of the namespace and symbol"
  [^clotran.host.string token]
  (when-not (or (= "" token)
                (.endsWith token ":")
                (.startsWith token "::"))
    (let [ns-idx (.indexOf token "/")]
      (if-let [^clotran.host.string ns (and (pos? ns-idx)
                               (subs token 0 ns-idx))]
        (let [ns-idx (inc ns-idx)]
          (when-not (== ns-idx (count token))
            (let [sym (subs token ns-idx)]
              (cond
                (re-matches #"[1-9]" sym)
                [ns sym]
                (and (not (numeric? (nth sym 0)))
                     (not (= "" sym))
                     (not (.endsWith ns ":"))
                     (or (= sym "/")
                         (== -1 (.indexOf sym "/"))))
                [ns sym]))))
        (when (or (= token "/")
                  (== -1 (.indexOf token "/")))
          [nil token])))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; readers
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn read-comment
  [rdr & _]
  (skip-line rdr))

(defn throwing-reader
  [msg]
  (fn [rdr & _]
    (reader-error rdr msg)))
