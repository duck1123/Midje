;; -*- indent-tabs-mode: nil -*-

(ns midje.unprocessed
  (:use clojure.test
        [midje.internal-ideas.fakes]
        [midje.internal-ideas.fact-context :only [nested-fact-description]]
        [midje.ideas.background :only [background-fakes]]
        [midje.util laziness report]
        [midje.checkers.extended-equality :only [extended-=]]
        [midje.checkers.chatty :only [chatty-checker?]]
        [midje.checkers.util]
        [midje.util.namespace :only [immigrate]]
        [clojure.tools.macro :only [macrolet]]))
(immigrate 'midje.checkers)


(defmulti ^{:private true} check-result (fn [actual call] 
                                          (:desired-check call)))

(defn- fail [type actual call]
  {:type type
   :description (nested-fact-description)
   :binding-note (:binding-note call)
   :position (:position call)
   :actual actual
   :expected (:expected-result-text-for-failures call)})

(defmethod check-result :check-match [actual call]
  (cond (extended-= actual (:expected-result call))
        (report {:type :pass})

        (fn? (:expected-result call))
        (let [failure (fail :mock-expected-result-functional-failure actual call)]
          (report (if-not (chatty-checker? (:expected-result call))
                    failure
                    (merge failure
                      (let [chatty-result ((:expected-result call) actual)]
                        (if (map? chatty-result)
                          chatty-result
                          {:notes ["Midje program error. Please report."
                                   (str "A chatty checker returned "
                                     (pr-str chatty-result)
                                     " instead of a map.")]}))))))
    :else (report (assoc 
                  (fail :mock-expected-result-failure actual call) 
                  :expected (:expected-result call) ))))

(defmethod check-result :check-negated-match [actual call]
   (cond (not (extended-= actual (:expected-result call)))
         (report {:type :pass})

        (fn? (:expected-result call))
        (report (fail :mock-actual-inappropriately-matches-checker actual call))

        :else
        (report (fail :mock-expected-result-inappropriately-matched actual call))))

(defn expect*
  "The core function in unprocessed Midje. Takes a map describing a
  call and a list of maps, each of which describes a secondary call
  the first call is supposed to make. See the documentation at
  http://github.com/marick/Midje."
  [unprocessed-check local-fakes]
  (macrolet [(capturing-exception [form]
               `(try ~form
                  (catch Throwable e#
                    (captured-exception e#))))]
    (with-installed-fakes (concat (reverse (filter :data-fake (background-fakes))) local-fakes)
      (let [code-under-test-result (capturing-exception
                                    (eagerly
                                     ((:function-under-test unprocessed-check))))]
        (check-call-counts local-fakes)
        (check-result code-under-test-result unprocessed-check)
        :irrelevant-return-value))))