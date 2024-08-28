(ns speculoos.function-specs
  "This namespace provides facilities to apply specifications to and validate
  functions. Roughly speaking, trying to replicate the instrumentation and
  testing capabilities of [spec.alpha](https://clojure.org/guides/spec#_instrumentation_and_testing)
  .

  Speculoos explores a few different _styles_ of function specification:

  1. Explicitly providing the specification during explicit validation.
  2. Implicitly providing the specification via metadata, explicitly validating.
  3. Implicitly providing the specification via metadata, implicitly validating
  via instrumentation.

  **Warning**: Several actions performed by functions in this namespace mutate
  state, and are brittle. Use this namespace with the understanding that it is
  very much a proof-of-concept."
  (:require
   [clojure.repl :refer [demunge]]
   [clojure.set :as set]
   [clojure.string :as str]
   [speculoos.core :refer [validate only-invalid dual-validate-scalars
                           dual-validate-collections all-paths regex?
                           validate-scalars validate-collections]]
   [speculoos.utility :refer [data-from-spec]]))


(defn fn-var
  "Returns var for a given function name f.

   This feels hacky. I'd prefer to use (var f) within a function definition,
   but that throws a java.lang.RuntimeException, perhaps because it's a special
   form. This is a work-around to eventually extract the function
   meta-data because (meta) needs the var itself.

  Another thing I'm unsure about: Is abusing (ns-resolve ns sym) a robust
  tactic? Will it work in all namespacing scenarios? Is the format of
  (.getClass) stable over time, consistent across clojure distributions?

  Does not work with (def (fn [] (...))) nor #(...)."
  {:UUIDv4 #uuid "605be447-6b47-47a7-9e85-69a4250e8184"
   :no-doc true}
  [f]
  (let [demunged (-> f
                     .getClass
                     .getName
                     demunge)
       [nspace fname] (str/split demunged #"\/")]
    (ns-resolve (symbol nspace) (symbol fname))))


(defn fn-meta
  "Returns meta-data for a function name f. See lamentations for (fn-var)."
  {:UUIDv4 #uuid "ee3f0f21-69ce-487e-81fc-9387f6e7bd09"
   :no-doc true}
  [f]
  (meta (fn-var f)))


(defn fn-meta*
  "Returns meta-data for a function's var #'f.
   This is a vesion of (fn-meta) whose arg is a var instead of a symbol."
  {:UUIDv4 #uuid "c44a9b55-3ce3-4e1f-bb86-59c472820bcb"
   :no-doc true}
  [f-var]
  (meta f-var))


(defn assoc-metadata-f!
  "Associate metadata key `k`, value `v` to the var of function `f`.
   Returns the metadata map. See [[dissoc-metadata-f!]] for the inverse
  operation.

  Example:
  ```clojure
  (defn foo [] true)

  (assoc-metadata-f! foo :assoced 'bar!)

  (:assoced (meta #'foo)) ;; => 'bar!
  ```"
  {:UUIDv4 #uuid "49827dda-5d66-4f99-bfe0-87610b712bef"}
  [f k v]
  (alter-meta! (fn-var f) #(assoc % k v)))


(defn dissoc-metadata-f!
  "Dissociate metadata key `k` from the var of function `f`.
  Returns the metadata map. See [[assoc-metadata-f!]] for the inverse
  operation.

  Example:
  ```clojure
  (defn foo [] true)

  (assoc-metadata-f! foo :assoced 'bar!)

  (:assoced (meta #'foo)) ;; => 'bar!

  (dissoc-metadata-f! foo :assoced)

  (:assoced (meta #'foo)) ;; => nil
  ```"
  {:UUIDv4 #uuid "be4fb294-e7ff-4c84-af33-0a34e241a00c"}
  [f k]
  (alter-meta! (fn-var f) #(dissoc % k)))


(def ^{:doc "A sequence that contains the only allowed pseudo-qualified keys to
 be added to, or referred from, a function's metadata. Only governs behavior of
 utilities provided by this namespace, such as [[instrument]], [[unstrument]],
 [[validate-fn-with]], and [[validate-fn-meta-spec]]. Does not affect anything
 outside this namespace."}
  recognized-spec-keys (with-meta [:speculoos/arg-scalar-spec
                                   :speculoos/arg-collection-spec
                                   :speculoos/ret-scalar-spec
                                   :speculoos/ret-collection-spec
                                   :speculoos/argument-return-relationships
                                   :speculoos/canonical-sample
                                   :speculoos/predicate->generator
                                   :speculoos/hof-specs]
                         {:UUIDv4 #uuid "76d7feb1-4917-4cb8-aa2a-2407acf7057a"}))


(defn inject-specs!
  "Given function `f`, associates scalar and collection specifications for
  arguments and return values. `specs` is a map, whose only recognized
  pseudo-qualified keys are contained in [[recognized-spec-keys]]. No warnings
  are given for key-vals that are not recognized and thus not injected. Returns
  `nil`. See [[unject-specs!]] for the inverse operation.

  Example:
  ```clojure
  (defn foo [] true)

  (inject-specs! foo {:speculoos/ret-scalar-spec boolean?}) ;; => nil

  (:speculoos/ret-scalar-spec (meta #'foo)) ;; => boolean?
  ```"
  {:UUIDv4 #uuid "3fef700d-eb84-4beb-ad8c-3d7d6f1c4b67"}
  [f specs]
  (doseq [k (set/intersection (set recognized-spec-keys)
                              (set (keys specs)))]
    (assoc-metadata-f! f k (k specs))))


(defn unject-specs!
  "Given function `f`, dissociates any key-vals contained in
  [[recognized-spec-keys]]. Inverse operation provided by [[inject-specs!]].
  Returns `nil`.

  Example:
  ```clojure
  (defn foo [] true)

  (inject-specs! foo {:speculoos/ret-scalar-spec boolean?}) ;; => nil

  (:speculoos/ret-scalar-spec (meta #'foo)) ;; => boolean?

  (unject-specs! foo) ;; => nil

  (:speculoos/ret-scalar-spec (meta #'foo)) ;; => nil
  ```"
  {:UUIDv4 #uuid "7f17537b-3b3c-40cb-8e20-da4ec0025660"}
  [f]
  (doseq [k recognized-spec-keys]
    (dissoc-metadata-f! f k)))


(defn valid?-or-report
  "Returns true if x fully satisfies both scalar-spec and collection-spec,
   otherwise, returns a report of un-satisfied predicates."
  {:UUIDv4 #uuid "8cd41605-d954-4b84-8d63-6b0ee2eae5e1"
   :no-doc true}
  [x scalar-spec collection-spec]
  (let [results (validate x scalar-spec collection-spec)
        invalid-results (only-invalid results)]
    (if (empty? invalid-results)
      true
      invalid-results)))


(defn validate-argument-return-relationship
  "Validates an argument/return relationship given argument sequence `arg`,
  function return value `ret`, and relationship `rel`, a map of
  `{:path-argument … :path-return … :relationship-fn …}`. `:relationship-fn` is
  a 2-arity function that presumably tests some relationship between a slice of
  `arg`, passed as the first argument to the relationship function, and a slice
  of `ret`, passed as the second argument to the relationship function. A `nil`
  path indicates a 'bare' scalar value (i.e., a non-collection). `arg` ought to
  always be a sequence.

  Intended to validate the relationship between a function's arguments and its
  return value.

  Examples:
  ```clojure
  (defn doubled? [x y] (= y (* 2 x)))

  ;; 'bare' function return, uses path `nil`
  (validate-argument-return-relationship [42] 84 {:path-argument [0]
                                                  :path-return nil
                                                  :relationship-fn doubled?})
  ;; => {:path-argument [0],
  ;;     :path-return nil,
  ;;     :relationship-fn doubled?,
  ;;     :datum-argument 42,
  ;;     :datum-return 84,
  ;;     :valid? true}


  (defn reversed? [v1 v2] (= v2 (reverse v1)))

  ;; argument and return value fail to satisfy relationship
  (validate-argument-return-relationship [1 2 3] [1 2 3] {:path-argument []
                                                          :path-return []
                                                          :relationship-fn reversed?})
  ;; => {:path-argument [],
  ;;     :path-return [],
  ;;     :relationship-fn reversed?,
  ;;     :datum-argument [1 2 3],
  ;;     :datum-return [1 2 3],
  ;;     :valid? false}
  ```"
  {:UUIDv4 #uuid "6a1d0075-0f4b-44c9-8d1f-2c86c94b78e6"}
  [arg ret rel]
  (let [arg-datum (fn-in.core/get-in* arg (:path-argument rel))
        ret-datum (if (:path-return rel)
                    (fn-in.core/get-in* ret (:path-return rel))
                    ret)
        validation ((:relationship-fn rel) arg-datum ret-datum)]
    (merge rel {:datum-argument arg-datum
                :datum-return ret-datum
                :valid? validation})))


(defn validate-fn-with
  "Validate the scalar and collection aspects of arguments `args`, the return
  value, and the relationship between the arguments and return value, of
  function `f`. `specs` is a map with any permutation of
  [[recognized-spec-keys]]. Returns `f`'s results if `specs` are fully
  satisfied, otherwise, returns a report.

  Note: Argument and return values satisfying the specifications does not
  guarantee that the function's output is correct.

  See [[validate-scalars]] and [[validate-collections]] for details
  about scalar and collection validation.

  See [[validate-argument-return-relationship]] for details about validating
  relationships between the function's argument and the function's return value.

  Example, validating scalars:
  ```clojure
  (defn foo [x y] (+ x y))

  ;; no specifications; return value passes through
  (validate-fn-with foo {} 2 3)
  ;; => 5

  ;; all scalar specifications satisfied; return value passes through
  (validate-fn-with foo
                    {:speculoos/arg-scalar-spec [int? int?]
                     :speculoos/ret-scalar-spec int?}
                    2 3)
  ;; => 5

  ;; one argument scalar specification and the return value scalar specification not satisfied
  (validate-fn-with foo
                    {:speculoos/arg-scalar-spec [int? int?]
                     :speculoos/ret-scalar-spec int?}
                    2 3.3)
  ;; => ({:path [1], :datum 3.3, :predicate int?, :valid? false, :fn-spec-type :speculoos/argument}
  ;;     {:path nil, :datum 5.3, :predicate int?, :valid? false, :fn-spec-type :speculoos/return})
  ```

  Example, validating argument/return value relationship:
  ```clojure
  ;; function to validate
  (defn broken-reverse [v] v)

  ;; function to test if the return collection is a correctly reversed version of the argument collection
  (defn reversed? [v1 v2] (= v2 (reverse v1)))

  ;; yup, it's truly broken
  (reversed? [11 22 33] (broken-reverse [11 22 33]))
  ;; => false

  ;; `broken-reverse` fails to satisfy the relationship function because it doesn't correctly reverse the argument collection
  (validate-fn-with broken-reverse
                    {:speculoos/argument-return-relationships [{:path-argument [0]
                                                                :path-return []
                                                                :relationship-fn reversed?}]}
                    [11 22 33])
  ;; => ({:path-argument [0],
  ;;      :path-return [],
  ;;      :relationship-fn reversed?,
  ;;      :datum-argument [11 22 33],
  ;;      :datum-return [11 22 33],
  ;;      :valid? false,
  ;;      :fn-spec-type
  ;;      :speculoos/argument-return-relationship})
  ```"
  {:UUIDv4 #uuid "44971243-841c-4c53-9926-90a94cd1407c"}
  [f specs & args]
  (let [arg-scalar-spec (:speculoos/arg-scalar-spec specs)
        arg-collection-spec (:speculoos/arg-collection-spec specs)
        bare-arg-scalar-spec? (and ((complement nil?) arg-scalar-spec)
                                   ((complement coll?) arg-scalar-spec)) ;; ((complment coll?) nil) evals to true
        ret-scalar-spec (:speculoos/ret-scalar-spec specs)
        ret-collection-spec (:speculoos/ret-collection-spec specs)
        arg-scalar-validation (if arg-scalar-spec
                                (if bare-arg-scalar-spec?
                                  (binding [speculoos.core/*notice-on-validation-bare-scalar* false] (validate-scalars (first args) arg-scalar-spec))
                                  (validate-scalars (vec args) arg-scalar-spec))
                                [])
        arg-collection-validation (if arg-collection-spec
                                    (validate-collections (vec args) arg-collection-spec)
                                    [])
        arg-spec-results (concat arg-scalar-validation arg-collection-validation)
        tagged-arg-results (map #(assoc % :fn-spec-type :speculoos/argument) arg-spec-results)
        return (try (apply f (vec args))
                    (catch Exception e
                      (println "Exception:" (.getMessage e)
                               "\narg scalar spec:" (:speculoos/arg-scalar-spec specs)
                               "\narg collection spec:" (:speculoos/arg-collection-spec specs))
                      []))
        ret-scalar-validation (if ret-scalar-spec
                                (binding [speculoos.core/*notice-on-validation-bare-scalar* false] (validate-scalars return ret-scalar-spec))
                                [])
        ret-collection-validation (if ret-collection-spec
                                    (validate-collections return ret-collection-spec)
                                    [])
        ret-spec-results (concat ret-scalar-validation ret-collection-validation)
        tagged-ret-results (map #(assoc % :fn-spec-type :speculoos/return) ret-spec-results)
        argument-return-relationship-validation (mapv #(validate-argument-return-relationship (vec args) return %) (:speculoos/argument-return-relationships specs))
        tagged-argument-return-relationship-results (map #(assoc % :fn-spec-type :speculoos/argument-return-relationship) argument-return-relationship-validation)
        non-satisfied-specs (filter #(not (:valid? %)) (concat tagged-arg-results
                                                               tagged-ret-results
                                                               tagged-argument-return-relationship-results))]
    (if (empty? non-satisfied-specs)
      return
      non-satisfied-specs)))


(defn validate-fn-meta-spec
  "Validates function `f` in the manner of [[validate-fn-with]], except
  specifications are supplied by the function's metadata, addressed by
  [[recognized-spec-keys]].

  Examples:
  ```clojure
  (defn foo [x s] (+ x (read-string s)))
  (foo 7 \"8\") ;; 15

  (def foo-spec {:speculoos/arg-scalar-spec [int? string?]
                 :speculoos/ret-scalar-spec number?})

  (inject-specs! foo foo-spec) ;; => nil

  ;; supplying valid arguments; function returns
  (validate-fn-meta-spec foo 7 \"8\") ;; 15

  ;; supplying invalid argument (arg2 is not a string); yields a report
  (validate-fn-meta-spec foo 7 8)
  ;; => ({:path [1], :datum 8, :predicate string?, :valid? false, :fn-spec-type :speculoos/argument}
  ;;     {:path nil, :datum [], :predicate number?, :valid? false, :fn-spec-type :speculoos/return})
  ```"
  {:UUIDv4 #uuid "be89a74f-5489-4901-9c88-37cb49b37482"}
  [f & args]
  (apply (partial validate-fn-with f (fn-meta f)) args))


(defn exhaust-higher-order-fn
  "Invoke higher-order function f until args, supplied as vectors, are exhausted."
  {:UUIDv4 #uuid "c62827b9-67bc-41a4-a82d-72339ed4e49e"
   :no-doc true}
  [f & args]
  (if (next args)
    (apply exhaust-higher-order-fn (apply f (first args)) (next args))
    (apply f (first args))))


(defn flatten-nested-hof-fn-specs
  "Given function f, flatten all nested higher-order-function specs, if any."
  {:UUIDv4 #uuid "89603ffa-3689-4076-b96e-904a918a6cd0"
   :no-doc true}
  [f]
  (let [top-level-specs (dissoc (select-keys (fn-meta f) recognized-spec-keys)
                                :speculoos/hof-specs)
        children-spec-tree (->> f
                                fn-meta
                                all-paths
                                (filter #(= :speculoos/hof-specs (last (:path %))))
                                (map #(select-keys (:value %) recognized-spec-keys))
                                (map #(dissoc % :speculoos/hof-specs)))]
    (into (vector top-level-specs) children-spec-tree)))


(defn rearrange-specs
  "Reshape spec so it structurally corresponds to &-args. Key k specifies which
   spec to pull out, typically :speculoos/arg-scalar-spec or
   :speculoos/arg-collection-spec."
  {:UUIDv4 #uuid "237bd6b4-0799-4337-8f58-31f0c5960abe"
   :no-doc true}
  [spec k]
  (reduce #(conj %1 (k %2)) [] spec))


(defn validate-flattened-fn-meta-specs
  "Given (possibly) higher-order function f with specs residing in metadata,
   validate each 'tier' in turn. If an argument collection spec is provided for
   a lower-level function, a collection spec must be supplied for all functions
   'above' that, even if empty. Otherwise, the lower-level function's collection
    spec will be applied to the arg vector of a higher-level function."
  {:UUIDv4 #uuid "be8fc757-819a-44fd-aadb-52579191d8b0"
   :no-doc true
   :implementation-notes "Args are stuctured [[args-1] [args-2] [...]], so
   leverage speculoos' structural validation capabilities to apply predicates
   against that structure by rearranging the specs in the metadata to mimic
   that shape."}
  [f & args]
  (let [flattened-specs (flatten-nested-hof-fn-specs f)
        scalar-spec (rearrange-specs flattened-specs :speculoos/arg-scalar-spec)
        collection-spec (rearrange-specs flattened-specs :speculoos/arg-collection-spec)]
    (validate (vec args) scalar-spec collection-spec)))


(defn validate-hof-return-spec
  "Given (possibly) higher-order function `f`, evalute `f` to exhaustion using
  supplied arguments `args` and validate against return specification `spec`.
  Only specifications in the metadata of the top-level function `f` are used;
  any specifications on lower-level functions are ignored."
  {:UUIDv4 #uuid "cc0a9e25-b882-4595-9154-303d232c0f37"
   :no-doc true}
  [f & args]
  (let [tail-specs (last (flatten-nested-hof-fn-specs f))
        spec (select-keys tail-specs [:speculoos/ret-scalar-spec
                                      :speculoos/ret-collection-spec
                                      :speculoos/arg-vs-ret-scalar-spec
                                      :speculoos/arg-vs-ret-scalar-spec])
        almost-exhausted-hof (apply exhaust-higher-order-fn f (butlast args))
        final-args (last args)]
    (apply validate-fn-with almost-exhausted-hof spec final-args)))


(defn validate-higher-order-fn
  "Evaluates arbitrarily-deep, higher-order function `f` to exhaustion
  (i.e., until it yields a value that is not a function), validating arguments
  `args` and the return value. Halts on exceptions. Caller must supply
  sufficient `&-arg` vectors such that the final output could satisfy the return
  specification, if it exists.

  Specifications are supplied in the top-level function's metadata. All other
  lower-level function var metadata is ignored. Specifications must be a member
  of [[recognized-spec-keys]]. Specifications for the top-level function reside
  in the standard keys. Specifications for lower-level functions are nested into
  their respective tiers of the `:speculoos/hof-specs` key. If an argument
  collection specification is supplied for a lower-level function, a collection
  specification must be supplied for all functions 'above' that, even if those
  higher-level specifications are empty.

  If every specification is satisfied, then the function's evaluated value is
  returned, otherwise a sequence of invalidation reports.

  Un-defined behavior if the final yield is `nil` or a function.

  If you would call

  ```(((f 1 2) :foo :bar) 'a 'b)```

  then the validation would be

  ```(validate-higher-order-fn f [1 2] [:foo :bar] ['a 'b])```

  Use `(with-meta f m)` for _ad hoc_ specification.

  Example:
  ```clojure
  (defn foo [x y] (fn [w z] (* (+ x w) (- y z))))

  (def bar (foo 8 11))
  (bar 12 6) ;; 100

  (def foo-spec {:speculoos/arg-scalar-spec [int? ratio?]
                 :speculoos/hof-specs {:speculoos/arg-scalar-spec [int? ratio?]}})

  (inject-specs! foo foo-spec) ;; nil

  ;; even though `foo` could consume these arguments and produce a value,
  ;; the arguments do not satisfy specification
  (validate-higher-order-fn foo [8 11] [12 6])
  ;; => ({:path [0 1], :datum 11, :predicate ratio?, :valid? false, :fn-tier :speculoos/argument}
  ;;     {:path [1 1], :datum 6, :predicate ratio?, :valid? false, :fn-tier :speculoos/argument})

  ;; arguments satisfy specification, so `foo` returns the value
  (validate-higher-order-fn foo [8 16/3] [12 1/3]) ;; 100N
  ```"
  {:UUIDv4 #uuid "af8f900e-772e-4dd7-8552-394404405a76"}
  [f & args]
  (let [plain-eval (apply exhaust-higher-order-fn f args)
        arg-validation (apply validate-flattened-fn-meta-specs f args)
        ret-validation (apply validate-hof-return-spec f args)
        arg-all-valid? (every? #(:valid? %) arg-validation)
        ret-all-valid? (= plain-eval ret-validation)
        apply-tags (fn [v tag] (map #(assoc % :fn-tier tag) v))
        insert-eval (fn [v] (map #(assoc % :evaled-result plain-eval) v))
        only-invalids (fn [v] (remove #(:valid? %) v))
        tagged-arg-validation (if (not arg-all-valid?) (apply-tags arg-validation :speculoos/argument))
        tagged-ret-validation (if (not ret-all-valid?) (-> ret-validation
                                                           (apply-tags :speculoos/return)
                                                           insert-eval))]
    (cond
      (and arg-all-valid? ret-all-valid?) plain-eval
      (and (not arg-all-valid?) (not ret-all-valid?)) (concat (only-invalids tagged-arg-validation) (only-invalids tagged-ret-validation))
      (not arg-all-valid?) (only-invalids tagged-arg-validation)
      (not ret-all-valid?) (only-invalids tagged-ret-validation)
      :else :speculoos/SENTINEL)))


(defn fn-args
  "Returns list of vectors of all arglist arities of function f.
   Within those vectors, args are represented by symbols.
   Beware: Ampersand (&) appears in variable-arity function arg lists."
  {:UUIDv4 #uuid "f4881ee2-66f9-45ac-ab6d-c1b389b7fc1f"
   :no-doc true}
  [f]
  (:arglists (fn-meta f)))


(defn set-fn-var-root!
  "Change function f's root value to val."
  {:UUIDv4 #uuid "c63386fc-deed-4ed0-8c87-45c982d15686"
   :no-doc true}
  [f val]
  (alter-var-root (fn-var f) (constantly val)))


(defonce ^{:no-doc true} old-new-fn (atom {}))

(comment ;; dev-time convenience
  (reset! old-new-fn {})
  )


(defn wrapping-fn
  "Low-level plumbing for [[instrument]]. See [[validate-fn-with]] for details.

  Returns a function that will sequentially:

  1. Validate arguments with scalar specifications.
  2. Validate argument sequence with collection specification.
  3. Invoke function `f`.
  4. Validate return value with scalar specification.
  5. Validate return value with collection specification.
  6. Validate arguments versus return specifications.

  Execution of the returned function will print non-satisfied specs to `*out*`
  and return the value produced by invoking `f` with the supplied arguments. At
  time of wrapping, function metadata must contain any desired specifications,
  possibly added by [[inject-specs!]]. Will not halt on non-valid specs.

  Note: Arguments and return value satisfying their specification does not
  guarantee that the function's output is correct."
  {:UUIDv4 #uuid "86670e61-df2a-4d78-92b7-d7e491f4ac3a"}
  [f]
  (fn [& args]
    (do (println (apply validate-fn-with f (fn-meta f) args))
        (apply f args))))


(defn update-old-new-fn-mappings
  "Add f to the old-new-fn registry.
   Inverse operation provided by (dissoc-old-new-fn-mappings)."
  {:UUIDv4 #uuid "ce8c118c-7912-4829-9497-27e7d16831df"
   :no-doc true}
  [f]
  (swap! old-new-fn assoc (fn-var f) f))


(defn instrument
  "_Wrap_ function `f` such that invoking `f` implicitly performs argument
  validation before and return value validation after execution. See
  [[wrapping-fn]] for details.

  Implemented by:

  1. Adding an entry into the instrumentation registry.
  2. Altering `f`'s root var to wrapped version of itself.

  The inverse operation is provided by [[unstrument]].

  **Warning:** This implementation is experimental and brittle. Since it relies
  on mutating vars, it is sensitive to order of invocation and multiple
  invocations.

  Examples:
  ```clojure
  (defn add-n-subtract [x y] [(+ x y) (- x y)])

  ;; `instrument` requires specifications be added *before* instrumentation
  (inject-specs! add-n-subtract {:speculoos/arg-scalar-spec [int? ratio?]})

  (instrument add-n-subtract)

  ;; while instrumented, each invocation automatically validates any supplied specification
  ;; non-satisfied predicates are send to *out*
  (with-out-str (add-n-subtract 5 2)) ;; printed to *out* ({:path [1], :datum 2, :predicate ratio?, :valid? false})

  ;; if all specifications are satisfied, the function returns a value
  (add-n-subtract 5 3/2) ;; => [13/2 7/2]

  ;; remove instrumentation wrapper...
  (unstrument add-n-subtract) ;; => {}

  ;; ...but metadata specifications remain
  (:speculoos/arg-scalar-spec (meta #'add-n-subtract)) ;; => [int? ratio?]

  ;; function resumes normal behavior; non-satisfying arguments are consumed without note
  (add-n-subtract 5 2) ;; => [7 3]
  (add-n-subtract 5 3/2) ;; => [13/2 7/2]
  ```"
  {:UUIDv4 #uuid "bc5e57cc-6c00-4707-b0cb-c0335dd3d630"}
  [f]
  (locking (update-old-new-fn-mappings f)
    (set-fn-var-root! f (wrapping-fn f))))


(defmacro revert-fn
  "Revert a wrapped function f to its original state as logged in the registry."
  {:UUIDv4 #uuid "8b94b0db-8cad-4591-b462-8b3960a596cb"
   :no-doc true}
  [f]
  `(alter-var-root (var ~f) (constantly (get @old-new-fn (resolve '~f)))))


(defn dissoc-old-new-fn-mappings
  "Remove f from the old-new-fn registry. The entry to be removed was presumably
  created by (update-old-new-fn-mappings)."
  {:UUIDv4 #uuid "bf1ba2c4-12e1-4328-97a4-7b9add1add8a"
   :no-doc true}
  [f]
  (swap! old-new-fn dissoc (fn-var f)))


(defmacro unstrument
  "_Un-wrap_ function `f` to its original form that does not implicitly
  validate arguments nor return values.pre-fn nor post-fn. This is the inverse
  operation of [[instrument]].

  Compliments to whoever originally thought of 'unstrument'."
  {:UUIDv4 #uuid "a7a53414-9fda-467b-9f64-321438057630"}
  [f]
  `(locking (alter-var-root (var ~f) (constantly (get @old-new-fn (resolve '~f))))
     (dissoc-old-new-fn-mappings ~f)))


(defn exercise-fn
  "Exercises the function `f` by applying it to `n` (default `10`) generated
  samples of its scalar argument specification, residing at
  `:speculoos/arg-scalar-spec` in its metadata.

  Returns a sequence of tuples of `[args ret]`. Does not currently handle
  higher-order-functions.

  See [[data-from-spec]] for details on predicates.

  Example:
  ```clojure
  (defn foo [x kw] (str (+ 100 x) \"is not equivalent to \" kw))

  (foo 5 :bar) ;; \"105 is not equivalent to :bar\"

  (inject-specs! foo {:speculoos/arg-scalar-spec [int? keyword?]})

  (exercise-fn foo 3)
  ;; => ([[76 :i-:!7S] \"176 is not equivalent to :i-:!7S\"]
  ;;     [[-381 :W] \"-281 is not equivalent to :W\"]
  ;;     [[-940 :LS1-:i] \"-840 is not equivalent to :LS1-:i\"])
  ```"
  {:UUIDv4 #uuid "d1db8e1f-1974-44c6-9056-d61e278e0f24"}
  ([f] (exercise-fn f 10))
  ([f n]
   (let [spec (:speculoos/arg-scalar-spec (fn-meta f))
         repeatedly-f (fn [] (let [data (data-from-spec spec :random)
                                   ret (apply f data)]
                               (vector data ret)))]
     (repeatedly n repeatedly-f))))