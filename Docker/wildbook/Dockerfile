#MANTAINER Ben Weinstein <weinsteb@oregonstate.edu>
FROM tomcat

# Install dependencies
## git to clone repo
RUN apt-get update

RUN apt-get -y install git 

## Curl
RUN apt-get -y install curl

## maven to build WAR
RUN apt-get -y install maven

## SQL database
RUN apt-get install -y postgresql

RUN apt-get install -y default-jdk

# Install wildbook
## Clone latest wildbook branch
RUN git clone https://github.com/bw4sz/Wildbook.git --depth 1 -b 5.x

## build war
RUN mvn clean install -f Wildbook/pom.xml

## Start database and make the needed user
USER postgres
RUN    /etc/init.d/postgresql start &&\
    psql --command "CREATE USER wildbook WITH SUPERUSER PASSWORD 'wildbook';" &&\
    createdb -O wildbook wildbook

##  Expose the PostgreSQL port
EXPOSE 5432
## Adjust PostgreSQL configuration so that remote connections to the database are possible.
RUN echo "host all  all    0.0.0.0/0  md5" >> /etc/postgresql/9.4/main/pg_hba.conf

## And add ``listen_addresses`` to ``/etc/postgresql/9.4/main/postgresql.conf``
RUN echo "listen_addresses='*'" >> /etc/postgresql/9.4/main/postgresql.conf

USER root

RUN service postgresql start

## Move wildbook to apache webapps
RUN mv Wildbook/target/wildbook-5.5.0-RELEASE.war /usr/local/tomcat/webapps/

# start tomcat
CMD ["catalina.sh", "run"]

EXPOSE 8080

#run with winpty docker run -it -p 8888:8080 wildbook
