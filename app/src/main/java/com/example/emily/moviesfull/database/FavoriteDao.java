package com.example.emily.moviesfull.database;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import com.example.emily.moviesfull.Movie;

import java.util.List;

@Dao
public interface FavoriteDao {

    @Query("SELECT * FROM movie ORDER BY movieId")
    LiveData<List<Movie>> loadAllMovies();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void addFavorite(Movie movie);

    @Delete
    int deleteFavorite(Movie movie);

    @Query("SELECT * FROM movie WHERE movieId = :movieId")
    LiveData<Movie> loadFavoriteMovie(long movieId);

    @Query("SELECT * FROM movie ORDER BY title")
    List<Movie> allMovies();

    @Query("SELECT * FROM movie WHERE movieId = :movieId")
    Movie loadFaveById(long movieId);

}
