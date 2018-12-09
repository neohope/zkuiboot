FROM java:8

MAINTAINER Miguel Garcia Puyol <miguelpuyol@gmail.com>

WORKDIR /var/app

ADD zkuiboot-*.jar /var/app/zkuiboot.jar
ADD config.cfg /var/app/config.cfg
ADD bootstrap.sh /var/app/bootstrap.sh

ENTRYPOINT ["/var/app/bootstrap.sh"]

EXPOSE 9090
