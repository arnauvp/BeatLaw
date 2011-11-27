
/* Graphic variables - time independent*/
int CANVAS_WIDTH = 800;
int CANVAS_HEIGHT = 600;
int SKETCH_FRAMERATE = 60;
int BEAT_HEIGHT = 20;
int FRAME_STROKE_WIDTH = 20;
int FRAME_YPOS_START = CANVAS_HEIGHT/5;
int FRAME_HEIGHT = CANVAS_WIDTH/2;
color bgColor = color(41, 49, 189);
color frameColor = color(190, 96, 255);


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
color p1Color = color(255, 129, 31);
color p2Color = color(234, 81, 235);

boolean justShot = false;
boolean currentPlayerShooting = true; 
boolean started = false;
boolean gameOver = false;

String startupText= "Click on this box.\nPress 's' to start, 'r' to reset, 'm' to mute";

/* Main control methods - setup, start, stop */
void setup () {
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

void stopSketch() {
  if (!started)
    return;
  metro.quit();
  theDJ.stopSoundSystem();
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
  if (!started) {
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
  if (gameOver) {
      stopSketch();
      showClosingScreen();
      return;
  } 
}

void drawFrame() {
  stroke(frameColor);
  strokeWeight(FRAME_STROKE_WIDTH);
  fill(0, 0, 0, 0);
  rect(FRAME_STROKE_WIDTH/2, FRAME_YPOS_START, CANVAS_WIDTH-FRAME_STROKE_WIDTH, FRAME_HEIGHT-FRAME_STROKE_WIDTH/2);
  noStroke();
}

void showOpeningScreen() {
  /*openingScreen = new Gif(this, OPENING_SCREEN_PATH);
   openingScreen.loop();*/
  openingScreen = loadImage(OPENING_SCREEN_PATH);
  image(openingScreen, 0, 0);
}

void dismissOpeningScreen() {
  //openingScreen.stop();
  openingScreen = null;
}

void showClosingScreen() {
  PImage closingScreen = loadImage(CLOSING_SCREEN_PATH);
  //background(bgColor);
  image(closingScreen, CANVAS_WIDTH/2 - closingScreen.width/2, CANVAS_HEIGHT/2 - closingScreen.height/2);
}

void newLine() {
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
  if (currentPlayer.lifeDown() == 0) {
    gameOver = true;
    println(">>>> Game over! " + otherPlayer.getName() + " wins! <<<<");
  }
}


/* Key events */
void keyPressed() {
  if (handleSpecialKey())
    return;
  getUpdatedElapsedTime();
  // Only shoot/dodge with A/L for P1/P2
  if (((key == 'a' || key == 'A') && (p1 == currentPlayer)) ||
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

boolean handleSpecialKey() {
  if (!started) {
    if (key >= 'A' && key <= 'z') {
      
      // Don't restart immediately
      if (gameOver && getUpdatedElapsedTime() < 4000)
        return true;
        
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


color getBGColor() {
  return bgColor;
}

synchronized long getUpdatedElapsedTime() {
  elapsed = millis()-beatStart;
  return elapsed;
}

