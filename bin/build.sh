#!/bin/sh

bindir=$(dirname $(realpath $0))
projectdir=$(dirname $bindir)
sourcedir=${projectdir}/src
builddir=${projectdir}/build
targetjar=${projectdir}/nuclibre.jar

[ ! -f ${projectdir}/README.md ] && {
  echo "ERROR: README.md not found in project root directory ${projectdir}"
  exit 1
}

libraries=$(ls ${projectdir}/libraries/*.jar | tr '\n' :)

mkdir -p ${builddir}
javac -d ${builddir} -cp ".:${libraries}:src" \
  ${sourcedir}/fi/stuk/ensdf/*.java \
  ${sourcedir}/fi/stuk/ensdf/type/*.java \
  ${sourcedir}/fi/stuk/ensdf/record/*.java \
  ${sourcedir}/fi/stuk/nuclibre/*.java
[ $? = 0 ] || exit 1

cd $builddir && jar -cvfm ${targetjar} ${projectdir}/nuclibre-manifest.mf *

echo "Built ${targetjar}"
