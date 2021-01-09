#!/bin/bash
function javaconfigpath {
# Manage JAVA_HOME variable
  if [[ -z "$JAVA_HOME" ]]; then
    echo 'export JAVA_HOME=/usr/java/jdk-13.0.1' >> $HOME/.bashrc
    export JAVA_HOME=/usr/java/jdk-13.0.1
    export PATH=$JAVA_HOME/bin:$PATH
    echo "[*] JAVA_HOME = $JAVA_HOME"
  fi

  exec "$BASH"

}
