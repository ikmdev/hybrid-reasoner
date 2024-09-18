# Hybrid Reasoner

A hybrid reasoner based on ELK, but using structural subsumption for the SNOMED situation with explicit context hierarchy. 

A prototype implementation including temporal reasoning is in the hybrid-reasoner module.

The hybrid-reasoner-snomed module scales to SNOMED and is based on the IKM reasoner elk-snomed implementation.

## Building and Running

Requires Java 21. Clone the repo, change to the cloned repo location.

Unix/Linux/OSX:

```
./mvnw clean install
```

Windows:

```
./mvnw.cmd clean install
```

To run the integration tests:

```
./mvnw clean install -DskipITs=false
```

The integration tests require the SNOMED test data artifacts from the [reasoner-test-data project](https://github.com/ikmdev/reasoner-test-data)

### Team Ownership - Product Owner

Data Team - Eric Mays (External) <emays@mays-systems.com>

## Issues and Contributions
Technical and non-technical issues can be reported to the [Issue Tracker](https://github.com/ikmdev/hybrid-reasoner/issues).

Contributions can be submitted via pull requests. Please check the [contribution guide](doc/how-to-contribute.md) for more details.

