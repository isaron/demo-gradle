FROM gradle:jdk11 as BUILD

COPY --chown=gradle:gradle . /project
RUN gradle -i -s -b /project/build.gradle clean bootJar && \
    tar -zxf /project/build/lib/*.jar && \
    rm -rf /project/build/lib/*.jar

FROM openjdk:11-jre-slim
ENV PORT 8080
EXPOSE 8080
COPY --from=BUILD /project/build/lib/* /opt/
WORKDIR /opt/bin
CMD ["/bin/bash", "-c", "find -type f -name '*' | xargs bash"]
