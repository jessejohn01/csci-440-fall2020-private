package edu.montana.csci.csci440.homework;

import edu.montana.csci.csci440.DBTest;
import edu.montana.csci.csci440.model.Track;
import edu.montana.csci.csci440.util.DB;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Homework4 extends DBTest {

    @Test
    /*
     * Use a transaction to safely move milliseconds from one track to anotherls
     *
     * You will need to use the JDBC transaction API, outlined here:
     *
     *   https://docs.oracle.com/javase/tutorial/jdbc/basics/transactions.html
     *
     */
    public void useATransactionToSafelyMoveMillisecondsFromOneTrackToAnother() throws SQLException {

        Track track1 = Track.find(1);
        Long track1InitialTime = track1.getMilliseconds();
        Track track2 = Track.find(2);
        Long track2InitialTime = track2.getMilliseconds();

        try(Connection connection = DB.connect()){
            connection.setAutoCommit(false); // Start of our transaction.
            PreparedStatement subtract = connection.prepareStatement("UPDATE tracks\n" +
                    "SET Milliseconds = ?\n" +
                    "WHERE TrackId = ?;");
            subtract.setLong(1, track1.getMilliseconds() - 10);
            subtract.setLong(2, track1.getTrackId());
            subtract.execute();

            PreparedStatement add = connection.prepareStatement("UPDATE tracks\n" +
                    "SET Milliseconds = ?\n" +
                    "Where TrackId = ?;");
            add.setLong(1, track2.getMilliseconds() + 10);
            add.setLong(2, track2.getTrackId());
            add.execute();

            connection.commit(); //Commit all or nothing.

            // commit with the connection
        }

        // refresh tracks from db
        track1 = Track.find(1);
        track2 = Track.find(2);
        assertEquals(track1.getMilliseconds(), track1InitialTime - 10);
        assertEquals(track2.getMilliseconds(), track2InitialTime + 10);
    }

    @Test
    /*
     * Select tracks that have been sold more than once (> 1)
     *
     * Select the albumbs that have tracks that have been sold more than once (> 1)
     *   NOTE: This is NOT the same as albums whose tracks have been sold more than once!
     *         An album could have had three tracks, each sold once, and should not be included
     *         in this result.  It should only include the albums of the tracks found in the first
     *         query.
     * */
    public void selectPopularTracksAndTheirAlbums() throws SQLException {

        // HINT: join to invoice items and do a group by/having to get the right answer
        List<Map<String, Object>> tracks = executeSQL("SELECT tracks.TrackId, tracks.Name from tracks\n" +
                "inner join invoice_items ii on tracks.TrackId = ii.TrackId\n" +
                "group by tracks.TrackId\n" +
                "having count(tracks.TrackId) > 1;");
        assertEquals(256, tracks.size());

        // HINT: join to tracks and invoice items and do a group by/having to get the right answer
        //       note: you will need to use the DISTINCT operator to get the right result!
        List<Map<String, Object>> albums = executeSQL("SELECT DISTINCT a.Title from tracks\n" +
                "inner join invoice_items ii on tracks.TrackId = ii.TrackId\n" +
                "inner join albums a on tracks.AlbumId = a.AlbumId\n" +
                "group by tracks.TrackId\n" +
                "having count(tracks.TrackId) > 1;");
        assertEquals(166, albums.size());
    }

    @Test
    /*
     * Select customers emails who are assigned to Jane Peacock as a Rep and
     * who have purchased something from the 'Rock' Genre
     *
     * Please use an IN clause and a sub-select to generate customer IDs satisfying the criteria
     * */
    public void selectCustomersMeetingCriteria() throws SQLException {
        // HINT: join to invoice items and do a group by/having to get the right answer
        List<Map<String, Object>> tracks = executeSQL("SELECT customers.Email from customers\n" +
                "inner join employees e on customers.SupportRepId = e.EmployeeId\n" +
                "inner join invoices i on customers.CustomerId = i.CustomerId\n" +
                "inner join invoice_items ii on i.InvoiceId = ii.InvoiceId\n" +
                "WHERE e.FirstName = 'Jane' AND e.LastName = 'Peacock'\n" +
                "group by customers.CustomerId having customers.CustomerId in (\n" +
                "    SELECT CustomerId from invoice_items\n" +
                "        inner join invoices i on invoice_items.InvoiceId = i.InvoiceId\n" +
                "        inner join tracks t on invoice_items.TrackId = t.TrackId\n" +
                "        inner join genres g on t.GenreId = g.GenreId\n" +
                "    WHERE g.Name = 'Rock')" );
        assertEquals(21, tracks.size());
    }


}
