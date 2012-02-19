(ns cackefile
  (:use [clojure.string :only [join]])
  (:require [cljs.nodejs :as n]))

(def fs (n/require "fs"))
(def process (n/require "child_process"))

; state the app files here
(def files ["fiscalia"])

(def app-files (map #(str "app/" % ".coffee") files))

(defn unlink [tmp-dir]
  (fn [err stdout stderr]
    (.unlink fs tmp-dir)
    (println "DONE BUILD COFFEE!")))


(defn docompile
  "supposed to be called after save as a callback."
  [targ-dir tmp-dir cb]
  (fn [err]
    (when-not err
      (.exec process (str "coffee -c -o " targ-dir " " tmp-dir) cb))))

(defn save-tmp [tmp-src content cb]
  (.writeFile fs tmp-src content cb))

(defn build [tmp-src tmp-dir targ-dir]
  (let [contents (apply array app-files)
        cter (atom (count app-files))]
    (amap contents idx ret
        (.readFile fs (aget contents idx) "utf-8"
          (fn [err c]
            (when-not err
                (aset ret idx c)
                (swap! cter dec)
                (when (= 0 @cter)
                  (save-tmp tmp-src 
                    (join "\n" ret) 
                    (docompile targ-dir tmp-dir 
                      (unlink tmp-dir))))
                  c))))))

(build "cbuild/fiscalia.coffee" "cbuild" "public/js")

