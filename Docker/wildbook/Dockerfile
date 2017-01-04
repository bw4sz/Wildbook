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

#Clone latest wildbook branch
RUN cd ~
RUN git clone https://github.com/bw4sz/Wildbook.git --depth 1 -b 6.x

#eventually we will build from source, fails tests.
#build war
RUN cd Wildbook

#build war
RUN mvn clean install

#Move wildbook to apache webapps
RUN mv target/wildbook-6.0.0-EXPERIMENTAL.war /usr/share/tomcat8/webapps/

#start tomcat8
CMD ["catalina.sh", "run"]

EXPOSE 8080
