#!/bin/sh

bindir=$(dirname $(realpath $0))
projectdir=$(dirname $bindir)

[ ! -f ${projectdir}/README.md ] && {
  echo "ERROR: README.md not found in project root directory ${projectdir}"
  exit 1
}

targetjar=${projectdir}/nuclibre.jar
libraries=$(ls ${projectdir}/libraries/*.jar | tr '\n' : | sed s/:$//)
classpath=".:${libraries}:${targetjar}"

exec java -cp ${classpath} fi.stuk.nuclibre.Main $*
