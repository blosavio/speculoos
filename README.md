# Speculoos

An experimental Clojure library for data specification.

Find more documentation at the [Speculoos project page](https://spec.uloos.net).

## Setup

### Leiningen

In your `project.clj` file's `:deps` entry:

```clojure
[com.sagevisuals/speculoos "version1"]
```

### deps.edn

```clojure
com.sagevisuals/speculoos {:mvn/version "version1"}
```

Then require it:

```clojure
(require '[speculoos.core :refer [valid-scalars? validate-scalars only-invalid]])
```

## Usage

Validate a sequence:

```clojure
(valid-scalars? [42 :foo 22/7] [int? keyword? ratio?]) ;; => true
```

Validate a map:

```clojure
(valid-scalars? {:x 42 :y 'foo} {:x int? :y symbol?}) ;; true
```

Predicates not satisfied:

```clojure
(only-invalid (validate-scalars [42 :foo 22/7] [string? keyword? int?]))
;; => ({:path [0], :datum 42, :predicate string?, :valid? false}
;;     {:path [2], :datum 22/7, :predicate int?, :valid? false})
```

## License

Copyright © 2024 Bradley Losavio

This program and the accompanying materials are made available under the
terms of the [MIT License](https://opensource.org/license/mit).
