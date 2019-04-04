/*
 * XMLforMovies
 *
 * A class for objects that are able to convert movie data from the
 * relational database used in PS 1 to XML.
 *
 * Before compiling this program, you must download the JAR file for the
 * SQLite JDBC Driver and add it to your classpath. See the JDBC-specific
 * notes in the assignment for more details.
 */

import java.util.*;     // needed for the Scanner class
import java.sql.*;      // needed for the JDBC-related classes
import java.io.*;       // needed for the PrintStream class

public class XMLforMovies {
    private Connection db;   // a connection to the database

    /*
     * XMLforMovies constructor - takes the name of a SQLite file containing
     * a Movie table like the one from PS 1, and creates an object that
     * can be used to convert the data in that table to XML.
     *
     * ** YOU SHOULD NOT CHANGE THIS METHOD **
     */
    public XMLforMovies(String dbFilename) throws SQLException {
        this.db = DriverManager.getConnection("jdbc:sqlite:" + dbFilename);
    }

    /*
     * idFor - takes the name of a movie and returns the id number of
     * that movie in the database as a string. If the movie is not in the
     * database, it returns an empty string.
     *
     * ** YOU SHOULD NOT CHANGE THIS METHOD **
     */
    public String idFor(String name) throws SQLException {
        String query = "SELECT id FROM Movie WHERE name = '" + name + "';";
        Statement stmt = this.db.createStatement();
        ResultSet results = stmt.executeQuery(query);

        if (results.next()) {
            String id = results.getString(1);
            return id;
        } else {
            return "";
        }
    }

    /*
     * simpleElem - takes the name and value of an XML element and
     * returns a string representation of that XML element
     */
    public String simpleElem(String name, String value) {

        // replace this return statement with your implementation of the method
        if (value == null) {
            return "";
        }
        else {
            return "    <" + name + ">" + value + "</" + name + ">" + "\n";
        }
    }

    /*
     * fieldsFor - takes a string representing the id number of a movie
     * and returns a sequence of XML elements for the non-null field values
     * of that movie in the database. If there is no movie with the specified
     * id number, the method returns an empty string.
     */
    public String fieldsFor(String movieID) throws SQLException {

        String query = "SELECT * FROM Movie WHERE id = '" + movieID + "';";
        Statement stmt = this.db.createStatement();
        ResultSet results = stmt.executeQuery(query);

        if (results.next()) {
            String id = results.getString(1);
            String name = results.getString(2);
            String year = results.getString(3);
            String rating = results.getString(4);
            String runtime = results.getString(5);
            String genre = results.getString(6);
            String earnings_rank = results.getString(7);

            return  simpleElem("name", name) +
                    simpleElem("year", year) +
                    simpleElem("rating", rating) +
                    simpleElem("runtime", runtime) +
                    simpleElem("genre", genre) +
                    simpleElem("earnings_rank", earnings_rank)
                    ;
        } else {
            return "";
        }

    }

    /*
     * actorsFor - takes a string representing the id number of a movie
     * and returns a single complex XML element named "actors" that contains a
     * nested child element named "actor" for each actor associated with that
     * movie in the database. If there is no movie with the specified
     * id number, the method returns an empty string.
     */
    public String actorsFor(String movieID) throws SQLException {

        String query = "SELECT P.name\n" +
                "FROM Person P, Actor A\n" +
                "WHERE A.movie_id = '" + movieID + "'\n" +
                "AND P.id = A.actor_id\n" +
                "ORDER BY P.name;";
        Statement stmt = this.db.createStatement();
        ResultSet results = stmt.executeQuery(query);

        String userWords = "    <actors>" + "\n";

        if (results.next()) {
            userWords += "  " + simpleElem("actor", results.getString(1));
            while (results.next()) {
                userWords += "  " + simpleElem("actor", results.getString(1));
            }
            userWords += "    </actors>" + "\n";
            return userWords;
        }
        else {
            return "";
        }

    }

    /*
     * directorsFor - takes a string representing the id number of a movie
     * and returns a single complex XML element named "directors" that contains a
     * nested child element named "director" for each director associated with
     * that movie in the database. If there is no movie with the specified
     * id number, the method returns an empty string.
     */
    public String directorsFor(String movieID) throws SQLException {

        String query = "SELECT P.name\n" +
                "FROM Person P, Director D\n" +
                "WHERE D.movie_id = '" + movieID + "'\n" +
                "AND P.id = D.director_id\n" +
                "ORDER BY P.name;";
        Statement stmt = this.db.createStatement();
        ResultSet results = stmt.executeQuery(query);

        String userWords = "    <directors>" + "\n";

        if (results.next()) {
            userWords += "  " + simpleElem("director", results.getString(1));
            while (results.next()) {
                userWords += "  " + simpleElem("director", results.getString(1));
            }
            userWords += "    </directors>" + "\n";
            return userWords;
        }
        else {
            return "";
        }

    }

    /*
     * elementFor - takes a string representing the id number of a movie
     * and returns a single complex XML element named "movie" that contains
     * nested child elements for all of the fields, actors, and directors
     * associated with  that movie in the database. If there is no movie with
     * the specified id number, the method returns an empty string.
     */
    public String elementFor(String movieID) throws SQLException {

        String query = "SELECT * FROM Movie WHERE id = '" + movieID + "';";
        Statement stmt = this.db.createStatement();
        ResultSet results = stmt.executeQuery(query);

        if (results.next()) {
            return "  <movie id=\"" + movieID + "\">\n" + fieldsFor(movieID) + actorsFor(movieID) + directorsFor(movieID) + "  </movie>";
        }
        else {
            return "";
        }

    }
    /*
     * createFile - creates a text file with the specified filename containing
     * an XML representation of the entire Movie table.
     *
     * ** YOU SHOULD NOT CHANGE THIS METHOD **
     */
    public void createFile(String filename)
            throws FileNotFoundException, SQLException
    {
        PrintStream outfile = new PrintStream(filename);
        outfile.println("<?xml version=\"1.0\" encoding=\"iso-8859-1\"?>");
        outfile.println("<Movies>");

        // Use a query to get all of the ids from the Movie Table.
        Statement stmt = this.db.createStatement();
        ResultSet results = stmt.executeQuery("SELECT id FROM Movie;");

        // Process one movie id at a time, creating its 
        // XML element and writing it to the output file.
        while (results.next()) {
            String movieID = results.getString(1);
            outfile.println(elementFor(movieID));
        }

        outfile.println("</Movies>");

        // Close the connection to the output file.
        outfile.close();
        System.out.println("movies.xml has been written.");
    }

    /*
     * closeDB - closes the connection to the database that was opened when
     * the XMLforMovies object was constructed
     *
     * ** YOU SHOULD NOT CHANGE THIS METHOD **
     */
    public void closeDB() throws SQLException {
        this.db.close();
    }

    public static void main(String[] args)
            throws ClassNotFoundException, SQLException, FileNotFoundException
    {
        // Get the name of the SQLite database file from the user.
        Scanner console = new Scanner(System.in);
        System.out.print("Enter the name of the database file: ");
        String dbFilename = console.next();

        // Create an XMLforMovies object for the SQLite database, and
        // convert the entire database into an XML file.
        XMLforMovies xml = new XMLforMovies(dbFilename);
        xml.createFile("movies.xml");
        // System.out.print(xml.elementFor("1234567"));
        xml.closeDB();
    }
}