import processing.core.*; 
import processing.xml.*; 

import ddf.minim.*; 
import ddf.minim.signals.*; 
import ddf.minim.effects.*; 

import java.applet.*; 
import java.awt.Dimension; 
import java.awt.Frame; 
import java.awt.event.MouseEvent; 
import java.awt.event.KeyEvent; 
import java.awt.event.FocusEvent; 
import java.awt.Image; 
import java.io.*; 
import java.net.*; 
import java.text.*; 
import java.util.*; 
import java.util.zip.*; 
import java.util.regex.*; 

public class BeatLaw extends PApplet {

/* Sound lib stuff */




Minim minim;
AudioOutput out;
SquareWave shotSound;
/* End of sound lib stuff */

/* Graphic variables - time independent*/
int CANVAS_WIDTH = 800;
int CANVAS_HEIGHT = 600;
int SKETCH_FRAMERATE = 60;
int BEAT_HEIGHT = 20;
int bgColor = color(41, 49, 189);

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
public void setup () {
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

public void initVariables() {
  // Reset variables
  yPos = 5 + BEAT_HEIGHT;
  resetNewLineVariables();
}

public void resetNewLineVariables() {
  xPos = 0;
  lastBeatNum = 0;
}

public void startSketch() {
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

public void stopSketch() {
  if (!started)
    return;
  metro.quit();
  out.mute();
  out.close();
  minim.stop();
  minim = null;
  started = false;
}

public void stop() {
  stopSketch();
  super.stop();
}

public void reset() {
  stopSketch();
  startSketch();
}


/* Main graphic methods */
public void draw() {
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

public void newLine() {
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
public Cowboy switchTurn() {
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

public void newBeat(float beatNum) {
  if (beatNum != lastBeatNum) {
    justShot = false;
  }
  lastBeatNum = beatNum;
}

public void shotMissed() {
  println("SHOT MISSED! " + currentPlayer.getName() + " loses!");
}


/* Key events */
public void keyPressed() {
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

public boolean handleSpecialKey() {
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


public int getBGColor() {
  return bgColor;
}

public synchronized long getUpdatedElapsedTime() {
  elapsed = millis()-beatStart;
  return elapsed;
}

class Bullet {
  long originT;

  public Bullet(long originT) {
    this.originT = originT;
  }

  public void render() {
    fill(255,0,0);
    rect(originT*CANVAS_WIDTH/metro.timeLength, 5 + BEAT_HEIGHT, 25, BEAT_HEIGHT);
    //rect(originT, 5+BEAT_HEIGHT, timelineTickWidth, BEAT_HEIGHT);
  }
}

class BulletThread extends Thread {

  long DODGE_TOLERANCE = 200;
  
  long shotTimestamps[];
  long dodgeTimestamps[];
  int nextShot;
  long prevShotTime;
  boolean running = false;

  public BulletThread(long[] timestamps) {
    shotTimestamps = timestamps;
    dodgeTimestamps = new long[shotTimestamps.length];
  }

  public void start() {
    running = true;
    println("BT starting ");
    super.start();
  }

  public void run() {
    nextShot = 0;
    while (running && nextShot<shotTimestamps.length && shotTimestamps[nextShot] >= 0) {
      //println("next shot to wake up for " + shotTimestamps[nextShot] + " and now is " + getUpdatedElapsedTime());
      try {
        sleep(shotTimestamps[nextShot]-getUpdatedElapsedTime());
      } 
      catch (Exception e) {
        e.printStackTrace();
      }
      if (!running)
        break;
      if (!isShotDodged(shotTimestamps[nextShot], dodgeTimestamps[nextShot])) {
        shotMissed();
        break;
      } 
      else {
        println("shot dodged sucessfully!");
      }
      nextShot++;
    }
    println("BT finishing");
  }

  public void quit() {
    running = false;
    interrupt();
  }

  public void dodgeAttempt(long dodgeRelTime) {
    dodgeTimestamps[nextShot] = dodgeRelTime;
  }

  public boolean isShotDodged(long shotTime, long dodgeTime) {
    return abs(shotTime-dodgeTime) <= DODGE_TOLERANCE;
  }
}

class Cactus {
  String name;
  int posX, posY;
  PImage[] imgs;
  int imgIndex;

  public Cactus(String name, String[] imgPaths, int posX, int posY) {
    this.name = name;
    this.posX = posX;
    this.posY = posY;
    imgs = new PImage[imgPaths.length];
    for (int j=0; j<imgPaths.length; j++) {
      this.imgs[j] = loadImage(imgPaths[j]);
    }
  }

  public void render(float ticNum) {
    if (ticNum%1 == 0)
      imgIndex = 1;
    else
      imgIndex = 0;
    pushMatrix();
    translate(posX-imgs[imgIndex].width/2, posY-imgs[imgIndex].width/2);
    image(imgs[imgIndex], 0, 0);
    popMatrix();
  }
}

class Cowboy {

  int STAND_STILL = 0;
  int SHOOT_STRAIGHT = 1;
  int DODGE_LEFT = 2;
  int DODGE_RIGHT = 3;

  int SHOT_RECOVERY_TIME = 200;
  int MAX_SHOTS = 6; //Those of a revolver
  int MAX_DODGES = 2*MAX_SHOTS;
  int DODGE_RECOVERY_TIME = 200;

  long[] shotsTime;
  Bullet[] bullets;
 
  int dodgeCount;
  Dodge[] dodges;

  String name;
  int shotsLeft;
  int posX, posY;
  PImage[] imgs;
  int stance;

  public Cowboy(String name, String[] imgPaths, int posX, int posY) {
    this.name = name;
    this.posX = posX;
    this.posY = posY;
    imgs = new PImage[imgPaths.length];
    for (int j=0; j<imgPaths.length; j++) {
      this.imgs[j] = loadImage(imgPaths[j]);
    }
    reload();
  }

  public void render(boolean active, boolean shooting) {
    if (!active)
      stance = STAND_STILL;
    else
      stance = currentStance(shooting);
    pushMatrix();
    translate(posX-imgs[stance].width/2, posY-imgs[stance].height/2);
    image(imgs[stance], 0, 0);
    popMatrix();
    for (int j=0; j<(MAX_SHOTS-shotsLeft); j++) {
      bullets[j].render();
    }
    for (int j=0; j<dodgeCount; j++) {
      dodges[j].render();
    }
  }

  public int currentStance(boolean shooting) {

    if (shooting) {    
      if (shotsLeft == MAX_SHOTS)
        return STAND_STILL;
      else if ((elapsed-shotsTime[MAX_SHOTS-shotsLeft-1]) < SHOT_RECOVERY_TIME)
        return SHOOT_STRAIGHT;
    } 
    else { // dodging
      if (dodgeCount == 0)
        return STAND_STILL;
      else if ((elapsed-dodges[dodgeCount-1].tstamp) < DODGE_RECOVERY_TIME)
        return DODGE_LEFT;
    }

    return STAND_STILL;
  }

  public boolean shoot(long timestamp) {
    if (shotsLeft == 0) {
      println(name + ": Geez, I have no bullets left!");
      return false;
    }
    shotsTime[MAX_SHOTS-shotsLeft] = timestamp;
    bullets[MAX_SHOTS-shotsLeft] = new Bullet(timestamp);
    shotsLeft--;
    println(name + " shoots! " + shotsLeft + " bullets left " + timestamp);
    return true;
  }

  public void reload() {
    shotsLeft = MAX_SHOTS;
    shotsTime = new long[] {
      -1, -1, -1, -1, -1, -1
    };
    bullets = new Bullet[MAX_SHOTS];
  }

  public long[] getShots() {
    return shotsTime;
  }

  public void dodge(long timestamp) {
   println(name + " dodges! " + dodgeCount + " " + timestamp);
   dodges[dodgeCount] = new Dodge(timestamp);
   dodgeCount++;
  }

  public void restartDuel() {
  }
  
  public void newTurn() {
    dodges = new Dodge[MAX_DODGES];
    dodgeCount = 0; 
  }

  public String getName() {
    return name;
  }
}

class Dodge {
  long tstamp;

  public Dodge(long tstamp) {
    this.tstamp = tstamp;
  }

  public void render() {
    fill(255,255,0);
    rect(tstamp*CANVAS_WIDTH/metro.timeLength, 5 + 2*BEAT_HEIGHT, 25, BEAT_HEIGHT);
    //rect(originT, 5+BEAT_HEIGHT, timelineTickWidth, BEAT_HEIGHT);
  }

}

class Metronome extends Thread {

  /* Time and beat variables */
  int BASE_TIME_LENGTH = 2000; // milliseconds
  int MIN_TIME_LENGTH = 1500;
  int TIME_DELTA = 125; // delta for controlling tempo
  int BEATS_PER_TIME = 16;

  int newTimeLength = BASE_TIME_LENGTH;
  int timeLength = BASE_TIME_LENGTH;
  int beatsPerSecond = BEATS_PER_TIME*1000/BASE_TIME_LENGTH; 

  int beats;
  long beatDelay;
  long timeElapsed;
  long currTime;
  long prevBeatTime;

  /* Graphical variables */
  int beatWidth = round(CANVAS_WIDTH/BEATS_PER_TIME); //pixels of a beat
  int metroXPos;
  int metroOffset = beatWidth/2;

  boolean running;

  /* Main applet communication */
  AudioOutput out;
  SquareWave ticSound;
  long ticSoundLen;


  public Metronome(AudioOutput out) {
    beats = 0;
    timeElapsed = 0;
    beatDelay = (long)(timeLength/BEATS_PER_TIME);

    this.out = out;
    ticSound = new SquareWave(20, 1, 256);
    ticSoundLen = beatDelay/4;
    out.addSignal(ticSound);
    out.disableSignal(ticSound);
  }

  // Overriding "start()"
  public void start () {
    running = true;
    println("Starting metronome (will execute every " + beatDelay + " milliseconds.)"); 
    super.start();
  }

  // We must implement run, this gets triggered by start()
  public void run () {
    while (running) {
      currTime = millis();
      timeElapsed = currTime - prevBeatTime;
      prevBeatTime = currTime;
      //println("metronome: TIC " + beats + " " + timeElapsed);
      if ((beats % (BEATS_PER_TIME/4)) == 0) {
        playSound();
      } 
      else {
        stopSound();
      }
      newBeat(getTicNum());
      try {
        sleep(beatDelay);
      } 
      catch (Exception e) {
      }
      beats = (beats+1)%BEATS_PER_TIME;
      if (beats == 0)
        newLine();
    }
    System.out.println("metronome is done!");  // The thread is done when we get to the end of run()
  }

  // Our method that quits the thread
  public void quit() {
    System.out.println("Quitting."); 
    running = false;  // Setting running to false ends the loop in run()
    // In case the thread is waiting. . .
    interrupt();
  }

  public void drawMarks() {
    // Reset the metronome in case the time length changed
    fill(bgColor);
    rect(0, 0, CANVAS_WIDTH, BEAT_HEIGHT);
    // Now paint the marks
    fill(0, 255, 0);
    for (int j=0; j<4; j++) {
      rect(j*CANVAS_WIDTH/4 + metroOffset - beatWidth/2, 0, beatWidth, BEAT_HEIGHT);
    }
  }

  public void render() {
    noStroke();
    int metroSpacing = CANVAS_WIDTH/4;
    drawMarks(); // Clear the other 3 marks
    fill(0, 100, 100);
    rect((beats/(BEATS_PER_TIME/4))*metroSpacing + metroOffset - beatWidth/2, 0, beatWidth, BEAT_HEIGHT);
    metroXPos += metroSpacing;
  }

  public void playSound() {
    out.enableSignal(ticSound);
  }

  public void stopSound() {
    out.disableSignal(ticSound);
  }

  public int getBeatWidth() {
    return beatWidth;
  }

  public int getBeatCount() {
    return beats;
  }

  public int getBeatsPerTime() {
    return BEATS_PER_TIME;
  }

  public int getTimeLength() {
    return timeLength;
  }

  public boolean isFullTic() {
    return (beats % (BEATS_PER_TIME/4) == 0);
  }

  public float getTicNum() {
    int tmp = beats/(BEATS_PER_TIME/8);
    return (float)tmp/2;
  }
}

  static public void main(String args[]) {
    PApplet.main(new String[] { "--present", "--bgcolor=#666666", "--stop-color=#cccccc", "BeatLaw" });
  }
}
