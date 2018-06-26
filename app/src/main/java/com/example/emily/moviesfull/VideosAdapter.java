package com.example.emily.moviesfull;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class VideosAdapter extends RecyclerView.Adapter<VideosAdapter.VideoViewHolder>{

    private static final String LOG_TAG = VideosAdapter.class.getSimpleName();

    private ArrayList<String> videoURLsAndTitles = new ArrayList<>();
    private Context mContext;


    public VideosAdapter(ArrayList<String> videoInfo, Context context){
        Log.d(LOG_TAG, "entering adapter: " + videoInfo.get(0));

        videoURLsAndTitles = videoInfo;
        mContext = context;
    }

    @Override
    public int getItemCount() {
        return videoURLsAndTitles.size();
    }

    public void clear(){
        videoURLsAndTitles.clear();
    }

    public void addAll(ArrayList<String> videoInfo){
        videoURLsAndTitles = videoInfo;
    }

    @NonNull
    @Override
    public VideoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LinearLayout layout = (LinearLayout) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.video_list_item_layout, parent, false);

        VideoViewHolder vh = new VideoViewHolder(layout);
        return vh;
    }


    @Override
    public void onBindViewHolder(@NonNull VideoViewHolder holder, int position) {
        String input = videoURLsAndTitles.get(position);

        Log.d(LOG_TAG, input);

        int delimiter = input.indexOf("TITLE:");
        if (delimiter < 1){
            return;
        }
        final String video_url = input.substring(0, delimiter);
        holder.tv_url.setText(video_url);

        delimiter = delimiter + 6;
        if (delimiter > input.length()){
            input = "";
        } else {
            input = input.substring(delimiter);
        }
        holder.tv_title.setText(input);

        holder.button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                Uri videoLink = Uri.parse(video_url);

                Intent intent = new Intent(Intent.ACTION_VIEW, videoLink);
                if (intent.resolveActivity(mContext.getPackageManager()) != null){
                    mContext.startActivity(intent);
                }
            }
        });


    }

    public static class VideoViewHolder extends RecyclerView.ViewHolder{
        public LinearLayout view;
        public ImageButton button;
        public TextView tv_url;
        public TextView tv_title;

        public VideoViewHolder(View view){
            super(view);
            this.view = (LinearLayout) view;
            tv_url = view.findViewById(R.id.tv_video_url);
            tv_title = view.findViewById(R.id.tv_video_title);
            button = view.findViewById(R.id.ib_play_video);
        }


    }

}
