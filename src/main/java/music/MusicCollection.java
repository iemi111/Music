package music;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;




public class MusicCollection {

	private List<AudioFile> collectionMusic;
	
	
	/**
	 * 
	 * @param songs from database
	 */
	public MusicCollection(ArrayList<AudioFile> songs) {
		super();
		this.collectionMusic = songs;
	}

	
	/**
	 * Go through all songs with an iterator
	 */
	public Iterator<AudioFile> iteratorSongs(){	
		
	return collectionMusic.iterator();
	
	}
	
	
	public void addFileToCollection(AudioFile file){
		collectionMusic.add(file);
	}
	
	public void deleteFileFromCollection(AudioFile file){
		collectionMusic.remove(file);
	}

	public boolean isCollectionEmpty() {
		return collectionMusic.isEmpty();
	}
}