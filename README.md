AZAS - Anmeldesystem ZaPF am See
================================

[![Build Status](https://travis-ci.org/Richard-W/azas.svg?branch=master)](https://travis-ci.org/Richard-W/azas)
[![Coverage Status](https://coveralls.io/repos/github/Richard-W/azas/badge.svg?branch=master)](https://coveralls.io/github/Richard-W/azas?branch=master)

Build
-----

A simple shell script has been added to this project to build the
application and bundle it with all its dependencies in a single jar
file. Just execute

```sh
$ ./build.sh
```

and a jar-file will appear in the root-dir of this project.

Usage
-----

Since version 1.2 azas is highly customizable. The default config should
only be used for testing purposes. Copy the `reference.conf` from the
jar (open it with a zip-program) and name it `application.conf`.

You can use the config file like this:

```sh
$ java -Dconfig.file=/path/to/application.conf -jar azas-<version>.jar
```

There are facilities in the config to specify what you want to know from
your participants. Also you should definitely change the database
settings. The default config writes a h2 database file to your home
directory and should only be used for testing.

You should use a reverse proxy to encrypt connections to the service.

To add councils to the database you have to write a json file like this:

```json
[
{
	"uni": "Uni Konstanz",
	"address": "no clue",
	"email": "idk",
	"token": "efgh"
}, {
	"uni": "FSU Jena",
	"address": "Max-Wien-Platz 1",
	"email": "fsr@paf.uni-jena.de",
	"token": "abcd"
}
]
```

You can add it with

```sh
$ java -Ddatabase.seed=/path/to/json/file -jar azas-<version>.jar
```

This only works once. Later additions can only be done by manipulating
the database directly (via phpmyadmin for example).
