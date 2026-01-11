# Server Configuration
server.port=8080
spring.application.name=badgemate-access-control

# MySQL Database Configuration
spring.datasource.url=jdbc:mysql://localhost:3306/access_control_db?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=root
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA/Hibernate Configuration
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.properties.hibernate.use_sql_comments=true

# MQTT Broker Configuration
mqtt.broker.url=tcp://localhost:1883
mqtt.broker.clientId=badgemate-server
mqtt.broker.username=
mqtt.broker.password=
mqtt.broker.connectionTimeout=30
mqtt.broker.keepAliveInterval=60
mqtt.broker.autoReconnect=true
mqtt.broker.cleanSession=true

# MQTT Topics
mqtt.topic.database.command=badgemate/controller/database/command
mqtt.topic.command.ack=badgemate/controller/command/ack
mqtt.topic.event.log=badgemate/controller/event/log
mqtt.topic.server.heartbeat=badgemate/controller/server/heartbeat

# MQTT Simulator Configuration
mqtt.simulator.enabled=true

# Local Storage for Offline Events
mqtt.offline.storage.path=./data/offline-events
mqtt.offline.storage.enabled=true

# Logging Configuration
logging.level.com.company.badgemate=INFO
logging.level.org.springframework.web=INFO
logging.level.org.hibernate.SQL=DEBUG
logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE
logging.pattern.console=%d{yyyy-MM-dd HH:mm:ss} - %msg%n

# Swagger/OpenAPI Configuration
springdoc.api-docs.path=/api-docs
springdoc.swagger-ui.path=/swagger-ui.html
springdoc.swagger-ui.operationsSorter=method
springdoc.swagger-ui.tagsSorter=alpha
