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
  void start () {
    running = true;
    println("Starting metronome (will execute every " + beatDelay + " milliseconds.)"); 
    super.start();
  }

  // We must implement run, this gets triggered by start()
  void run () {
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
  void quit() {
    System.out.println("Quitting."); 
    running = false;  // Setting running to false ends the loop in run()
    // In case the thread is waiting. . .
    interrupt();
  }

  void drawMarks() {
    // Reset the metronome in case the time length changed
    fill(bgColor);
    rect(0, 0, CANVAS_WIDTH, BEAT_HEIGHT);
    // Now paint the marks
    fill(0, 255, 0);
    for (int j=0; j<4; j++) {
      rect(j*CANVAS_WIDTH/4 + metroOffset - beatWidth/2, 0, beatWidth, BEAT_HEIGHT);
    }
  }

  void render() {
    noStroke();
    int metroSpacing = CANVAS_WIDTH/4;
    drawMarks(); // Clear the other 3 marks
    fill(0, 100, 100);
    rect((beats/(BEATS_PER_TIME/4))*metroSpacing + metroOffset - beatWidth/2, 0, beatWidth, BEAT_HEIGHT);
    metroXPos += metroSpacing;
  }

  void playSound() {
    //out.enableSignal(ticSound);
  }

  void stopSound() {
    out.disableSignal(ticSound);
  }

  int getBeatWidth() {
    return beatWidth;
  }

  int getBeatCount() {
    return beats;
  }

  int getBeatsPerTime() {
    return BEATS_PER_TIME;
  }

  int getTimeLength() {
    return timeLength;
  }

  boolean isFullTic() {
    return (beats % (BEATS_PER_TIME/4) == 0);
  }

  float getTicNum() {
    int tmp = beats/(BEATS_PER_TIME/8);
    return (float)tmp/2;
  }
}

