(defproject net.kronkltd/midje "1.3-alpha2-SNAPSHOT"
  :description "A TDD library for Clojure, with an emphasis on mocks"
  :dependencies [[org.clojure/clojure "1.3.0-RC0"]
                 [org.clojure.contrib/combinatorics "1.3.0-alpha4"]
                 [org.clojure.contrib/seq "1.3.0-alpha4"]
                 [org.clojure.contrib/math "1.3.0-alpha4"]
                 [org.clojure.contrib/ns-utils "1.3.0-alpha4"]
                 [org.clojure/algo.monads "0.1.0"]
                 [ordered "0.3.0"]
                 [unifycle "0.5.0"]]
  :exclusions [org.clojure/contrib
               org.clojure/clojure-contrib])
