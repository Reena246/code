<?xml version="1.0" encoding="UTF-8"?>
<Configuration status="WARN" monitorInterval="30">
    <Properties>
        <Property name="LOG_PATTERN">%d{yyyy-MM-dd HH:mm:ss.SSS} [%t] %-5level %logger{36} - %msg%n</Property>
        <Property name="APP_LOG_ROOT">logs</Property>
    </Properties>

    <Appenders>
        <Console name="Console" target="SYSTEM_OUT">
            <PatternLayout pattern="${LOG_PATTERN}"/>
        </Console>

        <RollingFile name="AppLogFile"
                     fileName="${APP_LOG_ROOT}/application.log"
                     filePattern="${APP_LOG_ROOT}/application-%d{yyyy-MM-dd}-%i.log">
            <PatternLayout pattern="${LOG_PATTERN}"/>
            <Policies>
                <TimeBasedTriggeringPolicy interval="1" modulate="true"/>
                <SizeBasedTriggeringPolicy size="100MB"/>
            </Policies>
            <DefaultRolloverStrategy max="30"/>
        </RollingFile>

        <RollingFile name="AuditLogFile"
                     fileName="${APP_LOG_ROOT}/audit.log"
                     filePattern="${APP_LOG_ROOT}/audit-%d{yyyy-MM-dd}-%i.log">
            <JsonTemplateLayout eventTemplateUri="classpath:LogstashJsonEventLayoutV1.json"/>
            <Policies>
                <TimeBasedTriggeringPolicy interval="1" modulate="true"/>
                <SizeBasedTriggeringPolicy size="100MB"/>
            </Policies>
            <DefaultRolloverStrategy max="90"/>
        </RollingFile>

        <RollingFile name="SecurityLogFile"
                     fileName="${APP_LOG_ROOT}/security.log"
                     filePattern="${APP_LOG_ROOT}/security-%d{yyyy-MM-dd}-%i.log">
            <PatternLayout pattern="${LOG_PATTERN}"/>
            <Policies>
                <TimeBasedTriggeringPolicy interval="1" modulate="true"/>
                <SizeBasedTriggeringPolicy size="50MB"/>
            </Policies>
            <DefaultRolloverStrategy max="60"/>
        </RollingFile>
    </Appenders>

    <Loggers>
        <Logger name="com.accesscontrol.service.AuditService" level="info" additivity="false">
            <AppenderRef ref="AuditLogFile"/>
            <AppenderRef ref="Console"/>
        </Logger>

        <Logger name="com.accesscontrol.security" level="info" additivity="false">
            <AppenderRef ref="SecurityLogFile"/>
            <AppenderRef ref="Console"/>
        </Logger>

        <Logger name="com.accesscontrol" level="info" additivity="false">
            <AppenderRef ref="AppLogFile"/>
            <AppenderRef ref="Console"/>
        </Logger>

        <Logger name="org.springframework" level="warn"/>
        <Logger name="org.hibernate" level="warn"/>

        <Root level="info">
            <AppenderRef ref="Console"/>
            <AppenderRef ref="AppLogFile"/>
        </Root>
    </Loggers>
</Configuration>
