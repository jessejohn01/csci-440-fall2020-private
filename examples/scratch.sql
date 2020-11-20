SELECT tracks.* ,artists.Name as arName,albums.Title as alName FROM tracks
JOIN albums on tracks.AlbumId = albums.AlbumId
JOIN artists on albums.ArtistId = artists.ArtistId;

SELECT tracks.* ,artists.Name as arName,albums.Title as alName
                             FROM tracks
                             JOIN albums on tracks.AlbumId = albums.AlbumId
                             JOIN artists on albums.ArtistId = artists.ArtistId
                             ORDER BY "Milliseconds"; LIMIT 1 OFFSET 0;