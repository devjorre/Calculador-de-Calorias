(ns calorie-calculator.core
  (:require [clojure.string :as str]))

;; Estado da aplicação
(def user-data (atom nil))
(def transactions (atom '()))

;; Funções de API Externa (Simuladas)
(def food-calories
  {"maca" 52
   "banana" 89
   "arroz" 130
   "feijao" 347
   "frango" 165})

(def exercise-calories
  {"corrida" 600
   "caminhada" 300
   "natacao" 500
   "ciclismo" 400})

(defn fetch-food-calories [food]
  (get food-calories (str/lower-case food) 100)) ; Valor padrão 100 se não encontrar

(defn fetch-exercise-calories [exercise]
  (get exercise-calories (str/lower-case exercise) 200)) ; Valor padrão 200 se não encontrar

;; Funções do Back-end (API)

(defn register-user [height weight age gender]
  (reset! user-data {:height height
                     :weight weight
                     :age age
                     :gender gender})
  @user-data)

(defn register-food-consumption [food date quantity total-calories]
  (let [transaction {:type :food
                     :name food
                     :date date
                     :quantity quantity
                     :calories total-calories}]
    (swap! transactions conj transaction)
    transaction))

(defn register-exercise [exercise date duration total-calories]
  (let [transaction {:type :exercise
                     :name exercise
                     :date date
                     :duration duration
                     :calories total-calories}]
    (swap! transactions conj transaction)
    transaction))

(defn get-transactions []
  @transactions)

(defn get-calorie-balance []
  (let [total-gained (reduce + (map :calories (filter #(= (:type %) :food) @transactions)))
        total-lost (reduce + (map :calories (filter #(= (:type %) :exercise) @transactions)))]
    (- total-gained total-lost)))

(defn get-user-data []
  @user-data)

(defn -main
  "Função principal para iniciar o servidor API."
  [& args]
  (println "Iniciando o servidor API..."))
