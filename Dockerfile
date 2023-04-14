FROM eclipse-temurin:19-jre

WORKDIR /opt/app/
COPY TweetsToDiscord-1.0-SNAPSHOT.jar /opt/app/
RUN chmod 755 /opt/app/TweetsToDiscord-1.0-SNAPSHOT.jar

RUN useradd -r -s /bin/false tweetstodiscord
USER tweetstodiscord

RUN ls -la /opt/app/
CMD ["java", "-Xms150m", "-Xmx150m", "-jar", "/opt/app/TweetsToDiscord-1.0-SNAPSHOT.jar"]
