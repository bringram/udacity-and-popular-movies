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

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.example.android.movies.model.Movie;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MovieDetailActivity extends AppCompatActivity {

    @BindView(R.id.iv_detail_movie_poster)
    ImageView moviePoster;

    @BindView(R.id.tv_detail_original_title)
    TextView titleTextView;

    @BindView(R.id.tv_detail_release_date)
    TextView releaseDateTextView;

    @BindView(R.id.rb_detail_user_rating)
    RatingBar userRatingBar;

    @BindView(R.id.tv_detail_plot_synopsis)
    TextView plotSynopsisTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_detail);

        ButterKnife.bind(this);

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(Movie.MOVIE_INTENT_KEY)) {
            Movie movie = (Movie) intent.getSerializableExtra(Movie.MOVIE_INTENT_KEY);
            setTitle(movie.getTitle());

            try {
                Date releaseDate = new SimpleDateFormat(getResources()
                        .getString(R.string.api_date_format)).parse(movie.getReleaseDate());
                releaseDateTextView.setText(new SimpleDateFormat(getResources()
                        .getString(R.string.display_date_format)).format(releaseDate));
            } catch (ParseException e) {
                e.printStackTrace();
            }

            titleTextView.setText(movie.getOriginalTitle());
            userRatingBar.setRating((movie.getVoteAverage() / 10) * 5);
            plotSynopsisTextView.setText(movie.getOverview());

            Picasso.get().load(getResources()
                    .getString(R.string.base_image_url) + movie.getPosterPath()).into(moviePoster);
        }
    }
}
