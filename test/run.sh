#!/bin/sh
# test/run.sh -- cljfmt on clotran against cljfmt 0.16.6 on clj, byte for
# byte, over a corpus of real files; and ./derive --check. CLOTRAN names the
# clotran checkout (default beside this repository); clj must be on PATH.
# Exits 1 on any difference.
here=$(cd "$(dirname "$0")/.." && pwd)
CLOTRAN=${CLOTRAN:-$here/../clotran}
work=$(mktemp -d) || exit 1
trap 'rm -rf "$work"' EXIT
fails=0
"$here/derive" --check || { echo "FAIL - src/ is not what ./derive writes"; exit 1; }
echo "ok   - src/ is what ./derive writes"
# the corpus: clotran's sources and tests, the examples, a few of Clojure's own
# libraries, and the three vendored libraries' own sources
{ ls "$CLOTRAN"/src/clj/clotran/*.cljf "$CLOTRAN"/test/clj/*.clj "$CLOTRAN"/examples/*.cljf "$CLOTRAN"/examples/*/*.cljf 2>/dev/null
  ls "$CLOTRAN"/src/clj/clojure/walk.clj "$CLOTRAN"/src/clj/clojure/set.clj "$CLOTRAN"/src/clj/clojure/string.clj "$CLOTRAN"/src/clj/clojure/zip.clj
  find "$here/upstream/cljfmt/cljfmt/src" "$here/upstream/rewrite-clj/src" "$here/upstream/tools.reader/src/main/clojure" -name '*.clj' -o -name '*.cljc' | sort; } > "$work/corpus"
# on Windows (MSYS2) clotran and clj are native: the corpus in paths they read
command -v cygpath >/dev/null && { cygpath -m -f "$work/corpus" > "$work/corpus.w" && mv "$work/corpus.w" "$work/corpus"; }
n=$(wc -l < "$work/corpus" | tr -d ' ')
cat > "$work/drive.cljf" <<CLJ
(require 'cljfmt.core 'clojure.string)
(doseq [f (clojure.string/split-lines (slurp (clotran.host.system/getenv "CORPUS")))]
  (let [out (str (clotran.host.system/getenv "OUT") "/" (clojure.string/replace f #"[/:]" "_"))]
    (try (spit out (cljfmt.core/reformat-string (slurp f)))
         (catch clotran.host.throwable e (spit out (str "!error " (.getMessage e)))))))
CLJ
sed -e 's/clotran.host.system\/getenv/System\/getenv/g' -e 's/clotran.host.throwable/Throwable/' "$work/drive.cljf" > "$work/drive.clj"
mkdir -p "$work/ct" "$work/clj"
CORPUS="$work/corpus" OUT="$work/ct" CLOTRAN_PATH="$here/src:$here/resources" "$CLOTRAN/clotran" "$work/drive.cljf" </dev/null >"$work/ct.log" 2>&1 || { echo "FAIL - clotran did not finish:"; tail -5 "$work/ct.log"; exit 1; }
(cd "$work" && CORPUS="$work/corpus" OUT="$work/clj" clj -Sdeps '{:deps {dev.weavejester/cljfmt {:mvn/version "0.16.6"}}}' -M "$work/drive.clj" >"$work/clj.log" 2>&1) || { echo "FAIL - clj did not finish:"; tail -5 "$work/clj.log"; exit 1; }
same=0
while read -r f; do
  b=$(printf '%s' "$f" | tr '/:' '__')
  if cmp -s "$work/clj/$b" "$work/ct/$b"; then same=$((same+1)); else fails=$((fails+1)); echo "FAIL - $f"; diff "$work/clj/$b" "$work/ct/$b" | head -6; fi
done < "$work/corpus"
echo "cljfmt: $same of $n files format byte for byte as on clj"
[ $fails -eq 0 ] && echo "CLJFMT TESTS PASSED" || { echo "CLJFMT TESTS FAILED: $fails"; exit 1; }
