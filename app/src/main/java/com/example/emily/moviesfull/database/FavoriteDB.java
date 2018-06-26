package com.example.emily.moviesfull.database;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

import com.example.emily.moviesfull.Movie;

@Database(entities = {Movie.class}, version = 1, exportSchema = false)
public abstract class FavoriteDB extends RoomDatabase {

    private static final String LOG_TAG = FavoriteDB.class.getSimpleName();
    private static final Object LOCK = new Object();
    private static final String DATABASE_NAME = "favorite_movies_db";
    private static FavoriteDB sInstance;

    public static FavoriteDB getsInstance(Context context){
        if (sInstance == null){
            synchronized (LOCK){
                sInstance = Room.databaseBuilder(context.getApplicationContext(),
                        FavoriteDB.class,
                        FavoriteDB.DATABASE_NAME)
                        .build();
            }
        }
        return sInstance;
    }

    public abstract FavoriteDao favoriteDao();

}
