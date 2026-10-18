# cljfmt on clotran

The Clojure community's formatter, [cljfmt](https://github.com/weavejester/cljfmt),
running on [clotran](https://github.com/clotran/clotran): `cljfmt.core/reformat-string`
as it is, byte for byte, so code formatted on clotran looks as code formatted
anywhere else does. It is also the proof that a portable `.cljc` library runs
on clotran: cljfmt's core, [rewrite-clj](https://github.com/clj-commons/rewrite-clj)
under it and Clojure's [tools.reader](https://github.com/clojure/tools.reader)
under that -- some 12,000 lines -- are vendored unmodified in `upstream/`
and derived into `src/` by `./derive`, which adds a `:cljf` branch wherever
a reader conditional has a `:clj` one and writes each host class in
clotran's name. Nothing else is changed (clotran's `DEVIATIONS.md` 33 and
`PACKAGES.md`, vendoring rule 6).

## Using it

In a project's `deps.edn`:

    {:deps {clotran/cljfmt {:git/url "https://github.com/clotran/cljfmt.git"
                            :git/sha "<a commit>"}}}

then

    (require '[cljfmt.core :as fmt])
    (fmt/reformat-string "(defn f [x]\n(+ x\n1))")
    ;=> "(defn f [x]\n  (+ x\n     1))"

`reformat-string` takes cljfmt's options map as its second argument, and
`cljfmt/indents/clojure.clj` under `resources/` is the default indentation,
read as cljfmt reads it (`io/resource`). clotran's language server formats
with it (clotran's PLAN step 36).

## Tests

    test/run.sh      # byte for byte against cljfmt on clj, over a corpus
    scripts/fuzz-format -n 50 -s 1

`test/run.sh` formats a corpus -- clotran's own sources and tests, these
three libraries' own sources -- through this port on clotran and through
cljfmt 0.16.6 on `clj`, and every file must come out identical. The fuzzer
generates code (nested forms, comments, strings, metadata, reader macros,
odd whitespace) and asks the same. `./derive --check` says whether `src/`
is what `derive` writes; both run in clotran's gate when this repository
is beside its checkout.
