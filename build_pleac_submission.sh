#!/bin/bash

set -e

cat << __HEADER__
# @@PLEAC@@_NAME
# @@SKIP@@ Clojure

# @@PLEAC@@_WEB
# @@SKIP@@ http://clojure.org/

# @@PLEAC@@_INTRO
# @@SKIP@@ You need version 1.3.0 or above for this code to work.

__HEADER__

ls | sort -n | grep -E '^[0-9]+_.*clj$' | xargs cat --
