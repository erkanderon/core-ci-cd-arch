FROM eclipse-temurin:21-jdk as jdk21

FROM rockylinux:9.3
ENV container docker
ENV JAVA_HOME=/opt/java/openjdk
ENV PATH "${JAVA_HOME}/bin:${PATH}"
COPY --from=jdk21 $JAVA_HOME $JAVA_HOME

RUN yum clean all && yum -y install unzip;
RUN yum install git -y
RUN yum clean all && \
    yum -y install epel-release && \
    yum -y install gcc wget && \
    yum -y install unzip python-pip && \
    yum -y install ca-certificates && \
    yum -y install libXext libXrender libXtst;

ADD start.sh /opt/jenkins/start.sh
RUN chmod +x /opt/jenkins/start.sh

RUN cd /tmp && \
    curl -LO "https://dl.k8s.io/release/$(curl -L -s https://dl.k8s.io/release/stable.txt)/bin/linux/amd64/kubectl" && \
    chmod +x ./kubectl && \
    mv ./kubectl /usr/local/bin/kubectl

RUN wget https://github.com/tektoncd/cli/releases/download/v0.37.0/tektoncd-cli-0.37.0_Linux-64bit.rpm && \
    rpm -Uvh tektoncd-cli-0.37.0_Linux-64bit.rpm

WORKDIR /opt/jenkins
ENTRYPOINT ["/opt/jenkins/start.sh"]