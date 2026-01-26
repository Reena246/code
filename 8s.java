# Application Configuration
spring.application.name=access-control-system
server.port=8080

# Database Configuration
spring.datasource.url=jdbc:mysql://localhost:3306/access_control_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=your_password
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA/Hibernate Configuration
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.properties.hibernate.use_sql_comments=true
spring.jpa.properties.hibernate.jdbc.batch_size=20
spring.jpa.properties.hibernate.order_inserts=true
spring.jpa.properties.hibernate.order_updates=true

# Connection Pool
spring.datasource.hikari.maximum-pool-size=20
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.connection-timeout=30000
spring.datasource.hikari.idle-timeout=600000
spring.datasource.hikari.max-lifetime=1800000

# Logging
logging.config=classpath:log4j2.xml

# SpringDoc OpenAPI / Swagger
springdoc.api-docs.path=/api-docs
springdoc.swagger-ui.path=/swagger-ui.html
springdoc.swagger-ui.enabled=true

# AES Encryption Configuration
encryption.algorithm=AES/CBC/PKCS5Padding
encryption.key=0123456789abcdef0123456789abcdef

# Kafka Configuration (Optional - Disabled by default)
spring.kafka.enabled=false
spring.kafka.bootstrap-servers=localhost:9092
spring.kafka.producer.key-serializer=org.apache.kafka.common.serialization.StringSerializer
spring.kafka.producer.value-serializer=org.springframework.kafka.support.serializer.JsonSerializer
kafka.topic.audit=audit-events

# Business Logic Configuration
access.sync.minimal-payload=true
access.bulk.chronological-processing=true
access.audit.log-structured=true
