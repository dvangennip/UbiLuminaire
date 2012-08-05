UbiLuminaire
------------

## What is it?

This project implements an ambient intelligent room lighting idea. I did it as part of a (Ubiquitous Computing course)[http://www.kth.se/student/kurser/kurs/ID2012?l=en] at KTH Stockholm in May, 2011. The idea is that people adapt ambient light levels to their current activities. Usually people do so by turning on or off various lamps in their environment until it suits their preferences. Such preferences are likely to be stable for common activities, such as reading or having friends over. Therefore, if the environment would understand what a person is doing it could adapt the light levels automatically. This project tries to do exactly this, adjusting a lamp’s brightness to its understanding of the social context in the environment.

While the idea itself is quite simple and straightforward, the implementation is still challenging. It uses a neural network with one hidden layer to decide which activity is the most likely at any moment. Various sensors feed this network at the lowest layer. Activation of higher layers depends on connection strengths (tuned by hand, so no automated learning occured). In turn, the ambient light is tuned by increasing or decreasing the output of the lamp.

For this study project some adaptations had to be made for practical reasons. Instead of using a real lighting fixture I adopted my laptop’s display as the lamp, with its built-in camera, microphone and ambient light sensors as inputs for sensing the context.

It was coded in (Processing)[http://www.processing.org/]. See for some screenshots my webpage: (Sinds1984.nl)[http://www.sinds1984.nl/621/ubiluminaire].

## How to use it

Open the UbiLuminaire.pde file in Processing, which will then load all other files. Press `Run` in the menu bar. A screen with the neural network will show. Make some noise or play music to see it adapt. A larger circle means the node has a higher level of activation.

* `Esc` exits the application
* `d` switches to demo status, so the ambient light sensor data is ignored
* `l` toggles some info on light levels in the top left corner
* `m` toggles audio info in the background
* `a` toggles the activation level circles
* `v` toggles the camera image and blob tracker
* `b` resets the background memory (the blob tracker is based on background subtraction)
* `n` switch motion detection method (check the source for detailed info)

## Dependencies

Before you can use this code, please make sure you have the following additional (Processing)[http://www.processing.org/] libraries installed:

* (minim)[http://code.compartmental.net/tools/minim/]  (audio library for Processing)
* Quicktime video library (including with Processing v1, could change for v2)
* (fullscreen)[http://www.superduper.org/processing/fullscreen_api/]  (fullscreen library for Processing; can be used without)
* (lmu)[http://projects.formatlos.de/ambientlightsensor/]  (for MacBook ambient light sensor)
* (flob)[http://s373.net/code/flob/flob.html]  (fast flood fill blob tracking lib for Processing)

I do not think this code will work on anything else than a MacBook, unless the ambient light sensor dependency is removed first (a rather trivial thing to do). At the very least a webcam and/or microphone is required.