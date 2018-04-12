package music;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.URI;
import java.util.Iterator;
import java.util.Vector;


public class Window extends JPanel {
    private static final long serialVersionUID = 1L;
    /**
     *
     */
    /* Initialise the the dialog to choose a file from system */
    final JFileChooser addAudioDialog = new JFileChooser();
    Vector<String> columns;
    Vector<Vector<Object>> songs;
    private final JTextField search;
    private final JMenuItem menuItemAddSong;
    private final MusicCollection audioCollection;
    private final Database database;
    private Iterator<AudioFile> audioIterator;
    private final DefaultTableModel tableModel;
    private final TableRowSorter<DefaultTableModel> sorter;
    private final JTable table;


    // Create the panel
    public Window() {
        setLayout(new BorderLayout(0, 0));

        JPanel panel = new JPanel();
        add(panel, BorderLayout.SOUTH);

        JScrollPane scrollPane = new JScrollPane();
        add(scrollPane, BorderLayout.CENTER);

        /* Create a database instance */
        this.database = Database.getInstance();
        /* initialise collection */
        audioCollection = new MusicCollection(database.getSongs());

        /* create iterator */
        audioIterator = audioCollection.iteratorSongs();


        table = new JTable(buildTableModel(audioIterator));
        tableModel = (DefaultTableModel) table.getModel();
        sorter = new TableRowSorter<>(tableModel);
        /* Updates the table every time the model is changed*/
        table.setAutoCreateRowSorter(true);
        /* set sorter */
        table.setRowSorter(sorter);
        scrollPane.setViewportView(table);


        JMenuBar menuBar = new JMenuBar();
        add(menuBar, BorderLayout.NORTH);

        JMenu menu = new JMenu("Menu");
        menuBar.add(menu);

        menuItemAddSong = new JMenuItem("Add Song");
        menu.add(menuItemAddSong);
        //menuItemAddSong.addActionListener(new AddSongListener());
        menuItemAddSong.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (e.getSource() == menuItemAddSong) {
                    addSong();
                }
            }
        });

        JTextArea searchSong = new JTextArea();
        searchSong.setEditable(false);
        searchSong.setText("Search:");
        panel.add(searchSong);

        search = new JTextField(20);
        panel.add(search);


        search.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void removeUpdate(DocumentEvent e) {
                newFilter();
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                newFilter();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                newFilter();
            }
        });


        JButton btnDelete = new JButton("Delete Song");
        panel.add(btnDelete);
        btnDelete.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (audioCollection.isCollectionEmpty()) {
                    return;
                }
                deleteSong();
            }
        });


    }


    /**
     * Update the row filter regular expression from the expression in
     * the text box.
     */
    private void newFilter() {
        String searchString = search.getText();
        RowFilter<DefaultTableModel, Object> rowFilter;

        //If current expression doesn't parse, don't update.
        try {
            rowFilter = RowFilter.regexFilter("(?i)" + searchString);
        } catch (java.util.regex.PatternSyntaxException e) {
            return;
        }
        sorter.setRowFilter(rowFilter);

    }

    private DefaultTableModel buildTableModel(Iterator<AudioFile> all) {

        columns = new Vector<>();
        columns.addElement("Artist");
        columns.addElement("Title");
        columns.addElement("Album");
        columns.addElement("Year");
        columns.addElement("URI");
        songs = new Vector<>();

        while (all.hasNext()) {
            AudioFile song = all.next();
            Vector<Object> vector = new Vector<>();
            vector.add(song.getArtist());
            vector.add(song.getTitle());
            vector.add(song.getAlbum());
            vector.add(song.getYear());
            vector.add(song.getURI());
            songs.addElement(vector);
        }

        return new DefaultTableModel(songs, columns);

    }

    private void deleteSong() {
        int x = 1;
        int y = table.getModel().getRowCount();
        if (x == y) {
            database.deleteLastFile();
        }

        /* initialise the iterator */
        audioIterator = audioCollection.iteratorSongs();
        /* gives us the selected song's index row */
        int iRow = table.getSelectedRow();

        AudioFile delete = null;
        /* Recreate the URI from the table */
        URI uri = URI.create(table.getValueAt(iRow, 4).toString());
        /* Find the file using the URI */
        while (audioIterator.hasNext()) {
            AudioFile audio = audioIterator.next();
            if (uri.equals(audio.getURI())) {
                delete = audio;
                break;
            }
        }
        /* Delete from music collection */
        audioCollection.deleteFileFromCollection(delete);
        /* Delete from database */
        database.deleteSong(delete);
        /* remove song from the list */
        tableModel.removeRow(iRow);
    }

    private void addSong(){
        /* variable to hold the constant field values for the state of the chooser */
        int returnVal = addAudioDialog.showOpenDialog(Window.this);
        /* Show only mp3 files */
        addAudioDialog.addChoosableFileFilter(new FileNameExtensionFilter("MP3 file", "mp3"));
        System.out.println("Got file");
        /* if button click yes */
        if (returnVal == JFileChooser.APPROVE_OPTION) {

            File file = addAudioDialog.getSelectedFile();
            System.out.println("created file object of the song");
            MetadataExtraction m = new MetadataExtraction();
            /* Make a song object from the chosen file */
            AudioFile tempSong = m.accessFile(file);
            audioCollection.addFileToCollection(tempSong);
            System.out.println("Successful extraction");
            /* Add song to display of songs */
            tableModel.addRow(new Object[]{tempSong.getArtist(), tempSong.getTitle(),
                    tempSong.getAlbum(), tempSong.getYear(), tempSong.getURI()});

            /* Add the song to the database */
            database.saveFile(tempSong);
            System.out.println("song saved to database");
        }
    }

}
