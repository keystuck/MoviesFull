package com.example.emily.moviesfull;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.PosterViewHolder> {

    private Context mContext;
    private ArrayList<Movie> mMovieList;


    public MovieAdapter(Context mContext, ArrayList<Movie> movieList){
        this.mContext = mContext;
        this.mMovieList = movieList;
    }

    @NonNull
    @Override
    public PosterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclerview_layout, parent, false);
        return new PosterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PosterViewHolder holder, final int position) {
        String posterPath = mMovieList.get(position).getPosterPath();
        if (posterPath == null || posterPath.isEmpty()){
            holder.mPoster.setImageResource(R.drawable.no_image);
        } else{
            Picasso.with(mContext)
                    .load(posterPath)
                    .fit()
                    .centerInside()
                    .placeholder(R.drawable.no_image)
                    .error(R.drawable.no_image)
                    .into(holder.mPoster);
        }
        holder.mPoster.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, MovieDetailActivity.class);
                intent.putExtra(MovieDetailActivity.MOVIE_EXTRA, mMovieList.get(position));
                mContext.startActivity(intent);
            }
        });
    }


    @Override
    public int getItemCount() {
        return mMovieList.size();
    }

    public void clear(){ mMovieList.clear(); }

    public void addAll(List<Movie> newMovieList){ mMovieList.addAll(newMovieList); }


    class PosterViewHolder extends RecyclerView.ViewHolder{
        ImageView mPoster;
        public PosterViewHolder(View itemView){
            super(itemView);
            mPoster = itemView.findViewById(R.id.iv_poster);
        }
    }
}
