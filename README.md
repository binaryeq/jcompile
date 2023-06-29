# Readme 

## Scripts

`docker-build-all.sh`

Using the data sets (projects) defined in `dataset.json` and the JVMs defined in `java-compilers.json`, build all projects and save the jars generated or errors in `jars/`.


`docker-build-project.sh`

Build a single project. Expected arguments: 

1. *docker image* -- the image name including version tag (e.g. from `java-compilers.json`)
2. *docker container* --  name to be used (e.g. from `java-compilers.json`)
3. *project name* -- the name of the project to be build (e.g. from `dataset.json`)
4. *jar* -- the name of the jar to be build (e.g. from `dataset.json`) , this is also used to determine whether a build is needed, see note on caching below
5. *tag* -- the git tag to checkout to build this particular version (and this particular jar)


## Caching Build Results 

Jars are generated in `<jars>/<jdk>/`.  The sh scripts will check whether a jar exists, and if so, skip the (expensive) build. 


## Building multiple jars. 

Some builds create multiple jars, including jars containing source code, but also jars containing the bytecode of tests. Only one jar is used, as specified in `dataset.json`. If other jars from the same build are to be used, a separate element must be added to `dataset.json`. 

This could be changed in future versions. Note that the existence of the single jar file is used to decide whether a project needs to be build.

## Optimising Scripts 

The following Maven skip flags are set to `true` to ommit non-essential functionality from builds: 

1. `rat.skip` 
2. `maven.test.skip` 
3. `maven.javadoc.skip`
4. `cyclonedx.skip`

The main reason is optimisation, i.e. to make builds faster. In case of `maven.test.skip`, this also helps to make builds deterministic as issues caused by flaky tests are avoided. The *cyclonedx* plugin sometimes causes builds to fail, so we disable this as well. The downside here is that with this builds do not generate boms. 

## Dealing with Build Failures

When a build fails, the build output is captured and saved in a file `<jar>.error`. 
When the `docker-build-all.sh` scripts run, those builds are not re-atempted. To trigger a new build attempt, delete the
respective `<jar>.error` file.

Those files can be used to analyse *why* builds fail.


## Common Build Failures

### Source option 5 is no longer supported. Use 8 or later. 

Example: *commons-cli* and *commons-math*. Those projects were not included in the dataset.

### Source | Target option 6  is no longer supported. Use 8 or later. 

Example: *commons-beanutils*. Those projects were not included in the dataset.


### Source | Target option 7  is no longer supported. Use 8 or later. 

Example: *commons-compress-1.16*. Those projects were not included in the dataset.


### Source | Target option 1.2 is no longer supported. Use 8 or later. 

Example: *commons-logging*. Those projects were not included in the dataset.

### No Tags

Example: *commons-ognl*.  Those projects were not included in the dataset.


### Execution bundle-manifest of goal org.apache.felix:maven-bundle-plugin:4.1.0:manifest failed.: ConcurrentModificationException

Common issue in some libraries including *commons-csv*, *commons-configuration2* when building with new versions of the openjdk (from 17).  This is a [known issue](https://stackoverflow.com/questions/64729046/apache-maven-build-failure-failed-to-execute-goal-org-apache-felixmaven-bundl), and would require updating the bundle-plugin, and therefore manipulate the original build scripts.  

In these case we attempt builds, and log errors. 


### org.codehaus.plexus.util.xml.pull.XmlPullParserException: UTF-8 BOM plus xml decl of ISO-8859-1 is incompatible 

This issue was observed bulding `commons-bcel`. Can still be build using `-Dcyclonedx.skip=true` to disable building a bom. 





