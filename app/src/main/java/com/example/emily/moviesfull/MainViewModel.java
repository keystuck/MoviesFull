package com.example.emily.moviesfull;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;

import com.example.emily.moviesfull.database.FavoriteDB;

import java.util.List;

public class MainViewModel extends AndroidViewModel {

    private LiveData<List<Movie>> movieList;
    private String sortMethod;

    public MainViewModel(@NonNull Application application) {
        super(application);
        FavoriteDB favoriteDB = FavoriteDB.getsInstance(this.getApplication());
        movieList = favoriteDB.favoriteDao().loadAllMovies();
    }

    public LiveData<List<Movie>> getMovieList() {
        return movieList;
    }

    public String getSortMethod() {
        return sortMethod;
    }

    public void setSortMethod(String sortMethod) {
        this.sortMethod = sortMethod;
    }
}
