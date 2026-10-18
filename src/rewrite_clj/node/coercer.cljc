(ns ^:no-doc rewrite-clj.node.coercer
  "Notes:
   - Coercion of records handled specially due different behaviors on different hosts.
     Must be in equality of specificity of in our protocol extensions.

     From https://clojure.org/reference/protocols:

       if one interface is derived from the other, the more derived is used,
       else which one is used is unspecified"
  (:require
   #?@(:cljf   [[rewrite-clj.node.comment]
        [rewrite-clj.node.fn]
        [rewrite-clj.node.forms]
        [rewrite-clj.node.integer]
        [rewrite-clj.node.keyword :refer [keyword-node]]
        [rewrite-clj.node.meta :refer [meta-node]]
        [rewrite-clj.node.namespaced-map]
        [rewrite-clj.node.protocols :as node :refer [NodeCoerceable coerce]]
        [rewrite-clj.node.quote]
        [rewrite-clj.node.reader-macro :refer [reader-macro-node var-node]]
        [rewrite-clj.node.regex :refer [regex-node pattern-string-for-regex]]
        [rewrite-clj.node.seq :refer [vector-node list-node set-node map-node]]
        [rewrite-clj.node.stringz :as stringz]
        [rewrite-clj.node.token :refer [token-node]]
        [rewrite-clj.node.uneval]
        [rewrite-clj.node.whitespace :as ws]
        [rewrite-clj.parser.impl :as pimpl]] :clj  
       [[rewrite-clj.node.comment]
        [rewrite-clj.node.fn]
        [rewrite-clj.node.forms]
        [rewrite-clj.node.integer]
        [rewrite-clj.node.keyword :refer [keyword-node]]
        [rewrite-clj.node.meta :refer [meta-node]]
        [rewrite-clj.node.namespaced-map]
        [rewrite-clj.node.protocols :as node :refer [NodeCoerceable coerce]]
        [rewrite-clj.node.quote]
        [rewrite-clj.node.reader-macro :refer [reader-macro-node var-node]]
        [rewrite-clj.node.regex :refer [regex-node pattern-string-for-regex]]
        [rewrite-clj.node.seq :refer [vector-node list-node set-node map-node]]
        [rewrite-clj.node.stringz :as stringz]
        [rewrite-clj.node.token :refer [token-node]]
        [rewrite-clj.node.uneval]
        [rewrite-clj.node.whitespace :as ws]
        [rewrite-clj.parser.impl :as pimpl]]
       :cljs
       [[clojure.string :as string]
        [rewrite-clj.node.comment :refer [CommentNode]]
        [rewrite-clj.node.fn :refer [FnNode]]
        [rewrite-clj.node.forms :refer [FormsNode]]
        [rewrite-clj.node.integer :refer [IntNode]]
        [rewrite-clj.node.keyword :refer [KeywordNode keyword-node]]
        [rewrite-clj.node.meta :refer [MetaNode meta-node]]
        [rewrite-clj.node.namespaced-map :refer [NamespacedMapNode MapQualifierNode]]
        [rewrite-clj.node.protocols :as node :refer [NodeCoerceable coerce]]
        [rewrite-clj.node.quote :refer [QuoteNode]]
        [rewrite-clj.node.reader-macro :refer [ReaderNode ReaderMacroNode DerefNode reader-macro-node var-node]]
        [rewrite-clj.node.regex :refer [RegexNode regex-node pattern-string-for-regex]]
        [rewrite-clj.node.seq :refer [SeqNode vector-node list-node set-node map-node]]
        [rewrite-clj.node.stringz :as stringz :refer [StringNode]]
        [rewrite-clj.node.token :refer [TokenNode SymbolNode token-node]]
        [rewrite-clj.node.uneval :refer [UnevalNode]]
        [rewrite-clj.node.whitespace :refer [WhitespaceNode CommaNode NewlineNode] :as ws]
        [rewrite-clj.parser.impl :as pimpl]])
   [rewrite-clj.reader :as reader])
  #?(:cljf   (:import [rewrite_clj.node.comment CommentNode]
              [rewrite_clj.node.fn FnNode]
              [rewrite_clj.node.forms FormsNode]
              [rewrite_clj.node.integer IntNode]
              [rewrite_clj.node.keyword KeywordNode]
              [rewrite_clj.node.meta MetaNode]
              [rewrite_clj.node.namespaced_map NamespacedMapNode MapQualifierNode]
              [rewrite_clj.node.quote QuoteNode]
              [rewrite_clj.node.reader_macro ReaderNode ReaderMacroNode DerefNode]
              [rewrite_clj.node.regex RegexNode]
              [rewrite_clj.node.seq SeqNode]
              [rewrite_clj.node.stringz StringNode]
              [rewrite_clj.node.token TokenNode SymbolNode]
              [rewrite_clj.node.uneval UnevalNode]
              [rewrite_clj.node.whitespace WhitespaceNode CommaNode NewlineNode]) :clj  
     (:import [rewrite_clj.node.comment CommentNode]
              [rewrite_clj.node.fn FnNode]
              [rewrite_clj.node.forms FormsNode]
              [rewrite_clj.node.integer IntNode]
              [rewrite_clj.node.keyword KeywordNode]
              [rewrite_clj.node.meta MetaNode]
              [rewrite_clj.node.namespaced_map NamespacedMapNode MapQualifierNode]
              [rewrite_clj.node.quote QuoteNode]
              [rewrite_clj.node.reader_macro ReaderNode ReaderMacroNode DerefNode]
              [rewrite_clj.node.regex RegexNode]
              [rewrite_clj.node.seq SeqNode]
              [rewrite_clj.node.stringz StringNode]
              [rewrite_clj.node.token TokenNode SymbolNode]
              [rewrite_clj.node.uneval UnevalNode]
              [rewrite_clj.node.whitespace WhitespaceNode CommaNode NewlineNode])))

#?(:cljf   (set! *warn-on-reflection* true) :clj   (set! *warn-on-reflection* true))


;; ## Rewrite-clj nodes coerce to themselves
;; It is important that all rewrite-clj nodes be specified, else we'll coerce them to records
(extend-protocol NodeCoerceable
  CommaNode          (coerce [v] v)
  CommentNode        (coerce [v] v)
  DerefNode          (coerce [v] v)
  FnNode             (coerce [v] v)
  FormsNode          (coerce [v] v)
  IntNode            (coerce [v] v)
  KeywordNode        (coerce [v] v)
  MapQualifierNode   (coerce [v] v)
  MetaNode           (coerce [v] v)
  NamespacedMapNode  (coerce [v] v)
  NewlineNode        (coerce [v] v)
  QuoteNode          (coerce [v] v)
  ReaderMacroNode    (coerce [v] v)
  ReaderNode         (coerce [v] v)
  RegexNode          (coerce [v] v)
  SeqNode            (coerce [v] v)
  StringNode         (coerce [v] v)
  SymbolNode         (coerce [v] v)
  TokenNode          (coerce [v] v)
  UnevalNode         (coerce [v] v)
  WhitespaceNode     (coerce [v] v))

;; ## Helpers

(defn node-with-meta
  [n value]
  (if #?(:cljf   (instance? clotran.host.i_meta value) :clj   (instance? clotran.host.i_meta value)
         :cljs (satisfies? IWithMeta value))
    (let [mta (node/meta-elided value)]
      (if (empty? mta)
        n
        (meta-node (coerce mta) n)))
    n))

(let [comma (ws/whitespace-nodes ", ")
      space (ws/whitespace-node " ")]
  (defn- map->children
    [m]
    (->> (mapcat
          (fn [[k v]]
            (list* (coerce k) space (coerce v) comma))
          m)
         (drop-last (count comma))
         (vec))))

(defn- record-node
  [m]
  (reader-macro-node
   [(token-node #?(:cljf   (symbol (.getName ^clotran.host.class (class m))) :clj   (symbol (.getName ^clotran.host.class (class m)))
                   :cljs ;; this is a bit hacky, but is one way of preserving original name
                   ;; under advanced cljs optimizations
                   (let [s (pr-str m)]
                     (symbol (subs s 1 (string/index-of s "{"))))))
    (map-node (map->children m))]))

(defn- create-map-node [children]
  (node-with-meta
   (map-node (map->children children))
   children))

;; ## Tokens (and special case for record for cljs)

(extend-protocol NodeCoerceable
  #?(:cljf   clotran.host.keyword :clj   clotran.host.keyword :cljs Keyword)
  (coerce [v]
    (keyword-node v)))

(extend-protocol NodeCoerceable
  #?(:cljf   clotran.host.string :clj   clotran.host.string :cljs string)
  (coerce [v]
    (stringz/string-node (pimpl/read-string-data (reader/string-reader (pr-str v))))))

#?(:cljf   (extend-protocol NodeCoerceable
     clotran.host.value
     (coerce [v]
       (node-with-meta
        (token-node v)
        v))) :clj  
   (extend-protocol NodeCoerceable
     clotran.host.value
     (coerce [v]
       (node-with-meta
        (token-node v)
        v)))
   :cljs
   (extend-protocol NodeCoerceable
     default
     (coerce [v]
       (node-with-meta
        ;; in cljs, this is where we check for a record, in clj it happens under map handling
        (if (record? v)
          (record-node v)
          (token-node v))
        v))))

(extend-protocol NodeCoerceable
  nil
  (coerce [_v]
    (token-node nil)))

;; ## Regex

(extend-protocol NodeCoerceable
  #?(:cljf   clotran.host.pattern :clj   clotran.host.pattern :cljs js/RegExp)
  (coerce [v]
    (regex-node (pattern-string-for-regex v))))

;; ## Seqs

(defn- seq-node
  [f sq]
  (node-with-meta
    (->> (map coerce sq)
         (ws/space-separated)
         (vec)
         (f))
    sq))

(extend-protocol NodeCoerceable
  #?(:cljf   clotran.host.i_persistent_vector :clj   clotran.host.i_persistent_vector :cljs PersistentVector)
  (coerce [sq]
    (seq-node vector-node sq))
  #?(:cljf   clotran.host.i_persistent_list :clj   clotran.host.i_persistent_list :cljs List)
  (coerce [sq]
    (seq-node list-node sq))
  #?(:cljf   clotran.host.lazy_seq :clj   clotran.host.lazy_seq :cljs LazySeq)
  (coerce [sq]
    (seq-node list-node sq))
  #?(:cljf   clotran.host.cons :clj   clotran.host.cons :cljs Cons)
  (coerce [sq]
    (seq-node list-node sq))
  #?(:cljf   clotran.host.i_persistent_set :clj   clotran.host.i_persistent_set :cljs PersistentHashSet)
  (coerce [sq]
    (seq-node set-node sq)))

#?(:cljs
   ;; cljs empty list is special
   (extend-protocol NodeCoerceable
     EmptyList
     (coerce [sq]
       (seq-node list-node sq))))

;; ## Maps

#?(:cljf   (extend-protocol NodeCoerceable
     clotran.host.i_persistent_map
     (coerce [m]
       (if (record? m)
         (node-with-meta (record-node m) m)
         (create-map-node m)))) :clj  
   (extend-protocol NodeCoerceable
     clotran.host.i_persistent_map
     (coerce [m]
       (if (record? m)
         (node-with-meta (record-node m) m)
         (create-map-node m))))
   :cljs
   (do
     (extend-protocol NodeCoerceable
       PersistentHashMap
       (coerce [m] (create-map-node m)))
     (extend-protocol NodeCoerceable
       PersistentArrayMap
       (coerce [m] (create-map-node m)))))

;; ## Vars

(extend-protocol NodeCoerceable
  #?(:cljf   clotran.host.var :clj   clotran.host.var :cljs Var)
  (coerce [v]
    (-> (str v)
        (subs 2)
        (symbol)
        (token-node)
        (vector)
        (var-node))))
