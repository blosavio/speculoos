<h1>Speculoos <code>prettyfy</code> check</h1><p>Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.</p><pre><code>(+ 1 2 3) ;; =&gt; 6</code><br /><br /><code>(map inc [11 22 33]) ;; =&gt; (12 23 34)</code><br /><br /><code>(defn foo [x y] (vector x y (+ x y) (* x y)))</code></pre><h2><code>all-paths</code></h2><p>Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.</p><pre><code>(require &apos;[speculoos.core :refer [all-paths]])</code><br /><br /><code>(all-paths [42 :foo 22/7])
;; =&gt; [{:path [], :value [42 :foo 22/7]}
;;     {:path [0], :value 42}
;;     {:path [1], :value :foo}
;;     {:path [2], :value 22/7}]</code><br /><br /><code>(all-paths {:a 42, :b &apos;foo, :c 22/7})
;; =&gt; [{:path [], :value {:a 42, :b foo, :c 22/7}}
;;     {:path [:a], :value 42}
;;     {:path [:b], :value foo}
;;     {:path [:c], :value 22/7}]</code><br /><br /><code>(all-paths [11 [22 [33] 44] 55 66 77 88 99])
;; =&gt; [{:path [],
;;      :value [11 [22 [33] 44] 55 66 77
;;              88 99]}
;;     {:path [0], :value 11}
;;     {:path [1], :value [22 [33] 44]}
;;     {:path [1 0], :value 22}
;;     {:path [1 1], :value [33]}
;;     {:path [1 1 0], :value 33}
;;     {:path [1 2], :value 44}
;;     {:path [2], :value 55}
;;     {:path [3], :value 66}
;;     {:path [4], :value 77}
;;     {:path [5], :value 88}
;;     {:path [6], :value 99}]</code></pre><h2><code>validate-scalars</code></h2><p>Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur.</p><pre><code>(require &apos;[speculoos.core :refer [validate-scalars]])</code><br /><br /><code>(validate-scalars [42 :foo 22/7]
                  [int? string? ratio?])
;; =&gt; [{:datum 42, :path [0], :predicate int?, :valid? true}
;;     {:datum :foo, :path [1], :predicate string?, :valid? false}
;;     {:datum 22/7, :path [2], :predicate ratio?, :valid? true}]</code><br /><br /><code>(validate-scalars {:a 42, :b &apos;foo, :c 22/7}
                  {:a int?, :b string?, :c ratio?})
;; =&gt; [{:datum 42, :path [:a], :predicate int?, :valid? true}
;;     {:datum foo, :path [:b], :predicate string?, :valid? false}
;;     {:datum 22/7, :path [:c], :predicate ratio?, :valid? true}]</code></pre><h2><code>validate-collections</code></h2><p>Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.</p><pre><code>(require &apos;[speculoos.core :refer [validate-collections]])</code><br /><br /><code>(validate-collections [11 [22 [33]]]
                      [vector? [set? [list?]]])
;; =&gt; ({:datum [11 [22 [33]]],
;;      :ordinal-path-datum [],
;;      :path-datum [],
;;      :path-predicate [0],
;;      :predicate vector?,
;;      :valid? true}
;;     {:datum [22 [33]],
;;      :ordinal-path-datum [0],
;;      :path-datum [1],
;;      :path-predicate [1 0],
;;      :predicate set?,
;;      :valid? false}
;;     {:datum [33],
;;      :ordinal-path-datum [0 0],
;;      :path-datum [1 1],
;;      :path-predicate [1 1 0],
;;      :predicate list?,
;;      :valid? false})</code><br /><br /><code>(validate-collections {:a 11, :b {:c [22]}}
                      {:is-map? map?, :b {:is-set? set?, :c [vector?]}})
;; =&gt; ({:datum {:a 11, :b {:c [22]}},
;;      :ordinal-path-datum [],
;;      :path-datum [],
;;      :path-predicate [:is-map?],
;;      :predicate map?,
;;      :valid? true}
;;     {:datum {:c [22]},
;;      :ordinal-path-datum [:b],
;;      :path-datum [:b],
;;      :path-predicate [:b :is-set?],
;;      :predicate set?,
;;      :valid? false}
;;     {:datum [22],
;;      :ordinal-path-datum [:b :c],
;;      :path-datum [:b :c],
;;      :path-predicate [:b :c 0],
;;      :predicate vector?,
;;      :valid? true})</code></pre><h2><code>valid-scalars?</code> and <code>valid-collections?</code></h2><p>Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.</p><pre><code>(require &apos;[speculoos.core :refer [valid-scalars? valid-collections?]])</code><br /><br /><code>(valid-scalars? [42 :foo 22/7]
                [int? keyword? ratio?])
;; =&gt; true</code><br /><br /><code>(valid-scalars? {:a 42, :b &apos;foo, :c 22/7}
                {:a int?, :b symbol?, :c ratio?})
;; =&gt; true</code><br /><br /><code>(valid-collections? [11 [22 [33]]]
                    [vector? [set? [map?]]])
;; =&gt; false</code><br /><br /><code>(valid-collections? {:a 42, :b {:c [&apos;foo]}}
                    {:is-map? map?, :b {:is-map? map?, :c [vector?]}})
;; =&gt; true</code></pre>