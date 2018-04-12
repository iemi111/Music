package music;

import java.net.URI;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.sql.Statement;
import java.util.ArrayList;

public class Database {

    private static Database instance = null;
    private Connection connect;
    private Statement statement;

    private Database() {
        super();
        this.connect = null;
        this.statement = null;

    }

    public static Database getInstance(){
        if(instance == null){
            instance = new Database();
        }
        return instance;
    }

    /**
     * Open connection
     */
    public void openConnection() {

        try {
            // recreate the connection if needed
            if (this.connect == null || this.connect.isClosed()) {
				/* Connect to database */

                this.connect = DriverManager.getConnection("jdbc:hsqldb:file:myDB","SA", "");
            }

        } catch (SQLException e) {
            System.out
                    .println("ERROR - Failed to create a connection to the database");
            throw new RuntimeException(e);
        }

        try {
            this.statement = this.connect.createStatement();

            /* create a database with a table if it doesn't exist */
            DatabaseMetaData meta = connect.getMetaData();

            //System.out.println("I was here");
            ResultSet res = meta.getTables(null, null, "ALLSONGS",
                    new String[] {"TABLE"});
            if (res.next()==false) {
                System.out.println("Created initial table");
                statement.executeUpdate("CREATE TABLE ALLSONGS (URI VARCHAR(500), ARTIST VARCHAR(40), TITLE VARCHAR(40), "
                        + "ALBUM VARCHAR(40), YEAR VARCHAR(4), LYRICS VARCHAR(4500), PRIMARY KEY (URI))");
            }
            this.statement.close();
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }


    /**
     * Close connection to database
     */
    public void closeConnection() {

        try {
            if (this.connect != null) {
                this.connect.close();
                this.statement.close();
            }
            System.out.println("Closed the connection to the database");
        } catch (Exception e) {
            System.out
                    .print("ERROR-Failed to close the connection to the database");
            throw new RuntimeException(e);
        }
    }

    /**
     * Gives an array with all of the songs from the database.
     * It called when the application is started so that MusicCollection
     * gets initialised.
     * @return List of songs
     */
    public ArrayList<AudioFile> getSongs(){
        //open connection
        this.openConnection();

        ArrayList<AudioFile> songs = new ArrayList<>();

        try {
            this.statement = this.connect.createStatement();
            ResultSet result = this.statement.executeQuery("SELECT * FROM ALLSONGS");

            if(result.next()){
                while (result.next()) {
                    String uriText = result.getString("URI");
                    String artist = result.getString("ARTIST");
                    String title = result.getString("TITLE");
                    String album = result.getString("ALBUM");
                    String year = result.getString("YEAR");
                    String lyrics = result.getString("LYRICS");
                    URI uri = URI.create(uriText);
			/* recreate song from database */
                    AudioFile audio = new AudioFile(uri, artist, title, album, year, lyrics);
                    songs.add(audio);
                    this.statement.close();
                }
            }

        }catch (SQLException e) {
            System.out.println("SQLException happened while retrieving records- abort programmme");
            throw new RuntimeException(e);
        }
        //close connection
        closeConnection();

        return songs;

    }



    /**
     * Add the song to the database using Savepoint and commit() to ensure that
     * there will be no errors while saving the file.
     * @param audio we want to save
     */
    public void saveFile(AudioFile audio){
        //open connection
        this.openConnection();
        try {
            connect.setAutoCommit(false);
        } catch (SQLException e1) {
            e1.printStackTrace();
        }
		/* make a savepoint variable */
        Savepoint savepointAdd = null;

        try{
		/* Add a save point to return to this point if something has gone wrong */
            savepointAdd = connect.setSavepoint("AddFileSavepoint");
		/* insert the song object into the database */
            PreparedStatement preparedStatement = connect.prepareStatement("INSERT INTO ALLSONGS (URI, ARTIST, TITLE, "
                    + "ALBUM, YEAR, LYRICS) VALUES (?,?,?,?,?,?)");
            preparedStatement.setString(1, audio.getURI().toString());
            preparedStatement.setString(2, audio.getArtist());
            preparedStatement.setString(3, audio.getTitle());
            preparedStatement.setString(4, audio.getAlbum());
            preparedStatement.setString(5, audio.getYear());
            preparedStatement.setString(6, audio.getLyrics());
            preparedStatement.executeUpdate();
		/* Ensure there are changes to the database */
            connect.commit();
		/* close the statement */
            preparedStatement.close();
        } catch(SQLException sqlException){
			/* if an error occurs go back to before the database was accessed to save the song */
            try {
                sqlException.printStackTrace();
                connect.rollback(savepointAdd);
                System.out.println("Song wasn't saved in database");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        //close connection
        closeConnection();
    }



    /**
     * Delete a song from database
     * @param song
     */
    public void deleteSong(AudioFile song){
        //open connection
        openConnection();

        Savepoint savepointDelete = null;

        try {
            connect.setAutoCommit(false);
            savepointDelete = connect.setSavepoint("DeleteFileSavepoint");
			/* Statement to delete the song */
            PreparedStatement preparedStatement = connect.prepareStatement("DELETE FROM ALLSONGS WHERE URI = ?");
            preparedStatement.setString(1, song.getURI().toString());
            preparedStatement.executeUpdate();
            connect.commit();
            preparedStatement.close();
        } catch (SQLException exception){
			/* if an error occurs */
            try {
                connect.rollback(savepointDelete);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        //close connection
        closeConnection();
    }


    public void deleteLastFile(){
        openConnection();

        try {
            this.statement = connect.createStatement();
            statement.executeUpdate("DROP TABLE ALLSONGS");
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        closeConnection();

    }

}
