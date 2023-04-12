FROM eclipse-temurin:19-jre
WORKDIR /opt/app/

RUN useradd -r -s /bin/false tweetstodiscord
USER tweetstodiscord

CMD ["java", "-Xms300m", "-Xmx300m", "-jar", "/opt/app/TweetsToDiscord-1.0-SNAPSHOT-jar-with-dependencies.jar"]