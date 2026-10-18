# The upstreams, as submodules

Three repositories, each a pointer to an exact commit, nothing copied:

| submodule | repository | commit | release | license |
|---|---|---|---|---|
| `cljfmt` | github.com/weavejester/cljfmt | `baab5008` | 0.16.6 | EPL-1.0 (`../licenses/cljfmt-LICENSE`) |
| `rewrite-clj` | github.com/clj-commons/rewrite-clj | `fd2a1820` | v1.2.50, the release cljfmt 0.16.6 pins | MIT (`../licenses/rewrite-clj-LICENSE`) |
| `tools.reader` | github.com/clojure/tools.reader | `d32920f3` | v1.5.2, the release cljfmt 0.16.6 pins | EPL-1.0 (`../licenses/tools.reader-LICENSE`) |

`../derive` reads them -- cljfmt's `cljfmt/src/cljfmt/core.cljc` and
`cljfmt/resources/cljfmt`, rewrite-clj's `src`, tools.reader's
`src/main/clojure` -- and writes `../src` and `../resources`, which are
committed, so a user of this library needs no submodule: clotran-deps
clones it plain. Re-deriving needs them: `git submodule update --init`.
Taking a new release is moving a pointer and running `../derive`. The
license texts are kept in `../licenses` so the derived files carry them
even where the pointers cannot be followed.
