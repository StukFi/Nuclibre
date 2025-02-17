## Introduction
-----
Nuclibre project contains the code for generating an sqlite nuclide database. Nuclibre is based on the *NUCLIB* database by
H. Toivonen and A. Pelican [1].

The nuclibre code is used to generate the database from ENSDF (evaluated nuclear structure data file) format data. It is recommended to use the data files available from the Brookhaven National Laboratory to provide the base data, and to patch it using the data available from the decay data evaluation project at Laboratoire National Henri Becquerel.

## Quick start
-----
Make sure you have JDK 8  or later installed
```
javac --version
```

Get the nuclibre source code by cloning the repository
```
git clone https://github.com/StukFi/Nuclibre.git
```

Change working directory into the repository
```
cd Nuclibre
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
cp nuclibre-manifest.mf manifest.mf
ant clean jar
cp dist/nuclibre.jar .
```

or with `javac` directly
```
mkdir build
javac -d build -cp ".:libraries/commons-cli-1.3.1.jar:libraries/sqlite-jdbc-3.8.6.jar:src" src/fi/stuk/ensdf/*.java src/fi/stuk/ensdf/type/*.java src/fi/stuk/ensdf/record/*.java src/fi/stuk/nuclibre/*.java
cd build
jar -cvfm ../nuclibre.jar ../nuclibre-manifest.mf *
cd ..
```

Note that on windows, you have to give the following commands for javac compilation
```
mkdir build
javac -d build -cp ".;libraries\commons-cli-1.3.1.jar;libraries\sqlite-jdbc-3.8.6.jar;src" src\fi\stuk\ensdf\*.java src\fi\stuk\ensdf\type\*.java src\fi\stuk\ensdf\record\*.java src\fi\stuk\nuclibre\*.java
cd build
jar -cvfm ..\nuclibre.jar ..\nuclibre-manifest.mf *
cd ..
```

Download the ENSDF nuclear data

(https://www.nndc.bnl.gov/ensdfarchivals/distributions/dist23/ensdf_230403.zip)

and unzip the package to directory ENSDF. Concatenate the files to form one big data file
```
mkdir ENSDF
unzip ensdf_230403.zip -d ENSDF
cat ENSDF/ensdf.*  > ensdf-all-230403.txt
```
On windows
```
type ENSDF\ensdf.*  > ensdf-all-230403.txt
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
java -jar nuclibre.jar -e ensdf-all-230403.txt -P DDEP -S LARA nuclib.sqlite
```

## Quick start with Maven

Download nuclear data as instructed in the previous section.

Build application JAR

    $ mvn package
    ...
    [INFO] Building jar: target/nuclibre-1.0.0-SNAPSHOT-jar-with-dependencies.jar

Execute JAR to create ``nuclib.sqlite``

    $ java -jar target/nuclibre-1.0.0-SNAPSHOT-jar-with-dependencies.jar -e ensdf-all-230403.txt -P DDEP -S LARA nuclib.sqlite


## Support for other SQL databases

Postgres and MySQL/MariaDB support has been tested. 

SQLite database can be exported to Postgres using pgloader tool:
```
createdb nuclibre
pgloader ./nuclibre.sqlite postgresql://user:pass@host/nuclibre
```

To export the SQLite database to MySQL/MariaDB, use the command:
```
sqlite3 nuclib.sqlite .dump | tr -d '"' | sed 1,2d | sed '$ d' | mysql -u <uname> -p <db>
```
Note that the database schema must already exist in MySQL/MariaDB.


## References
-----
[1] Toivonen H., Pelican A., TTL-TECDOC-2008-010 NUCLIB - Nuclide Library for LINSSI Applications.

## License
-----
Use of this work is governed by an MIT-style license that is found in the LICENSE file.
