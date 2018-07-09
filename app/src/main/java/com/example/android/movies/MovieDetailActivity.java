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

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;

import com.example.android.movies.model.Movie;
import com.example.android.movies.model.UserReview;
import com.example.android.movies.util.ApiDetailRequestType;
import com.example.android.movies.util.NetworkUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MovieDetailActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<List<UserReview>> {

    private static final String LOG_TAG = MovieDetailActivity.class.getSimpleName();

    private static final String API_MOVIE_ID_EXTRA = "movie_id";
    private static final int REVIEW_API_LOADER = 33;

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

    @BindView(R.id.rv_review_list)
    RecyclerView reviewsRecyclerView;

    @BindView(R.id.tv_review_error_message)
    TextView reviewsErrorMessage;

    @BindView(R.id.pb_review_loading)
    ProgressBar reviewsLoadingIndicator;

    private UserReviewAdapter reviewsListAdapter;

    private Movie movie;
    private List<UserReview> cachedReviews;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_detail);

        ButterKnife.bind(this);

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(Movie.MOVIE_INTENT_KEY)) {
            movie = intent.getParcelableExtra(Movie.MOVIE_INTENT_KEY);
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

            reviewsRecyclerView.setHasFixedSize(true);
            reviewsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

            reviewsListAdapter = new UserReviewAdapter();
            reviewsRecyclerView.setAdapter(reviewsListAdapter);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchUserReviews();
    }

    private void fetchUserReviews() {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = Objects.requireNonNull(connectivityManager).getActiveNetworkInfo();

        // Perform the request if the network is available & connected
        if (networkInfo != null && networkInfo.isConnected()) {
            Bundle bundle = new Bundle();
            bundle.putLong(API_MOVIE_ID_EXTRA, movie.getId());

            LoaderManager loaderManager = getSupportLoaderManager();
            Loader<String> githubSearchLoader = loaderManager.getLoader(REVIEW_API_LOADER);
            if (githubSearchLoader == null) {
                loaderManager.initLoader(REVIEW_API_LOADER, bundle, this);
            } else {
                loaderManager.restartLoader(REVIEW_API_LOADER, bundle, this);
            }
        } else {
            String error = getResources().getString(R.string.no_network_available_error);
            Log.w(LOG_TAG, error);
        }
    }

    private void showReviewsDataView() {
        reviewsErrorMessage.setText(null);
        reviewsErrorMessage.setVisibility(View.INVISIBLE);
        reviewsRecyclerView.setVisibility(View.VISIBLE);
    }

    private void showReviewsErrorMessage(String message) {
        reviewsErrorMessage.setText(message);
        reviewsErrorMessage.setVisibility(View.VISIBLE);
        reviewsRecyclerView.setVisibility(View.INVISIBLE);
    }

    @Override
    public Loader<List<UserReview>> onCreateLoader(int id, final Bundle args) {
        return new AsyncTaskLoader<List<UserReview>>(this) {
            private final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
                    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

            @Override
            protected void onStartLoading() {
                if (args == null) {
                    return;
                }

                if (cachedReviews != null) {
                    deliverResult(cachedReviews);
                } else {
                    reviewsLoadingIndicator.setVisibility(View.VISIBLE);
                    forceLoad();
                }

            }

            @Override
            public List<UserReview> loadInBackground() {
                Long movieId = args.getLong(API_MOVIE_ID_EXTRA);

                String jsonResponse;
                try {
                    URL url = NetworkUtils.buildDetailUrl(ApiDetailRequestType.USER_REVIEWS, movieId);
                    jsonResponse = NetworkUtils.getResponseFromHttpUrl(url);
                    Log.v(LOG_TAG, "JSON Results: " + jsonResponse);
                } catch (Exception e) {
                    Log.w(LOG_TAG, "Exception encountered retrieving movies: "
                            + e.getMessage(), e);
                    return null;
                }

                if (jsonResponse != null) {
                    try {
                        JsonNode rootNode = OBJECT_MAPPER.readTree(jsonResponse);
                        JsonNode resultsNode = rootNode.at("/results");
                        List<UserReview> reviews = OBJECT_MAPPER.readValue(resultsNode.toString(),
                                new TypeReference<List<UserReview>>() {
                                });
                        Log.d(LOG_TAG, "Parsed " + reviews.size() + " user reviews from JSON results");
                        return reviews;
                    } catch (IOException e) {
                        Log.w(LOG_TAG, "Exception encountered parsing JSON results: "
                                + e.getMessage(), e);
                        return null;
                    }
                }

                return null;
            }

            @Override
            public void deliverResult(@Nullable List<UserReview> reviews) {
                cachedReviews = reviews;
                super.deliverResult(reviews);
            }
        };
    }

    @Override
    public void onLoadFinished(Loader<List<UserReview>> loader, List<UserReview> reviews) {
        reviewsLoadingIndicator.setVisibility(View.INVISIBLE);

        if (reviews != null && reviews.size() > 0) {
            showReviewsDataView();
            reviewsListAdapter.setReviews(reviews);
        } else {
            showReviewsErrorMessage("No reviews were found");
        }
    }

    @Override
    public void onLoaderReset(Loader<List<UserReview>> loader) {

    }
}
