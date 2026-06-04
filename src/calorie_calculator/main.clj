(ns calorie-calculator.main
  (:require [ring.adapter.jetty :as jetty]
            [calorie-calculator.api :refer [app]]))

(defn -main
  "Função principal para iniciar o servidor da API."
  [& args]
  (println "Iniciando o servidor API na porta 3000...")
  (jetty/run-jetty app {:port 3000 :join? false}))
