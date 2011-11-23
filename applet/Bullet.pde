class Bullet {
  long originT;

  public Bullet(long originT) {
    this.originT = originT;
  }

  void render() {
    fill(255,0,0);
    rect(originT*CANVAS_WIDTH/metro.timeLength, 5 + BEAT_HEIGHT, 25, BEAT_HEIGHT);
    //rect(originT, 5+BEAT_HEIGHT, timelineTickWidth, BEAT_HEIGHT);
  }
}

