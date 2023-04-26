# Quickstart

This proof of concepts takes a CDA2 document in input:

*  canonicalizes it
*  removes legalAuthenticator node
*  calculates the sha256 of the new document

```bash
mvn clean package -Dmaven.test.skip=true

java -jar target/fse-cda-hash-0.0.1-SNAPSHOT-jar-with-dependencies.jar CDA_LDO.xml

java -jar target/fse-cda-hash-0.0.1-SNAPSHOT-jar-with-dependencies.jar CDA_LDO2.xml
```

watch for output SHAs