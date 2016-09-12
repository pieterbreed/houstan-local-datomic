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

;; This script will help the user figure out how to
;; configure the environment for this system to get
;; up and running.

;; we will need the following on the host system
;; - ansible
;; - vagrant

(with-tap!

  (def houstan-dir (some-> :houstan environ.core/env clojure.java.io/file))
  (defn diag-lines [ll] (->> ll (map diag!) dorun))

  ;; ----------------------------------------
  
  (diag-lines ["# ACCEPT"
               "An environment acceptance tool for `houstan`."])

  ;; ----------------------------------------
  
  ;; vagrant testing
  (if (not (ok! (utils/cmd-is-available "vagrant")
                "vagrant-is-installed"))
    (do
      (bail-out! "Vagrant has to be installed.")
      (System/exit 1))
    (ok! (utils/version-of-app-found #"Vagrant 1\.8\.."
                                     "vagrant" "-v")
         "correct-version-of-vagrant"
         :diag "I only tested with 1.8.5"))

  ;; ansible testing
  (if (not (ok! (utils/cmd-is-available "ansible")
                "ansible-is-installed"))
    (do
      (bail-out! "Ansible has to be installed.")
      (System/exit 1))
    (ok! (utils/version-of-app-found #"ansible 2\.1\..\.."
                                     "ansible" "--version")
         "correct-version-of-ansible"
         :diag "I only tested with ansible > 2.1.x.x"))

  ;; git testing
  (if (not (ok! (utils/cmd-is-available "git")
                "git-is-installed"))
    (do
      (bail-out! "Git has to be installed.")
      (System/exit 1))
    (ok! (utils/version-of-app-found #"git version 2\.7\.."
                                     "git" "version")
         "correct-version-of-git"
         :diag "I only tested with git > 2.7.x"))

  ;; ----------------------------------------

  (conch/programs git)

  ;; ----------------------------------------

  ;; houstan variable
  (if (not (ok! (and houstan-dir
                     (.exists houstan-dir))
                "houstan-var-points-to-dir"))
    (do (diag-lines
         ["## houstan-var-points-to-dir"
          " - You have to set the `HOUSTAN` environment variable."
          " - `HOUSTAN` env-var must point to a dir you may write to."
          " - It separates one `houstan` environment from another."])
        (bail-out! "HOUSTAN variable has to be set.")
        (System/exit 1)))

  ;; ----------------------------------------

  (def houstan-library-git
    (or (environ.core/env :houstan-library-git)
        (do
          (diag-lines ["You can override the HOUSTAN-LIBRARY-GIT repository using"
                       "HOUSTAN_LIBRARY_GIT environment variable."])
          "https://github.com/pieterbreed/houstan-library-local")))
  

  (def houstan-library-git-branch
    (or (environ.core/env :houstan-library-git-branch)
        (do
          (diag-lines ["You can override the HOUSTAN-LIBRARY-GIT _branch_"
                       "by specifying the HOUSTAN_LIBRARY_GIT_BRANCH"
                       "environment variable."])
          "0.0.2")))

  (git "clone" "--recursive"
       "-b" houstan-library-git-branch
       houstan-library-git
       (clojure.java.io/file houstan-dir "library"))
  (ok! ok "cloned-houstan-library")

  ;; ----------------------------------------

  (def vagrant-init-script-file (clojure.java.io/file houstan-dir "library" "houstan-local-vagrant" "init.clj"))
  (diag! (str "looking for vagrant-init-script at " (.getCanonicalPath vagrant-init-script-file)))
  (ok! (.exists vagrant-init-script-file)
       "vagrant-init-script-exists"
       :diag "If this fails, you have a broken library.")

  (diag! "Calling vagrant init script. This might take some time...")
  (conch/let-programs
      [init (.getAbsolutePath vagrant-init-script-file)]
    (init)))
