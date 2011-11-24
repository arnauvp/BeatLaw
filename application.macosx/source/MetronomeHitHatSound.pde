class MetronomeHitHatSound extends GameSound {
  
  int TYPE_HARD = 0;
  int TYPE_SOFT = 1;
  float VOL = 0.8;

  AudioSignal noiseSignal;

  public MetronomeHitHatSound(AudioOutput out, int type) {
    super(out);
    this.sustain = 1;
    if (type == TYPE_HARD)
      noiseSignal = new WhiteNoise(VOL);
    else if (type == TYPE_SOFT)
      noiseSignal = new PinkNoise(VOL);
  }

  void startSound() {
    super.startSound();
    out.addSignal(noiseSignal);
  }
  void stopSound() {
    out.removeSignal(noiseSignal);
  }
  
}

