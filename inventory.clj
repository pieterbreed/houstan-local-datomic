#!/usr/bin/env boot

(set-env! :dependencies '[[pieterbreed/ansible-inventory-clj "0.1.1"]
                          [environ "1.0.3"]
                          [org.clojure/data.json "0.2.6"]])

;; ----------------------------------------

(require '[ansible-inventory-clj.core :as yinv])
(require '[environ.core :as env])
(require '[clojure.data.json :as json])

;; ----------------------------------------


(let [vagrant-host ["vagrant" (env/env :houstan-hostname) 22]] 
  (as-> yinv/empty-inventory $
    (yinv/add-target $ vagrant-host {"ansible_user" "vagrant"
                                     "ansible_host" (env/env :houstan-ip)})

    (yinv/add-group $ "datomic-transactors" 
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
                      "pid-file" "/var/run/datomic/datomic_transactor.pid"}})
    (yinv/add-target-to-group $ vagrant-host "datomic-transactors")
    (yinv/make-ansible-list $)
    (json/write-str $)
    (println $)))
