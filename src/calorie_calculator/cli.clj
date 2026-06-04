(ns calorie-calculator.cli
  (:gen-class)
  (:require [clj-http.client :as client]
            [cheshire.core :as json]
            [clojure.string :as str]))

(def api-base-url "http://localhost:3000")

(defn- parse-json-response [response]
  (json/parse-string (:body response) true))

(defn- make-post-request [endpoint data]
  (try
    (let [response (client/post (str api-base-url endpoint)
                                {:headers {"Content-Type" "application/json"}
                                 :body (json/generate-string data)
                                 :throw-exceptions false})]
      (parse-json-response response))
    (catch Exception e
      {:error (str "Erro ao conectar à API: " (.getMessage e))}))) 

(defn- make-get-request [endpoint]
  (try
    (let [response (client/get (str api-base-url endpoint)
                               {:throw-exceptions false})]
      (parse-json-response response))
    (catch Exception e
      {:error (str "Erro ao conectar à API: " (.getMessage e))}))) 

(defn register-user-command [height weight age gender]
  (let [response (make-post-request "/register-user" {:height height :weight weight :age age :gender gender})]
    (if (:error response)
      (println "Erro:" (:error response))
      (println "Mensagem:" (:message response)))))

(defn register-food-consumption-command [food date quantity]
  (let [response (make-post-request "/register-food-consumption" {:food food :date date :quantity quantity})]
    (if (:error response)
      (println "Erro:" (:error response))
      (println "Mensagem:" (:message response)))))

(defn register-exercise-command [exercise date duration]
  (let [response (make-post-request "/register-exercise" {:exercise exercise :date date :duration duration})]
    (if (:error response)
      (println "Erro:" (:error response))
      (println "Mensagem:" (:message response)))))

(defn get-transactions-command []
  (let [response (make-get-request "/transactions")]
    (if (:error response)
      (println "Erro:" (:error response))
      (if (empty? response)
        (println "Nenhuma transação registrada.")
        (doall (map (fn [t]
                      (if (= (:type t) "food")
                        (println (format "[%s] Alimento: %s | Quantidade: %.2fg | Ganho: +%.2f kcal"
                                         (:date t) (:name t) (:quantity t) (:calories t)))
                        (println (format "[%s] Exercício: %s | Duração: %.2fmin | Perda: -%.2f kcal"
                                         (:date t) (:name t) (:duration t) (:calories t))))) response))))))

(defn get-calorie-balance-command []
  (let [response (make-get-request "/calorie-balance")]
    (if (:error response)
      (println "Erro:" (:error response))
      (let [balance (:balance response)]
        (println (format "Saldo atual: %.2f kcal" balance))
        (if (> balance 0)
          (println "Você está em superávit calórico (ganhando peso).")
          (if (< balance 0)
            (println "Você está em déficit calórico (perdendo peso).")
            (println "Você está em manutenção calórica."))))))) 

(defn get-user-data-command []
  (let [response (make-get-request "/user-data")]
    (if (:error response)
      (println "Erro:" (:error response))
      (if (empty? response)
        (println "Nenhum dado de usuário cadastrado.")
        (do
          (println (format "Altura: %.2fm" (:height response)))
          (println (format "Peso: %.2fkg" (:weight response)))
          (println (format "Idade: %d anos" (:age response)))
          (println (format "Sexo: %s" (:gender response))))))))

(defn print-help []
  (println "\n--- Comandos CLI da Calculadora de Calorias ---")
  (println "Uso: lein run cli <comando> [argumentos]")
  (println "Comandos disponíveis:")
  (println "  register-user <altura> <peso> <idade> <sexo>")
  (println "  register-food <alimento> <data> <quantidade>")
  (println "  register-exercise <exercicio> <data> <duracao>")
  (println "  get-transactions")
  (println "  get-calorie-balance")
  (println "  get-user-data")
  (println "  help"))

(defn -main [& args]
  (let [command (first args)
        cmd-args (rest args)]
    (case command
      "register-user" (register-user-command (Float/parseFloat (nth cmd-args 0)) (Float/parseFloat (nth cmd-args 1)) (Integer/parseInt (nth cmd-args 2)) (nth cmd-args 3))
      "register-food" (register-food-consumption-command (nth cmd-args 0) (nth cmd-args 1) (Float/parseFloat (nth cmd-args 2)))
      "register-exercise" (register-exercise-command (nth cmd-args 0) (nth cmd-args 1) (Float/parseFloat (nth cmd-args 2)))
      "get-transactions" (get-transactions-command)
      "get-calorie-balance" (get-calorie-balance-command)
      "get-user-data" (get-user-data-command)
      "help" (print-help)
      (print-help))))
