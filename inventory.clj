#!/usr/bin/env boot

(set-env! :dependencies '[[pieterbreed/yoostan-lib "0.0.1-SNAPSHOT"]
                          [environ "1.0.3"]
                          [org.clojure/data.json "0.2.6"]])

;; ----------------------------------------

(require '[yoostan-lib.inventory :as yinv])
(require '[environ.core :as env])
(require '[clojure.data.json :as json])

;; ----------------------------------------


(let [vagrant-host ["vagrant" (env/env :houstan-hostname) 22]] 
  (as-> yinv/empty-inventory $
    (yinv/target-> $ vagrant-host {"ansible_user" "vagrant"
                                   "ansible_host" (env/env :houstan-hostname)})

    (yinv/group-> $ "datomic-transactors" 
                  {"datomic_transactor_properties"
                   {"protocol" "dev"
                    "host" "0.0.0.0"
                    "alt-host" (env/env :houstan-hostname)
                    "port" "4334"
                    "license-key" (env/env :datomic-license-key)
                    "memory-index-threshold" "32m"
                    "memory-index-max" "256m"
                    "object-cache-max" "128m"
                    "data-dir" "/var/lib/datomic"
                    "log-dir" "/var/log/datomic"
                    "pid-file" "/var/run/datomic"}})
    (yinv/target->group-> $ vagrant-host "datomic-transactors")
    (yinv/inv->ansible--list $)
    (json/write-str $)
    (println $)))
