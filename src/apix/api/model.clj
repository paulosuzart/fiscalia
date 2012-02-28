(ns apix.api.model
	(:use [korma.core]))

; # Entities tied to the database
; ## AuthInfo is a record used to wrapp the authentication data around 
;    the app.
(defrecord AuthInfo [id username skeyid skey])

;; ## User entity
(defentity user
	(pk :user_id))

;; ## Securtity key
;; Used to get the auth information from
;; from the database in for every request.
(defentity security_key
	(pk :security_key_id)
	(belongs-to user {:fk :user_id}))

;; ## Service 
;; Represents a service in the database
(defentity service)

;; ## Ticket
;; Represents the tickets
(defentity ticket)

;;	"A base query. Used to distiguish the active rows in the
;;	database."
(defonce active 
	(not= nil :deleted_at))

(defn next-ticket-id 
	"TODO: Put in a protocol.
	By now it calls the `UUID();` 
	function from mysql."
	[] 
	(-> (exec-raw ["SELECT UUID();"] true) first first val))

(defmacro singleresult 
	"Helper macro to extract only a single result from
	a query. Allows more complex structured passed to it.
	See the simpler version `single`."
	[entity fields & condi]
	`(when-let [res# (-> (select* ~entity)
		 				 (fields ~@fields)
		 				 ~@condi
		 				 (limit 1)
		 				 (exec))]
		 		(first res#)))

(defmacro single
	"Simply gets the frist result from evaluating the query."
[query]
	`(first (-> ~query (exec))))

;; ## Constraints

(defn cons-new-service
	"Constraint called before insert a new Service.
	Every service is uniq for by name, version and user."
	[s]
	(not (empty? 
			(select service 
				(where (= :service_name (:service_name s)))
				(where (= :service_version (:service_version s)))
				(where (= :user_id (:user_id s)))))))