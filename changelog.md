<body><h1>Speculoos library changelog</h1><a href="#info">changelog meta</a><section><h3>version 2</h3><p>2024  <br />Brad Losavio (blosavio@sagevisuals.com)<br /><em>Description: </em>Work on addressing comments.<br /><em>Project status: </em><a href="https://github.com/metosin/open-source/blob/main/project-status.md">experimental</a><br /><em>Urgency: </em>low<br /><em>Breaking: </em>yes</p><p><div><em>added functions: </em><code>predicates-without-collections</code>, <code>thoroughly-valid-collections?</code>, <code>thoroughly-valid-scalars?</code></div><div><em>renamed functions: </em><ul><li><code>map-2</code> → <code>map*</code></li><li><code>map-indexed-2</code> → <code>map-indexed*</code></li><li><code>reduce-2</code> → <code>reduce*</code></li><li><code>reduce-indexed-2</code> → <code>reduce-indexed*</code></li><li><code>validate-fn-meta-spec</code> → <code>validate-fn</code></li></ul></div><div><em>moved functions: </em><code>recover-literal-path</code>, <code>reduce-indexed</code></div><div><em>removed functions: </em><code>bazooka-swatting-files</code>, <code>bed-of-procrustes</code>, <code>map*</code>, <code>map-indexed*</code>, <code>nil-out</code>, <code>reduce*</code>, <code>reduce-indexed*</code>, <code>smash-data</code></div><div><em>altered functions: </em><code>instrument</code>, <code>scalars-without-predicates</code>, <code>thoroughly-valid?</code>, <code>validate-collections</code>, <code>validate-fn-meta-spec</code>, <code>validate-fn-with</code>, <code>validate-scalars</code>, <code>validate-set-elements</code></div></p><div><h4>Breaking changes</h4><ul><li><div>Adjusted `scalars-without-predicates` to return a set of all-paths elements, instead of a sequence, to be consistent with `collections-without-predicates`.</div></li><li><div>Re-named functions in collection-functions namespace from `blah-2` to `blah` to emphasize they&apos;re not merely a new version, but that they operate on *any* Clojure collection type, and to be consistent with `fn-in` namespace members.</div></li><li><div>Removed Speculoos project&apos;s internal &apos;fn-in&apos; namespace to an external lib. &apos;fn-in&apos; is a new dependency.</div></li><li><div>Moved `recover-literal-path` and helper functions from utility namespace to core namespace; moved attendant tests from utility-tests to core-tests. In preparation for adding feature to `validate-collections`.</div></li><li><div><a href="https://github.com/blosavio/speculoos/issues/1">GitHub Issue #1</a>: Changed validation report key returned `validate-set-elements` from `:datum` to `:datums-set` to emphasize that *every* datum in the set is validated.</div></li><li><div>Removed un-serious utility functions of dubious usefulness.</div></li><li><div>Removed function arg-vs-ret scalar/collection validation feature from `validate-fn-with`. It was too complex, and difficult to use.</div></li><li><div>Re-named `validate-fn-meta-spec` to `validate-fn`.</div></li></ul><h4>Non-breaking changes</h4><ul><li><div>Added `thoroughly-valid-scalars?` and `thoroughly-valid-collections?` utilities.</div></li><li><div>Adjusted `thoroughly-valid?` to internally use new functions.</div></li><li><div>Removed hard-coded collection hierarchy derivation and replaced with a `load` from a single file.</div></li><li><div>Expanded docstrings and examples to provide clarity, completeness, and better convey how functions behave and what they&apos;re intended for, especially for a user who looks at the API doc before html documentation.</div></li><li><div>Internalized `reduce-indexed` into &apos;core&apos; namespace, plus unit tests into core-tests. Removed &apos;collection-functions&apos; namespace because Speculoos does not use `map*`, `map-indexed*`, nor `reduce*`.</div></li><li><div>Removed Speculoos project&apos;s own version of the derived collection hierarchy; now refers to the one provided by the &apos;fn-in&apos; dep.</div></li><li><div>Upgraded `validate-collections` report so that entries now indicate the literal path to the collection datum, at keyword `:path-datum`.</div></li><li><div><a href="https://github.com/blosavio/speculoos/issues/2">GitHub Issue #2</a>: Upgraded `validate-scalars` to accept &apos;bare&apos; scalar and &apos;bare&apos; predicate. This is intentionally undocumented behavior, but provided for convenience and least surprise.</div></li><li><div><a href="https://github.com/blosavio/speculoos/issues/1">GitHub Issue #1</a>: Fixed incorrect `:path` reporting in `validate-set-elements`.</div></li><li><div><a href="https://github.com/blosavio/speculoos/issues/3">GitHub Issue #3</a>: Fixed bug in `validate-collections` that validated un-paired collection predicates against `nil`.</div></li><li><div>Added `predicates-without-collections` to complete feature matrix.</div></li><li><div><a href="https://github.com/blosavio/speculoos/issues/4">GitHub Issue #4</a>: Fixed ignored return collection validations in `valdiate-fn-with`.</div></li><li><div>Adjusted `validate-fn-with` to take advantage of `validate-scalars` upgraded ability to validate bare scalars with bare predicates.</div></li><li><div>Added a completely new argument/return relationship validation to `validate-fn-with` that is much cleaner. Old, complex way remains.</div></li><li><div>Added `:canonical` option to `exercise` and `exercise-fn` to use canonical values instead of randomly-generated values.</div></li><li><div><a href="https://github.com/blosavio/speculoos/issues/9">GitHub Issue #9</a>: Fixed `valid-collections?` to properly handle collection predicates that yield truthy/falsey values.</div></li></ul></div><hr /></section><section><h3>version 1</h3><p>2024 July 26<br />Brad Losavio (blosavio@sagevisuals.com)<br /><em>Description: </em>Request for comments.<br /><em>Project status: </em><a href="https://github.com/metosin/open-source/blob/main/project-status.md">experimental</a><br /><em>Urgency: </em>low<br /><em>Breaking: </em>yes</p><p><div><em>added functions: </em><code>lazy-seq?</code>, <code>thoroughly-valid?</code></div><div><em>renamed functions: </em><ul><li><code>collections-without-specs</code> → <code>collections-without-predicates</code></li><li><code>data-with-specs</code> → <code>scalars-with-predicates</code></li><li><code>data-without-specs</code> → <code>scalars-without-predicates</code></li><li><code>valid-collection-spec?</code> → <code>valid-collections?</code></li><li><code>valid-macro-spec?</code> → <code>valid-macro?</code></li><li><code>valid-scalar-spec?</code> → <code>valid-scalars?</code></li><li><code>validate-collection-spec</code> → <code>validate-collections</code></li><li><code>validate-scalar-spec</code> → <code>validate-scalars</code></li></ul></div><div><em>altered functions: </em><code>data-with-specs</code>, <code>data-without-specs</code>, <code>defpred</code>, <code>specs-without-data</code></div></p><div><h4>Breaking changes</h4><ul><li><div>Re-named functions for clarity, correctness, and conciseness.
Also updated documentation to correspond to new function names.</div></li><li><div>Re-naming functions to more accurately convey purpose and action.
`data-without-specs` to `scalars-without-predicates`, `data-with-specs` to
 `scalars-with-predicates`, and `collections-without-specs` to
 `collections-without-predicates`.</div></li></ul><h4>Non-breaking changes</h4><ul><li><div>Upgraded `defpred` predicate definition macro that can inspect
 a function S-expression and create a random sample generator. Also added
 documentation and illustrations.</div></li><li><div>Added `thoroughly-valid?` utility function.</div></li><li><div>Improved `data-without-specs`, `specs-without-data`,
 `data-with-specs` to properly handle scalars within a set.</div></li><li><div>Removed un-needed/redundant api.clj and api.html pages because
they&apos;re automatically generated by Codox.</div></li><li><div>Added a `*max-tries*` dynamic var that governs random sample
 generators.</div></li><li><div>Added tests to verify handling of sequences emitted by
`interpose`, `interleave`, `lazy-cat`, `mapcat`, and `zipmap`.</div></li><li><div>Added `lazy-seq?` utility predicate.</div></li><li><div>Various documentation edits spanning 2024July10 through 2024July26.</div></li></ul></div><hr /></section><section><h3>version 0</h3><p>2024 June 6<br />Brad Losavio (blosavio@sagevisuals.com)<br /><em>Description: </em>Initial public release.<br /><em>Project status: </em><a href="https://github.com/metosin/open-source/blob/main/project-status.md">experimental</a><br /><em>Urgency: </em>low<br /><em>Breaking: </em>no</p><p></p><div><h4>Breaking changes</h4><ul></ul><h4>Non-breaking changes</h4><ul></ul></div><hr /></section><section><h4>Changelog info</h4><p id="info">A human- and machine-readable <code>changelog.edn</code> will accompany each version at the project&apos;s root directory. <code>changelog.edn</code> is tail-appended file constructed from all previous releases, possibly automatically-composed of per-version <code>changelog-v<em>N</em>.edn</code> files in a sub-directory.</p><p>A <code>changelog.md</code> file, intended for display on the web, is generated by a script. This script also contains specifications describing the changelog data.</p><p>Tentative policy: Bug fixes are non-breaking changes.</p></section><p id="page-footer">Copyright © 2024 Brad Losavio.<br />Compiled 2024 November 09.<span id="uuid"><br />d571f801-3b49-4fd9-a5f3-620e034d0a8d</span></p></body>