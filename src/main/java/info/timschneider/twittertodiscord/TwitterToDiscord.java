package info.timschneider.twittertodiscord;

import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.WebhookClientBuilder;
import club.minnced.discord.webhook.send.WebhookEmbed;
import club.minnced.discord.webhook.send.WebhookEmbedBuilder;
import io.github.redouane59.twitter.TwitterClient;
import io.github.redouane59.twitter.signature.TwitterCredentials;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.logging.Logger;

public class TwitterToDiscord {

    public static void main(String[] args) {
        new TwitterToDiscord(
                System.getenv("TWITTER_accessToken"),
                System.getenv("TWITTER_accessTokenSecret"),
                System.getenv("TWITTER_apiKey"),
                System.getenv("TWITTER_apiSecretKey"),
                System.getenv("DISCORD_WebHookURL"),
                System.getenv("Twitter_RuleName"),
                System.getenv("Twitter_RuleDefinition")
        );
    }
    public TwitterToDiscord(String accessToken, String accessTokenSecret, String apiKey, String apiSecretKey, String webhookURL, String ruleName, String ruleDefinition){
        TwitterClient twitterClient = new TwitterClient(TwitterCredentials.builder()
                .accessToken(accessToken)
                .accessTokenSecret(accessTokenSecret)
                .apiKey(apiKey)
                .apiSecretKey(apiSecretKey)
                .build());

        ResetAndSetStreamRule(twitterClient, ruleName, ruleDefinition);
        subscribeStream(twitterClient, webhookURL);

    }
    public void ResetAndSetStreamRule(TwitterClient twitterClient, String id, String rule){
        twitterClient.retrieveFilteredStreamRules().forEach(streamRule ->{
            twitterClient.deleteFilteredStreamRuleId(streamRule.getId());
        });

        twitterClient.addFilteredStreamRule(rule, id);

        twitterClient.retrieveFilteredStreamRules().forEach(streamRule -> {
            Logger.getLogger("TweetsToDiscord").info("Found Rule: " + streamRule.getId() + " " + streamRule.getTag() + " " + streamRule.getValue());
        });
    }

    public void subscribeStream(TwitterClient twitterClient, String webhookURL){
        twitterClient.startFilteredStream(tweet -> {
            if(tweet.getUser() == null)
                return;
            if(tweet.getMedia() == null) {
                sendWebHookMessage(webhookURL,
                        tweet.getText(),
                        tweet.getUser().getName(),
                        tweet.getUser().getDisplayedName(),
                        tweet.getUser().getProfileImageUrl(),
                        tweet.getUser().getUrl(),
                        "https://twitter.com/"+tweet.getUser().getName()+"/status/"+tweet.getId(),
                        null);
            }else {
                sendWebHookMessage(webhookURL,
                        tweet.getText(),
                        tweet.getUser().getName(),
                        tweet.getUser().getDisplayedName(),
                        tweet.getUser().getProfileImageUrl(),
                        tweet.getUser().getUrl(),
                        "https://twitter.com/"+tweet.getUser().getName()+"/status/"+tweet.getId(),
                        tweet.getMedia().get(0).getMediaUrl());
            }
            Logger.getLogger("TweetsToDiscord").info("Tweet found! " + "https://twitter.com/"+tweet.getUser().getName()+"/status/"+tweet.getId());

        });
    }

    public void sendWebHookMessage(String url, String tweetContent, String userName, String displayName, String userIconURL, String userProfileURL, String tweetURL, String imageURL){
        WebhookClientBuilder builder = new WebhookClientBuilder(url);
        WebhookClient client = builder.build();
        WebhookEmbed embed = new WebhookEmbedBuilder()
                .setColor(0x1da1f2)
                .setDescription(tweetContent)
                .setAuthor(new WebhookEmbed.EmbedAuthor(displayName +" (@"+userName+")", userIconURL, tweetURL))
                .setFooter(new WebhookEmbed.EmbedFooter("Twitter", "https://abs.twimg.com/icons/apple-touch-icon-192x192.png"))
                .setTimestamp(OffsetDateTime.now(Clock.systemUTC()))
                .setImageUrl(imageURL)
                .build();
        client.send(embed);
        client.close();
    }
}
