## Запуск Mojo
1. UpdateMicroserviceGraphMojo
```Bash
mvn letunov:contract-scanner-maven-plugin:1.0-SNAPSHOT:updateMicroserviceGraph -e  -"Dorg.slf4j.simpleLogger.defaultLogLevel"=DEBUG
```
2. VerifyMicroserviceMojo
```Bash
mvn letunov:contract-scanner-maven-plugin:1.0-SNAPSHOT:verifyMicroservice -e  -"Dorg.slf4j.simpleLogger.defaultLogLevel"=DEBUG
```