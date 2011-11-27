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

  void start() {
    running = true;
    //println("BT starting ");
    super.start();
  }

  void run() {
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
        //println("shot dodged sucessfully!");
      }
      nextShot++;
    }
    //println("BT finishing");
  }

  void quit() {
    running = false;
    interrupt();
  }

  void dodgeAttempt(long dodgeRelTime) {
    dodgeTimestamps[nextShot] = dodgeRelTime;
  }

  boolean isShotDodged(long shotTime, long dodgeTime) {
    return abs(shotTime-dodgeTime) <= 2*DODGE_TOLERANCE;
  }
}

