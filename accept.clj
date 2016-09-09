#!/usr/bin/env boot

(set-env! :dependencies '[[pieterbreed/tappit "0.9.8"]
                          [me.raynes/conch "0.8.0"]
                          [environ "1.0.3"]])


;; ----------------------------------------

;; warnings, drama. we need an unreleased version of clojure for this script
(require 'environ.core)
(when (not (re-find #"1\.9\.0"
                    (environ.core/env :boot-clojure-version)))
  (println "# Set ENV variables like this:")
  (println "# $ export BOOT_CLOJURE_VERSION=1.9.0-alpha10")
  (println "Bail out! Requires BOOT_CLOJURE_VERSION=1.9.0-alpha10 or higher")
  (System/exit 0))

;; ----------------------------------------

(require '[tappit.producer :refer [with-tap! ok]])
(require '[me.raynes.conch :as conch])

;; This script will help the user figure out how to
;; configure the environment for this system to get
;; up and running.

;; we will need the following on the host system
;; - ansible
;; - vagrant

(defn cmd-is-available
  "Tests whether a UNIX shell command can be found."
  [appname]
  (conch/with-programs [which]
    (let [res (which appname {:throw false
                              :verbose true})]
      (= 0 (deref (:exit-code res))))))

(defn version-of-app-found
  "Runs <command> <-version> and tests the output against a regex"
  [r cmd p]
  (conch/let-programs
      [c cmd]

    (let [res (c p {:throw false
                    :verbose true})
          exit-code (deref (:exit-code res))]
      
      (if (not= 0 exit-code) false
          (boolean (re-find r (:stdout res)))))))


(with-tap!
  ;; vagrant testing
  (when (ok! (cmd-is-available "vagrant")
             "vagrant-is-installed")
    (ok! (version-of-app-found #"Vagrant 1\.8\.."
                               "vagrant" "-v")
         "correct-version-of-vagrant"
         :diag "I only tested with 1.8.5"))

  ;; ansible testing
  (when (ok! (cmd-is-available "ansible")
             "ansible-is-installed")
    (ok! (version-of-app-found #"ansible 2\.1\..\.."
                               "ansible" "--version")
         "correct-version-of-ansible"
         :diag "I only tested with ansible > 2.1.0.0")))
