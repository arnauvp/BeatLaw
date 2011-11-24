class MetronomeBassSound extends GameSound {
  
  float VOL = 0.6;
  float SAMPL_FREQ = 44100;

  SquareWave bass;

  public MetronomeBassSound(AudioOutput out, float note) {
    super(out);
    this.sustain = 2;
    bass = new SquareWave(note, VOL, SAMPL_FREQ);
  }

  void startSound() {
    super.startSound();
    out.addSignal(bass);
  }
  void stopSound() {
    out.removeSignal(bass);
  }

}
