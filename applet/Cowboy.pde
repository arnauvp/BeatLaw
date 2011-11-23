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

  boolean shoot(long timestamp) {
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

