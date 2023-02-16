package spotify.playback.data.visual;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.stereotype.Component;

import com.google.common.collect.Iterables;
import com.neovisionaries.i18n.CountryCode;

import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.enums.AlbumType;
import se.michaelthelin.spotify.enums.CurrentlyPlayingType;
import se.michaelthelin.spotify.enums.ModelObjectType;
import se.michaelthelin.spotify.model_objects.IPlaylistItem;
import se.michaelthelin.spotify.model_objects.miscellaneous.CurrentlyPlayingContext;
import se.michaelthelin.spotify.model_objects.specification.Album;
import se.michaelthelin.spotify.model_objects.specification.Artist;
import se.michaelthelin.spotify.model_objects.specification.ArtistSimplified;
import se.michaelthelin.spotify.model_objects.specification.Context;
import se.michaelthelin.spotify.model_objects.specification.Episode;
import se.michaelthelin.spotify.model_objects.specification.Image;
import se.michaelthelin.spotify.model_objects.specification.Playlist;
import se.michaelthelin.spotify.model_objects.specification.PlaylistTrack;
import se.michaelthelin.spotify.model_objects.specification.Show;
import se.michaelthelin.spotify.model_objects.specification.ShowSimplified;
import se.michaelthelin.spotify.model_objects.specification.Track;
import se.michaelthelin.spotify.model_objects.specification.TrackSimplified;
import spotify.api.SpotifyApiException;
import spotify.api.SpotifyCall;
import spotify.playback.data.dto.PlaybackInfo;
import spotify.playback.data.dto.sub.TrackData;
import spotify.playback.data.help.BigPictureConstants;
import spotify.playback.data.help.BigPictureUtils;
import spotify.services.UserService;
import spotify.util.BotUtils;
import spotify.util.data.AlbumTrackPair;

@Component
public class ContextProvider {
  public static final String QUEUE_PREFIX = "Queue >> ";

  private final SpotifyApi spotifyApi;
  private final UserService userService;

  private CountryCode market;

  private String previousContextString;
  private Album currentContextAlbum;
  private List<TrackSimplified> currentContextAlbumTracks;
  private List<TrackData.ListTrack> listTracks;
  private Integer currentlyPlayingAlbumTrackNumber;
  private Integer currentlyPlayingAlbumTrackDiscNumber;
  private Integer trackCount;
  private Long totalTrackDuration;
  private String thumbnailUrl;

  ContextProvider(SpotifyApi spotifyApi, UserService userService) {
    this.spotifyApi = spotifyApi;
    this.userService = userService;
    this.listTracks = new ArrayList<>();
  }

  /**
   * Get the name of the currently playing context (either a playlist name, an
   * artist, or an album).
   *
   * @param info     the context info
   * @param previous the previous info to compare to
   * @return a String of the current context, null if none was found
   */
  public String findContextName(CurrentlyPlayingContext info, PlaybackInfo previous) {
    String contextName = null;
    try {
      Context context = info.getContext();
      ModelObjectType type = BigPictureUtils.getModelObjectType(info);
      if (context != null || type != null) {
        boolean force = previous == null || previous.getPlaybackContext().getContext() == null || previous.getPlaybackContext().getContext().isEmpty();
        if (type != null) {
          switch (type) {
            case ALBUM:
              contextName = getAlbumContext(info, force);
              break;
            case PLAYLIST:
              contextName = getPlaylistContext(context, force);
              break;
            case ARTIST:
              contextName = getArtistContext(context, force);
              break;
            case SHOW:
            case EPISODE:
              contextName = getPodcastContext(info, force);
              break;
          }
        }
      }
    } catch (SpotifyApiException e) {
      e.printStackTrace();
    }
    if (contextName != null) {
      return contextName;
    } else {
      return previous != null && previous.getPlaybackContext().getContext() != null
          ? previous.getPlaybackContext().getContext()
          : info.getCurrentlyPlayingType().toString();
    }
  }

  public List<TrackData.ListTrack> getListTracks() {
    return listTracks;
  }

  public Integer getCurrentlyPlayingAlbumTrackNumber() {
    return currentlyPlayingAlbumTrackNumber;
  }

  public Integer getCurrentlyPlayingAlbumTrackDiscNumber() {
    return currentlyPlayingAlbumTrackDiscNumber;
  }

  public Integer getTotalDiscCount() {
    return currentContextAlbumTracks.stream().mapToInt(TrackSimplified::getDiscNumber).max().orElse(1);
  }

  public Integer getTrackCount() {
    return trackCount;
  }

  public Long getTotalTime() {
    return totalTrackDuration;
  }

  public String getThumbnailUrl() {
    return thumbnailUrl;
  }

  private void setTrackCount(Integer trackCount) {
    this.trackCount = trackCount;
  }

  private void setTotalTrackDuration(List<TrackData.ListTrack> listTracks) {
    this.totalTrackDuration = listTracks.stream().mapToLong(TrackData.ListTrack::getLength).sum();
  }

  public Integer getCurrentlyPlayingPlaylistTrackNumber(CurrentlyPlayingContext context) {
    IPlaylistItem item = context.getItem();
    String id = item.getId();
    return Iterables.indexOf(listTracks, t -> {
      if (t != null) {
        if (t.getId() != null) {
          return t.getId().equals(id);
        } else {
          ModelObjectType type = item.getType();
          if (ModelObjectType.TRACK.equals(type)) {
            Track track = (Track) item;
            return t.getArtists().containsAll(BotUtils.toArtistNamesList(track)) && track.getName().equals(t.getTitle());
          } else if (ModelObjectType.EPISODE.equals(type)) {
            Episode episode = (Episode) item;
            return t.getArtists().contains(episode.getShow().getName()) && episode.getName().equals(t.getTitle());
          }
        }
      }
      return false;
    }) + 1;
  }

  private String getArtistContext(Context context, boolean force) {
    if (force || didContextChange(context)) {
      String artistId = context.getHref().replace(BigPictureConstants.ARTIST_PREFIX, "");
      Artist contextArtist = SpotifyCall.execute(spotifyApi.getArtist(artistId));

      Image[] artistImages = contextArtist.getImages();
      String largestImage = BotUtils.findLargestImage(artistImages);
      this.thumbnailUrl = largestImage != null ? largestImage : BigPictureUtils.BLANK;

      this.listTracks = Arrays.stream(SpotifyCall.execute(spotifyApi.getArtistsTopTracks(artistId, getMarketOfCurrentUser())))
          .map(TrackData.ListTrack::fromPlaylistItem)
          .collect(Collectors.toList());

      setTrackCount(contextArtist.getFollowers().getTotal());
      setTotalTrackDuration(List.of());

      return "ARTIST: " + contextArtist.getName();
    }
    return null;
  }

  private String getPlaylistContext(Context context, boolean force) {
    if (force || didContextChange(context)) {
      String playlistId = context.getHref().replace(BigPictureConstants.PLAYLIST_PREFIX, "");
      Playlist contextPlaylist = SpotifyCall.execute(spotifyApi.getPlaylist(playlistId));

      Image[] playlistImages = contextPlaylist.getImages();
      String largestImage = BotUtils.findLargestImage(playlistImages);
      this.thumbnailUrl = largestImage != null ? largestImage : BigPictureUtils.BLANK;

      // Limit to 200 for performance reasons
      PlaylistTrack[] firstHalf = contextPlaylist.getTracks().getItems();
      PlaylistTrack[] secondHalf = SpotifyCall.execute(spotifyApi.getPlaylistsItems(playlistId).offset(firstHalf.length)).getItems();
      this.listTracks = Stream.concat(Arrays.stream(firstHalf), Arrays.stream(secondHalf))
          .map(PlaylistTrack::getTrack)
          .map(TrackData.ListTrack::fromPlaylistItem)
          .collect(Collectors.toList());

      Integer realTrackCount = contextPlaylist.getTracks().getTotal();
      setTrackCount(realTrackCount);
      setTotalTrackDuration(realTrackCount <= this.listTracks.size() ? this.listTracks : List.of());

      return contextPlaylist.getName();
    }
    return null;
  }

  private String getAlbumContext(CurrentlyPlayingContext info, boolean force) {
    Context context = info.getContext();
    Track track = null;
    String albumId;
    if (info.getCurrentlyPlayingType().equals(CurrentlyPlayingType.TRACK)) {
      track = (Track) info.getItem();
      albumId = track.getAlbum().getId();
    } else {
      albumId = BotUtils.getIdFromUri(context.getUri());
    }

    if (force || didContextChange(context)) {
      currentContextAlbum = SpotifyCall.execute(spotifyApi.getAlbum(albumId));
      currentContextAlbumTracks = Arrays.asList(currentContextAlbum.getTracks().getItems());

      if (currentContextAlbum.getTracks().getNext() != null) {
        List<TrackSimplified> c = SpotifyCall.executePaging(spotifyApi.getAlbumsTracks(albumId).offset(currentContextAlbumTracks.size()));
        currentContextAlbumTracks = Stream.concat(currentContextAlbumTracks.stream(), c.stream()).collect(Collectors.toList());
      }

      this.thumbnailUrl = Arrays.stream(currentContextAlbum.getArtists())
          .findFirst()
          .map(ArtistSimplified::getId)
          .map(id -> SpotifyCall.execute(spotifyApi.getArtist(id)))
          .map(Artist::getImages)
          .map(BotUtils::findSmallestImage)
          .orElse(BigPictureUtils.BLANK);

      this.listTracks = currentContextAlbumTracks.stream()
          .map(BotUtils::asTrack)
          .map(TrackData.ListTrack::fromPlaylistItem)
          .collect(Collectors.toList());

      setTrackCount(this.listTracks.size());
      setTotalTrackDuration(this.listTracks);
    }
    String contextString = getReleaseTypeString() + ": " + BotUtils.getFirstArtistName(currentContextAlbum) + " \u2013 " + currentContextAlbum.getName();
    if (currentContextAlbumTracks != null && track != null) {
      // Track number (unfortunately, can't simply use track numbers because of disc numbers)
      final String trackId = track.getId();
      TrackSimplified currentTrack = currentContextAlbumTracks.stream()
          .filter(t -> t.getId().equals(trackId))
          .findFirst()
          .orElse(null);

      if (currentTrack != null) {
        this.currentlyPlayingAlbumTrackNumber = currentContextAlbumTracks.indexOf(currentTrack) + 1;
        this.currentlyPlayingAlbumTrackDiscNumber = currentTrack.getDiscNumber();
        if (this.currentlyPlayingAlbumTrackNumber > 0) {
          return contextString;
        }
      }
    }

    // Fallback when playing back from the queue
    return QUEUE_PREFIX + contextString;
  }

  private String getPodcastContext(CurrentlyPlayingContext info, boolean force) {
    Episode episode = (Episode) info.getItem();
    ShowSimplified showSimplified = episode.getShow();
    if (force || didContextChange(episode.toString())) {

      Image[] artistImages = showSimplified.getImages();
      String largestImage = BotUtils.findLargestImage(artistImages);
      this.thumbnailUrl = largestImage != null ? largestImage : BigPictureUtils.BLANK;

      Show show = SpotifyCall.execute(spotifyApi.getShow(showSimplified.getId()));
      setTrackCount(show.getEpisodes().getTotal());
      setTotalTrackDuration(List.of());

      return "PODCAST: " + show.getName();
    }
    return null;
  }

  private String getReleaseTypeString() {
    if (currentContextAlbum.getAlbumType() == AlbumType.SINGLE) {
      AlbumTrackPair atp = AlbumTrackPair.of(BotUtils.asAlbumSimplified(currentContextAlbum), currentContextAlbumTracks);
      if (BotUtils.isExtendedPlay(atp)) {
        return "EP";
      }
    }
    return currentContextAlbum.getAlbumType().toString();
  }

  private boolean didContextChange(Context context) {
    return didContextChange(context.toString());
  }

  private boolean didContextChange(String contextString) {
    if (!contextString.equals(previousContextString)) {
      this.previousContextString = contextString;
      return true;
    }
    return false;
  }

  private CountryCode getMarketOfCurrentUser() {
    if (this.market == null) {
      this.market = userService.getMarketOfCurrentUser();
      if (this.market == null) {
        throw new IllegalStateException("Market is null (user-read-private scope missing?)");
      }
    }
    return this.market;
  }
}
