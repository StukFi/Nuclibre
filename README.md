## Introduction
-----
Nuclibre project contains the code for generating an sqlite nuclide database. Nuclibre is based on the *NUCLIB* database by
H. Toivonen and A. Pelican [1].

## Quick start
-----

Get the source code by cloning the repository
```
git clone https://gitlab.valtori.fi/stuk-private/nuklididata/nuclibre.git
```

Change working directory into the repository
```
cd nuclibre
```

Download the required libraries

(https://jar-download.com/artifacts/org.xerial/sqlite-jdbc/3.8.6)

(https://jar-download.com/artifacts/commons-cli/commons-cli/1.3.1)

and place them in a directory `libraries` under the working directory
```
mkdir libraries
cp commons-cli-1.3.1.jar ./libraries
cp sqlite-jdbc-3.8.6.jar ./libraries
```

build the nuclibre project with netbeans, or with ant
```
ant clean build
```

Download the ENSDF nuclear data

(https://www.nndc.bnl.gov/ensdfarchivals/distributions/dist23/ensdf_230403.zip)

and unzip the package to directory ENSDF. Concatenate the files to form one big data file
```
mkdir ENSDF
unzip ensdf_230403.zip -d ENSDF
cat ENSDF/*.ens > ensdf-all-230403.txt
```

Download the DDEP nuclear data in ENSDF format

(http://www.lnhb.fr/nuclides/All-nuclides_Ensdf.zip)

and unzip the package to directory DDEP.
```
mkdir DDEP
unzip  All-nuclides_Ensdf.zip -d DDEP
```

Now run the nuclibre application to produce a database called *nuclib.sqlite* from the ENSDF data file patched by the DDEP data
```
java -jar dist/nuclibre.jar -e ensdf-all-230403.txt -P DDEP -S LARA nuclib.sqlite
```

## References
-----
[1] Toivonen H., Pelican A., TTL-TECDOC-2008-010 NUCLIB - Nuclide Library for LINSSI Applications.