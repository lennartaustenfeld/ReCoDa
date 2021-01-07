# sudo docker build -t recoda-demo ./
# sudo docker run -t -d -p 80:8443 --name recoda-demo recoda-demo
# sudo docker exec --interactive --tty recoda-demo /bin/sh

FROM openjdk:8-jdk-alpine
RUN wget -P /opt https://github.com/dice-group/ReCoDa/releases/download/1.0.2.demo/recoda-demo-1.0.2-exec.jar
EXPOSE 8443
CMD java -jar /opt/recoda-demo-1.0.2-exec.jar