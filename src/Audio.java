import java.io.File;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

public class Audio {
	private Clip song;
	private String path = "";
	private int count;

	Audio(){
		try{
			song = AudioSystem.getClip();
		} catch(Exception ex){System.out.println("Error with playing sound");}
	}

	public static void main(String[] args) {
		Audio a = new Audio();
		a.playPing();
		a.playDoot();
		a.playPang();
		a.playSong();
		a.stopSong();
	}

	public void playPing() {
		try {
			AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new File(path+"ping.wav").getAbsoluteFile());
			Clip clip = AudioSystem.getClip();
			clip.open(audioInputStream);
			clip.start();
		} catch(Exception ex) {
			System.out.println("Error with playing sound.");
			ex.printStackTrace();
		}
	}

	public void playPang(){
		try {
			AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new File(path+"pang.wav").getAbsoluteFile());
			Clip clip = AudioSystem.getClip();
			clip.open(audioInputStream);
			clip.start();
		} catch(Exception ex) {
			System.out.println("Error with playing sound.");
			ex.printStackTrace();
		}
	}

	public void playDoot() {
		try {
			AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new File(path+"doot.wav").getAbsoluteFile());
			Clip clip = AudioSystem.getClip();
			clip.open(audioInputStream);
			clip.start();
		} catch(Exception ex) {
			System.out.println("Error with playing sound.");
			ex.printStackTrace();
		}
	}

	public void playSong(){
		try{
			AudioInputStream battle = AudioSystem.getAudioInputStream(new File(path+"song.wav").getAbsoluteFile());
			if(count == 0){
				song.open(battle);
				count++;
			}
			song.loop(30);
		}
		catch(Exception ex){}
	}

	public void stopSong(){
		song.stop();
		song.setFramePosition(0);
	}
}