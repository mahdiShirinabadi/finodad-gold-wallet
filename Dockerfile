FROM repo.sadad.co.ir/repository/hub-mali/maven:3.9.7-eclipse-temurin-21-alpine
#FROM repo.sadad.co.ir/repository/hub-mali/maven:3.9.7-eclipse-temurin-21-alpine AS builder
#RUN mvn -Pprod clean install
ARG GIT_BRANCH=main
ARG GIT_COMMIT=null
ARG APP_VERSION=null
ARG BUILD_DATE=null

VOLUME /var/log

LABEL maintainer="Wallet"
LABEL project="Hub and Portal"
LABEL git_branch=$GIT_BRANCH
LABEL git_commit=$GIT_COMMIT
LABEL app_version=$APP_VERSION
LABEL build_date=$BUILD_DATE
ENV TZ="Asia/Tehran"
ENV SPRING_PROFILES_ACTIVE=prod
ENV SERVER_PORT=8080
ENV APP_USER=hub
ENV JAVA_MEMORY="-Xms512m -Xmx512m -XX:NewSize=512m -XX:MaxNewSize=512m"
ENV JAVA_OPTS="-DLog4jContextSelector=org.apache.logging.log4j.core.async.AsyncLoggerContextSelector -Djava.awt.headless=true -Dfile.encoding=UTF-8 -server -XX:+DisableExplicitGC -Djava.security.egd=file:/dev/./urandom"
ENV JAVA_EXTRAS=""

# hub-port=8080 portal-port=8010
EXPOSE 8080
EXPOSE 8010

# Add non-root user for app
RUN adduser -u 1500 -DH -g $APP_USER $APP_USER

RUN mkdir -p /var/log/hub && \
    chown -R $APP_USER:$APP_USER /var/log/

# Switch to non-root app user
USER $APP_USER

# Set working directory
WORKDIR /app

# Copy application files
COPY ./gold-web-hub/target/wallet-gold-web.jar .
COPY ./gold-wallet-grpc/target/wallet-gold-grpc.jar .

ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS:-} ${JAVA_MEMORY:-} ${JAVA_EXTRAS:-} -jar hub-web.jar"]

# COPY --from=build /app/hub-web/target/*.jar app.jar
# # Expose the port the app runs on
# EXPOSE ${CONTRINER_PORT}
# CMD ["java","-jar", "app.jar"]
