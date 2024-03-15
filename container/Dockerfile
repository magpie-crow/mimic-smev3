FROM        eclipse-temurin:17.0.10_7-jre-jammy
RUN         mkdir "/opt/app"
WORKDIR     "/opt/app"
COPY        ../build/libs/MimicSmev-*.jar /opt/app/app.jar
RUN         ln -sf /usr/share/zoneinfo/Europe/Moscow /etc/localtime
RUN         echo 'Europe/Moscow' > /etc/timezone
RUN         groupadd srv_user -g 7777
RUN         useradd srv_user -u 7777 -g 7777
RUN         chown -R srv_user:srv_user /opt/app
RUN         chmod -R a+rw /opt/app
USER        srv_user
EXPOSE      8080