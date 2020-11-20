package edu.montana.csci.csci440.model;

import edu.montana.csci.csci440.util.DB;
import redis.clients.jedis.Client;
import redis.clients.jedis.Jedis;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class Track extends Model {

    private Long trackId;
    private Long albumId;
    private Long mediaTypeId;
    private Long genreId;
    private String name;
    private String previousName;
    private Long milliseconds;
    private Long bytes;
    private BigDecimal unitPrice;
    private String artistName;
    private String albumName;

    public static final String REDIS_CACHE_KEY = "cs440-tracks-count-cache";

    public Track() {
        mediaTypeId = 1l;
        genreId = 1l;
        milliseconds  = 0l;
        bytes  = 0l;
        unitPrice = new BigDecimal("0");
    }

    private Track(ResultSet results) throws SQLException {
        name = results.getString("Name");
        milliseconds = results.getLong("Milliseconds");
        bytes = results.getLong("Bytes");
        unitPrice = results.getBigDecimal("UnitPrice");
        trackId = results.getLong("TrackId");
        albumId = results.getLong("AlbumId");
        mediaTypeId = results.getLong("MediaTypeId");
        genreId = results.getLong("GenreId");
        artistName = results.getString("arName");
        albumName = results.getString("alName");
    }

    public static Track find(long i) { //Altered for locally stored artistName and albumName for this model.
        try (Connection conn = DB.connect();
             PreparedStatement stmt = conn.prepareStatement("SELECT tracks.* ,artists.Name as arName,albums.Title as alName " +
                     "FROM tracks " +
                     "JOIN albums on tracks.AlbumId = albums.AlbumId " +
                     "JOIN artists on albums.ArtistId = artists.ArtistId " +
                     "WHERE TrackId=? ")) {
            stmt.setLong(1, i);
            ResultSet results = stmt.executeQuery();
            if (results.next()) {
                return new Track(results);
            } else {
                return null;
            }
        } catch (SQLException sqlException) {
            throw new RuntimeException(sqlException);
        }
    }

    public static Long count() {
        Jedis redisClient = new Jedis(); // use this class to access redis and create a cache
        String str = redisClient.get("cs440-tracks-count-cache");

        if(str == null) { //If count doesn't exist in cache. Put it in cache.
            try (Connection conn = DB.connect();
                 PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) as Count FROM tracks")) {
                ResultSet results = stmt.executeQuery();
                if (results.next()) {
                    Long count = results.getLong("Count");
                    str = count.toString();
                    redisClient.set(REDIS_CACHE_KEY, str);
                } else {
                    throw new IllegalStateException("Should find a count!");
                }
            } catch (SQLException sqlException) {
                throw new RuntimeException(sqlException);
            }
        }
        return Long.parseLong(str);
    }

    public Album getAlbum() {
        return Album.find(albumId);
    }

    public MediaType getMediaType() {
        return null;
    }
    public Genre getGenre() {
        return null;
    }
    public List<Playlist> getPlaylists(){return Playlist.forPlaylist(trackId);} // Implemented within the playlist class.


    public Long getTrackId() {
        return trackId;
    }

    public void setTrackId(Long trackId) {
        this.trackId = trackId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.previousName = this.name;
        this.name = name;

    }

    public Long getMilliseconds() {
        return milliseconds;
    }

    public void setMilliseconds(Long milliseconds) {
        this.milliseconds = milliseconds;
    }

    public Long getBytes() {
        return bytes;
    }

    public void setBytes(Long bytes) {
        this.bytes = bytes;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public Long getAlbumId() {
        return albumId;
    }

    public void setAlbumId(Long albumId) {
        this.albumId = albumId;
    }

    public void setAlbum(Album album) {
        albumId = album.getAlbumId();
    }

    public Long getMediaTypeId() {
        return mediaTypeId;
    }

    public void setMediaTypeId(Long mediaTypeId) {
        this.mediaTypeId = mediaTypeId;
    }

    public Long getGenreId() {
        return genreId;
    }

    public void setGenreId(Long genreId) {
        this.genreId = genreId;
    }

    public String getArtistName() { return artistName;}//Locally stored within model.

    public String getAlbumTitle() { return albumName;} //Locally stored within model.

    public static List<Track> advancedSearch(int page, int count,
                                             String search, Integer artistId, Integer albumId,
                                             Integer maxRuntime, Integer minRuntime) {
        LinkedList<Object> args = new LinkedList<>();

        String query = "SELECT tracks.* ,artists.Name as arName,albums.Title as alName " +
                "FROM tracks " +
                "JOIN albums on tracks.AlbumId = albums.AlbumId " +
                "JOIN artists on albums.ArtistId = artists.ArtistId " +
                "WHERE tracks.Name || albums.Title || artists.Name LIKE ?";
        args.add("%" + search + "%");

        // Conditionally include the query and argument
        if (artistId != null) {
            query += " AND artists.ArtistId=?";
            args.add(artistId);
        }
        if(albumId != null){
            query += " AND tracks.AlbumId=?";
            args.add(albumId);
        }
        if(maxRuntime != null){
            query += " AND tracks.Milliseconds/1000 < ?";
            args.add(maxRuntime);
        }
        if(minRuntime != null){
            query += " AND tracks.Milliseconds/1000 > ?";
            args.add(minRuntime);
        }

        query += " LIMIT ?";
        args.add(count);

        try (Connection conn = DB.connect();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            for (int i = 0; i < args.size(); i++) {
                Object arg = args.get(i);
                stmt.setObject(i + 1, arg);
            }
            ResultSet results = stmt.executeQuery();
            List<Track> resultList = new LinkedList<>();
            while (results.next()) {
                resultList.add(new Track(results));
            }
            return resultList;
        } catch (SQLException sqlException) {
            throw new RuntimeException(sqlException);
        }
    }

    public static List<Track> search(int page, int count, String orderBy, String search) {
        String query = "SELECT tracks.* ,artists.Name as arName,albums.Title as alName " +
                "FROM tracks " +
                "JOIN albums on tracks.AlbumId = albums.AlbumId " +
                "JOIN artists on albums.ArtistId = artists.ArtistId " +
                "WHERE tracks.Name || albums.Title || artists.Name LIKE ? LIMIT ?";

        search = "%" + search + "%";
        try (Connection conn = DB.connect();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, search);
            stmt.setInt(2, count);
            ResultSet results = stmt.executeQuery();
            List<Track> resultList = new LinkedList<>();
            while (results.next()) {
                resultList.add(new Track(results));
            }
            return resultList;
        } catch (SQLException sqlException) {
            throw new RuntimeException(sqlException);
        }
    }

    public static List<Track> forAlbum(Long albumId) {
        String query = "SELECT tracks.* ,artists.Name as arName,albums.Title as alName " +
                "FROM tracks " +
                "JOIN albums on tracks.AlbumId = albums.AlbumId " +
                "JOIN artists on albums.ArtistId = artists.ArtistId " +
                "WHERE tracks.AlbumId=?";
        try (Connection conn = DB.connect();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setLong(1, albumId);
            ResultSet results = stmt.executeQuery();
            List<Track> resultList = new LinkedList<>();
            while (results.next()) {
                resultList.add(new Track(results));
            }
            return resultList;
        } catch (SQLException sqlException) {
            throw new RuntimeException(sqlException);
        }
    }

    public static List<Track> forPlaylist(Long playListId){
        String query = "SELECT tracks.*,artists.Name as arName,albums.Title as alName " +
                "FROM playlists " +
                "JOIN playlist_track on playlists.PlaylistId = playlist_track.PlaylistId " +
                "JOIN tracks on playlist_track.TrackId = tracks.TrackId " +
                "JOIN albums on tracks.AlbumId = albums.AlbumId " +
                "JOIN artists on albums.ArtistId = artists.ArtistId " +
                "WHERE playlists.PlaylistId=?" +
                "ORDER BY tracks.Name";
        try (Connection conn = DB.connect();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setLong(1, playListId);
            ResultSet results = stmt.executeQuery();
            List<Track> resultList = new LinkedList<>();
            while (results.next()) {
                resultList.add(new Track(results));
            }
            return resultList;
        } catch (SQLException sqlException) {
            throw new RuntimeException(sqlException);
        }
    }

    // Sure would be nice if java supported default parameter values
    public static List<Track> all() {
        return all(0, Integer.MAX_VALUE);
    }

    public static List<Track> all(int page, int count) {
        return all(page, count, "TrackId");
    }

    public static List<Track> all(int page, int count, String orderBy) {
        try (Connection conn = DB.connect();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT tracks.* ,artists.Name as arName,albums.Title as alName " +
                             "FROM tracks " +
                             "JOIN albums on tracks.AlbumId = albums.AlbumId " +
                             "JOIN artists on albums.ArtistId = artists.ArtistId " +
                             "ORDER BY " + orderBy + " LIMIT ? OFFSET ?"
             )) {
            stmt.setInt(1, count);
            stmt.setInt(2, count * (page-1));
            ResultSet results = stmt.executeQuery();
            List<Track> resultList = new LinkedList<>();
            while (results.next()) {
                resultList.add(new Track(results));
            }
            return resultList;
        } catch (SQLException sqlException) {
            throw new RuntimeException(sqlException);
        }
    }
    public boolean create(){
        try (Connection conn = DB.connect()){
            PreparedStatement stmt = conn.prepareStatement(
                    "INSERT INTO tracks(Name, AlbumId, MediaTypeId,GenreId,Milliseconds,Bytes,UnitPrice)" +
                            "VALUES (?,?,?,?,?,?,?)"
            );
            stmt.setString(1, this.name);
            stmt.setLong(2, this.albumId);
            stmt.setLong(3, this.mediaTypeId);
            stmt.setLong(4, this.genreId);
            stmt.setLong(5, this.milliseconds);
            stmt.setLong(6, this.bytes);
            stmt.setBigDecimal(7, this.unitPrice);
            stmt.execute();
            this.trackId = DB.getLastID(conn);
            Jedis redisClient = new Jedis(); // Need to invalidate cache if we add a track.
            redisClient.del(REDIS_CACHE_KEY);
            return true;
        }catch (SQLException sqlException){
            throw new RuntimeException(sqlException);
        }
    }

    public boolean update(){
        if(verify()) {
            try (Connection conn = DB.connect();
                 PreparedStatement stmt = conn.prepareStatement(
                         "UPDATE tracks SET Name=?, Milliseconds=?, Bytes=?, UnitPrice=?, AlbumId=?, MediaTypeId=?," +
                                 " GenreId=?  WHERE TrackId=? and Name=?")) { // Simple OC implementation.
                stmt.setString(1, this.name);
                stmt.setLong(2, this.milliseconds);
                stmt.setLong(3, this.bytes);
                stmt.setBigDecimal(4,this.unitPrice);
                stmt.setLong(5, this.albumId);
                stmt.setLong(6, this.mediaTypeId);
                stmt.setLong(7, this.genreId);
                stmt.setLong(8, this.trackId);
                stmt.setString(9, this.previousName); //OC implementation.

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
                     "DELETE FROM tracks WHERE TrackId=?")) {
            stmt.setLong(1, this.trackId);
            stmt.executeUpdate();
            Jedis redisClient = new Jedis(); // Need to invalidate cache if we delete a track.
            redisClient.del(REDIS_CACHE_KEY);
        } catch (SQLException sqlException) {
            throw new RuntimeException(sqlException);
        }
    }

    public boolean verify() {
        _errors.clear(); // clear any existing errors
        if (name == null || "".equals(name)) {
            addError("Name can't be null or blank!");
        }
        if(albumId == null || albumId <= 0){
            addError("AlbumId can not be null or negative!");
        }

        return !hasErrors();
    }



}
