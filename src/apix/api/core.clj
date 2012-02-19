(ns apix.api.core
	(:require [apix.signer :as sig]
		      [apix.api.config :as c]
		      [apix.api.view :as v]
		      [apix.api.service :as s]
		      [apix.util :as u])
	(:use [lamina.core]
		  [aleph.http]
		  [ring.middleware.file]
		  [ring.middleware.file-info]))


;; ## API Actions

(defmulti act (fn [_ params _] (get params "Action")))

;; # Action: `CreateService`
;;    * `ServiceName` - Valid service name
;;    * `ServiceVersion` - Valid service version
;;    * `ServicePrice` - any numeric value
;;	  * `SercurityKeyID` - Security Key ID used to sign the request
;;    * `Signature` - Signature of the request
;;
;;    Returns: `{"ServiceId" : [numericid]}`
;;    
;;    or: `{"code" : 3021, "msg" : "Unable to create Service. See logs for detail."}`
(defmethod act "CreateService" 
	[channel params request]
	(if-let [service-id (s/create-service {:service_name (get params "ServiceName") 
					 :service_version (get params "ServiceVersion")
					 :price (get params "ServicePrice")}
					 (get params "SecurityKeyID"))]
		(enqueue-and-close channel (v/ok {:ServiceId service-id}))
		(enqueue-and-close channel (v/wrap-err 3021))))

(defmethod act "ListServices"
	[channel params request]
	(enqueue-and-close (v/ok 
		[{:ServiceName "MortgageService" :ServiceVersion "V1_0" :Price 0.001}
		 {:ServiceName "SampleService" :ServiceVersion "1.1" :Price 0.22}])))

; Get the summary usage by user and service service version.
(defmethod act "GetUserSummary" 
    [channel params request]
    (if-let [summary (s/get-user-summary (get params "SecurityKeyID") 
    								(get params "ServiceUser")
    								(get params "ServiceName") 
    								(get params "ServiceVersion"))]
    	(enqueue-and-close channel (v/ok summary))
    	(enqueue-and-close channel (v/nfound {:msg "No summary found for user"}))))

;; # Action: `RegisterTicket`
;;    * `SecurityKeyID` - A user should have one or more security key identifiers to ;;    identify the security key used to sign the request. Only valid keys should be
;;    used (not marked as deleted).
;;    * `ServiceUser` - A user should pass on his user. This user represents possible ;;    costumers of our users.
;;    * `ServiceName` - The name of the service beeing billed.
;;    * `ServiceVersion` - Since Bill IT allows users to have more than one servicer ;;    registered, they should identify the service version.
;;    * `TicketTime` - In the for of `2011-10-12 05:30:22 -0300`
;;    * `Signature` - The signature itself.

;; Returns: `{"ticket-uuid" : "The id for the created ticket"}`

;; `X-Billit-Api-Time` : Time spent to register the ticket in milliseconds - *as ;;    HTTP header*

;; Writes the ticket to the back end. Writes all the incomming parameters through 
;; s/register-ticket. 
;; Only adds `:RemoteAddr` to the ticket."
(defmethod act "RegisterTicket"
    [channel params request]
    (let [remote (or (get "x-forwarded-for" (:headers request)) 
    				 (:remote-addr request))
		  tid (s/register-ticket (assoc params :remoteAddr remote))]
            (enqueue channel (v/ok {:ticket-uuid (str tid)}))))


;; # Middlewares

(defn wrap-all-exceptions
	"Handle any erros and reply a default message."
	[handler]
	(fn [channel request]
		(try
			(handler channel request)
		(catch Throwable t 
			(do (prn t)
				(enqueue channel 
					{:status 500 
					 :body "Ooops! Something went wrong! Contact our support."}))))))

(defn wrap-params-filter
	"Wraps the handler and validate if all required params
	are present."
	[handler required]
	(fn [channel request]
		(run-pipeline (request-params request)
			(fn [params]
				(if-let [rrequired (get required (get params "Action"))]
					(if-let [pending (seq (filter #(not (contains? params %)) rrequired))]
						(enqueue channel (v/wrap-err 1021 (reduce str pending)))
						(handler channel request))
					(enqueue channel (v/wrap-err 1031)))))))

(defn wrap-verify-signature
	"Wraps the handler and verifies the signature. Adds user map to request"
	[handler]
	(fn [channel request]
		(run-pipeline (request-params request)
			(fn [params]
				(let [key-id (get params "SecurityKeyID")
					  signature (get params "Signature")]
					(if-let [authinfo (s/get-auth-info key-id)]
							(if (= (sig/sign-params (:skey authinfo) params) signature)
								(handler channel (assoc request :apix-user (:username authinfo)))
								(enqueue channel (v/wrap-err 2021 signature)))
							(enqueue channel (v/wrap-err 2022 key-id))))))))

(defn wrap-apitime
	"Adds an HTTP Header X-Billit-Api-Time. The total amount of time the API took processing."
	[handler]
	(fn [chan request]
		(let [start (System/currentTimeMillis)
			  mchan (channel)
			  resp (read-channel mchan)]
			  (handler mchan request)
			  (enqueue-and-close chan 
				    (update-in @resp [:headers :x-billit-api-time] 
					    (fn [_] (str (- (System/currentTimeMillis) start))))))))


;; # Handlers

(defn handler [channel request]
	(run-pipeline (request-params request)
		(fn [params]
			(act channel params request))))

;; # Main Application that assemblems middlewares and handlers.

(def api 
	(-> (wrap-aleph-handler 
			(-> handler
			;(wrap-verify-signature)
			(wrap-params-filter c/required-params)
			(wrap-apitime)
			(wrap-all-exceptions)))
		(wrap-file "public" [:index-files? false])
		wrap-file-info))

(defn -main [& m]
	(let [mode (keyword (or (first m) :dev))
          port (Integer. (u/env "PORT" "8080"))]
        (println mode port)
    	(start-http-server (wrap-ring-handler api) {:port port})))

;curl -G http://localhost:5000/ -d Action='RegisterTicket' -d 'SecurityKeyID=t5xC1SV2ISUiKfzIhPS2' -d 'ServiceUser=SampleuserDN' -d 'ServiceName=MortgageService' -d 'ServiceVersion=V1_0' -d 'TicketTime=2011-10-12+05%3A30%3A22+-0300' -d 'Signature=Ea75zmXp%2BdkJG%2F3m1IX%2FPiDIs%2B4%3D'  -v

;curl -G http://evening-meadow-8857.herokuapp.com/ -d 'SecurityKeyID=t5xC1SV2ISUiKfzIhPS2' -d 'User=SampleuserDN' -d 'ServiceName=MortgageService' -d 'ServiceVersion=V1_0' -d 'ServiceAction=calculate' -d 'ServiceResult=success' -d 'TicketTime=2011-10-12+05%3A30%3A22+-0300' -d 'Signature=Ea75zmXp%2BdkJG%2F3m1IX%2FPiDIs%2B4%3D' -d 'APIVersion=1.1' -v
    
 ;curl -G http://localhost:5000/ -d Action='GetTicketInfo' -d 'SecurityKeyID=t5xC1SV2ISUiKfzIhPS2' -d 'TicketId=90' -d 'Signature=iWcKmKr2Jp5rQK0bF6ywRFOGlDU%3D'  -v

 ;curl -G http://localhost:5000/ -d Action='RegisterTicket' -d 'SecurityKeyID=t5xC1SV2ISUiKfzIhPS2' -d 'ServiceUser=SampleuserDN' -d 'ServiceName=MortgageService' -d 'ServiceVersion=V1_0' -d 'TicketTime=1324241325488' -d 'Signature=awJIuEYeqBqTaG4ecn2rPRyzibc%3D'  -v

 ;curl -G http://localhost:5000/ -d Action='GetUserSummary' -d 'SecurityKeyID=t5xC1SV2ISUiKfzIhPS2' -d 'ServiceUser=SampleuserDN' -d 'ServiceName=MortgageService' -d 'ServiceVersion=V1_0' -d 'Signature=nP0C2%2FiE0PIigilhhVs3Kg3LSB4%3D'  -v

 ; uid 2
 ; curl -G http://localhost:5000/ -d Action='RegisterTicket' -d 'SecurityKeyID=XncG6CsDOX7EyZbUvQNo' -d 'ServiceUser=SampleuserDN' -d 'ServiceName=MortgageService' -d 'ServiceVersion=V1_0' -d 'TicketTime=1324241325488' -d 'Signature=awJIuEYeqBqTaG4ecn2rPRyzibc%3D'  -v

 ; curl -G http://localhost:5000/ -d Action='CreateService' -d 'SecurityKeyID=t5xC1SV2ISUiKfzIhPS2' -d 'ServiceName=SampleService' -d 'ServiceVersion=10' -d ServicePrice=0.25 -d 'Signature=HhweeaulTawGYAsyPRUiwYWI2Io%3D'  -vt5xC1SV2ISUiKfzIhPS2

 ; curl -G http://localhost:5000/ -d Action='CreateService' -d 'SecurityKeyID=t5xC1SV2ISUiKfzIhPS2' -d 'ServiceName=SampleService' -d 'ServiceVersion=10' -d 'Signature=Qrmkx6meCJ2DwwcrrO3Q%2FvPiYnE%3D' -d 'ServicePrice=.22'