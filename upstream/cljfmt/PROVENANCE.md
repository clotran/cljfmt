# cljfmt, vendored

`src/cljfmt/core.cljc` and `resources/cljfmt/` are James Reeves's, unmodified,
from `github.com/weavejester/cljfmt` at commit `baab5008032945434cbca23ef5eda516e3ea97b0`,
the `0.16.6` release tag, under the Eclipse Public License 1.0 (`LICENSE`).
Only the formatter itself is here: `config.clj`, `io.clj`, `main.clj`,
`report.clj`, `tool.clj` and `diff.clj` are the JVM command and its file
handling, which clotran's own command replaces.

The derived copy in `../../src/cljfmt/` is what `../../derive` writes from
this file. Do not edit either; a change belongs upstream or in `derive`.
