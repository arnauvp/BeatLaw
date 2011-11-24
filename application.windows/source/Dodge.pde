class Dodge {
  long tstamp;

  public Dodge(long tstamp) {
    this.tstamp = tstamp;
  }

  void render(color c) {
    fill(c);
    rect(tstamp*CANVAS_WIDTH/metro.timeLength, 5 + 2*BEAT_HEIGHT, 25, BEAT_HEIGHT);
  }

}

