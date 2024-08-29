<a href="https://clojars.org/com.sagevisuals/speculoos"><img src="https://img.shields.io/clojars/v/com.sagevisuals/speculoos.svg" /></a><br /><a href="#setup">Setup</a><br /><a href="https://blosavio.github.io/speculoos/index.html">API</a><br /><a href="#mantras">Mantras</a><br /><a href="#function-validation">Validating Functions</a><br /><a href="https://github.com/blosavio">Contact</a><br /><h1>Speculoos</h1><em>An experiment with Clojure specification literals</em><br /><section id="setup"><h2>Setup</h2><h3>Leiningen/Boot</h3><pre><code>[com.sagevisuals/speculoos &quot;2&quot;]</code></pre><h3>Clojure CLI/deps.edn</h3><pre><code>com.sagevisuals/speculoos {:mvn/version &quot;2&quot;}</code></pre><h3>Require</h3><pre><code>(require &apos;[speculoos.core :refer [valid-scalars? valid-collections?]])</code></pre></section><section id="mantras"><h2>Mantras</h2><p>When using Speculoos, remember these three Mantras:<ol><li>Validate scalars separately from validating collections.</li><li>Shape the specification to mimic the data.</li><li>Ignore un-paired predicates and un-paired datums.</li></ol></p><p>Speculoos provides functions for validating scalars contained within a heterogeneous, arbitrarily-nested data structure, and another, distinct set of functions for validating properties of those nested collections. Validating scalars separately from validating collections carries several advantages. First, Speculoos can consume specifications composed of regular Clojure data structures. Inspect and manipulate your specification with any Clojure collection-handling functions you prefer. Second, separating the two offers mental clarity about what&apos;s going on. Your predicates will only ever apply to a scalar, or to a collection, never both. Third, you only need to specify as much, or little, as necessary. If you only want to validate a few scalars, you won&apos;t be forced to specify anything converning a collection.</p><p>Speculoos aims to make composing specifications straightforward, and inspecting them transparent. A Speculoos specification is merely an arrangement of nested vectors, lists, maps, sequences, and sets that contain predicates. Those predicates are arranged in a pattern that instruct the validation functions where to apply the predicates. The specification for a vector is a vector. The specification for a map, is itself a map. There&apos;s a nearly one-to-one correspondence between the shape of the data and the shape of the specification. Speculoos specifications aim to be intuitive to peek at by eye, but also amenable to alteration. You can use your favorite Clojure data wrangling functions to tighten, relax, or remove portions of a Speculoos specification.</p><p>Speculoos provides flexibility, power, and reusability of specifications by ignoring datums that do not have a corresponding predicate in the specification and ignoring predicates that do not have a corresponding datum in the data. Maybe in your role in an assembly line, you only care about some slice of a large chunk of data. Supplying predicates for only a subset of datums allows you to only validate those specified datums while being agnostic towards the other datums. Going in the other direction, maybe somebody shared a giant specification that describes data about a person, their postal address, their contact info, etc. Because a Speculoos specification is just a data structure with regular predicates, you can, on-the-fly, pull out the portion relevent to postal addresses and apply that to your instances of address data. Speculoos lets you specify exactly what elements you&apos;d like to validate. No more, no less.</p></section><section id="function-validation"><h2>Specifying and Validating Functions</h2><p>Being able to validate Clojure data enables us to check the usage and behavior of functions.</p><ol><li><strong>Validating arguments</strong> Speculoos can validate any property of the arguments passed to a function when it is invoked. We can ask questions like <em>Is the argument passed to the function a number?</em>, a scalar validation, and <em>Are there an even number of arguments?</em>, a collection validation.</li><li><strong>Validating return values</strong> Speculoos can validate any property of the value returned by a function. We can ask questions like <em>Does the function return a four-character string?</em>, a scalar validation, and <em>Does the function return a map containing keys <code>:x</code> and <code>:y</code></em>, a collection validation.</li><li><strong>Validating function correctness</strong> Speculoos can validate the correctness of a function in two ways.<ul><li>Speculoos can validate the <em>relationships</em> between the arguments and the function&apos;s return value. We can ask questions like <em>Is each of the three integers in the return value larger than the three integers in the arguments?</em>, a scalar validation, and <em>Is the return sequence the same length as the argument sequence, and are all the elements in reverse order?</em>, a collection validation.</li><li>Speculoos can <em>exercise</em> a function. This allows us to check <em>If we give this function one thousand randomly-generated valid inputs, does the function always produce a valid return value?</em> Exercising functions with randomly-generated samples is described in the </li><a href="#exercising">next section</a>.</ul></li></ol><p>None of those six checks are strictly required. Speculoos will happily validate using only the specifications we provide.</p><h3 id="fn-args">1. Validating Function Arguments</h3><p>When we invoke a function with a series of arguments, that series of values forms a sequence, which Speculoos can validate like any other heterogeneous, arbitrarily-nested data structure. Speculoos offers <a href="#explicit">a trio</a> of function-validating functions with differing levels of explicitness. We&apos;ll be primarily using <code>validate-fn-with</code> because it is the most explicit of the trio, and we can most easily observe what&apos;s going on.</p><p>Let&apos;s pretend we want to validate the arguments to a function <code>sum-three</code> that expects three integers and returns their sum.</p><pre><code>(require &apos;[speculoos.function-specs :refer [validate-fn-with]])</code><br /><br /><code>(defn sum-three [x y z] (+ x y z))</code><br /><br /><code>(sum-three 1 20 300) ;; =&gt; 321</code></pre><p>The argument list is a <em>sequence</em> of values, in this example, a sequential thing of three integers. We can imagine a scalar specification for just such as sequence.</p><pre><code>[int? int? int?]</code></pre><p>When using <code>validate-fn-with</code>, we supply the function name, a map containing zero or more specifications, and some trailing <code>&amp;-args</code> as if they had been supplied directly to the function. Speculoos can validate five aspects of a function using up to five specifications, each specification associated in that map to a particular key. We&apos;ll cover each of those five aspects in turn. To start, we want to specify the <em>argument scalars</em>.</p><p>Instead of individually passing each of those five specifications to <code>validate-fn-with</code> and putting <code>nil</code> placeholders where don&apos;t wish to supply a specification, we organize the specifications. To do so, we associate the arguments&apos; scalar specification to the qualified key <code>:speculoos/arg-scalar-spec</code>.</p><pre><code>{:speculoos/arg-scalar-spec [int? int? int?]}</code></pre><p>Then, we validate the arguments to <code>sum-three</code> like this.</p><pre><code>(validate-fn-with sum-three
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
;;      :valid? false})</code></pre><p>…validating scalars with <code>validate-scalars</code> and keeping only the invalids.</p><p>Okay, we see that term <em>scalar</em> buzzing around, so there must be something else about validating collections. Yup. We can also validate collection properties of the argument sequence. Let&apos;s specify that the argument sequence must contain three elements with a custom collection predicate.</p><pre><code>(defn count-3? [v] (= 3 (count v)))</code></pre><p>Let&apos;s simulate the collection validation first. Remember, collection predicates are applied to their parent containers, so <code>count-3?</code> must appear within a collection so that it&apos;ll be paired with the data&apos;s containing collection.</p><pre><code>(validate-collections [1 20 30]
&nbsp;                     [count-3?])
;; =&gt; ({:datum [1 20 30],
;;      :ordinal-path-datum [],
;;      :path-datum [],
;;      :path-predicate [0],
;;      :predicate count-3?,
;;      :valid? true})</code></pre><p>That result fits with <a href="#validating-collections">our discussion</a> about validating collections.</p><p>Next, we&apos;ll associate that collection specification into our function specification map at <code>:speculoos/arg-collection-spec</code> and invoke <code>validate-fn-with</code> with three valid arguments.</p><pre><code>(validate-fn-with sum-three
&nbsp;                 {:speculoos/arg-collection-spec [count-3?]}
&nbsp;                 1
&nbsp;                 20
&nbsp;                 300)
;; =&gt; 321</code></pre><p>The argument sequence satisfies our collection specification, so <code>sum-three</code> returns the expected value. Now let&apos;s repeat, but with an additional argument that causes the argument list to violate its collection predicate.</p><pre><code>(validate-fn-with sum-three
&nbsp;                 {:speculoos/arg-collection-spec [count-3?]}
&nbsp;                 1 20
&nbsp;                 300 4000)
;; =&gt; ({:datum [1 20 300 4000],
;;      :fn-spec-type :speculoos/argument,
;;      :ordinal-path-datum [],
;;      :path-datum [],
;;      :path-predicate [0],
;;      :predicate count-3?,
;;      :valid? false})</code></pre><p>This latest argument list <code>[1 20 300 4000]</code> failed to satisfy our <code>count-3?</code> collection predicate, so <code>validate-fn-with</code> emitted a validation report.</p><p>Note: Don&apos;t specify and validate the <em>type</em> of the argument container, i.e., <code>vector?</code>. That&apos;s an implementation detail and not guaranteed.</p><p>Let&apos;s get fancy and combine an argument scalar specification and an argument collection specification. Outside of the context of checking a function, that <a href="#valid-thorough">combo validation</a> would look like this.</p><pre><code>(speculoos.core/only-invalid
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
;;      :valid? false})</code></pre><p>Let&apos;s remember: scalars and collections are <em>always</em> validated separately. <code>validate</code> is merely a convenience function that does both a scalar validation, then a collection validation, in discrete processes, with a single function invocation. Each of the first three scalars that paired with a scalar predicate were validated as scalars. The first and third scalars failed to satisfy their respective predicates. The fourth argument, <code>4000</code>, was not paired with a scalar predicate and was therefore ignored. Then, the argument sequence as a whole was validated against the collection predicate <code>count-3?</code>.</p><p><code>validate-fn-with</code> performs substantially that combo validation. We&apos;ll associate the <strong>arg</strong>ument <strong>scalar</strong> <strong>spec</strong>ification with <code>:speculoos/arg-scalar-spec</code> and the <strong>arg</strong>ument <strong>collection</strong> <strong>spec</strong>fication with <code>:speculoos/arg-collection-spec</code> and pass the invalid argument sequence.</p><pre><code>(validate-fn-with sum-three
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
;;      :valid? false})</code></pre><p>Just as in the <code>validate</code> simulation, we see three items fail to satisfy their predicates. Scalars <code>1.0</code> and <code>22/7</code> are not integers, and the argument sequence as a whole, <code>[1.0 20 22/7 4000]</code>, does not contain exactly three elements as its collection predicate requires.</p><h3 id="fn-ret">2. Validating Function Return Values</h3><p>Speculoos can also validate values returned by a function. Reusing our <code>sum-three</code> function, and going back to valid inputs, we can associate a <strong>ret</strong>urn <strong>scalar</strong> <strong>spec</strong>ification into <code>validate-fn-with</code>&apos;s specification map to key <code>:speculoos/ret-scalar-spec</code>. Let&apos;s stipulate that the function returns an integer. Here&apos;s how we pass that specification to <code>validate-fn-with</code>.</p><pre><coce>{:speculoos/ret-scalar-spec int?}</coce></pre><p>And now, the function validation.</p><pre><code>(validate-fn-with sum-three
&nbsp;                 {:speculoos/ret-scalar-spec int?}
&nbsp;                 1
&nbsp;                 20
&nbsp;                 300)
;; =&gt; 321</code></pre><p>The return value <code>321</code> satisfies <code>int?</code>, so <code>validate-fn-with</code> returns the computed sum.</p><p>What happens when the return value is invalid? Instead of messing up <code>sum-three</code>&apos;s defninition, we&apos;ll merely alter the scalar predicate. Instead of an integer, we&apos;ll stipulate that <code>sum-three</code> returns a string with scalar predicate <code>string?</code>.</p><pre><code>(validate-fn-with sum-three
&nbsp;                 {:speculoos/ret-scalar-spec string?}
&nbsp;                 1
&nbsp;                 20
&nbsp;                 300)
;; =&gt; ({:datum 321,
;;      :fn-spec-type :speculoos/return,
;;      :path nil,
;;      :predicate string?,
;;      :valid? false})</code></pre><p>Very nice. <code>sum-three</code> computed, quite correctly, the sum of the three arguments. But we gave it a bogus return scalar specification that claimed it ought to be a string, which integer <code>321</code> fails to satify.</p><p>Did you happen to notice the <code>path</code>? We haven&apos;t yet encountered a case where a path is <code>nil</code>. In this situation, the function returns a &apos;bare&apos; scalar, not contained in a collection. Speculoos can validate a bare scalar when that bare scalar is a function&apos;s return value.</p><p>Let&apos;s see how to validate a function when the return value is a collection of scalars. We&apos;ll write a new function that returns four scalars: the three arguments and their sum.</p><pre><code>(defn enhanced-sum-three [x y z] [x y z (+ x y z)])</code><br /><br /><code>(enhanced-sum-three 1 20 300) ;; =&gt; [1 20 300 321]</code></pre><p>Our enhanced function now returns a vector of four elements. Let&apos;s remind ourselves how we&apos;d manually validate that return value. If we decide we want <code>enhanced-sum-three</code> to return four integers, the scalar specification would look like this.</p><pre><code>[int? int? int? int?]</code></pre><p>And the manual validation would look like this.</p><pre><code>(validate-scalars [1 20 300 321]
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
;;      :valid? true}]</code></pre><p>Four paired scalars and scalar predicates yield four validaton results. Let&apos;s see what happens when we validate the function return scalars.</p><pre><code>(validate-fn-with enhanced-sum-three
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
;;      :valid? false})</code></pre><p><code>enhanced-sum-three</code>&apos;s function body remained the same, and we fed it the same integers as before, but we fiddled with the return scalar specification so that we got two invalid scalars. <code>1</code> at path <code>[0]</code> does not satisfy its wonky scalar predicate <code>char?</code> at the same path. And <code>321</code> at path <code>[3]</code> does not satisfy fraudulent scalar predicate <code>boolean?</code> that shares its path.</p><p>Let&apos;s set aside validating scalars for a moment and validate a facet of <code>enhanced-sum-three</code>&apos;s return collection. First, we&apos;ll do a manual demonstration with <code>validate-collections</code>. Let&apos;s remember: collection predicates apply to their immediate parent container. We wrote <code>enhanced-sum-three</code> to return a vector, but to make the validation produce something interesting to look at, we&apos;ll pretend we&apos;re expecting a list.</p><pre><code>(validate-collections [1 20 300 321]
&nbsp;                     [list?])
;; =&gt; ({:datum [1 20 300 321],
;;      :ordinal-path-datum [],
;;      :path-datum [],
;;      :path-predicate [0],
;;      :predicate list?,
;;      :valid? false})</code></pre><p>That collection validation aligns with our understanding. <code>[1 20 300 321]</code> is not a list. The <code>list?</code> collection predicate at path <code>[0]</code> in the specification was paired with the thing found at path <code>(drop-last [0])</code> in the data, which in this example is the root collection. We designed <code>enhanced-sum-three</code> to yield a vector.</p><p>Let&apos;s toss that collection specification at <code>validate-with-fn</code> and have it apply to <code>enhanced-sum-three</code>&apos;s return value, which won&apos;t satisfy. We pass the <strong>ret</strong>urn <strong>collection spec</strong>ification by associating it to the key <code>:speculoos/ret-collection-spec</code>.</p><pre><code>(validate-fn-with enhanced-sum-three
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
;;      :valid? false})</code></pre><p>Similarly to the manual collection validation we previously performed with <code>validate-collections</code>, we see that <code>enhanced-sum-three</code>&apos;s return vector <code>[1 20 300 321]</code> fails to satisfy its <code>list?</code> collection predicate.</p><p>A scalar validation followed by an independent collection validation allows us to check every possible aspect that we could want. Now we that we&apos;ve seen how to individually validate <code>enhance-sum-three</code>&apos;s return scalars and return collections, we know how to do both with one invocation.</p><p>Remember Mantra #1: Validate scalars separately from validating collections. Speculoos will only ever do one or the other, but <code>validate</code> is a convenience function that performs a scalar validation immediately followed by a collection validation. We&apos;ll re-use the scalar specification and collection specification from the previous examples.</p><pre><code>(speculoos.core/only-invalid
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
;;      :valid? false})</code></pre><p><code>valiate-fn-with</code>&apos;s validation is substantially the same as the one <code>validate</code> produced in the previous example, except, now, the data comes from invoking <code>enhanced-sum-three</code>. Two scalar invalids and one collectoin invalid. Integer <code>1</code> fails to satisfy scalar predicate <code>char?</code>, integer <code>321</code> fails to satisfy scalar predicate <code>boolean?</code>, and the entire return vector <code>[1 20 300 321]</code> fails to satisfy collection predicate <code>list?</code>.</p><p>Okay. I think we&apos;re ready to put together all four different function validations we&apos;ve so far seen. We&apos;ve seen…</p><ul><li>a function argument scalar validation,</li><li>a function argument collection validation,</li><li>a function return scalar validation, and</li><li>a function return collection validation.</li></ul><p>And we&apos;ve seen how to combine both function argument validations, and how to combine both function return validations. Now we&apos;ll combine all four validations into one <code>validate-fn-with</code> invocation.</p><p>Let&apos;s review our ingredients. Here&apos;s our <code>enhanced-sum-three</code> function.</p><pre><code>(defn enhanced-sum-three [x y z] [x y z (+ x y z)])</code></pre><p><code>enhanced-sum-three</code> accepts three number arguments and returns a vector of those three numbers with their sum appended to the end of the vector. Technically, Clojure would accept any numberic thingy for <code>x</code>, <code>y</code>, and <code>z</code>, but for illustration purposes, we&apos;ll make our scalar predicates something non-numeric so we can see something interesting in the validation reports.</p><p>With that in mind, we pretend that we want to validate the function&apos;s argument sequence as a string, followed by an integer, followed by a symbol. The function scalar specification will be…</p><pre><code>[string? int? symbol?]</code></pre><p>To allow <code>enhanced-sum-three</code> to calculate a result, we&apos;ll supply three numeric values, two of which will not satisfy that argument scalar specification. So that it produces something interesting, we&apos;ll make our function argument collection specification also complain.</p><pre><code><code>(defn length-2? [v] (= 2 (count v)))</code></code><br /><br /><code>;; collection predicates apply to the path of the parent container</code><br /><code>[length-2?]</code></pre><p>We know for sure that the argument sequence will contain three values, so that particular argument collection predicate will produce something interesting.</p><p>Jumping to <code>enhanced-sum-three</code>&apos;s output side, we expect a vector of four numbers. Again, we&apos;ll craft our function return scalar specification to contain two predicates that we know won&apos;t be satisfied because those scalar predicates are looking for something non-numeric.</p><pre><code>[char? int? int? boolean?]</code></pre><p>Finally, since we defined <code>enhanced-sum-three</code> to return a vector, we&apos;ll make the function return collection specification look for a list.</p><pre><code>[list?]</code></pre><p>Altogether, those four specification are organized like this.</p><pre><code>{:speculoos/arg-scalar-spec     [string? int? symbol?]
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
;;      :valid? false})</code></pre><p>We&apos;ve certainly made a mess of things. But it&apos;ll be understandable if we examine the invalidation report piece by piece. The first thing to know is that we have already seen each of those validations before in the previous examples, so we could always scroll back to those examples above and see the validations in isolation.</p><p>We see six non-satisfied predicates:<ul><li>Scalar <code>1</code> in the arguments sequence fails to satisfy scalar predicate <code>string?</code> in the argument scalar specification.</li><li>Scalar <code>300</code> in the arguments sequence fails to satisfy scalar predicate <code>symbol?</code> in the argument scalar specification.</li><li>The argument sequence <code>[1 20 300]</code> fails to satisfy collection predicate <code>length-2?</code> in the argument collection specification.</li><li>Scalar <code>1</code> in the return vector fails to satisfy scalar predicate <code>char?</code> in the return scalar specification.</li><li>Scalar <code>321</code> in the return vector fails to satisfy scalar predicate <code>boolean?</code> in the return scalar specification.</li><li>The return vector <code>[1 20 300 321]</code> fails to satisfy collection predicate <code>list?</code> in the return collection specification.</li></ul></p><p>Also note that the validation entries have a <code>:fn-spec-type</code> entry associated to either <code>:speculoos/return</code> or <code>:speculoos/argument</code>, which tells us where a particular invalid was located. There may be a situation where indistinguishable invalid datums appear in both the arguments and returns. In this case, integer <code>1</code> was an invalid datum at path <code>[0]</code> for both the argument sequence and the return vector. Keyword <code>:fn-spec-type</code> helps resolve the ambiguity.</p><h3 id="fn-correctness">3. Validating Function Correctness</h3><p>So far, we&apos;ve seen how to validate function argument sequences and function return values, both their scalars, and their collections. Validating function argument sequences allows us to check if the function was invoked properly. Validating function return values gives a limited ability to check the internal operation of the function.</p><p> If we want another level of thoroughness checking the correctness, we can specify and validate the relationships between the functions arguments and return values. Perhaps you&apos;d like to be able to express <em>The return value is a collection, with all the same elements as the input sequence.</em> Or <em>The return value is a concatenation of the even indexed elements of the input sequence.</em> Speculoos&apos; term for this action is <em>validating function argument and return value relationship</em>.</p><p>Let&apos;s pretend I wrote a reversing function, which accepts a sequential collection of elements and returns those elements in reversed order. If we give it…</p><pre><code>[11 22 33 44 55]</code></pre><p>…my reversing function ought to return…</p><pre><code>[55 44 33 22 11]</code></pre><p>Here are some critical features of that process that relate the reversing function&apos;s arguments to its return value.</p><ul><li>The return collection is the same length as the input collection.</li><li>The return collection contains all the same elements as the input collection.</li><li>The elements of the return collection appear in reverse order from their positions in the input collection.</li></ul><p>Oops. I must&apos;ve written it before I had my morning coffee.</p><pre><code>(defn broken-reverse [v] (conj v 42))</code><br /><br /><code>(broken-reverse [11 22 33 44 55]) ;; =&gt; [11 22 33 44 55 42]</code></pre><p>Pitiful.<code>broken-reverse</code> fulfilled none of the three relationships. The return collection is not the same length, contains additonal elements, and is not reversed. Let&apos;s codify that pitifulness.</p><p>First, we&apos;ll write three <a href="#relationships">relationship functions</a>. Relationship funcstions are a lot like predicate. They return a truthy or falsey value, but instead consume two things instead of one. The function&apos;s argument sequence is passed as the first thing and the function&apos;s return value is passed as the second thing.</p><pre><code>(defn same-length? [v1 v2] (= (count v1) (count v2)))</code><br /><br /><code>(same-length? [11 22 33 44 55]
&nbsp;             [11 22 33 44 55]) ;; =&gt; true</code><br /><br /><code>(same-length? [11 22]
&nbsp;             [11 22 33 44 55]) ;; =&gt; false</code><br /><br /><br /><code>(defn same-elements? [v1 v2] (= (sort v1) (sort v2)))</code><br /><br /><code>(same-elements? [11 22 33 44 55]
&nbsp;               [55 44 33 22 11]) ;; =&gt; true</code><br /><br /><code>(same-elements? [11 22 33 44 55]
&nbsp;               [55 44 33 22 9999]) ;; =&gt; false</code><br /><br /><br /><code>(defn reversed? [v1 v2] (= v1 (reverse v2)))</code><br /><br /><code>(reversed? [11 22 33 44 55]
&nbsp;          [55 44 33 22 11]) ;; =&gt; true</code><br /><br /><code>(reversed? [11 22 33 44 55]
&nbsp;          [11 22 33 44 55]) ;; =&gt; false</code></pre><p><code>same-length?</code>, <code>same-element?</code>, <code>reversed?</code> all consume two sequential things and test a relationship between the two. If their relationship is satisfied, they signal <code>true</code>, if not, then they signal <code>false</code>. They are all three gonna have something unkind to say about <code>broken-reverse</code>.</p><p>In our example, checking <code>broken-reverse</code>&apos;s argument/return relationship will be fairly straightforward: There&apos;s a single argument collection of elements, and a single return collection of elements. But we might someday want to check a more sophisticated relationship that needs to extract some slice of the argument or return value. Therefore, we must declare a path to the slice we want to check. Of the return value, we&apos;d like to check the root collection, so that path is merely <code>[]</code>.</p><p>To extract argument, there&apos;s one tricky detail we must accommodate. The vector we&apos;re going to pass as an argument to <code>broken-reverse</code> is itself contained in the argument sequence. Take a look.</p><pre><code>(defn arg-passthrough [&amp; args] args)</code><br /><br /><code>(arg-passthrough [11 22 33 44 55]) ;; =&gt; ([11 22 33 44 55])</code></pre><p>To extract the first argument passed to <code>broken-reverse</code>, the path needs to be <code>[0]</code>.</p><pre><code>(nth (arg-passthrough [11 22 33 44 55]) 0) ;; =&gt; [11 22 33 44 55]</code></pre><p>Now that we know how to extract the interesting pieces, we load each into a map that looks like this.</p><pre><code>{:path-argument [0]
&nbsp;:path-return []
&nbsp;:relationship-fn same-length?}</code></pre><p>We&apos;ve written three argument/function relationships to test <code>broken-reverse</code>, so we&apos;ll need to somehow feed them to <code>validate-fn-with</code>. We do that by associating them into the organizing map with keyword <code>:speculoos/argument-return-relationships</code>. Notice the plural <em>s</em>. Since there may be more than one relationship, we collect them into a vector. For the moment, let&apos;s just insert the <code>same-length?</code> relationship.</p><pre><code>{:speculoos/argument-return-relationships [{:path-argument [0]
&nbsp;                                           :path-return []
&nbsp;                                           :relationship-fn same-length?}]}</code></pre><p>We&apos;re ready to validate.</p><pre><code>(validate-fn-with
&nbsp; broken-reverse
&nbsp; {:speculoos/argument-return-relationships
&nbsp;    [{:path-argument [0],
&nbsp;      :path-return [],
&nbsp;      :relationship-fn same-length?}]}
&nbsp; [11 22 33 44 55])
;; =&gt; ({:datum-argument [11 22 33 44 55],
;;      :datum-return [11 22 33 44 55 42],
;;      :fn-spec-type :speculoos/argument-return-relationship,
;;      :path-argument [0],
;;      :path-return [],
;;      :relationship-fn same-length?,
;;      :valid? false})</code></pre><p>We supplied <code>broken-reverse</code> with a five-element vector, and it returned a six-element vector, failing to satisfy the specified <code>same-length?</code>relationship. We wrote two other relationship functions, but we did not send them to <code>validate-fn-with</code>, so it checked only what we explicitly supplied. Remember Mantra #3: Un-paired predicates (or, relationships in this instance) are ignored.</p><p>Let&apos;s check all three relationships now.</p><pre><code>(validate-fn-with
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
;;      :datum-return [11 22 33 44 55 42],
;;      :fn-spec-type :speculoos/argument-return-relationship,
;;      :path-argument [0],
;;      :path-return [],
;;      :relationship-fn same-length?,
;;      :valid? false}
;;     {:datum-argument [11 22 33 44 55],
;;      :datum-return [11 22 33 44 55 42],
;;      :fn-spec-type :speculoos/argument-return-relationship,
;;      :path-argument [0],
;;      :path-return [],
;;      :relationship-fn same-elements?,
;;      :valid? false}
;;     {:datum-argument [11 22 33 44 55],
;;      :datum-return [11 22 33 44 55 42],
;;      :fn-spec-type :speculoos/argument-return-relationship,
;;      :path-argument [0],
;;      :path-return [],
;;      :relationship-fn reversed?,
;;      :valid? false})</code></pre><h3 id="recognized-metadata-keys">Recognized metadata specification keys</h3><p>Speculoos consults the following defined group of keys in a specification map when it validates.</p><pre><code>speculoos.function-specs/recognized-spec-keys
;; =&gt; [:speculoos/arg-scalar-spec
;;     :speculoos/arg-collection-spec
;;     :speculoos/ret-scalar-spec
;;     :speculoos/ret-collection-spec
;;     :speculoos/argument-return-relationships
;;     :speculoos/canonical-sample
;;     :speculoos/predicate-&gt;generator
;;     :speculoos/hof-specs]</code></pre><h3 id="explicit">Function Metadata Specifications</h3><p>Speculoos function specifications <a href="https://clojure.org/about/spec#\_dont\_further\_add\_tooverload\_the\_reified\_namespaces\_of\_clojure">differ</a> from <code>spec.alpha</code> in that they are stored and retrieved directly from the function&apos;s metadata. Speculoos is an experiment, but I thought it would be nice if I could hand you one single thing and say </p><blockquote><p><em>Here&apos;s a Clojure function you can use. Its name suggests what it does, its docstring that tells you how to use it, and human- and machine-readable specifications check the validity of the inputs, and tests that it&apos;s working properly. All in one neat, tidy </em>S-expression.</p></blockquote><p>Speculoos offers three patterns of function validation.<ol><li><code>validate-fn-with</code> performs explicit validation with a specification supplied in a separate map. The function var is not altered.</li><li><code>validate-fn</code> performs explicit validation with specifications contained in the function&apos;s metadata.</li><li><code>instrument/unstrument</code> provide implicit validation with specifications contained in the function&apos;s metadata.</li></ol></p><p>Up until this point, we&apos;ve been using the most explicit variant, <code>validate-fn-with</code> because its behavior is the most readily apparent.</p><p>The first pattern is nice because you can quickly validate a function <em>on-the-fly</em> without messing with the function&apos;s metadata. Merely supply the function&apos;s name, a map of specifications (we&apos;ll discuss this soon), and a sequence of args as if you were directly invoking the function.</p><p>Speculoos offers a pair convenience functions to add and remove specifications from a function&apos;s metadata. To add, use <code>inject-specs!</code>.</p><pre>#&apos;speculoos-project-readme-generator/add-ten<code>(require &apos;[speculoos.function-specs :refer
&nbsp;          [inject-specs! unject-specs! validate-fn]])</code><br /><br /></pre><p>We can observe that the specifications indeed live in the function&apos;s metadata. If we later decided to undo that, <code>unject-specs!</code> removes all recognized Speculoos specification entries, regardless of how they got there. For the upcoming demonstrations, though, we&apos;ll keep those specifications in <code>add-ten</code>&apos;s metadata.</p><p>Now that <code>add-ten</code> holds the specifications in its metadata, we can try the second pattern of explicit validation pattern. It&apos;s similar, except we don&apos;t have to supply the specification map; it&apos;s already waiting in the metadata. Invoked with a valid argument, <code>add-ten</code> returns a valid value.</p><pre><code>(validate-fn add-ten 15) ;; =&gt; 25</code></pre><p>Invoking <code>add-ten</code> with an invalid float, Speculoos interrupts with a validation report.</p>#&apos;speculoos-project-readme-generator/silly<p>Next, we inject our specifications into the function&apos;s metadata: a scalar specification and collection specification, each, for both the argument sequence and the return sequence. A grand total of four specifications.</p><p>Valid inputs…</p><p>…produce a valid return vector of three elements. The function halves the ratio, increments the integer, and reverses the string. But invalid arguments…</p><p>…yields an invalidation report: the character <code>\a</code> does not satisfy its <code>string?</code> scalar predicate, and the integer <code>9</code> does not satisfy its <code>ratio?</code> scalar predicate.</p><p>Until this point in our discussion, Speculoos has only performed function validation when we explicitly call either <code>validate-fn-with</code> or <code> validate-fn</code>. The specifications in the metadata are passive and produce no effect, even with arguments that would otherwise fail to satisfy the specification&apos;s predicates.</p><pre><code>(add-ten 15) ;; =&gt; 25</code><br /><code>(add-ten 1.23) ;; =&gt; 11.23</code></pre><h3>Instrumenting Functions</h3><p>Speculoos&apos; third pattern of function validation <em>instruments</em> the function using the metadata specifications. (Beware: <code>instrument</code>-style function validation is very much a work in progress. The implementation is sensitive to invocation order and can choke on multiple calls. The var mutation is not robust.) Every invocation of the function itself automatically validates any specified arguments and return values.</p><pre><code>(require &apos;[speculoos.function-specs :refer [instrument unstrument]])</code></pre><p>The function returns if it doesn&apos;t throw an exception. Any non-satisfied predicates are reported to <code>\*out\*</code>. When we are done, we can <em>unstrument</em> the function, and Speculoos will no longer intervene.</p><p>Even though character <code>\a</code> and not-ratio <code>9</code> do not satisfy their respective predicates, <code>silly</code> is no longer instrumented, and Speculoos is not intercepting its invocation, so the function returns a value. (Frankly, I wrote <code>instrument/unstrument</code> to mimic the features <code>spec.alpha</code> offers. My implementation is squirrelly, and I&apos;m unskilled with mutating vars. I lean much more towards the deterministic <code>validate-fn</code> and <code>validate-fn-with</code>.)</p><p>Beyond validating a function&apos;s argument sequence and its return, Speculoos can perform a validation that checks the relationship between any aspect of the arguments and return values. When the arguments sequence and return sequence share a high degree of shape, an <em>argument versus return scalar specification</em> will work well. A good example of this is using <code>map</code> to transform a sequence of values. Each item in the return sequence has a corresponding item in the argument sequence.</p><p>The predicates in a <em>versus</em> specification are a little bit unusual. Each predicate accepts <em>two</em> arguments: the first is the element from the argument sequence, the second is the corresponding element from the return sequence.</p><p>Let&apos;s make a bogus predicate that we know will fail, just so we can see what happens when a relationship is not satisfied.</p><p>And stuff them into a map whose keys Speculoos recognizes.</p><p>Now that we&apos;ve prepared our predicates and composed a versus specification, we can validate the relationship between the arguments and the returns. We&apos;ll invoke <code>mult-ten</code> with the same <code>1 2 3</code> sequence we saw above.</p><p>We intentionally constructed our specification to fail at the middle element, and sure enough, the validation report tells us the argument and return scalars do not share the declared relationship. <code>20</code> from the return sequence is not nine times <code>2</code> from the arg sequence.</p><p>To complete the <em>scalars/collections/arguments/returns/versus</em> feature matrix, Speculoos can also validate function argument <em>collections</em> against return collections. All of the previous discussion holds, with the twist that the specification predicates apply against the argument collections and return collections. Examples show better than words.</p><p> Speculoos passes through the function&apos;s return if all predicates are satisfied, so we&apos;ll intentionally bungle one of the predicates to cause an invalidation report.</p><p>Composing our args versus return collection specification, using the proper pseudo-qualified key. Remember: collection predicates apply to their immediate containing collections.</p><p>Our goofy reverse function fails our buggy argument versus return validation.</p><p><code>goofy-reverse</code> behaves exactly as it should, but for illustration purposes, we applied a buggy collection specification that we knew would fail. The validation report shows us the two things it compared, in this instance, the argument sequence and the returned, reversed sequence, and furthermore, that those two collections failed to satisfy the buggy predicate, <code>equal-lengths?</code>. The other predicate, <code>mirror-elements-equal?</code> was satisfied because the first element of the argument collection is equal to the last element of the return collection, and was therefore not included in the report.</p><h3 id="hof">Validating Higher-Order Functions</h3><p>Speculoos has a story about validating higher-order functions, too. It uses very similar patterns to first-order function validation: put some specifications in the function&apos;s metadata with the properly qualified keys, then invoke the function with some sample arguments, then Speculoos will validate the results. Here&apos;s how it works. We start with a flourish of the classic adder <span class="small-caps">hof</span>.</p><pre><code>(require &apos;[speculoos.function-specs :refer [validate-higher-order-fn]])</code><br /><br /><code>(defn addder [x] (fn [y] (fn [z] (+ x (\* y z)))))</code><br /><br /><code>(((addder 3) 2) 10) ;; =&gt; 23</code></pre><p><code>addder</code> returns a function upon each of its first two invocations, and only on its third does it return a scalar. Specifying and validating a function value does not convey much meaning: it would merely satisfy <code>fn?</code> which isn&apos;t very interesting. So to validate a <span class="small-caps">hof</span>, Speculoos requires it to be invoked until it produces a value. So we&apos;ll supply the validator with a series of arg sequences  that, when fed in order to the <span class="small-caps">hof</span>, will produce a result. For the example above, it will look like <code>[3] [2] [10]</code>.</p><p>The last task we must do is create the specification. <span class="small-caps">hof</span> specifications live in the function&apos;s metadata at key <code>:speculoos/hof-specs</code> which is a series of nested specification maps, one nesting for each returned function. For this example, we might create this <span class="small-caps">hof</span> specification.</p><pre><code>(def addder-spec
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
;;      :valid? false})</code></pre><p>Let&apos;s step through the validation results. Speculoos validates <code>3</code> against scalar predicate <code>even?</code> and then invokes <code>addder</code> with argument <code>3</code>. It then validates <code>5</code> against scalar predicate <code>ratio?</code> and then invokes the returned function with argument <code>5</code>. Finally, Speculoos validates <code>10</code> against scalar predicate <code>float?</code> and invokes the previously returned function with argument <code>10</code>. If all the predicates were satisfied, Speculoos would yield the return value of the function call. In this case, all three arguments are invalid, and Speculoos yields a validation report.</p></section><br /><h2>License</h2><p>This program and the accompanying materials are made available under the terms of the <a href="https://opensource.org/license/MIT">MIT License</a>.</p>