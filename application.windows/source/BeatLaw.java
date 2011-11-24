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


/* Graphic variables - time independent*/
int CANVAS_WIDTH = 800;
int CANVAS_HEIGHT = 600;
int SKETCH_FRAMERATE = 60;
int BEAT_HEIGHT = 20;
int FRAME_STROKE_WIDTH = 20;
int FRAME_YPOS_START = CANVAS_HEIGHT/5;
int FRAME_HEIGHT = CANVAS_WIDTH/2;
int bgColor = color(41, 49, 189);
int frameColor = color(190, 96, 255);


/* Graphic variables - time dependent*/
float xPos;
int yPos;
float lastBeatNum;
long beatStart; // beginning of line
long elapsed;
String OPENING_SCREEN_PATH = "screens/open_static.png";
String CLOSING_SCREEN_PATH = "screens/gameover.png";
PImage openingScreen;

/* Game control variables */
Metronome metro;
Cowboy p1, p2;
Cowboy currentPlayer, otherPlayer;
Cactus theCactus;
BulletThread bThread;
DJ theDJ;

String[] cactusImgPaths = new String[] {
  "sprites/cactus.png", "sprites/cactus-2.png"
};
String p1ImgPath = "sprites/1up";
String p2ImgPath = "sprites/2up";
int p1Color = color(255, 129, 31);
int p2Color = color(234, 81, 235);

boolean justShot = false;
boolean currentPlayerShooting = true; 
boolean started = false;
boolean gameOver = false;

String startupText= "Click on this box.\nPress 's' to start, 'r' to reset, 'm' to mute";

/* Main control methods - setup, start, stop */
public void setup () {
  //noLoop();
  size(800, 600);
  frameRate(SKETCH_FRAMERATE);
  noStroke();
  smooth();
  showOpeningScreen();
  //background(bgColor);
  stroke(255);
  //text(startupText, 20, 20, 250, 100);
  p1 = new Cowboy("Billy", p1Color, p1ImgPath, CANVAS_WIDTH/4, CANVAS_HEIGHT/2);
  p2 = new Cowboy("Diablo", p2Color, p2ImgPath, 3*CANVAS_WIDTH/4, CANVAS_HEIGHT/2);
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
  theDJ = new DJ(this);
  theDJ.initSoundSystem();
  metro = new Metronome(theDJ.getAudioOutput());
  initVariables();

  p1.restartDuel();
  p2.restartDuel();

  currentPlayer = p1;
  otherPlayer = p2;
  beatStart = millis();
  metro.start();
  started = true;
  gameOver = false; 
  //loop();
}

public void stopSketch() {
  if (!started)
    return;
  metro.quit();
  theDJ.stopSoundSystem();
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
  if (gameOver) {
      showClosingScreen();
      return;
  } else if (!started) {
    //image(openingScreen, 0, 0);
    return;
  }
  background(bgColor);
  drawFrame();
  getUpdatedElapsedTime();
  metro.render();
  theCactus.render(lastBeatNum);
  currentPlayer.render(true, currentPlayerShooting);
  otherPlayer.render(false, false);
  fill(255);
  rect(elapsed*CANVAS_WIDTH/metro.timeLength, yPos, metro.beatWidth/2, BEAT_HEIGHT);
}

public void drawFrame() {
  stroke(frameColor);
  strokeWeight(FRAME_STROKE_WIDTH);
  fill(0, 0, 0, 0);
  rect(FRAME_STROKE_WIDTH/2, FRAME_YPOS_START, CANVAS_WIDTH-FRAME_STROKE_WIDTH, FRAME_HEIGHT-FRAME_STROKE_WIDTH/2);
  noStroke();
}

public void showOpeningScreen() {
  /*openingScreen = new Gif(this, OPENING_SCREEN_PATH);
   openingScreen.loop();*/
  openingScreen = loadImage(OPENING_SCREEN_PATH);
  image(openingScreen, 0, 0);
}

public void dismissOpeningScreen() {
  //openingScreen.stop();
  openingScreen = null;
}

public void showClosingScreen() {
  PImage closingScreen = loadImage(CLOSING_SCREEN_PATH);
  //background(bgColor);
  image(closingScreen, CANVAS_WIDTH/2 - closingScreen.width/2, CANVAS_HEIGHT/2 - closingScreen.height/2);
}

public void newLine() {
  beatStart = millis();
  switchTurn();
  // timeLength = newTimeLength;
  if (currentPlayerShooting) {
    initVariables();
  } 
  else {
    yPos = 5 + 2*BEAT_HEIGHT;
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
  //println("SHOT MISSED! " + currentPlayer.getName() + " loses!");
  if (currentPlayer.lifeDown() == 0) {
    stopSketch();
    gameOver = true;
    println(">>>> Game over! " + otherPlayer.getName() + " wins! <<<<");
  }
}


/* Key events */
public void keyPressed() {
  if (handleSpecialKey())
    return;
  getUpdatedElapsedTime();
  // Only shoot/dodge with A/L for P1/P2
  if (((key == 'a' || key == 'A') && (p1 == currentPlayer)) ||
    ((key == 'l' || key == 'L') && (p2 == currentPlayer))) {
    if (currentPlayerShooting) {
      justShot = currentPlayer.shoot(elapsed);
    } 
    else {
      currentPlayer.dodge(elapsed);
      bThread.dodgeAttempt(elapsed);
    }
  }
}

public boolean handleSpecialKey() {
  if (!started) {
    if (key >= 'A' && key <= 'z') {
      // start with any key
      dismissOpeningScreen();
      startSketch();
    }
    return true;
  } 
  else if (key == 's') {
    if (started) {
      stopSketch();
      showOpeningScreen();
    } 
    else {
      dismissOpeningScreen();
      startSketch();
    }
    return true;
  } 
  else if ( key == 'm' ) {
    theDJ.toggleSound();
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

  public void render(int c) {
    fill(c);
    rect(originT*CANVAS_WIDTH/metro.timeLength, 5 + BEAT_HEIGHT, 25, BEAT_HEIGHT);
  }
}

class BulletThread extends Thread {

  // absolute difference between the shot and the dodge
  long DODGE_TOLERANCE = 50;
  
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
    //println("BT starting ");
    super.start();
  }

  public void run() {
    nextShot = 0;
    while (running && nextShot<shotTimestamps.length && shotTimestamps[nextShot] >= 0) {
      //println("next shot to wake up for " + shotTimestamps[nextShot] + " and now is " + getUpdatedElapsedTime());
      try {
        sleep(shotTimestamps[nextShot]-getUpdatedElapsedTime() + DODGE_TOLERANCE);
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
    //println("BT finishing");
  }

  public void quit() {
    running = false;
    interrupt();
  }

  public void dodgeAttempt(long dodgeRelTime) {
    dodgeTimestamps[nextShot] = dodgeRelTime;
  }

  public boolean isShotDodged(long shotTime, long dodgeTime) {
    return abs(shotTime-dodgeTime) <= 2*DODGE_TOLERANCE;
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
  int MIN_SHOOT_POS = 1;
  int MAX_SHOOT_POS = 6;
  int MIN_DODGE_POS = 7;
  int MAX_DODGE_POS = 12;

  int SHOT_RECOVERY_TIME = 200;
  int MAX_SHOTS = 6; //Those of a revolver
  int MAX_DODGES = MAX_SHOTS;
  int DODGE_RECOVERY_TIME = 200;

  String LIFE_ICON_PATH = "sprites/cor.png";
  PImage lifeImg;
  boolean alignLeft;
  int INITIAL_LIVES = 3;
  int lives;

  long[] shotsTime;
  Bullet[] bullets;
 
  int dodgeCount;
  Dodge[] dodges;

  String name;
  int c;
  int shotsLeft;
  int posX, posY;
  PImage[] imgs;
  int stance;

  public Cowboy(String name, int c, String imgPath, int posX, int posY) {
    lives = INITIAL_LIVES;
    this.name = name;
    this.c = c;
    this.posX = posX;
    this.posY = posY;
    if (posX < CANVAS_WIDTH/2)
      alignLeft = true;
    else
      alignLeft = false;    
    imgs = new PImage[MAX_DODGE_POS];
    for (int j=0; j<MAX_DODGE_POS; j++) {
      this.imgs[j] = loadImage(imgPath + "/" + j + ".png");
    }
    lifeImg = loadImage(LIFE_ICON_PATH);
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
      bullets[j].render(c);
    }
    for (int j=0; j<dodgeCount; j++) {
      dodges[j].render(c);
    }
    if (alignLeft) {
      for (int j=0; j <lives; j++) {
         image(lifeImg, j*(lifeImg.width+5), CANVAS_HEIGHT-lifeImg.height);
      }
    } else {
      for (int j=0; j <lives; j++) {
         image(lifeImg, CANVAS_WIDTH-(j+1)*(lifeImg.width + 5), CANVAS_HEIGHT-lifeImg.height);
      }
    }
  }

  public int currentStance(boolean shooting) {

    if (shooting) {    
      if (shotsLeft == MAX_SHOTS)
        return STAND_STILL;
      else if ((elapsed-shotsTime[MAX_SHOTS-shotsLeft-1]) < SHOT_RECOVERY_TIME) {
        if (stance != STAND_STILL)
          return stance;
        else
          return randomShootingStance();
      }
    } 
    else { // dodging
      if (dodgeCount == 0)
        return STAND_STILL;
      else if ((elapsed-dodges[dodgeCount-1].tstamp) < DODGE_RECOVERY_TIME) {
        if (stance != STAND_STILL)
          return stance;
        else
          return randomDodgingStance();
      }
    }

    return STAND_STILL;
  }
  
  public int randomShootingStance() {
     return (int) random(MIN_SHOOT_POS, MAX_SHOOT_POS);    
  }
  
  public int randomDodgingStance() {
     return (int) random(MIN_DODGE_POS, MAX_DODGE_POS);    
  }

  public boolean shoot(long timestamp) {
    if (shotsLeft == 0) {
      println(name + ": Geez, I have no bullets left!");
      return false;
    }
    theDJ.playShot();
    shotsTime[MAX_SHOTS-shotsLeft] = timestamp;
    bullets[MAX_SHOTS-shotsLeft] = new Bullet(timestamp);
    shotsLeft--;
    //println(name + " shoots! " + shotsLeft + " bullets left " + timestamp);
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
    if (dodgeCount == MAX_DODGES) {
      println("Too tired to dance, bro!");
      return;
    }
   //println(name + " dodges! " + dodgeCount + " " + timestamp);
   theDJ.playDodge();
   dodgeCount++;
   dodges[dodgeCount-1] = new Dodge(timestamp);
  }

  public void restartDuel() {
     lives = INITIAL_LIVES;
     dodgeCount = 0;
     reload();
  }
  
  public void newTurn() {
    dodges = new Dodge[MAX_DODGES];
    dodgeCount = 0; 
  }
  
  public synchronized int lifeDown() {
    lives--;
    println(name + " loses a life! Has now " + lives);
    return lives;
  }
  
  public synchronized int lifeUp() {
    lives++;
    println("Woot! " + name + " wins an extra life! Has now " + lives);
    return lives;
  }
  
  public String getName() {
    return name;
  }
}





class DJ extends Thread {

  // All sound lengths will be a multiple
  // of this one 
  long BASE_SOUND_LEN = 20;//ms
  float MI = 58.27f;
  float SIb = 73.42f;
  float FA = 77.78f;
  int HITHAT_TYPE_HARD = 0;
  int HITHAT_TYPE_SOFT = 1;

  PApplet app;
  Minim minim;
  AudioOutput out;
  ArrayList sounds;
  boolean running = false;

  public DJ(PApplet app) {
    this.app = app;
    sounds = new ArrayList(10);
  }

  public void initSoundSystem() {
    minim = new Minim(app);
    out = minim.getLineOut(Minim.STEREO, 256);
    running = true;
    super.start();
  }

  public void stopSoundSystem() {
    out.mute();
    // backwards because we're removing items
    GameSound nextSound;
    for (int i = sounds.size()-1; i >= 0; i--) {
      nextSound = (GameSound)sounds.get(i);
      nextSound.stopSound();
      sounds.remove(i);
    }
    out.close();
    minim.stop();
    minim = null;
    running = false;
    interrupt();
  }

  public void run() {
    println("DJ starts the party");
    while (running) {
      try {
        sleep(BASE_SOUND_LEN);
        GameSound nextSound;
        for (int i = sounds.size()-1; i >= 0; i--) {
          nextSound = (GameSound)sounds.get(i);
          if(nextSound.timePassed())
            sounds.remove(i);
        }
      } 
      catch (Exception e) {
        if (running)
          e.printStackTrace();
      }
    }
    println("DJ goes home");
  }

  public void playShot() {
    ShotSound shot = new ShotSound(out);
    shot.startSound();
    sounds.add(shot);
  }
  
  public void playDodge() {
    DodgeSound dodge = new DodgeSound(out);
    dodge.startSound();
    sounds.add(dodge);
  }

  public void playMetroKick() {
    MetronomeKickSound kick = new MetronomeKickSound(out);
    kick.startSound();
    sounds.add(kick);
  }
  
  public void playMetroHitHatHard() {
    MetronomeHitHatSound hitHat = new MetronomeHitHatSound(out, HITHAT_TYPE_HARD);
    hitHat.startSound();
    sounds.add(hitHat);
  }
  
  public void playMetroHitHatSoft() {
    MetronomeHitHatSound hitHat = new MetronomeHitHatSound(out, HITHAT_TYPE_SOFT);
    hitHat.startSound();
    sounds.add(hitHat);
  }
  
  public void playMetroBass(float note) {
    MetronomeBassSound bass = new MetronomeBassSound(out, note);
    bass.startSound();
    sounds.add(bass);
  }

  public void toggleSound() {
    if (out == null)
      return;
    if ( out.isMuted() ) {
      out.unmute();
    } 
    else {
      out.mute();
    }
  }

  public AudioOutput getAudioOutput() {
    return out;
  }
}  

class Dodge {
  long tstamp;

  public Dodge(long tstamp) {
    this.tstamp = tstamp;
  }

  public void render(int c) {
    fill(c);
    rect(tstamp*CANVAS_WIDTH/metro.timeLength, 5 + 2*BEAT_HEIGHT, 25, BEAT_HEIGHT);
  }

}

class DodgeSound extends GameSound {

  int MAX_FREQ = 500;
  float SAMPL_FREQ = 44100;
  float VOL = 0.3f;

  SawWave dodge;
  float freq;
  
  public DodgeSound(AudioOutput out) {
    super(out);
    this.sustain = 6;
    freq = 0;
    dodge = new SawWave(calcNewFreq(), VOL, SAMPL_FREQ);
  }

  public void startSound() {
    super.startSound();
    out.addSignal(dodge);
  }
  public void stopSound() {
    out.removeSignal(dodge);
  }
  
  public boolean timePassed() {
    if (freq <= MAX_FREQ) {
        out.removeSignal(dodge);
        freq = freq*2;
        dodge = new SawWave(freq, VOL, SAMPL_FREQ);
        out.addSignal(dodge);
    } else {
      stopSound();
      return true;
    } 
    return super.timePassed();
  }
  
  public float calcNewFreq() {
    switch (PApplet.parseInt(random( 3 ))) {
    case 0:
      freq = 10.30f; //E1
      break;
    case 1:
      freq = 9.18f; // D1
      break;
    case 2:
      freq = 12.98f; // Ab1
      break;
    } 
    return freq;
  }

}
class GameSound {

  // length of the sound, multiplier of BASE_SOUND_LEN
  int sustain; 
  AudioOutput out;
  
  public GameSound(AudioOutput out) {
    this.out = out;
  }

  public void startSound() {
    //empty
  }
  public void stopSound() {
    //empty
  }

  public boolean timePassed() {
    sustain--;
    if (sustain == 0) {
      stopSound();
      return true;
    } 
    else {
      return false;
    }
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
      playSound();
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
    /*
    // Reset the metronome in case the time length changed
    fill(bgColor);
    rect(0, 0, CANVAS_WIDTH, BEAT_HEIGHT);
    // Now paint the marks
    fill(0, 255, 0);
    for (int j=0; j<4; j++) {
      rect(j*CANVAS_WIDTH/4 + metroOffset - beatWidth/2, 0, beatWidth, BEAT_HEIGHT);
    }*/
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
    float soundType = (beats % (BEATS_PER_TIME/4));
    if (soundType == 0)
      theDJ.playMetroKick();
    if (soundType == 2)
      theDJ.playMetroHitHatHard();
    if (soundType == 3)
      theDJ.playMetroHitHatHard();
    
    if (beats == 0 || beats == 8 || beats == 11)
      theDJ.playMetroBass(theDJ.FA);
    else if (beats == 4 || beats == 12)
      theDJ.playMetroBass(theDJ.MI);
    else if (beats == 14)
      theDJ.playMetroBass(theDJ.SIb); 
  }

  public void stopSound() {
   // out.disableSignal(ticSound);
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

class MetronomeBassSound extends GameSound {
  
  float VOL = 0.6f;
  float SAMPL_FREQ = 44100;

  SquareWave bass;

  public MetronomeBassSound(AudioOutput out, float note) {
    super(out);
    this.sustain = 2;
    bass = new SquareWave(note, VOL, SAMPL_FREQ);
  }

  public void startSound() {
    super.startSound();
    out.addSignal(bass);
  }
  public void stopSound() {
    out.removeSignal(bass);
  }

}
class MetronomeHitHatSound extends GameSound {
  
  int TYPE_HARD = 0;
  int TYPE_SOFT = 1;
  float VOL = 0.8f;

  AudioSignal noiseSignal;

  public MetronomeHitHatSound(AudioOutput out, int type) {
    super(out);
    this.sustain = 1;
    if (type == TYPE_HARD)
      noiseSignal = new WhiteNoise(VOL);
    else if (type == TYPE_SOFT)
      noiseSignal = new PinkNoise(VOL);
  }

  public void startSound() {
    super.startSound();
    out.addSignal(noiseSignal);
  }
  public void stopSound() {
    out.removeSignal(noiseSignal);
  }
  
}

class MetronomeKickSound extends GameSound {
  
  float SAMPL_FREQ = 44100;
  float VOL = 1;
  float FREQ = 20;
  
  TriangleWave kick;
  
  public MetronomeKickSound(AudioOutput out) {
    super(out);
    this.sustain = 1;
    kick = new TriangleWave(FREQ, VOL, SAMPL_FREQ);
  }

  public void startSound() {
    super.startSound();
    out.addSignal(kick);
  }
  public void stopSound() {
    out.removeSignal(kick);
  }
  
}
class ShotSound extends GameSound {

  int MIN_FREQ = 100;
  float SAMPL_FREQ = 44100;
  float VOL = 0.3f;

  SawWave bang;
  float freq;
  
  public ShotSound(AudioOutput out) {
    super(out);
    this.sustain = 3;
    freq = 0;
    bang = new SawWave(calcNewFreq(), VOL, SAMPL_FREQ);
  }

  public void startSound() {
    super.startSound();
    out.addSignal(bang);
  }
  public void stopSound() {
    out.removeSignal(bang);
  }
  
  public boolean timePassed() {
    if (freq >= MIN_FREQ) {
        out.removeSignal(bang);
        freq = freq/2;
        bang = new SawWave(freq, VOL, SAMPL_FREQ);
        out.addSignal(bang);
    } else {
      stopSound();
      return true;
    } 
    return super.timePassed();
  }
  
  public float calcNewFreq() {
    switch (PApplet.parseInt(random( 3 ))) {
    case 0:
      freq = 659.26f; //E5
      break;
    case 1:
      freq = 587.32f; // D5
      break;
    case 2:
      freq = 830.61f; // Ab5
      break;
    } 
    return freq;
  }

}
  static public void main(String args[]) {
    PApplet.main(new String[] { "--bgcolor=#FFFFFF", "BeatLaw" });
  }
}
