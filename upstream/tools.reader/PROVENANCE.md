# tools.reader, vendored

`src/clojure/tools/reader/` is Clojure's tools.reader, unmodified, from
`github.com/clojure/tools.reader` at commit `d32920f3c7b2` (`v1.5.2`, the
release cljfmt 0.16.6 pins), under the Eclipse Public License 1.0
(`LICENSE`). It is the JVM reader, in `.clj`: the derived copy in
`../../src/clojure/tools/reader/` is what `../../derive` writes, each host
class in clotran's name, as clotran derives `core.clj` itself. The
ClojureScript port tools.reader also carries is not used: clotran runs the
Clojure one, as it runs Clojure's own core.
