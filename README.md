API for registering users of you API. 

@paulosuzart

`model` package, `entity` and `query` strategy for MYSQL
=======================================================

In the model namespace, there is a `entities.clj` file tha concentrate all entities definitions and relations. This `ns` is not supposed to be used/required by the applications but by query specific namespaces such as `apix.model.security_key`.

The intention behind this approach is to use the same pattern of query by namespace. For example:

	(ns apix.api.core
	    (:require [apix.model.security_key :as sk]))
	    ...
	    (first (sk/by-id 21)) ;;

In the future we can standardize a `protocol` for queries or let things flaxible as it is right now.

Another point is to diminish the korma code's contact surface with the database, using compositions. For example:
	;; in the security_key ns
	(def base (select* security-key))

    (defn- id-is [q id] (where q {:security_key_id id}))

    (defn by-id [id]
	    (-> base
		    (fields :security_key :user.username :user.user_id)
		    (with user)
		    (id-is id)
		    (exec)))

Instead of having `where` being used around to filter, it is just put in a single place. The function `id-is` in this case.

In times of refactoring we should experience less pain if compared with a larger contact surface with the database.

**It is also a good practice to wrap a query result in a `defrecord`.**

* * *

API Usage
=========

**Action:** `RegisterTicket`
----------------------------------

**Description:** Returns the generated ticket id for futher checks.

**Request Parameters :**

* `SecurityKeyID` - A user should have one or more security key identifiers to identify the security key used to sign the request. Only valid keys should be used (not marked as deleted).
* `ServiceUser` - A user should pass on his user. This user represents possible costumers of our users.
* `ServiceName` - The name of the service beeing billed.
* `ServiceVersion` - Since Bill IT allows users to have more than one servicer registered, they should identify the service version.
* `TicketTime` - In the for of `2011-10-12 05:30:22 -0300`
* `Signature` - The signature itself.

Response Parameters (in json format)

* `ticket-uuid (string)`: The id for the created ticket
* `X-Billit-Api-Time` : Time spent to register the ticket in milliseconds - *as HTTP header*

Examples:

**Example Requet**
    curl -G http://apix.defthoughts.com/ -d Action=RegisterTicket -d 'SecurityKeyID=t5xC1SV2ISUiKfzIhPS2' -d 'ServiceUser=SampleuserDN' -d 'ServiceName=MortgageService' -d 'ServiceVersion=V1_0' -d 'TicketTime=2011-10-12+05%3A30%3A22+-0300' -d 'Signature=Ea75zmXp%2BdkJG%2F3m1IX%2FPiDIs%2B4%3D'  -v

**Example Response**

    {"ticket-uuid":"14"} 
     #http header: X-Billit-Api-Time: 8

Notes:
--------
APIX API returns json only. xml may be added in the future.

Signing API Calls
=================

There is a separate namespace with a simple set of functions to do so. We use an approach similar to AWS signatures. Our signatures should be assembled as following:

1. All parameters should ordered alphabetically.
1. Parameters names are then joined with their values and concatenated with no spaces. ex.: A given query string ?
1. ServiceName=MortigageService&ServiceVersion=V1_0 is signed after transformed toServiceNameMortigageServiceServiceVersionV1_0.
1. The secret key should be used to sign this content using HmacSHA1.
1. The returning value forms the signature to be put in the query string with the param name Signature.

The same procedure runs in the server side to check every request.

Notes:
--------
By now the API runs without SSL. It should be added soon.

A sign util can be found at apix-util project. Source at apix-util

Every APIX It user possess a Security Key Pair. Like AWS, the user signs requests with its Security Key, while sending the Security Key Id in the request.

A Security Key is a 320 bits sequence, while the Security Key ID is a 160 bits sequence generated by APIX It admins.

Forking
=======

There is a `db` folder with MySQL Workbench. Set up the schema and run the project with `foreman start`. We have a lot to do!