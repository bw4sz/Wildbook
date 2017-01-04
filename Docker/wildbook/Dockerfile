#MANTAINER Ben Weinstein <weinsteb@oregonstate.edu>
FROM tomcat

#git to clone repo
RUN apt-get update

RUN apt-get -y install git 

#Curl
RUN apt-get -y install curl

#maven to build WAR
RUN apt-get -y install maven

#SQL database
RUN apt-get install -y postgresql

RUN apt-get install -y default-jdk
#Clone latest wildbook branch
RUN git clone https://github.com/bw4sz/Wildbook.git --depth 1 -b 5.x

#build war
RUN mvn clean install -f Wildbook/pom.xml

#Move wildbook to apache webapps
RUN mv Wildbook/target/wildbook-5.5.0-RELEASE.war /usr/local/tomcat/webapps/

#start tomcat8
CMD ["catalina.sh", "run"]

EXPOSE 8080
