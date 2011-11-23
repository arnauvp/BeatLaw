class Bullet {
  long originT;

  public Bullet(long originT) {
    this.originT = originT;
  }

  void render(color c) {
    fill(c);
    rect(originT*CANVAS_WIDTH/metro.timeLength, 5 + BEAT_HEIGHT, 25, BEAT_HEIGHT);
  }
}

