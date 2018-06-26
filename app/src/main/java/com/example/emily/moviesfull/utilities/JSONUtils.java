package com.example.emily.moviesfull.utilities;

import android.util.Log;

import com.example.emily.moviesfull.Movie;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class JSONUtils {

    private static final String LOG_TAG = JSONUtils.class.getSimpleName();

    private static final String NUM_RESULTS = "total_results";
    private static final String RESULTS = "results";
    private static final String VIDEO_KEY = "key";
    private static final String VIDEO_NAME = "name";
    private static final String REVIEW_AUTHOR = "author";
    private static final String REVIEW_TEXT = "content";

    private static final String ID = "id";
    private static final String TITLE = "title";
    private static final String VOTE_AVG = "vote_average";
    private static final String POSTER_PATH = "poster_path";
    private static final String RELEASE_DATE = "release_date";
    private static final String SUMMARY = "overview";

    private static final String POSTER_PREFACE = "http://image.tmdb.org/t/p/w185//";
    private static final String VIDEO_PREFACE = "https://www.youtube.com/watch?v=";

    public static ArrayList<Movie> getMoviesFromJSON(String JSONInputString) throws JSONException{
        if (JSONInputString.isEmpty()){
            return null;
        }
        JSONObject inputAsJSON = new JSONObject(JSONInputString);
        int howManyMovies = inputAsJSON.getInt(NUM_RESULTS);

        if (howManyMovies == 0){
            return null;
        }

        JSONArray inputArray = inputAsJSON.getJSONArray(RESULTS);
        ArrayList<Movie> moviesList = new ArrayList<>();

        for (int i = 0; i < inputArray.length(); i++){
            long movieId;
            String title;
            String posterPath;
            String synopsis;
            double rating;
            String releaseDate;

            JSONObject currentMovieJSON = (JSONObject)inputArray.get(i);

            movieId = currentMovieJSON.getLong(ID);
            title = currentMovieJSON.getString(TITLE);
            posterPath = POSTER_PREFACE + currentMovieJSON.getString(POSTER_PATH);
            synopsis = currentMovieJSON.getString(SUMMARY);
            rating = currentMovieJSON.getDouble(VOTE_AVG);
            releaseDate = currentMovieJSON.getString(RELEASE_DATE);

            moviesList.add(new Movie(movieId, title, synopsis, posterPath, rating, releaseDate));
        }
        return moviesList;
    }

    public static ArrayList<String> getVideosFromJSON(String JSONInputString) throws JSONException {
        if (JSONInputString.isEmpty()){
            return null;
        }
        ArrayList<String> videoList = new ArrayList<String>();

        JSONObject inputAsJSON = new JSONObject(JSONInputString);
        JSONArray inputArray = inputAsJSON.getJSONArray(RESULTS);

        for (int i = 0; i < inputArray.length(); i++){
            String key;
            String name;

            JSONObject currentVideoJSON = (JSONObject)inputArray.get(i);

            key = currentVideoJSON.getString(VIDEO_KEY);

            name = currentVideoJSON.getString(VIDEO_NAME);

            videoList.add(VIDEO_PREFACE + key + "TITLE:" + name);

        }
        return videoList;
    }

    public static ArrayList<String> getReviewsFromJSON(String JSONInputString) throws JSONException {
        if (JSONInputString.isEmpty()){
            return null;
        }
        ArrayList<String> reviewList = new ArrayList<String>();

        JSONObject inputAsJSON = new JSONObject(JSONInputString);
        JSONArray inputArray = inputAsJSON.getJSONArray(RESULTS);

        for (int i = 0; i < inputArray.length(); i++){
            JSONObject currentReviewJSON = inputArray.getJSONObject(i);

            String author = currentReviewJSON.getString(REVIEW_AUTHOR);
            String content = currentReviewJSON.getString(REVIEW_TEXT);
            reviewList.add(author);
            reviewList.add(content);
        }
        return reviewList;
        }
    }
