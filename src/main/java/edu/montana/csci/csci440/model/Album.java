package edu.montana.csci.csci440.model;

import edu.montana.csci.csci440.util.DB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class Album extends Model {

    Long albumId;
    Long artistId;
    String title;
    String previousTitle;

    public Album() {
    }

    private Album(ResultSet results) throws SQLException {
        title = results.getString("Title");
        albumId = results.getLong("AlbumId");
        artistId = results.getLong("ArtistId");
    }

    public Artist getArtist() {
        return Artist.find(artistId);
    }

    public void setArtist(Artist artist) {
        artistId = artist.getArtistId();
    }

    public List<Track> getTracks() {
        return Track.forAlbum(albumId);
    }

    public Long getAlbumId() {
        return albumId;
    }

    public void setAlbum(Album album) { this.albumId = album.getAlbumId(); }

    public String getTitle() {
        return title;
    }

    public void setTitle(String name) {
        this.previousTitle = this.title;
        this.title = name;
    }

    public Long getArtistId() {
        return artistId;
    }

    public void setArtistId(Long artistId) { this.artistId = artistId;}

    public static List<Album> all() {
        return all(0, Integer.MAX_VALUE);
    }

    public static List<Album> all(int page, int count) {
        try (Connection conn = DB.connect();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT * FROM albums ORDER BY AlbumId LIMIT ? OFFSET ?"
             )) {
            stmt.setInt(1, count);
            stmt.setInt(2, count*(page-1));
            ResultSet results = stmt.executeQuery();
            List<Album> resultList = new LinkedList<>();
            while (results.next()) {
                resultList.add(new Album(results));
            }
            return resultList;
        } catch (SQLException sqlException) {
            throw new RuntimeException(sqlException);
        }
    }

    public boolean create(){
        try (Connection conn = DB.connect()){
            PreparedStatement stmt = conn.prepareStatement(
                    "INSERT INTO albums(title, artistid)" +
                            "VALUES (?,?)"
            );
            stmt.setString(1, this.title);
            stmt.setLong(2, this.artistId);
            stmt.execute();
            this.albumId = DB.getLastID(conn);
            return true;
        }catch (SQLException sqlException){
            throw new RuntimeException(sqlException);
        }
    }

    public boolean update(){
        if(verify()) {
            try (Connection conn = DB.connect();
                 PreparedStatement stmt = conn.prepareStatement(
                         "UPDATE albums SET Title=?, ArtistId=? WHERE AlbumId=? and Title=?")) { // Simple OC implementation.
                stmt.setString(1, this.getTitle());
                stmt.setLong(2, this.getArtistId());
                stmt.setLong(3, this.getAlbumId());
                stmt.setString(4, this.previousTitle);
                int rowsUpdated = stmt.executeUpdate();
                if (rowsUpdated == 0) {
                    System.out.println("The update failed. Please try again.");
                    return false;
                }

                return true;
            } catch (SQLException sqlException) {
                throw new RuntimeException(sqlException);
            }
        }else{
            return false;
        }
    }

    public void delete() {
        try (Connection conn = DB.connect();
             PreparedStatement stmt = conn.prepareStatement(
                     "DELETE FROM albums WHERE AlbumId=?")) {
            stmt.setLong(1, this.getAlbumId());
            stmt.executeUpdate();
        } catch (SQLException sqlException) {
            throw new RuntimeException(sqlException);
        }
    }

    public boolean verify() {
        _errors.clear(); // clear any existing errors
        if (title == null || "".equals(title)) {
            addError("Title can't be null or blank!");
        }
        if(artistId == null || artistId <= 0){
            addError("Artist can not be null or negative!");
        }

        return !hasErrors();
    }


    public static Album find(long i) {
        try (Connection conn = DB.connect();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM albums WHERE AlbumId=?")) {
            stmt.setLong(1, i);
            ResultSet results = stmt.executeQuery();
            if (results.next()) {
                return new Album(results);
            } else {
                return null;
            }
        } catch (SQLException sqlException) {
            throw new RuntimeException(sqlException);
        }
    }

    public static List<Album> getForArtist(Long artistId) {

        try (Connection conn = DB.connect();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM albums WHERE ArtistId=?")) {
            stmt.setLong(1, artistId);
            ResultSet results = stmt.executeQuery();
            List<Album> resultList = new LinkedList<>();
            while (results.next()) {
                resultList.add(new Album(results));
            }
            return resultList;
        } catch (SQLException sqlException) {
            throw new RuntimeException(sqlException);
        }

    }

}
