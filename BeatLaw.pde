/* Sound lib stuff */
import ddf.minim.*;
import ddf.minim.signals.*;
import ddf.minim.effects.*;

Minim minim; //minor change
AudioOutput out;
SquareWave shotSound;
/* End of sound lib stuff */

/* Graphic variables - time independent*/
int CANVAS_WIDTH = 800;
int CANVAS_HEIGHT = 600;
int SKETCH_FRAMERATE = 60;
int BEAT_HEIGHT = 20;
color bgColor = color(41, 49, 189);

/* Graphic variables - time dependent*/
float xPos;
int yPos;
float lastBeatNum;
long beatStart; // beginning of line
long elapsed;

/* Game control variables */
Metronome metro;
Cowboy p1, p2;
Cowboy currentPlayer, otherPlayer;
Cactus theCactus;
BulletThread bThread;

String[] cactusImgPaths = new String[] {
  "sprites/cactus.png", "sprites/cactus-2.png"
};
String[] p1ImgPaths = new String[] {
  "sprites/c1-0.png", "sprites/c1-1.png", "sprites/c1-2.png"
};
String[] p2ImgPaths = new String[] {
  "sprites/c2-0.png", "sprites/c2-1.png", "sprites/c2-2.png"
};

boolean justShot = false;
boolean currentPlayerShooting = true; 
boolean started = false;

String startupText= "Click on this box.\nPress 's' to start, 'r' to reset, 'm' to mute";

/* Main control methods - setup, start, stop */
void setup () {
  //noLoop();
  size(800, 600);
  frameRate(SKETCH_FRAMERATE);
  noStroke();
  smooth();
  background(bgColor);
  stroke(255);
  text(startupText, 20, 20, 250, 100);
  p1 = new Cowboy("Billy", p1ImgPaths, CANVAS_WIDTH/4, CANVAS_HEIGHT/2);
  p2 = new Cowboy("Diablo", p2ImgPaths, 3*CANVAS_WIDTH/4, CANVAS_HEIGHT/2);
  theCactus = new Cactus("Kak-Tuz", cactusImgPaths, CANVAS_WIDTH/2, CANVAS_HEIGHT/2);
}

void initVariables() {
  // Reset variables
  yPos = 5 + BEAT_HEIGHT;
  resetNewLineVariables();
}

void resetNewLineVariables() {
  xPos = 0;
  lastBeatNum = 0;
}

void startSketch() {
  minim = new Minim(this);
  out = minim.getLineOut(Minim.STEREO, 256);
  metro = new Metronome(out);
  shotSound = new SquareWave(220, 2, 256);
  out.mute();
  out.addSignal(shotSound);
  out.disableSignal(shotSound);
  initVariables();

  p1.restartDuel();
  p2.restartDuel();

  currentPlayer = p1;
  otherPlayer = p2;
  out.unmute();
  beatStart = millis();
  metro.start();
  started = true;
  //loop();
}

void stopSketch() {
  if (!started)
    return;
  metro.quit();
  out.mute();
  out.close();
  minim.stop();
  minim = null;
  started = false;
}

void stop() {
  stopSketch();
  super.stop();
}

void reset() {
  stopSketch();
  startSketch();
}


/* Main graphic methods */
void draw() {
  if (!started)
    return;
  background(bgColor);
  getUpdatedElapsedTime();
  metro.render();
  theCactus.render(lastBeatNum);
  currentPlayer.render(true, currentPlayerShooting);
  otherPlayer.render(false, false);
  if (justShot) {
    out.enableSignal(shotSound);
  } 
  else {
    out.disableSignal(shotSound);
  }
  fill(255);
  rect(elapsed*CANVAS_WIDTH/metro.timeLength, yPos, metro.beatWidth/2, BEAT_HEIGHT);
}

void newLine() {
  beatStart = millis();
  switchTurn();
  // timeLength = newTimeLength;
  if (currentPlayerShooting) {
    initVariables();
  } else {
    yPos = 5 + 2*BEAT_HEIGHT;
    /*    long[] shotsTime = otherPlayer.getShots();
     for (int j=0; j<shotsTime.length; j++) {
     println("Shot " + j + " time " + shotsTime[j]);    
     }
     println("-------");*/
    resetNewLineVariables();
    bThread = new BulletThread(otherPlayer.getShots());    
    bThread.start();
  }
}

/* Game control methods */
Cowboy switchTurn() {
   p1.newTurn();
   p2.newTurn();
  if (currentPlayerShooting) {
    if (currentPlayer == p1) {
      currentPlayer = p2;
      otherPlayer = p1;
    } 
    else {
      currentPlayer = p1;
      otherPlayer = p2;
    }
  } 
  else {
    otherPlayer.reload();
    // another turn for the same player, 
    //this time shooting
  }
  currentPlayerShooting = !currentPlayerShooting;
  currentPlayer.reload();
  //println("Next turn is for player " + (currentPlayer+1) + " shooting " + currentPlayerShooting);
  return currentPlayer;
}

void newBeat(float beatNum) {
  if (beatNum != lastBeatNum) {
    justShot = false;
  }
  lastBeatNum = beatNum;
}

void shotMissed() {
  println("SHOT MISSED! " + currentPlayer.getName() + " loses!");
}


/* Key events */
void keyPressed() {
  if (handleSpecialKey())
    return;
  getUpdatedElapsedTime();
  if (currentPlayerShooting)
    justShot = currentPlayer.shoot(elapsed);
  else {
    currentPlayer.dodge(elapsed);
    bThread.dodgeAttempt(elapsed);
  }
}

boolean handleSpecialKey() {
  if (key == 's') {
    if (started) {
      stopSketch();
    } 
    else {
      startSketch();
    }
    return true;
  } 
  else if (!started) {
    return true; // Absorb key events when not started
  }
  else if ( key == 'm' ) {
    if ( out.isMuted() ) {
      out.unmute();
    } 
    else {
      out.mute();
    }
    return true;
  } 
  else if (key == 't') {
    /* if (newTimeLength >= MIN_TIME_LENGTH ) {
     newTimeLength -= TIME_DELTA;
     } 
     else {
     // we reached the minimum, reset
     newTimeLength = BASE_TIME_LENGTH;
     }
     return true;*/
  } 
  else if (key == 'r') {
    reset();
    return true;
  }

  return false;
}


color getBGColor() {
  return bgColor;
}

synchronized long getUpdatedElapsedTime() {
  elapsed = millis()-beatStart;
  return elapsed;
}

