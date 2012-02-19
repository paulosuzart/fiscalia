(ns apix.api.view
	(:require [apix.api.config :as config])
	(:use [clojure.data.json]))

(defrecord Erro [code ^String msg ^String level])

(defn wrap-err
	"writes content map in the accept format.
	TODO: now printing json only."
	([code msgf]
	(let [[code msg] (get config/api-errors code)]
		{:status 403
		 :body (json-str {:code code
						  :msg (format msg msgf)})
		 :headers {"Content-Type" "application/json"}}))
						   ;:level level})}))
	([code]
	(wrap-err code nil)))

(defn render
	"Renders the obj as json."
	[status obj]
	{:status status
	 :body (json-str obj)
	 :headers {:content-type "application/json"}})

(defn ok [obj]
	(render 200 obj))

(defn nfound [obj]
	(render 404 obj))

