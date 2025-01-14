package spotify;

import java.util.List;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Component;

import spotify.api.SpotifyDependenciesSettings;

@SpringBootApplication
public class SpotifyBigPicture {

  /**
   * Main entry point of the bot
   */
  public static void main(String[] args) {
    SpringApplication.run(SpotifyBigPicture.class, args);
  }

  @Component
  public static class SpotifyBigPictureScopes implements SpotifyDependenciesSettings {

    @Override
    public List<String> requiredScopes() {
      return List.of(
          "user-read-playback-position",
          "user-read-playback-state",
          "user-read-currently-playing",
          "user-read-private"
      );
    }

    @Override
    public int port() {
      return 8183;
    }
  }
}