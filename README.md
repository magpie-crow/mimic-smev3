# mimic-smev

Эмулятор СМЭВ3 версии 1.1

* подготовка и обмен смэв3 сообщениями с mtom вложениями
по умолчанию пути: 
* http://hostname:8080/ws/service.wsdl - адрес вебсервиса
* http://hostname:8080/swagger-ui/index.html - swagger api для создания и просмотра сообщений
* http://hostname:8080/ - ui 

сброрка: `gradle build` 
варианты запуска после:

докер: `./mimic-smev
/container/compose.yaml `

локально после запуска бд: 
`java -Dspring.profiles.active=local -Dfile.encoding=UTF-8 -Dsun.jnu.encoding=UTF-8 -jar ./build/lib/MimicSmev-0.0.1.jar`

для работы нужен PostgreSQL
параметры подключения указываются в application-local.properties:
* spring.datasource.url= jdbc:postgresql://localhost:5432/mimic
* spring.datasource.username=postgres
* spring.datasource.password=postgres