/*
 * PROJECT LICENSE
 *
 * This project was submitted by Brandon Ingram as part of the Android Developer
 * Nanodegree Program at Udacity.
 *
 * As part of Udacity Honor code, your submissions must be your own work, hence
 * submitting this project as yours will cause you to break the Udacity Honor Code
 * and the suspension of your account.
 *
 * Me, the author of the project, allow you to check the code as a reference, but if
 * you submit it, it's your own responsibility if you get expelled.
 *
 * Copyright (c) 2018 Brandon Ingram
 *
 * Besides the above notice, the following license applies and this license notice
 * must be included in all works derived from this project.
 *
 * MIT License
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package com.example.android.movies;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.android.movies.model.VideoDetails;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.VideoListViewHolder> {

    private static final String LOG_TAG = VideoAdapter.class.getSimpleName();

    private final VideoAdapterOnClickHandler clickHandler;

    private List<VideoDetails> videos;

    public interface VideoAdapterOnClickHandler {

        void onClick(VideoDetails video);

    }

    public class VideoListViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @BindView(R.id.tv_video_title)
        TextView titleTextView;

        @BindView(R.id.tv_video_type)
        TextView typeTextView;

        VideoListViewHolder(View view) {
            super(view);

            ButterKnife.bind(this, view);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            VideoDetails video = videos.get(getAdapterPosition());
            Log.d(LOG_TAG, "Video '" + video.getName() + "' was clicked");
            clickHandler.onClick(video);
        }
    }

    public VideoAdapter(VideoAdapterOnClickHandler clickHandler) {
        this.clickHandler = clickHandler;
    }

    @NonNull
    @Override
    public VideoListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.video_list_item, parent, false);
        return new VideoListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VideoListViewHolder holder, int position) {
        VideoDetails video = videos.get(position);

        holder.titleTextView.setText(video.getName());
        holder.typeTextView.setText(video.getType());

        holder.itemView.setTag(video.getKey());
    }

    @Override
    public int getItemCount() {
        if (videos == null) return 0;
        return videos.size();
    }

    public List<VideoDetails> getVideos() {
        return videos;
    }

    public void setVideos(List<VideoDetails> videos) {
        this.videos = videos;
        notifyDataSetChanged();
    }

}
