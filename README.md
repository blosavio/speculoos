<a href="https://clojars.org/com.sagevisuals/speculoos"><img src="https://img.shields.io/clojars/v/com.sagevisuals/speculoos.svg" /></a><br /><a href="#setup">Setup</a><br /><a href="https://blosavio.github.io/speculoos/index.html">API</a><br /><a href="#intro">Introduction</a><br /><a href="#mottos">Mottos</a><br /><a href="#mechanics">Mechanics</a><br /><a href="#scalar-validation">Validating Scalars</a><br /><a href="#collection-validation">Validating Collections</a><br /><a href="#valid-thorough">Validation Summaries and Thorough Validations</a><br /><a href="#function-validation">Validating Functions</a><br /><a href="#exercising">Generating Random Samples and Exercising</a><br /><a href="#utilities">Utilities</a><br /><a href="#predicates">Predicates</a><br /><a href="#non-terminating-sequences">Non-terminating Sequences</a><br /><a href="#sets">Sets</a><br /><a href="https://blosavio.github.io/speculoos/diff.html">Comparison to spec.alpha</a><br /><a href="https://blosavio.github.io/speculoos/perhaps_so.html">Perhaps So</a><br /><a href="https://github.com/blosavio/speculoos/tree/main/doc/recipes.clj">Recipes</a><br /><a href="#troubleshooting">Troubleshooting</a><br /><a href="#alternatives">Alternatives</a><br /><a href="#glossary">Glossary</a><br /><a href="https://github.com/blosavio">Contact</a><br /><h1>Speculoos</h1><em>An experiment with Clojure specification literals</em><br /><section id="setup"><h2>Setup</h2><h3>Leiningen/Boot</h3><pre><code>[com.sagevisuals/speculoos &quot;2-SNAPSHOT5&quot;]</code></pre><h3>Clojure CLI/deps.edn</h3><pre><code>com.sagevisuals/speculoos {:mvn/version &quot;2-SNAPSHOT5&quot;}</code></pre><h3>Require</h3><pre><code>(require &apos;[speculoos.core :refer [valid-scalars? valid-collections?]])</code></pre></section><section id="intro"><h2>Introduction</h2><p>Imagine we&apos;d like to know if our <em>Clojure vector contains an integer, then a string, and finally a ratio</em>. One example of that data vector might look like this.</p><pre><code>[42 &quot;abc&quot; 22/7]</code></pre><p>It would be nice if we could write a specification that is shaped like that data.</p><pre><code>[int? string? ratio?]</code></pre><p>Speculoos can validate our data vector with that specification vector.</p><pre><code>(valid-scalars? [42 &quot;abc&quot; 22/7]
&nbsp;               [int? string? ratio?])
;; =&gt; true</code></pre><p>Notice now the specification&apos;s predicate functions in the the lower row line up with the data&apos;s values in the upper row. Integer <code>42</code> pairs with predicate <code>int?</code>, string <code>&quot;abc&quot;</code> pairs with predicate <code>string?</code>, and ratio <code>22/7</code> pairs with predicate <code>ratio?</code>. All three scalar values satisfy their respective predicates, so the validation returns <code>true</code>.</p><p>Now imagine we&apos;d like ensure our <em>Clojure hash-map contains an integer at key <code>:x</code> and a ratio at key <code>:y</code></em>. Something like this.</p><pre><code>{:x 42 :y 22/7}</code></pre><p>We could write a specification map that&apos;s shaped like that data map.</p><pre><code>{:x int? :y ratio?}</code></pre><p>Speculoos can validate our data map with that specification map.</p><pre><code>(valid-scalars? {:x 42, :y 22/7}
&nbsp;               {:x int?, :y ratio?})
;; =&gt; true</code></pre><p>Again, the specification&apos;s predicate functions in the lower row correspond to the data&apos;s values in the upper row. Integer <code>42</code> at key <code>:x</code> pairs with predicate <code>int?</code> also at key <code>:x</code>, while ratio <code>22/7</code> at key <code>:y</code> pairs with predicate <code>ratio?</code> also at key <code>:y</code>. Both scalar values satisfy their respective predicates, so the validation returns <code>true</code>.</p><p>Notice how in both cases, the specifications mimic the shape of the data. The vector&apos;s specification is itself a vector. The map&apos;s specification is itself a map.</p><p>Speculoos can validate any heterogeneous, arbitrarily-nested data collection using specifications composed of plain Clojure collections and functions. In short, Speculoos is an experimental library that aims to perform the same tasks as <a href="https://clojure.org/about/spec"><code>clojure.spec.alpha</code></a> with an intuitive interface that employs flexible and powerful specification literals.</p></section><section id="mottos"><h2>★ Three Mottos</h2><p>When using Speculoos, remember these three Mottos:<ol><li>Validate scalars separately from validating collections.</li><li>Shape the specification to mimic the data.</li><li>Ignore un-paired predicates and un-paired datums.</li></ol></p><p>Speculoos provides functions for validating <a href="#scalar">scalars</a> (integers, strings, booleans, etc.) contained within a heterogeneous, arbitrarily-nested data structure, and another, distinct group of functions for validating properties of those nested <a href="#HANDS">collections</a> (vectors, maps, sets, etc.). Validating scalars separately from validating collections carries several advantages. First, it&apos;s simpler. Libraries that validate scalars and collections with one specification tend to require a mini-language that mixes identities and quantities (e.g., regular expression-like syntax). Modifying, combining, and subsetting those specifications might be non-intuitive. In contrast, by validating scalars separately from collections, Speculoos can consume much simpler specifications composed of regular Clojure data structures containing regular Clojure predicate functions. We can inspect and manipulate those specifications with any familiar Clojure collection-handling function, such as <code>assoc-in</code>. No macros necessary. Second, specifying scalars separately from specifying collections offers mental clarity about what&apos;s going on. Our predicates will only ever apply to a scalar, or to a collection, never both. And our scalar predicate doesn&apos;t have to know anything about the quantity or location of the element. Third, we only need to specify as much, or as little, as necessary. If we only want to validate a few scalars, we aren&apos;t forced to specify a property concerning a collection.</p><p>Speculoos aims to make composing specifications straightforward. To that end, specifications mimic the shape of the data they describe. A Speculoos specification is merely an arrangement of nested vectors, lists, maps, sequences, and sets that contain predicate functions. Those predicates are arranged in a pattern that instruct the validation functions where to apply the predicates. The specification for a vector is a vector. Predicates are applied to the scalars in-order. The specification for a map, is itself a map. Predicates are applied to the scalars at the same key. There&apos;s a nearly one-to-one correspondence between the shape of the data and the shape of the specification. In fact, a solid strategy for creating a specification is to copy-paste the data, delete the contents, and then, using that as a template, replace the elements with predicates. Such a specification is straightforward to peek at by eye — merely evaluate them and they&apos;ll display themselves at our <span class="small-caps">repl</span> — but they&apos;re also amenable to alteration. We can use our favorite Clojure data wrangling functions to tighten, relax, or remove portions of a Speculoos specification. <code>assoc-in</code>, <code>update-in</code>, and <code>dissoc</code> are our friends.</p><p>Speculoos provides flexibility, power, optionality, and re-usability of specifications by ignoring datums that do not have a corresponding predicate in the specification and ignoring predicates that do not have a corresponding datum in the data. Maybe at our job in an assembly line, we only care about some slice of a large chunk of data. Supplying predicates for only a subset of datums allows us to only validate those specified datums while being agnostic towards the other datums. Going in the other direction, maybe somebody shared a giant, sprawling specification that describes a myriad of data about a person, their postal address, their contact info, etc. Because a Speculoos specification is just a data structure with regular predicates, we can, on-the-fly, <code>get-in</code> the portion relevant to postal addresses and apply that to our particular instances of address data. Speculoos lets us specify exactly what elements we&apos;d like to validate. No more, no less.</p></section><section id="mechanics"><h2>Mechanics</h2><p>Knowing a little bit about how Speculoos does its job will greatly help us understand how to use it. First, we need to know on how to address elements contained within a heterogeneous, arbitrarily-nested data structure. Speculoos follows the conventions set by <code>clojure.core/get-in</code>, and extends those conventions where necessary.</p><p>Vectors are addressed by zero-indexed integers.</p><pre><code>           [100 101 102 103]</code><br /><code>indexes --&gt; 0   1   2   3</code></pre><p>Same for lists…</p><pre><code>          &apos;(97 98 99 100)</code><br /><code>indexes --&gt; 0  1  2  3</code></pre><p>…and same for sequences, like <code>range</code>.</p><pre><code>(range 29 33) ;; =&gt; (29 30 31 32)</code><br /><code>indexes -----------&gt; 0  1  2  3</code></pre><p>Maps are addressed by their keys, which are often keywords, like this.</p><pre><code>        {:a 1 :foo &quot;bar&quot; :hello &apos;world}</code><br /><code>keys --&gt; :a   :foo       :hello</code></pre><p>But maps may be keyed by <em>any</em> value, including integers…</p><pre><code>        {0 &quot;zero&quot; 1 &quot;one&quot; 99 &quot;ninety-nine&quot;}</code><br /><code>keys --&gt; 0        1       99</code></pre><p>…or some other scalars…</p><pre><code>        {&quot;a&quot; :value-at-str-key-a &apos;b :value-at-sym-key-b \c :value-at-char-key-c}</code><br /><code>keys --&gt; &quot;a&quot;                     &apos;b                     \c</code></pre><p>…even composite values.</p><pre><code>        {[0] :val-at-vec-0 [1 2 3] :val-at-vec-1-2-3 {} :val-at-empty-map}</code><br /><code>keys --&gt; [0]               [1 2 3]                   {}</code></pre><p>Set elements are addressed by their identities, so they are located at themselves.</p><pre><code>             #{42 :foo true 22/7}</code><br /><code>identities --&gt; 42 :foo true 22/7</code></pre><p>A <em>path</em> is a sequence of indexes, keys, or identities that allow us refer to a single element buried within a nested data structure. For each level of nesting, we add an element to the path sequence. <code>clojure.core/get-in</code> illustrates how this works.</p><pre><code>(get-in [100 101 102 103] [2]) ;; =&gt; 102</code></pre><p>For a vector containing only integers, each element is addressed by a path of length one. To locate integer <code>102</code> in the vector above, the path is <code>[2]</code>.</p><p>If we consider a vector nested within a vector…</p><pre><code>(get-in [100 101 [102 103]] [2]) ;; =&gt; [102 103]</code></pre><p>…that same path <code>[2]</code> now locates the nested vector. To navigate to an integer contained within the nested vector…</p><pre><code>(get-in [100 101 [102 103]] [2 0]) ;; =&gt; 102</code></pre><p>…requires a path of length two: <code>[2 0]</code> where the <code>2</code> addresses the nested vector <code>[102 103]</code> and the <code>0</code> addresses the <code>102</code> within the nested vector. If we have an integer contained within a vector, contained within a vector, contained within a vector, we&apos;d use a path of length three to get that integer.</p><pre><code>(get-in [100 [101 [102]]] [1]) ;; =&gt; [101 [102]]</code><br /><code>(get-in [100 [101 [102]]] [1 1]) ;; =&gt; [102]</code><br /><code>(get-in [100 [101 [102]]] [1 1 0]) ;; =&gt; 102</code></pre><p>The <code>102</code> is buried three levels deep, so we use a path with three entries.</p><p>This system works similarly for maps. Elements contained in un-nested collections are located with a path of length one.</p><pre><code>(get-in {:x 100, :y 101, :z 102} [:z]) ;; =&gt; 102</code></pre><p>In this example, <code>102</code> is located with a path composed of a single key, keyword <code>:z</code>. If we now consider a map nested within another map…</p><pre><code>(get-in {:x 100, :y 101, :z {:w 102}} [:z :w]) ;; =&gt; 102</code></pre><p>…we need a path with two elements. Key <code>:z</code> navigates us to the nested <code>{:w 102}</code> map, and then key <code>:w</code> navigates us to the <code>102</code> within that nested map.</p><p>There&apos;s no restriction on what may be nested in what, so we can nest a map within a vector…</p><pre><code>(get-in [100 101 {:x 102}] [2 :x]) ;; =&gt; 102</code></pre><p>…or nest a vector within a map…</p><pre><code>(get-in {:x 100, :y {:z [101 102]}} [:y :z 1]) ;; =&gt; 102</code></pre><p>…or, if we use a <a href="https://github.com/blosavio/fn-in">modified version</a> of <code>clojure.core/get-in</code>, nest a vector within a map within a list.</p><pre><code>(require &apos;[fn-in.core :refer [get-in*]])</code><br /><br /><code>(get-in* &apos;(100 101 {:x [102]}) [2 :x 0]) ;; =&gt; 102</code></pre><p><code>102</code> is contained in three levels of nesting, so its path is comprised of three pieces.</p><p>Speculoos provides a little machine to enumerate paths for us. When supplied with a heterogeneous, arbitrarily-nested data structure, <code>speculoos.core/all-paths</code> returns a sequence of <code>{:path … :value …}</code> for every element, both scalars and collections.</p><pre><code>(require &apos;[speculoos.core :refer [all-paths]])</code><br /><br /><code>(all-paths [100 101 102])
;; =&gt; [{:path [], :value [100 101 102]}
;;     {:path [0], :value 100}
;;     {:path [1], :value 101}
;;     {:path [2], :value 102}]</code></pre><p>Note: We receive paths for four items, three integers, plus a path to the outer container itself. The root collection always has a path <code>[]</code>. The integer elements each have a path of a single, zero-indexed integer that locates them within the parent vector. Here&apos;s how it works with a map.</p><pre><code>(all-paths {:x 100, :y 101, :z 102})
;; =&gt; [{:path [], :value {:x 100, :y 101, :z 102}}
;;     {:path [:x], :value 100}
;;     {:path [:y], :value 101}
;;     {:path [:z], :value 102}]</code></pre><p>Each of the three integers has a path with a key that locates them within the parent map, and the parent map has a path of <code>[]</code> because it&apos;s the root collection.</p><p>If we supply a nested data structure, the paths reflect that nesting.</p><pre><code>(all-paths [100 101 [102 103]])
;; =&gt; [{:path [], :value [100 101 [102 103]]}
;;     {:path [0], :value 100}
;;     {:path [1], :value 101}
;;     {:path [2], :value [102 103]}
;;     {:path [2 0], :value 102}
;;     {:path [2 1], :value 103}]</code></pre><p>Now, we have six elements to consider: each of the four integers have a path, and both of the collections have a path. The outer parent vector has path <code>[]</code> because it&apos;s the root, and the nested collection is located at path <code>[2]</code>, the third element of the root vector. Let&apos;s look at all the paths of nested maps.</p><pre><code>(all-paths {:x 100, :y 101, :z {:w 102}})
;; =&gt; [{:path [], :value {:x 100, :y 101, :z {:w 102}}}
;;     {:path [:x], :value 100}
;;     {:path [:y], :value 101}
;;     {:path [:z], :value {:w 102}}
;;     {:path [:z :w], :value 102}]</code></pre><p>Again, each of the three integers has a path, and both of the maps have a path, for a total of five paths.</p><p>There is nothing special about integers. <code>all-paths</code> will treat any element, scalar or collection, the same way. <em>Every element has a path.</em> We could replace those integers with functions, un-nested in a vector…</p><pre><code>(all-paths [int? string? ratio?])
;; =&gt; [{:path [], :value [int? string? ratio?]}
;;     {:path [0], :value int?}
;;     {:path [1], :value string?}
;;     {:path [2], :value ratio?}]</code></pre><p>…or nested in a map…</p><pre><code>(all-paths {:x int?, :y string?, :z {:w ratio?}})
;; =&gt; [{:path [],
;;      :value {:x int?,
;;              :y string?,
;;              :z {:w ratio?}}}
;;     {:path [:x], :value int?}
;;     {:path [:y], :value string?}
;;     {:path [:z], :value {:w ratio?}}
;;     {:path [:z :w], :value ratio?}]</code></pre><p>The important principle to remember is this: Every element, scalar and collection, of a heterogeneous, arbitrarily-nested data structure, can be assigned an unambiguous path, regardless of its container type.</p><p>If we ever find ourselves with a nested list on our hands, <code>all-paths</code> has got us covered.</p><pre><code>(all-paths [42 (list &apos;foo &apos;bar &apos;baz)])
;; =&gt; [{:path [], :value [42 (foo bar baz)]}
;;     {:path [0], :value 42}
;;     {:path [1], :value (foo bar baz)}
;;     {:path [1 0], :value foo}
;;     {:path [1 1], :value bar}
;;     {:path [1 2], :value baz}]</code></pre><p>Likewise, sets are indispensable in some situations, so <code>all-paths</code> can handle it.</p><pre><code>(all-paths {:a 42, :b #{:chocolate :vanilla :strawberry}})
;; =&gt; [{:path [], :value {:a 42, :b #{:chocolate :strawberry :vanilla}}}
;;     {:path [:a], :value 42}
;;     {:path [:b], :value #{:chocolate :strawberry :vanilla}}
;;     {:path [:b :strawberry], :value :strawberry}
;;     {:path [:b :chocolate], :value :chocolate}
;;     {:path [:b :vanilla], :value :vanilla}]</code></pre><p>Admittedly, addressing elements in a set can be a little like herding cats, but it&apos;s still useful to have the capability. Wrangling sets merits its own <a href="#sets">dedicated section</a>.</p><p>So what does all this paths business have to do with validation? Speculoos inspects the path of a predicate within a specification in an attempt to pair it with an element in the data. If it <em>can</em> pair a predicate with a datum, it applies the predicate to that datum.</p></section><section id="scalar-validation"><h2>Scalar Validation</h2><p>Let&apos;s return to the English-language specification we saw in the introduction: <em>A vector containing an integer, then a string, then a ratio</em>. Consider the paths of this vector…</p><pre><code>(all-paths [42 &quot;abc&quot; 22/7])
;; =&gt; [{:path [], :value [42 &quot;abc&quot; 22/7]}
;;     {:path [0], :value 42}
;;     {:path [1], :value &quot;abc&quot;}
;;     {:path [2], :value 22/7}]</code></pre><p>…and the paths of this vector…</p><pre><code>(all-paths [int? string? ratio?])
;; =&gt; [{:path [], :value [int? string? ratio?]}
;;     {:path [0], :value int?}
;;     {:path [1], :value string?}
;;     {:path [2], :value ratio?}]</code></pre><p>We see that elements of both share paths. If we keep only the paths to scalars, i.e., we discard the root collections at path <code>[]</code>, each has three elements remaining.<ul><li><code>42</code> and <code>int?</code> both at path <code>[0]</code>, in their respective vectors,</li><li><code>&quot;abc&quot;</code> and <code>string?</code> both at path <code>[1]</code>, and</li><li><code>22/7</code> and <code>ratio?</code> both at path <code>[2]</code>.</li></ul></p><p>Those pairs of scalars and predicates line up nicely, and we could evaluate each pair, in turn.</p><pre><code>(int? 42) ;; =&gt; true</code><br /><code>(string? &quot;abc&quot;) ;; =&gt; true</code><br /><code>(ratio? 22/7) ;; =&gt; true</code></pre><p>All three scalars satisfy their respective predicates that they&apos;re paired with. Speculoos provides a function, <code>validate-scalars</code> that substantially does all that work for us. Given data and a specification that share the data&apos;s shape (Motto #2), <code>validate-scalars</code>:</p><ol id="scalar-algorithm"><li>Runs <code>all-paths</code> on the data, then the specification.</li><li>Removes the collection elements from each, keeping only the scalars in each.</li><li>Removes the scalars in data that lack a predicate at the same path in the specification, and removes the predicates in the specification that lack datums at the same path in the data.</li><li>For each remaining pair of scalar+predicate, applies the predicate to the scalar.</li></ol><p>Let&apos;s see that in action. We invoke <code>validate-scalars</code> with the data vector as the first argument and the specification vector as the second argument.</p><pre><code>(require &apos;[speculoos.core :refer [validate-scalars]])</code><br /><br /><code>(validate-scalars [42 &quot;abc&quot; 22/7]
&nbsp;                 [int? string? ratio?])
;; =&gt; [{:datum 42,
;;      :path [0],
;;      :predicate int?,
;;      :valid? true}
;;     {:datum &quot;abc&quot;,
;;      :path [1],
;;      :predicate string?,
;;      :valid? true}
;;     {:datum 22/7,
;;      :path [2],
;;      :predicate ratio?,
;;      :valid? true}]</code></pre><p>Let&apos;s apply the Mottos to what we just did.<ul><li>Motto #1: The <em>-scalars</em> suffix reminds us that <code>validate-scalars</code> ignores collections. If we examine the report (we&apos;ll look in detail in the next paragraph), the validation yielded only predicates applied to scalars.</li><li>Motto #2: The shape of our specification mimics the data. Because both are vectors whose contents are addressed by integer indexes, <code>validate-scalars</code> was able to make three pairs. Each of the three scalars in the data vector shares a path with a corresponding predicate in the specification vector.</li><li>Motto #3: Every predicate was paired with a datum and <em>vice versa</em>, so validation did not ignore anything.</li></ul></p><p><code>validate-scalars</code> returns a sequence of all the scalars in data that share a path with a predicate in the specification. For each of those pairs, we receive a map containing the <code>:datum</code> scalar element of the data, the <code>:predicate</code> test function element of the specification, the <code>:path</code> addressing each in their respective structures, and the <code>valid?</code> result of applying the predicate function to the datum. From top to bottom:<ul><li>Scalar <code>42</code> at path <code>[0]</code> in the data vector satisfied predicate <code>int?</code> at path <code>[0]</code> in the specification vector,</li><li>scalar <code>&quot;abc&quot;</code> at path <code>[1]</code> in the data vector satisfied predicate <code>string?</code> at path <code>[1]</code> in the specification vector, and</li><li>scalar <code>22/7</code> at path <code>[2]</code> in the data vector satisfied predicate <code>ratio?</code> at path <code>[2]</code> in the specification vector.</li></ul></p><p>What if there&apos;s a length mis-match between the data and the specification? Motto #3 tells us that validation ignores un-paired datums. Let&apos;s look at the <code>all-paths</code> for that situation.</p><pre><code>;; data vector containing an integer, a symbol, and a character</code><br /><code>(all-paths [42 &quot;abc&quot; 22/7])
;; =&gt; [{:path [], :value [42 &quot;abc&quot; 22/7]}
;;     {:path [0], :value 42}
;;     {:path [1], :value &quot;abc&quot;}
;;     {:path [2], :value 22/7}]</code><br /><br /><code>;; specification vector containing one predicate</code><br /><code>(all-paths [int?]) ;; =&gt; [{:path [], :value [int?]}
&nbsp;{:path [0], :value int?}]</code></pre><p>After discarding the root collections at path <code>[]</code> we find the only scalar+predicate pair at path <code>[0]</code>, and that&apos;s the only pair that <code>validate-scalars</code> looks at.</p><pre><code>(validate-scalars [42 &quot;abc&quot; 22/7]
&nbsp;                 [int?])
;; =&gt; [{:datum 42,
;;      :path [0],
;;      :predicate int?,
;;      :valid? true}]</code></pre><p>Only scalar <code>42</code> in the data vector has a corresponding predicate <code>int?</code> in the specification vector, so the validation report contains only one entry. The second and third scalars, <code>&quot;abc&quot;</code> and <code>22/7</code>, are ignored.</p><p>What about the other way around? More predicates in the specification than scalars in the data?</p><pre><code>;; data vector containing one scalar, an integer</code><br /><code>(all-paths [42]) ;; =&gt; [{:path [], :value [42]} {:path [0], :value 42}]</code><br /><br /><code>;; specification vector containing three predicates</code><br /><code>(all-paths [int? string? ratio?])
;; =&gt; [{:path [], :value [int? string? ratio?]}
;;     {:path [0], :value int?}
;;     {:path [1], :value string?}
;;     {:path [2], :value ratio?}]</code></pre><p>Motto #3 reminds us that validation ignores un-paired predicates. Only the predicate <code>int?</code> at path <code>[0]</code> in the specification vector shares its path with a scalar in the data vector, so that&apos;s the only scalar+predicate pair that <code>validate-scalars</code> processes.</p><pre><code>(validate-scalars [42]
&nbsp;                 [int? string? ratio?])
;; =&gt; [{:datum 42,
;;      :path [0],
;;      :predicate int?,
;;      :valid? true}]</code></pre><p><code>validate-scalars</code> ignores both <code>string?</code> and <code>ratio?</code> within the specification vector because the data vector does not contain scalars at their respective paths.</p><p>This principle of ignoring un-paired scalars and un-paired predicates provides some useful features. If we only care about validating the first datum, we could insert only one predicate into the specification and rely on the fact that the remaining un-paired datums are ignores. That pattern offers permissiveness. On the other hand, we could compose a lengthy specification and validate a steadily accreting vector with that single specification. That pattern promotes re-use. See <a href="https://blosavio.github.io/speculoos/perhaps_so.html">Perhaps So</a> for more discussion.</p><p>Validating scalars contained within a map proceeds similarly. Let&apos;s send this map, our data, to <code>all-paths</code>.</p><pre><code>(all-paths {:x 42, :y &quot;abc&quot;, :z 22/7})
;; =&gt; [{:path [], :value {:x 42, :y &quot;abc&quot;, :z 22/7}}
;;     {:path [:x], :value 42}
;;     {:path [:y], :value &quot;abc&quot;}
;;     {:path [:z], :value 22/7}]</code></pre><p>Four elements: the root collection (a map), and three scalars. Then we&apos;ll do the same for this map, our specification, which mimics the shape of the data (Motto #2), by also being a map with the same keys.</p><pre><code>(all-paths {:x int?, :y string?, :z ratio?})
;; =&gt; [{:path [], :value {:x int?, :y string?, :z ratio?}}
;;     {:path [:x], :value int?}
;;     {:path [:y], :value string?}
;;     {:path [:z], :value ratio?}]</code></pre><p>Again four elements: the root collection (a map), and three predicates. Note that each predicate shares a path with one of the scalars in the data map. Invoking <code>validate-scalars</code> with the data map followed by the specification map…</p><pre><code>(validate-scalars {:x 42, :y &quot;abc&quot;, :z 22/7}
&nbsp;                 {:x int?, :y string?, :z ratio?})
;; =&gt; [{:datum 42,
;;      :path [:x],
;;      :predicate int?,
;;      :valid? true}
;;     {:datum &quot;abc&quot;,
;;      :path [:y],
;;      :predicate string?,
;;      :valid? true}
;;     {:datum 22/7,
;;      :path [:z],
;;      :predicate ratio?,
;;      :valid? true}]</code></pre><p>…we can see that</p><ul><li>Scalar <code>42</code> at path <code>[:x]</code> in the data map satisfies predicate <code>int?</code> at path <code>[:x]</code> in the specification map, </li><li>scalar <code>&quot;abc&quot;</code> at path <code>[:y]</code> in the data map satisfies predicate <code>string?</code> at path <code>[:y]</code> in the specification map, and</li><li>scalar <code>22/7</code> at path <code>[:z]</code> in the data map satisfies predicate <code>ratio?</code> at path <code>[:z]</code> in the specification map.</li></ul><p>Let&apos;s apply the Three Mottos.</p><ul><li>Motto #1: <code>validate-scalars</code> ignores every element that is not a scalar, and we wrote our predicates to test only scalars.</li><li>Motto #2: We shaped our specification to mimic the data: we composed our specification to be a map with keys <code>:x</code>, <code>:y</code>, and <code>:z</code>, same as the data map. Because of this mimicry, <code>validate-scalars</code> is able to infer how to apply each predicate to the intended datum.</li><li>Motto #3: Because each predicate in the specification shared a path with each scalar in the data, and because each scalar in the data shared a path with a predicate in the specification, nothing scalars or predicates were ignored.</li></ul><p><code>validate-scalars</code> can only operate with complete scalar+predicate pairs. It ignores un-paired scalars and it ignores un-paired predicates. Since maps are not sequential, we can illustrate both scenarios with one example.</p><pre><code>;; data with keys :x and :q</code><br /><code>(all-paths {:x 42, :q &quot;foo&quot;})
;; =&gt; [{:path [], :value {:q &quot;foo&quot;, :x 42}}
;;     {:path [:x], :value 42}
;;     {:path [:q], :value &quot;foo&quot;}]</code><br /><br /><code>;; specification with keys :x and :s</code><br /><code>(all-paths {:x int?, :s decimal?})
;; =&gt; [{:path [], :value {:s decimal?, :x int?}}
;;     {:path [:x], :value int?}
;;     {:path [:s], :value decimal?}]</code></pre><p>Notice that the two maps contain only a single scalar/predicate that share a path, <code>[:x]</code>. The other two elements, scalar <code>&quot;foo&quot;</code> at path <code>[:q]</code> in the data map and predicate <code>decimal?</code> at path <code>[:s]</code> in the specification map, do not share a path with an element of the other. <code>&quot;foo&quot;</code> and <code>decimal?</code> will be ignored.</p><pre><code>(validate-scalars {:x 42, :q &quot;foo&quot;}
&nbsp;                 {:x int?, :s decimal?})
;; =&gt; [{:datum 42,
;;      :path [:x],
;;      :predicate int?,
;;      :valid? true}]</code></pre><p><code>validate-scalars</code> found only a single complete scalar+predicate pair located at path <code>[:x]</code>, so it applied <code>int?</code> to <code>42</code>, which returns satisfied.</p><p>Again, the principle of ignoring un-paired scalars and ignoring un-paired predicates provides quite a bit of utility. If we are handed a large data map, but we are only interested in the scalar at <code>:x</code>, we are free to validate only that value putting only one predicate at <code>:x</code> in the specification map. Validation ignores all the other stuff we don&apos;t care about. Similarly, perhaps we&apos;ve built a comprehensive specification map that contains keys <code>:a</code> through <code>:z</code>, but for one particular scenario, our data only contains a value at key <code>:y</code>. We can directly use that comprehensive specification un-modified, and <code>validate-scalars</code> will consider only the one paired datum+predicate and ignore the rest.</p><p>Scalars contained in nested collections are treated accordingly: predicates from the specification are only applied to scalars in the data which share their path. The paths are merely longer than one element. Non-scalars are ignored. Here are the paths for a simple nested data vector containing scalars.</p><pre><code>(all-paths [42 [&quot;abc&quot; [22/7]]])
;; =&gt; [{:path [], :value [42 [&quot;abc&quot; [22/7]]]}
;;     {:path [0], :value 42}
;;     {:path [1], :value [&quot;abc&quot; [22/7]]}
;;     {:path [1 0], :value &quot;abc&quot;}
;;     {:path [1 1], :value [22/7]}
;;     {:path [1 1 0], :value 22/7}]</code></pre><p>Six total elements: three vectors, which <code>validate-scalars</code> will ignore, and three scalars.</p><p> And here are the paths for a similarly-shaped nested specification.</p><pre><code>;;                         v --- char? predicate will be notable during validation in a moment</code><br /><code>(all-paths [int? [string? [char?]]])
;; =&gt; [{:path [], :value [int? [string? [char?]]]}
;;     {:path [0], :value int?}
;;     {:path [1], :value [string? [char?]]}
;;     {:path [1 0], :value string?}
;;     {:path [1 1], :value [char?]}
;;     {:path [1 1 0], :value char?}]</code></pre><p>Again, six total elements: three vectors that will be ignored, plus three predicates. When we validate…</p><pre><code>(validate-scalars [42 [&quot;abc&quot; [22/7]]]
&nbsp;                 [int? [string? [char?]]])
;; =&gt; [{:datum 42,
;;      :path [0],
;;      :predicate int?,
;;      :valid? true}
;;     {:datum &quot;abc&quot;,
;;      :path [1 0],
;;      :predicate string?,
;;      :valid? true}
;;     {:datum 22/7,
;;      :path [1 1 0],
;;      :predicate char?,
;;      :valid? false}]</code></pre><p>Three complete pairs of scalars and predicates.<ul><li>Scalar <code>42</code> at path <code>[0]</code> in the data satisfies predicate <code>int?</code> at path <code>[0]</code> in the specification,</li><li>scalar <code>&quot;abc&quot;</code> at path <code>[1 0]</code> in the data satisfies predicate <code>string?</code> at path <code>[1 0]</code> in the specification,</li><li>scalar <code>22/7</code> at path <code>[1 1 0]</code> in the data <strong>does not satisfy</strong> predicate <code>char?</code> at path <code>[1 1 0]</code> in the specification.</li></ul><a href="#valid-thorough">Later</a>, we&apos;ll see that the lone, unsatisfied <code>char?</code> predicate would cause an entire <code>valid?</code> operation to return <code>false</code>.</p><p>When the data contains scalars that are not paired with predicates in the specification, they are not validated.</p><pre><code>(validate-scalars [42 [&quot;abc&quot; [22/7]]]
&nbsp;                 [int? [string?]])
;; =&gt; [{:datum 42,
;;      :path [0],
;;      :predicate int?,
;;      :valid? true}
;;     {:datum &quot;abc&quot;,
;;      :path [1 0],
;;      :predicate string?,
;;      :valid? true}]</code></pre><p>Only the <code>42</code> and <code>&quot;abc&quot;</code> are paired with predicates, so <code>validate-scalars</code> only validated those two scalars. <code>22/7</code> is unpaired, and therefore ignored. Likewise…</p><pre><code>(validate-scalars [42]
&nbsp;                 [int? [string? [char?]]])
;; =&gt; [{:datum 42,
;;      :path [0],
;;      :predicate int?,
;;      :valid? true}]</code></pre><p>…<code>string?</code> and <code>char?</code> are not paired, and therefore ignored. When the data contains only one scalar, but the specification contains more predicates, <code>validate-scalars</code> only validates the complete scalar+predicate pairs.</p><p>Mis-matched, nested maps sing the same song. Here are the paths for all elements in a nested data map and a nested specification map.</p><pre><code>;; data</code><br /><code>(all-paths {:x 42, :y {:z 22/7}})
;; =&gt; [{:path [], :value {:x 42, :y {:z 22/7}}}
;;     {:path [:x], :value 42}
;;     {:path [:y], :value {:z 22/7}}
;;     {:path [:y :z], :value 22/7}]</code><br /><br /><code>;; specification</code><br /><code>(all-paths {:x int?, :y {:q string?}})
;; =&gt; [{:path [], :value {:x int?, :y {:q string?}}}
;;     {:path [:x], :value int?}
;;     {:path [:y], :value {:q string?}}
;;     {:path [:y :q], :value string?}]</code></pre><p>Notice that only the scalar <code>42</code> in the data and the predicate <code>int?</code>  in the specification share a path <code>[:x]</code>. Scalar <code>22/7</code> at path <code>[:y :z]</code> in the data and predicate <code>string?</code> at path <code>[:y :q]</code> in the specification are un-paired because they do not share paths.</p><pre><code>(validate-scalars {:x 42, :y {:z 22/7}}
&nbsp;                 {:x int?, :y {:q string?}})
;; =&gt; [{:datum 42,
;;      :path [:x],
;;      :predicate int?,
;;      :valid? true}]</code></pre><p><code>validate-scalars</code> dutifully evaluates the single scalar+predicate pair, and tells us that <code>42</code> is indeed an integer.</p><p>One final illustration: what happens if there are zero scalar+predicate pairs.</p><pre><code>(validate-scalars {:x 42} {:y int?}) ;; =&gt; []</code></pre><p>The only scalar, at the path <code>[:x]</code> in the data, does not share a path with the only predicate, at path <code>[:y]</code> in the specification. No validations were performed.</p><p>A Speculoos scalar specification says <em>This data element may or may not exist, but if it does, it must satisfy this predicate.</em> See <a href="#valid-thorough"> this later section</a> for functions that return high-level <code>true/false</code> validation summaries and for functions that ensure validation of <em>every</em> scalar element.</p></section><section id="collection-validation"><h2>Collection Validation</h2><p>You may have been uncomfortably shifting in your chair while reading through the examples above. Every example we&apos;ve seen so far shows Speculoos validating individual scalars, such as integers, strings, booleans, etc.</p><pre><code>(valid-scalars? [99 &quot;qwz&quot; -88]
&nbsp;               [int? string? neg-int?])
;; =&gt; true</code></pre><p>However, we might need to specify some property of a collection itself, such as a vector&apos;s length, the presence of a key in a map, relationships <em>between</em> datums, etc. That is <em>collection validation</em>.</p><p>One way to visualize the difference is this. Scalar validation targets…</p><pre><code> v----v-------v------v---- scalar validation targets these things</code><br /><code>[42   \z {:x &apos;foo :y 9.87} ]</code></pre><p>…integers, characters, symbols, etc.</p><p>In contrast, collection validation targets…</p><pre><code>v--------v---------------v-v---- collection validation targets these things</code><br /><code>[42   \z {:x &apos;foo :y 9.87} ]</code></pre><p>…vectors, maps, sequences, lists, and sets.</p><p>One of Speculoos&apos; <a href="#mottos">main concepts</a> is that scalars are specified and validated explicitly separately from collections. You perhaps noticed that the function name we have been using wasn&apos;t <code>validate</code>, but instead <code>validate-scalars</code>. Speculoos provides a distinct group of functions to validate the properties of collections, independent of the scalar values contained within the collection. The collection validation functions are distinguished by a <code>-collections</code> suffix.</p><p>Let&apos;s examine why and how they&apos;re distinct.</p><section id="when-collection-validate"><h3>When to validate collections versus validating scalars</h3><p>So <em>when</em> do we use collection validation instead of scalar validation? Basically, any time we want to verify a property that&apos;s beyond a single scalar.</p><p><strong>Validate a property of the collection itself.</strong> In this section, we&apos;ll often validate the type of the collection.</p><pre><code>(vector? []) ;; =&gt; true</code><br /><code>(vector? {}) ;; =&gt; false</code></pre><p>Those collection type predicates are short, mnemonic, and built-in, but knowing the mere type of a collection perhaps isn&apos;t broadly useful. But maybe we&apos;d like to know how many items a vector contains…</p><pre><code>(&gt;= 3 (count [1 2 3])) ;; =&gt; true</code></pre><p>…or if it contains an even number of elements…</p><pre><code>(even? (count [1 2 3])) ;; =&gt; false</code></pre><p>…or if a map contains a particular key…</p><pre><code>(contains? {:x 42} :y) ;; =&gt; false</code></pre><p>…or maybe if a set contains anything at all.</p><pre><code>(empty? #{}) ;; =&gt; true</code></pre><p>None of those tests are available without access to the whole collection. A scalar validation wouldn&apos;t suffice. We&apos;d need a collection validation.</p><p>Take particular notice: testing the presence or absence of a datum falls under collection validation.</p><p><strong>Validate a relationship between multiple scalars.</strong> Here&apos;s where the lines get blurry. If we&apos;d like to know whether the second element of a vector is greater than the first…</p><pre><code>(&lt; (get [42 43] 0)
&nbsp;  (get [42 43] 1)) ;; =&gt; true</code></pre><p>…or whether each successive value is double the previous value…</p><pre><code>(def doubles [2 4 8 16 32 64 128 256 512])</code><br /><br /><code>(every? #(= 2 %) (map #(/ %2 %1) doubles (next doubles))) ;; =&gt; true</code></pre><p>…it certainly looks at first glance that we&apos;re only interested in the values of the scalars. Where does the concept of a collection come into play? When validating the relationships <em>between</em> scalars, I imagine a double-ended arrow connecting the two scalars with a question floating above the arrow.</p><pre><code>;;    greater-than?</code><br /><code>[42 &lt;---------------&gt; 43]</code></pre><p>Validating a relationship is validating the concept that arrow represents. The relationship arrow is not a fundamental property of a single, underlying scalar, so a scalar validation won&apos;t work. The relationship arrow &apos;lives&apos; in the collection, so validating the relationship arrow requires a collection validation.</p><p>Don&apos;t feel forced to choose between scalar <em>or</em> collection validation. We could do both, reserving each kind of validation for when it&apos;s best suited.</p></section><section id="quick-collection-validation-examples"><h3>Quick examples</h3><p>The upcoming discussion is long and detailed, so before we dive in, let&apos;s look at a few examples of collection validation to give a little motivation to working through the concepts.</p><p>We can validate that <em>a vector contains three elements</em>. We compose that predicate like this.</p><pre><code>(defn length-3? [v] (= 3 (count v)))</code></pre><p>Then, we pull in one of Speculoos&apos; collection validation functions, and validate. The data is the first argument, in the upper row, the specification is the second argument, appearing in the lower row.</p><pre><code>(require &apos;[speculoos.core :refer [valid-collections?]])</code><br /><br /><br /><code>(valid-collections? [42 &quot;abc&quot; 22/7]
&nbsp;                   [length-3?]) ;; =&gt; true</code></pre><p>That example reminds us to consider the Three Mottos. We&apos;re validating strictly only collections, as the <code>valid-collections?</code> function name indicates. The shape of the specification (lower row) mimics the shape of the data (upper row). One predicate paired with one collection, zero ignored.</p><p>We&apos;ll go into much more detail soon, but be aware that during collection validation, predicates apply to their immediate parent collection. So the <code>length-3?</code> predicate applies to the vector, not the scalar <code>42</code>. The vector contains three elements, so the validation returns <code>true</code>.</p><p>We can validate whether <em>a map contains a key <code>:y</code></em>. Here&apos;s a predicate for that.</p><pre><code>(defn map-contains-keyword-y? [m] (contains? m :y))</code></pre><p>Then we validate, data in the upper row, specification in the lower row. For the moment, don&apos;t worry about that <code>:foo</code> key in the specification.</p><pre><code>(valid-collections? {:x 42}
&nbsp;                   {:foo map-contains-keyword-y?})
;; =&gt; false</code></pre><p>Data <code>{:x 42}</code> does not contain a key <code>:y</code>, so the validation returns <code>false</code>.</p><p>We can validate whether <em>a list contains an even number of elements</em>. Here&apos;s that predicate.</p><pre><code>(defn even-elements? [f] (even? (count f)))</code></pre><p>Then the validation.</p><pre><code>(valid-collections? (list &lt; 1 2 3)
&nbsp;                   (list even-elements?))
;; =&gt; true</code></pre><p>Yes, the list contains an even number of elements.</p><p>We could determine whether <em>every number in a set is an odd</em>. Here&apos;s a predicate to test that.</p><code>(defn all-odd? [s] (every? odd? s))</code><p>Collection validation looks like this.</p><pre><code>(valid-collections? #{1 2 3}
&nbsp;                   #{all-odd?}) ;; =&gt; false</code></pre><p>Our set, <code>#{1 2 3}</code>, contains one element that is not odd, so the set fails to satisfy the collection predicate.</p><p>None of those four examples could be accomplished with a scalar validation. They all require access to the collection itself.</p></section><section id="collection-predicate-application"><h3>Where collection predicates apply</h3><p>The principle to keep in mind is <em>Any collection predicate applies to its immediate parent collection.</em> Let&apos;s break that down into parts.</p><ul><li><p>Predicates apply to their <strong>parent</strong> collection.</p><pre><code>(valid-collections? [42 &quot;abc&quot; 22/7]
&nbsp;                   [length-3?]) ;; =&gt; true</code></pre><p>In contrast with scalar validation, which would have paired the predicate with integer <code>42</code>, collection validation pairs predicate <code>length-3?</code> with the parent vector <code>[42 &quot;abc&quot; 22/7]</code>. In the next section, we&apos;ll discuss the mechanics of how and why it&apos;s that way.</p></li><li><p>Predicates apply to their <strong>immediate</strong> parent collection.</p><pre><code>(valid-collections? [[42 &quot;abc&quot; 22/7]]
&nbsp;                   [[length-3?]])
;; =&gt; true</code></pre><p>The <code>length-3?</code> predicate applies to the nested vector that contains three elements. The outer, root collection that contains only one element was not paired with a predicate, so it was ignored. Each predicate is paired with at most one collection, the collection closest to it.</p></li><li><p><strong>Any</strong> collection predicates apply to their immediate parent collection.</p><pre><code>(valid-collections? [42 &quot;abc&quot; 22/7]
&nbsp;                   [vector? coll? sequential?])
;; =&gt; true</code></pre><p>Unlike scalar validation, which maintains an absolute one-to-one correspondence between predicates and datums, collection validation may include more than one predicate per collection. Collections may pair with more than one predicate, but each predicate pairs with at most one collection.</p></li></ul><p>The fact that predicates apply to their immediate parent collections is what allows us to write specifications whose shape mimic the shapes of the data.</p></section><h3>How collection validation works</h3><p>The Three Mottos and the principle of applying predicates to their containing collections are emergent properties of Speculoos&apos; collection validation algorithm. If we understand the algorithm, we will know when a collection validation is the best tool for the task, and be able to write clear, correct, and expressive collection specifications.</p><p>Imagine we wanted to specify that our data vector was exactly three elements long. We might reasonably write this predicate, whose argument is a collection.</p><pre><code>;; a predicate that returns `true` if the collection has three elements</code><br /><br /><code>(defn len-3? [c] (= 3 (count c)))</code></pre><p>Notice that this predicate tests a property of the collection itself: the number of elements it contains. <code>validate-scalars</code> has no way to do this kind of test because it deliberately only considers scalars.</p><p>Now, we invent some example data.</p><pre><code>[42 &quot;abc&quot; 22/7]</code></pre><p>The paths of that data look like this.</p><pre><code>(all-paths [42 &quot;abc&quot; 22/7])
;; =&gt; [{:path [], :value [42 &quot;abc&quot; 22/7]}
;;     {:path [0], :value 42}
;;     {:path [1], :value &quot;abc&quot;}
;;     {:path [2], :value 22/7}]</code></pre><p>We&apos;re validating collections (Motto #1), so we&apos;re only interested in the root collection at path <code>[]</code> in the data. Let&apos;s apply Motto #2 and shape our specification to mimic the shape of the data. We&apos;ll copy-paste the data…</p><pre><code>[42 &quot;abc&quot; 22/7]</code></pre><p>…delete the contents…</p><pre><code>[             ]</code></pre><p>…and replace the contents with our <code>len-3?</code> predicate.</p><pre><code>[len-3?       ]</code></pre><p>That will be our specification. Notice: during collection validation, we insert predicates <em>inside</em> the collection that they target.</p><p>Validating collections uses a <em>slightly</em> adjusted version of the <a href="#scalar-algorithm">scalar validation algorithm</a>. (If you are curious <em>why</em> the collection algorithm is different, see <a href="#collection-predicate-paths">this later subsection</a>.) The algorithm for validating collections is as follows:</p><ol class="collection-algorithm"><li>Run <code>all-paths</code> on the data, then the specification.</li><li>Remove <em>scalar</em> elements from the data, keeping only the collection elements.</li><li>Remove <em>non-predicate</em> elements from the collection specification.</li><li>Pair predicates at path <code>pth</code> in the specification with collections at path <code>(drop-last pth)</code> in the data. Discard all other un-paired collections and un-paired predicates.</li><li>For each remaining collection+predicate pair, apply the predicate to the collection.</li></ol><p>Let&apos;s perform that algorithm manually. We run <code>all-paths</code> on both the data…</p><pre><code>(all-paths [42 &quot;abc&quot; 22/7])
;; =&gt; [{:path [], :value [42 &quot;abc&quot; 22/7]}
;;     {:path [0], :value 42}
;;     {:path [1], :value &quot;abc&quot;}
;;     {:path [2], :value 22/7}]</code></pre><p>…and <code>all-paths</code> on our collection specification.</p><pre><code>(all-paths [len-3?])
;; =&gt; [{:path [], :value [len-3?]}
;;     {:path [0], :value len-3?}]</code></pre><p>We discard all scalar elements of the data, keeping only the collection elements.</p><pre><code>[{:path [], :value [42 &quot;abc&quot; 22/7]}]</code></pre><p>And we keep only the predicate elements of the specification.</p><pre><code>[{:path [0], :value len-3?}]</code></pre><p>The next step, pairing predicates to a target collection, is where it gets interesting. During scalar validation, we paired a predicate with a scalar when they shared the exact same path. That <a href="#collection-predicate-paths">doesn&apos;t work</a> for collection validation. Instead, we pair a collection and a predicate when the collection&apos;s path in the data is equivalent to <code>(drop-last pth)</code>, where <code>pth</code> is the predicate&apos;s path in the specification.</p><p>Looking at the previous two results, we see the root collection is path <code>[]</code>, while the <code>len-3?</code> predicate&apos;s path is <code>[0]</code>. <code>(drop-last [0])</code> evaluates to <code>()</code>, which is equivalent to the root path. So the predicate and the collection are paired. We then apply the predicate.</p><pre><code>(len-3? [42 &quot;abc&quot; 22/7]) ;; =&gt; true</code></pre><p>The root collection <code>[42 &quot;abc&quot; 22/7]</code> satisfies the <code>len-3?</code> predicate because it contains three elements, so the validation returns <code>true</code>.</p><p>Speculoos provides a function, <code>validate-collections</code>, that does all that for us. The function signature is similar to what we saw earlier while validating scalars: data on the upper row, and the specification mimicking the shape of the data on the lower row.</p><pre><code>(require &apos;[speculoos.core :refer [validate-collections]])</code><br /><br /><code>(validate-collections [42 &quot;abc&quot; 22/7]
&nbsp;                     [len-3?])
;; =&gt; ({:datum [42 &quot;abc&quot; 22/7],
;;      :ordinal-path-datum [],
;;      :path-datum [],
;;      :path-predicate [0],
;;      :predicate len-3?,
;;      :valid? true})</code></pre><p>Much of that looks familiar. <code>validate-collections</code> returns a validation entry for every collection+predicate pair. In this case, the data&apos;s root vector was paired with the single <code>len-3?</code>predicate. The <code>:datum</code> represents the thing being tested, the <code>:predicate</code>s indicate the predicate functions, and <code>valid?</code> reports whether that predicate was satisfied. The root vector contains three elements, so <code>len-3?</code> was satisfied.</p><p>There are now three things that involve some notion of a path. The predicate was found at <code>:path-predicate</code> in the specification. The datum was found at <code>:ordinal-path-datum</code> in the data, which is also presented in a more friendly format as the literal path <code>:path-datum</code>. (We&apos;ll explain the terms embodied by these keywords as the discussion progresses.) Notice that the path of the root vector <code>[]</code> is equivalent to running <code>drop-last</code> on the path of the <code>len-3?</code> predicate: <code>(drop-last [0])</code> evaluates to <code>()</code>.</p><p>Let&apos;s explore validating a two-element vector nested within a two-element vector. To test whether each of those two vectors contain two elements, we could write this collection predicate.</p><pre><code>(defn len-2? [c] (= 2 (count c)))</code></pre><p>Remember Motto #1: This predicate accepts a collection, <code>c</code>, not a scalar.</p><p>We&apos;ll invent some data, a two-element vector nested within a two-element vector by wrapping the final two elements inside an additional pair of brackets.</p><pre><code>[42 [&quot;abc&quot; 22/7]]</code></pre><p>Note that the outer root vector contains exactly two elements: one scalar <code>42</code> and one descendant collection, the nested vector <code>[&quot;abc&quot; 22/7]</code>.</p><p>Following Motto #2, we&apos;ll compose a collection specification whose shape mimics the shape of the data. We copy-paste the data, delete the scalars, and insert our predicates.</p><pre><code>[42     [&quot;abc&quot; 22/7]] ;; copy-paste data</code><br /><code>[       [          ]] ;; delete scalars</code><br /><code>[len-3? [len-2?    ]] ;; insert predicates</code></pre><p>(I&apos;ve re-used the <code>len-3?</code> predicate so that in the following examples, it&apos;ll be easier to keep track of which predicate goes where when we have multiple predicates.)</p><p>Let&apos;s take a look at the data&apos;s paths.</p><pre><code>(all-paths [42 [&quot;abc&quot; 22/7]])
;; =&gt; [{:path [], :value [42 [&quot;abc&quot; 22/7]]}
;;     {:path [0], :value 42}
;;     {:path [1], :value [&quot;abc&quot; 22/7]}
;;     {:path [1 0], :value &quot;abc&quot;}
;;     {:path [1 1], :value 22/7}]</code></pre><p>Five elements: three scalars, which we ignore during collection validation, and two collections, the root collection and the nested vector.</p><p>Here are the specification&apos;s paths.</p><pre><code>(all-paths [len-3? [len-2?]])
;; =&gt; [{:path [], :value [len-3? [len-2?]]}
;;     {:path [0], :value len-3?}
;;     {:path [1], :value [len-2?]}
;;     {:path [1 0], :value len-2?}]</code></pre><p>Four total elements: two collections, the root vector at path <code>[]</code> and a nested vector at path <code>[1]</code>, and two functions, predicate <code>len-3?</code> in the top-level at path <code>[0]</code> and predicate <code>len-2?</code> in the lower-level at path <code>[1 0]</code>.</p><p>Next, we remove all scalar elements from the data, keeping only the elements that are collections.</p><pre><code>;; non-scalar elements of data</code><br /><br /><code>[{:path [], :value [42 [&quot;abc&quot; 22/7]]}
&nbsp;{:path [1], :value [&quot;abc&quot; 22/7]}]</code></pre><p>We kept two such collections: the root collection at path <code>[]</code> and the nested vector at path <code>[1]</code>.</p><p>Next, we remove all non-predicate elements from the specification, keeping only the predicates.</p><pre><code>;; predicate elements of specification</code><br /><br /><code>[{:path [0], :value len-3?}
&nbsp;{:path [1 0], :value len-2?}]</code></pre><p>There are two such collection predicates: <code>len-3?</code> at path <code>[0]</code> and <code>len-2?</code> at path <code>[1 0]</code>. Let&apos;s notice that if we apply <code>drop-last</code> to those paths, we get the paths of the two vectors in the data: </p><ul><li><code>(drop-last [0])</code> yields <code>()</code>, which pairs with the data&apos;s root collection <code>[42 [&quot;abc&quot; 22/7]]</code> at path <code>[]</code>.</li><li><code>(drop-last [1 0])</code> yields <code>(1)</code>, which pairs with the nested vector <code>[&quot;abc&quot; 22/7]</code> at path <code>[1]</code> of the data.</li></ul><p>In the previous <a href="#scalar-validation">section</a> when we were validating scalars, we followed the principle that validation only proceeds when a predicate in the specification shares the <em>exact</em> path as the scalar in the data. However, we can now see an issue if we try to apply that principle here. The nested vector of the data is located at path <code>[1]</code>. The nested <code>len-2?</code> predicate in the specification is located at path <code>[1 0]</code>, nearly same except for the trailing <code>0</code>. The root vector of the data is located at path <code>[]</code> while the <code>len-3?</code> predicate is located at path <code>[0]</code> of the specification, again, nearly the same except for the trailing <code>0</code>. Clojure has a nice core function that performs that transformation.</p><p>The slightly modified rule for validating collections is <em>Collection predicates in the specification are applied to the collection in the data that correspond to their parent.</em> In other words, the predicate at path <code>pth</code> in the collection specification is applied to the collection at path <code>(drop-last pth)</code> in the data. So we pair predicate <code>len-3?</code> with the root collection <code>[42 [&quot;abc&quot; 22/7]]</code> and we pair predicate <code>len-2?</code> with the nested vector <code>[&quot;abc&quot; 22/7]</code>.</p><p>We can now perform the validation by hand. There are two vectors to validate, each with its own predicate.</p><pre><code>(len-3? [42 [&quot;abc&quot; 22/7]]) ;; =&gt; false</code><br /><code>(len-2? [&quot;abc&quot; 22/7]) ;; =&gt; true</code></pre><p>The root vector <code>[42 [&quot;abc&quot; 22/7]]</code> does not satisfy the <code>len-3?</code> predicate it was paired with because it only contains two elements (one integer plus one nested vector). The nested vector <code>[&quot;abc&quot; 22/7]</code> contains two elements, so it satisfies the <code>len-2?</code> collection predicate that it was paired with.</p><p><code>validate-collections</code> does that entire algorithm for us with one invocation. Data on the upper row, collection specification on the lower row.</p><pre><code>(validate-collections [42 [&quot;abc&quot; 22/7]]
&nbsp;                     [len-3? [len-2?]])
;; =&gt; ({:datum [42 [&quot;abc&quot; 22/7]],
;;      :ordinal-path-datum [],
;;      :path-datum [],
;;      :path-predicate [0],
;;      :predicate len-3?,
;;      :valid? false}
;;     {:datum [&quot;abc&quot; 22/7],
;;      :ordinal-path-datum [0],
;;      :path-datum [1],
;;      :path-predicate [1 0],
;;      :predicate len-2?,
;;      :valid? true})</code></pre><p>One invocation performs the entire algorithm, which found two pairs of predicates+collections. Predicate <code>len-3?</code> at path <code>[0]</code> in the specification was paired with root collection at path <code>[]</code> in the data. The root collection contains only two elements, so <code>len-3?</code> returns <code>false</code>. Predicate <code>len-2?</code> at path <code>[1 0]</code> in the specification was paired with the nested vector at path <code>[1]</code> in the data. The nested vector contains two elements, so <code>len-2?</code> returns <code>true</code>.</p><p>To solidify our knowledge, let&apos;s do one more example with an additional nested vector and a third predicate. I&apos;ll be terse because this is just a review of the concepts from before.</p><p>The nested data, similar to the previous data, but with an additional vector wrapping <code>22/7</code>.</p><pre><code>[42 [&quot;abc&quot; [22/7]]]</code></pre><p>A new predicate testing for a length of one.</p><pre><code>(defn len-1? [c] (= 1 (count c)))</code></pre><p>Motto #2: Shape the specification to mimic the data. Copy-paste the data, then delete the scalars.</p><pre><code>[   [      [    ]]]</code></pre><p>Insert collection predicates.</p><pre><code>[len-3? [len-2? [len-1?]]]</code></pre><p>Now that we have the data and specification in hand, we perform the collection validation algorithm.</p><ol><li><p>Run <code>all-paths</code> on the data.</p><pre><code>(all-paths [42 [&quot;abc&quot; [22/7]]])
;; =&gt; [{:path [], :value [42 [&quot;abc&quot; [22/7]]]}
;;     {:path [0], :value 42}
;;     {:path [1], :value [&quot;abc&quot; [22/7]]}
;;     {:path [1 0], :value &quot;abc&quot;}
;;     {:path [1 1], :value [22/7]}
;;     {:path [1 1 0], :value 22/7}]</code></pre><p>Six elements: three collections, three scalars (will be ignored).</p><p>Run <code>all-paths</code> on the specification.</p><pre><code>(all-paths [len-3? [len-2? [len-1?]]])
;; =&gt; [{:path [], :value [len-3? [len-2? [len-1?]]]}
;;     {:path [0], :value len-3?}
;;     {:path [1], :value [len-2? [len-1?]]}
;;     {:path [1 0], :value len-2?}
;;     {:path [1 1], :value [len-1?]}
;;     {:path [1 1 0], :value len-1?}]</code></pre><p>Six elements: three collections, three predicates.</p></li><li><p>Remove scalar elements from the data, keeping only the collection elements.</p><pre><code>[{:path [], :value [42 [&quot;abc&quot; [22/7]]]}
&nbsp;{:path [1], :value [&quot;abc&quot; [22/7]]}
&nbsp;{:path [1 1], :value [22/7]}]</code></pre><p>Remove non-predicate elements from the specification.</p><pre><code>[{:path [0], :value len-3?}
&nbsp;{:path [1 0], :value len-2?}
&nbsp;{:path [1 1 0], :value len-1?}]</code></pre></li><li><p>Pair predicates at path <code>pth</code> in the specification with collections at path <code>(drop-last pth)</code> in the data.</p><pre><code>;; paths of predicates  =&gt; paths of collections in data</code><br /><br /><code>(drop-last [0]) ;; =&gt; ()</code><br /><code>(drop-last [1 0]) ;; =&gt; (1)</code><br /><code>(drop-last [1 1 0]) ;; =&gt; (1 1)</code></pre><p><code>()</code> is equivalent to <code>[]</code>, <code>(1)</code> is equivalent to <code>[1]</code>, etc. Therefore,</p><ul><li><code>len-3?</code> pairs with <code>[42 [&quot;abc&quot; [22/7]]]</code></li><li><code>len-2?</code> pairs with <code>[&quot;abc&quot; [22/7]]</code></li><li><code>len-1?</code> pairs with <code>[22/2]</code></li></ul><p>All predicates pair with a collection, and all collections pair with a predicate. There are zero un-paired predicates, and zero un-paired collections.</p></li><li>For each collection+predicate pair, apply the predicate.<pre><code>(len-3? [42 [&quot;abc&quot; [22/7]]]) ;; =&gt; false</code><br /><code>(len-2? [&quot;abc&quot; [22/7]]) ;; =&gt; true</code><br /><code>(len-1? [22/7]) ;; =&gt; true</code></pre></li><p>The root collection fails to satisfy its predicate, but the two nested vectors do satisfy their respective predicates.</p></ol><p>Now, we lean on <code>validate-collections</code> to do all four steps of that algorithm with one invocation. Data on the upper row, specification on the lower row.</p><pre><code>(validate-collections [42 [&quot;abc&quot; [22/7]]]
&nbsp;                     [len-3? [len-2? [len-1?]]])
;; =&gt; ({:datum [42 [&quot;abc&quot; [22/7]]],
;;      :ordinal-path-datum [],
;;      :path-datum [],
;;      :path-predicate [0],
;;      :predicate len-3?,
;;      :valid? false}
;;     {:datum [&quot;abc&quot; [22/7]],
;;      :ordinal-path-datum [0],
;;      :path-datum [1],
;;      :path-predicate [1 0],
;;      :predicate len-2?,
;;      :valid? true}
;;     {:datum [22/7],
;;      :ordinal-path-datum [0 0],
;;      :path-datum [1 1],
;;      :path-predicate [1 1 0],
;;      :predicate len-1?,
;;      :valid? true})</code></pre><p><code>validate-collections</code> discovered the same three collection+predicate pairs and helpfully reports their paths, alongside the results of applying each of the three predicates to their respective collections. As we saw when we ran the manual validation, the root collection failed to satisfy its <code>len-3?</code> predicate, but the two nested vectors did satisfy their predicates, <code>len-2?</code> and <code>len-1?</code>, respectively.</p><p>Next we&apos;ll tackle validating the collection properties of maps. The same principle governs: predicates apply to their parent container. Let&apos;s assume this data.</p><pre><code>{:x 42}</code></pre><p>A hash-map containing one key-value. Here are the paths of that example data.</p><pre><code>(all-paths {:x 42})</code><br /><code>;; =&gt; [{:path [], :value {:x 42}}
&nbsp;      {:path [:x], :value 42}]</code></pre><p>One scalar, which <code>validate-collections</code> ignores, and one collection. Let&apos;s apply our rule: the predicate in the specification applies to the collection in the data whose path is one element shorter. The root collection is located at path <code>[]</code>. To write a collection specification, we&apos;d mimic the shape of the data, inserting predicates that apply to the parent. We can&apos;t simply write…</p><pre><code>{map?} ;; =&gt; java.lang.RuntimeException...</code></pre><p>…because maps must contain an even number of forms. So we&apos;re going to need to add a key in there. Let me propose this as a specification.</p><pre><code>{:foo map?}</code></pre><p><code>:foo</code> doesn&apos;t have any particular meaning, and it won&apos;t affect the validation. Let&apos;s examine the paths of that proposed specification and apply the Mottos.</p><pre><code>(all-paths {:foo map?})
;; =&gt; [{:path [], :value {:foo map?}}
;;     {:path [:foo], :value map?}]</code></pre><p>Two elements: the root collection at path <code>[]</code> and a predicate at path <code>[:foo]</code>. Since this will be the collection validation, Speculoos only considers the elements of the specification which are predicates, so non-predicate elements of the specification (i.e., the root collection) will be ignored, and only the <code>map?</code> predicate will participate, if it can be paired with a collection in the data.</p><p>Let&apos;s explore the <code>drop-last</code> business. There&apos;s only one element in the collection specification that&apos;s a predicate. Predicate <code>map?</code> is located at path <code>[:foo]</code>.</p><pre><code>(drop-last [:foo]) ;; =&gt; ()</code></pre><p>Fortunately, that evaluates to a path, <code>()</code>, which in the data, corresponds to a collection. Because the <code>(drop-last [:foo])</code> path of the predicate in the specification corresponds to the path of a collection in the data, we can form a validation pair.</p><pre><code>(map? {:x 42}) ;; =&gt; true</code></pre><p>The root collection satisfies the <code>map?</code> predicate it is paired with.</p><p>Let&apos;s do that sequence automatically with <code>validate-collections</code>, data on the upper row, specification on the lower row.</p><pre><code>(validate-collections {:x 42}
&nbsp;                     {:foo map?})
;; =&gt; ({:datum {:x 42},
;;      :ordinal-path-datum [],
;;      :path-datum [],
;;      :path-predicate [:foo],
;;      :predicate map?,
;;      :valid? true})</code></pre><p><code>validate-collections</code> was, in fact, able to pair one predicate to one collection. Predicate <code>map?</code> at path <code>[:foo]</code> in the specification was paired with the root collection at path <code>[]</code>. Unlike scalar validation which pairs predicates to scalars with their <em>exact</em> paths, collection validation pairs are formed when the target path is equivalent to the predicate&apos;s path right-trimmed. In this example, predicate <code>map?</code>&apos;s path is <code>[:foo]</code>. <code>(drop-last [:foo])</code> evaluates to <code>()</code>. A path <code>()</code> corresponds to the root collection, so the predicate <code>map?</code> was applied to the root collection. <code>{:x 42}</code> satisfies the predicate.</p><p>Because of the <code>drop-last</code> behavior, it mostly doesn&apos;t matter what key we associate our collection predicate. The key will merely get trimmed when searching for a target. In the example above, <code>:foo</code> was trimmed, but the key could be anything. Observe.</p><pre><code>(drop-last [:foo]) ;; =&gt; ()</code><br /><code>(drop-last [:bar]) ;; =&gt; ()</code><br /><code>(drop-last [:baz]) ;; =&gt; ()</code></pre><p><em>Any</em> single key would get trimmed off, resulting in a path of <code>[]</code>, which would always point to the root collection.</p><p>Technically, we could key our collection predicates however we want, but I strongly recommend choosing a key that doesn&apos;t appear in the data. This next example shows why.</p><p>Let&apos;s explore a map nested within a map. This will be our example data.</p><pre><code>{:x 42 :y {:z &quot;abc&quot;}</code></pre><p>Let&apos;s put a collection predicate at key <code>:y</code> of the specification.</p><pre><code>{:y map?}</code></pre><p>Notice that <code>:y</code> also appears in the data.</p><p>Now we run <code>all-paths</code> on both the data…</p><pre><code>(all-paths {:x 42, :y {:z &quot;abc&quot;}})
;; =&gt; [{:path [], :value {:x 42, :y {:z &quot;abc&quot;}}}
;;     {:path [:x], :value 42}
;;     {:path [:y], :value {:z &quot;abc&quot;}}
;;     {:path [:y :z], :value &quot;abc&quot;}]</code></pre><p>…and <code>all-paths</code> on the specification.</p><pre><code>(all-paths {:y map?})
;; =&gt; [{:path [], :value {:y map?}}
;;     {:path [:y], :value map?}]</code></pre><p>Discard all non-collection elements in the data…</p><pre><code>[{:path [],:value {:x 42, :y {:z &quot;abc&quot;}}}
&nbsp;{:path [:y], :value {:z &quot;abc&quot;}}]</code></pre><p>…and discard all non-predicate elements in the specification.</p><pre><code>[{:path [:y], :value map?}]</code></pre><p>This is something we see for the first time while discussing collection validation: Fewer predicates than collections. Since there is only one predicate, at least one collection will be un-paired, and ignored (Motto #3). In this example, the predicate&apos;s path is <code>[:y]</code>. We trim it with <code>drop-last</code>.</p><pre><code>(drop-last [:y]) ;; =&gt; ()</code></pre><p>The resulting <code>()</code> corresponds to path <code>[]</code> in the data. So we can now apply the collection predicate to the collection in the data.</p><pre><code>(map? {:x 42, :y {:z &quot;abc&quot;}}) ;; =&gt; true</code></pre><p>Let&apos;s confirm that we produced the same answer as <code>validate-collections</code> would give us.</p><pre><code>(validate-collections {:x 42, :y {:z &quot;abc&quot;}}
&nbsp;                     {:y map?})
;; =&gt; ({:datum {:x 42, :y {:z &quot;abc&quot;}},
;;      :ordinal-path-datum [],
;;      :path-datum [],
;;      :path-predicate [:y],
;;      :predicate map?,
;;      :valid? true})</code></pre><p>We see a <code>map?</code> predicate at key <code>:y</code> of the specification, and <code>validate-collections</code> merrily chugged along without a peep about masking the nested map <code>{:z &quot;abc&quot;}</code>.</p><p>We can see that the singular <code>map?</code> predicate located at specification path <code>[:y]</code> was indeed applied to the root container at data path <code>(drop-last [:y])</code> which evaluates to path <code>[]</code>. But now we&apos;ve consumed that key, and it cannot be used to target the nested map <code>{:z &quot;abc&quot;}</code> at path <code>[:y]</code> in the data. We would not be able to validate any aspect of the nested collection <code>{:z &quot;abc&quot;}</code>.</p><p>Instead, if we had invented a wholly fictitious key, <code>drop-last</code> would trim that sham key off the right end of the path and the predicate would still be applied to the root container, while key <code>:y</code> remains available to target the nested map. <code>:foo/:bar/:baz</code>-style keywords are nice because humans understand that they don&apos;t carry any particular meaning. In practice, I like to invent keys that are descriptive of the predicate so the validation results are easier to scan by eye.</p><p>For instance, if we&apos;re validating that a collection&apos;s type is a map, we could use sham key <code>:is-a-map?</code>. We could also verify that the nested map is not a set by associating predicate <code>set?</code> to <code>:is-a-set?</code>.</p><pre><code>(validate-collections {:x 42, :y {:z &quot;abc&quot;}}
&nbsp;                     {:is-a-map? map?, :y {:is-a-set? set?}})
;; =&gt; ({:datum {:x 42, :y {:z &quot;abc&quot;}},
;;      :ordinal-path-datum [],
;;      :path-datum [],
;;      :path-predicate [:is-a-map?],
;;      :predicate map?,
;;      :valid? true}
;;     {:datum {:z &quot;abc&quot;},
;;      :ordinal-path-datum [:y],
;;      :path-datum [:y],
;;      :path-predicate [:y :is-a-set?],
;;      :predicate set?,
;;      :valid? false})</code></pre><p>Notice that <code>validate-collections</code> completely ignored the scalars <code>42</code> and <code>&quot;abc&quot;</code> at data keys <code>:x</code> and <code>:z</code>. It only applied predicate <code>map?</code> to the root of data and predicate <code>set?</code> to the nested map at key <code>:y</code>, which failed to satisfy. Any possible meaning suggested by keys <code>:is-a-map?</code> and <code>:is-a-set?</code> did not affect the actual validation; they are merely convenient markers that we chose to make the results easier to read.</p><p>Let me emphasize: when we&apos;re talking about a nested map&apos;s collection specification, the predicate&apos;s key has <em>absolutely no bearing on the operation of the validation</em>. The key, at the tail position of the path, gets trimmed by the <code>drop-last</code> operation. That&apos;s why <code>:foo</code> in the earlier examples doesn&apos;t need to convey any meaning. We could have made the key misleading like this.</p><pre><code>;;             this keyword… ---v         v--- …gives the wrong impression about this predicate</code><br /><code>(validate-collections {:x 11} {:is-a-map? vector?})
;; =&gt; ({:datum {:x 11},
;;      :ordinal-path-datum [],
;;      :path-datum [],
;;      :path-predicate [:is-a-map?],
;;      :predicate vector?,
;;      :valid? false})</code></pre><p>Despite the <code>:is-a-map?</code> key suggesting that we&apos;re testing for a map, the predicate itself determines the outcome of the validation. The <code>vector?</code> predicate tests for a vector, and returns <code>false</code>.</p><p>It&apos;s our job to make sure we write the predicates correctly.</p><p>Here&apos;s something interesting.</p><pre><code>(validate-collections [42]
&nbsp;                     [vector? map?])
;; =&gt; ({:datum [42],
;;      :ordinal-path-datum [],
;;      :path-datum [],
;;      :path-predicate [0],
;;      :predicate vector?,
;;      :valid? true}
;;     {:datum [42],
;;      :ordinal-path-datum [],
;;      :path-datum [],
;;      :path-predicate [1],
;;      :predicate map?,
;;      :valid? false})</code></pre><p>If we focus on the paths of the two predicates in the specification, we see that both <code>vector?</code> at path <code>[0]</code> and <code>map?</code> at path <code>[1]</code> target the root container because…</p><pre><code>(drop-last [0]) ;; =&gt; ()</code></pre><p>…and…</p><pre><code>(drop-last [1]) ;; =&gt; ()</code></pre><p>…and in fact…</p><pre><code>(drop-last [99999]) ;; =&gt; ()</code></pre>…all evaluate to the same equivalent path <code>[]</code> in the data. So we have another consideration: <em>Every</em> predicate in a specification&apos;s collection applies to the parent collection in the data. This means that we can apply an unlimited number of predicates to each collection.<pre><code>(validate-collections [42]
&nbsp;                     [vector? map? list? set? coll?])
;; =&gt; ({:datum [42],
;;      :ordinal-path-datum [],
;;      :path-datum [],
;;      :path-predicate [0],
;;      :predicate vector?,
;;      :valid? true}
;;     {:datum [42],
;;      :ordinal-path-datum [],
;;      :path-datum [],
;;      :path-predicate [1],
;;      :predicate map?,
;;      :valid? false}
;;     {:datum [42],
;;      :ordinal-path-datum [],
;;      :path-datum [],
;;      :path-predicate [2],
;;      :predicate list?,
;;      :valid? false}
;;     {:datum [42],
;;      :ordinal-path-datum [],
;;      :path-datum [],
;;      :path-predicate [3],
;;      :predicate set?,
;;      :valid? false}
;;     {:datum [42],
;;      :ordinal-path-datum [],
;;      :path-datum [],
;;      :path-predicate [4],
;;      :predicate coll?,
;;      :valid? true})</code></pre><p>All five collection predicates were located at a single-element path, so for each of those five cases, <code>(drop-last [<em>0 through 4</em>])</code> evaluated to <code>()</code>, which is the path to the data&apos;s root collection. <code>validate-collections</code> was therefore able to make five pairs, and we see five validation results.</p><p>That feature can be useful, but it raises an issue. How would we specify the collections of this data?</p><pre><code>[42 {:y &quot;abc&quot;}]</code></pre><p>A map nested within a vector. And its paths.</p><pre><code>(all-paths [42 {:y &quot;abc&quot;}])
;; =&gt; [{:path [], :value [42 {:y &quot;abc&quot;}]}
;;     {:path [0], :value 42}
;;     {:path [1], :value {:y &quot;abc&quot;}}
;;     {:path [1 :y], :value &quot;abc&quot;}]</code></pre><p>We may want to specify two facets of the root collection, that it&apos;s both a collection and a vector (that&apos;s redundant, I know). Furthermore, we want to specify that the data&apos;s second element is a map. That collection specification might look something like this.</p><pre><code>[coll? vector? {:foo map?}]</code></pre><p>And its paths.</p><pre><code>(all-paths [coll? vector? {:foo map?}])
;; =&gt; [{:path [],
;;      :value [coll? vector? {:foo map?}]}
;;     {:path [0], :value coll?}
;;     {:path [1], :value vector?}
;;     {:path [2], :value {:foo map?}}
;;     {:path [2 :foo], :value map?}]</code></pre><p>Two predicates, <code>coll?</code> and <code>vector?</code>, apply to the root collection, because <code>(drop-last [0])</code> and <code>(drop-last [1])</code> both resolve the root collection&apos;s path. But somehow, we have to tell <code>validate-collections</code> how to target that <code>map?</code> predicate towards the nested map. We can see that <code>map?</code> is located at path <code>[2 :foo]</code>, and <code>(drop-last [2 :foo])</code> evaluates to <code>[2]</code>. The data&apos;s nested map <code>{:y &quot;abc&quot;}</code> is located at path <code>[1]</code>, which doesn&apos;t &apos;match&apos;.</p><p>If <strong>any</strong> number of predicates apply to the parent collection, there might be zero to infinity predicates before we encounter a nested collection in that sequence. How, then, does <code>validate-collections</code> determine where to apply the predicate inside a nested collection?</p><p>The rule <code>validate-collections</code> follows is <em>Within a sequential collection, apply nested collection predicates in the order which they appear, ignoring scalars.</em> Let&apos;s see that in action. Here is the data, with the scalars removed from the root level.</p><pre><code>[{:y &quot;abc&quot;}]</code></pre><p>Here is the specification with the scalar (i.e., functions) removed from its root level.</p><pre><code>[{:foo map?}]</code></pre><p>Now we generate the paths for both of those.</p><pre><code>;; pruned data</code><br /><br /><code>(all-paths [{:y &quot;abc&quot;}])
;; =&gt; [{:path [], :value [{:y &quot;abc&quot;}]}
;;     {:path [0], :value {:y &quot;abc&quot;}}
;;     {:path [0 :y], :value &quot;abc&quot;}]</code><br /><br /><br /><code>;; pruned specification</code><br /><br /><code>(all-paths [{:foo map?}])
;; =&gt; [{:path [], :value [{:foo map?}]}
;;     {:path [0], :value {:foo map?}}
;;     {:path [0 :foo], :value map?}]</code></pre><p>Next remove all non-collection elements from the data.</p><pre><code>[{:path [], :value [{:y &quot;abc&quot;}]}
&nbsp;{:path [0], :value {:y &quot;abc&quot;}}]</code></pre><p>And remove all non-predicate elements of the specification.</p><pre><code>[{:path [0 :foo], :value map?}]</code></pre><p>There are two remaining collections in the data, but only one predicate. Motto #3 reminds us that at least one of the collections will be ignored. Can we make at least one collection+predicate pair? Let&apos;s perform the <code>drop-last</code> maneuver on the predicate&apos;s path.</p><pre><code>(drop-last [0 :foo]) ;; =&gt; (0)</code></pre><p>Well, how about that? That resolves to <code>(0)</code>, which is equivalent to the path of the nested map <code>{:y &quot;abc&quot;}</code> in the pruned data. We can apply that predicate to the collection we paired it with.</p><pre><code>(map? {:y &quot;abc&quot;}) ;; =&gt; true</code></pre><p>So the nested map is indeed a map. Let&apos;s see what <code>validate-collections</code> has to say.</p><pre><code>(validate-collections [42 {:y &quot;abc&quot;}]
&nbsp;                     [coll? vector? {:foo map?}])
;; =&gt; ({:datum [42 {:y &quot;abc&quot;}],
;;      :ordinal-path-datum [],
;;      :path-datum [],
;;      :path-predicate [0],
;;      :predicate coll?,
;;      :valid? true}
;;     {:datum [42 {:y &quot;abc&quot;}],
;;      :ordinal-path-datum [],
;;      :path-datum [],
;;      :path-predicate [1],
;;      :predicate vector?,
;;      :valid? true}
;;     {:datum {:y &quot;abc&quot;},
;;      :ordinal-path-datum [0],
;;      :path-datum [1],
;;      :path-predicate [2 :foo],
;;      :predicate map?,
;;      :valid? true})</code></pre><p><code>validate-collections</code> found three predicates in the specification on the lower row that it could pair with a collection in the data in the upper row. Both <code>coll?</code> and <code>vector?</code> predicates pair with the root collection because their paths, when right-trimmed with <code>drop-last</code> correspond to <code>[]</code>, which targets the root collection. Predicate <code>map?</code> was paired with the nested map <code>{:y &quot;abc&quot;}</code> in the data because <code>map?</code> was located in the first nested collection of the specification, and <code>{:y &quot;abc&quot;}</code> is the first (and only) nested collection in the data. We can see how <code>validate-collections</code> calculated the nested map&apos;s path because <code>:ordinal-path-datum</code> is <code>[0]</code>. The ordinal path reports the path into the &apos;pruned&apos; collections, as if the sequentials in the data and the sequentials in the specification contained zero scalars.</p><p>Let&apos;s do another example that really exercises this principle. First, we&apos;ll make some example data composed of a parent vector, containing a nested map, a nested list, and a nested set, with a couple of interleaved integers.</p><pre><code>[{:a 11} 22 (list 33) 44 #{55}]</code></pre><p>Let&apos;s examine the paths of that data.</p><pre><code>(all-paths [{:a 11} 22 (list 33) 44 #{55}])
;; =&gt; [{:path [], :value [{:a 11} 22 (33) 44 #{55}]}
;;     {:path [0], :value {:a 11}}
;;     {:path [0 :a], :value 11}
;;     {:path [1], :value 22}
;;     {:path [2], :value (33)}
;;     {:path [2 0], :value 33}
;;     {:path [3], :value 44}
;;     {:path [4], :value #{55}}
;;     {:path [4 55], :value 55}]</code></pre><p>We got path elements for five scalars, and path elements for four collections: the root collection (a vector), and three nested collections (one each of map, list, and set).</p><p>We&apos;re in collection validation mindset (Motto #1), so we ought to be considering the order of the nested collections. Let&apos;s eliminate the five scalars and enumerate the paths of the pruned data.</p><pre><code>(all-paths [{} (list) #{}])
;; =&gt; [{:path [], :value [{} () #{}]}
;;     {:path [0], :value {}}
;;     {:path [1], :value ()}
;;     {:path [2], :value #{}}]</code></pre><p>Let&apos;s make note of a few facts. The nested map, nested list, and nested set remain in the same relative order as in the full data. The root collection is, as always, at path <code>[]</code>. The nested collections are zero-indexed: the nested map is located at index <code>0</code>, the nested list is at index <code>1</code>, and the nested set is at index <code>2</code>. These indexes are what <code>validate-collections</code> reports as <code>:ordinal-path-datum</code>, the prefix <em>ordinal</em> indicating a position within a sequence, &apos;first&apos;, &apos;second&apos;, &apos;third&apos;, etc.</p><p>Now we need to compose a collection specification. Motto #2 reminds us to make the specification mimic the shape of the data. I&apos;m going to copy-paste the data and mash the delete key to remove the scalar datums.</p><pre><code>[{     }    (       )    #{  }]</code></pre><p>Just to emphasize how they align, here are the data (upper row) and the collection specification (lower row) with some space for visual formatting.</p><pre><code>[{:a 11} 22 (list 33) 44 #{55}] ;; &lt;--- data</code><br /><code>[{     }    (       )    #{  }] ;; &lt;--- collection specification</code><br /><code> ^--- 1st   ^--- 2nd     ^--- 3rd nested collection</code></pre><p>The first thing to note is that our collection specification looks a lot like our data with all the scalars removed. The second thing to notice is that even though it contains zero predicates, that empty structure in the lower row is a legitimate collection specification which <code>validate-collections</code> can consume. Check this out.</p><pre><code>(validate-collections [{:a 11} 22 (list 33) 44 #{55}]
&nbsp;                     [{} () #{}])
;; =&gt; ()</code></pre><p>Motto #3: Validation ignores collections in the data that are not paired with a predicate in the specification. Zero predicates, zero pairs.</p><p>Okay, let&apos;s add one predicate. Let&apos;s specify that the second nested collection is a list. Predicates apply to their parent container, so we&apos;ll insert <code>list?</code> into the list of the specification (lower row).</p><pre><code>(validate-collections [{:a 11} 22 (list 33) 44 #{55}]
&nbsp;                     [{} (list list?) #{}])
;; =&gt; ({:datum (33),
;;      :ordinal-path-datum [1],
;;      :path-datum [2],
;;      :path-predicate [1 0],
;;      :predicate list?,
;;      :valid? true})</code></pre><p>One predicate in the specification pairs with one collection in the data, so we receive one validation result. That nested collection is indeed a list, so <code>:valid?</code> is <code>true</code>. The <code>list?</code> predicate at path <code>[1 0]</code> in the specification was applied to the collection located at path <code>[2]</code> in the data.</p><p>Notice how <code>validate-collections</code> did some tedious and boring calculations to achieve the general effect of <em>The predicate in the second nested collection of the specification applies to the second nested collection of the data.</em> It kinda skipped over that <code>22</code> because it ignores scalars, and we&apos;re validating collections. Basically, <code>validate-collections</code> performed that &apos;skip&apos; by pruning the scalars from the data…</p><pre><code>[{} (list) #{}]</code></pre><p>…and pruning all non-collections from the parent level above the predicate. In other words, <code>validate-collections</code> pruned from the specification any scalars with a path length exactly one element shorter than the path of the predicate.</p><pre><code>[{} (list list?) #{}] ;; no pruning because zero scalars within the parent&apos;s level</code></pre><p>Then, enumerating the paths for both pruned data and pruned specification.</p><pre><code>;; data, all scalars pruned</code><br /><br /><code>(all-paths [{} (list) #{}])
;; =&gt; [{:path [], :value [{} () #{}]}
;;     {:path [0], :value {}}
;;     {:path [1], :value ()}
;;     {:path [2], :value #{}}]</code><br /><br /><br /><code>;; specification, parent-level pruned of non-collections</code><br /><br /><code>(all-paths [{} (list list?) #{}])
;; =&gt; [{:path [], :value [{} (list?) #{}]}
;;     {:path [0], :value {}}
;;     {:path [1], :value (list?)}
;;     {:path [1 0], :value list?}
;;     {:path [2], :value #{}}]</code></pre><p>There&apos;s only one predicate, <code>list?</code>, which is located at path <code>[1 0]</code> in the pruned specification. Right-trimming the predicate&apos;s path give us this.</p><pre><code>(drop-last [1 0]) ;; =&gt; (1)</code></pre><p>That right-trimmed result is equivalent to ordinal path <code>[1]</code> which is the second element of the pruned data. There is indeed a collection at ordinal path <code>[1]</code> of the pruned data, so we have successfully formed a pair. We can apply the predicate to the thing at that path.</p><pre><code>(list? (list 33)) ;; =&gt; true</code></pre><p>The list in the data indeed satisfies the <code>list?</code> predicate. <code>validate-collections</code> does all that with one invocation: pairs up predicates in the specification with nested collections in the data, and applies all predicates to their paired targets.</p><p>Let&apos;s see, again, how <code>validate-collections</code> handles this validation.</p><pre><code>(validate-collections [{:a 11} 22 (list 33) 44 #{55}]
&nbsp;                     [{} (list list?) #{}])
;; =&gt; ({:datum (33),
;;      :ordinal-path-datum [1],
;;      :path-datum [2],
;;      :path-predicate [1 0],
;;      :predicate list?,
;;      :valid? true})</code></pre><p>We inserted only a single <code>list?</code> predicate into the specification, so, at most, we could receive only one collection+predicate pair. The data&apos;s nested list, <code>(list 33)</code> is the second nested collection within the sequential, so its ordinal path is <code>[1]</code>. The <code>list?</code> predicate is contained in the specification&apos;s second nested collection, so its ordinal path is also <code>[1]</code>. Since the <code>list?</code> predicate&apos;s container and the thing in the data share an ordinal path, <code>validate-collection</code> formed a collection+predicate pair. The <code>list?</code> predicate was satisfied because <code>(list 33)</code> is indeed a list.</p><p>Let&apos;s clear the slate and specify that nested set at the end. We start with the full data…</p><pre><code>[{:a 11} 22 (list 33) 44 #{55}]</code></pre><p>…and prune all non-scalars from data to serve as a template for the specification…</p><pre><code>[{     }    (list   )    #{  }]</code></pre><p>…and insert a <code>set?</code> predicate for the set. Collection predicates apply to their parent containers, so we&apos;ll insert it <em>inside</em> the set we want to validate.</p><pre><code>[{} (list) #{set?}]</code></pre><p> Usually, we wouldn&apos;t include non-predicates into the specification, but for demonstration purposes, I&apos;m going to insert a couple of scalars, keywords <code>:skip-1</code> and <code>:skip-2</code>, that will ultimately get skipped because validation ignores non-predicates in the specification.</p><pre><code>[{} :skip-1 (list) :skip-2 #{set?}]</code></pre><p>First, we prune the non-collections from the data…</p><pre><code>[{} (list) #{}]</code></pre><p>…then prune from the specification the non-predicates from the parent-level.</p><pre><code>[{} (list) #{set?}]</code></pre><p>That rids us of <code>:skip-1</code> and <code>:skip-2</code>, so now the nested collections in the specification align with the nested collections in the data.</p><p>We enumerate the paths of the pruned data…</p><pre><code>(all-paths [{} (list) #{}])
;; =&gt; [{:path [], :value [{} () #{}]}
;;     {:path [0], :value {}}
;;     {:path [1], :value ()}
;;     {:path [2], :value #{}}]</code></pre><p>…and enumerate the paths of the pruned specification.</p><pre><code>(all-paths [{} (list) #{set?}])
;; =&gt; [{:path [], :value [{} () #{set?}]}
;;     {:path [0], :value {}}
;;     {:path [1], :value ()}
;;     {:path [2], :value #{set?}}
;;     {:path [2 set?], :value set?}]</code></pre><p>There is only one predicate, specification element <code>{:path [2 set?], :value set?}</code>. When we right-trim that path, which we calculated with respect to the pruned specification, we get…</p><pre><code>(drop-last [2 :set?]) ;; =&gt; (2)</code></pre><p>…which is equivalent to ordinal path <code>[2]</code> with respect to the pruned data. The element at that path in the data is indeed a collection, so we successfully paired the predicate with a collection. Validation proceeds by applying the predicate to the element.</p><pre><code>(set? #{55}) ;; =&gt; true</code></pre><p>The element is indeed a set, so the predicate is satisfied.</p><p>Here&apos;s how we validate that nested set using <code>validate-collections</code>, data upper row, specification lower row.</p><pre><code>(validate-collections [{:a 11} 22 (list 33) 44 #{55}]
&nbsp;                     [{} :skip-1 () :skip-2 #{set?}])
;; =&gt; ({:datum #{55},
;;      :ordinal-path-datum [2],
;;      :path-datum [4],
;;      :path-predicate [4 set?],
;;      :predicate set?,
;;      :valid? true})</code></pre><p>One predicate applied to one collection, one validation result. And again, collection validation skipped right over the intervening scalars, <code>22</code> and <code>44</code>, in the data, and over the intervening non-predicates, <code>:skip-1</code> and <code>:skip-2</code>, in the specification. <code>validate-collections</code> applied the <code>set?</code> predicate in the specification&apos;s third nested collection to the data&apos;s third nested collection <code>#{55}</code>, both at ordinal path <code>[2]</code> (i.e., the third non-scalar elements).</p><p>We might as well specify and validate that nested map now. Here&apos;s our data again.</p><pre><code>[{:a 11} 22 (list 33) 44 #{55}]</code></pre><p>We remove all non-scalars to create a template for the specification.</p><pre><code>[{     }    (list   )    #{  }]</code></pre><p>Recall that collection predicates targeting a map require a sham key. We&apos;ll insert into the specification a <code>map?</code> predicate associated to a sham key,<code>:is-map?</code>, that doesn&apos;t appear in the data&apos;s corresponding nested map.</p><pre><code>[{:is-map? map?}    (list   )    #{  }]</code></pre><p>And again, just to demonstrate how the skipping works, I&apos;ll insert a couple of non-predicates in front of the nested map.</p><pre><code>[:skip-3 :skip-4 {:is-map? map?}    (list   )    #{  }]</code></pre><p>Note that the data&apos;s nested map is located at path <code>[0]</code>, the first element, while, because of those to non-predicates, the specification&apos;s corresponding nested map is located at path <code>[2]</code>, the third element. In a moment, matching the ordinal paths of each (by &apos;pruning&apos;) will cause them to be paired.</p><p>Now, we prune the non-scalars from the data…</p><pre><code>[{} () #{}]</code></pre><p>…and prune the non-predicates from the parent-level.</p><pre><code>[{:is-map? map?} () #{}]</code></pre><p>We enumerate the paths of the pruned data…</p><pre><code>(all-paths [{} () #{}])
;; =&gt; [{:path [], :value [{} () #{}]}
;;     {:path [0], :value {}}
;;     {:path [1], :value ()}
;;     {:path [2], :value #{}}]</code></pre><p>…and enumerate the paths of the pruned specification.</p><pre><code>(all-paths [{:is-map? map?} () #{}])
;; =&gt; [{:path [], :value [{:is-map? map?} () #{}]}
;;     {:path [0], :value {:is-map? map?}}
;;     {:path [0 :is-map?], :value map?}
;;     {:path [1], :value ()}
;;     {:path [2], :value #{}}]</code></pre><p>There is only the one predicate, <code>map?</code>, which is located at path <code>[0 :is-map?]</code> in the pruned specification. We right-trim that path.</p><pre><code>(drop-last [0 :is-map?]) ;; =&gt; (0)</code></pre><p>It turns out that there is, in fact, a collection at that ordinal path of the pruned data, so we&apos;ve made a collection+predicate pairing. We apply the predicate to that collection element.</p><pre><code>(map? {:a 11}) ;; =&gt; true</code></pre><p>The nested collection at ordinal path <code>[0]</code>, the first nested collection, in the pruned data satisfies the predicate <code>map?</code> located at ordinal path <code>[0]</code> in the pruned specification.</p><p><code>validate-collections</code> does all that work for us. Upper row, data; lower row, specification.</p><pre><code>(validate-collections [{:a 11} 22 (list 33) 44 #{55}]
&nbsp;                     [:skip-3 :skip-4 {:is-map? map?} () #{}])
;; =&gt; ({:datum {:a 11},
;;      :ordinal-path-datum [0],
;;      :path-datum [0],
;;      :path-predicate [2 :is-map?],
;;      :predicate map?,
;;      :valid? true})</code></pre><p>Unlike the previous two validations, <code>validate-collections</code> didn&apos;t have to skip over any scalars in the data because the nested map is the first element. It did, however, have to skip over two non-predicates, <code>:skip-3</code> and <code>:skip-4</code>, in the specification. It applied the predicate in the specification&apos;s first nested collection to the data&apos;s first nested collection (both at ordinal path <code>[0]</code>, i.e., the first non-scalar element), which is indeed a map.</p><p>We&apos;ve now seen how to specify and validate each of those three nested collections, so for completeness&apos; sake, let&apos;s specify the root. Predicates apply to their container, so for clarity, we&apos;ll insert it at the beginning.</p><pre><code>(validate-collections [{:a 11} 22 (list 33) 44 #{55}]
&nbsp;                     [vector? {} () #{}])
;; =&gt; ({:datum [{:a 11} 22 (33) 44 #{55}],
;;      :ordinal-path-datum [],
;;      :path-datum [],
;;      :path-predicate [0],
;;      :predicate vector?,
;;      :valid? true})</code></pre><p>Technically, we could put that particular predicate anywhere in the top-level vector as long <code>(drop-last <em>path</em>)</code> evaluates to <code>[]</code>. All the following yield substantially the same results.</p><pre><code>(validate-collections [{:a 11} 22 (list 33) 44 #{55}] [vector? {} () #{}])</code><br /><code>(validate-collections [{:a 11} 22 (list 33) 44 #{55}] [{} vector? () #{}])</code><br /><code>(validate-collections [{:a 11} 22 (list 33) 44 #{55}] [{} () vector? #{}])</code><br /><code>(validate-collections [{:a 11} 22 (list 33) 44 #{55}] [{} () #{} vector?])</code></pre><p>In practice, I find it visually clearer to insert the predicates at the head of a sequential.</p><p>Let&apos;s do one final, all-up demonstration where we validate all four collections, the root collection containing three nested collections. Once again, here&apos;s the data.</p><pre><code>[{:a 11} 22 (list 33) 44 #{55}]</code></pre><p>We copy-paste the data and delete all scalars to create a template for the specification.</p><pre><code>[{     }    (list   )    #{  }]</code></pre><p>Now we insert the predicates. The rule is <em>Predicates apply to the collection that contains the predicate.</em> So we insert a <code>set?</code> predicate into the set…</p><pre><code>[{     }    (list   )    #{set?}]</code></pre><p>…insert a <code>list?</code> predicate into the list…</p><pre><code>[{     }    (list list?)    #{set?}]</code></pre><p>…insert a <code>map?</code> predicate into the map, associated to sham key <code>:foo</code>…</p><pre><code>[{:foo map?}    (list list?)    #{set?}]</code></pre><p>…and insert a <code>vector?</code> predicate, a <code>sequential?</code> predicate, a <code>sequential?</code> predicate, and an <code>any?</code> predicate into the vector&apos;s top level.</p><pre><code>[vector? {:foo map?} sequential? (list list?) coll? #{set?} any?]</code></pre><p>There will be two &apos;phases&apos;, each phase pruning a different level. The first phase validates the root collection with the top-level predicates. To start, we enumerate the paths of the data…</p><pre><code>(all-paths [{:a 11} 22 (list 33) 44 #{55}])
;; =&gt; [{:path [], :value [{:a 11} 22 (33) 44 #{55}]}
;;     {:path [0], :value {:a 11}}
;;     {:path [0 :a], :value 11}
;;     {:path [1], :value 22}
;;     {:path [2], :value (33)}
;;     {:path [2 0], :value 33}
;;     {:path [3], :value 44}
;;     {:path [4], :value #{55}}
;;     {:path [4 55], :value 55}]</code></pre><p>…and enumerate the paths of our specification.</p><pre><code>(all-paths [vector? {:foo map?} sequential? (list list?) coll? #{set?} any?])</code><br /><code>;; =&gt; [{:path [], :value [vector? {:foo map?} sequential? (list?)  coll? #{set} any?]}
;;     {:path [0], :value vector?}
;;     {:path [1], :value {:foo map?}}
;;     {:path [1 :foo], :value map?}
;;     {:path [2], :value sequential?}
;;     {:path [3], :value (list?)}
;;     {:path [3 0], :value list?}
;;     {:path [4], :value coll?}
;;     {:path [5], :value #{set?}}
;;     {:path [5 set?], :value set?}
;;     {:path [6], :value any?]</code></pre><p>Then, we keep only elements that a) are predicates and b) have a single-element path.</p><pre><code>[{:path [0], :value vector?}
&nbsp;{:path [2], :value sequential?}
&nbsp;{:path [4], :value coll?}
&nbsp;{:path [6], :value any?}]</code></pre><p>In this first phase, we&apos;re focusing on predicates located at single-element paths, because <code>(drop-last [<em>i</em>])</code> will, for every <code>i</code>, resolve to <code>[]</code>, which targets the root collection. We see from that last step, predicates <code>vector?</code>, <code>sequential?</code>, <code>coll?</code>, and <code>any?</code> all have single-element paths, so they will target the root collection. The conceptual linkage between a predicate&apos;s right-trimmed path and its target has the practical result that <em>predicates apply to their parent containers</em>. So we right-trim those paths.</p><pre><code>(drop-last [0]) ;; =&gt; ()</code><br /><code>(drop-last [2]) ;; =&gt; ()</code><br /><code>(drop-last [4]) ;; =&gt; ()</code><br /><code>(drop-last [6]) ;; =&gt; ()</code></pre><p>They all evaluate to <code>()</code>, which is equivalent to <code>[]</code>, the path to the root collection. So we may now apply all four predicates to the root collection.</p><pre><code>(vector? [{:a 11} 22 (list 33) 44 #{55}]) ;; =&gt; true</code><br /><code>(sequential? [{:a 11} 22 (list 33) 44 #{55}]) ;; =&gt; true</code><br /><code>(coll? [{:a 11} 22 (list 33) 44 #{55}]) ;; =&gt; true</code><br /><code>(any? [{:a 11} 22 (list 33) 44 #{55}]) ;; =&gt; true</code></pre><p>Now that we&apos;ve applied all predicates in the top level to the root collection, the first phase is complete. The second phase involves validating the nested collections. We start the second phase with the original data…</p><pre><code>[{:a 11} 22 (list 33) 44 #{55}]</code></pre><p>…and the original specification.</p><pre><code>[vector? {:foo map?} sequential? (list list?) coll? #{set?} any?]</code></pre><p>We remove the scalars from the data…</p><pre><code>[{     }    (       )    #{  }]</code></pre><p>…and from the specification, we keep only the second-level predicates, i.e., the predicates contained in the nested collections.</p><pre><code>[{:foo map?} (list list?) #{set?}]</code></pre><p>Next, we enumerate the paths of the pruned data…</p><pre><code>(all-paths [{} () #{}])
;; =&gt; [{:path [], :value [{} () #{}]}
;;     {:path [0], :value {}}
;;     {:path [1], :value ()}
;;     {:path [2], :value #{}}]</code></pre><p>…and enumerate the paths of the pruned specification.</p><pre><code>(all-paths [{:foo map?} (list list?) #{set?}])
;; =&gt; [{:path [], :value [{:foo map?} (list?) #{set?}]}
;;     {:path [0], :value {:foo map?}}
;;     {:path [0 :foo], :value map?}
;;     {:path [1], :value (list?)}
;;     {:path [1 0], :value list?}
;;     {:path [2], :value #{set?}}
;;     {:path [2 set?], :value set?}]</code></pre><p>Next, we retain the path elements of the data&apos;s second-level collections only…</p><pre><code>[{:path [0], :value {}}
&nbsp;{:path [1], :value ()}
&nbsp;{:path [2], :value #{}}]</code></pre><p>…and retain only the predicates of the pruned specification, which in this phase are only in the nested collections.</p><pre><code>[{:path [0 :foo], :value map?}
&nbsp;{:path [1 0], :value list?}
&nbsp;{:path [2 set?], :value set?}]</code></pre><p>Now we run the trim-right operation on the predicate paths.</p><pre><code>(drop-last [0 :foo]) ;; =&gt; (0)</code><br /><code>(drop-last [1 0]) ;; =&gt; (1)</code><br /><code>(drop-last [2 set?]) ;; =&gt; (2)</code></pre><p>Then we try to form predicate+collection pairs. From top to bottom:</p><ul><li>Predicate <code>map?</code> at <code>[0 :foo]</code> pairs with the data element at path <pre><code>(drop-last [0 :foo]) ;; =&gt; (0)</code></pre>which resolves to the nested map <code>{:a 11}</code>.</li><li>Predicate <code>list?</code> at <code>[1 0]</code> pairs with the data element at path <pre><code>(drop-last [1 0]) ;; =&gt; (1)</code></pre>which resolves to the nested list <code>(list 33)</code>.</li><li>Predicate <code>set?</code> at <code>[2 set?]</code> pairs with the data element at path <pre><code>(drop-last [2 set?]) ;; =&gt; (2)</code></pre>which resolves to the nested set <code>#{55}</code>.</li></ul><p>We can finally apply each of those three predicates towards their respective target collections.</p><pre><code>(map? {:a 11}) ;; =&gt; true</code><br /><code>(list? (list? 33)) ;; =&gt; false</code><br /><code>(set? #{55}) ;; =&gt; true</code></pre><p>Combining the two phases, we have seven total predicate+collections pairs, four in the top level, one in each of the three nested collections. All predicates were satisfied.</p><p>Now that we&apos;ve manually done that collection validation, let&apos;s see how <code>validate-collections</code> compares.</p><pre><code>(validate-collections [{:a 11} 22 (list 33) 44 #{55}]
&nbsp;                     [vector? {:foo map?} sequential? (list list?) coll? #{set?} any?])
;; =&gt; ({:datum [{:a 11} 22 (33) 44 #{55}],
;;      :ordinal-path-datum [],
;;      :path-datum [],
;;      :path-predicate [0],
;;      :predicate vector?,
;;      :valid? true}
;;     {:datum {:a 11},
;;      :ordinal-path-datum [0],
;;      :path-datum [0],
;;      :path-predicate [1 :foo],
;;      :predicate map?,
;;      :valid? true}
;;     {:datum [{:a 11} 22 (33) 44 #{55}],
;;      :ordinal-path-datum [],
;;      :path-datum [],
;;      :path-predicate [2],
;;      :predicate sequential?,
;;      :valid? true}
;;     {:datum (33),
;;      :ordinal-path-datum [1],
;;      :path-datum [2],
;;      :path-predicate [3 0],
;;      :predicate list?,
;;      :valid? true}
;;     {:datum [{:a 11} 22 (33) 44 #{55}],
;;      :ordinal-path-datum [],
;;      :path-datum [],
;;      :path-predicate [4],
;;      :predicate coll?,
;;      :valid? true}
;;     {:datum #{55},
;;      :ordinal-path-datum [2],
;;      :path-datum [4],
;;      :path-predicate [5 set?],
;;      :predicate set?,
;;      :valid? true}
;;     {:datum [{:a 11} 22 (33) 44 #{55}],
;;      :ordinal-path-datum [],
;;      :path-datum [],
;;      :path-predicate [6],
;;      :predicate any?,
;;      :valid? true})</code></pre><p>We inserted four predicates — <code>vector?</code>, <code>sequential?</code>, <code>coll?</code>, and <code>any?</code> — directly into the specification&apos;s top level, interleaved among the nested map, list, and set. Because they&apos;re in the top level, those predicates apply to the collection that contains them, the root collection. The outer, parent vector satisfies all four predicates because it is indeed a vector, is sequential, is a collection, and it trivially satisfies <code>any?</code>.</p><p>In addition, <code>validate-collections</code> validated the data&apos;s three nested collections, each with the particular predicate they contained. Map <code>{:a 11}</code> is the first nested collection, so its <code>map?</code> predicate is found at ordinal path <code>[0]</code>. List <code>(list 33)</code>is the second nested collection, so its <code>list?</code> predicate is found at ordinal path <code>[1]</code>, skipping over the intervening scalar <code>22</code>. Set <code>#{55}</code> is the third nested collection, paired with the <code>set?</code> predicate at ordinal path <code>[2]</code>, skipping over the intervening scalars <code>22</code> and <code>44</code>. All three nested collections satisfied their respective predicates.</p><p>Collections nested within a map do not involve that kind of skipping because they&apos;re not sequential. To demonstrate that, let&apos;s make this our example data.</p><pre><code>{:a [99] :b (list 77)}</code></pre><p>Now, we copy-paste the data, then delete the scalars.</p><pre><code>{:a [  ] :b (list   )}</code></pre><p>That becomes the template for our collection specification. Let&apos;s pretend we want to specify something about those two nested collections at keys <code>:a</code> and <code>:b</code>. We stuff the predicates <em>directly inside those collections</em>. During a collection validation, predicates apply to the collection that contains them.</p><pre><code>{:a [vector?] :b (list list?)}</code></pre><p>This becomes our collection specification. For now, we&apos;ve only specified one property for each of the two nested collections. We haven&apos;t stated any requirement of the root collection, the outer map.</p><p>Let&apos;s validate with <code>validate-collections</code>.</p><pre><code>(validate-collections {:a [99], :b (list 77)}
&nbsp;                     {:a [vector?], :b (list list?)})
;; =&gt; ({:datum [99],
;;      :ordinal-path-datum [:a],
;;      :path-datum [:a],
;;      :path-predicate [:a 0],
;;      :predicate vector?,
;;      :valid? true}
;;     {:datum (77),
;;      :ordinal-path-datum [:b],
;;      :path-datum [:b],
;;      :path-predicate [:b 0],
;;      :predicate list?,
;;      :valid? true})</code></pre><p>Checklist time.<ul><li>Specification shape mimics data? <em>Check.</em></li><li>Validating collections, ignoring scalars? <em>Check.</em></li><li>Two paired predicates, two validations? <em>Check.</em></li></ul></p><p>There&apos;s a subtlety to pay attention to: the <code>vector?</code> and <code>list?</code> predicates are contained within a vector and list, respectively. Those two predicates apply to their <em>immediate</em> parent container. <code>validate-collections</code> needs those <code>:a</code> and <code>:b</code> keys to find that vector and that list. We only use a sham key when validating a map immediately above our heads. Let&apos;s demonstrate how a sham key works in this instance.</p><p>Let&apos;s re-use that specification and tack on a sham <code>:howdy</code> key with a <code>map?</code> predicate aimed at the root map.</p><pre><code>{:a [vector?] :b (list list?) :howdy map?}</code></pre><p>Now we validate with the new specification with three predicates: one predicate each for the root collection and the two nested collections.</p><pre><code>(validate-collections {:a [99], :b (list 77)}
&nbsp;                     {:a [vector?], :b (list list?), :howdy map?})
;; =&gt; ({:datum [99],
;;      :ordinal-path-datum [:a],
;;      :path-datum [:a],
;;      :path-predicate [:a 0],
;;      :predicate vector?,
;;      :valid? true}
;;     {:datum (77),
;;      :ordinal-path-datum [:b],
;;      :path-datum [:b],
;;      :path-predicate [:b 0],
;;      :predicate list?,
;;      :valid? true}
;;     {:datum {:a [99], :b (77)},
;;      :ordinal-path-datum [],
;;      :path-datum [],
;;      :path-predicate [:howdy],
;;      :predicate map?,
;;      :valid? true})</code></pre><p>We&apos;ve got the vector and list validations as before, and then, at the end, we see that <code>map?</code> at the sham <code>:howdy</code> key was applied to the root. Because the parent collection is <em>not</em> sequential (i.e., a map), <code>validate-collections</code> did not have to skip over any intervening non-collections. There is no concept of order; elements are addressed by non-sequential keys. For example, predicate <code>vector?</code> is located at path <code>[:a 0]</code> within the specification. Right-trimming that path…</p><pre><code>(drop-last [:a 0]) ;; =&gt; (:a)</code></pre><p>…resolves to directly to the path of the collection nested at path <code>[:a]</code> in the data. It made no difference that predicate <code>map?</code> was floating around there at path <code>[:howdy]</code> in the parent level. Likewise, predicate <code>list?</code> was applied to the list nested at path <code>[:b 0]</code> in the data because its right-trimmed path…</p><pre><code>(drop-last [:b 0]) ;; =&gt; (:b)</code></pre><p>…doesn&apos;t involve <code>:howdy</code> at any point.</p><p>One more example to illustrate how collection validation ignores un-paired elements. Again, here&apos;s our data.</p><pre><code>{:a [99] :b (list 77)}</code></pre><p>And again, we&apos;ll copy-paste the data, then delete the scalars. That&apos;ll be our template for our collection specification.</p><pre><code>{:a [  ] :b (list   )}</code></pre><p>Now, we&apos;ll go even further and delete the <code>:b</code>  key and its associated value, the nested list.</p><pre><code>{:a [  ]             }</code><br /><br /><code>;; without :b, impossible to validate the list associated to :b</code></pre><p>Insert old reliable <code>vector?</code>. That predicate is paired with its immediate parent vector, so we need to keep the <code>:a</code> key.</p><pre><code>{:a [vector?]        }</code></pre><p>Finally, we&apos;ll add in a wholly different key that doesn&apos;t appear in the data, <code>:flamingo</code>, with a <code>coll?</code> predicate nested in a vector associated to that new key.</p><pre><code>{:a [vector?] :flamingo [coll?]}</code></pre><p>Test yourself: How many validations will occur?</p><pre><code>(validate-collections {:a [99], :b (list 77)}
&nbsp;                     {:a [vector?], :flamingo [coll?]})
;; =&gt; ({:datum [99],
;;      :ordinal-path-datum [:a],
;;      :path-datum [:a],
;;      :path-predicate [:a 0],
;;      :predicate vector?,
;;      :valid? true})</code></pre><p>Answer: <em>one</em>.</p><p>In this example, there is only one predicate+collection pair. <code>vector?</code> applies to the vector at <code>:a</code>. We might have expected <code>coll?</code> to be applied to the root collection because <code>:flamingo</code> doesn&apos;t appear in the map, but notice that <code>coll?</code> is <em>contained</em> in a vector. It would only ever apply to the thing that contained it. Since the data&apos;s root doesn&apos;t contain a collection at key <code>:flamingo</code>, the predicate is unpaired, and thus ignored.</p><p>If we did want to apply <code>coll?</code> to the root, it needs to be contained directly in the root. We&apos;ll associate <code>coll?</code> to key <code>:emu</code>.</p><pre><code>(validate-collections {:a [99], :b (list 77)}
&nbsp;                     {:a [vector?], :emu coll?})
;; =&gt; ({:datum [99],
;;      :ordinal-path-datum [:a],
;;      :path-datum [:a],
;;      :path-predicate [:a 0],
;;      :predicate vector?,
;;      :valid? true}
;;     {:datum {:a [99], :b (77)},
;;      :ordinal-path-datum [],
;;      :path-datum [],
;;      :path-predicate [:emu],
;;      :predicate coll?,
;;      :valid? true})</code></pre><p>Now, <code>coll?</code>&apos;s immediate container is the root. Since it is now properly paired with a collection, it participates in validation.</p><p>We&apos;ve churned through a ton of examples to reinforce the underlying mechanics of collection validation. But don&apos;t get overwhelmed by the drudgery. The vast majority of the time, we will be well-served to remember just these ideas while validating collections.</p><ol><li>Shape the specification to mimic the data (Motto #2).</li><li>Predicates apply to the collections that contain them.</li><li>To validate a map, associate the predicate to a key that doesn&apos;t appear in the data.</li><li>When collections are nested in a sequential collection, the predicates are applied to their immediate parent, in the order as if there were no intervening scalars in the ancestor.</li><li>Collections nested in maps are not affected by order.</li></ol><p>All the detailed mechanics we&apos;ve discussed in this section have been to support those five ideas.</p><p>Two more additional notes.</p><ul><li>When we worked through the collection validation algorithm by hand, we discussed it in terms of &apos;steps&apos; and &apos;phases&apos;, etc., that have a strong imperative flavor. However, the implementation is purely functional. The &apos;steps&apos; and &apos;phases&apos; are merely one way to understand the consequences of the way Speculoos handles pairing predicates and their targets.</li><li>Our examples showed validating collections nested at most one level deep, e.g., a map nested in a vector. However, the algorithm is fully general. We can validate any element of any arbitrary depth, of any mixture of Clojure collection types. Just to show off:</li><pre><code>(validate-collections [99 88 77 {:x (list 66 55 {:y [44 33 22 11 #{42}]})}]
&nbsp;                     [{:x (list {:y [#{set?}]})}])
;; =&gt; ({:datum #{42},
;;      :ordinal-path-datum [0 :x 0 :y 0],
;;      :path-datum [3 :x 2 :y 4],
;;      :path-predicate [0 :x 0 :y 0 set?],
;;      :predicate set?,
;;      :valid? true})</code></pre><p>From the outset, I intended Speculoos to be capable of validating any <a href="#HANDS">heterogeneous, arbitrarily nested data structure</a>.</p></ul><section id="collection-predicate-paths"><h3>Why the collection validation algorithm is different from the scalar validation algorithm</h3><p>The algorithm implemented by <code>validate-collections</code> is <em>slightly</em> different from <code>validate-scalars</code>. It has to do with the fact that a scalar in the data can occupy the exact same path as a predicate in the specification. A function, after all, is also a scalar. To be fully general (i.e., handle any pattern and depth of nesting), a collection in the data can not share a path with a predicate in the specification.</p><p>To begin, we&apos;ll intentionally take a wrong turn to show why the collection validation algorithm is a little bit different from the scalar validation algorithm. As before, we want to specify that our data vector is exactly <code>n</code> elements long. Recall these predicates.</p><pre><code>(defn len-3? [c] (= 3 (count c)))</code><br /><code>(defn len-2? [c] (= 2 (count c)))</code><br /><code>(defn len-1? [c] (= 1 (count c)))</code></pre><p>We&apos;re interested in validating the root collection, at path <code>[]</code> in the data, so at first, we&apos;ll naively try to put our <code>len-3?</code> predicate at path <code>[]</code> in the specification.</p><p>We could then invoke some imaginary collection validation function that treats bare, free-floating predicates as being located at path <code>[]</code>.</p><pre><code>;; this fn doesn&apos;t actually exist</code><br /><br /><code>(imaginary-validate-collection [42 &quot;abc&quot; 22/7]
&nbsp;                              len-3?)
;; =&gt; true</code></pre><p>Okay, that scenario maybe kinda sorta could work. By policy, <code>imaginary-validate-collection</code> could consider a bare predicate as being located at path <code>[]</code> in the specification, and therefore would apply to the root collection at path <code>[]</code> in the data.</p><p>But consider this scenario: <em>A two-element vector nested within a two-element vector</em>. One example of that data looks like this.</p><pre><code>[42 [&quot;abc&quot; 22/7]]</code></pre><p>Let&apos;s take a look at the paths.</p><pre><code>(all-paths [42 [&quot;abc&quot; 22/7]])
;; =&gt; [{:path [], :value [42 [&quot;abc&quot; 22/7]]}
;;     {:path [0], :value 42}
;;     {:path [1], :value [&quot;abc&quot; 22/7]}
;;     {:path [1 0], :value &quot;abc&quot;}
;;     {:path [1 1], :value 22/7}]</code></pre><p>We&apos;re validating collections, so we&apos;re only interested in the root collection at path <code>[]</code> and the nested vector at path <code>[1]</code>.</p><pre><code>[{:path [], :value [42 [&quot;abc&quot; 22/7]]}
&nbsp;{:path [1], :value [&quot;abc&quot; 22/7]}]</code></pre><p>And now we run into an problem: How do we compose a specification with two predicates, one at <code>[]</code> and one at <code>[1]</code>? The predicate aimed at the root collection has already absorbed, by policy, the root path, so there&apos;s nowhere to &apos;put&apos; the second predicate.</p><pre><code>;; this fn doesn&apos;t actually exist</code><br /><br /><code>(imaginary-validate-collection [42 [&quot;abc&quot; 22/7]]
&nbsp;                              len-3?
&nbsp;                              len-2?)
;; =&gt; true</code></pre><p>Because the <code>len-3?</code> predicate absorbs the <code>[]</code> path to root, and because predicates are not themselves collections and cannot &apos;contain&apos; something else, the second predicate, <code>len-2?</code>, needs to also be free-floating at the tail of the argument list. Our <code>imaginary-validate-collections</code> would have to somehow figure out that predicate <code>len-3?</code> ought to be paired with the root collection, <code>[42 [&quot;abc&quot; 22/7]</code> and predicate <code>len-2?</code> ought to be paired with the nested vector <code>[&quot;abc&quot; 22/7]</code>.</p><p>It gets even worse if we have another level of nesting. How about three vectors, each nested within another?</p><pre><code>[42 [ &quot;abc&quot; [22/7]]]</code></pre><p>The paths for that.</p><pre><code>(all-paths [42 [&quot;abc&quot; [22/7]]])
;; =&gt; [{:path [], :value [42 [&quot;abc&quot; [22/7]]]}
;;     {:path [0], :value 42}
;;     {:path [1], :value [&quot;abc&quot; [22/7]]}
;;     {:path [1 0], :value &quot;abc&quot;}
;;     {:path [1 1], :value [22/7]}
;;     {:path [1 1 0], :value 22/7}]</code></pre><p>Regarding only the data&apos;s collections, we see three elements to validate, at paths <code>[]</code>, <code>[1]</code>, and <code>[1 1]</code>. </p><pre><code>[{:path [], :value [42 [&quot;abc&quot; [22/7]]]}
&nbsp;{:path [1], :value [&quot;abc&quot; [22/7]]}
&nbsp;{:path [1 1], :value [22/7]}]</code></pre><p>Invoking the imaginary collection validator would have to look something like this.</p><pre><code>;; this fn doesn&apos;t actually exist</code><br /><br /><code>(imaginary-validate-collection [42 [&quot;abc&quot; [22/7]]]
&nbsp;                              len-3?
&nbsp;                              len-2?
&nbsp;                              len-1?)
;; =&gt; true</code></pre><p>Three free-floating predicates, with no indication of where they ought to be applied. The imaginary validator would truly need to read our minds to know which predicate pairs with which nested collection, if any.</p><p>Someone might propose that we include some paths immediately following each predicate to inform the imaginary validator where to apply those predicates.</p><pre><code>;; this fn doesn&apos;t actually exist</code><br /><br /><code>(imaginary-validate-collection-2 [42 [&quot;abc&quot; [22/7]]]
&nbsp;                                len-3? [0]
&nbsp;                                len-2? [1 0]
&nbsp;                                len-1? [1 1 0])
;; =&gt; true</code></pre><p>That certainly works, but at that point, we&apos;ve manually serialized a nested data structure. I wouldn&apos;t want to have to write out the explicit paths of more than a few predicates. Furthermore, writing separate, explicit paths could be error-prone, and not terribly re-usable, nor compact. One of Speculoos&apos; goals is to make composing specifications intuitive. I find writing specifications with data structure literals expressive and straightforward to manipulate.</p><p>Here&apos;s that same specification, written as a literal data structure.</p><pre><code>[len-3? [len-2? [len-1?]]</code></pre><p>Visually, that specification looks a lot like the data. If we know the rule about predicates applying to their immediate parent containers during collection validation, that specification carries meaning. And, we can slice and dice it any way we&apos;d like with <code>assoc-in</code>, or any other standard tool.</p><p>Here is the collection validation, Speculoos-style, with data in the upper row, specification literal in the lower row.</p><pre><code>(validate-collections [42 [&quot;abc&quot; [22/7]]]
&nbsp;                     [len-3? [len-2? [len-1?]]])
;; =&gt; ({:datum [42 [&quot;abc&quot; [22/7]]],
;;      :ordinal-path-datum [],
;;      :path-datum [],
;;      :path-predicate [0],
;;      :predicate len-3?,
;;      :valid? false}
;;     {:datum [&quot;abc&quot; [22/7]],
;;      :ordinal-path-datum [0],
;;      :path-datum [1],
;;      :path-predicate [1 0],
;;      :predicate len-2?,
;;      :valid? true}
;;     {:datum [22/7],
;;      :ordinal-path-datum [0 0],
;;      :path-datum [1 1],
;;      :path-predicate [1 1 0],
;;      :predicate len-1?,
;;      :valid? true})</code></pre><p>Speculoos&apos; <a href="#mottos">Motto #2</a> is <em>Shape the specification to mimic the data</em>. The arrangement of our collection predicates inside a structure literal will instruct <code>validate-collections</code> where to apply the predicates. The advantage Speculoos offers is the fact that literals are easy for humans to inspect, understand, and manipulate.</p></section></section><section id="valid-thorough"><h2>Validation Summaries, Combo Validations, and Thorough Validations</h2><p>Up until now, we&apos;ve been using <code>validate-scalars</code> and <code>validate-collections</code>, because they&apos;re verbose. For teaching and learning purposes (and for <a href="#troubleshooting">diagnosing problems</a>), it&apos;s useful to see all the information considered by the validators. However, in many situations, once we&apos;ve got our specification shape nailed down, we&apos;ll want a cleaner <em>yes</em> or <em>no</em> answer on whether the data satisfied the specification. We could certainly pull out the non-truthy, invalid results ourselves…</p><pre><code>(filter #(not (:valid? %))
&nbsp; (validate-scalars [42 &quot;abc&quot; 22/7]
&nbsp;                   [int? symbol? ratio?]))
;; =&gt; ({:datum &quot;abc&quot;,
;;      :path [1],
;;      :predicate symbol?,
;;      :valid? false})</code></pre><p>…and then check for invalids ourselves…</p><pre><code>(empty? *1) ;; =&gt; false</code></pre><p>…but Speculoos provides a function that does exactly that, both for scalars…</p><pre><code>(require &apos;[speculoos.core :refer [valid-scalars? valid-collections?]])</code><br /><br /><code>(valid-scalars? [42 &quot;abc&quot; 22/7]
&nbsp;               [int? symbol? ratio?])
;; =&gt; false</code></pre><p>…and for collections.</p><pre><code>(valid-collections? [42 [&quot;abc&quot;]]
&nbsp;                   [vector? [vector?]])
;; =&gt; true</code></pre><p>Whereas the <code>validate-…</code> functions return a detailed validation report of every predicate+datum pair they see, the <code>valid-…?</code> variants provide a plain <code>true/false</code>.</p><p>Beware: Validation only considers paired predicates+datums (Motto #3). If our datum doesn&apos;t have a paired predicate, then it won&apos;t be validated. Observe.</p><pre><code>(valid-scalars? {:a 42}
&nbsp;               {:b string?}) ;; =&gt; true</code><br /><br /><code>(validate-scalars {:a 42}
&nbsp;                 {:b string?}) ;; =&gt; []</code></pre><p><code>42</code> does not share a path with <code>string?</code>, the lone predicate in the specification. Since there are zero invalid results, <code>valid-scalars?</code> returns <code>true</code>.</p><p><strong>» Within the Speculoos library, <code>valid?</code> means <em> zero invalids. «</em></strong></p><p>If you feel uneasy about this definition of &apos;valid&apos; — that, somehow, you wouldn&apos;t be able to accomplish some particular validation task — rest easy. Speculoos provides us with facilities for ensuring that every datum is validated.</p><h3 id="thorough">Thorough validation</h3><p>Motto #3 reminds us that data elements not paired with a predicate are ignored. For some tasks, we may want to ensure that all elements in the data are subjected to at least one predicate. Plain <code>valid?</code>  only reports if all datum+predicate pairs are <code>true</code>.</p><pre><code>(valid-scalars? [42 &quot;abc&quot; 22/7]
&nbsp;               [int?]) ;; =&gt; true</code></pre><p>In this example, only <code>42</code> and <code>int?</code> form a pair that is validated. <code>&quot;abc&quot;</code> and <code>22/7</code> are not paired with predicates, and therefore ignored. <code>valid-scalars</code> returns <code>true</code> regardless of the ignored scalars.</p><p>Speculoos&apos; <em>thorough</em> function <a href="#fn-terminology">variants</a> require that all data elements be specified, otherwise, they return <code>false</code>. Thoroughly validating that same data with that same specification shows the difference.</p><pre><code>(require &apos;[speculoos.utility :refer [thoroughly-valid-scalars?]])</code><br /><br /><code>(thoroughly-valid-scalars? [42 &quot;abc&quot; 22/7]
&nbsp;                          [int?])
;; =&gt; false</code></pre><p>Whereas <code>valid-scalars?</code> ignored the un-paired <code>&quot;abc&quot;</code> and <code>22/7</code>, <code>thoroughly-valid-scalars?</code> notices that neither have a predicate. Even though <code>42</code> satisfied <code>int?</code>, the un-paired scalars mean that this validation is not thorough, and thus <code>thoroughly-valid-scalars?</code> returns <code>false</code>.</p><p>The <code>utility</code> <a href="#utilities">namespace</a> provides a thorough variant for collections, as well as a variant for <a href="#combo">combo</a> validations. <code>thoroughly-valid-collections?</code> works analogously to what we&apos;ve just seen.</p><p>Let&apos;s do a quick preview of a <a href="#combo">combo</a> validation. A combo validation is a convenient way to validate the scalars, and then separately validate the collections, of some data with a single function invocation. First, the &apos;plain&apos;, non-thorough version. The data occupies the top row (i.e., first argument), the scalar specification occupies the middle row, and the collection specification occupies the lower row.</p><pre><code>(valid? [42 &quot;abc&quot; 22/7]
&nbsp;       [int?]
&nbsp;       [vector?]) ;; =&gt; true</code></pre><p>We validated the single vector, and only one out of the three scalars. <code>valid?</code> only considers paired elements+predicates, so it only validated <code>42</code>, a scalar, and the root vector, a collection. <code>valid?</code> ignored scalars <code>&quot;abc&quot;</code> and <code>22/7</code>.</p><p>The thorough variant, <code>thoroughly-valid?</code>, however, does not ignore un-paired data elements. The function signatures is identical: data on the top row, scalar specification on the middle row, and the collection specification on the lower row.</p><pre><code>(require &apos;[speculoos.utility :refer [thoroughly-valid?]])</code><br /><br /><code>(thoroughly-valid? [42 &quot;abc&quot; 22/7]
&nbsp;                  [int?]
&nbsp;                  [vector?])
;; =&gt; false</code></pre><p>Even though both predicates, <code>int?</code> and <code>vector?</code>, were satisfied, <code>thoroughly-valid?</code> requires that all data elements be validated. Since <code>42</code> and <code>22/7</code> are un-paired, the entire validation returns <code>false</code>.</p><p>Note: Thoroughly validating does not ensure any measure of correctness nor rigor. &apos;Thorough&apos; merely indicates that each element was exposed to <em>some</em> kind of predicate. That predicate could actually be trivially permissive. In the next example, <code>any?</code> returns <code>true</code> for all values.</p><pre><code>(thoroughly-valid? [42 &quot;abc&quot; 22/7]
&nbsp;                  [any? any? any?]
&nbsp;                  [any?])
;; =&gt; true</code></pre><p>The only thing <code>thoroughly-valid?</code> tells us in this example is that the one vector and all three scalars were paired with a predicate, and that all four data elements satisfied a guaranteed-to-be-satisfied predicate.</p><p>Validation is only as good as the predicate. It&apos;s our responsibility to write a proper predicate.</p><h3 id="combo">Combo validation</h3><p>Validating scalars separately from validating collections is a core principle (Motto #1) embodied by the Speculoos library. Separating the two into distinct processes carries solid advantages because the specifications are more straightforward, the mental model is clearer, the implementation code is simpler, and it makes validation <em>à la carte</em>. Much of the time, we can probably get away with just a scalar specification.</p><p>All that said, it is not possible to specify and validate every aspect of our data with only scalar validation or only collection validation. When we really need to be strict and validate both scalars and collections, we could manually combine the two validations like this.</p><pre><code>(and (valid-scalars? [42] [int?])
&nbsp;    (valid-collections? [42] [vector?]))
;; =&gt; true</code></pre><p>Speculoos provides a pre-made utility that does exactly that. We supply some data, then a scalar specification, then a collection specification.</p><pre><code>(require &apos;[speculoos.core :refer [valid? validate]])</code><br /><br /><code>(valid? [42]
&nbsp;       [int?]
&nbsp;       [vector?]) ;; =&gt; true</code></pre><p>Let me clarify what <code>valid?</code> is doing here, because it is <em>not</em> violating the first Motto about separately validating scalars and collections. First, <code>valid?</code> performs a scalar validation on the data, and puts that result on the shelf. Then, in a completely distinct operation, it performs a collection validation. <code>valid?</code> then pulls the scalar validation results off the shelf and combines it with the collection validation results, and returns a singular <code>true/false</code>.  (Look back at the first example of this sub-section to see the separation.)</p><p>As an affirmation to how much I believe this, I reserved the shortest, most mnemonic function name, <code>valid?</code> to encourage Speculoos users to validate both scalars and collections, but separately.</p><p>Speculoos also provides a variant that returns detailed validation results after performing distinct scalar validation and collection validation.</p><pre><code>(validate [42 &quot;abc&quot; 22/7]
&nbsp;         [int? symbol? ratio?]
&nbsp;         [vector?])
;; =&gt; ({:datum 42,
;;      :path [0],
;;      :predicate int?,
;;      :valid? true}
;;     {:datum &quot;abc&quot;,
;;      :path [1],
;;      :predicate symbol?,
;;      :valid? false}
;;     {:datum 22/7,
;;      :path [2],
;;      :predicate ratio?,
;;      :valid? true}
;;     {:datum [42 &quot;abc&quot; 22/7],
;;      :ordinal-path-datum [],
;;      :path-datum [],
;;      :path-predicate [0],
;;      :predicate vector?,
;;      :valid? true})</code></pre><p><code>validate</code> gives us the exact results as if we had run <code>validate-scalars</code> and then immediately thereafter <code>validate-collections</code>. <code>validate</code> merely provides us the convenience of quickly running both in succession without having to re-type the data. With one invocation, we can validate <em>all</em> aspects of our data, both scalars and collections, and we never violated Motto #1.</p><h3 id="fn-terminology">Function Naming Conventions</h3><p>Here are the general patterns regarding the function names.<ul><li><strong><code>validate-…</code></strong> functions return a detailed report for every datum+predicate pair.</li><li><strong><code>valid-…?</code></strong> functions return <code>true</code> if the predicate+datum pairs produce zero falsey results, <code>false</code> otherwise.</li><li><strong><code>…-scalars</code></strong> functions consider only non-collection datums.</li><li><strong><code>…-collections</code></strong> functions consider only non-scalar datums.</li><li><strong><code>thoroughly-…</code></strong> functions return <code>true</code> only if every element (scalar or collection, as the case may be) is paired with a predicate, and every element satisfies its predicate.</li></ul>&apos;Plain&apos; functions (i.e., <code>validate</code>, <code>valid?</code>, and <code>thoroughly-valid?</code>) perform a scalar validation, followed by performing a distinct collection validation, and returns a single comprehensive response that merges the results of both.</p><p>Here&apos;s how those terms are put together, and what they do.</p><table><tr><th>function</th><th>checks…</th><th>returns…</th><th>note</th></tr><tr><td><code>validate-scalars</code></td><td>scalars only</td><td>detailed validation report</td><td></td></tr><tr><td><code>valid-scalars?</code></td><td>scalars only</td><td><code>true/false</code></td><td></td></tr><tr><td><code>thoroughly-valid-scalars?</code></td><td>scalars only</td><td><code>true/false</code></td><td>only <code>true</code> if all scalars paired with a predicate</td></tr><tr><td><code>validate-collections</code></td><td>collections only</td><td>detailed validation report</td><td></td></tr><tr><td><code>valid-collections?</code></td><td>collections only</td><td><code>true/false</code></td><td></td></tr><tr><td><code>thoroughly-valid-collections?</code></td><td>collections only</td><td><code>true/false</code></td><td>only <code>true</code> if all collections paired with a predicate</td></tr><tr><td><code>validate</code></td><td>scalars, then collections, separately</td><td>detailed validation report</td><td></td></tr><tr><td><code>valid?</code></td><td>scalars, then collections, separately</td><td><code>true/false</code></td><td></td></tr><tr><td><code>thoroughly-valid?</code></td><td>scalars, then collections separately</td><td><code>true/false</code></td><td>only <code>true</code> if all datums paired with a predicate</td></tr></table></section><section id="function-validation"><h2>Specifying and Validating Functions</h2><p>Being able to validate Clojure data enables us to check the usage and behavior of functions.</p><ol><li><strong>Validating arguments</strong> Speculoos can validate any property of the arguments passed to a function when it is invoked. We can ask questions like <em>Is the argument passed to the function a number?</em>, a scalar validation, and <em>Are there an even number of arguments?</em>, a collection validation.</li><li><strong>Validating return values</strong> Speculoos can validate any property of the value returned by a function. We can ask questions like <em>Does the function return a four-character string?</em>, a scalar validation, and <em>Does the function return a map containing keys <code>:x</code> and <code>:y</code></em>, a collection validation.</li><li><strong>Validating function correctness</strong> Speculoos can validate the correctness of a function in two ways.<ul><li>Speculoos can validate the <em>relationships</em> between the arguments and the function&apos;s return value. We can ask questions like <em>Is each of the three integers in the return value larger than the three integers in the arguments?</em>, a scalar validation, and <em>Is the return sequence the same length as the argument sequence, and are all the elements in reverse order?</em>, a collection validation.</li><li>Speculoos can <em>exercise</em> a function. This allows us to check <em>If we give this function one thousand randomly-generated valid inputs, does the function always produce a valid return value?</em> Exercising functions with randomly-generated samples is described in the </li><a href="#exercising">next section</a>.</ul></li></ol><p>None of those six checks are strictly required. Speculoos will happily validate using only the specifications we provide.</p><h3 id="fn-args">1. Validating Function Arguments</h3><p>When we invoke a function with a series of arguments, that series of values forms a sequence, which Speculoos can validate like any other heterogeneous, arbitrarily-nested data structure. Speculoos offers <a href="#explicit">a trio</a> of function-validating functions with differing levels of explicitness. We&apos;ll be primarily using <code>validate-fn-with</code> because it is the most explicit of the trio, and we can most easily observe what&apos;s going on.</p><p>Let&apos;s pretend we want to validate the arguments to a function <code>sum-three</code> that expects three integers and returns their sum.</p><pre><code>(require &apos;[speculoos.function-specs :refer [validate-fn-with]])</code><br /><br /><code>(defn sum-three [x y z] (+ x y z))</code><br /><br /><code>(sum-three 1 20 300) ;; =&gt; 321</code></pre><p>The argument list is a <em>sequence</em> of values, in this example, a sequential thing of three integers. We can imagine a <a href="#scalar-validation">scalar specification</a> for just such a sequence.</p><pre><code>[int? int? int?]</code></pre><p>When using <code>validate-fn-with</code>, we supply the function name, a map containing zero or more specifications, and some trailing <code>&amp;-args</code> as if they had been supplied directly to the function. Here&apos;s the function signature, formatted the way we&apos;ll be seeing in the upcoming discussion. The function name will appear in the top row (i.e., first argument), a specification organizing map in the second row, followed by zero or more arguments to be supplied to the function being validated.</p><pre><code>(validate-fn-with </code><code><em>function-name</em></code><br /><code><em>                  specification-organizing-map</em></code><br /><code><em>                  argument-1</em></code><br /><code><em>                  argument-2</em></code><br /><code>                  ⋮</code><br /><code><em>                  argument-n</em></code><code>)</code></pre><p>Speculoos can validate five aspects of a function using up to five specifications, each specification associated in that map to a particular key. We&apos;ll cover each of those five aspects in turn. To start, we want to specify the <em>argument scalars</em>.</p><p>Instead of individually passing each of those five specifications to <code>validate-fn-with</code> and putting <code>nil</code> placeholders where we don&apos;t wish to supply a specification, we organize the specifications. To do so, we associate the arguments&apos; scalar specification to the qualified key <code>:speculoos/arg-scalar-spec</code>.</p><pre><code>{:speculoos/arg-scalar-spec [int? int? int?]}</code></pre><p>Then, we validate the arguments to <code>sum-three</code> like this.</p><pre><code>(validate-fn-with sum-three
&nbsp;                 {:speculoos/arg-scalar-spec [int? int? int?]}
&nbsp;                 1
&nbsp;                 20
&nbsp;                 300)
;; =&gt; 321</code></pre><p>The arguments conformed to the scalar specification, so <code>validate-fn-with</code> returns the value produced by <code>sum-three</code>. Let&apos;s intentionally invoke <code>sum-three</code> with one invalid argument by swapping integer <code>1</code> with a floating-point <code>1.0</code>.</p><pre><code>(validate-fn-with sum-three
&nbsp;                 {:speculoos/arg-scalar-spec [int? int? int?]}
&nbsp;                 1.0
&nbsp;                 20
&nbsp;                 300)
;; =&gt; ({:datum 1.0,
;;      :fn-spec-type :speculoos/argument,
;;      :path [0],
;;      :predicate int?,
;;      :valid? false})</code></pre><p>Hey, that kinda <a href="#scalar-validation">looks familiar</a>. It looks a lot like something <code>validate-scalars</code> would emit if we filtered to keep only the invalids. We see that <code>1.0</code> at path <code>[0]</code> failed to satisfy its <code>int?</code> scalar predicate. We can also see that the function specification type is <code>:speculoos/argument</code>. Since Speculoos can validate scalars and collections of both arguments and return values, that key-val is a little signpost to help us pinpoint exactly what and where. Let&apos;s invoke <code>sum-three</code> with a second invalid argument, a ratio <code>22/7</code> instead of integer <code>300</code>.</p><pre><code>(validate-fn-with sum-three
&nbsp;                 {:speculoos/arg-scalar-spec [int? int? int?]}
&nbsp;                 1.0
&nbsp;                 20
&nbsp;                 22/7)
;; =&gt; ({:datum 1.0,
;;      :fn-spec-type :speculoos/argument,
;;      :path [0],
;;      :predicate int?,
;;      :valid? false}
;;     {:datum 22/7,
;;      :fn-spec-type :speculoos/argument,
;;      :path [2],
;;      :predicate int?,
;;      :valid? false})</code></pre><p>In addition to the invalid <code>1.0</code> at path <code>[0]</code>, we see that <code>22/7</code> at path <code>[2]</code> also fails to satisfy its <code>int?</code> scalar predicate. The scalar predicate&apos;s path in the scalar specification is the same as the path of the <code>22/7</code> in the <code>[1.0 20 22/7]</code> sequence of arguments. Roughly, <code>validate-fn-with</code> is doing something like this…</p><pre><code>(speculoos.core/only-invalid
&nbsp; (validate-scalars [1.0 20 22/7]
&nbsp;                   [int? int? int?]))
;; =&gt; ({:datum 1.0,
;;      :path [0],
;;      :predicate int?,
;;      :valid? false}
;;     {:datum 22/7,
;;      :path [2],
;;      :predicate int?,
;;      :valid? false})</code></pre><p>…validating scalars with <code>validate-scalars</code> and keeping only the invalids.</p><p>Okay, we see that term <em>scalar</em> buzzing around, so there must be something else about validating collections. Yup. We can also <a href="#collection-validation">validate collection</a> properties of the argument sequence. Let&apos;s specify that the argument sequence must contain three elements, using a custom collection predicate, <code>count-3?</code>.</p><pre><code>(defn count-3? [v] (= 3 (count v)))</code></pre><p>Let&apos;s simulate the collection validation first. Remember, collection predicates are applied to their parent containers, so <code>count-3?</code> must appear within a collection so that it&apos;ll be paired with the data&apos;s containing collection.</p><pre><code>(validate-collections [1 20 30]
&nbsp;                     [count-3?])
;; =&gt; ({:datum [1 20 30],
;;      :ordinal-path-datum [],
;;      :path-datum [],
;;      :path-predicate [0],
;;      :predicate count-3?,
;;      :valid? true})</code></pre><p>That result fits with <a href="#collection-validation">our discussion</a> about validating collections.</p><p>Next, we&apos;ll associate that collection specification into our function specification map at <code>:speculoos/arg-collection-spec</code> and invoke <code>validate-fn-with</code> with three valid arguments.</p><pre><code>(validate-fn-with sum-three
&nbsp;                 {:speculoos/arg-collection-spec [count-3?]}
&nbsp;                 1
&nbsp;                 20
&nbsp;                 300)
;; =&gt; 321</code></pre><p>The argument sequence satisfies our collection specification, so <code>sum-three</code> returns the expected value. Now let&apos;s repeat, but with an additional argument, <code>4000</code>, that causes the argument sequence to violate its collection predicate.</p><pre><code>(validate-fn-with sum-three
&nbsp;                 {:speculoos/arg-collection-spec [count-3?]}
&nbsp;                 1 20
&nbsp;                 300 4000)
;; =&gt; ({:datum [1 20 300 4000],
;;      :fn-spec-type :speculoos/argument,
;;      :ordinal-path-datum [],
;;      :path-datum [],
;;      :path-predicate [0],
;;      :predicate count-3?,
;;      :valid? false})</code></pre><p>This four-element argument sequence, <code>[1 20 300 4000]</code>, failed to satisfy our <code>count-3?</code> collection predicate, so <code>validate-fn-with</code> emitted a validation report.</p><p>Note #1: Invoking <code>sum-three</code> with four arguments would normally trigger an arity exception. <code>validate-fn-with</code> catches the exception and validates as much as it can.</p><p>Note #2: Don&apos;t specify and validate the <em>type</em> of the arguments container, i.e., <code>vector?</code>. That&apos;s an implementation detail and not guaranteed.</p><p>Let&apos;s get fancy and combine an argument scalar specification and an argument collection specification. Outside of the context of checking a function, that <a href="#combo">combo validation</a> would look like this: data is the first argument to <code>validate</code>, then the scalar specification on the next row, then the collection specification on the lower row.</p><pre><code>(speculoos.core/only-invalid
&nbsp; (validate [1.0 20 22/7 4000]
&nbsp;           [int? int? int?]
&nbsp;           [count-3?]))
;; =&gt; ({:datum 1.0,
;;      :path [0],
;;      :predicate int?,
;;      :valid? false}
;;     {:datum 22/7,
;;      :path [2],
;;      :predicate int?,
;;      :valid? false}
;;     {:datum [1.0 20 22/7 4000],
;;      :ordinal-path-datum [],
;;      :path-datum [],
;;      :path-predicate [0],
;;      :predicate count-3?,
;;      :valid? false})</code></pre><p>Let&apos;s remember: scalars and collections are <em>always</em> validated separately. <code>validate</code> is merely a convenience function that does both a scalar validation, then a collection validation, in discrete processes, with a single function invocation. Each of the first three scalars that paired with a scalar predicate were validated as scalars. The first and third scalars, <code>1.0</code> and <code>22/7</code>, failed to satisfy their respective predicates. The fourth argument, <code>4000</code>, was not paired with a scalar predicate and was therefore ignored (<a href="#mottos">Motto #3</a>). Then, the argument sequence as a whole was validated against the collection predicate <code>count-3?</code>.</p><p><code>validate-fn-with</code> performs substantially that combo validation. We&apos;ll associate the <strong>arg</strong>ument <strong>scalar</strong> <strong>spec</strong>ification with <code>:speculoos/arg-scalar-spec</code> and the <strong>arg</strong>ument <strong>collection</strong> <strong>spec</strong>fication with <code>:speculoos/arg-collection-spec</code> and pass the invalid, four-element argument sequence.</p><pre><code>(validate-fn-with sum-three
&nbsp;                 {:speculoos/arg-scalar-spec [int? int? int?],
&nbsp;                  :speculoos/arg-collection-spec [count-3?]}
&nbsp;                 1.0 20
&nbsp;                 22/7 4000)
;; =&gt; ({:datum 1.0,
;;      :fn-spec-type :speculoos/argument,
;;      :path [0],
;;      :predicate int?,
;;      :valid? false}
;;     {:datum 22/7,
;;      :fn-spec-type :speculoos/argument,
;;      :path [2],
;;      :predicate int?,
;;      :valid? false}
;;     {:datum [1.0 20 22/7 4000],
;;      :fn-spec-type :speculoos/argument,
;;      :ordinal-path-datum [],
;;      :path-datum [],
;;      :path-predicate [0],
;;      :predicate count-3?,
;;      :valid? false})</code></pre><p>Just as in the <code>validate</code> simulation, we see three items fail to satisfy their predicates. Scalars <code>1.0</code> and <code>22/7</code> are not integers, and the argument sequence as a whole, <code>[1.0 20 22/7 4000]</code>, does not contain exactly three elements, as required by its collection predicate, <code>count-3?</code>.</p><h3 id="fn-ret">2. Validating Function Return Values</h3><p>Speculoos can also validate values returned by a function. Reusing our <code>sum-three</code> function, and going back to valid inputs, we can associate a <strong>ret</strong>urn <strong>scalar</strong> <strong>spec</strong>ification into <code>validate-fn-with</code>&apos;s specification map to key <code>:speculoos/ret-scalar-spec</code>. Let&apos;s stipulate that the function returns an integer. Here&apos;s how we pass that specification to <code>validate-fn-with</code>.</p><pre><code>{:speculoos/ret-scalar-spec int?}</code></pre><p>And now, the function return validation.</p><pre><code>(validate-fn-with sum-three
&nbsp;                 {:speculoos/ret-scalar-spec int?}
&nbsp;                 1
&nbsp;                 20
&nbsp;                 300)
;; =&gt; 321</code></pre><p>The return value <code>321</code> satisfies <code>int?</code>, so <code>validate-fn-with</code> returns the computed sum.</p><p>What happens when the return value is invalid? Instead of messing up <code>sum-three</code>&apos;s definition, we&apos;ll merely alter the scalar predicate. Instead of an integer, we&apos;ll stipulate that <code>sum-three</code> returns a string with scalar predicate <code>string?</code>.</p><pre id="nil"><code>(validate-fn-with sum-three
&nbsp;                 {:speculoos/ret-scalar-spec string?}
&nbsp;                 1
&nbsp;                 20
&nbsp;                 300)
;; =&gt; ({:datum 321,
;;      :fn-spec-type :speculoos/return,
;;      :path nil,
;;      :predicate string?,
;;      :valid? false})</code></pre><p>Very nice. <code>sum-three</code> computed, quite correctly, the sum of the three arguments. But we gave it a bogus return scalar specification that claimed it ought to be a string, which integer <code>321</code> fails to satisfy.</p><p>Did you happen to notice the <code>path</code>? We haven&apos;t yet encountered a case where a path is <code>nil</code>. In this situation, the function returns a &apos;bare&apos; scalar, not contained in a collection. Speculoos can validate a bare scalar when that bare scalar is a function&apos;s return value.</p><p>Let&apos;s see how to validate a function when the return value is a collection of scalars. We&apos;ll write a new function, <code>enhanced-sum-three</code>, that returns four scalars: the three arguments and their sum, all contained in a vector.</p><pre><code>(defn enhanced-sum-three [x y z] [x y z (+ x y z)])</code><br /><br /><code>(enhanced-sum-three 1 20 300) ;; =&gt; [1 20 300 321]</code></pre><p>Our enhanced function now returns a vector of four elements. Let&apos;s remind ourselves how we&apos;d manually validate that return value. If we decide we want <code>enhanced-sum-three</code> to return four integers, the scalar specification would look like this.</p><pre><code>[int? int? int? int?]</code></pre><p>The scalar specification is shaped like our data (Motto #2).</p><p>And the manual validation would look like this, with the data on the upper row, the scalar specification on the lower row.</p><pre><code>(validate-scalars [1 20 300 321]
&nbsp;                 [int? int? int? int?])
;; =&gt; [{:datum 1,
;;      :path [0],
;;      :predicate int?,
;;      :valid? true}
;;     {:datum 20,
;;      :path [1],
;;      :predicate int?,
;;      :valid? true}
;;     {:datum 300,
;;      :path [2],
;;      :predicate int?,
;;      :valid? true}
;;     {:datum 321,
;;      :path [3],
;;      :predicate int?,
;;      :valid? true}]</code></pre><p>Four paired scalars and scalar predicates yield four validation results. Let&apos;s see what happens when we validate the function return scalars.</p><pre><code>(validate-fn-with enhanced-sum-three
&nbsp;                 {:speculoos/ret-scalar-spec [int? int? int? int?]}
&nbsp;                 1
&nbsp;                 20
&nbsp;                 300)
;; =&gt; [1 20 300 321]</code></pre><p>Since we fed <code>validate-fn-with</code> a specification that happens to agree with those arguments, <code>enhanced-sum-three</code> returns its computed value, <code>[1 20 300 321]</code>.</p><p>Let&apos;s stir things up. We&apos;ll change the return scalar specification to something we know will fail: The first scalar a character, the final scalar a boolean.</p><pre><code>(validate-fn-with enhanced-sum-three
&nbsp;                 {:speculoos/ret-scalar-spec [char? int? int? boolean?]}
&nbsp;                 1
&nbsp;                 20
&nbsp;                 300)
;; =&gt; ({:datum 1,
;;      :fn-spec-type :speculoos/return,
;;      :path [0],
;;      :predicate char?,
;;      :valid? false}
;;     {:datum 321,
;;      :fn-spec-type :speculoos/return,
;;      :path [3],
;;      :predicate boolean?,
;;      :valid? false})</code></pre><p><code>enhanced-sum-three</code>&apos;s function body remained the same, and we fed it the same integers as before, but we fiddled with the return scalar specification so that the returned vector contained two scalars that failed to satisfy their respective predicates. <code>1</code> at path <code>[0]</code> does not satisfy its wonky scalar predicate <code>char?</code> at the same path. And <code>321</code> at path <code>[3]</code> does not satisfy fraudulent scalar predicate <code>boolean?</code> that shares its path.</p><p>Let&apos;s set aside validating scalars for a moment and validate a facet of <code>enhanced-sum-three</code>&apos;s return collection. First, we&apos;ll do a manual demonstration with <code>validate-collections</code>. Remember: Collection predicates apply to their immediate parent container. We wrote <code>enhanced-sum-three</code> to return a vector, but to make the validation produce something interesting to look at, we&apos;ll pretend we&apos;re expecting a list.</p><pre><code>(validate-collections [1 20 300 321]
&nbsp;                     [list?])
;; =&gt; ({:datum [1 20 300 321],
;;      :ordinal-path-datum [],
;;      :path-datum [],
;;      :path-predicate [0],
;;      :predicate list?,
;;      :valid? false})</code></pre><p>That collection validation aligns with our understanding. <code>[1 20 300 321]</code> is not a list. The <code>list?</code> collection predicate at path <code>[0]</code> in the specification was paired with the thing found at path <code>(drop-last [0])</code> in the data, which in this example is the root collection. We designed <code>enhanced-sum-three</code> to yield a vector, which failed to satisfy predicate <code>list?</code>.</p><p>Let&apos;s toss that collection specification at <code>validate-with-fn</code> and have it apply to <code>enhanced-sum-three</code>&apos;s return value, which won&apos;t satisfy. We pass the <strong>ret</strong>urn <strong>collection spec</strong>ification by associating it to the key <code>:speculoos/ret-collection-spec</code>.</p><pre><code>(validate-fn-with enhanced-sum-three
&nbsp;                 {:speculoos/ret-collection-spec [list?]}
&nbsp;                 1
&nbsp;                 20
&nbsp;                 300)
;; =&gt; ({:datum [1 20 300 321],
;;      :fn-spec-type :speculoos/return,
;;      :ordinal-path-datum [],
;;      :path-datum [],
;;      :path-predicate [0],
;;      :predicate list?,
;;      :valid? false})</code></pre><p>Similarly to the manual collection validation we previously performed with <code>validate-collections</code>, we see that <code>enhanced-sum-three</code>&apos;s return vector <code>[1 20 300 321]</code> fails to satisfy its <code>list?</code> collection predicate.</p><p>A scalar validation followed by an independent collection validation allows us to check every possible aspect that we could want. Now that we&apos;ve seen how to individually validate <code>enhance-sum-three</code>&apos;s return scalars and return collections, we know how to do both with one invocation.</p><p>Remember Motto #1: Validate scalars separately from validating collections. Speculoos will only ever do one or the other, but <code>validate</code> is a <a href="#combo">convenience function</a> that performs a scalar validation immediately followed by a collection validation. We&apos;ll re-use the scalar specification and collection specification from the previous examples.</p><pre><code>(speculoos.core/only-invalid
&nbsp; (validate [1 20 300 321]
&nbsp;           [char? int? int? boolean?]
&nbsp;           [list?]))
;; =&gt; ({:datum 1,
;;      :path [0],
;;      :predicate char?,
;;      :valid? false}
;;     {:datum 321,
;;      :path [3],
;;      :predicate boolean?,
;;      :valid? false}
;;     {:datum [1 20 300 321],
;;      :ordinal-path-datum [],
;;      :path-datum [],
;;      :path-predicate [0],
;;      :predicate list?,
;;      :valid? false})</code></pre><p><code>only-invalid</code> discards the validations where the predicates are satisfied, leaving only the invalids. Two scalars failed to satisfy their scalar predicates. Integer <code>1</code> at path <code>[0]</code> in the data fails to satisfy scalar predicate <code>char?</code> at path <code>[0]</code> in the scalar specification. Integer <code>321</code> fails to satisfy scalar predicate <code>boolean?</code> at path <code>[3]</code> in the scalar specification. Finally, our root vector <code>[1 20 300 321]</code> located at path <code>[]</code> fails to satisfy the collection predicate <code>list?</code> at path <code>[0]</code>.</p><p>Now that we&apos;ve seen the combo validation done manually, let&apos;s validate <code>enhanced-sum-three</code>&apos;s return in the same way. Here we see the importance of organizing the specifications in a container instead of passing them as individual arguments: it keeps our invocation neater.</p><pre><code>(validate-fn-with enhanced-sum-three
&nbsp;                 {:speculoos/ret-scalar-spec [char? int? int? boolean?],
&nbsp;                  :speculoos/ret-collection-spec [list?]}
&nbsp;                 1
&nbsp;                 20
&nbsp;                 300)
;; =&gt; ({:datum 1,
;;      :fn-spec-type :speculoos/return,
;;      :path [0],
;;      :predicate char?,
;;      :valid? false}
;;     {:datum 321,
;;      :fn-spec-type :speculoos/return,
;;      :path [3],
;;      :predicate boolean?,
;;      :valid? false}
;;     {:datum [1 20 300 321],
;;      :fn-spec-type :speculoos/return,
;;      :ordinal-path-datum [],
;;      :path-datum [],
;;      :path-predicate [0],
;;      :predicate list?,
;;      :valid? false})</code></pre><p><code>validate-fn-with</code>&apos;s validation is substantially the same as the one <code>validate</code> produced in the previous example, except now, the data comes from invoking <code>enhanced-sum-three</code>. Two scalar invalids and one collection invalid. Integer <code>1</code> fails to satisfy scalar predicate <code>char?</code>, integer <code>321</code> fails to satisfy scalar predicate <code>boolean?</code>, and the entire return vector <code>[1 20 300 321]</code> fails to satisfy collection predicate <code>list?</code>.</p><p>Okay. I think we&apos;re ready to put together all four different function validations we&apos;ve so far seen. We&apos;ve seen…</p><ul><li>a function argument scalar validation,</li><li>a function argument collection validation,</li><li>a function return scalar validation, and</li><li>a function return collection validation.</li></ul><p>And we&apos;ve seen how to combine both function argument validations, and how to combine both function return validations. Now we&apos;ll combine all four validations into one <code>validate-fn-with</code> invocation.</p><p>Let&apos;s review our ingredients. Here&apos;s our <code>enhanced-sum-three</code> function.</p><pre><code>(defn enhanced-sum-three [x y z] [x y z (+ x y z)])</code></pre><p><code>enhanced-sum-three</code> accepts three number arguments and returns a vector of those three numbers with their sum appended to the end of the vector. Technically, Clojure would accept any numeric thingy for <code>x</code>, <code>y</code>, and <code>z</code>, but for illustration purposes, we&apos;ll make our scalar predicates something non-numeric so we can see something interesting in the validation reports.</p><p>With that in mind, we pretend that we want to validate the function&apos;s argument sequence as a string, followed by an integer, followed by a symbol. The function scalar specification will be…</p><pre><code>[string? int? symbol?]</code></pre><p>To allow <code>enhanced-sum-three</code> to calculate a result, we&apos;ll supply three numeric values, two of which will not satisfy that argument scalar specification.</p><p>So that it produces something interesting, we&apos;ll make our function argument collection specification also complain. First, we&apos;ll write a collection predicate.</p><pre><code>(defn length-2? [v] (= 2 (count v)))</code></pre><p>We know for sure that the argument sequence will contain three values, so predicate <code>length-2?</code> will produce something interesting to see.</p><p>During collection validation, predicates apply to the collection in the data that corresponds to the collection that contains the predicate. We want our predicate, <code>length-2?</code> to apply to the argument sequence, so we&apos;ll insert it into a vector. Our argument collection specification will look like this.</p><pre><code>[length-2?]</code></pre><p>Jumping to <code>enhanced-sum-three</code>&apos;s output side, we expect a vector of four numbers. Again, we&apos;ll craft our function return scalar specification to contain two predicates that we know won&apos;t be satisfied because those scalar predicates are looking for something non-numeric.</p><pre><code>[char? int? int? boolean?]</code></pre><p>We know <code>enhanced-sum-three</code> will return a vector containing four integers, but the <code>char?</code> and <code>boolean?</code> will give us something to look at.</p><p>Finally, since we defined <code>enhanced-sum-three</code> to return a vector, we&apos;ll make the function return collection specification look for a list.</p><pre><code>[list?]</code></pre><p>Altogether, those four specification are organized like this.</p><pre><code>{:speculoos/arg-scalar-spec     [string? int? symbol?]
&nbsp;:speculoos/arg-collection-spec [#(= 2 (count %))]
&nbsp;:speculoos/ret-scalar-spec     [char? int? int? boolean?]
&nbsp;:speculoos/ret-collection-spec [list?]}</code></pre><p>It&apos;s time to see what we&apos;ve assembled.</p><pre><code>(validate-fn-with enhanced-sum-three
&nbsp;                 {:speculoos/arg-scalar-spec [string? int? symbol?],
&nbsp;                  :speculoos/arg-collection-spec [length-2?],
&nbsp;                  :speculoos/ret-scalar-spec [char? int? int? boolean?],
&nbsp;                  :speculoos/ret-collection-spec [list?]}
&nbsp;                 1
&nbsp;                 20
&nbsp;                 300)
;; =&gt; ({:datum 1,
;;      :fn-spec-type :speculoos/argument,
;;      :path [0],
;;      :predicate string?,
;;      :valid? false}
;;     {:datum 300,
;;      :fn-spec-type :speculoos/argument,
;;      :path [2],
;;      :predicate symbol?,
;;      :valid? false}
;;     {:datum [1 20 300],
;;      :fn-spec-type :speculoos/argument,
;;      :ordinal-path-datum [],
;;      :path-datum [],
;;      :path-predicate [0],
;;      :predicate length-2?,
;;      :valid? false}
;;     {:datum 1,
;;      :fn-spec-type :speculoos/return,
;;      :path [0],
;;      :predicate char?,
;;      :valid? false}
;;     {:datum 321,
;;      :fn-spec-type :speculoos/return,
;;      :path [3],
;;      :predicate boolean?,
;;      :valid? false}
;;     {:datum [1 20 300 321],
;;      :fn-spec-type :speculoos/return,
;;      :ordinal-path-datum [],
;;      :path-datum [],
;;      :path-predicate [0],
;;      :predicate list?,
;;      :valid? false})</code></pre><p>We&apos;ve certainly made a mess of things. But it&apos;ll be understandable if we examine the invalidation report piece by piece. The first thing to know is that we have already seen each of those validations before in the previous examples, so we could always scroll back to those examples above and see the validations in isolation.</p><p>We see six non-satisfied predicates:<ul><li>Scalar <code>1</code> in the arguments sequence fails to satisfy scalar predicate <code>string?</code> in the argument scalar specification.</li><li>Scalar <code>300</code> in the arguments sequence fails to satisfy scalar predicate <code>symbol?</code> in the argument scalar specification.</li><li>The argument sequence <code>[1 20 300]</code> fails to satisfy collection predicate <code>length-2?</code> in the argument collection specification.</li><li>Scalar <code>1</code> in the return vector fails to satisfy scalar predicate <code>char?</code> in the return scalar specification.</li><li>Scalar <code>321</code> in the return vector fails to satisfy scalar predicate <code>boolean?</code> in the return scalar specification.</li><li>The return vector <code>[1 20 300 321]</code> fails to satisfy collection predicate <code>list?</code> in the return collection specification.</li></ul></p><p>Also note that the validation entries have a <code>:fn-spec-type</code> entry associated to either <code>:speculoos/return</code> or <code>:speculoos/argument</code>, which tells us where a particular invalid was located. There may be a situation where indistinguishable invalid datums appear in both the arguments and returns. In this case, integer <code>1</code> was an invalid datum at path <code>[0]</code> for both the argument sequence and the return vector. Keyword <code>:fn-spec-type</code> helps resolve the ambiguity.</p><h3 id="fn-correctness">3. Validating Function Correctness</h3><p>So far, we&apos;ve seen how to validate function argument sequences and function return values, both their scalars, and their collections. Validating function argument sequences allows us to check if the function was invoked properly. Validating function return values gives a limited ability to check the internal operation of the function.</p><p> If we want another level of thoroughness for checking correctness, we can specify and validate the <strong>relationships</strong> between the functions arguments and return values. Perhaps we&apos;d like to be able to express <em>The return value is a collection, with all the same elements as the input sequence.</em> Or <em>The return value is a concatenation of the even indexed elements of the input sequence.</em> Speculoos&apos; term for this action is <em>validating function argument and return value relationship</em>.</p><p>Let&apos;s pretend I wrote a reversing function, which accepts a sequential collection of elements and returns those elements in reversed order. If we give it…</p><pre><code>[11 22 33 44 55]</code></pre><p>…my reversing function ought to return…</p><pre><code>[55 44 33 22 11]</code></pre><p>Here are some critical features of that process that relate the reversing function&apos;s arguments to its return value.</p><ul><li>The return collection is the same length as the input collection.</li><li>The return collection contains all the same elements as the input collection.</li><li>The elements of the return collection appear in reverse order from their positions in the input collection.</li></ul><p>Oops. I must&apos;ve written it before I had my morning coffee.</p><pre><code>(defn broken-reverse [v] (conj v 9999))</code><br /><br /><code>(broken-reverse [11 22 33 44 55]) ;; =&gt; [11 22 33 44 55 9999]</code></pre><p>Pitiful. We can see by eye that <code>broken-reverse</code> fulfilled none of the three relationships. The return collection is not the same length, contains additional elements, and is not reversed. Let&apos;s codify that pitifulness.</p><p>First, we&apos;ll write three <a href="#relationship">relationship functions</a>. Relationship functions are a lot like predicates. They return a truthy or falsey value, but instead consume two things instead of one. The function&apos;s argument sequence is passed as the first thing and the function&apos;s return value is passed as the second thing.</p><p>The first predicate tests <em>Do two collections contain the same number of elements?</em></p><pre><code>(defn same-length? [v1 v2] (= (count v1) (count v2)))</code><br /><br /><code>(same-length? [11 22 33 44 55]
&nbsp;             [11 22 33 44 55]) ;; =&gt; true</code><br /><br /><code>(same-length? [11 22]
&nbsp;             [11 22 33 44 55]) ;; =&gt; false</code></pre><p>When supplied with two collections whose counts are the same, predicate <code>same-length?</code> returns <code>true</code>.</p><p>The second predicate tests <em>Do two collections contain the same elements?</em></p><pre><code>(defn same-elements? [v1 v2] (= (sort v1) (sort v2)))</code><br /><br /><code>(same-elements? [11 22 33 44 55]
&nbsp;               [55 44 33 22 11]) ;; =&gt; true</code><br /><br /><code>(same-elements? [11 22 33 44 55]
&nbsp;               [55 44 33 22 9999]) ;; =&gt; false</code></pre><p>When supplied with two collections which contain the same elements, predicate <code>same-elements?</code> returns <code>true</code>.</p><p>The third predicate tests <em>Do the elements of one collection appear in reversed order when compared to another collection?</em></p><pre><code>(defn reversed? [v1 v2] (= v1 (reverse v2)))</code><br /><br /><code>(reversed? [11 22 33 44 55]
&nbsp;          [55 44 33 22 11]) ;; =&gt; true</code><br /><br /><code>(reversed? [11 22 33 44 55]
&nbsp;          [11 22 33 44 55]) ;; =&gt; false</code></pre><p>When supplied with two collections, predicate <code>reversed?</code> returns <code>true</code> if the elements of the first collection appear in the reverse order relative to the elements of the second collection.</p><p><code>same-length?</code>, <code>same-element?</code>, <code>reversed?</code> all consume two sequential things and test a relationship between the two. If their relationship is satisfied, they signal <code>true</code>, if not, then they signal <code>false</code>. They are all three gonna have something unkind to say about <code>broken-reverse</code>.</p><p>Now that we&apos;ve established a few relationships, we need to establish <em>where</em> to apply those relationship tests. Checking <code>broken-reverse</code>&apos;s argument/return relationships with <code>same-length?</code>, <code>same-elements?</code>, and <code>reversed?</code> will be fairly straightforward: For each predicate, we&apos;ll pass the first argument (itself a collection), and the return value (also a collection). But some day, we might want to check a more sophisticated relationship that needs to extract some slice of the argument and/or slice of the return value. Therefore, we must declare a path to the slices we want to check. Of the return value, we&apos;d like to check the root collection, so the return value&apos;s path is merely <code>[]</code>.</p><p>When we consider how to extract the arguments, there&apos;s one tricky detail we must accommodate. The <code>[11 22 33 44 55]</code> vector we&apos;re going to pass to <code>broken-reverse</code> is itself contained in the argument sequence. Take a look.</p><pre><code>(defn arg-passthrough [&amp; args] args)</code><br /><br /><code>(arg-passthrough [11 22 33 44 55]) ;; =&gt; ([11 22 33 44 55])</code></pre><p>To extract <code>[11 22 33 44 55]</code>, the path will need to be <code>[0]</code>.</p><pre><code>(nth (arg-passthrough [11 22 33 44 55]) 0) ;; =&gt; [11 22 33 44 55]</code></pre><p>When invoked with paths <code>[0]</code> and <code>[]</code>, respectively for the arguments and returns, <code>validate-argument-return-relationship</code> does something like this.</p><pre><code>(same-length? (get-in [[11 22 33 44 55]] [0])
&nbsp;             (get-in [11 22 33 44 55 9999] []))
;; =&gt; false</code></pre><p>So here are the components to a single argument/return relationship validation.</p><ul><li>A path to the interesting slice of the arguments. Example: <code>[0]</code></li><li>A path to the interesting slice of the return value. Example: <code>[]</code></li><li>A relationship function. Example: <code>same-length?</code></li></ul><p>We stuff all three of those items into a map, which will be used for a single relationship validation.</p><pre><code>{:path-argument [0]
&nbsp;:path-return []
&nbsp;:relationship-fn same-length?}</code></pre><p>Within that map, both <code>:path-…</code> entries govern what slices of the argument and return are given to the relationship function. In this example, we want to extract the first item, at path <code>[0]</code>, of the argument sequence and the entire return value, at path <code>[]</code>.</p><p>We&apos;ve written three argument/function relationships to test <code>broken-reverse</code>, so we&apos;ll need to somehow feed them to <code>validate-fn-with</code>. We do that by associating them into the organizing map with keyword <code>:speculoos/argument-return-relationships</code>. Notice the plural <em>s</em>. Since there may be more than one relationship, we collect them into a vector. For the moment, let&apos;s insert only the <code>same-length?</code> relationship.</p><pre><code>{:speculoos/argument-return-relationships [{:path-argument [0]
&nbsp;                                           :path-return []
&nbsp;                                           :relationship-fn same-length?}]}</code></pre><p>Eventually, we&apos;ll test all three relationships, but for now, we&apos;ll focus on <code>same-length?</code>.</p><p>We&apos;re ready to validate.</p><pre><code>(validate-fn-with
&nbsp; broken-reverse
&nbsp; {:speculoos/argument-return-relationships
&nbsp;    [{:path-argument [0],
&nbsp;      :path-return [],
&nbsp;      :relationship-fn same-length?}]}
&nbsp; [11 22 33 44 55])
;; =&gt; ({:datum-argument [11 22 33 44 55],
;;      :datum-return [11 22 33 44 55 9999],
;;      :fn-spec-type :speculoos/argument-return-relationship,
;;      :path-argument [0],
;;      :path-return [],
;;      :relationship-fn same-length?,
;;      :valid? false})</code></pre><p>We supplied <code>broken-reverse</code> with a five-element vector, and it returned a six-element vector, failing to satisfy the specified <code>same-length?</code> relationship.</p><p>We wrote two other relationship functions, but <code>same-elements?</code> and <code>reversed?</code> are merely floating around in the current namespace. We did not send them to <code>validate-fn-with</code>, so it checked only <code>same-length?</code>, which we explicitly supplied. Remember Motto #3: Un-paired predicates (or, relationships in this instance) are ignored.</p><p>Let&apos;s check all three relationships now. We explicitly supply the additional two relationship predicates, all with the same paths.</p><pre><code>(validate-fn-with
&nbsp; broken-reverse
&nbsp; {:speculoos/argument-return-relationships
&nbsp;    [{:path-argument [0],
&nbsp;      :path-return [],
&nbsp;      :relationship-fn same-length?}
&nbsp;     {:path-argument [0],
&nbsp;      :path-return [],
&nbsp;      :relationship-fn same-elements?}
&nbsp;     {:path-argument [0],
&nbsp;      :path-return [],
&nbsp;      :relationship-fn reversed?}]}
&nbsp; [11 22 33 44 55])
;; =&gt; ({:datum-argument [11 22 33 44 55],
;;      :datum-return [11 22 33 44 55 9999],
;;      :fn-spec-type :speculoos/argument-return-relationship,
;;      :path-argument [0],
;;      :path-return [],
;;      :relationship-fn same-length?,
;;      :valid? false}
;;     {:datum-argument [11 22 33 44 55],
;;      :datum-return [11 22 33 44 55 9999],
;;      :fn-spec-type :speculoos/argument-return-relationship,
;;      :path-argument [0],
;;      :path-return [],
;;      :relationship-fn same-elements?,
;;      :valid? false}
;;     {:datum-argument [11 22 33 44 55],
;;      :datum-return [11 22 33 44 55 9999],
;;      :fn-spec-type :speculoos/argument-return-relationship,
;;      :path-argument [0],
;;      :path-return [],
;;      :relationship-fn reversed?,
;;      :valid? false})</code></pre><p><code>broken-reverse</code> is truly broken. The <code>same-length?</code> result appears again, and then we see the two additional unsatisfied relationships because we added <code>same-elements?</code> and <code>reversed?</code>. <code>broken-reverse</code> returns a vector with more and different elements, and the order is not reversed.</p><p>Just for amusement, let&apos;s see what happens when we validate <code>clojure.core/reverse</code> with the exact same relationship specifications.</p><pre><code>(reverse [11 22 33 44 55]) ;; =&gt; (55 44 33 22 11)</code><br /><br /><code>(validate-fn-with
&nbsp; reverse
&nbsp; {:speculoos/argument-return-relationships
&nbsp;    [{:path-argument [0],
&nbsp;      :path-return [],
&nbsp;      :relationship-fn same-length?}
&nbsp;     {:path-argument [0],
&nbsp;      :path-return [],
&nbsp;      :relationship-fn same-elements?}
&nbsp;     {:path-argument [0],
&nbsp;      :path-return [],
&nbsp;      :relationship-fn reversed?}]}
&nbsp; [11 22 33 44 55])
;; =&gt; (55 44 33 22 11)</code></pre><p><code>clojure.core/reverse </code> satisfies all three argument/return relationships, so <code>validate-fn-with</code> passes through the correctly-reversed output.</p><p>Not every function consumes a collection. Some functions consume a scalar value. Some functions return a scalar. And some functions have the audacity to do both. <code>validate-fn-with</code> can validate that kind of argument/return relationship. </p><p>I&apos;ll warn you now, I&apos;m planning on writing a buggy increment function. We could express two ideas about the argument/return relationship. First, a correctly-working increment function, when supplied with a number, <code>n</code>, ought to return a number that is larger than <code>n</code>. Second, a correctly-working return value ought to be <code>n</code> plus one. Let&apos;s specify those relationships.</p><p>The first predicate tests <em>Is the second number larger than the first number?</em> We don&apos;t need to write a special predicate for this job; <code>clojure.core</code> provides one that does everything we need.</p><pre><code>(&lt; 99 100) ;; =&gt; true</code><br /><code>(&lt; 99 -99) ;; =&gt; false</code></pre><p>When supplied with two (or more) numbers, predicate <code>&lt;</code> returns <code>true</code> if the second number is larger than the first number.</p><p>The second predicate tests <em>Is the second number equal to the first number plus one?</em></p><pre><code>(defn plus-one? [n1 n2] (= (+ n1 1) n2))</code><br /><br /><code>(plus-one? 99 100) ;; =&gt; true</code><br /><code>(plus-one? 99 -99) ;; =&gt; false</code></pre><p>When supplied with two numbers, predicate <code>plus-one?</code> returns <code>true</code> if the second number equals the sum of the first number and one.</p><p>Validating argument/return relationships requires us to declare which parts of the argument sequence and which parts of the return value to send to the relationship function. When we invoke the increment function with a single number, the number lives in the first spot of the argument sequence, so it will have a path of <code>[0]</code>. The increment function will return a &apos;bare&apos; number, so a path is not really an applicable concept. We <a href="#nil">previously saw</a> how a <code>nil</code> path indicates a bare scalar, so now we can assemble the two relationship maps, one each for <code>&lt;</code> and <code>plus-one?</code>.</p><pre><code>{:path-argument [0]
&nbsp;:path-return nil
&nbsp;:relationship-fn &lt;}</code><br /><br /><code>{:path-argument [0]
&nbsp;:path-return nil
&nbsp;:relationship-fn plus-one?}</code></pre><p>Now is a good time to write the buggy incrementing function.</p><pre><code>(defn buggy-inc [n] (- n))</code><br /><br /><code>(buggy-inc 99) ;; =&gt; -99</code></pre><p>Looks plenty wrong. Let&apos;s see exactly how wrong.</p><pre><code>(validate-fn-with
&nbsp; buggy-inc
&nbsp; {:speculoos/argument-return-relationships
&nbsp;    [{:path-argument [0],
&nbsp;      :path-return nil,
&nbsp;      :relationship-fn &lt;}
&nbsp;     {:path-argument [0],
&nbsp;      :path-return nil,
&nbsp;      :relationship-fn plus-one?}]}
&nbsp; 99)
;; =&gt; ({:datum-argument 99,
;;      :datum-return -99,
;;      :fn-spec-type :speculoos/argument-return-relationship,
;;      :path-argument [0],
;;      :path-return nil,
;;      :relationship-fn &lt;,
;;      :valid? false}
;;     {:datum-argument 99,
;;      :datum-return -99,
;;      :fn-spec-type :speculoos/argument-return-relationship,
;;      :path-argument [0],
;;      :path-return nil,
;;      :relationship-fn plus-one?,
;;      :valid? false})</code></pre><p><code>buggy-inc</code>&apos;s return value failed to satisfy both relationships with its argument. <code>-99</code> is not larger than <code>99</code>, nor is it what we&apos;d get by adding one to <code>99</code>.</p><p>Just to verify that our relationships are doing what we think they&apos;re doing, let&apos;s run the same thing on <code>clojure.core/inc</code>.</p><pre><code>(validate-fn-with
&nbsp; inc
&nbsp; {:speculoos/argument-return-relationships
&nbsp;    [{:path-argument [0],
&nbsp;      :path-return nil,
&nbsp;      :relationship-fn &lt;}
&nbsp;     {:path-argument [0],
&nbsp;      :path-return nil,
&nbsp;      :relationship-fn plus-one?}]}
&nbsp; 99)
;; =&gt; 100</code></pre><p><code>inc</code> correctly returns <code>100</code> when invoked with <code>99</code>, so both <code>&lt;</code> and <code>plus-one?</code> relationships are satisfied. Since all relationships were satisfied, the return value <code>100</code> passes through.</p><p>So far, the <code>:path-argument</code>s and the <code>:path-return</code>s have been similar between relationship specifications, but they don&apos;t need to be. I&apos;m going to invent a really contrived example. <code>pull-n-put</code> and <code>pull-n-whoops</code> are both intended to pull out emails and phone numbers and stuff them into some output vectors. Here&apos;s our raw person data: emails and phone numbers.</p><pre><code>(def person-1 {:email &quot;aragorn@sonofarath.org&quot;, :phone &quot;867-5309&quot;})</code><br /><code>(def person-2 {:email &quot;vita@meatavegam.info&quot;, :phone &quot;123-4567&quot;})</code><br /><code>(def person-3 {:email &quot;jolene@justbecauseyou.com&quot;, :phone &quot;555-FILK&quot;})</code></pre><p><code>pull-n-put</code> is the correct implementation, producing correct results.</p><pre><code>;; correct implementation</code><br /><br /><code>(defn pull-n-put
&nbsp; [p1 p2 p3]
&nbsp; {:email-addresses [(p1 :email) (p2 :email) (p3 :email)],
&nbsp;  :phone-numbers [(p1 :phone) (p2 :phone) (p3 :phone)]})</code><br /><br /><br /><code>;; intended results</code><br /><br /><code>(pull-n-put person-1 person-2 person-3)
;; =&gt; {:email-addresses [&quot;aragorn@sonofarath.org&quot;
;;                       &quot;vita@meatavegam.info&quot;
;;                       &quot;jolene@justbecauseyou.com&quot;],
;;     :phone-numbers [&quot;867-5309&quot; &quot;123-4567&quot; &quot;555-FILK&quot;]}</code></pre><p><code>pull-n-put</code> pulls out the email addresses and phone numbers and puts each at the proper place. However, <code>pull-n-whoops</code>…</p><pre><code>;; incorrect implementation</code><br /><br /><code>(defn pull-n-whoops
&nbsp; [p1 p2 p3]
&nbsp; {:email-addresses [(p1 :phone) (p2 :phone) (p3 :phone)],
&nbsp;  :phone-numbers [:apple :banana :mango]})</code><br /><br /><br /><code>;; wrong results</code><br /><br /><code>(pull-n-whoops person-1 person-2 person-3)
;; =&gt; {:email-addresses [&quot;867-5309&quot; &quot;123-4567&quot; &quot;555-FILK&quot;],
;;     :phone-numbers [:apple :banana :mango]}</code></pre><p>…does neither. <code>pull-n-whoops</code> puts the phone numbers where the email addresses ought to be and inserts completely bogus phone numbers.</p><p>We can specify a couple of relationships to show that <code>pull-n-whoops</code> produces a return value that does not validate. In a correctly-working implementation, the scalars aren&apos;t transformed, <em>per se</em>, merely moved to another location. So our relationship function will merely be equality, and the paths will do all the work.</p><p>Phone number <code>555-FILK</code> at argument path <code>[2 :phone]</code> ought to appear at return path <code>[:phone-numbers 2]</code>. That relationship specification looks like this.</p><pre><code>{:path-argument [2 :phone]
&nbsp;:path-return [:phone-numbers 2]
&nbsp;:relationship-fn =}</code></pre><p>Similarly, email address <code>aragorn@sonofarath.org</code> at argument path <code>[0 :email]</code> ought to appear at return path <code>[:email-addresses 0]</code>. That relationship specification looks like this.</p><pre><code>{:path-argument [0 :email]
&nbsp;:path-return [:phone-numbers 0]
&nbsp;:relationship-fn =}</code></pre><p>Now, we insert those two specifications into a vector and associate that vector into the organizing map.</p><pre><code>{:speculoos/argument-return-relationships [{:path-argument [2 :phone]
&nbsp;                                            :path-return [:phone-numbers 2]
&nbsp;                                            :relationship-fn =}]}
&nbsp;                                           {:path-argument [0 :email]
&nbsp;                                            :path-return [:email-addresses 0]
&nbsp;                                            :relationship-fn =}</code></pre><p>All that remains is to consult <code>validate-fn-with</code> to see if the relationships are satisfied. First, we&apos;ll do <code>pull-n-put</code>, which should yield the intended results.</p><pre><code>(validate-fn-with pull-n-put
&nbsp;                 {:speculoos/argument-return-relationships
&nbsp;                    [{:path-argument [2 :phone],
&nbsp;                      :path-return [:phone-numbers 2],
&nbsp;                      :relationship-fn =}
&nbsp;                     {:path-argument [0 :email],
&nbsp;                      :path-return [:email-addresses 0],
&nbsp;                      :relationship-fn =}]}
&nbsp;                 person-1
&nbsp;                 person-2
&nbsp;                 person-3)
;; =&gt; {:email-addresses
;;       [&quot;aragorn@sonofarath.org&quot;
;;        &quot;vita@meatavegam.info&quot;
;;        &quot;jolene@justbecauseyou.com&quot;],
;;     :phone-numbers [&quot;867-5309&quot; &quot;123-4567&quot;
;;                     &quot;555-FILK&quot;]}</code></pre><p>Yup. <code>pull-n-put</code>&apos;s return value satisfied both equality relationships with the arguments we supplied, so <code>validate-fn-with</code> passed on that correct return value.</p><p>Now we&apos;ll validate <code>pull-n-whoops</code>, which does not produce correct results.</p><pre><code>(validate-fn-with pull-n-whoops
&nbsp;                 {:speculoos/argument-return-relationships
&nbsp;                    [{:path-argument [2 :phone],
&nbsp;                      :path-return [:phone-numbers 2],
&nbsp;                      :relationship-fn =}
&nbsp;                     {:path-argument [0 :email],
&nbsp;                      :path-return [:email-addresses 0],
&nbsp;                      :relationship-fn =}]}
&nbsp;                 person-1
&nbsp;                 person-2
&nbsp;                 person-3)
;; =&gt; ({:datum-argument &quot;555-FILK&quot;,
;;      :datum-return :mango,
;;      :fn-spec-type
;;        :speculoos/argument-return-relationship,
;;      :path-argument [2 :phone],
;;      :path-return [:phone-numbers 2],
;;      :relationship-fn =,
;;      :valid? false}
;;     {:datum-argument
;;        &quot;aragorn@sonofarath.org&quot;,
;;      :datum-return &quot;867-5309&quot;,
;;      :fn-spec-type
;;        :speculoos/argument-return-relationship,
;;      :path-argument [0 :email],
;;      :path-return [:email-addresses 0],
;;      :relationship-fn =,
;;      :valid? false})</code></pre><p><code>validate-fn-with</code> tells us that <code>pull-n-whoops</code>&apos;s output satisfies neither argument/return relationship. Where we expected phone number <code>555-FILK</code>, we see <code>:mango</code>, and where we expected email <code>aragorn@sonofarath.org</code>, we see phone number <code>867-5309</code>.</p><p>The idea to grasp from validating <code>pull-n-put</code> and <code>pull-n-whoops</code> is that even though the relationship function was a basic equality <code>=</code>, the relationship validation is precise, flexible, and powerful because we used paths to focus on exactly the relationship we&apos;re interested in. On the other hand, whatever function we put at <code>:relationship-fn</code> is completely open-ended, and can be similarly sophisticated.</p><p>Before we finish this subsection, I&apos;d like to demonstrate how to combine all five types of validation: argument scalars, argument collections, return scalars, return collections, and argument/return relationship. We&apos;ll rely on our old friend <code>broken-reverse</code>. Let&apos;s remember what <code>broken-reverse</code> actually does.</p><pre><code>(broken-reverse [11 22 33 44 55]) ;; =&gt; [11 22 33 44 55 9999]</code></pre><p>Instead of properly reversing the argument collection, it merely appends a spurious <code>9999</code>.</p><p>We&apos;ll pass a vector as the first and only argument. Within that vector, we pretend to not care about the first two elements, so we&apos;ll use <code>any?</code> predicates as placeholders.  We&apos;ll specify the third element of that vector to be a decimal with a <code>decimal?</code> scalar predicate. The entire <em>argument sequence</em> is validated, so we must make sure the shape of the scalar specification mimics the shape of the data.</p><pre><code>:speculoos/arg-scalar-spec [[any? any? decimal?]]</code></pre><p>Just so we see an invalid result, we&apos;ll make the argument collection specification expect a list, even though we know we&apos;ll be passing a vector. And again, we must make the collection specification&apos;s shape mimic the data, so to mimic the argument sequence, it looks like this.</p><pre><code>:speculoos/arg-collection-spec [[list?]]</code></pre><p>We know that <code>broken-reverse</code> returns the input collection with <code>9999</code> conjoined. We&apos;ll write the return scalar specification to expect a string in the fourth slot, merely so that we&apos;ll see integer <code>44</code> fail to satisfy.</p><pre><code>:speculoos/ret-scalar-spec [any? any? any? string?]</code></pre><p>And since we&apos;re expecting <code>broken-reverse</code> to return a vector, we&apos;ll write the return collection specification to expect a set.</p><pre><code>:speculoos/ret-collection-spec [set?]</code></pre><p>Finally, we&apos;ve previously demonstrated that <code>broken-reverse</code> fails to satisfy the <code>reversed?</code> argument/return relationship specification. We&apos;ll pass <code>reversed?</code> the first argument and the entire return.</p><pre><code>:speculoos/argument-return-relationships [{:path-argument [0]
&nbsp;                                          :path-return []
&nbsp;                                          :relationship-fn reversed?}]</code></pre><p id="messy">We assemble all five of those specifications into an organizing map…</p><pre><code>(def organizing-map
&nbsp; {:speculoos/arg-scalar-spec [[any? any? decimal?]],
&nbsp;  :speculoos/arg-collection-spec [[list?]],
&nbsp;  :speculoos/ret-scalar-spec [any? any? any? string?],
&nbsp;  :speculoos/ret-collection-spec [set?],
&nbsp;  :speculoos/argument-return-relationships
&nbsp;    [{:path-argument [0], :path-return [], :relationship-fn reversed?}]})</code></pre><p>…and invoke <code>validate-fn-with</code>.</p><pre><code>(validate-fn-with broken-reverse
&nbsp;                 organizing-map
&nbsp;                 [11 22 33 44 55])
;; =&gt; ({:datum 33,
;;      :fn-spec-type :speculoos/argument,
;;      :path [0 2],
;;      :predicate decimal?,
;;      :valid? false}
;;     {:datum [11 22 33 44 55],
;;      :fn-spec-type :speculoos/argument,
;;      :ordinal-path-datum [0],
;;      :path-datum [0],
;;      :path-predicate [0 0],
;;      :predicate list?,
;;      :valid? false}
;;     {:datum 44,
;;      :fn-spec-type :speculoos/return,
;;      :path [3],
;;      :predicate string?,
;;      :valid? false}
;;     {:datum [11 22 33 44 55 9999],
;;      :fn-spec-type :speculoos/return,
;;      :ordinal-path-datum [],
;;      :path-datum [],
;;      :path-predicate [0],
;;      :predicate set?,
;;      :valid? false}
;;     {:datum-argument [11 22 33 44 55],
;;      :datum-return [11 22 33 44 55 9999],
;;      :fn-spec-type :speculoos/argument-return-relationship,
;;      :path-argument [0],
;;      :path-return [],
;;      :relationship-fn reversed?,
;;      :valid? false})</code></pre><p>We supplied five specifications, five datums failed to satisfy those specifications, and we receive five invalidation entries.<ul><li>Argument scalar <code>33</code> did not satisfy <code>decimal?</code>.</li><li>Argument collection <code>[11 22 33 44 55]</code> did not satisfy <code>list?</code>.</li><li>Return scalar <code>44</code> did not satisfy <code>string?</code>.</li><li>Return collection <code>[11 22 33 4  55 9999]</code> did not satisfy <code>set?</code>.</li><li>Argument <code>[11 22 33 44 55]</code> and return <code>[11 22 33 44 55 9999]</code> did not satisfy relationship <code>reversed?</code>.</li></ul></p><h3 id="recognized-metadata-keys">Recognized metadata specification keys</h3><p>Speculoos consults the following defined group of keys in a specification map when it validates.</p><pre><code>speculoos.function-specs/recognized-spec-keys
;; =&gt; [:speculoos/arg-scalar-spec
;;     :speculoos/arg-collection-spec
;;     :speculoos/ret-scalar-spec
;;     :speculoos/ret-collection-spec
;;     :speculoos/argument-return-relationships
;;     :speculoos/canonical-sample
;;     :speculoos/predicate-&gt;generator
;;     :speculoos/hof-specs]</code></pre><h3 id="explicit">Function Metadata Specifications</h3><p>Speculoos offers three patterns of function validation.<ol><li><code>validate-fn-with</code> performs <strong>explicit</strong> validation with a specification supplied in a separate map. The function var is not altered.</li><li><code>validate-fn</code> performs <strong>explicit</strong> validation with specifications contained in the function&apos;s metadata.</li><li><code>instrument</code> provides <strong>implicit</strong> validation with specifications contained in the function&apos;s metadata.</li></ol></p><p>Up until this point, we&apos;ve been using the most explicit variant, <code>validate-fn-with</code> because its behavior is the most readily apparent. <code>validate-fn-with</code> is nice when we want to quickly validate a function <em>on-the-fly</em> without messing with the function&apos;s metadata. We merely supply the function&apos;s name, a map of specifications, and a sequence of arguments as if we were directly invoking the function.</p><p>Speculoos function specifications <a href="https://clojure.org/about/spec#_dont_further_add_tooverload_the_reified_namespaces_of_clojure">differ</a> from <code>spec.alpha</code> in that they are stored and retrieved directly from the function&apos;s metadata. Speculoos is an experimental library, and I wanted to test whether it was a good idea to store a function&apos;s specifications in its own metadata. I thought it would be nice if I could hand you one single thing and say </p><blockquote><p><em>Here&apos;s a Clojure function you can use. Its name suggests what it does, its docstring tells you how to use it, and human- and machine-readable specifications check the validity of the inputs, and tests that it&apos;s working properly. All in one neat, tidy </em>S-expression.</p></blockquote><p>To validate a function with metadata specifications, we use <code>validate-fn</code> (or as we&apos;ll <a href="#instrument">discuss later</a>, <code>instrument</code>). Speculoos offers a pair convenience functions to add and remove specifications from a function&apos;s metadata. To add, use <code>inject-specs!</code>. Let&apos;s inject a couple of function specifications to <code>sum-three</code> which we <a href="#fn-args">saw earlier</a>.</p><pre><code>(require &apos;[speculoos.function-specs :refer
&nbsp;          [validate-fn inject-specs! unject-specs!]])</code><br /><br /><code>(inject-specs! sum-three
&nbsp;              {:speculoos/arg-scalar-spec [int? int? int?],
&nbsp;               :speculoos/ret-scalar-spec int?})
;; =&gt; nil</code></pre><p>We can observe that the specifications indeed live in the function&apos;s metadata with <code>clojure.core/meta</code>. There&apos;s a lot of metadata, so we&apos;ll use <code>select-keys</code> to extract only the key-values associated by <code>inject-specs!</code>.</p><pre><code>(select-keys (meta #&apos;sum-three) speculoos.function-specs/recognized-spec-keys)
;; =&gt; #:speculoos{:arg-scalar-spec [int? int? int?],
;;                :ret-scalar-spec int?}</code></pre><p>We see that <code>inject-specs!</code> injected both an argument scalar specification and a return scalar specification.</p><p>If we later decided to undo that, <code>unject-specs!</code> removes all recognized Speculoos specification entries, regardless of how they got there (maybe some combination of <code>inject-specs!</code> and <code>with-meta</code>). For the upcoming demonstrations, though, we&apos;ll keep those specifications in <code>sum-three</code>&apos;s metadata.</p><p>Now that <code>sum-three</code> holds the specifications in its metadata, we can try the second pattern of explicit validation pattern, using <code>validate-fn</code>. It&apos;s similar to <code>validate-fn-with</code>, except we don&apos;t have to supply the specification map; it&apos;s already waiting in the metadata. Invoked with valid arguments, <code>sum-three</code> returns a valid value.</p><pre><code>(validate-fn sum-three 1 20 300) ;; =&gt; 321</code></pre><p>Invoking <code>sum-three</code> with an invalid floating-point number, Speculoos interrupts with a validation report.</p><pre><code>(validate-fn sum-three 1 20 300.0)
;; =&gt; ({:datum 300.0,
;;      :fn-spec-type :speculoos/argument,
;;      :path [2],
;;      :predicate int?,
;;      :valid? false}
;;     {:datum 321.0,
;;      :fn-spec-type :speculoos/return,
;;      :path nil,
;;      :predicate int?,
;;      :valid? false})</code></pre><p>Scalar argument <code>300.0</code> failed to satisfy its paired scalar predicate <code>int?</code>. Also, scalar return <code>321.0</code> failed to satisfy its paired scalar predicate <code>int?</code>.</p><p>The metadata specifications are passive and have no effect during normal invocation.</p><pre><code>(sum-three 1 20 300.0) ;; =&gt; 321.0</code></pre><p>Even though <code>sum-three</code> currently holds a pair of scalar specifications within its metadata, directly invoking <code>sum-three</code> does not initiate any validation.</p><p><code>validate-fn</code> only interrupts when a predicate paired with a datum is not satisfied. If we remove all the specifications, there won&apos;t be any predicates. Let&apos;s remove <code>sum-three</code>&apos;s metadata specifications with <code>unject-specs!</code>.</p><pre><code>(unject-specs! sum-three) ;; =&gt; nil</code><br /><br /><br /><code>;; all recognized keys are removed</code><br /><br /><code>(select-keys (meta #&apos;sum-three) speculoos.function-specs/recognized-spec-keys)
;; =&gt; {}</code></pre><p>Now that <code>sum-three</code>&apos;s metadata no longer contains specifications, <code>validate-fn</code> will not perform any validations.</p><pre><code>(validate-fn sum-three 1 20 300.0) ;; =&gt; 321.0</code></pre><p>The return value <code>321.0</code> merely passes through because there were zero predicates.</p><p>We can try a more involved example. Let&apos;s inject that <a href="#messy">messy ball</a> of metadata specifications into <code>broken-reverse</code>.</p><pre><code>(inject-specs!
&nbsp; broken-reverse
&nbsp; {:speculoos/arg-scalar-spec [[any? any? decimal?]],
&nbsp;  :speculoos/arg-collection-spec [[list?]],
&nbsp;  :speculoos/ret-scalar-spec [any? any? any? string?],
&nbsp;  :speculoos/ret-collection-spec [set?],
&nbsp;  :speculoos/argument-return-relationships
&nbsp;    [{:path-argument [0], :path-return [], :relationship-fn reversed?}]})
;; =&gt; nil</code></pre><p>Now we double-check the success of injecting the metadata.</p><pre><code>(select-keys (meta #&apos;broken-reverse)
&nbsp;            speculoos.function-specs/recognized-spec-keys)
;; =&gt; #:speculoos{:arg-collection-spec [[list?]],
;;                :arg-scalar-spec [[any? any? decimal?]],
;;                :argument-return-relationships [{:path-argument [0],
;;                                                 :path-return [],
;;                                                 :relationship-fn reversed?}],
;;                :ret-collection-spec [set?],
;;                :ret-scalar-spec [any? any? any? string?]}</code></pre><p>We confirm that all five function specifications in <code>broken-reverse</code>&apos;s metadata. <code>validate-fn</code> can now find those specifications.</p><p>Finally, we validate <code>broken-reverse</code>.</p><pre><code>(validate-fn broken-reverse [11 22 33 44 55])
;; =&gt; ({:datum 33,
;;      :fn-spec-type :speculoos/argument,
;;      :path [0 2],
;;      :predicate decimal?,
;;      :valid? false}
;;     {:datum [11 22 33 44 55],
;;      :fn-spec-type :speculoos/argument,
;;      :ordinal-path-datum [0],
;;      :path-datum [0],
;;      :path-predicate [0 0],
;;      :predicate list?,
;;      :valid? false}
;;     {:datum 44,
;;      :fn-spec-type :speculoos/return,
;;      :path [3],
;;      :predicate string?,
;;      :valid? false}
;;     {:datum [11 22 33 44 55 9999],
;;      :fn-spec-type :speculoos/return,
;;      :ordinal-path-datum [],
;;      :path-datum [],
;;      :path-predicate [0],
;;      :predicate set?,
;;      :valid? false}
;;     {:datum-argument [11 22 33 44 55],
;;      :datum-return [11 22 33 44 55 9999],
;;      :fn-spec-type
;;        :speculoos/argument-return-relationship,
;;      :path-argument [0],
;;      :path-return [],
;;      :relationship-fn reversed?,
;;      :valid? false})</code></pre><p>Notice, this is the exact same validation <a href="#messy">as before</a>, but because all the messy specifications were already tucked away in the metadata, the validation invocation was a much cleaner one-liner, <br /><code>(validate-fn broken-reverse [11 22 33 44 55])</code>.</p><p>Again, metadata specification have no effect when the function is directly invoked.</p><pre><code>(broken-reverse [11 22 33 44 55]) ;; =&gt; [11 22 33 44 55 9999]</code></pre><p>We never <em>unjected</em> the specifications from <code>broken-reverse</code>&apos;s metadata, but they have absolutely no influence outside of Speculoos&apos; function validation.</p><h3 id="instrument">Instrumenting Functions</h3><p><strong>Beware: </strong><code>instrument</code>-style function validation is very much a work in progress. The current implementation is sensitive to invocation order and can choke on multiple calls.</p><p>Until this point in our discussion, Speculoos has only performed function validation when we explicitly called either <code>validate-fn-with</code> or <code> validate-fn</code>. With those two utilities, the specifications in the metadata are passive and produce no effect, even when invoking with arguments that would otherwise fail to satisfy the specification&apos;s predicates.</p><p>Speculoos&apos; third pattern of function validation <em>instruments</em> the function using the metadata specifications. Every direct invocation of the function itself automatically validates arguments and returns using any specification in the metadata. Let&apos;s explore function instrumentation using <code>sum-three</code> <a href="#fn-args">from earlier</a>. <code>instrument</code> will only validate with metadata specifications. First, we need to inject our specifications.</p><pre><code>(inject-specs! sum-three
&nbsp;              {:speculoos/arg-scalar-spec [int? int? int?],
&nbsp;               :speculoos/ret-scalar-spec int?})
;; =&gt; nil</code></pre><p><code>sum-three</code> now holds two scalar specifications within its metadata, but those specifications are merely sitting there, completely passive.</p><pre><code>;; valid args and return value</code><br /><br /><code>(sum-three 1 20 300) ;; =&gt; 321</code><br /><br /><br /><code>;; invalid arg 300.0 and invalid return value 321.0</code><br /><br /><code>(sum-three 1 20 300.0) ;; =&gt; 321.0</code></pre><p>That second invocation above supplied an invalid argument and produced an invalid return value, according to the metadata specifications. But we didn&apos;t explicitly validate with <code>validate-fn</code>, and <code>sum-three</code> is not yet instrumented, so <code>sum-three</code> returns the computed value <code>321.0</code> without interruption.</p><p>Let&apos;s instrument <code>sum-three</code> and see what happens.</p><pre><code>(require &apos;[speculoos.function-specs :refer [instrument unstrument]])</code><br /><br /><code>(instrument sum-three)</code></pre><p>Not much. We&apos;ve only added the specifications to the metadata and instrumented <code>sum-three</code>. An instrumented function is only validated when it is invoked.
</p><pre><code>(sum-three 1 20 300) ;; =&gt; 321</code></pre><p>We just invoked <code>sum-three</code>, but all three integer arguments and the bare scalar return value satisfied all their predicates, so <code>321</code> passes through. Let&apos;s invoke with two integer arguments and one non-integer argument.</p><pre><code>;; arg 300.0 does not satisfy its paired predicate in the argument scalar specification,</code><br /><code>;; but `sum-three` is capable of computing a return with those given inputs</code><br /><br /><code>(sum-three 1 20 300.0) ;; =&gt; 321.0</code></pre><p>That&apos;s interesting. In contrast to <code>validate-fn-with</code> and <code>validate-fn</code>, an instrumented function is not interrupted with an invalidation report when predicates are not satisfied. The invalidation report is instead written to <code>*out*</code>.</p><pre><code>;; validation report is written to *out*</code><br /><br /><code>(with-out-str (sum-three 1 20 300.0))</code><br /><code>;; =&gt; ({:path [2], :datum 300.0, :predicate int?, :valid? false, :fn-spec-type :speculoos/argument}
</code><code>;;     {:path nil, :datum 321.0, :predicate int?, :valid? false, :fn-spec-type :speculoos/return})</code></pre><p>Speculoos will implicitly validate any instrumented function with any permutation of <a href="#recognized-metadata-keys">specifications within its metadata</a>.</p><p>When we want to revert <code>sum-three</code> back to normal, we <code>unstrument</code> it.</p><pre><code>(unstrument sum-three)</code></pre><p>Now that it&apos;s no longer instrumented, <code>sum-three</code> will yield values as normal, even if the arguments and return value do not satisfy the metadata specifications.</p><pre><code>;; valid arguments and return value</code><br /><code>(sum-three 1 20 300) ;; =&gt; 321</code><br /><br /><code>;; one invalid argument, invalid return value</code><br /><code>(sum-three 1 20 300.0) ;; =&gt; 321.0</code><br /><br /><code>;; nothing written to *out*</code><br /><code>(with-out-str (sum-three 1 20 300.0)) ;; =&gt; &quot;&quot;</code></pre><h3 id="hof">Validating Higher-Order Functions</h3><p>Speculoos has a story about validating higher-order functions, too. It uses very similar patterns to first-order function validation: Put some specifications in the function&apos;s metadata with the <a href="#recognized-metadata-keys">proper, qualified keys</a>, then invoke the function with some sample arguments, then Speculoos will validate the results.</p><p>The classic <span class="small-caps">hof</span> is something like <code>(defn adder [x] #(+ x %))</code>. To make things a tad more interesting, we&apos;ll add a little flourish.</p><pre><code>(require &apos;[speculoos.function-specs :refer [validate-higher-order-fn]])</code><br /><br /><code>(defn addder [x] (fn [y] (fn [z] (+ x (+ y z)))))</code><br /><br /><code>(((addder 7) 80) 900) ;; =&gt; 987</code></pre><p><code>addder</code> returns a function upon each of its first two invocations, and only on its third invocation does <code>addder</code> return a scalar. Specifying and validating a function object does not convey much meaning: it would merely satisfy <code>fn?</code> which isn&apos;t very interesting. So to validate a <span class="small-caps">hof</span>, Speculoos requires it to be invoked until it produces a value. So we&apos;ll supply the validator with a series of argument sequences that, when fed in order to the <span class="small-caps">hof</span>, will produce a result. For the example above, it will look like <code>[7] [80] [900]</code>.</p><p>The last task we must do is create the specification. <span class="small-caps">hof</span> specifications live in the function&apos;s metadata at key <code>:speculoos/hof-specs</code>, which is a series of nested specification maps, one nesting for each returned function. For this example, we might create this <span class="small-caps">hof</span> specification.</p><pre><code>(def addder-spec
&nbsp; {:speculoos/arg-scalar-spec [string?],
&nbsp;  :speculoos/hof-specs {:speculoos/arg-scalar-spec [boolean?],
&nbsp;                        :speculoos/hof-specs
&nbsp;                          {:speculoos/arg-scalar-spec [char?],
&nbsp;                           :speculoos/ret-scalar-spec keyword?}}})</code></pre><p>Once again, for illustration purposes, we&apos;ve crafted a specification composed of predicates that we know will invalidate, but will permit the function stack to evaluate to completion. (Otherwise, validation halts on exceptions.)</p><p><span class="small-caps">hof</span> validation requires that the function&apos;s metadata hold the specifications. So we inject them.</p><pre><code>(inject-specs! addder addder-spec) ;; =&gt; nil</code></pre><p>And finally, we execute the validation with <code>validate-higher-order-fn</code></p><pre><code>(require &apos;[speculoos.function-specs :refer [validate-higher-order-fn]])</code><br /><br /><code>(validate-higher-order-fn addder [7] [80] [900])
;; =&gt; ({:datum 7,
;;      :fn-tier :speculoos/argument,
;;      :path [0 0],
;;      :predicate string?,
;;      :valid? false}
;;     {:datum 80,
;;      :fn-tier :speculoos/argument,
;;      :path [1 0],
;;      :predicate boolean?,
;;      :valid? false}
;;     {:datum 900,
;;      :fn-tier :speculoos/argument,
;;      :path [2 0],
;;      :predicate char?,
;;      :valid? false}
;;     {:datum 987,
;;      :evaled-result 987,
;;      :fn-spec-type :speculoos/return,
;;      :fn-tier :speculoos/return,
;;      :path nil,
;;      :predicate keyword?,
;;      :valid? false})</code></pre><p>Let&apos;s step through the validation results. Speculoos validates <code>7</code> against scalar predicate <code>string?</code> and then invokes <code>addder</code> with argument <code>7</code>. It then validates <code>80</code> against scalar predicate <code>boolean?</code> and then invokes the returned function with argument <code>80</code>. It then validates <code>900</code> against scalar predicate <code>char?</code> and invokes the previously returned function with argument <code>900</code>. Finally, Speculoos validates the ultimate return value <code>987</code> against scalar predicate <code>keyword?</code>. If all the predicates were satisfied, <code>validate-higher-order-fn</code> would yield the return value of the function call. In this case, all three arguments and the return value are invalid, and Speculoos yields a validation report.</p></section><section id="exercising"><h2>Generating Random Samples and Exercising</h2><p>Before we have some fun with random samples, we must create random sample generators and put them in particular spots. Random sample generators are closely related to predicates. A predicate is a thing that can answer <em>Is the value you put in my hand an even, positive integer between ninety and one-hundred?</em> A random sample generator is a thing that says <em>I&apos;m putting in your hand an even, positive integer between ninety and one-hundred</em>. When properly constructed, a generator will produce samples that satisfy its companion predicate.</p><p>Starting with a quick demonstration, Speculoos can generate valid data when given a scalar specification.</p><pre><code>(require &apos;[speculoos.utility :refer [data-from-spec]])</code><br /><br /><code>(data-from-spec [int? string? keyword?] :random)
;; =&gt; [-872 &quot;BDew7mtcgyBbRwMK6k04&quot; :_s.?]</code></pre><p>When dealing with the basic <code>clojure.core</code> predicates, such as <code>int?</code>, <code>string?</code>, <code>keyword?</code>, etc., Speculoos provides pre-made random sample generators that satisfy those predicates. (There are a few exceptions, due to the fact that there is not a one-to-one-to-one correspondence between scalar data types, <code>clojure.core</code> predicates, and <code>clojure.test.check</code> generators.)</p><p>Speculoos can also generate random scalar samples from predicate-like things, such as regular expressions and sets.</p><pre><code>      built-in               v--- regex        v--- set-as-a-predicate</code><br /><code>      predicate ----v</code><br /><code>(data-from-spec {:x int?, :y #&quot;fo{3,6}bar&quot;, :z #{:red :green :blue}} :random)
;; =&gt; {:x -961, :y &quot;fooooobar&quot;, :z :green}</code></pre><p>When we use either a &apos;basic&apos; scalar predicate, such as <code>int?</code>, a regex, or a set-as-a-predicate, Speculoos knows how to generate a valid random sample that satisfies that predicate-like thing. Within the context of generating samples or exercising, basic predicate <code>int?</code> elicits an integer, regular expression <code>#fo{3,6}</code> generates a valid string, and set-as-a-predicate <code>#{:red :green :blue}</code> emits a sample randomly drawn from that set.</p><h3 id="create-gen">Creating Sample Generators</h3><p>This document often uses &apos;basic&apos; predicates like <code>int?</code> and <code>string?</code> because they&apos;re short to type and straightforward to understand. In real life, we&apos;ll want to specify our data with more precision. Instead of merely <em>An integer</em>, we&apos;ll often want to express a more sophisticated description, such as <em>An even positive integer between ninety and one-hundred.</em> To do that, we need to create custom generators.</p><p><code>clojure.test.check</code> provides a group of powerful, flexible, generators.</p><pre><code>(require &apos;[clojure.test.check.generators :as gen])</code><br /><br /><code>(gen/generate (gen/large-integer* {:min 700, :max 999})) ;; =&gt; 930</code><br /><br /><code>(gen/generate gen/keyword) ;; =&gt; :R*1.</code><br /><br /><code>(gen/generate gen/string-alphanumeric) ;; =&gt; &quot;8mHRYO1p4nrNgD0wb17g1B5a&quot;</code></pre><p>Speculoos leans heavily on these generators.</p><h3 id="access-gen">Storing and Accessing Sample Generators</h3><p>The custom generators we discussed in the previous subsection are merely floating around in the ether. To use them for exercising, we need to put those generators in a spot that Speculoos knows: the predicate&apos;s metadata.</p><p>Let&apos;s imagine a scenario. We want a predicate that specifies an integer between ninety (inclusive) and one-hundred (exclusive) and a corresponding random sample generator. First, we write the predicate, something like this.</p><pre><code>(fn [n] (and (int? n) (&lt;= 90 n 99)))</code></pre><p>Second, we write our generator.</p><pre><code>;; produce ten samples with `gen/sample`</code><br /><br /><code>(gen/sample (gen/large-integer* {:min 90, :max 99}))
;; =&gt; (90 91 90 92 95 92 91 91 90 95)</code><br /><br /><br /><code>;; produce one sample with `gen/generate`</code><br /><br /><code>(gen/generate (gen/large-integer* {:min 90, :max 99})) ;; =&gt; 99</code></pre><p>To make the generator invocable, we&apos;ll wrap it in a function.</p><pre><code>(defn generate-nineties
&nbsp; []
&nbsp; (gen/generate (gen/large-integer* {:min 90, :max 99})))</code><br /><br /><br /><code>;; invoke the generator</code><br /><br /><code>(generate-nineties) ;; =&gt; 97</code></pre><p>Third, we need to associate that generator into the predicate&apos;s metadata. We have a couple of options. The manual option uses <code>with-meta</code> when we bind a name to the function body. We&apos;ll associate <code>generate-nineties</code> to the predicate&apos;s <a href="#recognized-metadata-keys">metadata key</a> <code>:speculoos/predicate-&gt;generator</code>.</p><pre><code>(def nineties?
&nbsp; (with-meta (fn [n] (and (int? n) (&lt;= 90 n 99)))
&nbsp;   {:speculoos/predicate-&gt;generator generate-nineties}))</code><br /><br /><br /><code>(nineties? 92) ;; =&gt; true</code><br /><br /><br /><code>(meta nineties?) ;; =&gt; #:speculoos{:predicate-&gt;generator generate-nineties}</code></pre><p>That gets the job done, but the manual option is kinda cluttered. The other option involves a Speculoos utility, <code>defpred</code>, that <strong>def</strong>ines a <strong>pred</strong>icate much the same as <code>defn</code>, but associates the generator with less keyboarding than the <code>with-meta</code> option. Supply a symbol, a predicate function body, and a random sample generator.</p><pre><code>(require &apos;[speculoos.utility :refer [defpred]])</code><br /><br /><br /><code>(defpred NINEties? (fn [n] (and (int? n) (&lt;= 90 n 99))) generate-nineties)</code><br /><br /><br /><code>(NINEties? 97) ;; =&gt; true</code><br /><br /><br /><code>(meta NINEties?)
;; =&gt; #:speculoos{:canonical-sample :NINEties?-canonical-sample,
;;                :predicate-&gt;generator generate-nineties}</code></pre><p><code>defpred</code> automatically puts <code>generate-nineties</code> into the predicate <code>NINEties?</code> metadata. <a href="#auto-sample">Soon</a>, we&apos;ll discuss another couple of benefits to using <code>defpred</code>. Whichever way we accomplished getting the generator into the metadata at <code>:speculoos/predicate-&gt;generator</code>, Speculoos can now find it.</p><p>Speculoos uses function metadata for two purposes, and it&apos;s important to keep clear in our minds which is which.<ul><li><p>Store <em>function specifications</em> in the metadata for that function. For example, if we have a <code>reverse</code> function, we put the specification to test <code>equal-lengths?</code> in the metadata at <code>:speculoos/argument-return-relationships</code>.</p></li><li><p>Store <em>random sample generators</em> in the metadata for that predicate. If we have a <code>nineties?</code> predicate, we put the random sample generator <code>generate-nineties</code> in the metadata at <code>:speculoos/predicate-&gt;generator</code>.</p></li></ul></p><h3 id="auto-sample">Creating Sample Generators Automatically</h3><p><code>defpred</code> does indeed relieve us of some tedious keyboarding, but it offers another benefit. If we arrange the predicate definition according to <code>defpred</code>&apos;s expectations, it can automatically create a random sample generator for that predicate. Let&apos;s see it in action and then we&apos;ll examine the details.</p><pre><code>(defpred auto-nineties? (fn [n] (and (int? n) (&lt;= 90 n 99))))</code><br /><br /><br /><code>(meta auto-nineties?)
;; =&gt; #:speculoos{:canonical-sample :auto-nineties?-canonical-sample,
&nbsp;                 :predicate-&gt;generator #fn--88795}</code></pre><p>Well, there&apos;s certainly <em>something</em> at <code>:speculoos/predicate-&gt;generator</code>, but is it anything useful?</p><pre><code>(binding [speculoos.utility/*such-that-max-tries* 1000]
&nbsp; (let [possible-gen-90 (:speculoos/predicate-&gt;generator (meta auto-nineties?))]
&nbsp;   (possible-gen-90)))
;; =&gt; 91</code></pre><p>Yup! Since it is not-so-likely that a random integer generator would produce a value in the nineties, we bound the <code>max-tries</code> to a high count to give the generator lots of attempts. We then pulled out the generator from predicate <code>auto-nineties?</code>&apos;s metadata and bound it to <code>possible-gen-90</code>. Then we invoked <code>possible-gen-90</code> and, in fact, it generated an integer in the nineties that satisfies the original predicate we defined as <code>auto-nineties</code>. <code>defpred</code> automatically created a random sample generator whose output satisfies the predicate.</p><p>For <code>defpred</code> to do its magic, the predicate definition must follow a few patterns.</p><ul><li>We must provide the textual representation of the definition. We can&apos;t merely assign another already-defined function.</li><li>The first symbol must be <code>and</code>, <code>or</code>, or a basic predicate for a Clojure built-in scalar, such as <code>int?</code>, that is registered at <code>speculoos.utility/predicate-&gt;generator</code>.<pre><code>(and (...)) ;; okay</code><br /><code>(or (...)) ;; okay</code><br /><code>(int? ...) ;; okay</code><br /><code>(let ...) ;; not okay</code><br /></pre></li><li>The first clause after <code>and</code> and all immediate descendants of <code>or</code> must start with a basic predicate described above.</li></ul><p>Subsequent clauses of <code>and</code> will be used to create <code>test.check.generators/such-that</code> modifiers. Direct descendants of a top-level <code>or</code> will produce<code>n</code> separate random sample generators, each with <code>1/n</code> probability.</p><p>Speculoos exposes the internal tool <code>defpred</code> uses to create a generator, so we can inspect how it works. (I&apos;ve lightly edited the output for clarity.)</p><pre><code>(require &apos;[speculoos.utility :refer [inspect-fn]])</code><br /><br /><br /><code>(inspect-fn &apos;(fn [i] (int? i)))</code><br /><code>;; =&gt; gen/small-integer</code></pre><p>We learn that <code>inspect-fn</code> examines the textual representation of the predicate definition, extracts <code>int?</code> and infers that the base generator ought to be <code>gen/small-integer</code>. Next, we&apos;ll add a couple of modifiers with <code>and</code>. To conform to the requirements, we&apos;ll put <code>int?</code>  in the first clause. (Again, lightly edited.)</p><pre><code>(inspect-fn &apos;(fn [i] (and (int? i) (even? i) (pos? i))))</code><br /><code>;; =&gt; (gen/such-that (fn [i] (and (even? i) (pos? i)))
;;       gen/small-integer {:max-tries speculoos.utility/*such-that-max-tries*}) </code></pre><p><code>int?</code> elicits a small-integer generator. <code>inspect-fn</code> then uses the subsequent clauses of the <code>and</code> expression to create a <code>such-that</code> modifier that generates only positive, even numbers.</p><p>Let&apos;s see what happens with an <code>or</code>.</p><pre><code>(inspect-fn &apos;(fn [x] (or (int? x) (string? x))))</code><br /><code>;; =&gt; (gen/one-of [gen/small-integer gen/string-alphanumeric])</code></pre><p>Our predicate definition is satisfied with either an integer or a string. <code>inspect-fn</code> therefore creates a generator that will produce either an integer or a string with equal probability.</p><p>When automatically creating random sample generators, <code>defpred</code> handles nesting up to two levels deep. Let&apos;s see how we might combine both <code>or</code> and <code>and</code>. We&apos;ll define a predicate that tests for either an odd integer, a string of at least three characters, or a ratio greater than one-ninth.</p><pre><code>(defpred combined-pred
&nbsp;        #(or (and (int? %) (odd? %))
&nbsp;             (and (string? %) (&lt;= 3 (count %)))
&nbsp;             (and (ratio? %) (&lt; 1/9 %))))</code><br /><br /><br /><code>(data-from-spec {:a combined-pred,
&nbsp;                :b combined-pred,
&nbsp;                :c combined-pred,
&nbsp;                :d combined-pred,
&nbsp;                :e combined-pred,
&nbsp;                :f combined-pred,
&nbsp;                :g combined-pred,
&nbsp;                :h combined-pred,
&nbsp;                :i combined-pred}
&nbsp;               :random)
;; =&gt; {:a 7,
;;     :b &quot;6Bfs1P5PsCfxh&quot;,
;;     :c 6/7,
;;     :d -29,
;;     :e 19,
;;     :f &quot;j99H1&quot;,
;;     :g &quot;XVA1o876yTX&quot;,
;;     :h 4/31,
;;     :i -29}</code></pre><p>We&apos;re kinda abusing <code>data-from-spec</code> here to generate nine samples. Inferring from <code>combined-pred</code>&apos;s predicate structure, <code>defpred</code>&apos;s automatically-created random sample generator emits one of three elements with equal probability: an odd integer, a string of at least three characters, or a ratio greater than one-ninth. All we had to do was write the predicate; <code>defpred</code> wrote all three random sample generators.</p><h3 id="test-gen">Testing Sample Generators Residing in Metadata</h3><p>Some scenarios block us from using <code>defpred</code>&apos;s automatic generators. We may not have access to the textual representation of the predicate definition. Or, sometimes we must hand-write a generator because a naive generator would be unlikely to find a satisfying value (e.g., a random number that must fall within a narrow range).</p><p>The Write-generator-then-Apply-to-metadata-then-Test loop can be tedious, so the <code>utility</code> namespace provides a tool to help. <code>validate-predicate-&gt;generator</code> accepts a predicate function we supply, extracts the random sample generator residing in its metadata, generates a sample, and then feeds that sample back into the predicate to see if it satisfies.</p><pre><code>(require &apos;[speculoos.utility :refer [validate-predicate-&gt;generator]])</code><br /><br /><br /><code>(defpred pred-with-incorrect-generator
&nbsp;        (fn [i] (int? i))
&nbsp;        #(gen/generate gen/ratio))</code><br /><br /><br /><code>(validate-predicate-&gt;generator pred-with-incorrect-generator)
;; =&gt; ([21/16 false]
;;     [-7/15 false]
;;     [-5/14 false]
;;     [-6 true]
;;     [-13/7 false]
;;     [20/29 false]
;;     [19/23 false])</code></pre><p>We defined scalar predicate <code>pred-with-incorrect-generator</code> to require an integer, but, using <code>defpred</code>, we manually created a generator that emits ratio values. Each of the generated samples fails to satisfy the <code>int?</code> predicate.</p><p>With help from <code>validate-predicate-&gt;generator</code>, we can hop back and forth to adjust the hand-made generator.</p><pre><code>(defpred pred-with-good-generator
&nbsp;        (fn [i] (int? i))
&nbsp;        #(gen/generate gen/small-integer))</code><br /><br /><br /><code>(validate-predicate-&gt;generator pred-with-good-generator)
;; =&gt; ([-6 true]
;;     [4 true]
;;     [19 true]
;;     [14 true]
;;     [9 true]
;;     [17 true]
;;     [18 true])</code></pre><p>In this particular case, we could have relied on <code>defpred</code> to <a href="#auto-sample">create a sample generator</a> for us.</p><p>Pretend somebody hands us a specification. It might be useful to know if we need to write a random sample generator for any of the predicates it contains, or if Speculoos can find a generator for all of them, either in the collection of known predicates-to-generators associations, or in the predicates&apos; metadata. <code>unfindable-generators</code> tells us this information.</p><p>Let&apos;s compose a scalar specification containing <code>int?</code>, a set-as-a-predicate <code>#{:red :green :blue}</code>, and a regular expression <code>#&quot;fo{2,5}&quot;</code>.</p><pre><code>(require &apos;[speculoos.utility :refer [unfindable-generators]])</code><br /><br /><br /><code>(unfindable-generators [int? #{:red :green :blue} #&quot;fo{2,5}&quot;]) ;; =&gt; []</code></pre><p>Speculoos knows how to create random samples from all three of those predicate-like things, so <code>unfindable-generators</code> returns an empty vector, <em>nothing unfindable</em>. Now, let&apos;s make a scalar specification with three predicates that intentionally lack generators.</p><pre><code>;; silly &apos;predicates&apos; that lack generators</code><br /><br /><code>(def a? (fn [] &apos;a))</code><br /><code>(def b? (fn [] &apos;b))</code><br /><code>(def c? (fn [] &apos;c))</code><br /><br /><br /><code>(unfindable-generators [a? b? c?])
;; =&gt; [{:path [0], :value a?}
;;     {:path [1], :value b?}
;;     {:path [2], :value c?}]</code></pre><p><code>unfindable-generators</code> informs us that if we had tried to do a task that <a href="#using-gen">uses a sample generator</a>, we&apos;d have failed. With this knowledge, we could go back and add random sample generators to <code>a?</code>, <code>b?</code>, and <code>c?</code>.</p><h3 id="using-gen">Using Sample Generators</h3><p>Speculoos can do three things with random sample generators.<ul><li>Create a heterogeneous, arbitrarily-nested data structure when given a scalar specification.</li><li>Exercise a scalar specification.</li><li>Exercise a function with a scalar specification.</li></ul></p><p>The first, creating a valid set of data from a given scalar specification, provides the foundation of the later two exercising functions, so we&apos;ll begin with <code>data-from-spec</code>.</p><p>Imagine we&apos;d like to specify the scalars contained within a vector to be an integer, followed by a ratio, followed by a double-precision floating-point number. We&apos;ve seen <a href="#scalar-validation">how to compose that scalar specification</a>. Let&apos;s give that scalar specification to <code>data-from-spec</code>.</p><pre><code>(require &apos;[speculoos.utility :refer [data-from-spec]])</code><br /><br /><br /><code>(data-from-spec [int? ratio? double?] :random) ;; =&gt; [385 -3/13 -8.0]</code></pre><p>That scalar specification contains three predicates, and each of those predicates targets a basic Clojure numeric type, so Speculoos automatically refers to <code>test.check</code>&apos;s generators to produce a random sample.</p><p>Let&apos;s try another example. The scalar specification will be a map with three keys associated with predicates for a character, a set-as-a-predicate, and a regex-predicate.</p><pre><code>(data-from-spec {:x char?,
&nbsp;                :y #{:red :green :blue},
&nbsp;                :z #&quot;fo{3,5}bar&quot;}
&nbsp;               :random)
;; =&gt; {:x \k,
;;     :y :blue,
;;     :z &quot;fooooobar&quot;}</code></pre><p>Again, without any further assistance, <code>data-from-spec</code> knew how to find or create a random sample generator for each predicate in the scalar specification. <code>char?</code> targets a basic Clojure type, so it generated a random character. Sets in a scalar specification, in this context, are considered a membership predicate. The random sample generator is merely a random selection of one of the members of set <code>#{:red :green :blue}</code>. Finally, Speculoos regards a regular expression as a predicate for validating strings. <code>data-from-spec</code> consults the <a href="https://github.com/weavejester/re-rand"><code>re-rand</code></a> library to generate a random string from regular expression <code>#&quot;foo{3,5}bar&quot;</code>.</p><p>If our scalar specification contains custom predicates, we&apos;ll have to provide a little more information. We&apos;ll make another scalar specification containing a positive, even integer…</p><pre><code>(defpred pos-even-int? (fn [i] (and (int? i) (pos? i) (even? i))))</code></pre><p>…relying on <code>defpred</code>&apos;s predicate inspection machinery to infer a generator. After making our <code>pos-even-int?</code> predicate, we&apos;ll make a predicate satisfied by a three-character string, <code>(fn [s] (and (string? s) (= 3 (count s))))</code>. The generator which <code>defpred</code> would create for that predicate is kinda naive.</p><pre><code>(inspect-fn &apos;(fn [s] (and (string? s) (= 3 (count s)))))</code><br /><code>;; =&gt; (gen/such-that (fn [s] (and (= 3 (count s)))) gen/string-alphanumeric)</code><br /><br /><code>;; (…output elided…)</code></pre><p>That naive generator would produce random strings of random lengths until it found one exactly three characters long. It&apos;s possible it would fail to produce a valid value before hitting the <code>max-tries</code> limit. However, we can explicitly write a generator and attach it with <code>defpred</code>.</p><pre><code>(defpred three-char-string?
&nbsp;        (fn [s] (and (string? s) (= 3 (count s))))
&nbsp;        #(clojure.string/join (gen/sample gen/char-alphanumeric 3)))</code></pre><p>Now that we have two scalar predicates with custom sample generators — one created by <code>defpred</code>, one created by us — we&apos;ll bring them together into a single scalar specification and invoke <code>data-from-spec</code>.</p><pre><code>(data-from-spec [pos-even-int? three-char-string?] :random) ;; =&gt; [26 &quot;124&quot;]</code></pre><p><code>data-from-spec</code> generates a valid data set whose randomly-generated scalars satisfy the scalar specification. In fact, we can feed the generated data back into the specification and it ought to validate <code>true</code>. We provide <code>valid-scalars?</code> with the generated data as the first argument (upper row) and the specification as the second argument (lower row).</p><pre><code>(speculoos.core/valid-scalars? (data-from-spec [int? ratio? double?])
&nbsp;                              [int? ratio? double?])
;; =&gt; true</code></pre><p>Perhaps it would be nice to do that multiple times, one immediately after another: generate some random data from a specification and feed it back into the specification to see if it validates. Don&apos;t go off and write your own utility. Speculoos can <em>exercise</em> a scalar specification.</p><pre><code>(require &apos;[speculoos.utility :refer [exercise]])</code><br /><br /><br /><code>(exercise [int? ratio? double?])
;; =&gt; ([[69 13/23 -0.00543212890625] true]
;;     [[-260 -22/23 -0.008148193359375] true]
;;     [[-631 -11/12 -0.4983367919921875] true]
;;     [[207 -1/24 -0.06145776854828] true]
;;     [[-418 -23/16 -0.010118484497070312] true]
;;     [[311 -3/17 -294.0] true]
;;     [[-652 3/2 14.813166737556458] true]
;;     [[349 -2/5 0.010761824669316411] true]
;;     [[-469 -3/2 -453.0] true]
;;     [[-74 -2/3 -16.535888671875] true])</code></pre><p>Ten times, <code>exercise</code> generated a vector containing an integer, ratio, and double-precision numbers, then performed a scalar validation using those random samples as the data and the original scalar specification. In each of those ten runs, we see that <code>exercise</code> generated valid, <code>true</code> data.</p><p>So now we&apos;ve seen that Speculoos can repeatedly generate random valid data from a scalar specification and run a validation of that random data. If we have injected an argument scalar specification into a function&apos;s metadata, Speculoos can repeatedly generate specification-satisfying arguments and repeatedly invoke that function. That activity would be considered <em>exercising the function</em>.</p><p>We revisit our friend, <code>sum-three</code>, a function which accepts three numbers and sums them. That scalar specification we&apos;ve been using, <code>[int? ratio? double?]</code>, mimics the shape of the argument sequence, so let&apos;s inject it into <code>sum-three</code>&apos;s metadata.</p><pre><code>(defn sum-three [x y z] (+ x y z))</code><br /><br /><br /><code>(inject-specs! sum-three {:speculoos/arg-scalar-spec [int? ratio? double?]})
;; =&gt; nil</code></pre><p><code>sum-three</code> is certainly capable of summing any three numbers we feed it, but just for fun, we specify that the arguments ought to be an integer, a ratio, and a double-precision number. Now that we&apos;ve defined our function and added an argument scalar specification, let&apos;s exercise <code>sum-three</code>.</p><pre><code>(require &apos;[speculoos.function-specs :refer [exercise-fn]])</code><br /><br /><br /><code>(exercise-fn sum-three)
;; =&gt; ([[-336 -29/5 0.75] -341.05]
;;     [[645 -9/29 0.890625] 645.5802801724138]
;;     [[696 -2/25 -0.8469765093177557] 695.0730234906822]
;;     [[-290 -1/6 -0.0145263671875] -290.1811930338542]
;;     [[-943 -16/7 54.30241394042969] -890.9833003452846]
;;     [[238 -5/9 0.01953125] 237.4639756944444]
;;     [[522 -29/23 325.24284076690674] 845.9819712016894]
;;     [[-375 -9/31 110.953125] -264.3371975806452]
;;     [[774 5/9 -15.796875] 758.7586805555557]
;;     [[-701 -2/3 0.75] -700.9166666666667])</code></pre><p><code>int?</code>, <code>ratio?</code>, and <code>double?</code> all have built-in generators, so we didn&apos;t have to create any custom generators. <code>exercise-fn</code> extracted <code>sum-three</code>&apos;s argument scalar specification, then, ten times, generated a data set from random sample generators, then invoked the function with those arguments.</p><h3 id="canonical">Canonical Samples</h3><p>Sometimes it might be useful that a generated value be predictable. Perhaps we&apos;re writing documentation, or making a presentation, and we&apos;d like the values to be aesthetically pleasing. Or, sometimes during development, it&apos;s nice to be able to quickly eyeball a known value.</p><p>Speculoos provides a canonical sample for many of Clojure&apos;s fundamental scalars when the relevant functions are invoked with the <code>:canonical</code> option. Here we use <code>data-from-spec</code> to illustrate the built-in canonical values of six of the basic scalars.</p><pre><code>(data-from-spec {:x int?,
&nbsp;                :y char?,
&nbsp;                :z string?,
&nbsp;                :w double?,
&nbsp;                :q ratio?,
&nbsp;                :v keyword?}
&nbsp;               :canonical)
;; =&gt; {:q 22/7,
;;     :v :kw,
;;     :w 1.0E32,
;;     :x 42,
;;     :y \c,
;;     :z &quot;abc&quot;}</code></pre><p>The two exercising functions, <code>exercise</code> and <code>exercise-fn</code> both accept the <code>:canonical</code> option, as well.</p><pre><code>(exercise [int? ratio? double?] :canonical) ;; =&gt; ([[42 22/7 1.0E32] true])</code><br /><br /><br /><code>(exercise-fn sum-three :canonical) ;; =&gt; ([[42 22/7 1.0E32] 1.0E32])</code></pre><p>Since the canonical values don&apos;t vary, it doesn&apos;t make much sense to exercise more than once.</p><p>Beyond the built-in canonical values, we can supply canonical values of our own choosing when we define a predicate. We can manually add the canonical values via <code>with-meta</code> or we can add a canonical value using <code>defpred</code> as an argument following a custom generator.</p><pre><code>;; won&apos;t bother to write a proper generator; use `(constantly :ignored)` as a placeholder</code><br /><br /><code>(defpred neg-odd-int?
&nbsp;        (fn [i] (and (int? i) (neg? i) (odd? i)))
&nbsp;        (constantly :ignored)
&nbsp;        -33)</code><br /><br /><br /><code>(defpred happy-string?
&nbsp;        (fn [s] (string? s))
&nbsp;        (constantly :ignored)
&nbsp;        &quot;Hello Clojure!&quot;)</code><br /><br /><br /><code>(defpred pretty-number? (fn [n] (number? n)) (constantly :ignored) 123.456)</code><br /><br /><br /><br /><code>(data-from-spec [neg-odd-int? happy-string? pretty-number?] :canonical)
;; =&gt; [-33 &quot;Hello Clojure!&quot; 123.456]</code></pre><p>We see that <code>data-from-spec</code> found the custom canonical values for each of the three predicates: <code>-33</code> for <code>neg-odd-int?</code>, <code>&quot;Hello Clojure!&quot;</code> for <code>happy-string?</code>, and <code>123.456</code> for <code>pretty-number?</code>. Notice that <em>exercising</em> a function does not validate the arguments or returns. Function argument and return validation only occurs when we explicitly invoke <code>validate-fn-with</code>, <code>validate-fn</code>, or we intentionally instrument it.</p></section><section id="utilities"><h2>Utility Functions</h2><p>You won&apos;t miss any crucial piece of Speculoos&apos; functionality if you don&apos;t use this namespace, but perhaps something here might make your day a little nicer. Nearly every function takes advantage of <code>speculoos.core/all-paths</code>, which decomposes a heterogeneous, arbitrarily-nested data structure into a sequence of paths and datums. With that in hand, these not-clever functions churn through the entries and give us back something useful.</p><pre><code>(require &apos;[speculoos.utility :refer
&nbsp;          [scalars-without-predicates predicates-without-scalars
&nbsp;           collections-without-predicates predicates-without-collections
&nbsp;           sore-thumb spec-from-data data-from-spec
&nbsp;           basic-collection-spec-from-data]])</code></pre><p>Recall that Speculoos only validates using elements in the data and predicates in the specification located at identical paths. This next duo of utilities tells us where we have unmatched scalars or unmatched predicates. The first of the duo tells us about un-paired scalars.</p><pre><code>(scalars-without-predicates [42 [&quot;abc&quot; 22/7]]
&nbsp;                           [int?])
;; =&gt; #{{:path [1 0], :value &quot;abc&quot;}
;;      {:path [1 1], :value 22/7}}</code></pre><p>With this information, we can see if the specification was ignoring scalars that we were expecting to validate, and adjust our specification for better coverage. (The <code>thoroughly-…</code> <a href="#thorough"> group of functions</a> would strictly enforce all datums be paired with predicates.)</p><p>The second utility of that duo performs the complementary operation by telling us about un-paired predicates.</p><pre><code>(predicates-without-scalars [42]
&nbsp;                           [int? string? ratio?])
;; =&gt; ({:path [1], :value string?}
;;     {:path [2], :value ratio?})</code></pre><p>It is especially helpful for <a href="#troubleshooting">diagnosing surprising results</a>. Just because we put a predicate into the scalar specification doesn&apos;t force validation of a scalar that doesn&apos;t exist.</p><pre><code>(predicates-without-scalars [42 &quot;abc&quot;]
&nbsp;                           [int? [string? ratio?]])
;; =&gt; ({:path [1 0], :value string?}
;;     {:path [1 1], :value ratio?})</code></pre><p>Now we can see two un-paired predicates. <code>ratio?</code> simply doesn&apos;t have a scalar to pair with, and <code>string?</code> doesn&apos;t share a path with <code>&quot;abc&quot;</code> so it wasn&apos;t used during validation.</p><p>It&apos;s not difficult to neglect a predicate for a nested element within a collection specification, so Speculoos offers analogous utilities to highlight those possible issues.</p><pre><code>(collections-without-predicates [11 [22 {:a 33}]]
&nbsp;                               [vector? [{:is-a-map? map?}]])
;; =&gt; #{{:path [1], :value [22 {:a 33}]}}</code></pre><p>Yup, we didn&apos;t specify that inner vector whose first element is <code>22</code>. That&apos;s okay, though. Maybe we don&apos;t care to specify it. But at least, we&apos;re aware, now.</p><p>Maybe we put a predicate into a collection specification that clearly ought to be unsatisfied, but for some reason, <code>validate-collections</code> isn&apos;t picking it up.</p><pre><code>(predicates-without-collections {:a 42}
&nbsp;                               {:is-map? map?, :b [set?]})
;; =&gt; #{{:path [:b 0], :value set?}}</code></pre><p>Aha. <code>set?</code> in the collection specification isn&apos;t paired with an element in the data, so it is unused during validation.</p><p>Taking those ideas further, the <a href="#thorough"><em>thorough validation variants</em></a> return <code>true</code> only if every scalar and every collection in data have a corresponding predicate in the scalar specification and the collection specification, respectively, and all those predicates are satisfied.</p><p>This next utility is probably only useful during development. Given data and a scalar specification, <code>sore-thumb</code> prints back both, but with only the invalid scalars and predicates showing.</p><div class="no-display">#&apos;speculoos-project-readme-generator/sore-thumb-example#&apos;speculoos-project-readme-generator/sore-thumb-example-eval</div><pre><code>(sore-thumb [42 {:a true, :b [22/7 :foo]} 1.23]
&nbsp;           [int? {:a boolean?, :b [ratio? string?]} int?])</code><br /><br /><br /><code>;; to *out*</code><br /><br /><code>data: [_ {:a _, :b [_ :foo]} 1.23]
spec: [_ {:a _, :b [_ string?]} int?]
</code></pre><p>I&apos;ve found it handy for quickly pin-pointing the unsatisfied scalar-predicate pairs in a large, deeply-nested data structure.</p><p>I think of the next few utilities as <em>creative</em>, making something that didn&apos;t previously exist. We&apos;ll start with a pair of functions which perform complimentary actions.</p><pre><code>(spec-from-data [33 {:a :baz, :b [1/3 false]} &apos;(3.14 \z)])
;; =&gt; [int?
;;     {:a keyword?,
;;      :b [ratio? boolean?]}
;;     (double? char?)]</code><br /><br /><br /><code>(data-from-spec
&nbsp; {:x int?, :y [ratio? boolean?], :z (list char? neg-int?)}
&nbsp; :random)
;; =&gt; {:x -711,
;;     :y [-13/27 true],
;;     :z (\9 -18)}</code></pre><p>I hope their names give good indications of what they do. The generated specification contains only basic predicates, that is, merely <em>Is it an integer?</em>, not <em>Is it an even integer greater than 25, divisible by 3?</em>. But it&apos;s convenient raw material to start crafting a tighter specification. (Oh, yeah…they both round-trip.) A few <a href="#custom-generators">paragraphs down</a> we&apos;ll see some ways to create random sample generators for compound predicates.</p><p>Speaking of raw material, Speculoos also has a collection specification generator.</p><pre><code>(basic-collection-spec-from-data [55 {:q 33, :r [&apos;foo &apos;bar]} &apos;(22 44 66)])
;; =&gt; [{:r [vector?], :speculoos.utility/collection-predicate map?} (list?) vector?]</code></pre><p>Which produces a specification that is perhaps not immediately useful, but does provide a good starting template, because collection specifications can be tricky to get just right.</p><p id="custom-generators">The <code>utility</code> namespace contains a trio of functions to assist <a href="#exercising">writing, checking, and locating</a> compound predicates that can be used by <code>data-from-spec</code>, <code>validate-fn</code>, and <code>validate-fn-with</code> to generate valid random sample data. A compound predicate such as <code>#(and (int? %) (&lt; % 100))</code> does not have built-in generator provided by <code>clojure.test.check.generators</code>. However, <code>data-from-spec</code> and friends can extract a generator residing in the predicate&apos;s metadata. The <code>defpred</code> utility <a href="#access-gen"> streamlines</a> that task.</p></section><section id="predicates"><h2>Predicates</h2><p>A predicate function returns a truthy or falsey value.</p><pre><code>(#(&lt;= 5 %) 3) ;; =&gt; false</code><br /><br /><br /><code>(#(= 3 (count %)) [1 2 3]) ;; =&gt; true</code></pre><p>Non-boolean returns work, too. For example, <a href="#sets">sets</a> make wonderful membership tests.</p><pre><code>;; truthy</code><br /><code>(#{:blue :green :orange :purple :red :yellow} :green) ;; =&gt; :green</code><br /><br /><code>;; falsey</code><br /><code>(#{:blue :green :orange :purple :red :yellow} :swim) ;; =&gt; nil</code></pre><p>Regular expressions come in handy for validating string contents.</p><pre><code>;; truthy</code><br /><code>(re-find #&quot;^Four&quot; &quot;Four score and seven years ago...&quot;) ;; =&gt; &quot;Four&quot;</code><br /><br /><code>;; falsey</code><br /><code>(re-find #&quot;^Four&quot; &quot;When in the course of human events...&quot;) ;; =&gt; nil</code></pre><p>Invoking a predicate when supplied with a datum — scalar or collection — is the core action of Speculoos&apos; validation.</p><pre><code>(int? 42) ;; =&gt; true</code><br /><br /><br /><code>(validate-scalars [42]
&nbsp;                 [int?])
;; =&gt; [{:datum 42,
;;      :path [0],
;;      :predicate int?,
;;      :valid? true}]</code></pre><p>Speculoos is fairly ambivalent about the predicate return value. The <code>validate…</code> <a href="#fn-terminology">family of functions</a> mindlessly churns through its sequence of predicate-datum pairs, evaluates them, and stuffs the results into <code>:valid?</code> keys. The <code>valid…?</code> family of functions rips through <em>that</em> sequence, and if none of the results are falsey, returns <code>true</code>, otherwise it returns <code>false</code>.</p><p>For most of this document, we&apos;ve been using the built-in predicates offered by <code>clojure.core</code> such as <code>int?</code> and <code>vector?</code> because they&apos;re short, understandable, and they render clearly. But in practice, it&apos;s not terribly useful to validate an element with a mere <em>Is this scalar an integer?</em> or <em>Is this collection a vector?</em> Often, we&apos;ll want to combine multiple predicates to make the validation more specific. We could certainly use <code>clojure.core/and</code>…</p><pre><code>#(and (int? %) (pos? %) (even? %))</code></pre><p>…and <code>clojure.core/or</code>…</p><pre><code>#(or (string? %) (char? %))</code></pre><p>…which have the benefit of being universally understood. But Clojure also provides a pair of nice functions that streamline the expression and convey our intention. <code>every-pred</code> composes an arbitrary number of predicates with <code>and</code> semantics.</p><pre><code>((every-pred number? pos? even?) 100) ;; =&gt; true</code></pre><p>Similarly, <code>some-fn</code> composes predicates with <code>or</code> semantics.</p><pre><code>((some-fn number? string? boolean?) \z) ;; =&gt; false</code></pre><p>When Speculoos validates the scalars of a sequence, it consumes each element in turn. If we care only about validating some of the elements, we must include placeholders in the specification to maintain the sequence of predicates.</p><p>For example, suppose we only want to validate <code>\z</code>, the third element of <code>[42 :foo \z]</code>. The first two elements are irrelevant to us. We have a few options. We could write our own little always-true predicate. <code>#(true)</code> won&apos;t work because <code>true</code> is not invocable. <code>#(identity true)</code> loses the conciseness. This works…</p><pre><code>(fn [] true)</code></pre><p>…but Clojure already includes a couple of nice options.</p><pre><code>(valid-scalars? [42 :foo \z]
&nbsp;               [(constantly true) (constantly true) char?])
;; =&gt; true</code></pre><p><code>constantly</code> is nice because it accepts any number of args. But for my money, nothing tops <code>any?</code>.</p><pre><code>(valid-scalars? [42 :foo \z]
&nbsp;               [any? any? char?]) ;; =&gt; true</code></pre><p><code>any?</code> is four characters, doesn&apos;t require typing parentheses, and the everyday usage of <em>any</em> aligns well with its technical purpose.</p><p>A word of warning about <code>clojure.core/contains?</code>. It might seem natural to use <code>contains?</code> to check if a collection contains an item, but it doesn&apos;t do what its name suggests. Observe.</p><pre><code>(contains? [97 98 99] 1) ;; =&gt; true</code></pre><p><code>contains?</code> actually tells us whether a collection contains a key. For a vector, it tests for an index. If we&apos;d like to check whether a value is contained in a collection, we can use this pattern.</p><pre><code>(defn in? [coll item] (some #(= item %) coll))</code><br /><br /><br /><code>;; integer 98 is a value found in the vector</code><br /><br /><code>(in? [97 98 99] 98) ;; =&gt; true</code><br /><br /><br /><code>;; integer 1 is not a value found in the vector</code><br /><br /><code>(in? [97 98 99] 1) ;; =&gt; false</code></pre><p>(Check out <code>speculoos.utility/in?</code>.)</p><p>I&apos;ve been using the <code>#(…)</code> form because it&apos;s compact, but it does have a drawback when Speculoos renders the function in a validation report.</p><pre><code>[{:path [0],
&nbsp; :datum 42,
&nbsp; :predicate #function[documentation/eval94717/fn--94718],
&nbsp; :valid? false}]</code></pre><p>The function rendering is not terribly informative when the validation displays the predicate. Same problem with <code>(fn [v] (…))</code>.</p><p>One solution to this issue is to define our predicates with an informative name.</p><pre><code>(def greater-than-50? #(&lt; 50 %))</code><br /><br /><br /><code>(validate-scalars [42]
&nbsp;                 [greater-than-50?])
;; =&gt; [{:datum 42,
;;      :path [0],
;;      :predicate greater-than-50?,
;;      :valid? false}]</code></pre><p>Now, the predicate entry carries a bit more meaning.</p><p>Regular expressions check the content of strings.</p><pre><code>(def re #&quot;F\dQ\d&quot;)</code><br /><br /><br /><code>(defn re-pred [s] (re-matches re s))</code><br /><br /><br /><code>(validate-scalars [&quot;F1Q5&quot; &quot;F2QQ&quot;]
&nbsp;                 [re-pred re-pred])
;; =&gt; [{:datum &quot;F1Q5&quot;, :path [0], :predicate re-pred, :valid? &quot;F1Q5&quot;}
;;     {:datum &quot;F2QQ&quot;, :path [1], :predicate re-pred, :valid? nil}]</code></pre><p>Speculoos considers regexes in a scalar specification as predicates, so we can simply jam them in there.</p><pre><code>(valid-scalars? [&quot;A1B2&quot; &quot;CDEF&quot;]
&nbsp;               [#&quot;(\w\d){2}&quot; #&quot;\w{4}&quot;])
;; =&gt; true</code><br /><br /><br /><code>(validate-scalars {:a &quot;foo&quot;, :b &quot;bar&quot;}
&nbsp;                 {:a #&quot;f.\w&quot;, :b #&quot;^[abr]{0,3}$&quot;})
;; =&gt; [{:datum &quot;foo&quot;,
;;      :path [:a],
;;      :predicate #&quot;f.\w&quot;,
;;      :valid? &quot;foo&quot;}
;;     {:datum &quot;bar&quot;,
;;      :path [:b],
;;      :predicate #&quot;^[abr]{0,3}$&quot;,
;;      :valid? &quot;bar&quot;}]</code></pre><p>Using bare regexes in our scalar specification has a nice side benefit in that the <code>data-from-spec</code>, <code>exercise</code>, and <code>exercise-fn</code> utilities can inspect the regex and automatically generate valid strings.</p><p>Beyond their critical role they play in validating data, predicate functions can also carry metadata that describes how to <a href="#exercising">generate valid, random samples</a>. To help with that task, the <a href="#utilities">utility namespace</a> provides <code>defpred</code>, a helper macro that streamlines <strong>def</strong>ing <strong>pred</strong>icates and associating random sample generators.</p><p>Instead of storing specifications in a dedicated <a href="https://clojure.org/guides/spec#_registry">registry</a>,  Speculoos takes a <em>laissez-faire</em> approach: specifications may live directly in whatever namespace we please. If we feel that some sort of registry would be useful, we could make our own <a href="https://github.com/clojure/spec.alpha/blob/c630a0b8f1f47275e1a476dcdf77507316bad5bc/src/main/clojure/clojure/spec/alpha.clj#L52">modeled after</a> <code>spec.alpha</code>&apos;s.</p><p>Finally, when checking function correctness, <a href="#fn-correctness">validating the relationship</a> between the function&apos;s arguments and the function&apos;s return value uses a function that kinda looks like a predicate. In contrast to a typical predicate that accepts one argument, that relationship-checking function accepts exactly two elements: the function&apos;s argument sequence and the function&apos;s return value.</p></section><section id="non-terminating-sequences"><h2>Non-terminating sequences</h2><p>Speculoos absorbs lots of power from Clojure&apos;s infinite, lazy sequences. That power stems from the fact that Speculoos only validates complete pairs of datums and predicates. Datums without predicates are not validated, and predicates without datums are ignored. That policy provides optionality in our data. If a datum is present, it is validated against its corresponding predicate, but if that datum is non-existent, it is not required.</p><p>In the following examples, the first argument in the upper row is the data, the second argument in the lower row is the specification.</p><pre><code>;; un-paired scalar predicates</code><br /><br /><code>(validate-scalars [42]
&nbsp;                 [int? keyword? char?])
;; =&gt; [{:datum 42,
;;      :path [0],
;;      :predicate int?,
;;      :valid? true}]</code><br /><br /><br /><code>;; un-paired scalar datums</code><br /><br /><code>(validate-scalars [42 :foo \z]
&nbsp;                 [int?])
;; =&gt; [{:datum 42,
;;      :path [0],
;;      :predicate int?,
;;      :valid? true}]</code></pre><p>We remember <a href="#mottos">Motto #3</a>: <em>Ignore un-paired predicates and un-paired datums. </em> In the first example, only the single integer <code>42</code> is validated because it was paired with predicate <code>int?</code>; the remaining two predicates, <code>keyword?</code> and <code>char?</code>, are un-paired, and therefore ignored. In the second example, only  <code>int?</code> participated in validation because it was the only predicate that pairs with a scalar. Scalars <code>:foo</code> and <code>\z</code> were not paired with a predicate, and were therefore ignored. The fact that the specification vector is shorter than the data implies that any trailing, un-paired data elements are un-specified. We can take advantage of this fact by intentionally making either the data or the specification <em>run off the end</em>.</p><p>First, if we&apos;d like to validate a non-terminating sequence, specify as many datums as necessary to capture the pattern. <code>repeat</code> produces multiple instances of a single value, so we only need to specify one datum.</p><pre><code>(validate-scalars (repeat 3)
&nbsp;                 [int?])
;; =&gt; [{:datum 3,
;;      :path [0],
;;      :predicate int?,
;;      :valid? true}]</code></pre><p>Despite <code>(repeat 3)</code> producing a non-terminating sequence of integers, only the first integer was validated because that&apos;s the only predicate supplied by the specification.</p><p><code>cycle</code> can produce different values, so we ought to test for as many as appear in the definition.</p><pre><code>(validate-scalars (cycle [42 :foo 22/7])
&nbsp;                 [int? keyword? ratio?])
;; =&gt; [{:datum 42,
;;      :path [0],
;;      :predicate int?,
;;      :valid? true}
;;     {:datum :foo,
;;      :path [1],
;;      :predicate keyword?,
;;      :valid? true}
;;     {:datum 22/7,
;;      :path [2],
;;      :predicate ratio?,
;;      :valid? true}]</code></pre><p>Three unique datums. Only three predicates needed.</p><p>On the other side of the coin, non-terminating sequences serve a critical role in composing Speculoos specifications. They express <em>I don&apos;t know how many items there are in this sequence, but they all must satisfy these predicates</em>.</p><pre><code>(valid-scalars? [1] (repeat int?)) ;; =&gt; true</code><br /><code>(valid-scalars? [1 2] (repeat int?)) ;; =&gt; true</code><br /><code>(valid-scalars? [1 2 3] (repeat int?)) ;; =&gt; true</code><br /><code>(valid-scalars? [1 2 3 4] (repeat int?)) ;; =&gt; true</code><br /><code>(valid-scalars? [1 2 3 4 5] (repeat int?)) ;; =&gt; true</code></pre><p>Basically, this idiom serves the role of a regular expression <code>zero-or-more</code>. Let&apos;s pretend we&apos;d like to validate an integer, then a string, followed by any number of characters. We compose our specification like this.</p><pre><code>;; use `concat` to append an infinite sequence of `char?`</code><br /><br /><code>(validate-scalars [99 &quot;abc&quot; \x \y \z]
&nbsp;                 (concat [int? string?] (repeat char?)))
;; =&gt; [{:datum 99,
;;      :path [0],
;;      :predicate int?,
;;      :valid? true}
;;     {:datum &quot;abc&quot;,
;;      :path [1],
;;      :predicate string?,
;;      :valid? true}
;;     {:datum \x,
;;      :path [2],
;;      :predicate char?,
;;      :valid? true}
;;     {:datum \y,
;;      :path [3],
;;      :predicate char?,
;;      :valid? true}
;;     {:datum \z,
;;      :path [4],
;;      :predicate char?,
;;      :valid? true}]</code><br /><br /><br /><code>(require &apos;[speculoos.core :refer [only-invalid]])</code><br /><br /><br /><code>;; string &quot;y&quot; will not satisfy scalar predicate `char?`; use `only-valid` to highlight invalid element</code><br /><br /><code>(only-invalid (validate-scalars [99 &quot;abc&quot; \x &quot;y&quot; \z]
&nbsp;                               (concat [int? string?] (repeat char?))))
;; =&gt; ({:datum &quot;y&quot;,
;;      :path [3],
;;      :predicate char?,
;;      :valid? false})</code></pre><p>Or perhaps we&apos;d like to validate a function&apos;s argument list composed of a ratio followed by <code>&amp;-args</code> consisting of any number of alternating keyword-string pairs.</p><pre><code>;; zero &amp;-args</code><br /><br /><code>(valid-scalars? [2/3]
&nbsp;               (concat [ratio?] (cycle [keyword string?])))
;; =&gt; true</code><br /><br /><br /><code>;; two pairs of keyword+string optional args</code><br /><br /><code>(valid-scalars? [2/3 :opt1 &quot;abc&quot; :opt2 &quot;xyz&quot;]
&nbsp;               (concat [ratio?] (cycle [keyword string?])))
;; =&gt; true</code><br /><br /><br /><code>;; one pair of optional args; &apos;foo does not satisfy `string?` scalar predicate</code><br /><br /><code>(only-invalid (validate-scalars [2/3 :opt1 &apos;foo]
&nbsp;                               (concat [ratio?] (cycle [keyword string?]))))
;; =&gt; ({:datum foo,
;;      :path [2],
;;      :predicate string?,
;;      :valid? false})</code></pre><p>Using non-terminating sequences this way sorta replicates <code>spec.alpha</code>&apos;s sequence regexes. I think of it as Speculoos&apos; super-power.</p><p>Also, Speculoos can handle nested, non-terminating sequences.</p><pre><code>(valid-scalars? [[1] [2 &quot;2&quot;] [3 &quot;3&quot; :3]]
&nbsp;               (repeat (cycle [int? string? keyword?])))
;; =&gt; true</code></pre><p>This specification is satisfied with a <em>Possibly infinite sequence of arbitrary-length vectors, each vector containing a pattern of an integer, then a string, followed by a keyword</em>.</p><p>One detail that affects usage: A non-terminating sequence must not appear at the same path within both the data and specification. I am not aware of any method to inspect a sequence to determine if it is infinite, so Speculoos will refuse to validate a non-terminating data sequence at the same path as a non-terminating predicate sequence, and <em>vice versa</em>. However, feel free to use them in either data or in the specification, as long as they live at different paths.</p><pre><code>;; data&apos;s infinite sequence at :a, specification&apos;s infinite sequence at :b</code><br /><br /><code>(valid-scalars? {:a (repeat 42), :b [22/7 true]}
&nbsp;               {:a [int?], :b (cycle [ratio? boolean?])})
;; =&gt; true</code><br /><br /><br /><code>;; demo of some invalid scalars</code><br /><br /><code>(only-invalid (validate-scalars {:a (repeat 42), :b [22/7 true]}
&nbsp;                               {:a [int? int? string?], :b (repeat ratio?)}))
;; =&gt; ({:datum 42,
;;      :path [:a 2],
;;      :predicate string?,
;;      :valid? false}
;;     {:datum true,
;;      :path [:b 1],
;;      :predicate ratio?,
;;      :valid? false})</code></pre><p>In both cases above, the data contains a non-terminating sequence at key <code>:a</code>, while the specification contains a non-terminating sequence at key <code>:b</code>. Since in both cases, the two infinite sequences do not share a path, validation can proceed to completion.</p><p>So what&apos;s going on? Internally, Speculoos finds all the potentially non-terminating sequences in both the data and the specification. For each of those hits, Speculoos looks into the other nested structure to determine how long the counterpart sequence is. Speculoos then <em>clamps</em> the non-terminating sequence to that length. Validation proceeds with the clamped sequences. Let&apos;s see the clamping in action.</p><pre><code>(require &apos;[speculoos.core :refer [expand-and-clamp-1]])</code><br /><br /><br /><code>(expand-and-clamp-1 (range) [int? int? int?]) ;; =&gt; [0 1 2]</code></pre><p><code>range</code> would have continued merrily on forever, but the clamp truncated it at three elements, the length of the second argument vector. That&apos;s why two non-terminating sequences at the same path are not permitted. Speculoos has no way of knowing how short or long the sequences ought to be, so instead of making a bad guess, it throws the issue back to us. The way <em>we</em> indicate how long it should be is by making the counterpart sequence a specific length. Where should Speculoos clamp that <code>(range)</code> in the above example? The answer is the length of the other sequential thing, <code>[int? int? int?]</code>, or three elements.</p><p>Speculoos&apos; <a href="#utilities">utility</a> namespace provides a <code>clamp-in*</code> tool for us to clamp any sequence within a homogeneous, arbitrarily-nested data structure. We invoke it with a pattern of arguments similar to <code>clojure.core/assoc-in</code>.</p><pre><code>(require &apos;[speculoos.utility :refer [clamp-in*]])</code><br /><br /><br /><code>(clamp-in* {:a 42, :b [&apos;foo 22/7 {:c (cycle [3 2 1])}]}
&nbsp;          [:b 2 :c]
&nbsp;          5)
;; =&gt; {:a 42, :b [foo 22/7 {:c [3 2 1 3 2]}]}</code></pre><p><code>clamp-in*</code> used the path <code>[:b 2 :c]</code> to locate the non-terminating <code>cycle</code> sequence, clamped it to <code>5</code> elements, and returned the new data structure with that terminating sequence, converted to a vector. This way, if Speculoos squawks at us for having two non-terminating sequences at the same path, we have a way to clamp the data, specification, or both at any path, and validation can proceed.</p><p>Be sure to set your development environment&apos;s printing length</p><pre><code>(set! *print-length* 99) ;; =&gt; 99</code></pre><p>or you may jam up your session.</p></section><section id="sets"><h2>Sets</h2><p>Sets are…a handful. They perform certain tasks with elegance that ought not be dismissed, but using sets present some unique challenges compared to the other Clojure collections. <em>The elements in a set are addressed by their identities.</em> What does that even mean? Let&apos;s compare to Clojure&apos;s other collections to get some context.</p><p>The elements of a sequence are addressed by monotonically increasing integer indexes. Give a vector index <code>2</code> and it&apos;ll give us back the third element, if it exists.</p><pre><code>([11 22 33] 2) ;; =&gt; 33</code></pre><p>The elements of a map are addressed by its keys. Give a map a key <code>:howdy</code> and it&apos;ll give us back the value at that key, if it exists.</p><pre><code>({:hey &quot;salut&quot;, :howdy &quot;bonjour&quot;} :howdy) ;; =&gt; &quot;bonjour&quot;</code></pre><p>Give a set some value, and it will give us back that value…</p><pre><code>(#{:index :middle :pinky :ring :thumb} :thumb) ;; =&gt; :thumb</code></pre><p>…but only if that element exists in the set.</p><pre><code>(#{:index :middle :pinky :ring :thumb} :bird) ;; =&gt; nil</code></pre><p>So the <a href="#path">paths</a> to elements of vectors, lists, and maps are composed of indexes or keys. The paths to members of a set are the thing themselves. Let&apos;s take a look at a couple of examples.</p><p>We use <code>all-paths</code> to <a href="#mechanics"> enumerate the paths</a> of elements contained in a Clojure data collection.</p><pre><code>(all-paths #{:foo 42 &quot;abc&quot;})
;; =&gt; [{:path [], :value #{42 :foo &quot;abc&quot;}}
;;     {:path [&quot;abc&quot;], :value &quot;abc&quot;}
;;     {:path [:foo], :value :foo}
;;     {:path [42], :value 42}]</code></pre><p>In this first example, the root element, a set, has a path <code>[]</code>. The remaining three elements, direct descendants of the root set have paths that consist of themselves. We find <code>42</code> at path <code>[42]</code> and so on.</p><p>The second example applies the principle further. This set contains one integer and one set-nested-in-a-vector-nested-in-a-map.</p><pre><code>(all-paths #{11 {:a [22 #{33}]}})
;; =&gt; [{:path [], :value #{11 {:a [22 #{33}]}}}
;;     {:path [{:a [22 #{33}]}], :value {:a [22 #{33}]}}
;;     {:path [{:a [22 #{33}]} :a], :value [22 #{33}]}
;;     {:path [{:a [22 #{33}]} :a 0], :value 22}
;;     {:path [{:a [22 #{33}]} :a 1], :value #{33}}
;;     {:path [{:a [22 #{33}]} :a 1 33], :value 33}
;;     {:path [11], :value 11}]</code></pre><p>As an exercise, we&apos;ll walk through how we&apos;d navigate to that <code>33</code>. Let&apos;s borrow a function from the <a href="https://github.com/blosavio/fn-in">fn-in project</a> to zoom in on what&apos;s going on. The first argument (upper row) is our example set. The second argument (lower row) is a path. We&apos;ll build up the path to <code>33</code> piece by piece.</p><p>To start, we&apos;ll get the root.</p><pre><code>(require &apos;[fn-in.core :refer [get-in*]])</code><br /><br /><br /><code>(get-in* #{11 {:a [22 #{33}]}}
&nbsp;        [])</code><br /><code>;; =&gt; #{{:a [22 #{33}]} 11} </code></pre><p>As with any collection type, the root element has a path <code>[]</code>. Supplying <code>get-in*</code> with a path <code>[]</code> retrieves the entire collection.</p><p>There are two direct descendants of the root set: scalar <code>11</code> and a map. We&apos;ve already seen that the integer&apos;s path is the value of the integer.</p><pre><code>(get-in* #{11 {:a [22 #{33}]}} [11]) ;; =&gt; 11</code></pre><p>The path to the map is the map itself, which appears as the first element of its path. Combining the root path <code>[]</code> with the value of the map <code>{:a [22 #{33}]}</code> results in this path to the map.</p><pre><code>[{:a [22 #{33}]}]]</code></pre><p>Since we&apos;re often dealing with maps and sequentials, indexed by keywords and integers, that path may look unusual. But Speculoos handles goofy paths without skipping a beat.</p><pre><code>(get-in* #{11 {:a [22 #{33}]}}
&nbsp;        [{:a [22 #{33}]}]) ;; =&gt; {:a [22 #{33}]}</code></pre><p>When supplied with that path, <code>get-in*</code> extracts the map contained in the set.</p><p>The map has one <code>MapEntry</code>, key <code>:a</code>, with an associated value, a two-element vector <code>[22 #{33}]</code>. A map value is addressed by its key, so the vector&apos;s path contains that key. Its path is that of its parent, with its <code>:a</code> key appended.</p><pre><code>(get-in* #{11 {:a [22 #{33}]}}
&nbsp;        [{:a [22 #{33}]} :a]) ;; =&gt; [22 #{33}]</code></pre><p>Paths into a vector are old hat by now. Our <code>33</code> is contained in a set, located at the second position, index <code>1</code> in zero-based land, which we append to the accumulating path.</p><pre><code>(get-in* #{11 {:a [22 #{33}]}}
&nbsp;        [{:a [22 #{33}]} :a 1]) ;; =&gt; #{33}</code></pre><p>We&apos;ve now arrived at the little nested set which holds our <code>33</code>. Items in a set are addressed by their identity, and the identity of <code>33</code> is <code>33</code>. So we append that to the path so far.</p><pre><code>(get-in* #{11 {:a [22 #{33}]}}
&nbsp;        [{:a [22 #{33}]} :a 1 33]) ;; =&gt; 33</code></pre><p>And now we&apos;ve finally fished out our <code>33</code>. Following this algorithm, we can get, change, and delete any element of any heterogeneous, arbitrarily-nested data structure, and that includes sets at any level of nesting. We could even make a path to a set, nested within a set, nested within a set.</p><p>When using Speculoos, we encounter sets in three scenarios. We&apos;ll briefly sketch the three scenarios, then later go into the details.</p><ol><li><em>Scalar validation, scalar in data, set in specification.</em><p>In this scenario, we&apos;re validating scalars, so we&apos;re using a function with <code>scalar</code> in its name.</p><pre><code>(validate-scalars [42 :red]
&nbsp;                 [int? #{:red :green :blue}])</code></pre><p>In the example above, we&apos;re testing a property of a scalar, keyword <code>:red</code>, the second element of the data (first argument, upper row). The set <code>#{:red :green :blue}</code> in the specification (lower row) is a predicate-like thing that tests membership.</p></li><li><em>Scalar validation, set in data, set in specification.</em><p>In this scenario, we&apos;re validating scalars, so we&apos;re using a scalar validation function, again <code>validate-scalars</code>.</p><pre><code>(validate-scalars [42 #{:chocolate :vanilla :strawberry}]
&nbsp;                 [int? #{keyword?}])</code></pre><p>This time, we&apos;re validating scalars <em>contained within a set</em> in the data (upper row), with scalar predicates contained within a set in the specification (lower row).</p></li><li><em>Collection validation, set in data, set in specification.</em><p>In this scenario, we&apos;re validating a property of a collection, so we&apos;re using <code>validate-collections</code>.</p><pre><code>(validate-collections [42 #{:puppy :kitten :goldfish}]
&nbsp;                     [vector? #{set?}])</code></pre><p>Collection predicates — targeting the nested set in the data (upper row) — are themselves contained in a set nested in the collection specification (lower row).</p></li></ol><h3>1. Set as Scalar Predicate</h3><p>Let&apos;s remember back to the beginning of this section where we saw that Clojure sets can serve as membership tests. Speculoos can therefore use sets as a nice shorthand for a membership predicate.</p><pre><code>(def color? #{:red :green :blue})</code><br /><br /><code>(ifn? color?) ;; =&gt; true</code><br /><br /><br /><code>(color? :red) ;; =&gt; :red</code><br /><br /><code>(color? :plaid) ;; =&gt; nil</code></pre><p><code>color?</code> implements <code>IFn</code> and thus behaves like a predicate when invoked as a function. <code>:red</code> satisfies our <code>color?</code> predicate and returns a truthy value, <code>:red</code>, whereas <code>:plaid</code> does not and returns a falsey value, <code>nil</code>.</p><p>During scalar validation, when a scalar in our data shares a path with a set in the specification, Speculoos enters <em>set-as-a-predicate</em> mode. I say &apos;mode&apos; only in the casual sense. The implementation uses no modes nor state. The algorithm merely branches to treat the set differently depending on the scenario.</p><p>We&apos;ll make our specification mimic the shape of our data (Motto #2), but instead of two predicate functions pairing with two scalars (Motto #3), we&apos;ll insert one scalar predicate function, followed by a set, which behaves like a membership predicate.</p><pre><code>;; data</code><br /><br /><code>(all-paths [42 :red])
;; =&gt; [{:path [], :value [42 :red]}
;;     {:path [0], :value 42}
;;     {:path [1], :value :red}]</code><br /><br /><br /><code>;; scalar specification</code><br /><br /><code>(all-paths [int? #{:red :green :blue}])
;; =&gt; [{:path [], :value [int? #{:blue :green :red}]}
;;     {:path [0], :value int?}
;;     {:path [1], :value #{:blue :green :red}}
;;     {:path [1 :green], :value :green}
;;     {:path [1 :red], :value :red}
;;     {:path [1 :blue], :value :blue}]</code></pre><p>Our example data contains two scalar datums: <code>42</code> in the first spot and <code>:red</code>  in the second. Each of those datums shares a path with a predicate-like thing in the scalar specification. The <code>42</code> is paired with the <code>int?</code> scalar predicate because they both share the path <code>[0]</code>. Both <code>:red</code> and <code>#{:red :green :blue}</code> share a path <code>[1]</code>, so Speculoos regards it as a <em>set-as-a-scalar-predicate</em>.</p><p>Let&apos;s run that validation now. The data vector is the first argument in the upper row, the specification vector is the second argument in the lower row.</p><pre><code>(validate-scalars [42 :red]
&nbsp;                 [int? #{:red :green :blue}])
;; =&gt; [{:datum :red,
;;      :path [1],
;;      :predicate #{:blue :green :red},
;;      :valid? :red}
;;     {:datum 42,
;;      :path [0],
;;      :predicate int?,
;;      :valid? true}]</code></pre><p>When Speculoos validates scalars, it treats the set in the specification as a predicate because the corresponding element in the data is a scalar, not a set. In this example, <code>:red</code> is a member of the <code>#{:red :green :blue}</code> set-predicate.</p><p>The same principles hold when validating elements of a map containing a set-predicate. When a set in the specification contains a set that shares a path with a scalar in the data, that set is treated as a membership predicate.</p><pre><code>(validate-scalars {:x 42, :y :red}
&nbsp;                 {:x int?, :y #{:red :green :blue}})
;; =&gt; [{:datum :red,
;;      :path [:y],
;;      :predicate #{:blue :green :red},
;;      :valid? :red}
;;     {:datum 42,
;;      :path [:x],
;;      :predicate int?,
;;      :valid? true}]</code></pre><p>Scalar <code>42</code> pairs with predicate <code>int?</code> at path <code>[:x]</code> and scalar <code>:red</code> pairs with set-predicate <code>#{:red :green :blue}</code> at path <code>[:y]</code>. Speculoos validates scalars in the data that share paths with predicates in the specification. Since <code>#{:red :green :blue}</code> is considered a predicate, scalar <code>:red</code> is validated.</p><h3>2. Validate Scalars within Set</h3><p>Sometimes the scalars in our data are contained in a set. Speculoos can validate scalars within a set during a scalar validation operation. Validating a set&apos;s scalar members follows all the same principles as validating a vector&apos;s scalar members, except for one wrinkle: Since elements of a set have no inherent location, i.e., sets are unordered, sets in our data are validated against <em>all</em> predicates contained in the corresponding set at the same path in the specification. An example shows this better than words.</p><p>Let&apos;s enumerate the paths of some example data, with some scalars contained in a nested set, and then enumerate the paths of a specification, shaped like that data, with one predicate contained in a nested set.</p><pre><code>;; data, some scalars are contained within a set</code><br /><br /><code>(all-paths [42 #{:chocolate :vanilla :strawberry}])
;; =&gt; [{:path [], :value [42 #{:chocolate :strawberry :vanilla}]}
;;     {:path [0], :value 42}
;;     {:path [1], :value #{:chocolate :strawberry :vanilla}}
;;     {:path [1 :strawberry], :value :strawberry}
;;     {:path [1 :chocolate], :value :chocolate}
;;     {:path [1 :vanilla], :value :vanilla}]</code><br /><br /><br /><code>;; scalar specification, one predicate contained within a set</code><br /><br /><code>(all-paths [int? #{keyword?}])
;; =&gt; [{:path [], :value [int? #{keyword?}]}
;;     {:path [0], :value int?}
;;     {:path [1], :value #{keyword?}}
;;     {:path [1 keyword?], :value keyword?}]</code></pre><p>Let&apos;s apply the Mottos. We intend to validate scalars, so we&apos;ll use <code>validate-scalars</code>, which only applies predicates to scalars. Next, we&apos;ll make our our specification mimic the shape of the data. In this example, both the data and the specification are a vector, with something in the first spot, and a set in the second spot. Finally, we&apos;ll make sure that all predicates are paired with a scalar.</p><p>Now we validate the scalars.</p><pre><code>(validate-scalars [42 #{:glass :rubber :paper}]
&nbsp;                 [int? #{keyword?}])
;; =&gt; ({:datum 42,
;;      :path [0],
;;      :predicate int?,
;;      :valid? true}
;;     {:datums-set #{:glass :paper :rubber},
;;      :path [1],
;;      :predicate keyword?,
;;      :valid? true})</code></pre><p>First, notice how the scalar specification (lower row) looks a lot like the data (upper row). Because the shapes are similar, <code>validate-scalars</code> is able to systematically apply predicates from the specification to scalars in the data. Speculoos validates <code>42</code> against predicate <code>int?</code> because they share a path, <code>[0]</code>, in their respective vectors. Path <code>[1]</code> navigates to a set in both our data vector and our specification vector, so Speculoos enters <em>validate-scalars-within-a-set-mode</em>.</p><p>Every predicate contained in the specification set is applied to every datum in the data&apos;s set. In this example, <code>keyword?</code> is individually applied to <code>:glass</code>, <code>:rubber</code>, and <code>:paper</code>, and since each satisfies the predicate, the validation returns <code>true</code>.</p><p>One of the defining features of Clojure sets is that they&apos;re amorphous bags of items, without any inherent ordering. Within the context of a set, it doesn&apos;t make sense to target one scalar predicate towards one particular scalar datum. Therefore, Speculoos validates scalars contained within a set more broadly. If our specification set contains more than one predicate, each of the predicates is applied to <em>all</em> the scalars in the data&apos;s set.</p><p>In the next example, the specification set contains two predicates, <code>keyword?</code> an <code>qualified-keyword?</code>.</p><pre><code>(validate-scalars #{:chocolate}
&nbsp;                 #{keyword? qualified-keyword?})
;; =&gt; ({:datums-set #{:chocolate},
;;      :path [],
;;      :predicate qualified-keyword?,
;;      :valid? false}
;;     {:datums-set #{:chocolate},
;;      :path [],
;;      :predicate keyword?,
;;      :valid? true})</code></pre><p>Two scalar predicates in the specification applied to the one scalar datum. <code>:chocolate</code> is a keyword, but not a qualified keyword.</p><p>Next, we&apos;ll see how to validate multiple scalars with multiple scalar predicates. The set in the data contains three scalars. The set in the specification contains two predicates, same as the previous example.</p><pre><code>(validate-scalars #{:chocolate :vanilla :strawberry}
&nbsp;                 #{keyword? qualified-keyword?})
;; =&gt; ({:datums-set #{:chocolate :strawberry :vanilla},
;;      :path [],
;;      :predicate qualified-keyword?,
;;      :valid? false}
;;     {:datums-set #{:chocolate :strawberry :vanilla},
;;      :path [],
;;      :predicate keyword?,
;;      :valid? true})</code></pre><p>Validation applies <code>keyword?</code> and <code>simple-keyword?</code>, in turn, to every scalar member of the data set. Speculoos tells us that all the scalars in the data are indeed keywords, but at least one of the data&apos;s scalars is not a qualified keyword. Notice how Speculoos condenses the validation results. Instead of a validation entry for each individual scalar in the data set, Speculoos combines all the results for all the scalars, associated to key <code>:datums-set</code>. Two scalar predicates, two validation results.</p><p>Again, the same principles apply for validating sets contained in a map.</p><pre><code>(validate-scalars {:x 42, :y #{&quot;a&quot; &quot;b&quot; &quot;c&quot;}}
&nbsp;                 {:x int?, :y #{string?}})
;; =&gt; ({:datum 42,
;;      :path [:x],
;;      :predicate int?,
;;      :valid? true}
;;     {:datums-set #{&quot;a&quot; &quot;b&quot; &quot;c&quot;},
;;      :path [:y],
;;      :predicate string?,
;;      :valid? true})</code></pre><p><code>int?</code> at <code>:x</code> applies to <code>42</code> also at <code>:x</code>. Then, <code>string?</code> at <code>:y</code> is applied to scalars <code>&quot;a&quot;</code>, <code>&quot;b&quot;</code>, and <code>&quot;c&quot;</code> at <code>:y</code>.</p><p>Speculoos performs the two modes in separate passes, so we may even use both <em>set-as-a-predicate-mode</em> and <em>validate-scalars-within-a-set-mode</em> during the same validation, as long as the predicates stay on their own side of the fence.</p><pre><code>(validate-scalars [42 #{:foo :bar :baz}]
&nbsp;                 [#{40 41 42} #{keyword?}])
;; =&gt; ({:datum 42,
;;      :path [0],
;;      :predicate #{40 41 42},
;;      :valid? 42}
;;     {:datums-set #{:bar :baz :foo},
;;      :path [1],
;;      :predicate keyword?,
;;      :valid? true})</code></pre><p>In this example, the predicate <code>#{40 41 42}</code> at index <code>0</code> of the specification is a set while the datum at same index of the data is <code>42</code>, a scalar. Speculoos uses the set-as-a-predicate mode. Since <code>42</code> is a member of <code>#{40 41 42}</code>, that datum validates as truthy. Because the data at index <code>1</code> is itself a set, Speculoos performs set-scalar-validation. The <code>keyword?</code> predicate is applied to each element of <code>#{:foo :bar :baz}</code> at index <code>1</code> and they all validate <code>true</code>.</p><h3>3. Validate Set as a Collection</h3><p>Let&apos;s discuss how collection validation works when a set is involved. During a collection validation operation, Speculoos will ignore all scalars in the data. It will only apply predicates to collections. The rules are identical to how the other collections are validated: predicates from the specification are applied to the corresponding parent container in the data. But let&apos;s not get bogged down in a textual description; let&apos;s look at some examples.</p><p>First, we&apos;ll start with some data that consists of a vector containing an integer, followed by a three element set. Let&apos;s generate all the paths.</p><pre><code>(all-paths [42 #{:puppy :kitten :goldfish}])
;; =&gt; [{:path [], :value [42 #{:goldfish :kitten :puppy}]}
;;     {:path [0], :value 42}
;;     {:path [1], :value #{:goldfish :kitten :puppy}}
;;     {:path [1 :puppy], :value :puppy}
;;     {:path [1 :goldfish], :value :goldfish}
;;     {:path [1 :kitten], :value :kitten}]</code></pre><p>Motto #1: Collection validation ignores scalars, so out of all those elements, validation will only consider the root at path <code>[]</code> and the nested set at path <code>[1]</code>.</p><p>A good strategy for creating a collection specification is to copy-paste the data and delete all the scalars…</p><pre><code>[        #{    }]</code></pre><p>…and insert some collection predicates near the opening bracket.</p><pre><code>[vector? #{set?}]</code></pre><p>Let&apos;s generate the paths for that collection specification.</p><pre><code>(all-paths [vector? #{set?}])
;; =&gt; [{:path [], :value [vector? #{set?}]}
;;     {:path [0], :value vector?}
;;     {:path [1], :value #{set?}}
;;     {:path [1 set?], :value set?}]</code></pre><p>Notice the paths to the two predicates. Predicate <code>vector</code> is located at path <code>[0]</code>, while predicate <code>set?</code> is located at path <code>[1 set?]</code>. When validating collections, Speculoos only considers predicates within a specification.</p><p>Now, let&apos;s run a collection validation.</p><pre><code>(validate-collections [42 #{:puppy :kitten :goldfish}]
&nbsp;                     [vector? #{set?}])
;; =&gt; ({:datum [42 #{:goldfish :kitten :puppy}],
;;      :ordinal-path-datum [],
;;      :path-datum [],
;;      :path-predicate [0],
;;      :predicate vector?,
;;      :valid? true}
;;     {:datum #{:goldfish :kitten :puppy},
;;      :ordinal-path-datum [0],
;;      :path-datum [1],
;;      :path-predicate [1 set?],
;;      :predicate set?,
;;      :valid? true})</code></pre><p><code>validate-collections</code> was able to pair two collections in the data with two predicates in the specification, and we received two validation results. Collection predicate <code>vector?</code> at path <code>[0]</code> in the specification was applied to whatever is at path <code>(drop-last [0])</code> in the data, which happens to be the root collection. Collection predicate <code>set?</code> at path <code>[1 set?]</code> in the specification was applied to path <code>(drop-last [1 set?])</code> in the data, which happens to be our nested set containing pet keywords. Both predicates were satisfied.</p><p>Remember: Scalar predicates apply to the scalar at their exact location. Collection predicates apply to the collection directly above their head.</p></section><section id="troubleshooting"><h2>Troubleshooting</h2><p>If you see surprising results, try these ideas.</p><ul><li><p>Remember the <a href="#mottos">Three Mottos</a>, and follow them.<ol><li><strong>Validate scalars separately from validating collections.</strong><p>We should never have a collection predicate like <code>vector?</code> in a scalar specification. Similarly, scalar predicates like <code>int?</code> should only appear in a collection specification in the context of testing a collection, like…</p><pre><code>(defn all-ints? [v] (every? #(int? %) v))</code></pre><p>…or when validating some relationship <em>between</em> datums, like this.</p><pre><code>(defn b-greater-than-a? [m] (&lt; (m :a) (m :b)))</code></pre><p>The function names <code>validate-scalars</code>, <code>validate-collections</code>, et. al., are strong beacons to remind you that you&apos;re either validating scalars, or validating collections.</p></li><li><strong>Make the specification mimic the shape of the data.</strong><p>The Speculoos functions don&apos;t enforce any requirements on the data and specification. If we feed it data that&apos;s a map and a specification that&apos;s a vector, it will dutifully try to validate what it has.</p><pre><code>(validate-scalars {:a 99}
&nbsp;                 [int?]) ;; =&gt; []</code><br /><br /><code>;; No error nor exception with map data and vector specification</code></pre><p><code>validate-scalars</code> was not able to pair any predicates with datums, so it returns an empty vector.</p><p>One word of warning: Because sequential things are indexed by integers, and map elements may also be indexed by integers, we could certainly abuse that flexibility like this.</p><pre><code>;; data is a vector, specification is a map keyed with integers</code><br /><br /><code>(validate-scalars [42 &quot;abc&quot; 22/7]
&nbsp;                 {0 int?, 1 string?, 2 ratio?})
;; =&gt; [{:datum 42,
;;      :path [0],
;;      :predicate int?,
;;      :valid? true}
;;     {:datum &quot;abc&quot;,
;;      :path [1],
;;      :predicate string?,
;;      :valid? true}
;;     {:datum 22/7,
;;      :path [2],
;;      :predicate ratio?,
;;      :valid? true}]</code></pre><p>Speculoos merely knows that it could successfully locate <code>42</code> and <code>int?</code> at <code>0</code>, etc. It &apos;worked&apos; in this instance, but surprise lurks if we try to get to overly clever.</p></li><li><strong>Validation ignores un-paired predicates and un-paired datums.</strong><p>A decent number of surprising validations result from predicates pairing to unexpected datums or not being paired at all.</p><pre><code>;; Oops! specification contains un-paired key :c; string &quot;abc&quot; isn&apos;t validated</code><br /><br /><code>(valid-scalars? {:a 42, :b &quot;abc&quot;}
&nbsp;               {:a int?, :c symbol?})
;; =&gt; true</code><br /><br /><br /><code>;; Oops! specification uses an extra level of nesting; [33] wasn&apos;t validated</code><br /><br /><code>(validate-collections [11 [22 [33]]] [[[[list?]]]]) ;; =&gt; ()</code></pre><p>Corollary: <strong><code>valid?</code> being <code>true</code> means there were zero non-true results.</strong> If the validation did not find any predicate+datum pairs, there would be zero invalid results, and thus return valid. Use the <code>thorough-…</code> function <a href="#thorough">variants</a> to require all datums to be validated.</p><p>See below for strategies and tools for diagnosing mis-pairing.</p></li></ol></p></li><li><p>Checking the presence or absence of an element is the job of a collection validation. Scalar validation is only concerned with testing the properties of a scalar, <em>assuming that scalar exists</em>.</p><p>Testing whether an integer, located in the first slot of a vector, is greater than forty…</p><pre><code>(valid-scalars? [42]
&nbsp;               [#(&lt; 40 %)]) ;; =&gt; true</code></pre><p>…is a completely orthogonal concern from whether there is anything present in the first slot of a vector.</p><pre><code>(valid-collections? [42]
&nbsp;                   [#(get % 0)]) ;; =&gt; true</code></pre><p>Asking about an element&apos;s presence is, fundamentally, asking about whether a collection contains an item. If we want to test both a property of the scalar <em>and</em> its existence at a particular location in a collection, we could use the <a href="#combo">combo utility</a> functions.</p><pre><code>(valid? [42]
&nbsp;       [#(&lt; 40 %)]
&nbsp;       [#(get % 0)]) ;; =&gt; true</code></pre><p>This combo pattern validates the concept <em>The first element must exist, and it must be larger than forty</em>.</p></li><li><p>How would we validate the concept <em>The third element of a sequential collection is a scalar <strong>or</strong> a nested collection</em>? Both the following are valid.</p><pre><code>[42 &quot;abc&quot; 22/7]</code><br /><br /><code>[42 &quot;abc&quot; [&apos;foo]]</code></pre><p>The example in the upper row contains a scalar in the third position, while the example in the lower row contains a nested vector in the third position. According to our English language specification, both would be valid.</p><p>Scalar validation discards all non-scalar elements (i.e., collections), so we must rely on the power and flexibility of collection validation. Collection validation passes the collection itself to the predicate, so the predicate has access to the collection&apos;s elements.</p><p>We would write our predicate to pull out that third element and test whether it was a ratio or a vector.</p><pre><code>(defn third-element-ratio-or-vec?
&nbsp; [c]
&nbsp; (or (ratio? (get c 2)) (vector? (get c 2))))</code></pre><p>The validation passes the entire collection, <code>c</code>, to our predicate, and the predicate does the grunt work of pulling out the third element by using <code>(get c 2)</code>.</p><p>The validation would then look like this.</p><pre><code>(valid-collections? [42 &quot;abc&quot; 22/7]
&nbsp;                   [third-element-ratio-or-vec?])
;; =&gt; true</code><br /><br /><br /><code>(valid-collections? [42 &quot;abc&quot; [&apos;foo]]
&nbsp;                   [third-element-ratio-or-vec?])
;; =&gt; true</code></pre><p>The first validation returns <code>true</code> because <code>22/9</code> satisfies our <code>third-element-ratio-or-vec?</code> predicate. The second validation returns <code>true</code> because <code>[&apos;foo]</code> also satisfies <code>third-element-ratio-or-vec?</code>.</p><p>The principle holds for all collection types: <em>Collection validation is required when either a scalar or a collection is a valid element.</em></p></li><li><p>Speculoos specifications are regular old data structures containing regular old functions. (I assume your data is, too.) If we&apos;re wrangling with something deep down in some nested mess, use our Clojure powers to dive in and pull out the relevant pieces.</p><pre><code>(let [data (get-in {:a {:b {:c [22/7]}}} [:a :b :c])
&nbsp;     spec (get-in {:a {:b {:c [int?]}}} [:a :b :c])]
&nbsp; (validate-scalars data spec))
;; =&gt; [{:datum 22/7,
;;      :path [0],
;;      :predicate int?,
;;      :valid? false}]</code></pre></li><li><p>Use the verbose functions. If we&apos;re using the high-level <code>valid-…?</code> function variants, we&apos;ll only see <code>true/false</code>, which isn&apos;t helpful when troubleshooting. The <code>validate-…</code> <a href="#fn-terminology">variants</a> are chatty and will display everything it considered during validation.</p></li><li><p>The <a href="https://blosavio.github.io/speculoos/speculoos.utility.html"><code>speculoos.utility</code></a> namespace provides many functions for creating, viewing, analyzing, and modifying both scalar and collection specifications.</p></li><li><p>When the going really gets tough, break out <code>speculoos.core/all-paths</code> and apply it to our data, then to our specification, and then step through the validation with our eyes.</p><pre><code>(all-paths {:a [99]})
;; =&gt; [{:path [], :value {:a [99]}}
;;     {:path [:a], :value [99]}
;;     {:path [:a 0], :value 99}]</code><br /><br /><br /><code>(all-paths {:a &apos;int?})
;; =&gt; [{:path [], :value {:a int?}}
;;     {:path [:a], :value int?}]</code><br /><br /><br /><code>;; Aha! The predicate `int?` at path [:a] and the integer 99 at path [:a 0] do not share a path!</code></pre></li><li><p>When validating a function&apos;s arguments, remember that arguments are contained in an implicit sequence.</p><pre><code>(defn arg-passthrough [&amp; args] args)</code><br /><br /><br /><code>(arg-passthrough [1 2 3]) ;; =&gt; ([1 2 3])</code><br /><br /><br /><code>(arg-passthrough [1 2 3] [4 5 6]) ;; =&gt; ([1 2 3] [4 5 6])</code></pre><p>If we&apos;re passing only a single value, it&apos;s easy to forget that the single value is contained in the argument sequence. Validating a function&apos;s arguments validates the <em>argument sequence</em>, not just the first lonely element that happens to also be a sequence.</p><pre><code>;; seemingly single vector in, single integer out...</code><br /><br /><code>(first [1 2 3]) ;; =&gt; 1</code><br /><br /><br /><code>;; shouldn&apos;t integer `1` fail to satisfy predicate `string?`</code><br /><br /><code>(validate-fn-with first {:speculoos/arg-scalar-spec [string?]} [1 2 3]) ;; =&gt; 1</code></pre><p><code>validate-fn-with</code> passes through the value returned by <code>first</code> because <code>validate-fn-with</code> did not find any invalid results. Why not? In this example, <code>1</code> and <code>string?</code> do not share a path, and therefore <code>validate-fn-with</code> performed zero validations. Let&apos;s take a look.</p><pre><code>(all-paths [[1 2 3]])
;; =&gt; [{:path [], :value [[1 2 3]]}
;;     {:path [0], :value [1 2 3]}
;;     {:path [0 0], :value 1}
;;     {:path [0 1], :value 2}
;;     {:path [0 2], :value 3}]</code><br /><br /><br /><code>(all-paths [string?])
;; =&gt; [{:path [], :value [string?]}
;;     {:path [0], :value string?}]</code></pre><p>We  find scalar <code>1</code> at path <code>[0 0]</code> in the <em>argument sequence</em>, while scalar predicate <code>string?</code> is located at path <code>[0]</code> in the scalar specification. The datum and predicate do not share paths, are therefore not paired, thus no validation (Motto #3). The fix is to make the specification mimic the shape of the data, the &apos;data&apos; in this case being the <em>argument sequence</em>.</p><pre><code>(validate-fn-with first {:speculoos/arg-scalar-spec [[string?]]} [1 2 3])
;; =&gt; ({:datum 1,
;;      :fn-spec-type :speculoos/argument,
;;      :path [0 0],
;;      :predicate string?,
;;      :valid? false})</code></pre><p>Now that argument scalar specification properly mimics the shape of the <em>argument sequence</em>, scalar <code>1</code> and scalar predicate <code>string?</code> share a path <code>[0 0]</code>, and <code>validate-fn-with</code> performs a scalar validation. <code>1</code> fails to satisfy <code>string?</code>.</p><p>This also applies to validating arguments that are collections.</p></li></ul><p>Finally, if you hit a wall, file a <a href="https://github.com/blosavio/speculoos/issues">bug report</a> or <a href="https://github.com/blosavio"> email me</a>.</p></section><section id="alternatives"><h2>Alternatives</h2><ul><li>Staples SparX <a href="https://github.com/staples-sparx/clj-schema">clj-schema</a><p>Schemas for Clojure data structures and values. Delineates operations on maps, seqs, and sets. Contributors: Alex Baranosky, Laurent Petit, Punit Rathore</p><br /></li><li>Steve Miner&apos;s <a href="https://github.com/miner/herbert">Herbert</a><p>A schema language for Clojure data for documenting and validating.</p><br /></li><li>Metosin <a href="https://github.com/metosin/malli">Malli</a><p>Data-driven schemas incorporating the best parts of existing libs, mixed with their own tools.</p><br /></li><li>Plumatic <a href="https://github.com/plumatic/schema">Schema</a><p>A Clojure(Script) library for declarative data description and validation.</p><br /></li><li>Christophe Grand&apos;s <a href="https://github.com/cgrand/seqexp">seqexp</a><p>Regular expressions for sequences (and other sequables).</p><br /></li><li>Jonathan Claggett&apos;s <a href="https://github.com/jclaggett/seqex">seqex</a><p>Sequence Expressions, similar to regular expressions but able to describe arbitrary sequences of values (not just characters).</p><br /></li><li>Clojure&apos;s <a href="https://github.com/clojure/spec.alpha"><code>spec.alpha</code></a><p>[A] Clojure library to describe the structure of data and functions.</p><br /></li><li>Clojure&apos;s <a href="https://github.com/clojure/spec-alpha2"><code>spec-alpha2</code> or <code>alpha.spec</code></a><p>[A]n evolution from spec.alpha as well as work towards several new features. Note: Alex Miller considers it <a href="https://ask.clojure.org/index.php/9397/clarify-usage-on-the-spec-alpha2-github-page?show=9398#a9398">a work in progress</a> as of 2020 June 20.</p><br /></li><li>Jamie Brandon&apos;s <a href="https://github.com/jamii/strucjure">Strucjure</a><p>A <a href="https://www.scattered-thoughts.net/writing/strucjure-motivation/">library for describing stuff</a> in an executable manner.</p><br /></li><li>Brian Marick&apos;s <a href="https://github.com/marick/structural-typing">structural-typing</a><p>A library that provides good error messages when checking the correctness of structures, and a way to define <a href="https://en.wikipedia.org/wiki/Structural_type_system">structural types</a> that are checked at runtime.</p><br /></li><li>Peter Taoussanis&apos; <a href="https://github.com/taoensso/truss">Truss</a><p>A tiny library that provides fast and flexible runtime assertions with terrific error messages.</p><br /></li></ul></section><section id="glossary"><h2>Glossary</h2><dl><dt id="element">element</dt><dd><p>A thing contained within a collection, either a scalar value or another nested collection.</p></dd><dt id="HANDS">heterogeneous, arbitrarily-nested data structure</dt><dd><p>Exactly one Clojure collection (vector, map, list, sequence, or set) with zero or more <a href="#element">elements</a>, nested to any depth.</p></dd><dt id="non-term-seq">non-terminating sequence</dt><dd><p>One of <code>clojure.lang.{Cycle,Iterate,LazySeq,LongRange,Range,Repeat}</code> that may or may not be realized, and possibly infinite. (I am not aware of any way to determine if such a sequence is infinite, so Speculoos treats them as if they are.)</p></dd><dt id="path">path</dt><dd><p>A series of values that unambiguously navigates to a single <a href="#element">element</a> (scalar or sub-collection) in a <a href="#HANDS">heterogeneous, arbitrarily-nested data structure</a>. In the context of the Speculoos library, the series of values comprising a path is generated by the <code>all-paths</code> function and consumed by the <code>validate-…</code> functions. Almost identical to the second argument of <a href="https://clojure.github.io/clojure/clojure.core-api.html#clojure.core/get-in"><code>clojure.core/get-in</code></a>, but with more generality.</p><p>Elements of vectors, lists, and sequences are addressed by zero-indexed integers. Map values are addressed by their keys, which are often keywords, but can be any data type, including integers, or composite types. Set members are addressed by their identities. Nested collections contained in a set can indeed be addressed: the path vector itself contains the collections. An empty vector <code>[]</code> addresses the outermost, containing collection.</p></dd><dt id="predicate">predicate</dt><dd><p>A function, or something that implements <code>IFn</code>, like a set, that returns a truthy or falsey value.  In most instances, a predicate is a function of one argument. Some Speculoos functions, such as <code>validate-scalars</code> and <code>valid-scalars?</code> also regard a regular expression as a competent predicate.</p></dd><dt id="relationship">relationship</dt><dd><p>A human- and machine-readable declaration about the congruence between two elements. Specifically, Speculoos function validation may involve specifying a relationship between the function&apos;s argument and the function&apos;s return value.</p></dd><dt id="scalar">scalar</dt><dd><p>A single, non-divisible datum, such as an integer, string, boolean, etc. Essentially, a shorter term for <em>non-collection</em>.</p></dd><dt id="specification">specification</dt><dd><p>A human- and machine-readable declaration about properties of data, composed of a <a href="#HANDS">heterogeneous, arbitrarily-nested data collection</a> and <a href="#predicate">predicates</a>.</p></dd><dt id="validate">validate</dt><dd><p>An action that returns an exhaustive listing of all datum+predicate pairs, their paths, and whether the datum satisfies the predicate. Note: Validation requires <em>two</em> components, a datum and a predicate. Any unpaired datum or any unpaired predicate, will not participate in validation.</p></dd><dt id="valid">valid?</dt><dd><p>An action that returns <code>true</code> if all paired datums satisfy their predicates during a validation, <code>false</code> otherwise. Note: A validation operation&apos;s result is considered <em>valid</em> if there are zero datum+predicates.</p></dd></dl></section><br /><h2>License</h2><p>This program and the accompanying materials are made available under the terms of the <a href="https://opensource.org/license/MIT">MIT License</a>.</p>