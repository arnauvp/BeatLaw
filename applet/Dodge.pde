class Dodge {
  long tstamp;

  public Dodge(long tstamp) {
    this.tstamp = tstamp;
  }

  void render() {
    fill(255,255,0);
    rect(tstamp*CANVAS_WIDTH/metro.timeLength, 5 + 2*BEAT_HEIGHT, 25, BEAT_HEIGHT);
    //rect(originT, 5+BEAT_HEIGHT, timelineTickWidth, BEAT_HEIGHT);
  }

}

