(ns apix.admins
    (:use [apix.util])
    (:import [org.apache.commons.lang3 RandomStringUtils]
	    	 [java.net URLEncoder]))

(defn gen-keypair 
	"Generates a random pair of strings with len 20 and 40. 
	May be used as security key ID and security key."
	[]
	(let [gen #(RandomStringUtils/randomAlphanumeric %)]
	 (list (gen 20) (gen 40))))

(defn encode 
	"Encodes s with java.net.URLEncoder"
	[s]
	(URLEncoder/encode s))
