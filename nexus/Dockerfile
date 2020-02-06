FROM sonatype/nexus3
USER root
# ARG TOOLS_PATH=/opt/tools
RUN yum install -y \
    wget \
    unzip \
 && wget https://bintray.com/artifact/download/groovy/maven/apache-groovy-sdk-2.5.8.zip \
 && unzip apache-groovy-sdk-2.5.8.zip
ENV PATH=/groovy-2.5.8/bin:$PATH

USER nexus
COPY ./workspace/ /opt/sonatype/nexus
CMD ["sh", "-c", "${NEXUS_HOME}/scripts/wrapper.sh"]