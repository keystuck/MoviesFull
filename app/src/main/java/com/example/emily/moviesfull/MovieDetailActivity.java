package com.example.emily.moviesfull;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.example.emily.moviesfull.database.FavoriteDB;

import com.example.emily.moviesfull.utilities.JSONUtils;
import com.example.emily.moviesfull.utilities.NetworkUtilities;
import com.squareup.picasso.Picasso;
import com.example.emily.moviesfull.utilities.AppExecutors;

import org.json.JSONException;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.URL;
import java.util.ArrayList;

public class MovieDetailActivity extends AppCompatActivity {

    private FavoriteDB mDb;

    public static final String MOVIE_EXTRA = "movie";

    private Movie currentMovie;
    private final static String LOG_TAG = MovieDetailActivity.class.getSimpleName();
    private final static String ICON_STATE = "icon_state";
    private final static String SUCCESS = "success";
    private final static String FAILURE = "failure";
    private final static String VIDEOS = "videos";
    private final static String REVIEWS = "reviews";

    ImageView bigPosterImageView;
    TextView titleView;
    TextView summaryView;
    TextView voteAvgView;
    TextView releaseDateView;
    ImageButton faveButton;
    ConstraintLayout extrasLayout;
    RecyclerView mRecyclerView;
    RecyclerView.LayoutManager mLayoutManager;
    RecyclerView.Adapter mAdapter;

    private String videosJSON;
    private String reviewsJSON;
    ArrayList<String> videosKeyAndTitleList = new ArrayList<>();
    ArrayList<String> reviewsAuthorAndContentList = new ArrayList<>();

    TextView videoText;
    TextView reviewText;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        mDb = FavoriteDB.getsInstance(this);

        bigPosterImageView = findViewById(R.id.iv_big_poster);
        titleView = findViewById(R.id.tv_title);
        summaryView = findViewById(R.id.tv_summary);
        voteAvgView = findViewById(R.id.tv_vote_avg);
        releaseDateView = findViewById(R.id.tv_release_date);
        faveButton = findViewById(R.id.btn_fave);

        extrasLayout = findViewById(R.id.layout_extras);

        reviewText = findViewById(R.id.tv_reviews);
        videoText = findViewById(R.id.tv_video_message_text);



        Intent intent = getIntent();
        if (intent == null || !intent.hasExtra(MOVIE_EXTRA)){
            finish();
            Toast.makeText(this, "No movie info", Toast.LENGTH_SHORT).show();
        }
        currentMovie = intent.getParcelableExtra(MOVIE_EXTRA);
        if (currentMovie == null){
            finish();
            Toast.makeText(this, "problem obtanining movie info", Toast.LENGTH_SHORT).show();
            currentMovie = new Movie(0, "Problem", "", "", 0.0, "");
        }
        populateUI(savedInstanceState);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(ICON_STATE, (String) faveButton.getTag());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState.containsKey(ICON_STATE)) {
            faveButton.setImageResource(
                    savedInstanceState.getString(ICON_STATE).equals("0") ?
                            R.drawable.ic_not_favorite :
                            R.drawable.ic_favorite
            );
            faveButton.setTag(savedInstanceState.getString(ICON_STATE));
        }
    }

    public void populateUI(Bundle savedInstanceState){

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int screenHeight = displayMetrics.heightPixels;
        int screenWidth = displayMetrics.widthPixels;
        bigPosterImageView.getLayoutParams().height = screenHeight/3;
        bigPosterImageView.getLayoutParams().width = screenWidth;



        if (!currentMovie.getPosterPath().isEmpty()){
            Picasso.with(this).load(currentMovie.getPosterPath())
                    .resize(screenWidth, screenHeight/3)
                    .centerCrop()
                    .into(bigPosterImageView);
        }
        else {
            bigPosterImageView.setImageResource(R.drawable.no_image);
        }
        titleView.setText(currentMovie.getTitle());
        summaryView.setText(currentMovie.getSummary());
        String rating = currentMovie.getRating() + getResources().getString(R.string.out_of_stars);
        voteAvgView.setText(rating);
        releaseDateView.setText(currentMovie.getReleaseDate());

        //preserve favorite/not favorite indicator
        if (savedInstanceState == null) {
            final LiveData<Movie> faveMovie = mDb.favoriteDao().loadFavoriteMovie(currentMovie.getMovieId());
            faveMovie.observe(this, new Observer<Movie>() {
                @Override
                public void onChanged(@Nullable Movie movie) {
                    if (movie == null) {
                        faveButton.setImageResource(R.drawable.ic_not_favorite);
                        faveButton.setTag("0");
                    } else {
                        faveButton.setImageResource(R.drawable.ic_favorite);
                        faveButton.setTag("1");
                    }

                    faveMovie.removeObserver(this);
                }
            });
        }

        //retrieve videos and reviews from Internet
        new GetExtrasTask().execute();


        mRecyclerView = findViewById(R.id.video_recycler);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

    }

    //if not favorite, add to favorites db
    //if favorite, delete from favorites db
    public void toggleFavorite(View view){
        final LiveData<Movie> faveMovie = mDb.favoriteDao().loadFavoriteMovie(currentMovie.getMovieId());
        final boolean faveMovieWasFave = (faveButton.getTag().equals("1"));
        faveMovie.observe(this, new Observer<Movie>() {
            @Override
            public void onChanged(@Nullable Movie movie) {
                faveMovie.removeObserver(this);
              if (!faveMovieWasFave){
                    faveButton.setImageResource(R.drawable.ic_favorite);
                    faveButton.setTag("1");
                } else {
                    faveButton.setImageResource(R.drawable.ic_not_favorite);
                    faveButton.setTag("0");
                }
             }
        });

        //add/delete movie to/from database
        AppExecutors.getInstance().getDiskIO().execute(new Runnable() {
            @Override
            public void run() {
                if (!faveMovieWasFave){
                    mDb.favoriteDao().addFavorite(currentMovie);

                } else {
                    int deleted = mDb.favoriteDao().deleteFavorite(currentMovie);
                }
            }
        });

    }

    //show message when no Internet connection is available
    public void showExtrasErrorMessage(){
        videoText.setText(getResources().getString(R.string.no_connection_extras));
    }

    //AsyncTask to retrieve videos and reviews
    public class GetExtrasTask extends AsyncTask<Void, Void, String>{
        @Override
        protected String doInBackground(Void... voids) {

                URL videoRequestUrl = NetworkUtilities.buildExtrasURL(currentMovie.getMovieId(), VIDEOS);
                URL reviewRequestUrl = NetworkUtilities.buildExtrasURL(currentMovie.getMovieId(), REVIEWS);
                try {
                    // Code in this try block is adapted from Levit:
                    // https://stackoverflow.com/a/27312494
                    int timeout = 1500;
                    Socket socket = new Socket();
                    SocketAddress socketAddress = new InetSocketAddress("8.8.8.8", 53);
                    socket.connect(socketAddress, timeout);
                    socket.close();
                } catch (IOException e){
                    e.printStackTrace();
                    return FAILURE;
                }

                try {
                    videosJSON = NetworkUtilities.getResponseFromHttpUrl(videoRequestUrl);
                    reviewsJSON = NetworkUtilities.getResponseFromHttpUrl(reviewRequestUrl);

                    return SUCCESS;
                } catch (Exception e){
                    e.printStackTrace();
                    return FAILURE;
                }
        }

        @Override
        protected void onPostExecute(String s) {
            if (s.equals(FAILURE)){
                showExtrasErrorMessage();
            } else {
                try {
                    videosKeyAndTitleList =
                            JSONUtils.getVideosFromJSON(videosJSON);

                    reviewsAuthorAndContentList =
                            JSONUtils.getReviewsFromJSON(reviewsJSON);

                    if (videosKeyAndTitleList == null
                            || videosKeyAndTitleList.size() < 2){
                        videoText.setText(getResources().getString(R.string.no_videos));
                    } else {
                        mAdapter = new VideosAdapter(videosKeyAndTitleList, getApplicationContext());
                        mRecyclerView.setAdapter(mAdapter);
                    }

                        if (reviewsAuthorAndContentList == null
                                || reviewsAuthorAndContentList.size() < 2) {
                            reviewText.setText(getResources().getString(R.string.no_reviews));
                        } else {
                            String reviewPreface = getResources().getString(R.string.review_text);
                            StringBuilder reviewTextString = new StringBuilder();

                            for (int i = 0; i < reviewsAuthorAndContentList.size() - 1; i += 2) {
                                reviewTextString.append(reviewPreface);
                                reviewTextString.append(" " + reviewsAuthorAndContentList.get(i));
                                reviewTextString.append("\n\n");
                                reviewTextString.append(reviewsAuthorAndContentList.get(i + 1));
                                reviewTextString.append("\n\n\n\n");
                            }
                            reviewText.setText(reviewTextString);
                        }

                } catch (JSONException e){
                    showExtrasErrorMessage();
                }
            }
        }
    }


}
