class DodgeSound extends GameSound {

  int MAX_FREQ = 500;
  float SAMPL_FREQ = 44100;
  float VOL = 0.3;

  SawWave dodge;
  float freq;
  
  public DodgeSound(AudioOutput out) {
    super(out);
    this.sustain = 6;
    freq = 0;
    dodge = new SawWave(calcNewFreq(), VOL, SAMPL_FREQ);
  }

  void startSound() {
    super.startSound();
    out.addSignal(dodge);
  }
  void stopSound() {
    out.removeSignal(dodge);
  }
  
  boolean timePassed() {
    if (freq <= MAX_FREQ) {
        out.removeSignal(dodge);
        freq = freq*2;
        dodge = new SawWave(freq, VOL, SAMPL_FREQ);
        out.addSignal(dodge);
    } else {
      stopSound();
      return true;
    } 
    return super.timePassed();
  }
  
  float calcNewFreq() {
    switch (int(random( 3 ))) {
    case 0:
      freq = 10.30; //E1
      break;
    case 1:
      freq = 9.18; // D1
      break;
    case 2:
      freq = 12.98; // Ab1
      break;
    } 
    return freq;
  }

}
