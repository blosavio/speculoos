<a href="https://clojars.org/com.sagevisuals/speculoos"><img src="https://img.shields.io/clojars/v/com.sagevisuals/speculoos.svg" /></a><br /><a href="#setup">Setup</a><br /><a href="https://blosavio.github.io/speculoos/index.html">API</a><br /><a href="#intro">Introduction</a><br /><a href="#mantras">Mantras</a><br /><a href="#mechanics">Mechanics</a><br /><a href="#scalar-validation">Validating Scalars</a><br /><a href="#collection-validation">Validating Collections</a><br /><a href="#valid-thorough">Validation Summaries and Thorough Validations</a><br /><a href="#function-validation">Validating Functions</a><br /><a href="#exercising">Generating Random Samples and Exercising</a><br /><a href="#utilities">Utilities</a><br /><a href="#predicates">Predicates</a><br /><a href="#non-terminating-sequences">Non-terminating Sequences</a><br /><a href="#sets">Sets</a><br /><a href="diff.html">Comparison to spec.alpha</a><br /><a href="recipes.clj">Recipes</a><br /><a href="#troubleshooting">Troubleshooting</a><br /><a href="#case-study">Case Study</a><br /><a href="#alternatives">Alternatives</a><br /><a href="#glossary">Glossary</a><br /><a href="https://github.com/blosavio">Contact</a><br /><h1>Speculoos</h1><em>An experiment with Clojure specification literals</em><br /><section id="setup"><h2>Setup</h2><h3>Leiningen/Boot</h3><pre><code>[com.sagevisuals/speculoos &quot;2&quot;]</code></pre><h3>Clojure CLI/deps.edn</h3><pre><code>com.sagevisuals/speculoos {:mvn/version &quot;2&quot;}</code></pre><h3>Require</h3><pre><code>(require &apos;[speculoos.core :refer [valid-scalars? valid-collections?]])</code></pre></section><section id="intro"><h2>Introduction</h2><p>Imagine you&apos;d like to know if <em>My Clojure vector contains an integer, then a string, and finally a ratio</em>. One example of that data vector might look like this.</p><pre><code>[42 &quot;abc&quot; 22/7]</code></pre><p>It would be nice if we could write a specification that is shaped like that data.</p><pre><code>[int? string? ratio?]</code></pre><p>Speculoos can validate our data vector with that specification vector.</p><pre><code>(valid-scalars? [42 &quot;abc&quot; 22/7]
&nbsp;               [int? string? ratio?])
;; =&gt; true</code></pre><p>Now imagine we&apos;d like ensure we have <em>A Clojure hash-map with an integer at key <code>:x</code> and a ratio at key <code>:y</code></em>. Something like this.</p><pre><code>{:x 42 :y 22/7}</code></pre><p>We could write a specification map that&apos;s shaped like that data map.</p><pre><code>{:x int? :y ratio?}</code></pre><p>Speculoos can validate our data map with that specification map.</p><pre><code>(valid-scalars? {:x 42, :y 22/7}
&nbsp;               {:x int?, :y ratio?})
;; =&gt; true</code></pre><p>Notice how in both cases, the specifications mimic the shape of the data. The vector&apos;s specification is itself a vector. The map&apos;s specification is itself a map.</p><p>Speculoos can validate any heterogeneous, arbitrarily-nested data collection using specifications composed of plain Clojure collections and functions. In short, Speculoos is an experimental library that aims to perform the same tasks as <a href="https://clojure.org/about/spec"><code>clojure.spec.alpha</code></a> with a simple, intuitive interface that employs flexible and powerful specification literals.</p></section><section id="mantras"><h2>Mantras</h2><p>When using Speculoos, remember these three Mantras:<ol><li>Validate scalars separately from validating collections.</li><li>Shape the specification to mimic the data.</li><li>Ignore un-paired predicates and un-paired datums.</li></ol></p><p>Speculoos provides functions for validating scalars contained within a heterogeneous, arbitrarily-nested data structure, and another, distinct set of functions for validating properties of those nested collections. Validating scalars separately from validating collections carries several advantages. First, Speculoos can consume specifications composed of regular Clojure data structures. Inspect and manipulate your specification with any Clojure collection-handling functions you prefer. Second, separating the two offers mental clarity about what&apos;s going on. Your predicates will only ever apply to a scalar, or to a collection, nevern both. Third, you only need to specify as much, or little, as necessary. If you only want to validate a few scalars, you won&apos;t be forced to specify anything converning a collection.</p><p>Speculoos aims to make composing specifications straightforward, and inspecting them transparent. A Speculoos specification is merely an arrangement of nested vectors, lists, maps, sequences, and sets that contain predicates. Those predicates are arranged in a pattern that instruct the validation functions where to apply the predicates. The specification for a vector is a vector. The specification for a map, is itself a map. There&apos;s a nearly one-to-one correspondence between the shape of the data and the shape of the specification. Speculoos specifications aim to be intuitive to peek at by eye, but also amenable to alteration. You can use your favorite Clojure data wrangling functions to tighten, relax, or remove portions of a Speculoos specification.</p><p>Speculoos provides flexibility, power, and reusability of specifications by ignoring datums that do not have a corresponding predicate in the specification and ignoring predicates that do not have a corresponding datum in the data. Maybe in your role in an assembly line, you only care about some slice of a large chunk of data. Supplying predicates for only a subset of datums allows you to only validate those specified datums while being agnostic towards the other datums. Going in the other direction, maybe somebody shared a giant specification that describes data about a person, their postal address, their contact info, etc. Because a Speculoos specification is just a data structure with regular predicates, you can, on-the-fly, pull out the portion relevent to postal addresses and apply that to your instances of address data. Speculoos lets you specify exactly what elements you&apos;d like to validate. No more, no less.</p></section><section id="mechanics"><h2>Mechanics</h2><p>Knowing a little bit about how Speculoos does its job will greatly help you understand how to use it. First, we need to know on how to address elements contained within a heterogeneous, arbitrarily-nested data structure. Speculoos follows the conventions set by <code>clojure.core/get-in</code>, and extends those conventions where necessary.</p><p>Vectors are addressed by zero-indexed integers.</p><pre><code>           [100 101 102 103]</code><br /><code>indexes --&gt; 0   1   2   3</code></pre><p>Same for lists…</p><pre><code>          &apos;(97 98 99 100)</code><br /><code>indexes --&gt; 0  1  2  3</code></pre><p>…and same for other sequences, like <code>range</code>.</p><pre><code>(range 29 33) ;; =&gt; (29 30 31 32)</code><br /><code>indexes -----------&gt; 0  1  2  3</code></pre><p>Maps are addressed by their keys, which are often keywords, like this.</p><pre><code>        {:a 1 :foo &quot;bar&quot; :hello &apos;world}</code><br /><code>keys --&gt; :a   :foo       :hello</code></pre><p>But maps may be keyed by <em>any</em> value, including integers…</p><pre><code>        {0 &quot;zero&quot; 1 &quot;one&quot; 99 &quot;ninety-nine&quot;}</code><br /><code>keys --&gt; 0        1       99</code></pre><p>…or some other scalars…</p><pre><code>        {&quot;a&quot; :value-at-str-key-a &apos;b :value-at-sym-key-b \c :value-at-char-key-c}</code><br /><code>keys --&gt; &quot;a&quot;                     &apos;b                     \c</code></pre><p>…even composite values.</p><pre><code>        {[0] :val-at-vec-0 [1 2 3] :val-at-vec-1-2-3 {} :val-at-empty-map}</code><br /><code>keys --&gt; [0]               [1 2 3]                   {}</code></pre><p>Set elements are addressed by their identities, so they are located at themselves.</p><pre><code>             #{42 :foo true 22/7}</code><br /><code>identities --&gt; 42 :foo true 22/7</code></pre><p>A <em>path</em> is a sequence of indexes, keys, or identities that allow us refer to a single element buried within a nested data structure. For each level of nesting, we add an element to the path sequence. <code>clojure.core/get-in</code> illustrates how this works.</p><pre><code>(get-in [100 101 102 103] [2]) ;; =&gt; 102</code></pre><p>For a vector containing only integers, each element is addressed by a path of length one. To locate integer <code>102</code>, the path is <code>[2]</code>. If we consider a vector nested within a vector…</p><pre><code>(get-in [100 101 [102 103]] [2]) ;; =&gt; [102 103]</code></pre><p>…that same path <code>[2]</code> now locates the nested vector. To navigate to an integer contained within the nested vector…</p><pre><code>(get-in [100 101 [102 103]] [2 0]) ;; =&gt; 102</code></pre><p>…requires a path of length two: <code>[2 0]</code> where the <code>2</code> addresses the nested vector <code>[102 103]</code> and the <code>0</code> addresses the <code>102</code> within the nested vector. If we have an integer contained within a vector, contained within a vector, contained within a vector, we&apos;d use a path of length three to get that integer.</p><pre><code>(get-in [100 [101 [102]]] [1]) ;; =&gt; [101 [102]]</code><br /><code>(get-in [100 [101 [102]]] [1 1]) ;; =&gt; [102]</code><br /><code>(get-in [100 [101 [102]]] [1 1 0]) ;; =&gt; 102</code></pre><p>The <code>102</code> is buried three levels deep, so we use a path with that many entries.</p><p>This system works similary for maps. Elements contained in un-nested collections are located with a path of length one.</p><pre><code>(get-in {:x 100, :y 101, :z 102} [:z]) ;; =&gt; 102</code></pre><p>In this example, <code>102</code> is located with a path composed of a single key, keyword <code>:z</code>. If we now consider a map nested within another map…</p><pre><code>(get-in {:x 100, :y 101, :z {:w 102}} [:z :w]) ;; =&gt; 102</code></pre><p>…we need a path with two elements: key <code>:z</code> navigates us to the nested <code>{:w 102}</code> map, and then key <code>:w</code> navigates us to the <code>102</code> within that nested map.</p><p>There&apos;s no restriction on what may be nested in what, so we can nest a map within a vector…</p><pre><code>(get-in [100 101 {:x 102}] [2 :x]) ;; =&gt; 102</code></pre><p>…or nest a vector within a map…</p><pre><code>(get-in {:x 100, :y {:z [101 102]}} [:y :z 1]) ;; =&gt; 102</code></pre><p>…or, if we use a <a href="https://github.com/blosavio/fn-in">modified version</a> of <code>clojure.core/get-in</code>, nest a vector within a map within a list.</p><pre><code>(require &apos;[fn-in.core :refer [get-in*]])</code><br /><br /><code>(get-in* &apos;(100 101 {:x [102]}) [2 :x 0]) ;; =&gt; 102</code></pre><p><code>102</code> is contained in three levels of nesting, so its path is comprised of three pieces.</p><p>Speculoos provides a little machine to wrangle paths for you. When supplied with a heterogeneous, arbitrarily-nested data structure, <code>speculoos.core/all-paths</code> returns a sequence of <code>{:path _ :value _ }</code> for every element, both scalars and collections.</p><pre><code>(require &apos;[speculoos.core :refer [all-paths]])</code><br /><br /><code>(all-paths [100 101 102])
;; =&gt; [{:path [], :value [100 101 102]}
;;     {:path [0], :value 100}
;;     {:path [1], :value 101}
;;     {:path [2], :value 102}]</code></pre><p>Note: we receive paths for four items, three integers, plus a path to the outer container itself. The root collection always has a path <code>[]</code>. The integer elements each have a path of a single, zero-indexed integer that locates them within the parent vector. Here&apos;s how it works with a map.</p><pre><code>(all-paths {:x 100, :y 101, :z 102})
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
;;     {:path [:z :w], :value 102}]</code></pre><p>Again, each of the integers has a path, and each of the maps has a path, for a total of five paths.</p><p>There is nothing special about integers. <code>all-paths</code> will treat any element, scalar or collection, the same way. <em>Every element has a path.</em> We could replace those integers with functions, un-nested in a vector…</p><pre><code>(all-paths [int? string? ratio?])
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
;;     {:path [:z :w], :value ratio?}]</code></pre><p>The important principle to remember is this: Every element, scalar and collection, of a heterogeneous, arbitrarily-nested data structure, can be assigned an unambiguous path, regardless of its container type.</p><p>If you ever find yourself with a nested list on your hands, <code>all-paths</code> has got you covered.</p><pre><code>(all-paths [42 (list &apos;foo &apos;bar &apos;baz)])
;; =&gt; [{:path [], :value [42 (foo bar baz)]}
;;     {:path [0], :value 42}
;;     {:path [1], :value (foo bar baz)}
;;     {:path [1 0], :value foo}
;;     {:path [1 1], :value bar}
;;     {:path [1 2], :value baz}]</code></pre><p>Likewise, sets are indispensible in some situations, so <code>all-paths</code> can handle it.</p><pre><code>(all-paths {:a 42, :b #{:chocolate :vanilla :strawberry}})
;; =&gt; [{:path [], :value {:a 42, :b #{:chocolate :strawberry :vanilla}}}
;;     {:path [:a], :value 42}
;;     {:path [:b], :value #{:chocolate :strawberry :vanilla}}
;;     {:path [:b :strawberry], :value :strawberry}
;;     {:path [:b :chocolate], :value :chocolate}
;;     {:path [:b :vanilla], :value :vanilla}]</code></pre><p>Admittedly, addressing elements in a set can be a little like herding cats, but it&apos;s still useful to have the capability. Wrangling sets merits its own <a href="#sets">dedicated section</a>.</p></section><section id="scalar-validation"><h2>Scalar Validation</h2><p>Let&apos;s return to the English-language specification we saw in the introduction: <em>A vector containing an integer, then a string, then a ratio</em>. Consider the paths of this vector…</p><pre><code>(all-paths [42 &quot;abc&quot; 22/7])
;; =&gt; [{:path [], :value [42 &quot;abc&quot; 22/7]}
;;     {:path [0], :value 42}
;;     {:path [1], :value &quot;abc&quot;}
;;     {:path [2], :value 22/7}]</code></pre><p>…and the paths of this vector…</p><pre><code>(all-paths [int? string? ratio?])
;; =&gt; [{:path [], :value [int? string? ratio?]}
;;     {:path [0], :value int?}
;;     {:path [1], :value string?}
;;     {:path [2], :value ratio?}]</code></pre><p>We see that elements of both share paths. If we keep only the paths to scalars, i.e., we discard the root collections at path <code>[]</code>, each has three elements remaining.<ul><li><code>42</code> and <code>int?</code> both at path <code>[0]</code>, in their respective vectors,</li><li><code>&quot;abc&quot;</code> and <code>string?</code> both at path <code>[1]</code>, and</li><li><code>22/7</code> and <code>ratio?</code> both at path <code>[2]</code>.</li></ul></p><p>Those pairs of scalars and predicates line up nicely, and we could evaluate each pair, in turn.</p><pre><code>(int? 42) ;; =&gt; true</code><br /><code>(string? &quot;abc&quot;) ;; =&gt; true</code><br /><code>(ratio? 22/7) ;; =&gt; true</code></pre><p>All three scalars satisfy their respective predicates that they&apos;re paired with. Speculoos provides a function, <code>validate-scalars</code> that substantially does all that work for us. Given data and a specification that share the data&apos;s shape (Mantra #2), <code>validate-scalars</code>:</p><ol><li>Runs <code>all-paths</code> on both the data, then the specification.</li><li>Removes the collection elements from each, keeping only the scalars in each.</li><li>Removes the scalars in data that lack a predicate at the same path in the specification, and removes the predicates in the specification that lack datums at the same path in the data.</li><li>For each remaining pair of scalar+predicate, applies the predicate to the scalar.</li></ol><p>Let&apos;s see that in action. We invoke <code>validate-scalars</code> with the data vector as the first argument and the specification vector as the second argument.</p><pre><code>(require &apos;[speculoos.core :refer [validate-scalars]])</code><br /><br /><code>(validate-scalars [42 &quot;abc&quot; 22/7]
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
;;      :valid? true}]</code></pre><p>Let&apos;s apply the Mantras to what we just did. Mantra #1: At the moment, we&apos;re validating scalars, as the <em>-scalars</em> suffix of the function name reminds us. The validation yielded only predicates applied to scalars; scalar validation ignored the collections. Mantra #2: The shape of our specification mimics the data. Because both are vectors, <code>validate-scalars</code> was able to properly apply each predicate its respective datum. Mantra #3: Every predicate was paired with a datum and <em>vice versa</em>, so validation did not ingore anything.</p><p><code>validate-scalars</code> returns a sequence of all the scalars in data that share a path with a predicate in the specfication. For each of those pairs, we receive a map containing the <code>:datum</code> scalar element of the data, the <code>:predicate</code> test function element of the specification, the <code>:path</code> addressing each in their respective structures, and the <code>valid?</code> result of applying the predicate function to the datum.</p><p>What if there&apos;s a length mis-match between the data and the specification? Mantra #3 tells us that validation ignores un-paired datums. Let&apos;s look at the <code>all-paths</code> for that situation.</p><pre><code>;; data vector containing an integer, a symbol, and a character</code><br /><code>(all-paths [42 &quot;abc&quot; 22/7])
;; =&gt; [{:path [], :value [42 &quot;abc&quot; 22/7]}
;;     {:path [0], :value 42}
;;     {:path [1], :value &quot;abc&quot;}
;;     {:path [2], :value 22/7}]</code><br /><br /><code>;; specification vector containing one predicate</code><br /><code>(all-paths [int?]) ;; =&gt; [{:path [], :value [int?]}
&nbsp;{:path [0], :value int?}]</code></pre><p>After discarding the root collections at path <code>[]</code> we find the only scalar+predicate pair at path <code>[0]</code>, and that&apos;s the only pair that <code>validate-scalars</code> looks at.</p><pre><code>(validate-scalars [42 &quot;abc&quot; 22/7]
&nbsp;                 [int?])
;; =&gt; [{:datum 42,
;;      :path [0],
;;      :predicate int?,
;;      :valid? true}]</code></pre><p>Only scalar <code>42</code> in the data vector has a corresponding predicate <code>int?</code> in the specfication vector, so the validation report contains only one entry. The second and third scalars, <code>&quot;abc&quot;</code> and <code>22/7</code>, are ignored.</p><p>What about the other way around? More predicates in the specification than scalars in the data?</p><pre><code>;; data vector containing one scalar, an integer</code><br /><code>(all-paths [42]) ;; =&gt; [{:path [], :value [42]} {:path [0], :value 42}]</code><br /><br /><code>;; specfication vector containing three predicates</code><br /><code>(all-paths [int? string? ratio?])
;; =&gt; [{:path [], :value [int? string? ratio?]}
;;     {:path [0], :value int?}
;;     {:path [1], :value string?}
;;     {:path [2], :value ratio?}]</code></pre><p>Mantra #3 reminds us that validation ignores un-paired predicates. Only the predicate <code>int?</code> at path <code>[0]</code> in the specification vector shares its path with a scalar in the data vector, so that&apos;s the only scalar+predicate pair that <code>validate-scalars</code> processes.</p><pre><code>(validate-scalars [42]
&nbsp;                 [int? string? ratio?])
;; =&gt; [{:datum 42,
;;      :path [0],
;;      :predicate int?,
;;      :valid? true}]</code></pre><p><code>validate-scalars</code> ignores both <code>string?</code> and <code>ratio?</code> within the specification vector because the data vector does not contain scalars at their respective paths.</p><p>Validating scalars contained within a map proceeds similarly. Let&apos;s send this map, our data, to <code>all-paths</code>.</p><pre><code>(all-paths {:x 42, :y &quot;abc&quot;, :z 22/7})
;; =&gt; [{:path [], :value {:x 42, :y &quot;abc&quot;, :z 22/7}}
;;     {:path [:x], :value 42}
;;     {:path [:y], :value &quot;abc&quot;}
;;     {:path [:z], :value 22/7}]</code></pre><p>Four elements: the root collection (a map), and three scalars. Then we&apos;ll do the same for this map, our specification, which mimics the shape of the data (Mantra #2), by also being a map with the same keys.</p><pre><code>(all-paths {:x int?, :y string?, :z ratio?})
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
;;      :valid? true}]</code></pre><p>…we can see that <ul><li><code>42</code> at path <code>[:x]</code> in the data satisfies <code>int?</code> at path <code>[:x]</code> in the specification, </li><li><code>&quot;abc&quot;</code> at path <code>[:y]</code> in the data satisfies <code>string?</code> at path <code>[:y]</code> in the specification, and</li><li><code>22/7</code> at path <code>[:z]</code> in the data satisfies <code>ratio?</code> at path <code>[:z]</code> in the specification. </li></ul>Because the specification mimics the shape of the data (i.e., the specifation is a map with the same keys), <code>validate-scalars</code> is able to infer how to apply each predicate to the intended datum.</p><p><code>validate-scalars</code> can only operate with complete scalar+predicate pairs. It ignores un-paired scalars and un-paired predicates. Since maps are not sequential, we can illustrate both scenarios with one example.</p><pre><code>;; data with keys :x and :q</code><br /><code>(all-paths {:x 42, :q &quot;foo&quot;})
;; =&gt; [{:path [], :value {:q &quot;foo&quot;, :x 42}}
;;     {:path [:x], :value 42}
;;     {:path [:q], :value &quot;foo&quot;}]</code><br /><br /><code>;; specification with keys :x and :s</code><br /><code>(all-paths {:x int?, :s decimal?})
;; =&gt; [{:path [], :value {:s decimal?, :x int?}}
;;     {:path [:x], :value int?}
;;     {:path [:s], :value decimal?}]</code></pre><p>Notice that the two maps contain only a single scalar/predicate that share a path, <code>[:x]</code>. The other two elements, scalar <code>&quot;foo&quot;</code> at path <code>[:q]</code> in the data map and predicate <code>decimal?</code> at path <code>[:s]</code> in the specification map, do not share a path with an element of the other. Those later two will be ignored.</p><pre><code>(validate-scalars {:x 42, :q &quot;foo&quot;}
&nbsp;                 {:x int?, :s decimal?})
;; =&gt; [{:datum 42,
;;      :path [:x],
;;      :predicate int?,
;;      :valid? true}]</code></pre><p><code>validate-scalars</code> found only a single complete scalar+predicate pair located at path <code>[:x]</code>, so it applied <code>int?</code> to <code>42</code>, which returns satisfied.</p><hr /><p>I am curious to know whether the discussion to this point is sufficient for Clojure programmers to get 80% of their specification and validation work done. 50%? </p><p>Onward…</p><hr /><p>Scalars contained in nested collections are treated accordingly: predicates from the specification are only applied to scalars in the data which share their path. Non-scalars are ignored. Here are the paths for a simple nested data vector with some scalars.</p><pre><code>(all-paths [42 [&quot;abc&quot; [22/7]]])
;; =&gt; [{:path [], :value [42 [&quot;abc&quot; [22/7]]]}
;;     {:path [0], :value 42}
;;     {:path [1], :value [&quot;abc&quot; [22/7]]}
;;     {:path [1 0], :value &quot;abc&quot;}
;;     {:path [1 1], :value [22/7]}
;;     {:path [1 1 0], :value 22/7}]</code></pre><p>Six total elements: three vectors, which <code>validate-scalars</code> will ignore, and three scalars. And here are the paths for a similarly-shaped nested specification.</p><pre><code>;;                         v --- char? predicate will be notable during validation in a moment</code><br /><code>(all-paths [int? [string? [char?]]])
;; =&gt; [{:path [], :value [int? [string? [char?]]]}
;;     {:path [0], :value int?}
;;     {:path [1], :value [string? [char?]]}
;;     {:path [1 0], :value string?}
;;     {:path [1 1], :value [char?]}
;;     {:path [1 1 0], :value char?}]</code></pre><p>Again, six total elements: three vectors that will be ignored, pluse three predicates. When we validate…</p><pre><code>(validate-scalars [42 [&quot;abc&quot; [22/7]]]
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
;;      :valid? false}]</code></pre><p>Three complete pairs of scalars and predicates.<ul><li><code>42</code> at path <code>[0]</code> in the data satisfies predicate <code>int?</code> at path <code>[0]</code> in the specification,</li><li><code>&quot;abc&quot;</code> at path <code>[1 0]</code> in the data satisfies predicate <code>string?</code> at path <code>[1 0]</code> in the specification,</li><li><code>22/7</code> at path <code>[1 1 0]</code> in the data <strong>does not satisfy</strong> predicate <code>char?</code> at path <code>[1 1 0]</code> in the specification.</li></ul><a href="#valid-and-thorough">Later</a>, we&apos;ll see that the lone unsatisfied <code>char?</code> predicate would result in <em>valid</em> operation return <code>false</code>.</p><p>When the data contains scalars that are not paired with predicates in the specification, they are not validated.</p><pre><code>(validate-scalars [42 [&quot;abc&quot; [22/7]]]
&nbsp;                 [int? [string?]])
;; =&gt; [{:datum 42,
;;      :path [0],
;;      :predicate int?,
;;      :valid? true}
;;     {:datum &quot;abc&quot;,
;;      :path [1 0],
;;      :predicate string?,
;;      :valid? true}]</code></pre><p>Only the <code>42</code> and <code>&quot;abc&quot;</code> are paired with predicates, so <code>validate-scalars</code> only validated those two scalars. Likewise…</p><pre><code>(validate-scalars [42]
&nbsp;                 [int? [string? [char?]]])
;; =&gt; [{:datum 42,
;;      :path [0],
;;      :predicate int?,
;;      :valid? true}]</code></pre><p>When the data contains only one scalar, but the specificaiton contains more predicates, <code>validate-scalars</code> only validates the complete scalar+predicate pairs.</p><p>Mis-matched, nested maps sing the same song. Here are the paths for all elements in a nested data map and a nested specification map.</p><pre><code>;; data</code><br /><code>(all-paths {:x 42, :y {:z 22/7}})
;; =&gt; [{:path [], :value {:x 42, :y {:z 22/7}}}
;;     {:path [:x], :value 42}
;;     {:path [:y], :value {:z 22/7}}
;;     {:path [:y :z], :value 22/7}]</code><br /><br /><code>;; specification</code><br /><code>(all-paths {:x &apos;int?, :y {:q &apos;string?}})
;; =&gt; [{:path [], :value {:x int?, :y {:q string?}}}
;;     {:path [:x], :value int?}
;;     {:path [:y], :value {:q string?}}
;;     {:path [:y :q], :value string?}]</code></pre><p>Notice that only the scalar <code>42</code> in the data and the predicate <code>int?</code>  in the specification share a path <code>[:x]</code>. <code>22/7</code> in the data and <code>string?</code> in the specification are un-paired.</p><pre><code>(validate-scalars {:x 42, :y {:z 22/7}}
&nbsp;                 {:x int?, :y {:q string?}})
;; =&gt; [{:datum 42,
;;      :path [:x],
;;      :predicate int?,
;;      :valid? true}]</code></pre><p><code>validate-scalars</code> dutifully applies the only scalar+predicate pair, and tells us that <code>42</code> is indeed an integer.</p><p>One final illustration: what happens if there are zero scalar+predicate pairs.</p><pred><code>(validate-scalars {:x 42} {:y int?}) ;; =&gt; []</code></pred><p>The only scalar at the path <code>[:x]</code> in the data does not share a path with the only predicate at path <code>[:y]</code> in the specification. No validations were performed.</p><p>A Speculoos scalar specification says <em>This data element may or may not exist, but if it does, it must satisfy this predicate.</em> See <a href="#valid-and-thorough"> this later section</a> for functions that return high-level <code>true/false</code> validation summaries and for functions that ensure validation of <em>every</em> scalar element.</p></section><section id="collection-validation"><h2>Collection Validation</h2><p>You may have been uncomfortably shifting in your chair while reading through the examples above. Every example we&apos;ve seen so far shows Speculoos validating individual scalars, such as integers, strings, booleans, etc.</p><pre><code>(valid-scalars? [99 &quot;qwz&quot; -88]
&nbsp;               [int? string? neg-int?])
;; =&gt; true</code></pre><p>However, you might need to specify some property of a collection itself, such as a vector&apos;s length, the presence of a key in a map, relationships <em>between</em> datums, etc.</p><p>One way to visualize the difference is this.</p><pre><code> v----v-------v------v---- scalar validation targets these things</code><br /><code>[42   \z {:x &apos;foo :y 22.7}]</code></pre><p>In contrast…</p><pre><code>v -------v---------------v-v---- collection validation targets these things</code><br /><code>[42   \z {:x &apos;foo :y 22.7} ]</code></pre><p>One of Speculoos&apos; main concepts is that scalars are specified and validated explicitly separately from collections. You perhaps noticed that the function name we have been using wasn&apos;t <code>validate</code> but instead <code>validate-scalars</code>. Speculoos provides a parallel group of functions to validate the properties of collections, independent of the scalar values they contain. Let&apos;s examine why and how they&apos;re separated.</p><p>Imagine we wanted to specify that our data vector was exactly three elements long. The paths of that data might look like this.</p><pre><code>(all-paths [42 &quot;abc&quot; 22/7])
;; =&gt; [{:path [], :value [42 &quot;abc&quot; 22/7]}
;;     {:path [0], :value 42}
;;     {:path [1], :value &quot;abc&quot;}
;;     {:path [2], :value 22/7}]</code></pre><p>Since we&apos;re now interested in specifying collections, we&apos;ll discard the <em>scalars</em> and focus only on the <em>collections</em>. In this case, there&apos;s only one collection, the vector at path <code>[]</code>, which signifies that it&apos;s the root collection.</p><p>We could try to write a specification with a bare predicate, like this.</p><pre><code>;; a predicate that returns true if the collection has three elements</code><br /><code>(def len-3? #(= 3 (count %)))</code></pre><p>Then we could imagine some function might do this.</p><pre><code>;; this fn doesn&apos;t actually exist</code><br /><code>(imaginary-validate-collection [42 &apos;foo \z] len-3?) ;; =&gt; true</code></pre><p>Okay, that scenario maybe kinda could work. But what about this scenario: <em>A three-element vector nested within a two-element vector</em>. The paths would look like this.</p><pre><code>(all-paths [11 [22 33 44]])
;; =&gt; [{:path [], :value [11 [22 33 44]]}
;;     {:path [0], :value 11}
;;     {:path [1], :value [22 33 44]}
;;     {:path [1 0], :value 22}
;;     {:path [1 1], :value 33}
;;     {:path [1 2], :value 44}]</code></pre><p>Oh. Still ignoring the scalars, there are now two vectors which would be targets for our predicate, one at the root, and one at path <code>[1]</code>. We can&apos;t merely supply a pair of bare predicates to our <code>imaginary-validate-collection</code> function and have it magically know how to apply the predicates to the correct vector.  It quickly becomes apparent that we need to somehow arrange our collection predicates inside some kind of structure that will instruct the validtion function where to apply the predicates. One of Speculoos&apos; principles is <em>Make the specification shaped like the data</em>. Let me propose this structure.</p><pre><code>[len-3? [len-3?]]</code></pre><p>What do the paths of that thing look like?</p><pre><code>(all-paths [&apos;len-3? [&apos;len-3?]])
;; =&gt; [{:path [], :value [len-3? [len-3?]]}
;;     {:path [0], :value len-3?}
;;     {:path [1], :value [len-3?]}
;;     {:path [1 0], :value len-3?}]</code><code> ;; using &apos;len-3? symbol as a stand-in because the function object renders as an ugly #function[...]</code></pre><p>Hmm. In the previous <a href="#scalar-validation">section</a>, when we were validating scalars, we followed the principle that validation only proceeds when a predicate in the specification shares the exact path as the scalar in the data. However, we can now see an issue if we try to apply that principle here. The nested vector of the data is located at path <code>[1]</code>. The nested predicate in the specification is located at path <code>[1 0]</code>, nearly same except for the trailing <code>0</code>. The root vector of the data is located at path <code>[]</code> while the predicate is located at path <code>[0]</code> of the specification, again, nearly the same except for the trailing zero. Clojure has a nice core function that performs that transformation.</p><pre><code>(drop-last [1 0]) ;; =&gt; (1)</code><br /><code>(drop-last [0]) ;; =&gt; ()</code></pre><p>The slightly modified rule for validating collections is <em>Collection predicates in the specification are applied to the collection in the data that correspond to their parent.</em> In other words, the predicate at path <code>pth</code> in the collection specification is applied to the collection at path <code>(drop-last pth)</code> in the data.</p><p>The modified algorithm for validating collections is as follows:<ol><li>Run <code>all-paths</code> on both the data, then the specification.</li><li>Remove <em>scalar</em> elements from the data, keeping only the collection elements.</li><li>Remove <em>non-predicate</em> elements from the collection specification.</li><li>Pair predicates at path <code>pth</code> in the spefication with collections at path <code>(drop-last pth)</code> in the data. Discard all other un-paired collections and un-paired predicates.</li><li>For each remaining collection+predicate pair, apply the predicate to the collection.</li></ol></p><p>Speculoos provides a function, <code>validate-collections</code>, that does that for us. Let&apos;s see.</p><pre><code>(require &apos;[speculoos.core :refer [validate-collections]])</code><br /><br /><code>(validate-collections [11 [22 33 44]]
&nbsp;                     [len-3? [len-3?]])
;; =&gt; ({:datum [11 [22 33 44]],
;;      :ordinal-path-datum [],
;;      :path-datum [],
;;      :path-predicate [0],
;;      :predicate #function [speculoos-project-readme-generator/len-3?],
;;      :valid? false}
;;     {:datum [22 33 44],
;;      :ordinal-path-datum [0],
;;      :path-datum [1],
;;      :path-predicate [1 0],
;;      :predicate #function [speculoos-project-readme-generator/len-3?],
;;      :valid? true})</code></pre><p>Much of that looks familiar. <code>validate-collections</code> returns a validation entry for each to the two collections+predicate pairs. The <code>:datum</code>s represent the things being tested and the <code>:predicate</code>s report the predicate functions, and similarly <code>valid?</code> reports whether that predicate was satisfied. There are now three things that involve the concept of a path: the predicate was found at <code>:path-predicate</code> in the specification and the datum was found at <code>:ordinal-path-datum</code> in the data, which is also presented in a more friendly format as the literal path <code>:path-datum</code>. (We&apos;ll explain the terms embodied by these keywords as the discussion progresses.) In this example, the nested vector contains three elements, so its predicate was satisfied, while the root vector contains only two elements, and thus failed to satisfy its predicate.</p><p>Let&apos;s take a look at validating nested maps. Here are the paths of some example data.</p><pre><code>(all-paths {:x 11, :y {:z 22}})
;; =&gt; [{:path [], :value {:x 11, :y {:z 22}}}
;;     {:path [:x], :value 11}
;;     {:path [:y], :value {:z 22}}
;;     {:path [:y :z], :value 22}]</code></pre><p>Two scalars, which <code>validate-collections</code> ignores, and two collections. Let&apos;s apply our rule: the predicate in the specfication applies to the collection in the data whose path is one element shorter. The two collections are located at paths <code>[]</code> and <code>[:y]</code>. To write a collection specification, we&apos;d mimic the shape of the data, inserting predicates that apply to the parent. We can&apos;t simply write</p><pre><code>{map? {map?}}</code></pre><p>because maps must contain an even number of forms. Technically, you could key your collection predicates however you want, but I strongly recommend chosing a key that doesn&apos;t appear in the data. This example shows why. We could put a predicate at key <code>:y</code> of the specification, and <code>validate-collections</code> will merrily chug along.</p><pre><code>(validate-collections {:x 11, :y {:z 22}}
&nbsp;                     {:y map?})
;; =&gt; ({:datum {:x 11, :y {:z 22}},
;;      :ordinal-path-datum [],
;;      :path-datum [],
;;      :path-predicate [:y],
;;      :predicate map?,
;;      :valid? true})</code></pre><p>We can see that the singular <code>map?</code> predicate located at specificition path <code>[:y]</code> was indeed applied to the root container at data path <code>(drop-last [:y])</code> which evaluates to path <code>[]</code>. But now we&apos;ve consumed that key, and it cannot be used to target the nested map <code>{:z 22}</code> at <code>[:y]</code> in the data. If we had instead invented a synthetic key, <code>drop-last</code> would trim it off the right end and the predicate would still be applied to the root container, while key <code>:y</code> remains available to target the nested map. In practice, I like to invent keys that are descriptive of the predicate so the validtion results are easier to scan.</p><pre><code>(validate-collections {:x 11, :y {:z 22}}
&nbsp;                     {:is-a-map? map?, :y {:is-a-set? set?}})
;; =&gt; ({:datum {:x 11, :y {:z 22}},
;;      :ordinal-path-datum [],
;;      :path-datum [],
;;      :path-predicate [:is-a-map?],
;;      :predicate map?,
;;      :valid? true}
;;     {:datum {:z 22},
;;      :ordinal-path-datum [:y],
;;      :path-datum [:y],
;;      :path-predicate [:y :is-a-set?],
;;      :predicate set?,
;;      :valid? false})</code></pre><p>Notice that <code>validate-collections</code> completely ignored the scalars <code>11</code> and <code>22</code> at data keys <code>:x</code> and <code>:z</code>. It only applied predicate <code>map?</code> to the root of data and predciate <code>set?</code> to the nested map at key <code>:y</code>, which failed to satisfy. Let me emphasize: within a collection specification, the name of the predicate keys targeting a nested map have <em>absolutely no bearing on the operation of the validation</em>; they get truncated by the <code>drop-last</code> operation. We could have used something misleading like this.</p><pre><code>;;             this keyword… ---v         v--- …gives the wrong impression about this predicate</code><br /><code>(validate-collections {:x 11} {:is-a-map? vector?})
;; =&gt; ({:datum {:x 11},
;;      :ordinal-path-datum [],
;;      :path-datum [],
;;      :path-predicate [:is-a-map?],
;;      :predicate vector?,
;;      :valid? false})</code></pre><p>Despite the key suggesting that we&apos;re testing for a map, the actual predicate tests for a vector, and returns <code>false</code>.</p><p>Here&apos;s something interesting.</p><pre><code>(validate-collections [42]
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
;;      :valid? false})</code></pre><p>If we focus on the paths of the two predicates in the specification, we see that both <code>vector?</code> and <code>map?</code> target the root container because <code>(drop-last [0])</code> and <code>(drop-last [1])</code> both evaluate to the same path in the data. So we have another consideration: <em>Every</em> predicate in a specification&apos;s collection applies to the parent collection in the data. This means that we can apply an unlimited number of predicates to each collection.</p><pre><code>(validate-collections [42]
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
;;      :valid? true})</code></pre><p>If <strong>any</strong> number of predicates apply to the parent collection, there might be zero to infinity predicates before we encounter a nested collection in that sequence. How does <code>validate-collections</code> then determine where to apply the predicate inside a nested collection?</p><p>The rule <code>validate-collections</code> follows is <em>Apply nested collection predicates in the order which they appear, ignoring scalars.</em> Let&apos;s see that in action. First, we&apos;ll make some example data composed of a parent vector, containing a nested map, list, and set, with a couple of interleaved integers.</p><pre><code>[{:a 11} 22 (list 33) 44 #{55}]</code></pre><p>Now we need to compose a collection specification. Mantra #2 reminds us to make the specification mimic the shape of the data. I&apos;m going to copy-paste the data and mash the delete key to remove the scalar datums.</p><pre><code>[{} () #{}]</code></pre><p>Just to emphasize how they align, here are the data (top) and the collection specification (bottom) with some spaced formatting.</p><pre><code>[{:a 11} 22 (list 33) 44 #{55}] ;; &lt;--- data</code><br /><code>[{     }    (       )    #{  }] ;; &lt;--- collection specification</code><br /><code> ^--- 1st   ^--- 2nd     ^--- 3rd collection</code></pre><p>The first thing to note is that, that our collection specification looks a lot like our data with all the scalars removed. The second thing to notice is that even though it contains zero predicates, that&apos;s a legitimate collection specification which <code>validate-collections</code> can consume.</p><pre><code>(validate-collections [{:a 11} 22 (list 33) 44 #{55}] [{} () #{}]) ;; =&gt; ()</code></pre><p>Validation ignores collections in the data that are not paried with a predicate in the specification.</p><p>Okay, let&apos;s add a predicate. Let&apos;s specify that the second nested collection is a list. Predicates apply to their container, so we&apos;ll insert <code>list?</code> into the corresponding collection.</p><pre><code>(validate-collections [{:a 11} 22 (list 33) 44 #{55}]
&nbsp;                     [{} (list list?) #{}])
;; =&gt; ({:datum (33),
;;      :ordinal-path-datum [1],
;;      :path-datum [2],
;;      :path-predicate [1 0],
;;      :predicate list?,
;;      :valid? true})</code></pre><p>One predicate in the specification pairs with one collection in the data, so we receive one validation result. The <code>list?</code> predicate at path <code>[1 0]</code> in the specification was applied to the collection located at path <code>[2]</code> in the data. That nested collection is indeed a list, so <code>:valid?</code> is <code>true</code>.</p><p> Notice how <code>valiate-collections</code> did some tedious and boring calculations to achieve the general effect of <em>The predicate in the second collection of the specification applies to the second collection of the data.</em> It kinda skipped over that <code>22</code> because it ignores scalars, and we&apos;re validating collections.</p><p>Let&apos;s clear the slate and specify that nested set at the end.</p><pre><code>(validate-collections [{:a 11} 22 (list 33) 44 #{55}]
&nbsp;                     [{} () #{set?}])
;; =&gt; ({:datum #{55},
;;      :ordinal-path-datum [2],
;;      :path-datum [4],
;;      :path-predicate [2 set?],
;;      :predicate set?,
;;      :valid? true})</code></pre><p>One predicate applied to one collection, one validation result. And again, collection validation skipped right over the intervening scalars <code>22</code> and <code>44</code> in the data. <code>validate-collections</code> applied the predicate in the specification&apos;s third collection to the data&apos;s third collection.</p><p>We might as well specify that nested map. Recall that collection predicates targeting a map require a sham key. Removing the previous predicate, we&apos;ll insert a <code>map?</code> predicate at a key that doesn&apos;t appear in the data.</p><pre><code>(validate-collections [{:a 11} 22 (list 33) 44 #{55}]
&nbsp;                     [{:is-map? map?} () #{}])
;; =&gt; ({:datum {:a 11},
;;      :ordinal-path-datum [0],
;;      :path-datum [0],
;;      :path-predicate [0 :is-map?],
;;      :predicate map?,
;;      :valid? true})</code></pre><p>Unlike the previous two validations, <code>validate-collections</code> didn&apos;t have to skip over any scalars. It merely applied the predicate in the specification&apos;s first collection to the data&apos;s first collection, which is indeed a map.</p><p>We&apos;ve now seen how to specify and validate each of those three nested collections, so for completeness&apos; sake, let&apos;s specify the root. Predicates apply to their container, so we&apos;ll insert it at the beginning.</p><pre><code>(validate-collections [{:a 11} 22 (list 33) 44 #{55}]
&nbsp;                     [vector? {} () #{}])
;; =&gt; ({:datum [{:a 11} 22 (33) 44 #{55}],
;;      :ordinal-path-datum [],
;;      :path-datum [],
;;      :path-predicate [0],
;;      :predicate vector?,
;;      :valid? true})</code></pre><p>Technically, we could put that particular predicate anywhere in the top-level vector as long <code>(drop-last path)</code> evaluates to <code>[]</code>. All the following yield substantially the same results.</p><pre><code>(validate-collections [{:a 11} 22 (list 33) 44 #{55}] [vector? {} () #{}])</code><br /><code>(validate-collections [{:a 11} 22 (list 33) 44 #{55}] [{} vector? () #{}])</code><br /><code>(validate-collections [{:a 11} 22 (list 33) 44 #{55}] [{} () vector? #{}])</code><br /><code>(validate-collections [{:a 11} 22 (list 33) 44 #{55}] [{} () #{} vector?])</code></pre><p>In practice, I find it visually clearer to insert the predicates at the front.</p><p>Let&apos;s do one final all-up demonstration.</p><pre><code>(validate-collections [{:a 11} 22 (list 33) 44 #{55}]
&nbsp;                     [vector? {:is-map? map?} sequential? (list list?) coll? #{set?} any?])
;; =&gt; ({:datum [{:a 11} 22 (33) 44 #{55}],
;;      :ordinal-path-datum [],
;;      :path-datum [],
;;      :path-predicate [0],
;;      :predicate vector?,
;;      :valid? true}
;;     {:datum {:a 11},
;;      :ordinal-path-datum [0],
;;      :path-datum [0],
;;      :path-predicate [1 :is-map?],
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
;;      :valid? true})</code></pre><p><code>validate-collections</code> applied to the data&apos;s root four predicates <code>vector?</code>, <code>sequential?</code>, <code>coll?</code>, and <code>any?</code>, which we interleaved among the nested collections. In addition, it validated each of the three nested collections, skipping over the intervening scalars.</p><p>Collections nested within a map do not involve that kind of skipping because they&apos;re not sequential. Here&apos;s an example. Let&apos;s make this our data.</p><pre><code>{:a [99] :b (list 77)}</code></pre><p>Now, we copy-paste the data, then delete the scalars.</p><pre><code>{:a [  ] :b (list   )}</code></pre><p>That becomes the template for our collection specification. Let&apos;s pretend we want to specify something about those two nested collections at keys <code>:a</code> and <code>:b</code>. We stuff the predicates <em>directly inside those collections</em>.</p><pre><code>{:a [vector?] :b (list list?)}</code></pre><p>This becomes our collection specification. Let&apos;s see what happens.</p><pre><code>(validate-collections {:a [99], :b (list 77)}
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
;;      :valid? true})</code></pre><p>Checklist time: Specification shape mimics data? Check. Validating collections, ignoring scalars? Check. Two paired predciates, two validations? Check. There&apos;s a subtlety to pay attention to: the <code>vector?</code> and <code>list?</code> predicates are contained within a vector and list, respectively. Those two predicates apply to their <em>immediate</em> parent container. <code>validate-collections</code> needs those <code>:a</code> and <code>:b</code> keys to find that vector and that list. You only use a sham key when validating the map immediately above your head. Let&apos;s re-use that validation and tack on a sham key with a predicate aimed at the root map.</p><pre><code>(validate-collections {:a [99], :b (list 77)}
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
;;      :valid? true})</code></pre><p>We&apos;ve got the vector and list validations as before, and then at the end we see that <code>map?</code> was applied to the root. One more example to illustrate this point. Again, here&apos;s our data.</p><pre><code>{:a [99] :b (list 77)}</code></pre><p>And again, we&apos;ll copy-paste the data, then delete the scalars. That&apos;ll be our template for our collection specification.</p><pre><code>{:a [  ] :b (list   )}</code></pre><p>Now, we&apos;ll go even further and delete the <code>:b</code>  key and its value.</p><pre><code>{:a [  ]             }</code><br /><br /><code>;; without :b, won&apos;t be able to validate the list</code></pre><p>Insert old reliable <code>vector?</code>. That predicate is paired with its immediate parent vector, so we need to keep the <code>:a</code> key.</p><pre><code>{:a [vector?]        }</code></pre><p>Finally, we&apos;ll add in a wholly different key, with a <code>coll?</code> predicate nested in a collection at the new key.</p><pre><code>{:a [vector?] :flamingo [coll?]}</code></pre><p>Test yourself: How many validations will occur?</p><pre><code>(validate-collections {:a [99], :b (list 77)}
&nbsp;                     {:a [vector?], :flamingo [coll?]})
;; =&gt; ({:datum [99],
;;      :ordinal-path-datum [:a],
;;      :path-datum [:a],
;;      :path-predicate [:a 0],
;;      :predicate vector?,
;;      :valid? true})</code></pre><p>In this example, there is only one predciate+collection pair. <code>vector?</code> applies to the vector at <code>:a</code>. You might have expected <code>coll?</code> to be applied somewhere because <code>:flamingo</code> doesn&apos;t appear in the map, but notice that <code>coll?</code> is <em>contained</em> in a vector. It would only ever apply to the thing that contained it. Since the data&apos;s root doesn&apos;t contain a collection at that key, the predicate is unpaired, and thus ignored. If we wanted to apply <code>coll?</code> to the root, we shed its immediate container.</p><pre><code>(validate-collections {:a [99], :b (list 77)}
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
;;      :valid? true})</code></pre><p><em>Now, </em><code>coll?</code>&apos;s immediate continer is the root. Since it is now properly paired with a collection, it participates in validation. </p></section><section id="valid-thorough"><h2>Validation Summaries and Thorough Validations</h2><p>Up until now, we&apos;ve been using <code>validate-scalars</code> and <code>validate-collections</code>, because they&apos;re verbose. For teaching and learning purposes (and for diagnosing problems), it&apos;s useful to see all the information considered by the validators. However, in production, once you&apos;ve got your specification shape nailed down, you&apos;ll want a cleaner <em>yes</em> or <em>no</em> answer on whether the data satisfied the specification. You could certainly pull out the non-truthy, invalid results yourself…</p><pre><code>(filter #(not (:valid? %))
&nbsp; (validate-scalars [42 &quot;abc&quot; 22/7]
&nbsp;                   [int? symbol? ratio?]))
;; =&gt; ({:datum &quot;abc&quot;,
;;      :path [1],
;;      :predicate symbol?,
;;      :valid? false})</code></pre><p>…and then check for invalids yourself…</p><pre><code>(empty? \*1) ;; =&gt; false</code></pre><p>…but Speculoos provides a function that does exactly that, both for scalars…</p><pre><code>(require &apos;[speculoos.core :refer [valid-scalars? valid-collections?]])</code><br /><br /><code>(valid-scalars? [42 &quot;abc&quot; 22/7]
&nbsp;               [int? symbol? ratio?])
;; =&gt; false</code></pre><p>…and for collections.</p><pre><code>(valid-collections? [42 [&quot;abc&quot;]]
&nbsp;                   [vector? [vector?]])
;; =&gt; true</code></pre><p>Whereas the <code>validate-…</code> functions return a detailed validation report of every predicate+datum pair they see, the <code>valid-…?</code> variants provide a plain <code>true/false</code>.</p><p>Beware: Validation only considers paired predicates+datums (Mantra #3). If your datum doesn&apos;t have a paired predicate, then it won&apos;t be validated. Observe.</p><pre><code>(valid-scalars? {:a 42}
&nbsp;               {:b string?}) ;; =&gt; true</code><br /><br /><code>(validate-scalars {:a 42}
&nbsp;                 {:b string?}) ;; =&gt; []</code></pre><p><code>42</code> does not share a path with <code>string?</code>, the lone predicate in the specification. Since there are zero invalid results, <code>valid-scalars?</code> returns <code>true</code>.</p><p><strong>» Within the Speculoos library, <code>valid?</code> means <em> zero invalids. «</em></strong></p><h3>Combo validation</h3><p>Validating scalars separately from validating collections is a core principle embodied by the Speculoos library. I believe that separating the two into distinct processes carries solid advantages because the specifications are more straightforward, the mental model is clearer, the implementation code is simpler, and it makes validation <em>à la carte</em>. Much of the time, you can probably get away with just a scalar spefication.</p><p>All that said, it is not possible to specify and validate every aspect of your data with only scalar validation or only collection validation. When you really need to be strict and validate both scalars and collections, you could manually combine like this.</p><pre><code>(and (valid-scalars? [42] [int?])
&nbsp;    (valid-collections? [42] [vector?]))
;; =&gt; true</code></pre><p>Speculoos provides a pre-made utility that does exactly that. You supply some data, then a scalar specification, then a collection specification.</p><pre><code>(require &apos;[speculoos.core :refer [valid? validate]])</code><br /><br /><code>(valid? [42]
&nbsp;       [int?]
&nbsp;       [vector?]) ;; =&gt; true</code></pre><p>Let me emphasize what <code>valid?</code> is doing here, because it is <em>not</em> violating the first Mantra about separately validating scalars and collectios. First, <code>valid?</code> performs a scalar validation on the data, and puts that result on the shelf. Then, in a completely distinct operation, it performs a collection validation. <code>valid?</code> then pulls the scalar validation results off the shelf and combines it with the collection validation results, and returns a singular <code>true/false</code>.  (Look back at the first example of this sub-section to see the separation.)</p><p>I reserved the shortest, most mnemonic function name, <code>valid?</code>, to signal how important it is to separate scalar and collection validation.</p><p>Speculoos also provides a variant that returns detailed validation results after performing distinct scalar validation and collection validation.</p><pre><code>(validate [42 &quot;abc&quot; 22/7]
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
;;      :valid? true})</code></pre><p><code>validate</code> gives you the exact results as if we had run <code>validate-scalars</code> and then immediately thereafter <code>validate-collections</code>. <code>validate</code> merely gives us the convenience of quickly running both in succession without having to re-type the data. With one invocation, we can validate <em>all</em> aspects of our data, both scalars and collections, and we never violated Matra #1.</p><h3>Thorough validation</h3><p>Here are the general patterns regarding the function names.<ul><li><strong><code>validate-…</code></strong> functions return a detailed report for every datum+predicate pair.</li><li><strong><code>valid-…?</code></strong> functions return <code>true</code> if the predicate+datum pairs produce zero falsey results, <code>false</code> otherwise.</li><li><strong><code>…-scalars</code></strong> functions consider only non-collection datums.</li><li><strong><code>…-collections</code></strong> functions consider only non-scalar datums.</li><li><strong><code>thoroughly-…</code></strong> functions return <code>true</code> only if every element (scalar or collection, as the case may be) is paired with a predicate, and every element satisfies its predicate.</li></ul>&apos;Plain&apos; functions (i.e., <code>validate</code>, <code>valid?</code>, and <code>thoroughly-valid?</code>) perform a scalar validation, followed by performing a distict collection validation, and returns a single comprehensive response that merges the results of both.</p><p>Here&apos;s how those terms are put together, and what they do.</p><table><tr><th>function</th><th>checks…</th><th>returns…</th><th>note</th></tr><tr><td><code>validate-scalars</code></td><td>scalars only</td><td>detailed validation report</td><td></td></tr><tr><td><code>valid-scalars?</code></td><td>scalars only</td><td><code>true/false</code></td><td></td></tr><tr><td><code>thoroughly-valid-scalars?</code></td><td>scalars only</td><td><code>true/false</code></td><td>only <code>true</code> if all scalars paired with a predicate</td></tr><tr><td><code>validate-collections</code></td><td>collections only</td><td>detailed validation report</td><td></td></tr><tr><td><code>valid-collections?</code></td><td>collections only</td><td><code>true/false</code></td><td></td></tr><tr><td><code>thoroughly-valid-collections?</code></td><td>collections only</td><td><code>true/false</code></td><td>only <code>true</code> if all collections paired with a predicate</td></tr><tr><td><code>validate</code></td><td>scalars, then collections, separately</td><td>detailed validation report</td><td></td></tr><tr><td><code>valid?</code></td><td>scalars, then collections, separately</td><td><code>true/false</code></td><td></td></tr><tr><td><code>thoroughly-valid?</code></td><td>scalars, then collections separately</td><td><code>true/false</code></td><td>only <code>true</code> if all datums paired with a predicate</td></tr></table></section><section id="function-validation"><h2>Specifying and Validating Functions</h2><p>Being able to validate Clojure data enables us to check the usage and behavior of functions.</p><ol><li><strong>Validating arguments</strong> Speculoos can validate any property of the arguments passed to a function when it is invoked. We can ask questions like <em>Is the argument passed to the function a number?</em>, a scalar validation, and <em>Are there an even number of arguments?</em>, a collection validation.</li><li><strong>Validating return values</strong> Speculoos can validate any property of the value returned by a function. We can ask questions like <em>Does the function return a four-character string?</em>, a scalar validation, and <em>Does the function return a map containing keys <code>:x</code> and <code>:y</code></em>, a collection validation.</li><li><strong>Validating function correctness</strong> Speculoos can check the validate the correctness of a function in two ways.<ul><li>Speculoos can validate the <em>relationships</em> between the arguments and the function&apos;s return value. We can ask questions like <em>Is each of the three integers in the return value larger than the three integers in the arguments?</em>, a scalar validation, and <em>Is the return sequence the same length as the argument sequence, and are all the elements in reverse order?</em>, a collection validation.</li><li>Speculoos can <em>exercise</em> a function. This allows us to check <em>If we give this function one thousand randomly-generated valid inputs, does the function always produce a valid return value?</em> Exercising functions with randomly-generated samples is described in the </li><a href="#exercising">next section</a>.</ul></li></ol><p>None of those six checks are strictly required. Speculoos will happily validate using only what specifications we provide.</p><h3 id="fn-args">1. Validating Function Arguments</h3><p>When we invoke a function with a series of arguments, that series of values forms a sequence, which Speculoos can validate like any other heterogeneous, arbitrarily-nested data structure. Speculoos offers <a href="#explicit">a trio</a> of function-validating functions with differing levels of explicitness. We&apos;ll be primarily using <code>validate-fn-with</code> because it is the most explicit of the trio, and we can most easily observe what&apos;s going on.</p><p>Let&apos;s pretend we want to validate the arguments to a function <code>sum-three</code> that expects three integers and returns their sum.</p><pre><code>(require &apos;[speculoos.function-specs :refer [validate-fn-with]])</code><br /><br /><code>(defn sum-three [x y z] (+ x y z))</code><br /><br /><code>(sum-three 1 20 300) ;; =&gt; 321</code></pre><p>The argument list is a <em>sequence</em> of values, in this example, a sequential thing of three integers. We can imagine a scalar specification for just such as sequence.</p><pre><code>[int? int? int?]</code></pre><p>When using <code>validate-fn-with</code>, we supply the function name, a map of specifications, and some trailing <code>&amp;-args</code>. Speculoos can validate six aspects of a function using up to six specifications, each specification associated in that map to a particular key. We&apos;ll cover each of those six aspects in turn. Right now, we want to specify the <em>argument scalars</em>.</p><p>Instead of individually passing each of those six specifications to <code>validate-fn-with</code> and putting <code>nil</code> placeholders where don&apos;t wish to supply a specification, we organize the specifications. To do so, we associate the arguments&apos; scalar specification to the qualified key <code>:speculoos/arg-scalar-spec</code>.</p><pre><code>{:speculoos/arg-scalar-spec [int? int? int?]}</code></pre><p>Then, we validate the arguments to <code>sum-three</code> like this.</p><pre><code>(validate-fn-with sum-three
&nbsp;                 {:speculoos/arg-scalar-spec [int? int? int?]}
&nbsp;                 1
&nbsp;                 20
&nbsp;                 300)
;; =&gt; 321</code></pre><p>The arguments conformed to the specification, so <code>validate-fn-with</code> returns the value produced by <code>sum-three</code>. Let&apos;s intentionally invoke <code>sum-three</code> with one invalid argument by swapping integer <code>1</code> with a floating-point <code>1.0</code>.</p><pre><code>(validate-fn-with sum-three
&nbsp;                 {:speculoos/arg-scalar-spec [int? int? int?]}
&nbsp;                 1.0
&nbsp;                 20
&nbsp;                 300)
;; =&gt; ({:datum 1.0,
;;      :fn-spec-type :speculoos/argument,
;;      :path [0],
;;      :predicate int?,
;;      :valid? false})</code></pre><p>Hey, that kinda looks familiar. It looks a lot like something <code>validate-scalars</code> would emit if we filtered to keep only the invalids. We see that <code>1.0</code> at path <code>[0]</code> failed to satisfy its <code>int?</code> scalar predicate. We can also see that the function specification type is <code>:speculoos/argument</code>. Since Speculoos can validate scalars and collections of both arguments and collections, that key-val is a little signpost to help us pinpoint exactly what and where. Let&apos;s invoke <code>sum-three</code> with a second invalid argument, a ratio <code>22/7</code> instead of integer <code>300</code>.</p><pre><code>(validate-fn-with sum-three
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
;;      :valid? false})</code></pre><p>In addition to the invalid <code>1.0</code>, we see that <code>22/7</code> at path <code>[2]</code> also fails to satisfy its <code>int?</code> scalar predicate. The scalar predicate&apos;s path in the scalar specification is the same as the path of the <code>22/7</code> in the <code>[1.0 20 22/7]</code> sequence of arguments. Basically, <code>validate-fn-with</code> is doing something like this…</p><pre><code>(speculoos.core/only-invalid
&nbsp; (validate-scalars [1.0 20 22/7]
&nbsp;                   [int? int? int?]))
;; =&gt; ({:datum 1.0,
;;      :path [0],
;;      :predicate int?,
;;      :valid? false}
;;     {:datum 22/7,
;;      :path [2],
;;      :predicate int?,
;;      :valid? false})</code></pre><p>…validating scalars with <code>validate-scalars</code> and keeping only the invalids.</p><p>Okay, we see that term <em>scalar</em> buzzing around, so there must be something else about validating collections. Yup. We can also validate collection properties of the argument sequence. Let&apos;s specify that the argument sequence must contain three elements with a custom collection predicate.</p><pre><code>(defn count-3? [v] (= 3 (count v)))</code></pre><p>Let&apos;s simulate the collection validation first. Remember, collection predicates are applied to their parent containers, so <code>count-3?</code> must appear within a collection so that it&apos;ll be paired with the data&apos;s containing collection.</p><pre><code>(validate-collections [1 20 30]
&nbsp;                     [count-3?])
;; =&gt; ({:datum [1 20 30],
;;      :ordinal-path-datum [],
;;      :path-datum [],
;;      :path-predicate [0],
;;      :predicate #function [speculoos-project-readme-generator/count-3?],
;;      :valid? true})</code></pre><p>That result fits with <a href="#validating-collections">our discussion</a> about validating collections. Let&apos;s remember: scalars and collections are <em>always</em> validated separately. <code>validate</code> is merely a convenience function that does both scalar a collection validation, in discrete processes, with a single function invocation.</p><p>Next, we&apos;ll associate that collection specification into our function specification map at <code>:speculoos/arg-collection-spec</code> and invoke <code>validate-fn-with</code> with three valid arguments.</p><pre><code>(validate-fn-with sum-three
&nbsp;                 {:speculoos/arg-collection-spec [count-3?]}
&nbsp;                 1
&nbsp;                 20
&nbsp;                 300)
;; =&gt; 321</code></pre><p>The argument sequence satisfies our collection specification, so <code>sum-three</code> returns the expected value. Now let&apos;s repeat, but with an additional argument that causes the argument list to violate its collection predciate.</p><pre><code>(validate-fn-with sum-three
&nbsp;                 {:speculoos/arg-collection-spec [count-3?]}
&nbsp;                 1 20
&nbsp;                 300 4000)
;; =&gt; ({:datum [1 20 300 4000],
;;      :fn-spec-type :speculoos/argument,
;;      :ordinal-path-datum [],
;;      :path-datum [],
;;      :path-predicate [0],
;;      :predicate #function [speculoos-project-readme-generator/count-3?],
;;      :valid? false})</code></pre><p>Our argument list <code>[1 20 300 4000]</code> failed to satisfy our <code>count-3?</code> collection predicate, so <code>validate-fn-with</code> emitted a validation report.</p><p>Note: Don&apos;t specify and validate the <em>type</em> of the argument container, i.e., <code>vector?</code>. That&apos;s an implementation detail and not guaranteed.</p><p>Let&apos;s get fancy and combine an argument scalar specification and an argument collection specification. Outside of the context of checking a function, that <a href="#valid-thorough">combo validation</a> would look like this.</p><pre><code>(speculoos.core/only-invalid
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
;;      :predicate
;;        #function
;;         [speculoos-project-readme-generator/count-3?],
;;      :valid? false})</code></pre><p>Each of the first three scalars that paired with a scalar predicate were validated as scalars. The first and third scalars failed to satisfy their respective predicates. The fourth argument, <code>4000</code>, was not paired with a scalar predicate and was therefore ignored. Then, the argument sequence as a whole was validated against the collection predicate <code>count-3?</code>.</p><p><code>validate-fn-with</code> performs substantially that combo validation. We&apos;ll associate the <strong>arg</strong>ument <strong>scalar</strong> <strong>spec</strong>ification with <code>:speculoos/arg-scalar-spec</code> and the <strong>arg</strong>ument <strong>collection</strong> <strong>spec</strong>fication with <code>:speculoos/arg-collection-spec</code> and pass the invalid argument sequence.</p><pre><code>(validate-fn-with sum-three
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
;;      :predicate #function [speculoos-project-readme-generator/count-3?],
;;      :valid? false})</code></pre><p>Just as in the <code>validate</code> simulation, we see three items fail to satisfy their predicates. Scalars <code>1.0</code> and <code>22/7</code> are not integers, and the argument sequence as a whole, <code>[1.0 20 22/7 4000]</code>, does not contain exactly three elements as its collection predicate requires.</p><h3 id="fn-ret">2. Validating Function Return Values</h3><p>Speculoos can also validate values returned by a function. Reusing our <code>sum-three</code> function, and going back to valid inputs, we can associate a <strong>ret</strong>urn <strong>scalar</strong> <strong>spec</strong>ification into <code>validate-fn-with</code>&apos;s specification map to key <code>:speculoos/ret-scalar-spec</code>. Let&apos;s stipulate that the function returns an integer. Here&apos;s how we pass that specification to <code>validate-fn-with</code>.</p><pre><coce>{:speculoos/ret-scalar-spec int?}</coce></pre><p>And now, the function validation itself.</p><pre><code>(validate-fn-with sum-three
&nbsp;                 {:speculoos/ret-scalar-spec int?}
&nbsp;                 1
&nbsp;                 20
&nbsp;                 300)
;; =&gt; 321</code></pre><p>The return value <code>321</code> satisfies <code>int?</code>, so <code>validate-fn-with</code> returns the computed sum.</p><p>What happens when the return value is invalid? Instead of messing up <code>sum-three</code>&apos;s defninition, we&apos;ll merely alter the scalar predicate. Instead of an integer, we&apos;ll stipulate that <code>sum-three</code> returns a string?</p><pre><code>(validate-fn-with sum-three
&nbsp;                 {:speculoos/ret-scalar-spec string?}
&nbsp;                 1
&nbsp;                 20
&nbsp;                 300)
;; =&gt; ({:datum 321,
;;      :fn-spec-type :speculoos/return,
;;      :path nil,
;;      :predicate string?,
;;      :valid? false})</code></pre><p>Very nice. <code>sum-three</code> computed quite properly the sum of the three arguments. But we gave it a bogus return scalar specification that claimed it ought to be a string, which integer <code>321</code> fails to satify.</p><p>Did you happen to notice the <code>path</code>? We haven&apos;t yet encountered a case where a path is <code>nil</code>. In this situation, the function returns a &apos;bare&apos; scalar, not contained in a collection. Speculoos can validate a bare scalar when that bare scalar is a function&apos;s return value.</p><p>Let&apos;s see how to validate a function when the return value is a collection of scalars. We&apos;ll write a new function that returns four scalars: the three arguments and their sum.</p><pre><code>(defn enhanced-sum-three [x y z] [x y z (+ x y z)])</code><br /><br /><code>(enhanced-sum-three 1 20 300) ;; =&gt; [1 20 300 321]</code></pre><p>Our enhanced function now returns a vector of four elements. Let&apos;s remind ourselves how we&apos;d manually validate that return value. If we decide we want <code>enhanced-sum-three</code> to return four integers, the scalar specification would look like this.</p><pre><code>[int? int? int? int?]</code></pre><p>And the manual validation would look like this.</p><pre><code>(validate-scalars [1 20 300 321]
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
;;      :valid? true}]</code></pre><p>Four paired scalars and predicates, four validaton results. Let&apos;s see what happens when we validate the function return scalars.</p><pre><code>(validate-fn-with enhanced-sum-three
&nbsp;                 {:speculoos/ret-scalar-spec [int? int? int? int?]}
&nbsp;                 1
&nbsp;                 20
&nbsp;                 300)
;; =&gt; [1 20 300 321]</code></pre><p>Since we fed <code>validate-fn-with</code> a specification that happens to agree with those arguments, <code>enhanced-sum-three</code> returns its value, <code>[1 20 300 321]</code>.</p><p>Let&apos;s stir things up. We&apos;ll change the return scalar specification to something we know will fail: The first scalar a character, the final scalar a boolean.</p><pre><code>(validate-fn-with enhanced-sum-three
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
;;      :valid? false})</code></pre><p><code>enhanced-sum-three</code>&apos;s function body remained the same, and we fed it the same integers as before, but we fiddled with the return scalar specification so that we got two invalid results. <code>1</code> at path <code>[0]</code> does not satisfy its scalar predicate <code>char?</code> at the same path. And <code>321</code> at path <code>[3]</code> does not satisfy scalar predicate <code>boolean?</code> that shares its path.</p><p>Let&apos;s set aside validating scalars for a moment and validate a facet of <code>enhanced-sum-three</code>&apos;s return collection. First, we&apos;ll do a manual demonstration. Let&apos;s remember: collection predicates apply to their immediate parent container. We wrote <code>enhanced-sum-three</code> to return a vector, but to make the validation report an invalid, we&apos;ll pretend we&apos;re expecting a list.</p><pre><code>(validate-collections [1 20 300 321]
&nbsp;                     [list?])
;; =&gt; ({:datum [1 20 300 321],
;;      :ordinal-path-datum [],
;;      :path-datum [],
;;      :path-predicate [0],
;;      :predicate list?,
;;      :valid? false})</code></pre><p>That validation aligns with our understanding. <code>[1 20 300 321]</code> is not a list. The <code>list?</code> collection predicate at path <code>[0]</code> in the specification was paired with the thing found at path <code>(drop-last [0])</code> in the data, which in this example is the root collection. We designed <code>enhanced-sum-three</code> to yield a vector.</p><p>Let&apos;s toss that collection specification at <code>validate-with-fn</code> and have it apply to <code>enhanced-sum-three</code>&apos;s return value, which won&apos;t satisfy. We pass the return collection specification by associating it to the key <code>:speculoos/ret-collection-spec</code>.</p><pre><code>(validate-fn-with enhanced-sum-three
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
;;      :valid? false})</code></pre><p>Similarly to the manual collection validation we previously performed with <code>validate-collections</code>, we see that <code>enhanced-sum-three</code>;s return vector <code>[1 20 300 321]</code> fails to satisfy its <code>list?</code> collection predicate.</p><p>A scalar validation followed by an independent collection validation allows us to check every possible aspect that we could want. Now we that we&apos;ve seen how to individually validate <code>enhance-sum-three</code>&apos;s return scalars and return collections, we know how to do both with one invocation.</p><p>Remember Mantra #1: Validate scalars separately from validating collections. Speculoos will only ever do one or the other, but <code>validate</code> is a convenience function that performs a scalar validation immediately followed by a collection validation. We&apos;ll re-use the scalar specification and collection specification from the previous examples.</p><pre><code>(speculoos.core/only-invalid
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
;;      :valid? false})</code></pre><p><code>only-invalid</code> discards the validation where the predicates are satisfied, leaving only the invalids. Two scalars failed to satisfy their scalar predicates. Integer <code>1</code> at path <code>[0]</code> in the data fails to satisfy scalar predicate <code>char?</code> at path <code>[0]</code> in the scalar specification. Integer <code>321</code> fails to satisfy scalar predicate <code>boolean?</code> at path <code>[3]</code> in the scalar specification. Finally, our root vector <code>[1 20 300 321]</code> located at path <code>[]</code> fails to satisfy the collection predicate <code>list?</code> at path <code>[0]</code>.</p><p>Now that we&apos;ve seen the combo validation done manually, let&apos;s validate <code>enhanced-sum-three</code>&apos;s return in the same way. Here&apos;s where we see why to organize the specifications in a container instead of passing them as individual arguments: it keeps our invocation neater.</p><pre><code>(validate-fn-with enhanced-sum-three
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
;;      :valid? false})</code></pre><p><code>valiate-fn-with</code>&apos;s validation is substantially the same as the one <code>validate</code> produced in the previous example, except, now, the data comes from invoking <code>enhanced-sum-three</code>. Two scalar invalids and one collectoin invalid. Integer <code>1</code> fails to satisfy scalar predicate <code>char?</code>, integer <code>321</code> fails to satisfy scalar predicate <code>boolean?</code>, and the entire return vector <code>[1 20 300 321]</code> fails to satisfy collection predicate <code>list?</code>.</p><p>Okay. I think we&apos;re ready to put together all four different function validations we&apos;ve so far seen. We&apos;ve seen…</p><ul><li>a function argument scalar validation,</li><li>a function argument collection validation,</li><li>a function return scalar validation, and</li><li>a function return collection validation.</li></ul><p>And we&apos;ve seen how to combine both function argument validations, and how to combine both function return validations. Now we&apos;ll combine all four validations into one <code>validate-fn-with</code> invocation.</p><p>Let&apos;s review our ingredients. Here&apos;s our <code>enhanced-sum-three</code> function.</p><pre><code>(defn enhanced-sum-three [x y z] [x y z (+ x y z)])</code></pre><p><code>enhanced-sum-three</code> accepts three number arguments and returns a vector of those three numbers with their sum appended to the end of the vector. Technically, Clojure would accept any number for <code>x</code>, <code>y</code>, and <code>z</code>, but for illustration purposes, we&apos;ll make our scalar predicates something non-numeric so we can see something interesting in the validation reports.</p><p>With that in mind, we pretend that we want to validate the function&apos;s argument sequence as a string, followed by an integer, followed by a symbol. The function scalar specification will be…</p><pre><code>[string? int? symbol?]</code></pre><p>To allow <code>enhanced-sum-three</code> to calculate a result, we&apos;ll supply three numeric values, two of which will not satisfy that argument scalar specification. So that it produces something interesting, we&apos;ll make our function argument collection specification also complain.</p><pre><code><code>(defn length-2? [v] (= 2 (count v)))</code></code><br /><br /><code>[length-2?]</code></pre><p>We know for sure that the argument sequence will contain three values, so that argument collection predicate will produce something interesting.</p><p>Jumping to <code>enhanced-sum-three</code>&apos;s output side, we expect a vector of four numbers. Again, we&apos;ll craft our function return scalar specification to contain two predicates that we know won&apos;t be satisfied because those scalar predicates are looking for something non-numeric.</p><pre><code>[char? int? int? boolean?]</code></pre><p>Finally, since we defined <code>enhanced-sum-three</code> to return a vector, we&apos;ll make the function return collection specification look for a list.</p><pre><code>[list?]</code></pre><p>Altogether, those four specification are organized like this.</p><pre><code>{:speculoos/arg-scalar-spec     [string? int? symbol?]
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
;;      :predicate #function [speculoos-project-readme-generator/length-2?],
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
;;      :valid? false})</code></pre><p>We&apos;ve certainly made a mess of things. But it&apos;ll be understandable if we examine the invalidation report piece by piece. The first thing to know is that we have already seen each of those validations before in the previous examples, so could always go back to those examples and see the validations in isolation.</p><p>We see six non-satisfied predicates:<ul><li>Scalar <code>1</code> in the arguments sequence fails to satisfy scalar predicate <code>string?</code> in the argument scalar specification.</li><li>Scalar <code>300</code> in the arguments sequence fails to satisfy scalar predicate <code>symbol?</code> in the argument scalar specification.</li><li>The argument sequence <code>[1 20 300]</code> fails to satisfy collection predicate <code>length-2?</code> in the argument collection specification.</li><li>Scalar <code>1</code> in the return vector fails to satisfy scalar predicate <code>char?</code> in the return scalar specification.</li><li>Scalar <code>321</code> in the return vector fails to satisfy scalar predicate <code>boolean?</code> in the return scalar specification.</li><li>The return vector <code>[1 20 300 321</code> fails to satisfy collection predicate <code>list?</code> in the return collection specification.</li></ul></p><p>Also note that the validation entries have a <code>:fn-spec-type</code> entry associated to either <code>:speculoos/return</code> or <code>:speculoos/argument</code>, which tells us where a particular invalid was located. There may be a situation where indistinguishable invalid datums appear in both the arguments and returns. In this case, integer <code>1</code> was an invalid datum at path <code>[0]</code> for both the argument sequence and the return vector. <code>fn-spec-type</code> helps resolve the ambiguity.</p><h3 id="fn-correctness">3. Validating Function Correctness</h3><p>So far, we&apos;ve seen how to validate function argument sequences and function return values, both their scalars, and their collections. Validating function argument sequences allows us to check if the function was invoked properly. Validating function return values gives a limited ability to check the internal operation of the function. If we want another level of thoroughness checking the correctness, we can specify and validate the relationships between the functions arguments and return values. Perhaps you&apos;d like to be able to express <em>The return value is a collection, with all the same elements as the input sequence.</em> Or <em>The return value is a concatenation of the even indexed elements of the input sequence.</em> Speculoos&apos; term for this action is <em>validating function argument and return value relationship</em>.</p><p>There&apos;s one tricky detail we must accommodate. The vector we&apos;re going to pass as an argument to our reversing function is itself contained in the argument sequence. Take a look.</p><pre><code>(defn arg-passthrough [&amp; args] args)</code><br /><br /><code>(arg-passthrough [7 8 9]) ;; =&gt; ([7 8 9])</code></pre><p>So we must account for the fact that the vector of integers we pass to the relationship function is also contained within a sequence. We could quickly modify our three relationship functions to destructure their first argument.</p><h3 id="recognized-metadata-keys">Recognized metadata specification keys</h3><p>Speculoos consults the following defined group of keys in a specification map when it validates.</p><pre><code>speculoos.function-specs/recognized-spec-keys
;; =&gt; [:speculoos/arg-scalar-spec
;;     :speculoos/arg-collection-spec
;;     :speculoos/ret-scalar-spec
;;     :speculoos/ret-collection-spec
;;     :speculoos/argument-return-relationships
;;     :speculoos/canonical-sample
;;     :speculoos/predicate-&gt;generator
;;     :speculoos/hof-specs]</code></pre><h3 id="explicit">Function Metadata Specifications</h3><p>Speculoos function specifications <a href="https://clojure.org/about/spec#_dont_further_add_tooverload_the_reified_namespaces_of_clojure">differ</a> from <code>spec.alpha</code> in that they are stored and retrieved directly from the function&apos;s metadata. Speculoos is an experiment, but I thought it would be nice if I could hand you one single thing and say </p><blockquote><p><em>Here&apos;s a Clojure function you can use. Its name suggests what it does, its docstring that tells you how to use it, and human- and machine-readable specifications check the validity of the inputs, and tests that it&apos;s working properly. All in one neat, tidy </em>S-expression.</p></blockquote><p>Speculoos offers three patterns of function validation.<ol><li><code>validate-fn-with</code> performs explicit validation with a specification supplied in a separate map. The function var is not altered.</li><li><code>validate-fn-meta-spec</code> performs explicit validation with specifications contained in the function&apos;s metadata.</li><li><code>instrument/unstrument</code> provide implicit validation with specifications contained in the function&apos;s metadata.</li></ol></p><p>Up until this point, we&apos;ve been using the most explicit variant, <code>validate-fn-with</code> because its behavior is the most readily apparent.</p><p>The first pattern is nice because you can quickly validate a function <em>on-the-fly</em> without messing with the function&apos;s metadata. Merely supply the function&apos;s name, a map of specifications (we&apos;ll discuss this soon), and a sequence of args as if you were directly invoking the function.</p><p>Speculoos offers a pair convenience functions to add and remove specifications from a function&apos;s metadata. To add, use <code>inject-specs!</code>.</p><pre>#&apos;speculoos-project-readme-generator/add-ten<code>(require &apos;[speculoos.function-specs :refer
&nbsp;          [inject-specs! unject-specs! validate-fn-meta-spec]])</code><br /><br /></pre><p>We can observe that the specifications indeed live in the function&apos;s metadata. If we later decided to undo that, <code>unject-specs!</code> removes all recognized Speculoos specification entries, regardless of how they got there. For the upcoming demonstrations, though, we&apos;ll keep those specifications in <code>add-ten</code>&apos;s metadata.</p><p>Now that <code>add-ten</code> holds the specifications in its metadata, we can try the second pattern of explicit validation pattern. It&apos;s similar, except we don&apos;t have to supply the specification map; it&apos;s already waiting in the metadata. Invoked with a valid argument, <code>add-ten</code> returns a valid value.</p><pre><code>(validate-fn-meta-spec add-ten 15) ;; =&gt; 25</code></pre><p>Invoking <code>add-ten</code> with an invalid float, Speculoos interrupts with a validation report.</p>#&apos;speculoos-project-readme-generator/silly<p>Next, we inject our specifications into the function&apos;s metadata: a scalar specification and collection specification, each, for both the argument sequence and the return sequence. A grand total of four specifications.</p><p>Valid inputs…</p><p>…produce a valid return vector of three elements. The function halves the ratio, increments the integer, and reverses the string. But invalid arguments…</p><p>…yields an invalidation report: the character <code>\a</code> does not satisfy its <code>string?</code> scalar predicate, and the integer <code>9</code> does not satisfy its <code>ratio?</code> scalar predicate.</p><p>Until this point in our discussion, Speculoos has only performed function validation when we explicitly call either <code>validate-fn-with</code> or <code> validate-fn-meta-spec</code>. The specifications in the metadata are passive and produce no effect, even with arguments that would otherwise fail to satisfy the specification&apos;s predicates.</p><pre><code>(add-ten 15) ;; =&gt; 25</code><br /><code>(add-ten 1.23) ;; =&gt; 11.23</code></pre><h3>Instrumenting Functions</h3><p>Speculoos&apos; third pattern of function validation <em>instruments</em> the function using the metadata specifications. (Beware: <code>instrument</code>-style function validation is very much a work in progress. The implementation is sensitive to invocation order and can choke on multiple calls. The var mutation is not robust.) Every invocation of the function itself automatically validates any specified arguments and return values.</p><pre><code>(require &apos;[speculoos.function-specs :refer [instrument unstrument]])</code></pre><p>The function returns if it doesn&apos;t throw an exception. Any non-satisfied predicates are reported to <code>*out*</code>. When we are done, we can <em>unstrument</em> the function, and Speculoos will no longer intervene.</p><p>Even though character <code>\a</code> and not-ratio <code>9</code> do not satisfy their respective predicates, <code>silly</code> is no longer instrumented, and Speculoos is not intercepting its invocation, so the function returns a value. (Frankly, I wrote <code>instrument/unstrument</code> to mimic the features <code>spec.alpha</code> offers. My implementation is squirrelly, and I&apos;m unskilled with mutating vars. I lean much more towards the deterministic <code>validate-fn-meta-spec</code> and <code>validate-fn-with</code>.)</p><p>Beyond validating a function&apos;s argument sequence and its return, Speculoos can perform a validation that checks the relationship between any aspect of the arguments and return values. When the arguments sequence and return sequence share a high degree of shape, an <em>argument versus return scalar specification</em> will work well. A good example of this is using <code>map</code> to transform a sequence of values. Each item in the return sequence has a corresponding item in the argument sequence.</p><p>The predicates in a <em>versus</em> specification are a little bit unusual. Each predicate accepts <em>two</em> arguments: the first is the element from the argument sequence, the second is the corresponding element from the return sequence.</p><p>Let&apos;s make a bogus predicate that we know will fail, just so we can see what happens when a relationship is not satisfied.</p><p>And stuff them into a map whose keys Speculoos recognizes.</p><p>Now that we&apos;ve prepared our predicates and composed a versus specification, we can validate the relationship between the arguments and the returns. We&apos;ll invoke <code>mult-ten</code> with the same <code>1 2 3</code> sequence we saw above.</p><p>We intentionally constructed our specification to fail at the middle element, and sure enough, the validation report tells us the argument and return scalars do not share the declared relationship. <code>20</code> from the return sequence is not nine times <code>2</code> from the arg sequence.</p><p>To complete the <em>scalars/collections/arguments/returns/versus</em> feature matrix, Speculoos can also validate function argument <em>collections</em> against return collections. All of the previous discussion holds, with the twist that the specification predicates apply against the argument collections and return collections. Examples show better than words.</p><p> Speculoos passes through the function&apos;s return if all predicates are satisfied, so we&apos;ll intentionally bungle one of the predicates to cause an invalidation report.</p><p>Composing our args versus return collection specification, using the proper pseudo-qualified key. Remember: collection predicates apply to their immediate containing collections.</p><p>Our goofy reverse function fails our buggy argument versus return validation.</p><p><code>goofy-reverse</code> behaves exactly as it should, but for illustration purposes, we applied a buggy collection specification that we knew would fail. The validation report shows us the two things it compared, in this instance, the argument sequence and the returned, reversed sequence, and furthermore, that those two collections failed to satisfy the buggy predicate, <code>equal-lengths?</code>. The other predicate, <code>mirror-elements-equal?</code> was satisfied because the first element of the argument collection is equal to the last element of the return collection, and was therefore not included in the report.</p><h3 id="hof">Validating Higher-Order Functions</h3><p>Speculoos has a story about validating higher-order functions, too. It uses very similar patterns to first-order function validation: put some specifications in the function&apos;s metadata with the properly qualified keys, then invoke the function with some sample arguments, then Speculoos will validate the results. Here&apos;s how it works. We start with a flourish of the classic adder <span class="small-caps">hof</span>.</p><pre><code>(require &apos;[speculoos.function-specs :refer [validate-higher-order-fn]])</code><br /><br /><code>(defn addder [x] (fn [y] (fn [z] (+ x (* y z)))))</code><br /><br /><code>(((addder 3) 2) 10) ;; =&gt; 23</code></pre><p><code>addder</code> returns a function upon each of its first two invocations, and only on its third does it return a scalar. Specifying and validating a function value does not convey much meaning: it would merely satisfy <code>fn?</code> which isn&apos;t very interesting. So to validate a <span class="small-caps">hof</span>, Speculoos requires it to be invoked until it produces a value. So we&apos;ll supply the validator with a series of arg sequences  that, when fed in order to the <span class="small-caps">hof</span>, will produce a result. For the example above, it will look like <code>[3] [2] [10]</code>.</p><p>The last task we must do is create the specification. <span class="small-caps">hof</span> specifications live in the function&apos;s metadata at key <code>:speculoos/hof-specs</code> which is a series of nested specification maps, one nesting for each returned function. For this example, we might create this <span class="small-caps">hof</span> specification.</p><pre><code>(def addder-spec
&nbsp; {:speculoos/arg-scalar-spec [even?],
&nbsp;  :speculoos/hof-specs {:speculoos/arg-scalar-spec [ratio?],
&nbsp;                        :speculoos/hof-specs {:speculoos/arg-scalar-spec
&nbsp;                                                [float?]}}})</code></pre><p>Once again, for illustration purposes, we&apos;ve crafted predicates that we know will invalidate, but will permit the function stack to evaluate to completion. (Otherwise, validation halts on exceptions.</p><p><span class="small-caps">hof</span> validation requires that the metadata hold the specifications. So we inject them.</p><pre><code>(inject-specs! addder addder-spec) ;; =&gt; nil</code></pre><p>And finally, we execute the validation with <code>validate-higher-order-fn</code></p><pre><code>(require &apos;[speculoos.function-specs :refer [validate-higher-order-fn]])</code><br /><br /><code>(validate-higher-order-fn addder [3] [5] [10])
;; =&gt; ({:datum 3,
;;      :fn-tier :speculoos/argument,
;;      :path [0 0],
;;      :predicate even?,
;;      :valid? false}
;;     {:datum 5,
;;      :fn-tier :speculoos/argument,
;;      :path [1 0],
;;      :predicate ratio?,
;;      :valid? false}
;;     {:datum 10,
;;      :fn-tier :speculoos/argument,
;;      :path [2 0],
;;      :predicate float?,
;;      :valid? false})</code></pre><p>Let&apos;s step through the validation results. Speculoos validates <code>3</code> against scalar predicate <code>even?</code> and then invokes <code>addder</code> with argument <code>3</code>. It then validates <code>5</code> against scalar predicate <code>ratio?</code> and then invokes the returned function with argument <code>5</code>. Finally, Speculoos validates <code>10</code> against scalar predicate <code>float?</code> and invokes the previously returned function with argument <code>10</code>. If all the predicates were satisfied, Speculoos would yield the return value of the function call. In this case, all three arguments are invalid, and Speculoos yields a validation report.</p></section><section id="exercising"><h2>Generating Random Samples and Exercising</h2><pre><code>(require &apos;[speculoos.function-specs :refer [exercise-fn]])</code></pre><p>Another tool Speculoos offers in this category: exercising specified functions. If you have injected an argument scalar specification into your function, Speculoos can generate a series of specification-satisfying arguments and repeatedly invoke your function. Let&apos;s take advantage of the nice feature of <code>defn</code> that adds metadata during function definition.</p><pre><code>(defn bottles
&nbsp; {:speculoos/arg-scalar-spec [pos-int? string?]}
&nbsp; [n liquid]
&nbsp; (str n &quot; bottles of &quot; liquid &quot; on the wall, &quot; n &quot; bottles of &quot; liquid &quot;...&quot;))</code><br /><br /><code>(bottles 99 &quot;espresso&quot;)
;; =&gt; &quot;99 bottles of espresso on the wall, 99 bottles of espresso...&quot;</code></pre><p>Because we included a scalar specification where it could be found, Speculoos can exercise our function using random sample generators synthesized using that specification as a blueprint.</p><pre><code>(exercise-fn bottles 5)
;; =&gt; ([[7 &quot;77ms1U6iP5SZn&quot;] &quot;7 bottles of 77ms1U6iP5SZn on the wall, 7 bottles of 77ms1U6iP5SZn...&quot;]
;;     [[12 &quot;bJmHs5dn5m&quot;] &quot;12 bottles of bJmHs5dn5m on the wall, 12 bottles of bJmHs5dn5m...&quot;]
;;     [[28 &quot;7&quot;] &quot;28 bottles of 7 on the wall, 28 bottles of 7...&quot;]
;;     [[29 &quot;ebg85U66u&quot;] &quot;29 bottles of ebg85U66u on the wall, 29 bottles of ebg85U66u...&quot;]
;;     [[7 &quot;o1Q1wla0Ts3mS052AAoT9n2B1&quot;] &quot;7 bottles of o1Q1wla0Ts3mS052AAoT9n2B1 on the wall, 7 bottles of o1Q1wla0Ts3mS052AAoT9n2B1...&quot;])</code></pre><p>Not exactly thirst-quenching.</p></section><section id="utilities"><h2>Utility Functions</h2><p>You won&apos;t miss any crucial piece of Speculoos&apos; functionality if you don&apos;t use this namespace, but perhaps something here might make your day a little nicer. Nearly every function takes advantage of <code>speculoos.core/all-paths</code>, which decomposes a heterogeneous, arbitrarily-nested data structure into a sequence of paths and datums. With that in hand, these not-clever functions churn through the entries and give you back something useful.</p><pre><code>(require &apos;[speculoos.utility :refer
&nbsp;          [scalars-without-predicates predicates-without-scalars
&nbsp;           collections-without-predicates predicates-without-collections
&nbsp;           thoroughly-valid? sore-thumb spec-from-data data-from-spec
&nbsp;           basic-collection-spec-from-data exercise]])</code></pre><p>Recall that Speculoos only validates scalars and predicate located at identical paths in the. This pair of utilities tells us where we have unmatched scalars or unmatched predicates.</p><pre><code>(scalars-without-predicates [42 [&quot;abc&quot; 22/7]]
&nbsp;                           [int?])
;; =&gt; #{{:path [1 0], :value &quot;abc&quot;}
;;      {:path [1 1], :value 22/7}}</code></pre><p>With this information, we can see if the specification was ignoring scalars that we were expecting to validate, and adjust our specification for better coverage. (The <code>thoroughly-…</code> group of functions would strictly enforce all datums be paired with predicates.)</p><p>This next utility can help diagnose surprising results. Just because you put a predicate into the scalar specification doesn&apos;t force validation of a scalar that doesn&apos;t exist.</p><pre><code>(predicates-without-scalars [42 :foo]
&nbsp;                           [int? [keyword? string?]])
;; =&gt; ({:path [1 0], :value keyword?}
;;     {:path [1 1], :value string?})</code></pre><p>Now we can see two un-paired predicates. <code>string?</code> simply doesn&apos;t have a scalar, and <code>keyword</code> doesn&apos;t share a path with <code>:foo</code> so it wasn&apos;t used during validation.</p><p>It&apos;s not difficult to neglect a predicate for a nested element within a collection specification, so Speculoos offers analogous utilities to highlight those possible issues.</p><pre><code>(collections-without-predicates [11 [22 {:a 33}]]
&nbsp;                               [vector? [{:is-a-map? map?}]])
;; =&gt; #{{:path [1], :value [22 {:a 33}]}}</code></pre><p>Yup, we didn&apos;t specify that inner vector whose first element is <code>22</code>. That&apos;s okay, though. Maybe we don&apos;t care to specify it. But now we&apos;re aware.</p><p>Maybe we put a predicate into a collection specification that clearly ought to be unsatisfied, but for some reason, <code>validate-collections</code> isn&apos;t picking it up.</p><pre><code>(predicates-without-collections {:a 42}
&nbsp;                               {:is-map? map?, :b [set?]})
;; =&gt; #{{:path [:b 0], :value set?}}</code></pre><p>Aha. <code>set?</code> in the collection specification isn&apos;t paired with an element in the data, so it is unused during validation.</p><p>Taking that idea to its logical conclusion, <code>thoroughly-valid?</code> returns <code>true</code> only if every scalar and every collection in data have a corresponding predicate in the scalar specification and the collection specification, respectively, and all those predicates are satisfied.</p><pre><code>;; all scalars and the vector have predicates; all predicates satisfied</code><br /><code>(thoroughly-valid? [42 :foo 22/7]
&nbsp;                  [int? keyword? ratio?]
&nbsp;                  [vector?])
;; =&gt; true</code><br /><br /><code>;; all scalars and the vector have predicates, but the 22/7 fails the scalar predicate</code><br /><code>(thoroughly-valid? [42 :foo 22/7]
&nbsp;                  [int? keyword? string?]
&nbsp;                  [vector?])
;; =&gt; false</code><br /><br /><code>;; all scalars and the vector have predicates, but the vector fails the collection predicate</code><br /><code>(thoroughly-valid? [42 :foo 22/7]
&nbsp;                  [int? keyword? ratio?]
&nbsp;                  [list?])
;; =&gt; false</code><br /><br /><code>;; all predicates are satisfied, but the 22/7 scalar is missing a predicate</code><br /><code>(thoroughly-valid? [42 :foo 22/7]
&nbsp;                  [int? keyword?]
&nbsp;                  [vector?])
;; =&gt; false</code><br /><br /><code>;; all predicates are satisfied, but the vector is missing a predicate</code><br /><code>(thoroughly-valid? [42 :foo 22/7]
&nbsp;                  [int? keyword? ratio?]
&nbsp;                  [])
;; =&gt; false</code></pre><p>In the first example, we learned that all our predicates are valid predicates, but in the second example, we see where our specification contains two non-predicates.</p><p>I envision that you&apos;d be using these utility functions mainly during dev time, but I won&apos;t protest if you find them useful in production. This next utility, however, is probably only useful at the keyboard. Given data and a scalar specification, it prints back both, but with only the invalid scalars and predicates showing.</p><div class="no-display">#&apos;speculoos-project-readme-generator/sore-thumb-example#&apos;speculoos-project-readme-generator/sore-thumb-example-eval</div><pre><code>(sore-thumb [42 {:a true, :b [22/7 :foo]} 1.23]
&nbsp;           [int? {:a boolean?, :b [ratio? string?]} int?])</code><br /><br /><code>;; to *out*</code><br /><code>data: [_ {:a _, :b [_ :foo]} 1.23]
spec: [_ {:a _, :b [_ string?]} int?]
</code></pre><p>I&apos;ve found it handy for quickly pin-pointing the unsatisfied scalar-predicate pairs in a large, deeply-nested data structure.</p><p>I think of the next few utilities as <em>creative</em>, making something that didn&apos;t previously exist. We&apos;ll start with a pair of functions which perform complimentary actions.</p><pre><code>(spec-from-data [33 {:a :baz, :b [1/3 false]} &apos;(3.14 \z)])
;; =&gt; [int?
;;     {:a keyword?,
;;      :b [ratio? boolean?]}
;;     (double? char?)]</code><br /><br /><code>(data-from-spec
&nbsp; {:x int?, :y [ratio? boolean?], :z (list char? neg-int?)}
&nbsp; :random)
;; =&gt; {:x -408,
;;     :y [30/7 true],
;;     :z (\E -18)}</code></pre><p>I hope their names give good indications of what they do. The generated specification contains only basic predicates, that is, merely <em>Is it an integer?</em>, not <em>Is it an even integer greater than 25, divisible by 3?</em>. But it&apos;s convenient raw material to start crafting a tighter specification. (Oh, yeah…they both round-trip.) A few <a href="#custom-generators">paragraphs down</a> we&apos;ll see some ways to create random sample generators for compound predicates.</p><p>Speaking of raw material, Speculoos also has a collection specification generator.</p><pre><code>(basic-collection-spec-from-data [55 {:q 33, :r [&apos;foo &apos;bar]} &apos;(22 44 66)])
;; =&gt; [{:r [vector?], :speculoos.utility/collection-predicate map?} (list?) vector?]</code></pre><p>Which produces a specification that is prehaps not immediately useful, but does provide a good starting template, because collection specifications can be tricky to get just right.</p><p id="custom-generators">The <code>utility</code> namespace contains a trio of functions to assist writing, checking, and locating compound predicates that can be used by <code>data-from-spec</code> and the function validation functions to generate valid random sample data. A compound predicate such as <code>#(and (int? %) (&lt; % 100))</code> does not have built-in generator provided by <code>clojure.test.check.generators</code>. However, <code>data-from-spec</code> can extract a generator residing in the predicate&apos;s metadata. Here&apos;s an example of how to add a hand-written generator to a scalar predicate. We&apos;ll write a scalar specification requiring a vector composed of a keyword followed by a number less than one-hundred. </p><pre><code>(data-from-spec [keyword? (with-meta #(and (number? %) (&lt; % 100)) {:speculoos/predicate-&gt;generator #(rand 101)})] :random)
;; =&gt; [:--d 3.6915340167991566]</code></pre><p><code>data-from-spec</code> was able to locate two generators. First, because it&apos;s a predicate included in the core Clojure distribution, <code>keyword?</code> owns a built-in random sample generator. Second, the compound predicate for <em>number greater than one hundred</em> doesn&apos;t have a built-in generator, but <code>data-from-spec</code> found the generator stored in the predicate&apos;s metadata.</p><p>The custom generator must be provided at the metadata key <code>:speculoos/predicate-&gt;generator</code>. If we conform to their expectations, Speculoos&apos; data-generating functions can automatically create generators from its specifications. But if for some reason we were handed a specification that did not, we must write the generators by hand. It can be tricky to make sure the generator produces values that precisely satisfy the predicate, so Speculoos provides a utility to check one against the other. What if we don&apos;t quite have the generator written correctly?</p><pre><code>(require &apos;[speculoos.utility :refer
&nbsp;          [validate-predicate-&gt;generator unfindable-generators defpred]]
&nbsp;        &apos;[clojure.test.check.generators :as gen])</code><br /><br /><code>;; warm up the generator for better-looking results</code><br /><code>(validate-predicate-&gt;generator
&nbsp; (with-meta #(and (int? %) (even? %) (&lt;= 50 500))
&nbsp;   {:speculoos/predicate-&gt;generator
&nbsp;      #(last (gen/sample
&nbsp;               (gen/such-that odd? (gen/large-integer* {:min 50, :max 500}))
&nbsp;               22))})
&nbsp; 5)
;; =&gt; ([359 false]
;;     [289 false]
;;     [143 false]
;;     [53 false]
;;     [423 false])</code></pre><p>Oops. We paired <code>odd?</code> with <code>such-that</code> when we should have used <code>even?</code>. Let&apos;s fix it using another helper, <code>defpred</code>, that relieves us of a bit of that keyboarding.</p><pre><code>(defpred fixed
&nbsp;        #(and (int? %) (even? %) (&lt;= 50 500))
&nbsp;        #(last (gen/sample
&nbsp;                 (gen/such-that even? (gen/large-integer* {:min 50, :max 500}))
&nbsp;                 22)))</code><br /><br /><code>(validate-predicate-&gt;generator fixed 5)
;; =&gt; ([338 true]
;;     [292 true]
;;     [50 true]
;;     [62 true]
;;     [228 true])</code></pre><p>We can see that the generator now yields values that satisfy its predicate.</p><p>If you don&apos;t manually supply a generator, <code>defpred</code> will create one automatically if your predicate&apos;s structure fulfills some assumptions. A top-level <code>or</code> indicates a set of possible Clojure scalar types. Let&apos;s define a predicate that tests for either an integer, keyword, or ratio and then check how it works.</p><pre><code>(defpred int-kw-ratio? #(or (int? %) (keyword? %) (ratio? %)))</code><br /><br /><code>(validate-predicate-&gt;generator int-kw-ratio? 9)
;; =&gt; ([-30 true]
;;     [24 true]
;;     [:.950bBiY true]
;;     [-1/5 true]
;;     [8/29 true]
;;     [:P true]
;;     [29/23 true]
;;     [29/16 true]
;;     [-7/6 true])</code></pre><p>Here&apos;s what just happened. <code>defpred</code> binds the symbol <code>int-kw-ratio?</code> to the anonymous function <code>#(or (int? %) (keyword? %) (ratio? %))</code> in the same manner as if we had used <code>def</code>. Furthermore, because we used a top-level <code>or</code> and recognized predicates, <code>defpred</code> attached to its metadata a random sample generator that produces an integer, keyword, or a ratio scalar with equal probabilities. Then, <code>validate-predicate-&gt;generator</code> exercised that generator nine times, and we can see that each of the nine samples satisfied the predicate.</p><p><code>and</code> signals a set of modifiers as long as the first clause tests for a basic Clojure scalar type. Let&apos;s define a predicate that tests for an integer that is even and greater-or-equal to two.</p><pre><code>(defpred even-int-more-5 (fn [i] (and (int? i) (even? i) (&lt;= 2 i))))</code><br /><br /><code>(validate-predicate-&gt;generator even-int-more-5 5)
;; =&gt; ([16 true]
;;     [2 true]
;;     [30 true]
;;     [4 true]
;;     [18 true])</code></pre><p>Note how the first <em>s-expression</em> immediately following <code>and</code>, <code>(int? i)</code>, selects the basic Clojure scalar type. All subsequent <em>s-expressions</em> refine the integer sample generator.</p><p>Now, let&apos;s see how we might combine both <code>or</code> and <code>and</code>. We&apos;ll define a predicate that tests for either an odd integer, a string of at least three characters, or a ratio greater than one-ninth.</p><pre><code>(defpred combined-pred
&nbsp;        #(or (and (int? %) (odd? %))
&nbsp;             (and (string? %) (&lt;= 3 (count %)))
&nbsp;             (and (ratio? %) (&lt; 1/9 %))))</code><br /><code>(validate-predicate-&gt;generator combined-pred 7)
;; =&gt; ([8/15 true]
;;     [30/23 true]
;;     [19/31 true]
;;     [10/23 true]
;;     [-5 true]
;;     [24/19 true]
;;     [&quot;VN6Kfn&quot; true])</code></pre><p>Perhaps we&apos;ve got a specification in hand, and we&apos;d like to know if all of the predicates have a random sample generator. Let&apos;s write a couple of specifications and deliberately neglect to give them random sample generators.</p><pre><code>(def not-int? #(not (int? %)))</code><br /><br /><code>(def needless-str? #(string? %))</code><br /><br /><code>(unfindable-generators [int? {:a string?, :b [not-int? needless-str?]}])
;; =&gt; [{:path [1 :b 0],
;;      :value
;;        #function
;;         [speculoos-project-readme-generator/not-int?]}
;;     {:path [1 :b 1],
;;      :value
;;        #function
;;         [speculoos-project-readme-generator/needless-str?]}]</code></pre><p><code>int?</code> and <code>string?</code> are included in the core Clojure distribution and have a sibling in <code>test.check</code>, so they <em>do</em> have a findable generator. However, we did not supply generators for <code>not-int?</code> and <code>needless-str?</code> within the metadata, so <code>unfindable-generators</code> reports the paths to those two predicates.</p><p>One final creative utility: <code>exercise</code>, which consumes a scalar specification, and generates a series of random data, then validates them.</p><pre><code>(exercise [int? symbol? {:x boolean?, :y ratio?}] 5)
;; =&gt; ([[913 x:8JW*/L76cG02 {:x false, :y 7/2}] true]
;;     [[573 _ek9/UW {:x true, :y 1/5}] true]
;;     [[-176 +*?9i3/VG {:x false, :y -20/23}] true]
;;     [[208 -b+S/. {:x true, :y 13/9}] true]
;;     [[816 J4/f {:x false, :y 23/17}] true])</code></pre><p>Five times, <code>exercise</code> generated a random sample of an integer, symbol, boolean, and a ratio, arranged in exactly the shape of the specification. Thankfully, they all validated as <code>true</code>.</p></section><section id="predicates"><h2>Predicates</h2><p>A predicate function returns a truthy or falsey value.</p><pre><code>(#(&lt;= 5 %) 3) ;; =&gt; false</code><br /><br /><code>(#(= 3 (count %)) [1 2 3]) ;; =&gt; true</code></pre><p>Non-boolean returns work, too. For example, <a href="#sets">sets</a> make wonderful membership tests.</p><pre><code>;; truthy</code><br /><code>(#{:blue :green :orange :purple :red :yellow} :green) ;; =&gt; :green</code><br /><br /><code>;; falsey</code><br /><code>(#{:blue :green :orange :purple :red :yellow} :swim) ;; =&gt; nil</code></pre><p>Regular expressions come in handy for validating string contents.</p><pre><code>;; truthy</code><br /><code>(re-find #&quot;^Four&quot; &quot;Four score and seven years ago...&quot;) ;; =&gt; &quot;Four&quot;</code><br /><br /><code>;; falsey</code><br /><code>(re-find #&quot;^Four&quot; &quot;When in the course of human events...&quot;) ;; =&gt; nil</code></pre><p>Invoking a predicate when supplied with a datum — scalar or collection — is the core action of Speculoos&apos; validation.</p><pre><code>(int? 42) ;; =&gt; true</code><br /><br /><code>(validate-scalars [42]
&nbsp;                 [int?])
;; =&gt; [{:datum 42,
;;      :path [0],
;;      :predicate int?,
;;      :valid? true}]</code></pre><p>Speculoos is fairly ambivalent about the predicate return value. The <code>validate…</code> family of functions mindlessly churns through its sequence of predicate-datum pairs, evaluates them, and stuffs the results into <code>:valid?</code> keys. The <code>valid…?</code> family of functions rips through <em>that</em> sequence, and if none of the results are falsey, returns <code>true</code>, otherwise it returns <code>false</code>.</p><p>For most of this document, we&apos;ve been using the built-in predicates offered by <code>clojure.core</code> such as <code>int?</code> and <code>vector?</code> because they&apos;re short, understandable, and they render well. But in practice, it&apos;s not terribly useful to validate an element with a mere <em>Is this scalar an integer?</em> or <em>Is this collection a vector?</em> Often, we&apos;ll want to combine multiple predciates to make the validation more specific. We could certainly use <code>clojure.core/and</code></p><pre><code>#(and (int? %) (pos? %) (even? %))</code></pre><p> and <code>clojure.core/or</code></p><pre><code>#(or (string? %) (char? %))</code></pre><p>which have the benefit of being universally understood. But Clojure also provides a pair of nice functions that streamline the expression and convey your intention. <code>every-pred</code> composes an arbitrary number of predicates with <code>and</code> semantics.</p><pre><code>((every-pred number? pos? even?) 100) ;; =&gt; true</code></pre><p>Similarly, <code>some-fn</code> composes predicates with <code>or</code> semantics.</p><pre><code>((some-fn number? string? boolean?) \z) ;; =&gt; false</code></pre><p>When Speculoos validates the scalars of a sequence, it consumes each element in turn. If we care only about validating some of the elements, we must include placeholders in the specification to maintain the sequence of predicates.</p><p>For example, suppose we only want to validate the third element of <code>[42 :foo \z]</code>. The first two elements are irrelevant to us. We have a few options. We could write our own little always-true predicate. <code>#(true)</code> won&apos;t work because <code>true</code> is not invocable. <code>#(identity true)</code> loses the conciseness. This works…</p><pre><code>(fn [] true)</code></pre><p>…but Clojure already includes a couple of nice options.</p><pre><code>(valid-scalars? [42 :foo \z]
&nbsp;               [(constantly true) (constantly true) char?])
;; =&gt; true</code></pre><p><code>constantly</code> is nice because it accepts any number of args. But for my money, nothing tops <code>any?</code>.</p><pre><code>(valid-scalars? [42 :foo \z]
&nbsp;               [any? any? char?]) ;; =&gt; true</code></pre><p>It&apos;s four characters, doesn&apos;t require typing parentheses, and the everyday usage of <em>any</em> aligns well with its technical purpose.</p><p>A word of warning about <code>clojure.core/contains?</code>. It might seem natural to use <code>contains?</code> to check if a collection contains an item, but it doesn&apos;t do what its name suggests. Observe.</p><pre><code>(contains? [97 98 99] 1) ;; =&gt; true</code></pre><p><code>contains?</code> actually tells you whether a collection contains a key. For a vector, it tests for an index. If you&apos;d like to check whether a value is contained in a collection, you can use this pattern.</p><pre><code>(defn in? [coll item] (some #(= item %) coll))</code><br /><br /><code>;; integer 98 is a value found in the vector</code><br /><code>(in? [97 98 99] 98) ;; =&gt; true</code><br /><br /><code>;; integer 1 is not a value found in the vector</code><br /><code>(in? [97 98 99] 1) ;; =&gt; nil</code></pre><p>(Check out <code>speculoos.utility/in?</code>.)</p><p>I&apos;ve been using the <code>#(…)</code> form because it&apos;s compact, but it does have a drawback for use with Speculoos.</p><pre><code>[{:path [0],
&nbsp; :datum 42,
&nbsp; :predicate #function[documentation/eval94717/fn--94718],
&nbsp; :valid? false}]</code></pre><p>The function rendering is not terribly informative when the validation displays the predicate. Same problem with <code>(fn [v] (…))</code>.</p><p>One solution to this issue is to define your predicates with an informative name.</p><pre><code>(def greater-than-50? #(&lt; 50 %))</code><br /><br /><code>(validate-scalars [42]
&nbsp;                 [greater-than-50?])
;; =&gt; [{:datum 42,
;;      :path [0],
;;      :predicate #function [speculoos-project-readme-generator/greater-than-50?],
;;      :valid? false}]</code></pre><p>Now, the predicate entry carries a bit more meaning.</p><p>Regular expressions check the content of strings.</p><pre><code>(def re #&quot;F\dQ\d&quot;)</code><br /><br /><code>(defn re-pred [s] (re-matches re s))</code><br /><br /><code>(validate-scalars [&quot;F1Q5&quot; &quot;F2QQ&quot;]
&nbsp;                 [re-pred re-pred])
;; =&gt; [{:datum &quot;F1Q5&quot;,
;;      :path [0],
;;      :predicate #function [speculoos-project-readme-generator/re-pred],
;;      :valid? &quot;F1Q5&quot;}
;;     {:datum &quot;F2QQ&quot;,
;;      :path [1],
;;      :predicate #function [speculoos-project-readme-generator/re-pred],
;;      :valid? nil}]</code></pre><p>Speculoos considers free-floating regexes in a scalar specification as predicates, so you can simply jam them in there.</p><pre><code>(valid-scalars? [&quot;A1B2&quot; &quot;CDEF&quot;]
&nbsp;               [#&quot;(\w\d){2}&quot; #&quot;\w{4}&quot;])
;; =&gt; true</code><br /><br /><code>(validate-scalars {:a &quot;foo&quot;, :b &quot;bar&quot;}
&nbsp;                 {:a #&quot;f.\w&quot;, :b #&quot;^[abr]{0,3}$&quot;})
;; =&gt; [{:datum &quot;foo&quot;,
;;      :path [:a],
;;      :predicate #&quot;f.\w&quot;,
;;      :valid? &quot;foo&quot;}
;;     {:datum &quot;bar&quot;,
;;      :path [:b],
;;      :predicate #&quot;^[abr]{0,3}$&quot;,
;;      :valid? &quot;bar&quot;}]</code></pre><p>Using bare regexes in your scalar specification has a nice side benefit in that the <code>data-from-spec</code> and <code>exercise-fn</code> utilities can generate valid strings.</p><p>Instead of storing specifications in a dedicated <a href="https://clojure.org/guides/spec#_registry">registry</a>,  Speculoos takes a <em>laissez-faire</em> approach: specifications may live directly in whatever namespace you please. If you feel that some sort of registry would be useful, you could make your own <a href="https://github.com/clojure/spec.alpha/blob/c630a0b8f1f47275e1a476dcdf77507316bad5bc/src/main/clojure/clojure/spec/alpha.clj#L52">modeled after</a> <code>spec.alpha</code>&apos;s.</p><p>Finally, when checking function correctness, <a href="#fn-correctness">validating the relationship</a> between the function&apos;s arguments and the function&apos;s return value uses a function that kinda looks like a predicate. That relationship-checking function accepts exactly two elements: the function&apos;s argument sequence and the function&apos;s return value.</p></section><section id="non-terminating-sequences"><h2>Non-terminating sequences</h2><p>Speculoos absorbs lots of power from Clojure&apos;s infinite, lazy sequences. That power stems from the fact that Speculoos only validates complete pairs of datums and predicates. Datums without predicates are not validated, and predicates without datums are ignored. That policy provides optionality in your data. If a datum is present, it is validated against its corresponding predicate, but if that datum is non-existent, it is not required.</p><pre><code>;; un-paired scalar predicates</code><br /><code>(validate-scalars [42]
&nbsp;                 [int? keyword? char?])
;; =&gt; [{:datum 42,
;;      :path [0],
;;      :predicate int?,
;;      :valid? true}]</code><br /><br /><code>;; un-paired scalar datums</code><br /><code>(validate-scalars [42 :foo \z]
&nbsp;                 [int?])
;; =&gt; [{:datum 42,
;;      :path [0],
;;      :predicate int?,
;;      :valid? true}]</code></pre><p>In the first example, only the single integer <code>42</code> is validated, the rest of the predicates are ignored. In the second example, only the <code>42</code>  was validated because the specification implies that any trailing elements are un-specified. We can take advantage of this fact by intentionally making either the data or the specification <em>run off the end</em>.</p><p>First, if you&apos;d like to validate a non-terminating sequence, specify as many datums as necessary to capture the pattern. <code>repeat</code> produces multiple instances of a single value, so we only need to specify one datum.</p><pre><code>(validate-scalars (repeat 3)
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
;;      :valid? true}]</code></pre><p>Three unique datums. Only three predicates needed.</p><p>On the other side of the coin, non-terminating sequences serve a critical role in composing Speculoos specifications. They express <em>I don&apos;t know how many items there are in this sequence, but they all must satisfy these predicates</em>.</p><pre><code>(valid-scalars? [1] (repeat int?)) ;; =&gt; true</code><br /><code>(valid-scalars? [1 2] (repeat int?)) ;; =&gt; true</code><br /><code>(valid-scalars? [1 2 3] (repeat int?)) ;; =&gt; true</code><br /><code>(valid-scalars? [1 2 3 4] (repeat int?)) ;; =&gt; true</code><br /><code>(valid-scalars? [1 2 3 4 5] (repeat int?)) ;; =&gt; true</code></pre><p>Basically, this idiom serves the role of a regular expression <code>zero-or-more</code>. Let&apos;s pretend we&apos;d like to validate an integer, then a string, followed by any number of characters. We compose our specification like this.</p><pre><code>;; use `concat` to append an infinite sequence of `char?`</code><br /><code>(validate-scalars [99 &quot;abc&quot; \x \y \z]
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
;;      :valid? true}]</code><br /><br /><code>(require &apos;[speculoos.core :refer [only-invalid]])</code><br /><br /><code>;; string &quot;y&quot; will not satisfy scalar predicate `char?`; use `only-valid` to highlight invalid element</code><br /><code>(only-invalid (validate-scalars [99 &quot;abc&quot; \x &quot;y&quot; \z]
&nbsp;                               (concat [int? string?] (repeat char?))))
;; =&gt; ({:datum &quot;y&quot;,
;;      :path [3],
;;      :predicate char?,
;;      :valid? false})</code></pre><p>Or perhaps we&apos;d like to validate a function&apos;s argument list composed of a ratio followed by <code>&amp;-args</code> consisting of any number of alternating keyword-string pairs.</p><pre><code>;; zero &amp;-args</code><br /><code>(valid-scalars? [2/3]
&nbsp;               (concat [ratio?] (cycle [keyword string?])))
;; =&gt; true</code><br /><br /><code>;; two pairs of keyword+string optional args</code><br /><code>(valid-scalars? [2/3 :opt1 &quot;abc&quot; :opt2 &quot;xyz&quot;]
&nbsp;               (concat [ratio?] (cycle [keyword string?])))
;; =&gt; true</code><br /><br /><code>;; one pair of optional args; &apos;foo does not satisfy `string?` scalar predicate</code><br /><code>(only-invalid (validate-scalars [2/3 :opt1 &apos;foo]
&nbsp;                               (concat [ratio?] (cycle [keyword string?]))))
;; =&gt; ({:datum foo,
;;      :path [2],
;;      :predicate string?,
;;      :valid? false})</code></pre><p>Using non-terminating sequences this way sorta replicates <code>spec.alpha</code>&apos;s sequence regexes. I think of it as Speculoos&apos; super-power.</p><p>Also, Speculoos can handle nested, non-terminating sequences.</p><pre><code>(valid-scalars? [[1] [2 &quot;2&quot;] [3 &quot;3&quot; :3]]
&nbsp;               (repeat (cycle [int? string? keyword?])))
;; =&gt; true</code></pre><p>This specification is satisfied with a <em>Possibly infinite sequence of arbitrary-length vectors, each vector containing a pattern of an integer, then a string, followed by a keyword</em>.</p><p>One detail that affects usage: A non-terminating sequence must not appear at the same path within both the data and specification. I am not aware of any method to inspect a sequence to determine if it is infinite, so Speculoos will refuse to validate a non-terminating data sequence at the same path as a non-terminating predicate sequence, and <em>vice versa</em>. However, feel free to use them in either data or in the specification, as long as they live at different paths.</p><pre><code>;; data&apos;s infinite sequence at :a, specification&apos;s infinite sequence at :b</code><br /><code>(valid-scalars? {:a (repeat 42), :b [22/7 true]}
&nbsp;               {:a [int?], :b (cycle [ratio? boolean?])})
;; =&gt; true</code><br /><br /><code>;; demo of some invalid scalars</code><br /><code>(only-invalid (validate-scalars {:a (repeat 42), :b [22/7 true]}
&nbsp;                               {:a [int? int? string?], :b (repeat ratio?)}))
;; =&gt; ({:datum 42,
;;      :path [:a 2],
;;      :predicate string?,
;;      :valid? false}
;;     {:datum true,
;;      :path [:b 1],
;;      :predicate ratio?,
;;      :valid? false})</code></pre><p>In both cases above, the data contains a non-terminating sequence at key <code>:a</code>, while the specification contains a non-terminating sequence at key <code>:b</code>. Since in both cases, the two inifinite sequences do not share a path, validation can proceed to completion.</p><p>So what&apos;s going on? Internally, Speculoos finds all the potentially non-terminating sequences in both the data and the specification. For each of those hits, Speculoos looks into the other nested structure to determine how long the counterpart sequence is. Speculoos then <em>clamps</em> the non-terminating sequence to that length. Validation proceeds with the clamped sequences. Let&apos;s see the clamping in action.</p><pre><code>(require &apos;[speculoos.core :refer [expand-and-clamp-1]])</code><br /><br /><code>(expand-and-clamp-1 (range) [int? int? int?]) ;; =&gt; [0 1 2]</code></pre><p><code>range</code> would have continued merrily on forever, but the clamp truncated it at three elements, the length of the second argument vector. That&apos;s why two non-terminating sequences at the same path are not permitted. Speculoos has no way of knowing how short or long the sequences ought to be, so instead of making a bad guess, it throws the issue back to us. The way <code>we</code> indicate how long it should be is by making the counterpart sequence a specific length. Where should Speculoos clamp that <code>(range)</code> in the above example? The answer is the length of the other sequential thing <code>[int? int? int?]</code>, or three elements.</p><p>Speculoos&apos; <a href="#utility"><code>utility</code></a> namespace provides a <code>clamp-in*</code> tool for you to clamp any sequence within a homogeneous, arbitrarily-nested data structure. You invoke it with a pattern of arguments similar to <code>clojure.core/assoc-in</code>.</p><pre><code>(require &apos;[speculoos.utility :refer [clamp-in*]])</code><br /><br /><code>(clamp-in* {:a 42, :b [&apos;foo 22/7 {:c (cycle [3 2 1])}]}
&nbsp;          [:b 2 :c]
&nbsp;          5)
;; =&gt; {:a 42, :b [foo 22/7 {:c [3 2 1 3 2]}]}</code></pre><p><code>clamp-in*</code> used the path <code>[:b 2 :c]</code> to locate the non-terminating <code>cycle</code> sequence, clamped it to <code>5</code> elements, and returned the new data structure with that terminating sequence. This way, if Speculoos squawks at you, you have a way to clamp the data, specification, or both at any path, and validation can proceed.</p><p>Be sure to set your development environment&apos;s printing length</p><pre><code>(set! *print-length* 99) ;; =&gt; 99</code></pre><p>or you may jam up your session.</p></section><section id="sets"><h2>Sets</h2><p>Sets are…a handful. They enable some nice features, but they present some unique challenges compared to the other Clojure collections. <em>The elements in a set are addressed by their identities.</em> What does that even mean? Let&apos;s compare to Clojure&apos;s other collections to get some context.</p><p>The elements of a sequence are addressed by monotonically increasing integer indexes. Give a vector index <code>2</code> and it&apos;ll give you back the third element, if it exists.</p><pre><code>([11 22 33] 2) ;; =&gt; 33</code></pre><p>The elements of a map are addressed by its keys. Give a map a key <code>:howdy</code> and it&apos;ll give you back the value at that key, if it exists.</p><pre><code>({:hey &quot;salut&quot;, :howdy &quot;bonjour&quot;} :howdy) ;; =&gt; &quot;bonjour&quot;</code></pre><p>Give a set some value, and it will give you back that value…</p><pre><code>(#{:index :middle :pinky :ring :thumb} :thumb) ;; =&gt; :thumb</code></pre><p>…but only if that element exists in the set.</p><pre><code>(#{:index :middle :pinky :ring :thumb} :bird) ;; =&gt; nil</code></pre><p>So the <a href="#path">paths</a> to elements of vectors, lists, and maps are composed of indexes or keys. The paths to members of a set are the thing themselves. Let&apos;s take a look at a couple of examples.</p><pre><code>(all-paths #{:foo 42 &quot;abc&quot;})
;; =&gt; [{:path [], :value #{42 :foo &quot;abc&quot;}}
;;     {:path [&quot;abc&quot;], :value &quot;abc&quot;}
;;     {:path [:foo], :value :foo}
;;     {:path [42], :value 42}]</code></pre><p>In the first example, the root element, a set, has a path <code>[]</code>. The remaining three elements, direct descendants of the root set have paths that consist of themselves. We find <code>42</code> at path <code>[42]</code> and so on. The second example applies the principle further.</p><pre><code>(all-paths #{11 {:a [22 #{33}]}})
;; =&gt; [{:path [], :value #{11 {:a [22 #{33}]}}}
;;     {:path [{:a [22 #{33}]}], :value {:a [22 #{33}]}}
;;     {:path [{:a [22 #{33}]} :a], :value [22 #{33}]}
;;     {:path [{:a [22 #{33}]} :a 0], :value 22}
;;     {:path [{:a [22 #{33}]} :a 1], :value #{33}}
;;     {:path [{:a [22 #{33}]} :a 1 33], :value 33}
;;     {:path [11], :value 11}]</code></pre><p>How would we navigate to that <code>33</code>? Again the root element set has a path <code>[]</code>. There are two direct descendants of the root set: <code>11</code> and a map. We&apos;ve already seen that the integer&apos;s path is the value of the integer. The path to the map is the map itself, which appears as the first element of its path. That path may look unusual, but Speculoos handles it without skipping a beat.</p><p>Let&apos;s borrow a function from the <a href="https://github.com/blosavio/fn-in">fn-in project</a> to zoom in on what&apos;s going on. The first argument is our example set. The second argument is a path. We&apos;ll build up the path to <code>33</code> piece by piece.</p><pre><code>(require &apos;[fn-in.core :refer [get-in*]])</code><br /><br /><code>(get-in* #{11 {:a [22 #{33}]}}
&nbsp;        [{:a [22 #{33}]}]) ;; =&gt; {:a [22 #{33}]}</code></pre><p>The map has one <code>MapEntry</code>, key <code>:a</code>, with an associated value, a two-element vector <code>[22 #{33}]</code>. A map value is addressed by its key, so the vector&apos;s path contains that key. Its path is that of its parent, with its key appended.</p><pre><code>(get-in* #{11 {:a [22 #{33}]}}
&nbsp;        [{:a [22 #{33}]} :a]) ;; =&gt; [22 #{33}]</code></pre><p>Paths into a vector are old hat by now. Our <code>33</code> is in a set at the second position, index <code>1</code> in zero-based land, which we append to the path.</p><pre><code>(get-in* #{11 {:a [22 #{33}]}}
&nbsp;        [{:a [22 #{33}]} :a 1]) ;; =&gt; #{33}</code></pre><p>We&apos;ve now arrived at the little nested set which holds our <code>33</code>. Items in a set are addressed by their identity, and the identity of <code>33</code> is <code>33</code>. So we append that to the path so far.</p><pre><code>(get-in* #{11 {:a [22 #{33}]}}
&nbsp;        [{:a [22 #{33}]} :a 1 33]) ;; =&gt; 33</code></pre><p>And now we&apos;ve finally fished out our <code>33</code>. Following this algorithm, we can get, change, and delete any element of any heterogeneous, arbitrarily-nested data structure, and that includes sets at any level of nesting. We could even make a path to a set, nested within a set, nested within a set.</p><p>When using Speculoos, we encounter sets in three scenarios. Well briefly sketch the three scenarios, then later go into the details.</p><ol><li><em>Scalar validation, scalar in data, set in specification.</em><pre><code>(validate-scalars [42 :red]
&nbsp;                 [int? #{:red :green :blue}])</code></pre><p>In this scenario, we&apos;re validating scalars, so we&apos;re using a function with <code>scalar</code> in its name. We&apos;ll be testing properties of a scalar, in this example, the second element of a vector the keyword <code>:red</code>. The set in the specification is a predicate-like thing that tests membership.</p></li><li><em>Scalar validation, set in data, set in specification.</em><pre><code>(validate-scalars [42 #{:chocolate :vanilla :strawberry}]
&nbsp;                 [int? #{keyword?}])</code></pre><p>In this scenario, we&apos;re validating scalars, so we&apos;re using a scalar validation function, again <code>validate-scalars</code>. But this time, we&apos;re validating scalars <em>contained within a set</em> in the data, with scalar predicates contained within a set in the specification.</p></li><li><em>Collection validation, set in data, set in specification.</em><pre><code>(validate-collections [42 #{:puppy :kitten :goldfish}]
&nbsp;                     [vector? #{set?}])</code></pre><p>In this scenario, we&apos;re validating some property of a collection, so we&apos;re using <code>validate-collections</code>. Collection predicates — targeting the nested set in the data — are themselves contained in a set nested in the collection specification.</p></li></ol><h3>1. Set as Scalar Predicate</h3><p>Let&apos;s remember back to the beginning of this section where we saw that Clojure sets can serve as membership tests. Speculoos can therefore use sets as a nice shorthand for a membership predicate.</p><pre><code>(def color? #{:red :green :blue})</code><br /><br /><code>(ifn? color?) ;; =&gt; true</code><br /><br /><code>(color? :red) ;; =&gt; :red</code><br /><br /><code>(color? :plaid) ;; =&gt; nil</code></pre><p><code>color?</code> implements <code>IFn</code> and thus behaves like a predicate when invoked as a function. <code>:red</code> satisfies our <code>color?</code> predicate and returns a truthy value, whereas <code>:plaid</code> does not and returns a falsey value.</p><p>During scalar validation, when a scalar in our data shares a path with a set in the specification, Speculoos enters <em>set-as-a-predicate</em> mode. (<em>Mode</em> only in the casual sense.  There are no modes nor states. The algorithm merely branches to treat the set differently depending on the scenario.) We&apos;ll make our specification mimic the shape of our data, but instead of two predicate functions, we&apos;ll insert one predicate function, followed by a set, which behaves like a function.</p><pre><code>;; data</code><br /><code>(all-paths [42 :red])
;; =&gt; [{:path [], :value [42 :red]}
;;     {:path [0], :value 42}
;;     {:path [1], :value :red}]</code><br /><br /><code>;; scalar specification</code><br /><code>(all-paths [int? #{:red :green :blue}])
;; =&gt; [{:path [], :value [int? #{:blue :green :red}]}
;;     {:path [0], :value int?}
;;     {:path [1], :value #{:blue :green :red}}
;;     {:path [1 :green], :value :green}
;;     {:path [1 :red], :value :red}
;;     {:path [1 :blue], :value :blue}]</code></pre><p>Our example data contains two scalar datums: <code>42</code> in the first spot and <code>:red</code>  in the second. Each of those datums shares a path with a predicate in the scalar specification. The <code>42</code> is paired with the <code>int?</code> scalar predicate because they both share the path <code>[0]</code>. Both <code>:red</code> and <code>#{:red :green :blue}</code> share a path <code>[1]</code>, Speculoos regards it as a <em>set-as-a-scalar-predicate</em>. Let&apos;s run that validation now.</p><pre><code>(validate-scalars [42 :red]
&nbsp;                 [int? #{:red :green :blue}])
;; =&gt; [{:datum :red,
;;      :path [1],
;;      :predicate #{:blue :green :red},
;;      :valid? :red}
;;     {:datum 42,
;;      :path [0],
;;      :predicate int?,
;;      :valid? true}]</code></pre><p>When Speculoos validates scalars, it treats the set in the specification as a predicate because the corresponding element in the data is a scalar, not a set. In this example, <code>:red</code> is a member of the <code>#{:red :green :blue}</code> set-predicate.</p><p>The same principles hold when validating elements of a map with a set-predicate. When a set in the specification contains a set that shares a path with a scalar in the data, that set is treated as a membership predicate.</p><pre><code>(validate-scalars {:x 42, :y :red}
&nbsp;                 {:x int?, :y #{:red :green :blue}})
;; =&gt; [{:datum :red,
;;      :path [:y],
;;      :predicate #{:blue :green :red},
;;      :valid? :red}
;;     {:datum 42,
;;      :path [:x],
;;      :predicate int?,
;;      :valid? true}]</code></pre><p>Scalar <code>42</code> pairs with predicate <code>int?</code> at path <code>[:x]</code> and scalar <code>:red</code> pairs with set-predciate <code>#{:red :green :blue}</code> at path <code>[:y]</code>.</p><h3>2. Validate Scalars within Set</h3><p>Sometimes the scalars in our data are contained in a set. Speculoos can validate scalars within a set during a scalar validation operation. Validating a set&apos;s scalar members follows all the same principles as validating a vector&apos;s scalar members, except for one wrinkle: Since elements of a set have no inherent location, i.e., sets are unordered, sets in our data are validated against <em>all</em> predicates contained in the corresponding set at the same path in the specification. An example shows this better than words.</p><pre><code>;; data, some scalars are contained within a set</code><br /><code>(all-paths [42 #{:chocolate :vanilla :strawberry}])
;; =&gt; [{:path [], :value [42 #{:chocolate :strawberry :vanilla}]}
;;     {:path [0], :value 42}
;;     {:path [1], :value #{:chocolate :strawberry :vanilla}}
;;     {:path [1 :strawberry], :value :strawberry}
;;     {:path [1 :chocolate], :value :chocolate}
;;     {:path [1 :vanilla], :value :vanilla}]</code><br /><br /><code>;; scalar specification</code><code>(all-paths [int? #{keyword?}])
;; =&gt; [{:path [], :value [int? #{keyword?}]}
;;     {:path [0], :value int?}
;;     {:path [1], :value #{keyword?}}
;;     {:path [1 keyword?], :value keyword?}]</code></pre><p>Let&apos;s apply the Mantras. We intend to validate scalars, so we&apos;ll use <code>validate-scalars</code>, which only applies predicates to scalars. Next, we&apos;ll make our our specification mimic the shape of the data. In this example, both the data and the specification are a vector, with something in the first spot, and a set in the second spot. Finally, we&apos;ll make sure that all predicates are paired with a scalar.</p><pre><code>(validate-scalars [42 #{:glass :rubber :paper}]
&nbsp;                 [int? #{keyword?}])
;; =&gt; ({:datum 42,
;;      :path [0],
;;      :predicate int?,
;;      :valid? true}
;;     {:datums-set #{:glass :paper :rubber},
;;      :path [1],
;;      :predicate keyword?,
;;      :valid? true})</code></pre><p>First, notice how the scalar specification looks a lot like the data. Because the shapes are similar, <code>validate-scalars</code> is able to systematically apply predicates from the specification to scalars in the data. Speculoos validates <code>42</code> against predicate <code>int?</code> because they share paths in their respective vectors. At vector index <code>1</code> our data and specification both hold sets, so Speculoos enters <em>validate-scalars-within-a-set-mode</em>. Every predicate contained in the specification set is applied to every datum in the data&apos;s set. In this example, <code>keyword?</code> is individually applied to <code>:glass</code>, <code>:rubber</code>, and <code>:paper</code>, and since each satisfy the predicate, the validation returns <code>true</code>.</p><p>One of the defining features of Clojure sets is that they&apos;re amorphous bags of items, without any inherent ordereing. Within the conext of a set, it doesn&apos;t make sense to target one scalar predicate towards one particular scalar datum. Therefore, Speculoos validates scalars contained within a set more broadly. If our specification set contains more than one predicate, each of the predicates is applied to <em>all</em> the scalars in the data&apos;s set. In the next example, the specification set contains two predicates.</p><pre><code>(validate-scalars #{:chocolate}
&nbsp;                 #{keyword? qualified-keyword?})
;; =&gt; ({:datums-set #{:chocolate},
;;      :path [],
;;      :predicate qualified-keyword?,
;;      :valid? false}
;;     {:datums-set #{:chocolate},
;;      :path [],
;;      :predicate keyword?,
;;      :valid? true})</code></pre><p>Two scalar predicates in the specification applied to the one scalar datum. <code>:chocolate</code> is a keyword, but not a qualified keyword. Next, we&apos;ll see how to validate multiple scalars with multiple scalar predicates.</p><pre><code>(validate-scalars #{:chocolate :vanilla :strawberry}
&nbsp;                 #{keyword? qualified-keyword?})
;; =&gt; ({:datums-set #{:chocolate :strawberry :vanilla},
;;      :path [],
;;      :predicate qualified-keyword?,
;;      :valid? false}
;;     {:datums-set #{:chocolate :strawberry :vanilla},
;;      :path [],
;;      :predicate keyword?,
;;      :valid? true})</code></pre><p>Validation applies <code>keyword?</code> and <code>simple-keyword?</code>, in turn, to every scalar member of the data set. Speculoos tells us that all the scalars in the data are indeed keywords, but at least one of the data&apos;s scalars is not a qualified keyword. Notice how Speculoos condenses the validation results. Instead of a validation entry for each individual scalar in the data set, Speculoos combines all the results for all the scalars. Two scalar predicates, two validation results.</p><p>Again, the same principles apply for validating sets contained in a map.</p><pre><code>(validate-scalars {:x 42, :y #{&quot;a&quot; &quot;b&quot; &quot;c&quot;}}
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
;;      :valid? true})</code></pre><p>In this example, the predicate at index <code>0</code> of the specification is a set while the datum at same index of the data is <code>42</code>, a scalar. Speculoos uses the set-as-a-predicate mode. Since <code>42</code> is a member of <code>#{40 41 42}</code>, that datum validates as truthy. Because the data at index <code>1</code> is itself a set, Speculoos performs set-scalar-validation. The <code>keyword?</code> predicate is applied to each element of <code>#{:foo :bar :baz}</code> at index <code>1</code> and they all validate <code>true</code>.</p><h3>3. Validate Set as a Collection</h3><p>Let&apos;s discuss how collection validation works when a set is involved. During a collection validation operation, Speculoos will ignore all scalars in the data. It will only apply predicates to collections. The rules are identical to how the other collections are validated: predicates from the specification are applied to the parent container in the data. But let&apos;s not get bogged down in a textual description; let&apos;s look at some examples.</p><p>First, we&apos;ll start with some data that consists of a vector containing an integer, followed by a three element set. Let&apos;s generate all the paths.</p><pre><code>(all-paths [42 #{:puppy :kitten :goldfish}])
;; =&gt; [{:path [], :value [42 #{:goldfish :kitten :puppy}]}
;;     {:path [0], :value 42}
;;     {:path [1], :value #{:goldfish :kitten :puppy}}
;;     {:path [1 :puppy], :value :puppy}
;;     {:path [1 :goldfish], :value :goldfish}
;;     {:path [1 :kitten], :value :kitten}]</code></pre><p>Mantra #1: Collection validation ignores scalars, so out of all those elements, validation will only be considering the root at path <code>[]</code> and the nested set at path <code>[1]</code>.</p><p>A good strategy for creating a collection specification is to copy-paste the data and delete all the scalars…</p><pre><code>[        #{    }]</code></pre><p>…and insert some collection predicates near the opening bracket.</p><pre><code>[vector? #{set?}]</code></pre><p>Let&apos;s generate the paths for that collection specification.</p><pre><code>(all-paths [vector? #{set?}])
;; =&gt; [{:path [], :value [vector? #{set?}]}
;;     {:path [0], :value vector?}
;;     {:path [1], :value #{set?}}
;;     {:path [1 set?], :value set?}]</code></pre><p>Notice the paths to the two predicates. Now, let&apos;s run a collection validation.</p><pre><code>(validate-collections [42 #{:puppy :kitten :goldfish}]
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
;;      :valid? true})</code></pre><p><code>validate-collections</code> was able to pair two collections in the data with two predicates in the specification, and we received two validation results. Collection predicate <code>vector?</code> at path <code>[0]</code> in the specification was applied to whatever is at path <code>(drop-last [0])</code> in the data, which happens to be the root collection. Collection predicate <code>set?</code> at path <code>[1 set?]</code> in the specification was applied to path <code>(drop-last [1 set?])</code> in the data, which happens to be our nested set containg pet keywords.</p><p>Remember: Scalar predicates apply to the scalar at their exact location. Collection predicates apply to the collection directly hugging them.</p></section><section id="troubleshooting"><h2>Troubleshooting</h2><p>If you see surprising results, try these ideas.</p><ul><li><p>Remember the Mantras, and follow them.<ol><li><strong>Validate scalars separately from validating collections.</strong><p>You should never have a collection predicate like <code>vector?</code> in a scalar specification. Similarly, scalar predicates like <code>int?</code> should only appear in a collection specification in the context of testing a collection, like…</p><pre><code>(defn all-ints? [v] (every? #(int? %) v))</code></pre><p>…or when validating some relationship <em>between</em> datums, like this.</p><pre><code>(defn b-greater-than-a? [m] (&lt; (m :a) (m :b)))</code></pre><p>The function names <code>validate-scalars</code>, <code>validate-collections</code>, et. al., are strong beacons to remind you that you&apos;re either validating scalars, or validating collections.</p></li><li><strong>Make the specification mimic the shape of the data.</strong><p>The speculoos functions don&apos;t enforce any requirements on the data and specification. If you feed it data that&apos;s a map and a specification that&apos;s a vector, it will dutifully try to validate what it has.</p><pre><code>(validate-scalars {:a 99}
&nbsp;                 [int?]) ;; =&gt; []</code><br /><br /><code>;; No error nor exception with map data and vector specification</code></pre><p>One word of warning: Because sequential things are indexed by integers, and map elements may also be indexed by integers, you could certainly abuse that flexibility like this.</p><pre><code>;; data is a vector, specification is a map keyed with integers</code><br /><br /><code>(validate-scalars [42 &quot;abc&quot; 22/7]
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
;;      :valid? true}]</code></pre><p>Speculoos merely knows that it could successfully locate <code>42</code> and <code>int?</code> at <code>0</code>, etc. It &apos;worked&apos; in this instance, but surprise lurks if you try to get to clever.</p></li><li><strong>Validation ignores un-paired predicates and un-paired datums.</strong><p>A decent number of surprsing validations result from predicates pairing to unexpected datums or not being paired at all.</p><pre><code>;; Oops! specification contains un-paired key :c; string &quot;abc&quot; isn&apos;t validated</code><br /><code>(valid-scalars? {:a 42, :b &quot;abc&quot;}
&nbsp;               {:a int?, :c symbol?})
;; =&gt; true</code><br /><br /><br>;; Oops! specification uses an extra level of nesting; [33] wasn&apos;t validated</br><code>(validate-collections [11 [22 [33]]] [[[[list?]]]]) ;; =&gt; ()</code></pre><p>Corollary: <strong><code>valid?</code> being <code>true</code> means there were zero non-true results.</strong> If the validation did not find any predicate+datum pairs, there would be zero invalid results, and thus return valid. Use the <code>thorough-…</code> function variants to require all datums to be validated.</p><p>See below for strategies and tools for diagnosing mis-pairing.</p></li></ol></p></li><li><p>Speculoos specifications are regular old data structures containing regular old functions. (I assume your data is, too.) If you&apos;re wrangling with something deep down in some nested mess, use your Clojure powers to dive in and pull out the relevant pieces.</p><pre><code>(let [data (get-in {:a {:b {:c [22/7]}}} [:a :b :c])
&nbsp;     spec (get-in {:a {:b {:c [int?]}}} [:a :b :c])]
&nbsp; (validate-scalars data spec))
;; =&gt; [{:datum 22/7,
;;      :path [0],
;;      :predicate int?,
;;      :valid? false}]</code></pre></li><li><p>Use the verbose functions. If you&apos;re using the high-level <code>valid-…?</code> function variants, you&apos;ll only see <code>true/false</code>, which isn&apos;t helpful when troubleshooting. The <code>validate-…</code> variants are chatty and will display everything it considered during validation.</p></li><li><p>The <a href="https://blosavio.github.io/speculoos/speculoos.utility.html"><code>speculoos.utility</code></a> namespace provides many functions for creating, viewing, analyzing, and modifying both scalar and collection specifications.</p></li><li><p>When the going really gets tough, break out <code>speculoos.core/all-paths</code> and apply it to your data, then to your specification, and then step through the validtion with your eyes.</p><pre><code>(all-paths {:a [99]})
;; =&gt; [{:path [], :value {:a [99]}}
;;     {:path [:a], :value [99]}
;;     {:path [:a 0], :value 99}]</code><br /><br /><code>(all-paths {:a &apos;int?})
;; =&gt; [{:path [], :value {:a int?}}
;;     {:path [:a], :value int?}]</code><br /><br /><code>;; Aha! The predicate int? at path [:a] and the integer 99 at path [:a 0] do not share a path!</code></pre></li></ul><p>Finally, if you hit a wall, file a <a href="https://github.com/blosavio/speculoos/issues">bug report</a> or <a href="https://github.com/blosavio"> email me</a>.</p></section><section><h2 id="case-study">Case Study</h2><p>Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.</p></section><section id="alternatives"><h2>Alternatives</h2><ul><li>Staples SparX <a href="https://github.com/staples-sparx/clj-schema">clj-schema</a><p>Schemas for Clojure data structures and values. Delineates operations on maps, seqs, and sets. Contributors: Alex Baranosky, Laurent Petit, Punit Rathore</p><br /></li><li>Steve Miner&apos;s <a href="https://github.com/miner/herbert">Herbert</a><p>A schema language for Clojure data for documenting and validating.</p><br /></li><li>Metosin <a href="https://github.com/metosin/malli">Malli</a><p>Data-driven schemas incorporating the best parts of existing libs, mixed with their own tools.</p><br /></li><li>Plumatic <a href="https://github.com/plumatic/schema">Schema</a><p>A Clojure(Script) library for declarative data description and validation.</p><br /></li><li>Christophe Grand&apos;s <a href="https://github.com/cgrand/seqexp">seqexp</a><p>Regular expressions for sequences (and other sequables).</p><br /></li><li>Jonathan Claggett&apos;s <a href="https://github.com/jclaggett/seqex">seqex</a><p>Sequence Expressions, similar to regular expressions but able to describe arbitrary sequences of values (not just characters).</p><br /></li><li>Clojure&apos;s <a href="https://github.com/clojure/spec.alpha"><code>spec.alpha</code></a><p>[A] Clojure library to describe the structure of data and functions.</p><br /></li><li>Clojure&apos;s <a href="https://github.com/clojure/spec-alpha2"><code>spec-alpha2</code> or <code>alpha.spec</code></a><p>[A]n evolution from spec.alpha as well as work towards several new features. Note: Alex Miller considers it <a href="https://ask.clojure.org/index.php/9397/clarify-usage-on-the-spec-alpha2-github-page?show=9398#a9398">a work in progress</a> as of 2020 June 20.</p><br /></li><li>Jamie Brandon&apos;s <a href="https://github.com/jamii/strucjure">Strucjure</a><p>A <a href="https://www.scattered-thoughts.net/writing/strucjure-motivation/">library for describing stuff</a> in an executable manner.</p><br /></li><li>Brian Marick&apos;s <a href="https://github.com/marick/structural-typing">structural-typing</a><p>A library that provides good error messages when checking the correctness of structures, and a way to define <a href="https://en.wikipedia.org/wiki/Structural_type_system">structural types</a> that are checked at runtime.</p><br /></li><li>Peter Taoussanis&apos; <a href="https://github.com/taoensso/truss">Truss</a><p>A tiny library that provides fast and flexible runtime assertions with terrific error messages.</p><br /></li></ul></section><section id="glossary"><h2>Glossary</h2><dl><dt id="element">element</dt><dd>A thing contained within a collection, either a scalar value or another nested collection.</dd><dt id="HANDS">heterogeneous, arbitrarily-nested data structure</dt><dd>Exactly one Clojure collection (vector, map, list, sequence, or set) with zero or more <a href="#element">elements</a>, nested to any depth.</dd><dt id="non-term-seq">non-terminating sequence</dt><dd>One of <code>clojure.lang.{Cycle,Iterate,LazySeq,LongRange,Range,Repeat}</code> that may or may not be realized, and possibly infinite. (I am not aware of any way to determine if such a sequence is infinite, so Speculoos treats them as if they are.)</dd><dt id="path">path</dt><dd><p>A series of values that unambiguously navigates to a single <a href="#element">element</a> (scalar or sub-collection) in a <a href="#HANDS">heterogeneous, arbitrarily-nested data structure</a>. In the context of the Speculoos library, the series of values comprising a path is generated by the <code>all-paths</code> function and consumed by the <code>validate-…</code> functions. Almost identical to the second argument of <a href="https://clojure.github.io/clojure/clojure.core-api.html#clojure.core/get-in"><code>clojure.core/get-in</code></a>, but with more generality.</p><p>Elements of vectors, lists, and other sequences are addressed by zero-indexed integers. Map values are addressed by their keys, which are often keywords, but can be any data type, including integers, or composite types. Set members are addressed by their identities. Nested collections contained in a set can indeed be addressed: the path vector itself contains the collections. An empty vector <code>[]</code> addresses the outermost, containing collection.</p></dd><dt id="predicate">predicate</dt><dd>A function, or something that implements <code>IFn</code>, like a set, that returns a truthy or falsey value.  In the vast majority of instances, a function of one argument, but in certain corners of Speculoos, such as in argument-vs-return specifications, the function consumes more than one argument. Some Speculoos functions, such as <code>validate-scalars</code> and <code>valid-scalars?</code> also regard a regular expression as a competant predicate.</dd><dt id="scalar">scalar</dt><dd>A single, non-divisible datum, such as an integer, string, boolean, etc. Essentially, a shorter term for <em>non-collection</em>.</dd><dt id="specification">specification</dt><dd>A human- and machine- readable declaration about properties of data, composed of a <a href="#HANDS">heterogeneous, arbitrarily-nested data collection</a> and <a href="#predicate">predicates</a>.</dd><dt id="validate">validate</dt><dd>An action that returns an exhaustive listing of all datum+predicate pairs, their paths, and whether the datum satisfies the predicate. Note: Validation requires <em>two</em> components, a datum and a predicate. Any unpaired datum or any unpaired predicate, will not participate in validation.</dd><dt id="valid">valid?</dt><dd>An action that returns <code>true</code> if all paired datums satisfy their predicates during a validation, <code>false</code> otherwise. Note: A validation operation&apos;s result is considered <em>valid</em> if there are zero datum+predicates.</dd></dl></section><br /><h2>License</h2><p>This program and the accompanying materials are made available under the terms of the <a href="https://opensource.org/license/MIT">MIT License</a>.</p>