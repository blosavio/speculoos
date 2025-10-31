
  <body>
    <h1>
      valid-scalars performance
    </h1>
    <div>
      <a href="#group-0">Hashmaps</a><br>
      <a href="#group-1">Lists</a><br>
      <a href="#group-2">Sequences</a><br>
      <a href="#group-3">Vectors</a>
    </div>
    <div>
      <p>
        For the moment, this page illustrates the baseline performance of <code>valid-scalars?</code> before any attempt to optimize performance. See analagous
        benchmarks for <a href="https://blosavio.github.io/speculoos/all_paths_performance.html"><code>all-paths</code></a> and <a href=
        "https://blosavio.github.io/speculoos/valid_collections_performance.html"><code>valid-collections?</code></a>.
      </p>
      <p>
        The sample data structures and specifications are defined in the <a href=
        "https://github.com/blosavio/speculoos/blob/main/test/speculoos/performance/benchmark_structures.clj">benchmarks structures namespace</a> while the
        benchmarks themselves are defines in the <a href=
        "https://github.com/blosavio/speculoos/blob/main/test/speculoos/performance/all_paths_benchmarks.clj">definitions namespace</a>.
      </p>
      <p>
        For each of the collection types (hashmaps, lists, sequences, and vectors), we see benchmark results different lengths of &apos;flat&apos; collections,
        and different lenghts+depths of nested collections. In general, <code>n</code> is an exponenet to ten, i.e., the lengths increase exponentially.
      </p>
    </div>
    <section>
      <h3 id="group-0">
        Hashmaps
      </h3>
      <div>
        <p>
          Hashmaps comments...
        </p>
      </div>
      <div>
        <h4 id="group-0-fexpr-0">
          (fn [n] (valid-scalars? (map-of-n-key-vals n) (map-spec n)))
        </h4><img alt=
        "Benchmark measurements for expression `(fn [n] (valid-scalars? (map-of-n-key-vals n) (map-spec n)))`, time versus &apos;n&apos; arguments, comparing different versions."
        src="img_valid_scalars/group-0-fexpr-0.svg"><button class="collapser" type="button">Show details</button>
        <div class="collapsable">
          <table>
            <caption>
              times in seconds, <em>mean±std</em>
            </caption>
            <thead>
              <tr>
                <td></td>
                <th colspan="4">
                  arg, n
                </th>
              </tr>
              <tr>
                <th>
                  version
                </th>
                <th>
                  1
                </th>
                <th>
                  10
                </th>
                <th>
                  100
                </th>
                <th>
                  1000
                </th>
              </tr>
            </thead>
            <tr>
              <td>
                7
              </td>
              <td>
                <a href="https://github.com/blosavio/speculoos/blob/master/resources/performance/valid_scalars/version 7/test-16.edn">4.7e-04±6.9e-07</a>
              </td>
              <td>
                <a href="https://github.com/blosavio/speculoos/blob/master/resources/performance/valid_scalars/version 7/test-17.edn">2.5e-03±5.5e-06</a>
              </td>
              <td>
                <a href="https://github.com/blosavio/speculoos/blob/master/resources/performance/valid_scalars/version 7/test-18.edn">2.3e-02±3.7e-05</a>
              </td>
              <td>
                <a href="https://github.com/blosavio/speculoos/blob/master/resources/performance/valid_scalars/version 7/test-19.edn">2.3e-01±7.6e-04</a>
              </td>
            </tr>
          </table>
        </div>
        <h4 id="group-0-fexpr-1">
          (fn [n] (valid-scalars? (nested-map n) (nested-map-spec n)))
        </h4><img alt=
        "Benchmark measurements for expression `(fn [n] (valid-scalars? (nested-map n) (nested-map-spec n)))`, time versus &apos;n&apos; arguments, comparing different versions."
        src="img_valid_scalars/group-0-fexpr-1.svg"><button class="collapser" type="button">Show details</button>
        <div class="collapsable">
          <table>
            <caption>
              times in seconds, <em>mean±std</em>
            </caption>
            <thead>
              <tr>
                <td></td>
                <th colspan="5">
                  arg, n
                </th>
              </tr>
              <tr>
                <th>
                  version
                </th>
                <th>
                  1
                </th>
                <th>
                  2
                </th>
                <th>
                  3
                </th>
                <th>
                  4
                </th>
                <th>
                  5
                </th>
              </tr>
            </thead>
            <tr>
              <td>
                7
              </td>
              <td>
                <a href="https://github.com/blosavio/speculoos/blob/master/resources/performance/valid_scalars/version 7/test-20.edn">4.6e-04±7.8e-07</a>
              </td>
              <td>
                <a href="https://github.com/blosavio/speculoos/blob/master/resources/performance/valid_scalars/version 7/test-21.edn">1.2e-03±2.1e-06</a>
              </td>
              <td>
                <a href="https://github.com/blosavio/speculoos/blob/master/resources/performance/valid_scalars/version 7/test-22.edn">5.3e-03±1.1e-05</a>
              </td>
              <td>
                <a href="https://github.com/blosavio/speculoos/blob/master/resources/performance/valid_scalars/version 7/test-23.edn">4.2e-02±1.2e-04</a>
              </td>
              <td>
                <a href="https://github.com/blosavio/speculoos/blob/master/resources/performance/valid_scalars/version 7/test-24.edn">4.9e-01±1.1e-03</a>
              </td>
            </tr>
          </table>
        </div>
      </div>
      <hr>
      <h3 id="group-1">
        Lists
      </h3>
      <div>
        <p>
          Lists comments...
        </p>
      </div>
      <div>
        <h4 id="group-1-fexpr-0">
          (fn [n] (valid-scalars? (list-of-n-rand-ints n) (list-spec n)))
        </h4><img alt=
        "Benchmark measurements for expression `(fn [n] (valid-scalars? (list-of-n-rand-ints n) (list-spec n)))`, time versus &apos;n&apos; arguments, comparing different versions."
        src="img_valid_scalars/group-1-fexpr-0.svg"><button class="collapser" type="button">Show details</button>
        <div class="collapsable">
          <table>
            <caption>
              times in seconds, <em>mean±std</em>
            </caption>
            <thead>
              <tr>
                <td></td>
                <th colspan="3">
                  arg, n
                </th>
              </tr>
              <tr>
                <th>
                  version
                </th>
                <th>
                  1
                </th>
                <th>
                  10
                </th>
                <th>
                  100
                </th>
              </tr>
            </thead>
            <tr>
              <td>
                7
              </td>
              <td>
                <a href="https://github.com/blosavio/speculoos/blob/master/resources/performance/valid_scalars/version 7/test-13.edn">4.3e-04±7.3e-07</a>
              </td>
              <td>
                <a href="https://github.com/blosavio/speculoos/blob/master/resources/performance/valid_scalars/version 7/test-14.edn">2.3e-03±5.2e-06</a>
              </td>
              <td>
                <a href="https://github.com/blosavio/speculoos/blob/master/resources/performance/valid_scalars/version 7/test-15.edn">2.2e-02±3.9e-05</a>
              </td>
            </tr>
          </table>
        </div>
        <h4 id="group-1-fexpr-1">
          (fn [n] (valid-scalars? (nested-list n) (nested-list-spec n)))
        </h4><img alt=
        "Benchmark measurements for expression `(fn [n] (valid-scalars? (nested-list n) (nested-list-spec n)))`, time versus &apos;n&apos; arguments, comparing different versions."
        src="img_valid_scalars/group-1-fexpr-1.svg"><button class="collapser" type="button">Show details</button>
        <div class="collapsable">
          <table>
            <caption>
              times in seconds, <em>mean±std</em>
            </caption>
            <thead>
              <tr>
                <td></td>
                <th colspan="4">
                  arg, n
                </th>
              </tr>
              <tr>
                <th>
                  version
                </th>
                <th>
                  1
                </th>
                <th>
                  2
                </th>
                <th>
                  3
                </th>
                <th>
                  4
                </th>
              </tr>
            </thead>
            <tr>
              <td>
                7
              </td>
              <td>
                <a href="https://github.com/blosavio/speculoos/blob/master/resources/performance/valid_scalars/version 7/test-0.edn">4.3e-04±1.6e-06</a>
              </td>
              <td>
                <a href="https://github.com/blosavio/speculoos/blob/master/resources/performance/valid_scalars/version 7/test-1.edn">1.5e-03±1.2e-05</a>
              </td>
              <td>
                <a href="https://github.com/blosavio/speculoos/blob/master/resources/performance/valid_scalars/version 7/test-2.edn">8.8e-03±2.6e-05</a>
              </td>
              <td>
                <a href="https://github.com/blosavio/speculoos/blob/master/resources/performance/valid_scalars/version 7/test-3.edn">7.6e-02±4.9e-04</a>
              </td>
            </tr>
          </table>
        </div>
      </div>
      <hr>
      <h3 id="group-2">
        Sequences
      </h3>
      <div>
        <p>
          Seq comments...
        </p>
      </div>
      <div>
        <h4 id="group-2-fexpr-0">
          (fn [n] (valid-scalars? (nested-seq n) (nested-seq-spec n)))
        </h4><img alt=
        "Benchmark measurements for expression `(fn [n] (valid-scalars? (nested-seq n) (nested-seq-spec n)))`, time versus &apos;n&apos; arguments, comparing different versions."
        src="img_valid_scalars/group-2-fexpr-0.svg"><button class="collapser" type="button">Show details</button>
        <div class="collapsable">
          <table>
            <caption>
              times in seconds, <em>mean±std</em>
            </caption>
            <thead>
              <tr>
                <td></td>
                <th colspan="5">
                  arg, n
                </th>
              </tr>
              <tr>
                <th>
                  version
                </th>
                <th>
                  1
                </th>
                <th>
                  2
                </th>
                <th>
                  3
                </th>
                <th>
                  4
                </th>
                <th>
                  5
                </th>
              </tr>
            </thead>
            <tr>
              <td>
                7
              </td>
              <td>
                <a href="https://github.com/blosavio/speculoos/blob/master/resources/performance/valid_scalars/version 7/test-25.edn">7.3e-04±1.5e-06</a>
              </td>
              <td>
                <a href="https://github.com/blosavio/speculoos/blob/master/resources/performance/valid_scalars/version 7/test-26.edn">1.5e-03±3.2e-06</a>
              </td>
              <td>
                <a href="https://github.com/blosavio/speculoos/blob/master/resources/performance/valid_scalars/version 7/test-27.edn">8.8e-03±1.5e-05</a>
              </td>
              <td>
                <a href="https://github.com/blosavio/speculoos/blob/master/resources/performance/valid_scalars/version 7/test-28.edn">7.6e-02±1.8e-04</a>
              </td>
              <td>
                <a href="https://github.com/blosavio/speculoos/blob/master/resources/performance/valid_scalars/version 7/test-29.edn">8.9e-01±1.4e-03</a>
              </td>
            </tr>
          </table>
        </div>
        <h4 id="group-2-fexpr-1">
          (fn [n] (valid-scalars? (seq-of-n-rand-ints n) (seq-spec n)))
        </h4><img alt=
        "Benchmark measurements for expression `(fn [n] (valid-scalars? (seq-of-n-rand-ints n) (seq-spec n)))`, time versus &apos;n&apos; arguments, comparing different versions."
        src="img_valid_scalars/group-2-fexpr-1.svg"><button class="collapser" type="button">Show details</button>
        <div class="collapsable">
          <table>
            <caption>
              times in seconds, <em>mean±std</em>
            </caption>
            <thead>
              <tr>
                <td></td>
                <th colspan="4">
                  arg, n
                </th>
              </tr>
              <tr>
                <th>
                  version
                </th>
                <th>
                  1
                </th>
                <th>
                  10
                </th>
                <th>
                  100
                </th>
                <th>
                  1000
                </th>
              </tr>
            </thead>
            <tr>
              <td>
                7
              </td>
              <td>
                <a href="https://github.com/blosavio/speculoos/blob/master/resources/performance/valid_scalars/version 7/test-4.edn">7.4e-04±3.4e-06</a>
              </td>
              <td>
                <a href="https://github.com/blosavio/speculoos/blob/master/resources/performance/valid_scalars/version 7/test-5.edn">2.7e-03±9.2e-06</a>
              </td>
              <td>
                <a href="https://github.com/blosavio/speculoos/blob/master/resources/performance/valid_scalars/version 7/test-6.edn">2.7e-03±4.9e-06</a>
              </td>
              <td>
                <a href="https://github.com/blosavio/speculoos/blob/master/resources/performance/valid_scalars/version 7/test-7.edn">2.7e-03±5.1e-06</a>
              </td>
            </tr>
          </table>
        </div>
      </div>
      <hr>
      <h3 id="group-3">
        Vectors
      </h3>
      <div>
        <p>
          Vectors comments...
        </p>
      </div>
      <div>
        <h4 id="group-3-fexpr-0">
          (fn [n] (valid-scalars? (nested-vec n) (nested-vec-spec n)))
        </h4><img alt=
        "Benchmark measurements for expression `(fn [n] (valid-scalars? (nested-vec n) (nested-vec-spec n)))`, time versus &apos;n&apos; arguments, comparing different versions."
        src="img_valid_scalars/group-3-fexpr-0.svg"><button class="collapser" type="button">Show details</button>
        <div class="collapsable">
          <table>
            <caption>
              times in seconds, <em>mean±std</em>
            </caption>
            <thead>
              <tr>
                <td></td>
                <th colspan="5">
                  arg, n
                </th>
              </tr>
              <tr>
                <th>
                  version
                </th>
                <th>
                  1
                </th>
                <th>
                  2
                </th>
                <th>
                  3
                </th>
                <th>
                  4
                </th>
                <th>
                  5
                </th>
              </tr>
            </thead>
            <tr>
              <td>
                7
              </td>
              <td>
                <a href="https://github.com/blosavio/speculoos/blob/master/resources/performance/valid_scalars/version 7/test-8.edn">6.5e-04±1.3e-06</a>
              </td>
              <td>
                <a href="https://github.com/blosavio/speculoos/blob/master/resources/performance/valid_scalars/version 7/test-9.edn">2.3e-03±4.7e-06</a>
              </td>
              <td>
                <a href="https://github.com/blosavio/speculoos/blob/master/resources/performance/valid_scalars/version 7/test-10.edn">1.3e-02±2.3e-05</a>
              </td>
              <td>
                <a href="https://github.com/blosavio/speculoos/blob/master/resources/performance/valid_scalars/version 7/test-11.edn">1.1e-01±4.2e-04</a>
              </td>
              <td>
                <a href="https://github.com/blosavio/speculoos/blob/master/resources/performance/valid_scalars/version 7/test-12.edn">1.3e+00±4.3e-03</a>
              </td>
            </tr>
          </table>
        </div>
        <h4 id="group-3-fexpr-1">
          (fn [n] (valid-scalars? (vec-of-n-rand-ints n) (vec-spec n)))
        </h4><img alt=
        "Benchmark measurements for expression `(fn [n] (valid-scalars? (vec-of-n-rand-ints n) (vec-spec n)))`, time versus &apos;n&apos; arguments, comparing different versions."
        src="img_valid_scalars/group-3-fexpr-1.svg"><button class="collapser" type="button">Show details</button>
        <div class="collapsable">
          <table>
            <caption>
              times in seconds, <em>mean±std</em>
            </caption>
            <thead>
              <tr>
                <td></td>
                <th colspan="4">
                  arg, n
                </th>
              </tr>
              <tr>
                <th>
                  version
                </th>
                <th>
                  1
                </th>
                <th>
                  10
                </th>
                <th>
                  100
                </th>
                <th>
                  1000
                </th>
              </tr>
            </thead>
            <tr>
              <td>
                7
              </td>
              <td>
                <a href="https://github.com/blosavio/speculoos/blob/master/resources/performance/valid_scalars/version 7/test-30.edn">6.5e-04±1.7e-06</a>
              </td>
              <td>
                <a href="https://github.com/blosavio/speculoos/blob/master/resources/performance/valid_scalars/version 7/test-31.edn">3.5e-03±8.9e-06</a>
              </td>
              <td>
                <a href="https://github.com/blosavio/speculoos/blob/master/resources/performance/valid_scalars/version 7/test-32.edn">3.3e-02±5.8e-05</a>
              </td>
              <td>
                <a href="https://github.com/blosavio/speculoos/blob/master/resources/performance/valid_scalars/version 7/test-33.edn">3.3e-01±7.1e-04</a>
              </td>
            </tr>
          </table>
        </div>
      </div>
      <hr>
    </section>
    <p id="page-footer">
      Copyright © 2024–2025 Brad Losavio.<br>
      Compiled by <a href="https://github.com/blosavio/Fastester">Fastester</a> on 2025 October 31.<span id="uuid"><br>
      3cacb81b-dd3c-425c-a1e4-ecfbfdbb91f3</span>
    </p>
  </body>
</html>
