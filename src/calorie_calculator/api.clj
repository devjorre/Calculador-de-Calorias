(ns calorie-calculator.api
  (:require [compojure.core :refer [defroutes GET POST]]
            [compojure.route :as route]
            [ring.middleware.json :refer [wrap-json-response wrap-json-body]]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [cheshire.core :as json]
            [calorie-calculator.core :as core]))

;; Funções auxiliares para respostas HTTP
(defn json-response [data & [status]]
  {:status (or status 200)
   :headers {"Content-Type" "application/json"}
   :body (json/generate-string data)})

(defn bad-request [message]
  (json-response {:error message} 400))

;; Endpoints da API
(defroutes app-routes
  (POST "/register-user" request
    (let [{:keys [height weight age gender]} (-> request :body)]
      (if (and height weight age gender)
        (do
          (core/register-user height weight age gender)
          (json-response {:message "Usuário cadastrado com sucesso!"}))
        (bad-request "Dados do usuário incompletos."))))

  (POST "/register-food-consumption" request
    (let [{:keys [food date quantity]} (-> request :body)]
      (if (and food date quantity)
        (let [calories (core/fetch-food-calories food)
              total-calories (* calories (/ quantity 100.0))]
          (core/register-food-consumption food date quantity total-calories)
          (json-response {:message (format "Consumo de %s registrado. Ganho de %.2f calorias." food total-calories)}))
        (bad-request "Dados de consumo de alimento incompletos."))))

  (POST "/register-exercise" request
    (let [{:keys [exercise date duration]} (-> request :body)]
      (if (and exercise date duration)
        (let [calories (core/fetch-exercise-calories exercise)
              total-calories (* calories (/ duration 60.0))]
          (core/register-exercise exercise date duration total-calories)
          (json-response {:message (format "Exercício %s registrado. Perda de %.2f calorias." exercise total-calories)}))
        (bad-request "Dados de exercício incompletos."))))

  (GET "/transactions" []
    (json-response (core/get-transactions)))

  (GET "/calorie-balance" []
    (json-response {:balance (core/get-calorie-balance)}))

  (GET "/user-data" []
    (json-response @core/user-data))

  (route/not-found (json-response {:error "Não encontrado"} 404)))

(def app
  (-> app-routes
      (wrap-json-response)
      (wrap-json-body {:keywords? true})
      (wrap-defaults (assoc-in site-defaults [:security :anti-forgery] false))))
