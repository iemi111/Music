package music;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;

import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.mp3.LyricsHandler;
import org.apache.tika.parser.mp3.Mp3Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.SAXException;


public class MetadataExtraction {

	Metadata metadata;
	Mp3Parser  Mp3Parser;
	BodyContentHandler handler;
    FileInputStream inputStream;
    ParseContext pcontext;
    LyricsHandler lyrics;
   
	
	public MetadataExtraction() {
		super();
		//not sure if meta data should be here
		metadata = new Metadata();
		Mp3Parser = new  Mp3Parser();
		handler = new BodyContentHandler();
		pcontext = new ParseContext();
	}
	
	
	
	/**
	 * Accessing the song's metadata and returning the song's object 
	 * @param file
	 */
	public AudioFile accessFile(File file){
		/* Make a connection with the file */
		try {
			inputStream = new FileInputStream(new File(file.getAbsolutePath()));
		} catch (FileNotFoundException fileNotFoundException){
			fileNotFoundException.printStackTrace();
		}
		
		try {
			Mp3Parser.parse(inputStream, handler, metadata, pcontext);
		} catch (IOException | SAXException | TikaException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		URI uri = file.toURI();
		
		return assignFileInfo(metadata, uri);
	}
	
	/**
	 * Method to create the artist, album... to a song object
	 * @param metadata
	 * @return an audio file object with its info
	 */
	private AudioFile assignFileInfo(Metadata metadata, URI uri){
		
		String title = metadata.get("title");
		String artist = metadata.get("xmpDM:artist");
		String album = metadata.get("xmpDM:album");
		String year = metadata.get("xmpDM:releaseDate");
		year = year.substring(0, 4);
		/* get the lyrics of the song */
		String lyrics;
		lyrics = getLyrics();
		
		AudioFile song = new AudioFile(uri, artist, title, album, year, lyrics);
		
		try {
			inputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return song;
		
	}
	
	/**
	 * Check for lyrics, if there aren't any we get "No Lyrics" 
	 * @return the lyrics of the song
	 */
	private String getLyrics(){
		
		String lyricsText = "";
		
		try {
			lyrics = new LyricsHandler(inputStream,handler);
		} catch (IOException | SAXException | TikaException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(lyrics.hasLyrics()){
			while(lyrics.hasLyrics()) {
	    	System.out.println(lyrics.toString());
	      }
		}
		
		return lyricsText;
	}
	
	
	
	
	
	
	
	
	
}
