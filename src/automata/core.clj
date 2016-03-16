(ns automata.core
  (:require [clojure.java.io :as io]
            [instaparse.core :as insta])
  (:gen-class))

(comment (def example-dfa
  {:transition 
   {"q1"
    {\a "q1"
     \b "q2"
     \c "q3"}
    "q2"
    {\a "q2"
     \b "q3"
     \c "q3"}
    "q3"
    {\a "q3"
     \b "q3"
     \c "q3"}}
   :final "q3"
   :start "q1"}))

(defn t-function
  [dfa state sym]
  (let [t-table (:transition dfa)]
   (get-in t-table [state sym])))

(defn xt-function
  [dfa state word]
  (let [t-table (:transition dfa)]
    (loop
      [state state
       sym (first word)
       word (rest word)]
      (let [next-state (t-function dfa state sym)]
        (if (empty? word)
          next-state
          (recur next-state (first word) (rest word)))))))

(def dfa-parser
  (insta/parser
    "AUTOMATA = SDEF (<force_white> SDEF)* <op_white>
     SDEF = SLABEL <op_white> <'{'> <op_white> (TDEF <force_white>)+ <'}'>
     SLABEL = #'q[a-bA-B0-9_-]+'
     TDEF = TLABEL <':'> <force_white> SLABEL
     TLABEL = #'[a-zA-z0-9_-]' | TLABEL <','> TLABEL
     op_white = #'\\s*'
     force_white = #'\\s+'"))

(comment
  (def dfa-example-string
    (-> "dfa.txt" io/resource io/file slurp))

  (def dfa-example-parse
    (dfa-parser dfa-example-string)))

(defn transform-tlabel
  ([x] (list (first x)))
  ([x & more] (apply concat x more)))

(def transform-slabel identity)

(defn transform-tdef
  [sym-list state]
  (apply hash-map (interleave sym-list (repeat state))))

(defn transform-sdef
  [state & transitions]
  (hash-map state (into {} transitions)))

(defn transform-automata
  [& maps]
  {:transition (into {} maps)})

(defn tree-to-transition-table
  [x]
  (insta/transform {:TLABEL transform-tlabel
                    :SLABEL transform-slabel
                    :TDEF transform-tdef
                    :SDEF transform-sdef
                    :AUTOMATA transform-automata} x))

(defn -main
  [dfa-fname start-state word & args]
  (println 
    "End state:" 
    (xt-function 
      (tree-to-transition-table (-> dfa-fname io/resource io/file slurp dfa-parser))
      start-state
      word)))
