class ShotSound extends GameSound {

  int MIN_FREQ = 100;
  float SAMPL_FREQ = 44100;
  float VOL = 0.3;

  SawWave bang;
  float freq;
  
  public ShotSound(AudioOutput out) {
    super(out);
    this.sustain = 3;
    freq = 0;
    bang = new SawWave(calcNewFreq(), VOL, SAMPL_FREQ);
  }

  void startSound() {
    super.startSound();
    out.addSignal(bang);
  }
  void stopSound() {
    out.removeSignal(bang);
  }
  
  boolean timePassed() {
    if (freq >= MIN_FREQ) {
        out.removeSignal(bang);
        freq = freq/2;
        bang = new SawWave(freq, VOL, SAMPL_FREQ);
        out.addSignal(bang);
    } else {
      stopSound();
      return true;
    } 
    return super.timePassed();
  }
  
  float calcNewFreq() {
    switch (int(random( 3 ))) {
    case 0:
      freq = 659.26; //E5
      break;
    case 1:
      freq = 587.32; // D5
      break;
    case 2:
      freq = 830.61; // Ab5
      break;
    } 
    return freq;
  }

}
