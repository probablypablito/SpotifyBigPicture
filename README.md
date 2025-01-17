# Spotify Big Picture

A highly customizable interface that displays your current playback status on Spotify in a beautiful little browser page!

You might want to use this over [Spotify's own (in my opinion, rather underwhelming) full-screen mode](https://i.imgur.com/dvreOAX.jpg), or you can use it for your TV/media-streamer to give [that outdated, low-resolution OSD](https://i.imgur.com/lNfCcrW.jpg) a fresh paint job!

This interface is entirely read-only. Specifically, this means that you **cannot actually control your music**. Instead, the idea is to set this app up once, and then it permanently runs as a pure information display. An example where this is useful would be hosting a party where you want to let your guests see at any time which songs are up ahead, by putting a monitor near to the dance floor that you connect to a Raspberry Pi.

## Screenshots
*Note: Due to the progressive development nature of this app, the screenshots may be slightly out of date at any time. The general layout is pretty much written in stone though.*

### Album View
![Deafheaven - Dream House](https://i.imgur.com/7qlGlf1.png)

### Playlist View
![Playlist View](https://i.imgur.com/HsrgpeQ.png)

### Customization
Click the cog symbol in the top left of the interface to open the settings for Visual Preferences. Here you can customize the styling of the interface from a number of presets with a single click!

Your settings are automatically stored locally, so you won't need to worry about reconfiguring everything each time you reopen the website.

#### Choose a Presets
The recommended way to customize the appearance is to choose one of the various available presets:

![Presets](https://i.imgur.com/1nrYKLp.png)
*From left to right: Balanced Mode, XL-Artwork Mode, Track-List Mode, Split-Text Mode, Current-Song Mode, Minimalistic Mode*

There's no harm done in just switching between presets at any time, so feel free to experiment until you find your desired configuration!

#### All Settings
If you wish for a more nuanced way to control the appearance, click the button next to the cog symbol to switch show *all* settings. In here, you will be able to customize every single option individually. A detailed explanation for each option appears when you hover over the respective settings.

Do note that, under the hood, presets are just preconfigured selections of these individual settings; they each form a unique purpose and identity. While you're free to change individual settings as you like, there are combinations that simply won't make sense together. Stick to the presets if you're unsure about anything.

## Installation
Here's a basic guide on how to set this app up, as a few people have been requesting it. As such, it isn't quite as simple to set up yet, but it isn't terribly difficult either:

1. Download the [current release](https://github.com/Selbi182/SpotifyBigPicture/releases)
2. Create an app on the [Spotify Developers dashboard](https://developer.spotify.com/dashboard) (you might need to create an account first)
3. As redirect URI for the app, use `http://localhost:8183/login-callback` (make sure you click the little green "Add" button before saving!)
4. Copy the *Client ID* and *Client Secret* and insert them into the respective fields in the `spotifybot.properties` file
5. Start the app with `Start_SpotifyBigPicture.sh` (or open a terminal and just write `java -jar SpotifyBigPicture.jar`)
6. Once prompted to log in, copy-paste the displayed URL into your preferred browser (should look like this `https://accounts.spotify.com:443/authorize?client_id=[...]&response_type=code&redirect_uri=[...]&scope=[...]`) and log in
7. If everything worked out, the app will be available under http://localhost:8183/

## Requirements
* **Java 11:**
  * [Download here](https://adoptium.net/de/temurin/releases/?version=11) 
* **Monitor should have 16:9 aspect ratio:**
  * While most features should work in any aspect ratio, this app has been optimized primarily for 16:9 (landscape mode). For anything else (namely portrait mode, such as on your phone), expect some limitations
* **Mozilla Firefox:**
  * While this app may work to some degree on Chrome and other browsers, I won't guarantee full stability over there. This app has been solely optimized for Firefox
* **Spotify Premium:**
  * You can still use this app as a free user, but you won't get the full functionality. For example, getting your current queue is **only** available for Spotify premium users. For free users, only the current song can be displayed. For albums, more than a good guess whichever song comes next is unfortunately not possible

### Idle Mode
After two hours of playing no music, the interface will turn black and stop rendering anything to save on resources. As soon as music is played again, the interface will automatically return from its idle state as well.

It may take up to a minute during idle mode for the interface to catch up again; alternatively, you can simply refresh the page.

### Interface doesn't update?
The information is fetched from Spotify's API by polling it once a second. Unfortunately, there is no "proper" way of doing it, as webhooks for song changes (like Discord uses them, for example) are unavailable for the public API.

As a result, the connection might get stuck from time to time. The app will automatically try to reestablish connections when possible, which usually only takes a few seconds. To keep the interface appearance as smooth as possible though, _the timer will simulate playback by keep counting up seconds on its own_ if a song is currently playing.

However, if the interface becomes completely unresponsive, try these approaches:

1. Change the current playback context (e.g. changing from a playlist to an album)

2. For whatever bizarre reason, simply clicking on the devices button in Spotify on your PC (not even selecting any different device, literally just opening the dropdown) sometimes forces the interface to catch up. This has been my go-to for fixing stuck screens, and it works surprisingly well:

![dropdown](https://user-images.githubusercontent.com/8850085/206453960-12d34f5e-03c0-41a0-aba1-7c214de4e53e.png)

3. Open the web console (F12) and take a look at the console out tab. There, you should hopefully find more helpful information about what's going on. The occasional "heartbeat timeout" is nothing to worry about, but if the console is just getting spammed with errors, something's definitely preventing a stable connection.

## Support
If you got any problems, [write an issue ticket on GitHub](https://github.com/Selbi182/SpotifyBigPicture/issues) and I will gladly take a look at it :)

Alternatively, message me on Discord: **Selbi#7270**
