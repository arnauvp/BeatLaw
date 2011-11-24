class MetronomeKickSound extends GameSound {
  
  float SAMPL_FREQ = 44100;
  float VOL = 1;
  float FREQ = 20;
  
  TriangleWave kick;
  
  public MetronomeKickSound(AudioOutput out) {
    super(out);
    this.sustain = 1;
    kick = new TriangleWave(FREQ, VOL, SAMPL_FREQ);
  }

  void startSound() {
    super.startSound();
    out.addSignal(kick);
  }
  void stopSound() {
    out.removeSignal(kick);
  }
  
}
