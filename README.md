# Spotify Playback Info

An interface that checks for your current playback status on Spotify and displays the information in a beautiful little browser page.

You might want to use this over [Spotify's own (rather underwhelming) full-screen mode](https://i.imgur.com/dvreOAX.jpg), or you can use it for your TV/media-streamer to give [that outdated, low-resolution OSD](https://i.imgur.com/lNfCcrW.jpg) a fresh paintjob!

## Some Examples

![Rammstein - Mein Herz brennt](https://i.imgur.com/711oYL9.png)

![Haken - Prothetic](https://i.imgur.com/vBBLKkq.png)

![Deafheaven - Dream House](https://i.imgur.com/FO64o96.png)

![Finsterforst - Ecce Homo](https://i.imgur.com/p3OGz6s.png)

![Steven Wilson - Personal Shopper](https://i.imgur.com/JKhjSXn.png)

## Features

### Displayed Information

* **Main info:** Song name, artist name, album name (with the release year)
* **Time:** Current progress of the song with properly formatted times (works even for 1h+ songs!)
* **States:** Whether something is paused, set to shuffle, or repeat/repeat-one
* **Context:** The playlist/album/artist name and the current device name (your PC, your phone, etc.)
* **Volume:** Appears on the left when volume is changed (see _Haken - Prothetic_ in the examples)

### Idle Mode

The display will automatically enter an idle mode if no Spotify device has been playing music for a while.

![Idle Mode](https://i.imgur.com/js9NlQk.png)

### Smooth Transitions

Smoothly fade from one song's artwork to the next!

![Transitions](https://s2.gifyu.com/images/playback-fading40aaa708d859e2ff.gif)

### Background Color

The background is the album artwork again, but stretched to fit the screen and blurred to not clash with the main image too much. Furthermore, the most dominant color of the art will be used as additional overlay to better separate the two.

This is done using [ColorThief.js](https://lokeshdhakar.com/projects/color-thief) and a very rough implementation of the [Colorfulness Index defined by Hasler and Süsstrunk](https://infoscience.epfl.ch/record/33994/files/HaslerS03.pdf). This closely emulates what Spotify would attempt on a Chromecast (minus the blurred image).

### Lite Mode

I originally wanted to use this on my Raspberry Pi 3, but unfortunately that little guy just isn't strong enough to smoothly do the song transitions and the background color overlay. Various features can therefore be selectively disabled by `?lite=#` to the URL:

* 1: Disable transitions
* 2: Disable background color overlay
* 3: Disable background artwork (just use a colored gradient)
* 4: Disable all three (the same as 3 without a colored overlay, always gray)

## Note about stability

This bot is in *very* early development stages and probably not 100% stable yet. The biggest problem is getting a reliable `EventSource` stream, since it just dies after some time (though, that often takes hours), despite my best attempts to keep it alive with heartbeats and whatnot.

Therefore, any time the connection gets lost, the player will automatically try to reestablish one. This usually only takes a few seconds, so as to not mess with the interface _the player will keep ticking down seconds on its own, despite having no connection_. While perhaps not the cleanest solution on a technical level, it certainly is an unobtrusive one for the viewer.