(require '[clojure.string :as str])		; for splitting an input line into words

(def debug false)

(defn abs [x]
    (if (> x 0) x (* -1 x)))

(defn prepend [list value] (cons value list))	; put value at the front of list

(defrecord node [i e h adj])			; index excess-preflow height adjacency-list

(defn node-adj [u] (:adj u))			; get the adjacency-list of a node
(defn node-height [u] (:h u))			; get the height of a node
(defn node-excess [u] (:e u))			; get the excess-preflow of a node

(defn has-excess [u nodes]
	(> (node-excess @(nodes u)) 0))

(defrecord edge [u v f c])			; one-node another-node flow capacity
(defn edge-flow [e] (:f e))			; get the current flow on an edge
(defn edge-capacity [e] (:c e))			; get the capacity of an edge

; read the m edges with the normal format "u v c"
(defn read-graph [i m nodes edges]
	(if (< i m)
		(do	(let [line 	(read-line)]			
			(let [words	(str/split line #" ") ]

			(let [u		(Integer/parseInt (first words))]
			(let [v 	(Integer/parseInt (first (rest words)))]
			(let [c 	(Integer/parseInt (first (rest (rest words))))]
			
			(ref-set (edges i) (update @(edges i) :u + u))
			(ref-set (edges i) (update @(edges i) :v + v))
			(ref-set (edges i) (update @(edges i) :c + c))

			(ref-set (nodes u) (update @(nodes u) :adj prepend i))
			(ref-set (nodes v) (update @(nodes v) :adj prepend i)))))))

			; read remaining edges
			(recur (+ i 1) m nodes edges))))

(defn other [edge u]
	(if (= (:u edge) u) (:v edge) (:u edge)))

(defn u-is-edge-u [edge u]
	(= (:u edge) u))

(defn increase-flow [edges i d]
	(ref-set (edges i) (update @(edges i) :f + d)))

(defn decrease-flow [edges i d]
	(ref-set (edges i) (update @(edges i) :f - d)))

(defn move-excess [nodes u v d]
	(ref-set (nodes u) (update @(nodes u) :e - d))
	(ref-set (nodes v) (update @(nodes v) :e + d)))

(defn insert [excess-nodes v]
	(ref-set excess-nodes (cons v @excess-nodes)))

(defn check-insert [excess-nodes v s t]
	(if (and (not= v s) (not= v t))
		(insert excess-nodes v)))

(defn push [edge-index u nodes edges excess-nodes s t]
	(let [v 	(other @(edges edge-index) u)]
	(let [uh	(node-height @(nodes u))]
	(let [vh	(node-height @(nodes v))]
	(let [e 	(node-excess @(nodes u))]
	(let [i		edge-index]
	(let [f 	(edge-flow @(edges i))]
	(let [c 	(edge-capacity @(edges i))]
	(let [ve (node-excess @(nodes v))]

	(if debug
		(do
			(println "--------- before push -------------------")
			(println "i = " i)
			(println "u = " u)
			(println "uh = " uh)
			(println "e = " e)
			(println "f = " f)
			(println "c = " c)
			(println "v = " v)
			(println "vh = " vh)
			(println "ve = " ve)))

	(if (u-is-edge-u @(edges edge-index) u) 
		(do
				(let [d (min (node-excess @(nodes u)) (- (edge-capacity @(edges edge-index)) (edge-flow @(edges edge-index))))]
					(increase-flow edges edge-index  d)
						(move-excess nodes u v d)		
				))
		(do
				(let [d (min (node-excess @(nodes u)) (+ (edge-capacity @(edges edge-index)) (edge-flow @(edges edge-index))))]
					(decrease-flow edges edge-index d)
						(move-excess nodes u v d)		
				)))

	(let [v 	(other @(edges edge-index) u)]
	(let [uh	(node-height @(nodes u))]
	(let [vh	(node-height @(nodes v))]
	(let [e 	(node-excess @(nodes u))]
	(let [i		edge-index]
	(let [f 	(edge-flow @(edges i))]
	(let [c 	(edge-capacity @(edges i))]
	(let [ve (node-excess @(nodes v))]
	(if debug
		(do
			(println "--------- after push -------------------")
			(println "i = " i)
			(println "u = " u)
			(println "uh = " uh)
			(println "e = " e)
			(println "f = " f)
			(println "c = " c)
			(println "v = " v)
			(println "vh = " vh)
			(println "ve = " ve)
			(println "--------- Done with push ---------------")))))))))))




	(assert (>= (node-excess @(nodes u)) 0))
	(assert (<= (abs (edge-flow @(edges i))) (edge-capacity @(edges i))))

	(if (has-excess u nodes)
		(do (check-insert excess-nodes u s t)
			(if debug (println "try put node" u " back after push"))))

	(if (has-excess v nodes)
		(do (check-insert excess-nodes v s t)
		(if debug (println "try put node" v " in excess after push")))))))))))))

(defn flower [edge u]
	(if (u-is-edge-u edge u) 
	(do 1)
	(do -1)))


; go through adjacency-list of source and push
(defn initial-push [adj s t nodes edges excess-nodes]
	(let [change (ref 0)] ; unused for initial pushes since we know they will be performed
	(if (not (empty? adj))
		(do 
			; give source this capacity as excess so the push will be accepted
			(ref-set (nodes s) (update @(nodes s) :e + (edge-capacity @(edges (first adj)))))
			(push (first adj) s nodes edges excess-nodes s t)
			(initial-push (rest adj) s t nodes edges excess-nodes)))))

(defn initial-pushes [nodes edges s t excess-nodes]
	(initial-push (node-adj @(nodes s)) s t nodes edges excess-nodes))

(defn remove-any [excess-nodes]
	(dosync 
		(let [ u (ref -1)]
			(do
				(if (not (empty? @excess-nodes))
					(do
						(ref-set u (first @excess-nodes))
						(ref-set excess-nodes (rest @excess-nodes))))
			@u))))

; read first line with n m c p from stdin

(def line (read-line))

; split it into words
(def words (str/split line #" "))

(def n (Integer/parseInt (first words)))
(def m (Integer/parseInt (first (rest words))))

(def s 0)
(def t (- n 1))
(def excess-nodes (ref ()))

(def nodes (vec (for [i (range n)] (ref (->node i 0 (if (= i 0) n 0) '())))))

(def edges (vec (for [i (range m)] (ref (->edge 0 0 0 0)))))



(defn relabel [nodes u]
    (ref-set (nodes u) (update @(nodes u) :h + 1))
    (if debug (println "Relabling "u))
    (check-insert excess-nodes u s t)
)


(defn activate [adj u nodes edges s t excess-nodes]
	(if(empty? adj)
			(do
			0)
	
			(do
				(let [b (flower @(edges (first adj)) u)]
						(let [v (other @(edges (first adj)) u)]
						
						(if debug
						(do
						(println "----- push check -----")
						(println "b = "b)
						(println "Node height u: " (node-height @(nodes u) ) " v:" (node-height @(nodes v)))
						(println "Flow*b: "(* b (edge-flow @(edges (first adj)))) " cap: "(edge-capacity @(edges (first adj))))))
						
						(if (and (> (node-height @(nodes u) )  (node-height @(nodes v)))  (< (*  b (edge-flow @(edges (first adj)))) (edge-capacity @(edges (first adj))) ) )
							(do
									(push (first adj) u nodes edges excess-nodes s t )
										1)

							(do
								(recur (rest adj) u nodes edges  s t excess-nodes))))))))
							

	(defn activateNode [ u nodes edges s t excess-nodes]
		(let [cond (activate (node-adj @(nodes u)) u nodes edges s t excess-nodes)]

		(if(== cond 0)
		(do
		(relabel nodes u)))))

	(defn work [ nodes edges s t excess-nodes]
		(let [u (remove-any excess-nodes)]
		(if debug(println "Took out "u))

		(if (not(= u -1))
		(do 
		(activateNode u nodes edges s t excess-nodes)
		(recur nodes edges s t excess-nodes)))))

(dosync (read-graph 0 m nodes edges))

(defn preflow []

	(dosync (initial-pushes nodes edges s t excess-nodes))
	(dosync (work nodes edges s t excess-nodes))

	(println "f =" (node-excess @(nodes t))))

(preflow)
