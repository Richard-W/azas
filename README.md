AZAS - Anmeldesystem ZaPF am See
================================

[![Build Status](https://travis-ci.org/Richard-W/azas.svg?branch=master)](https://travis-ci.org/Richard-W/azas)
[![Coverage Status](https://coveralls.io/repos/github/Richard-W/azas/badge.svg?branch=master)](https://coveralls.io/github/Richard-W/azas?branch=master)

Build
-----

A simple shell script has been added to this project to build the application and bundle it with all its dependencies
in a single jar file. Just execute

```sh
$ ./build.sh
```

and a jar-file will appear in the root-dir of this project.

Usage
-----

To use azas you will want to configure the database. Copy the `reference.conf` from the jar (open it with a zip-program),
name it `application.conf`. Adjust this file to your needs. You can start the service with

```sh
$ java -Dconfig.file=/path/to/application.conf -jar azas-<version>.jar
```

You should definitely use a reverse proxy to encrypt connections to the service.
