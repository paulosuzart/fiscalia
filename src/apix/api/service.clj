(ns apix.api.service
	(:require [apix.util :as u]
			  [apix.api.model :as m])
	(:use [lamina.core]
		  [korma.core]
		  [apix.api.model :only [single]]))


;; ## Auth information

(def auth-infoq
	(-> (select* m/security_key)
		(fields :user.user_id :user.username :security_key :security_key_id)
		(where m/active)
		(with m/user)
		(limit 1)))

(defn get-auth-info [id]
	(when-let [auth (single 
						(-> auth-infoq
							(where {:security_key_id id})))]
	(apix.api.model.AuthInfo. (:user.user_id auth) 
			   (:user.username auth) 
			   (:security_key_id auth)
			   (:security_key auth))))

(def ^{:private true} to-uidq 
	(-> (select* m/security_key) 
		(fields :user.user_id) 
		(with m/user)
		(where m/active)))

(defn to-uid 
	"Given the skeyid, tries to find the corresponding user id."
	[skeyid]
	(when-let [uid (single 
					  (-> to-uidq
						  (where {:security_key_id skeyid})))]
		(:user_id uid)))


;; ## Service

(defn create-service 
	"Inserts a new service into the database if it pass
	the constraints. Returns the service id."
	[s skeyid]
	(when-let [uid (to-uid skeyid)]
		(when-not (m/cons-new-service (assoc s :user_id uid))
			(:GENERATED_KEY (insert m/service (values (assoc s :user_id uid)))))))

(def ^{:private true} get-service-priceq 
	(-> (select* m/service)
		(fields :price)
		(where m/active)
		(limit 1)))

(defn get-service-price [uid sname sversion]
	(when-let [price (single 
						(-> get-service-priceq
							(where {:user_id uid 
									:service_name sname 
									:service_version sversion})))]
		(:price price)))

;; ## Ticket

;; ### Channels

;; New tickets are put into the `nticket-chan` to be processed.
(def nticket-chan (channel))

;; Processed tickets are put into `processed-chan` to be saved.
(def processed-chan (channel))

(defn save-ticket 
	"Put a ticket into the database."
	[ticket]
	(let [dt #(doto (new java.util.Date) (.setTime (new Long %)))]
		(insert m/ticket
			(values {:ticket_id (:ticketId ticket)
					 :service_user (:serviceUser ticket)
					 :user_id (:userId ticket)
					 :security_key_id (:securityKeyID ticket)
					 :service_name (:serviceName ticket)
					 :ticket_in (dt (:ticketIn ticket))
					 :ticket_time (dt (:ticketTime ticket))
					 :remote_addr (:remoteAddr ticket)
					 :service_version (:serviceVersion ticket)
					 :ticket_out (dt (:ticketOut ticket))})))
	ticket)

(defn process
	"Process the ticket grabing from the database missing information
	like price and user owner."
    [out]
    (fn [pre-ticket]
	(println "processando " pre-ticket)
	(let [start (System/currentTimeMillis)
	      gt #(get pre-ticket %)
	  	  tid (:ticketId pre-ticket)
	  	  uid (to-uid (:securityKeyID pre-ticket))
	  	  price (get-service-price uid (gt :serviceName) (gt :serviceVersion))
     	  ticket (-> pre-ticket 
		  		   		(assoc :userId (str uid)) 
		  				(assoc :ticketOut (str (System/currentTimeMillis)))
		  				(assoc :price price))]
			(enqueue out ticket)
			;;(save-ticket ticket)
		(println "TicketID" tid " processed in " (- (System/currentTimeMillis) start) ticket)
		ticket)))

;; Registers the creation ticket function to consume the new
;; tickets channel 'nticket-chan'
(receive-all nticket-chan (process processed-chan))
(receive-all processed-chan save-ticket)
;;(->> nticket-chan (map* process) (map* save-ticket))

(defn register-ticket
 "Save the ticket in a pending state for further processing."
 [params]
	(let [tid (m/next-ticket-id)
		  gt #(get params %)]
		(enqueue nticket-chan
			{:securityKeyID (gt "SecurityKeyID")
			 :serviceUser (gt "ServiceUser") 
			 :serviceName (gt "ServiceName")
			 :serviceVersion (gt "ServiceVersion")
			 :remoteAddr (:remoteAddr params)
			 :ticketTime (gt "TicketTime")
			 :ticketId tid
			 :ticketIn (System/currentTimeMillis)})
		tid))


;; ## User and Ticket relations

(defn get-user-summary 
	[kid suid sname sv]	1)

