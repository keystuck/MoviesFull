//Emily Stuckey
//Udacity Android Development
//Movies Project 2
//June 26, 2018
//API key required -- add it in utilities/NetworkUtilities file

package com.example.emily.moviesfull;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.example.emily.moviesfull.database.FavoriteDB;
import com.example.emily.moviesfull.utilities.JSONUtils;
import com.example.emily.moviesfull.utilities.NetworkUtilities;

import org.json.JSONException;


public class MainActivity extends AppCompatActivity {
    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    RecyclerView mRecyclerView;
    GridLayoutManager mGridLayoutManager;
    MovieAdapter mAdapter;
    ArrayList<Movie> mMovieList = new ArrayList<>();
    private String moviesJSON;

    private final static String POPULAR = "popular";
    private final static String TOP_RATED = "top_rated";
    private final static String FAVES_LIST = "favorites";
    private final static String NO_INTERNET = "no internet";
    private final static String SUCCESS = "success";

    private String CURRENT_SORT = POPULAR;

    private TextView mErrorMessageView;

    private MainViewModel mainViewModel;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mRecyclerView = findViewById(R.id.recyclerView);
        mGridLayoutManager = new GridLayoutManager(MainActivity.this, 2);
        mRecyclerView.setLayoutManager(mGridLayoutManager);
        mErrorMessageView = findViewById(R.id.tv_error_message_display);

        mainViewModel = ViewModelProviders.of(this).get(MainViewModel.class);
        String previous_sort = mainViewModel.getSortMethod();
        if (!(previous_sort == null)){
            CURRENT_SORT = mainViewModel.getSortMethod();
        }
        fetchList(CURRENT_SORT);

        mAdapter = new MovieAdapter(MainActivity.this, mMovieList);
        mRecyclerView.setAdapter(mAdapter);

    }

    private void fetchList(String sortMethod){
        if (sortMethod.equals(POPULAR) || sortMethod.equals(TOP_RATED)){
            new GetMovieListTask().execute(CURRENT_SORT);
        } else if (sortMethod.equals(FAVES_LIST)){
            getFavesFromViewModel();
        }
    }

    private void getFavesFromViewModel(){
        if (mainViewModel.getMovieList() == null) {
            showErrorMessage();
        } else {
            mainViewModel.getMovieList().observe(this, new Observer<List<Movie>>() {
                @Override
                public void onChanged(@Nullable List<Movie> movies) {
                    mAdapter.clear();
                    mAdapter.addAll(movies);
                    mRecyclerView.setAdapter(mAdapter);
                    mErrorMessageView.setVisibility(View.INVISIBLE);
                    mRecyclerView.setVisibility(View.VISIBLE);
                }
            });
        }
    }

    public void showErrorMessage(){
        mRecyclerView.setVisibility(View.INVISIBLE);
        mErrorMessageView.setVisibility(View.VISIBLE);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.sort_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch(id) {
            case R.id.action_sortPopular:
                if (CURRENT_SORT.equals(TOP_RATED) || CURRENT_SORT.equals(FAVES_LIST)){
                    CURRENT_SORT = POPULAR;
                    mainViewModel.setSortMethod(CURRENT_SORT);
                    new GetMovieListTask().execute(CURRENT_SORT);
                } else {
                    Toast.makeText(this, getResources().getString(R.string.already_popular), Toast.LENGTH_SHORT).show();
                }
                return true;

            case R.id.action_sortRating:
                if (CURRENT_SORT.equals(POPULAR) || CURRENT_SORT.equals(FAVES_LIST)) {
                    CURRENT_SORT = TOP_RATED;
                    mainViewModel.setSortMethod(CURRENT_SORT);
                    new GetMovieListTask().execute(CURRENT_SORT);
                } else {
                    Toast.makeText(this, getResources().getString(R.string.already_top), Toast.LENGTH_SHORT).show();
                }
                return true;

            case R.id.action_showFaves:
                if (CURRENT_SORT.equals(POPULAR) || CURRENT_SORT.equals(TOP_RATED)) {
                    CURRENT_SORT = FAVES_LIST;
                    mainViewModel.setSortMethod(CURRENT_SORT);
                    getFavesFromViewModel();
                } else {
                    Toast.makeText(this, getResources().getString(R.string.already_fave), Toast.LENGTH_SHORT).show();
                }
                return true;

            default:
                Toast.makeText(this, getResources().getString(R.string.bad_selection), Toast.LENGTH_SHORT).show();
                return super.onOptionsItemSelected(item);
        }
    }


    public class GetMovieListTask extends AsyncTask<String, Void, String>{

        @Override
        protected String doInBackground(String... strings) {
            if (strings.length == 0) {
                return null;
            } else {
                URL movieListUrl = NetworkUtilities.buildURL(strings[0]);
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
                    return NO_INTERNET;
                }

                try {
                    moviesJSON = NetworkUtilities.getResponseFromHttpUrl(movieListUrl);
                    return SUCCESS;
                } catch (Exception e){
                    e.printStackTrace();
                    return NO_INTERNET;
                }
            }
        }

        @Override
        protected void onPostExecute(String s) {
            if (s.equals(SUCCESS)) {
                try {
                    ArrayList<Movie> moviesFromJSON =
                            JSONUtils.getMoviesFromJSON(moviesJSON);
                    if (moviesFromJSON == null){
                        showErrorMessage();
                    } else {
                        mAdapter.clear();
                        mAdapter.addAll(moviesFromJSON);
                        mRecyclerView.setAdapter(mAdapter);
                        mErrorMessageView.setVisibility(View.INVISIBLE);
                        mRecyclerView.setVisibility(View.VISIBLE);
                        }
                } catch (JSONException e){
                    showErrorMessage();
                }
            } else {
                showErrorMessage();
            }
        }
    }
}
