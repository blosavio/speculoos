[![Clojars Project](https://img.shields.io/clojars/v/com.sagevisuals/speculoos.svg)](https://clojars.org/com.sagevisuals/speculoos)

* [Ideas](https://blosavio.github.io/speculoos/ideas.html) An in-depth discussion of the Speculoos experiment.
* [Documentation](https://blosavio.github.io/speculoos/documentation.html) Discussion of why and how to use Speculoos, including implementation details and a glossary of terms.
* [Recipes](https://blosavio.github.io/speculoos/recipes.html) Quick examples showing how to use various features of the Speculoos library.
* [diff](https://blosavio.github.io/speculoos/diff.html) Side-by-side comparison of spec.alpha and Speculoos.
* [API](https://blosavio.github.io/speculoos/index.html)

# Speculoos

An experimental Clojure library for data specification.

## Setup

### Leiningen/Boot

In your `project.clj` file's `:deps` entry:

```clojure
[com.sagevisuals/speculoos "1"]
```

### Clojure CLI/deps.edn

```clojure
com.sagevisuals/speculoos {:mvn/version "1"}
```

Then require it:

```clojure
(require '[speculoos.core :refer [valid-scalars? validate-scalars only-invalid]])
```

## Brief usage

Validate a sequence of scalars:

```clojure
(valid-scalars? [42 :foo 22/7] [int? keyword? ratio?]) ;; => true
```

Validate a map of scalars:

```clojure
(valid-scalars? {:x 42 :y 'foo} {:x int? :y symbol?}) ;; true
```

Predicates not satisfied:

```clojure
(only-invalid (validate-scalars [42 :foo 22/7] [string? keyword? int?]))
;; => ({:path [0], :datum 42, :predicate string?, :valid? false}
;;     {:path [2], :datum 22/7, :predicate int?, :valid? false})
```

More at the [Speculoos project page](https://blosavio.github.io/speculoos/home.html).

## License

Copyright © 2024 Bradley Losavio

This program and the accompanying materials are made available under the
terms of the [MIT License](https://opensource.org/license/mit).
