(defproject apix/apix "1.0.0"
  :dependencies [[apix/apix-util "1.0.1"] ;; TODO: bring this dependence to the projec
                 [org.clojure/clojure "1.2.1"]
                 [aleph "0.2.0"]
		 		         [org.clojure/data.json "0.1.1"]
				         [org.apache.commons/commons-lang3 "3.1"]
                 [korma "0.2.1"]
                 [mysql/mysql-connector-java "5.1.18"]
                 [ring/ring-core "1.0.1"]]
  :dev-dependencies [[lein-marginalia "0.7.0-SNAPSHOT"]]
  :jvm-opts ["-server" "-XX:+UseConcMarkSweepGC"])

