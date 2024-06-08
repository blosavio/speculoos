(ns speculoos.example-data)

(def x [1 2 3])
(def three-int-vector [int? int? int?])

(comment
  ((three-int-vector 0) (x 0))
  )


(def simple-vector [1 2 3 4 5])
(def simple-map {:a 1 :b 2 :c 3})
(def simple-set #{:item1 :item2 :item3})

(def vector-of-fns [+ - *])
(def loos-spec-fns [[#(= (%) 0) ; (+) => 0
                     #(= (% 1) 1) ; (+ 1) => 1
                     #(= (% 1 2 3) 6)] ; (+ 1 2 3) => 6

                    [#(= (%) 0) ; (-) => 0
                     #(= (% 1) -1) ; => (- 1) => -1
                     #(= (% 6 3 2) 1)] ; (- 6 3 2) => 1

                    [#(= (%) 1) ; (*) => 1
                     #(= (% 2 3) 6) ; (* 2 3) => 6
                     #(= (% 8 1/2) 4N)] ; (* 8 1/2) => 4
                    ])

(comment
  ((get-in loos-spec-fns [0 0]) (get-in vector-of-fns [0])) 
  ((get-in loos-spec-fns [0 1]) (get-in vector-of-fns [0]))
  ((get-in loos-spec-fns [0 2]) (get-in vector-of-fns [0]))
  )

(def stacked-vector [[[[1 2 3 4 5]]]])
(def nested-vector [1 [2] 3 [[4 5] 6 [[[7 8 9]]]]])
(def nested-map {:person {:last-name "Bogart"
                          :first-name "Humphrey"
                          :films ["Maltese Falcon" "Casablanca"]}
                 :places [{:city "Casablanca"
                           :country "Morocco"}
                          {:city "San Franciso"
                           :state "California"
                           :country "United States of America"}]})

(def heterogeneous-nested-vector [:a
                                 {:b 1 :c 2}
                                 "three"
                                 true
                                 [nil 3.0 false]
                                  {:kw1 [7.7 8.8 9.9]
                                   :kw2 [[0.0] 1.1 2.2]}
                                  22/7
                                 #{"d" 4.0M \e :f}
                                 42
                                 [10 [20 [30 40]]]])

(def nested-test-vector [:a
                         [:b :c]
                         :d
                         [:e
                          [:f :g]
                          :h]
                         :i
                         [[[:j]]]])

(def simple-x [:a :b :c :d])
(def nested-x [:a [:b :c] :d])
(def nested-y [:a [:b :c [:d [:e]]] :g [[[:h]]]])
(def nested-z [:a [:b :c '(:d [:e]) :g] [['(:h)]] #{:i :j :k}])
(def nested-q '(:a [:b {:c 1.1 :d 2.2}] #{:e :f} :before-empty-vec [] :after-empty-vec))


(comment
  (get-in heterogeneous-nested-vector [0]) ; :a
  (get-in heterogeneous-nested-vector [1]) ; {:b 1, :c 2}
  (get-in heterogeneous-nested-vector [2]) ; "three"
  (get-in heterogeneous-nested-vector [3]) ; true
  (get-in heterogeneous-nested-vector [4]) ; [nil 3.0 false]
  (get-in heterogeneous-nested-vector [5]) ; {:kw1 [7.7 8.8 9.9], :kw2 [[0.0] 1.1 2.2]}
  (get-in heterogeneous-nested-vector [6]) ; 22/7
  (get-in heterogeneous-nested-vector [7]) ; #{"d" \e :f 4.0M}
  (get-in heterogeneous-nested-vector [8]) ; 42
  (get-in heterogeneous-nested-vector [9]) ; [10 [20 [30 40]]]

  (get-in heterogeneous-nested-vector [1 :b]) ; 1
  (get-in heterogeneous-nested-vector [1 :c]) ; 2

  (get-in heterogeneous-nested-vector [4 0]) ; nil
  (get-in heterogeneous-nested-vector [4 1]) ; 3.0
  (get-in heterogeneous-nested-vector [4 2]) ; false

  (get-in heterogeneous-nested-vector [5 :kw1]) ; [7.7 8.8 9.9]
  (get-in heterogeneous-nested-vector [5 :kw2]) ; [[0.0] 1.1 2.2]

  (get-in heterogeneous-nested-vector [5 :kw2 0]) ; [0.0]
  (get-in heterogeneous-nested-vector [5 :kw2 1]) ; 1.1
  (get-in heterogeneous-nested-vector [5 :kw2 2]) ; 2.2

  (get-in heterogeneous-nested-vector [5 :kw2 0 0]) ; 0.0

  (get-in heterogeneous-nested-vector [9 0]) ; 10
  (get-in heterogeneous-nested-vector [9 1]) ; [20 [30 40]]
  (get-in heterogeneous-nested-vector [9 1 0]) ; 20
  (get-in heterogeneous-nested-vector [9 1 1]) ; [30 40]
  (get-in heterogeneous-nested-vector [9 1 1 0]) ; 30
  (get-in heterogeneous-nested-vector [9 1 1 1]) ; 40
  )





(def uber-example [:a [:b :c] :d [[:e]]])


(comment
  (get-in uber-example [0]) ; :a
  (get-in uber-example [1 0]) ; :b
  (get-in uber-example [1 1]) ; :c
  (get-in uber-example [2]) ; :d
  (get-in uber-example [3 0 0]) ; :e
  )

(def paths-uber-examples [[0]
                          [1 0]
                          [1 1]
                          [2]
                          [3 0 0]])

(map (partial get-in uber-example) paths-uber-examples)
