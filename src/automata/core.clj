(ns automata.core
  (:require [clojure.java.io :as io])
  (:gen-class))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))

(def dfa-example-string
  (-> "dfa.txt" io/resource io/file slurp))
