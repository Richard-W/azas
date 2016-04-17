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

### Build

```sh
$ ./build.sh
```

This command is everything it takes to build a functioning version of
azas. There are however some dependencies:

 * Java 8 (Java 7 may or may not work. Don't find out during production)
 * wget
 
Just for reference you also need

 * tar
 * gzip
 * bash
 
But these are already installed on most linux systems.

### Configure

This repository contains an example configuration that provides a basic
setup already. Adjust this file to your needs. It should be documented
well enough.

### Initalize database

You need to assemble a list of all councils in the form of a json file
like this one:

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

This list can be added to the DB with the following command

```sh
$ java -Dconfig.file=/path/to/application.conf -Ddatabase.seed=/path/to/json/file -jar azas-<version>.jar
```

This only works once. Later additions can only be done by manipulating
the database directly (via phpmyadmin for instance).

### Run

```sh
$ java -Dconfig.file=/path/to/application.conf -jar azas-<version>.jar
```

This starts the application. You can either use screen or write systemd
files to daemonize the application. It's up to you.

AZaS has no built in TLS support. You should therefore use an encrypting
reverse proxy. Both apache2 and nginx are capable of that.

Frontend
--------

AZaS provides a default frontend that you can use. There are two ways to
use it:

### Standalone

Just navigate to the URL where the azas backend is running. For example
`http://localhost:8080/` when you use no config.

### Embedded

Using System.js you can embed AZaS in your own website. An example might
look like this:

```html
<!DOCTYPE html>
<html>
<head>
    <title>AZAS</title>
    <meta charset="utf-8" />
</head>
<body>
<!-- Load polyfills -->
<script src="http://url.to.azas/modules/angular2/bundles/angular2-polyfills.min.js"></script>
<script src="/modules/systemjs/dist/system-polyfills.js"></script>

<!-- Configure module loader -->
<script src="http://url.to.azas/modules/systemjs/dist/system.js"></script>
<script>
System.config({
    baseURL: 'https://url.to.azas/modules',
    defaultJSExtensions: true,
    packages: {
        azas: {
            format: 'register'
        }
    }
});
</script>

<!--
Load dependencies. This is not strictly necessary but you will want to
do it if you do not like loading times longer than 10 seconds
-->
<script src="http://url.to.azas/modules/rxjs/bundles/Rx.min.js"></script>
<script src="http://url.to.azas/modules/angular2/bundles/angular2.dev.js"></script>
<script src="http://url.to.azas/modules/angular2/bundles/http.min.js"></script>

<!-- Load app -->
<script>
System.import('azas/main').then(function(module) {
    // Initialize the application with the URL to the
    // API endpoint.
    module.azasBootstrap('http://url.to.azas');
}, console.error.bind(console));
</script>

<!--
The application will be initialized within these tags. What is in there
initially will only be displayed until the app is fully loaded. The
perfect place for a beautiful spinning gif.
-->
<azas>Loading...</azas>
</body>
</html>
```

You can also check `src/main/resources/html/index.html` for reference.

Development
-----------

### General hints

The build system is optimized for fast development iterations. The
fabulous `sbt-revolver` plugin is integrated so you do not have to
interact withthe build system at all during development. Fire up azas by
typing `sbt ~re-start`. Every time you change a file the application is 
automatically recompiled and restarted.

### Build system

The sbt-file of this project is a tic more complicated than your usual
run-off-the-mill scala project to ensure easier deployment. It seems
that authors of sbt-web plugins think that it is OK to just start your
projects using `sbt run` and that all files can be served directly from
the `target` directory.

I think that it is very bad practice to do that because it invites
people to fiddle with systems that are in production status. The only
way you should ever deploy this application is by using the jar-file
that `build.sh` produces and you should only actually use released
versions. I try very hard to make them as stable as possible and every
official release is tested extensively before an actual version number
is assigned.

AZaS uses resource generators to provide the frontend that is written in
typescript using the framework `angular2`. When you change anything in
the sbt-file make sure that the following run configurations still
provide a working frontend:

 * `./build.sh && java -jar azas*.jar`
 * `sbt run`
 * `sbt ~re-start`

This is not covered by unit tests. They only test the backend. Frontend
unit testing is a great way to make yourself less productive. Even if
you do it it would be careless to release without having actually used
it once or twice.

### Backwards compatibility

For a given minor release the database is guaranteed to stay compatible.
For a new minor release it is likely to have a totally new scheme. Given
the short lifetime of deployments this is no contradiction to semantic
versioning. I will provide no database upgrade paths at all until i see
an actual need for them.

The API will stay compatible over a given major release. All API
endpoints will be prefixed by a version (e.g. "/v1/addpart"). However i
reserve the possibility to make a specific endpoints answer with status
code 501 (Not implemented) for all requests if structural changes
require it. This makes it obvious to developers of alternative frontends
what the problem is. Changed behaviour of a given endpoint over the
lifecycle of a major release is considered a critical bug (excluding
501 of course).
