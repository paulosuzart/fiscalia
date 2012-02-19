(ns apix.api.config
	(:require [apix.util :as u]
			  [clojure.string :as s])
	(:use [korma.db :only [defdb]]))

;; All api required parameters in a set
(defonce required-params 
	{"RegisterTicket" #{"Action" "SecurityKeyID"   "ServiceUser" 
			               "ServiceName"   "ServiceVersion"
			               "TicketTime"      "Signature"}
	 "GetTicketInfo" #{"Action" "TicketId" "SecurityKeyID" "Signature"}
	 "GenerateBill" #{"Action" "SecurityKeyID" "Signature"}
	 "GetUserSummary" #{"Action" "SecurityKeyID" "Signature" 
	 					"ServiceName" "ServiceUser" "ServiceVersion"}
	 "CreateService" #{"Action" "ServiceName" "ServiceVersion" "ServicePrice"}})

;; Concentrates all errors exposed by ty API
(defonce api-errors
	{1021 [1021 "Pending Parameter(s): %s"]
	 2021 [2021 "Invalid signature: %s"]
	 1031 [1031 "Sorry. No such Action"]
	 2022 [2022 "User not found for SecurityKeyID: %s"]
	 3021 [3021 "Unable to create Service. See logs for detail."]})


(def db-url (str "//" (last (s/split (u/env "DATABASE_URL" "mysql://localhost:3306/fiscalia") #"//"))))
(def db-passwd (u/env "APIX_MYSQLPDW" ""))
(def db-usr (u/env "APIX_MYSQLUSR" "root"))

(println "Database URL used by Korma: "
	 db-url)

(defonce dbconf {:classname   "com.mysql.jdbc.Driver" 
               :subprotocol "mysql"
               :subname db-url
               :user     db-usr
               :password db-passwd})

(defdb *apixdb* dbconf)

;; TODO: ADD DB CONNECTION TEST
