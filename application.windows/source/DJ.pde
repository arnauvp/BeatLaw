import ddf.minim.*;
import ddf.minim.signals.*;
import ddf.minim.effects.*;

class DJ extends Thread {

  // All sound lengths will be a multiple
  // of this one 
  long BASE_SOUND_LEN = 20;//ms
  float MI = 58.27;
  float SIb = 73.42;
  float FA = 77.78;
  int HITHAT_TYPE_HARD = 0;
  int HITHAT_TYPE_SOFT = 1;

  PApplet app;
  Minim minim;
  AudioOutput out;
  ArrayList sounds;
  boolean running = false;

  public DJ(PApplet app) {
    this.app = app;
    sounds = new ArrayList(10);
  }

  void initSoundSystem() {
    minim = new Minim(app);
    out = minim.getLineOut(Minim.STEREO, 256);
    running = true;
    super.start();
  }

  void stopSoundSystem() {
    out.mute();
    // backwards because we're removing items
    GameSound nextSound;
    for (int i = sounds.size()-1; i >= 0; i--) {
      nextSound = (GameSound)sounds.get(i);
      nextSound.stopSound();
      sounds.remove(i);
    }
    out.close();
    minim.stop();
    minim = null;
    running = false;
    interrupt();
  }

  void run() {
    println("DJ starts the party");
    while (running) {
      try {
        sleep(BASE_SOUND_LEN);
        GameSound nextSound;
        for (int i = sounds.size()-1; i >= 0; i--) {
          nextSound = (GameSound)sounds.get(i);
          if(nextSound.timePassed())
            sounds.remove(i);
        }
      } 
      catch (Exception e) {
        if (running)
          e.printStackTrace();
      }
    }
    println("DJ goes home");
  }

  void playShot() {
    ShotSound shot = new ShotSound(out);
    shot.startSound();
    sounds.add(shot);
  }
  
  void playDodge() {
    DodgeSound dodge = new DodgeSound(out);
    dodge.startSound();
    sounds.add(dodge);
  }

  void playMetroKick() {
    MetronomeKickSound kick = new MetronomeKickSound(out);
    kick.startSound();
    sounds.add(kick);
  }
  
  void playMetroHitHatHard() {
    MetronomeHitHatSound hitHat = new MetronomeHitHatSound(out, HITHAT_TYPE_HARD);
    hitHat.startSound();
    sounds.add(hitHat);
  }
  
  void playMetroHitHatSoft() {
    MetronomeHitHatSound hitHat = new MetronomeHitHatSound(out, HITHAT_TYPE_SOFT);
    hitHat.startSound();
    sounds.add(hitHat);
  }
  
  void playMetroBass(float note) {
    MetronomeBassSound bass = new MetronomeBassSound(out, note);
    bass.startSound();
    sounds.add(bass);
  }

  void toggleSound() {
    if (out == null)
      return;
    if ( out.isMuted() ) {
      out.unmute();
    } 
    else {
      out.mute();
    }
  }

  AudioOutput getAudioOutput() {
    return out;
  }
}  

