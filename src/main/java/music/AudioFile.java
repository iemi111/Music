package music;

import java.net.URI;

public class AudioFile {

	private String title;
	private String artist;
	private String album;
	private String year;
	private String lyrics;
	private URI uri;
	
	
	
	public AudioFile(URI uri, String artist, String title, String album, String year, String lyrics) {
		super();
		this.uri = uri;
		this.artist = artist;
		this.album = album;
		this.title = title;
		this.year = year;
		this.lyrics = lyrics;
	}



	public String getTitle() {
		return title;
	}

	public String getArtist() {
		return artist;
	}

	public String getAlbum() {
		return album;
	}

	public String getYear() {
		return year;
	}
	
	
	public String getLyrics(){
		return lyrics;
	}
	
	public URI getURI(){
		return uri;
	}
	
	
		

}
         