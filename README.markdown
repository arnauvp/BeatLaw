# BeatLaw

This is a 2 player rhythm-based videogame initially conceived and developed by [Marià Codina](http://mariaambaccent.com/) and 
[Arnau Vàzquez](http://softwaremeetsdesign.com) during a course at IDAT Barcelona.
You can read more about the game and the design process in [the course blog](https://tallergamemod2011.wordpress.com/).  

The license of all the code and assets is CC BY SA NC. More about it [here](http://creativecommons.org/licenses/by-nc-sa/3.0/).
## Getting started

We recommend that you run the game in your computer, as performance will be better than running as an applet.
You can either get the source code and compile it with [Processing](http://processing.org) or use one of the generated binaries
for your OS.

## How to play

This preliminary version of the game has no pauses, and it's wicked fast, but you'll get it quickly.
Start the game pressing any letter. Player 1 takes action with letter _A_ and Player 2 with letter _L_ (case doesn't matter).

The game works in turns:

 * Player 1 shoots, marking a rhythm.
 * Player 2 tries to repeat the rhythm, dodging the bullets if successful.
 * Then Player 2 shoots...
 * ...and Player 1 dodges.
 * Let's do it again! Follow the beat! 

## Roadmap

These are some of the most important things that need to be implemented:

 * __Turn indicator:__ to know who is shooting and who is dodging
 * __Pause time:__ before a player starts shooting
 * __Bullet trajectory:__ show how bullets get closer to the dodging player instead
    of showing statically on the time line. 
