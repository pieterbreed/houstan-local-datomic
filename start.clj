#!/usr/bin/env boot

(set-env! :dependencies '[[pieterbreed/tappit "0.9.8"]
                          [me.raynes/conch "0.8.0"]
                          [environ "1.0.3"]
                          [pieterbreed/yoostan-lib "0.0.1-SNAPSHOT"]])


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
(require '[me.raynes.conch.low-level :as sh])
(require '[yoostan-lib.utils :as utils])

;; ----------------------------------------

(let [bsf (-> boot.core/*boot-script* clojure.java.io/file)]
  (def work-dir
    (-> (or (if (.isAbsolute bsf) bsf)
            (clojure.java.io/file (System/getProperty "user.dir")
                                  bsf))
        .getParentFile
        .getCanonicalPath)))

;; ----------------------------------------

(with-tap!

  (defn diag-lines [ll] (->> ll (map diag!) dorun))

  ;; ----------------------------------------

  (diag! (str "working directory at: " work-dir))

  ;; ansible testing
  (if (not (ok! (utils/cmd-is-available "ansible-playbook")
                "ansible-is-installed"))
    (do
      (bail-out! "Ansible (ansible-playbook) has to be installed.")
      (System/exit 1))
    (ok! (utils/version-of-app-found #"ansible-playbook 2\.1\..\.."
                                     "ansible-playbook" "--version")
         "correct-version-of-ansible"
         :diag "I only tested with ansible > 2.1.x.x"))

  ;; ----------------------------------------

  (diag! "Running ansible-playbook ... start.yml")
  (conch/with-programs [ansible-playbook]
    (let [playbook-folder work-dir
          ansible-proc (ansible-playbook "-i" (.getCanonicalPath
                                               (clojure.java.io/file playbook-folder
                                                                     "inventory.clj"))
                                         (.getCanonicalPath
                                          (clojure.java.io/file playbook-folder
                                                                "start.yml"))
                                         {:seq true
                                          :throw false
                                          :verbose true})]
      (diag-lines (-> ansible-proc :proc :out))
      (if (not (=! 0 (-> ansible-proc :exit-code deref)
                   "ansible-playbook-start-succeeded"))
        (do 
          (bail-out! "Ansible start playbook failed")
          (System/exit 1)))))





  )
