SELECT tracks.Name as TrackName, albums.Title as AlbumTitle, artists.Name as ArtistsName
FROM tracks
JOIN albums on tracks.AlbumId = albums.AlbumId
JOIN artists on albums.ArtistId = artists.ArtistId
WHERE tracks.Milliseconds >  1000 * 60 * 6
