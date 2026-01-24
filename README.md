## Kanban hub 
(in Progress)
Wraps different task management software and unifies them in one app.

### Build locally

```
cd sources-api
../gradlew clean publishToMavenLocal
cd ..
cd gtask-plugin
../gradlew clean publishToMavenLocal
cd ..
cd taskwarrior-plugin
../gradlew clean publishToMavenLocal
cd ..
cd todotxt-plugin
../gradlew clean publishToMavenLocal
cd ..
cd console
../gradlew clean bootJar
```