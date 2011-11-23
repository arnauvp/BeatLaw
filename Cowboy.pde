class Cowboy {

  int STAND_STILL = 0;
  int MIN_SHOOT_POS = 1;
  int MAX_SHOOT_POS = 6;
  int MIN_DODGE_POS = 7;
  int MAX_DODGE_POS = 12;

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

  public Cowboy(String name, String imgPath, int posX, int posY) {
    this.name = name;
    this.posX = posX;
    this.posY = posY;
    imgs = new PImage[MAX_DODGE_POS];
    for (int j=0; j<MAX_DODGE_POS; j++) {
      this.imgs[j] = loadImage(imgPath + "/" + j + ".png");
    }
    reload();
  }

  void render(boolean active, boolean shooting) {
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

  int currentStance(boolean shooting) {

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
  
  int randomShootingStance() {
     return (int) random(MIN_SHOOT_POS, MAX_SHOOT_POS);    
  }
  
  int randomDodgingStance() {
     return (int) random(MIN_DODGE_POS, MAX_DODGE_POS);    
  }

  boolean shoot(long timestamp) {
    if (shotsLeft == 0) {
      println(name + ": Geez, I have no bullets left!");
      return false;
    }
    theDJ.playShot();
    shotsTime[MAX_SHOTS-shotsLeft] = timestamp;
    bullets[MAX_SHOTS-shotsLeft] = new Bullet(timestamp);
    shotsLeft--;
    println(name + " shoots! " + shotsLeft + " bullets left " + timestamp);
    return true;
  }

  void reload() {
    shotsLeft = MAX_SHOTS;
    shotsTime = new long[] {
      -1, -1, -1, -1, -1, -1
    };
    bullets = new Bullet[MAX_SHOTS];
  }

  long[] getShots() {
    return shotsTime;
  }

  void dodge(long timestamp) {
   println(name + " dodges! " + dodgeCount + " " + timestamp);
   theDJ.playDodge();
   dodges[dodgeCount] = new Dodge(timestamp);
   dodgeCount++;
  }

  void restartDuel() {
  }
  
  void newTurn() {
    dodges = new Dodge[MAX_DODGES];
    dodgeCount = 0; 
  }

  String getName() {
    return name;
  }
}

