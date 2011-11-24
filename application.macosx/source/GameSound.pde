class GameSound {

  // length of the sound, multiplier of BASE_SOUND_LEN
  int sustain; 
  AudioOutput out;
  
  public GameSound(AudioOutput out) {
    this.out = out;
  }

  void startSound() {
    //empty
  }
  void stopSound() {
    //empty
  }

  boolean timePassed() {
    sustain--;
    if (sustain == 0) {
      stopSound();
      return true;
    } 
    else {
      return false;
    }
  }
}

