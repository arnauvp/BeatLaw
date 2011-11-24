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

  void render(float ticNum) {
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

