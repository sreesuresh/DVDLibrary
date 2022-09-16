package dao;


import dto.Dvd;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;


public class DvdLibraryDaoFileImpl implements DvdLibraryDao {
    /*As DvdLibraryDaoFileImpl implements DvdLibraryDao, DvdLibraryDaoFileImpl must provide implementations for each of the methods defined in the
    DvdLibraryDao interface. The DAO is responsible for the persistence and retrieval of the DVD data. DAO is a part of the model of the MVC pattern.*/

    //dvds map to hold data in memory
    private Map<String, Dvd> dvds = new HashMap<>();
    private final String LIBRARY_FILE;  //static removed
    private final String DELIMITER = "::";

    //default constructor
    public DvdLibraryDaoFileImpl() {
        LIBRARY_FILE = "DvdLibrary.txt";
    }

    //constructor to use when testing
    // required to separate the production data from the test data
    //now we can create instances of DvdLibraryDaoFileImpl that utilise another file.
    //This ensures we dont overwrite our production application data during testing.

    public DvdLibraryDaoFileImpl(String libraryTextFile) {

        LIBRARY_FILE = libraryTextFile;
    }

    @Override
    public Dvd addDvd(String title, Dvd dvd) throws DvdLibraryDaoException {
        //loadLibrary reads the LIBRARY_FILE into memory.
        loadLibrary();
        //New DVD is added to the dvds HashMap.
        Dvd newDvd = dvds.put(title, dvd);
        //writeLibrary writes all the DVDs in the DVD library out to a LIBRARY_FILE.
        writeLibrary();
        return newDvd;
    }

    @Override
    public Dvd removeDvd(String title) throws DvdLibraryDaoException {
        loadLibrary();
        Dvd removedDvd = dvds.remove(title);
        writeLibrary();
        return removedDvd;
    }

    /**
     * This code gets all of the DVD objects out of the DVD map as a collection by calling values() method. We pass that returned
     * collection into a the constructor for a new ArrayList. As one of the constructors of ArrayList takes a collection as a parameter
     * we can effectively convert the collection of DVD objects into an ArrayList of DVD objects that we can return from the method.
     *
     * @return As ArrayList implements the List interface,it can be treated as a list and so we can return an ArrayList.
     * @throws com.chloe.dvdlibrary.dao.DvdLibraryDaoException
     */
    @Override
    public List<Dvd> getAllDvds() throws DvdLibraryDaoException {
        loadLibrary();
        return new ArrayList(dvds.values());
    }

    @Override
    public Dvd getDvd(String title) throws DvdLibraryDaoException {
        loadLibrary();
        return dvds.get(title);
    }

    @Override
    public Dvd changeReleaseDate(String title, LocalDate releaseDate) throws DvdLibraryDaoException {
        loadLibrary();
        Dvd dvdToEdit = dvds.get(title);
        dvdToEdit.setReleaseDate(releaseDate);
        writeLibrary();
        return dvdToEdit;
    }

    @Override
    public Dvd changeMpaaRating(String title, String mpaaRating) throws DvdLibraryDaoException {
        loadLibrary();
        Dvd dvdToEdit = dvds.get(title);
        dvdToEdit.setMpaaRating(mpaaRating);
        writeLibrary();
        return dvdToEdit;
    }

    @Override
    public Dvd changeDirectorName(String title, String directorName) throws DvdLibraryDaoException {
        loadLibrary();
        Dvd dvdToEdit = dvds.get(title);
        dvdToEdit.setDirectorName(directorName);
        writeLibrary();
        return dvdToEdit;
    }

    @Override
    public Dvd changeUserRating(String title, String userRating) throws DvdLibraryDaoException {
        loadLibrary();
        Dvd dvdToEdit = dvds.get(title);
        dvdToEdit.setUserRating(userRating);
        writeLibrary();
        return dvdToEdit;
    }

    @Override
    public Dvd changeStudioName(String title, String studioName) throws DvdLibraryDaoException {
        loadLibrary();
        Dvd dvdToEdit = dvds.get(title);
        dvdToEdit.setStudio(studioName);
        writeLibrary();
        return dvdToEdit;
    }

    @Override
    public Map<String, Dvd> getDvdsLastYears(int years) throws DvdLibraryDaoException {
        LocalDate now = LocalDate.now();
        LocalDate sinceThisDate = now.minusYears(years);
        loadLibrary();
        Map<String, Dvd> dvdsLastYears = dvds.entrySet().stream()
                .filter((dvd) -> dvd.getValue().getReleaseDate().isAfter(sinceThisDate))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        return dvdsLastYears;
    }

    @Override
    public Map<String, Dvd> getDvdsByMpaaRating(String mpaaRating) throws DvdLibraryDaoException {
        loadLibrary();
        Map<String, Dvd> dvdsMpaRating = dvds
                .entrySet()
                .stream()
                .filter((dvd) -> dvd.getValue().getMpaaRating().equalsIgnoreCase(mpaaRating))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        return dvdsMpaRating;
    }

    @Override
    public Map<String, Dvd> getDvdsByDirector(String directorName) throws DvdLibraryDaoException {
        loadLibrary();
        Map<String, Dvd> dvdsByDirector = dvds
                .entrySet()
                .stream()
                .filter((dvd) -> dvd.getValue().getDirectorName().equalsIgnoreCase(directorName))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        return dvdsByDirector;
    }

    @Override
    public Map<String, Dvd> getDvdsByStudio(String studioName) throws DvdLibraryDaoException {
        loadLibrary();
        Map<String, Dvd> dvdsByStudio = dvds
                .entrySet().stream().filter((dvd) -> dvd.getValue().getStudio().equalsIgnoreCase(studioName))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        return dvdsByStudio;
    }


    //FILE PERSISTENCE
    // Data Marshalling & Unmarshalling

    /**
     * marshallDvd organises the DVD information from an in memory object into a
     * line of text, so it is in an appropriate format for writing it to permanent storage.
     * @param aDvd a DVD object in memory
     * @return a String consisting of the format DVD title::release date::MPAA rating::director name::user rating
     */
    private String marshallDvd(Dvd aDvd) {
        //A DVD object needs to be turned into a line of text for the file.
        //E.g, need to turn an in memory object to end up like this:
        //The Shawshank Redemption::1994::15::Frank Darabont::Excellent::Castle Rock Entertainment

        //the properties are got out of the DVD object using getters and these are
        //concatenated with the delimiter as a kind of spacer.
        String dvdAsText = aDvd.getTitle() + DELIMITER;
        dvdAsText += aDvd.getReleaseDate() + DELIMITER;
        dvdAsText += aDvd.getMpaaRating() + DELIMITER;
        dvdAsText += aDvd.getDirectorName() + DELIMITER;
        dvdAsText += aDvd.getUserRating() + DELIMITER;
        dvdAsText += aDvd.getStudio();
        return dvdAsText;
    }

    /**
     * UnmarshallDvd translates a line of text into a DVD object.
     * @param dvdAsText
     * @return
     */
    private Dvd unmarshallDvd(String dvdAsText) {
        //dvdAsText is expecting a line read in from our file.
        //E.g., it might look like this:
        //The Car::1996::15::Frank Wilson::Excellent::Saina Entertainment
        //This line is then split at the DELIMITER (::), leaving an array of Strings,
        //stored as dvdTokens, which should look like this:
        //_____________________________________________________________________________________
        //|The Car                 |1996|15|Frank Wilson  |Excellent|Saina Entertainment|
        //-------------------------------------------------------------------------------------
        //             [0]          [1]  [2]      [3]         [4]              [5]

        String[] dvdTokens = dvdAsText.split(DELIMITER);
        //Given the pattern above, the DVD title is in index 0 of the array.
        String title = dvdTokens[0];
        String releaseDate = dvdTokens[1];
        String mpaaRating = dvdTokens[2];
        String directorName = dvdTokens[3];
        String userRating = dvdTokens[4];
        String studio = dvdTokens[5];

        //A new DVD object is created using the title to satisfy the
        //requirements of the DVD constructor
        Dvd dvdFromFile = new Dvd(title);
        //The remaining tokens are then set into the DVD object using the appropriate setters.
        dvdFromFile.setReleaseDate(LocalDate.parse(releaseDate));
        dvdFromFile.setMpaaRating(mpaaRating);
        dvdFromFile.setDirectorName(directorName);
        dvdFromFile.setUserRating(userRating);
        dvdFromFile.setStudio(studio);
        return dvdFromFile;
    }


    //loadLibrary reads the LIBRARY_FILE into memory.


    private void loadLibrary() throws DvdLibraryDaoException {
        Scanner scanner;
        try {
            // Create Scanner for reading the file
            scanner = new Scanner(
                    new BufferedReader(
                            new FileReader(LIBRARY_FILE)));
        } catch (FileNotFoundException e) {
            throw new DvdLibraryDaoException(
                    "-_- Could not load roster data into memory.", e);
        }
        //currentLine holds the most recent line read from the file
        String currentLine;
        //curentDvd holds the most recent DVD unmarshalled
        Dvd currentDvd;
        //Go through LIBRARY_FILE line by line, decoding each line into a DVD
        //object by calling the unmarshallDvd method. Process while we have more
        //more lines in the file
        while (scanner.hasNextLine()) {
            //get the next line in the file
            currentLine = scanner.nextLine();
            //unmarshall the line into a DVD
            currentDvd = unmarshallDvd(currentLine);

            //The Dvd title is used as a map key to put the currentDvd ino the map
            dvds.put(currentDvd.getTitle(), currentDvd);
        }
        //Clean up
        scanner.close();
    }


    /**
     * writeLibrary writes all the DVDs in the DVD library out to a LIBRARY_FILE. The DVD objects in the DVD map
     * will be converted to text one by one, using the marshallDvd method, spaced by the delimiter. The string
     * will then be written to an output file.
     *
     * @throws ClassLibraryDaoException if an error occurs writing to the file
     */
    private void writeLibrary() throws DvdLibraryDaoException {
        // We are translating the IOException to an application specific exception
        //and then simple throwing it i.e. reporting it to the code that called us.

        PrintWriter out;

        try {
            out = new PrintWriter(new FileWriter(LIBRARY_FILE));
        } catch (IOException e) {
            throw new DvdLibraryDaoException("Could not save DVD data", e);
        }
        String dvdAsText;
        List<Dvd> dvdList = this.getAllDvds();
        for (Dvd currentDvd : dvdList) {
            //turn a DVD into a string
            dvdAsText = marshallDvd(currentDvd);
            //write the DVD object to to the file;
            out.println(dvdAsText);
            //force PrintWriter to write line to the file
            out.flush();
        }
        //Clean up
        out.close();
    }

}

