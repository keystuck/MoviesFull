package com.example.emily.moviesfull;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.PrimaryKey;
import android.os.Parcel;
import android.os.Parcelable;

@Entity(tableName = "movie")
public class Movie implements Parcelable{

    @PrimaryKey
    private long movieId;
    private String title;
    private String posterPath;
    private String summary;
    private double rating;
    private String releaseDate;

    @Ignore
    private Movie(Parcel in){
        movieId = in.readLong();
        title = in.readString();
        posterPath = in.readString();
        summary = in.readString();
        rating = in.readDouble();
        releaseDate = in.readString();
    }

    public Movie(long movieId, String title, String summary, String posterPath, double rating, String releaseDate){
        this.movieId = movieId;
        this.title = title;
        this.summary = summary;
        this.posterPath = posterPath;
        this.rating = rating;
        this.releaseDate = releaseDate;
    }

    public long getMovieId() {
        return movieId;
    }

    public void setMovieId(long movieId) {
        this.movieId = movieId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPosterPath() {
        return posterPath;
    }

    public void setPosterPath(String posterPath) {
        this.posterPath = posterPath;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }

    public static final Parcelable.Creator<Movie> CREATOR =
            new Parcelable.Creator<Movie>(){
        public Movie createFromParcel(Parcel in){ return new Movie(in); }
        public Movie[] newArray(int size){ return new Movie[size]; }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeLong(movieId);
        parcel.writeString(title);
        parcel.writeString(posterPath);
        parcel.writeString(summary);
        parcel.writeDouble(rating);
        parcel.writeString(releaseDate);
    }
}
