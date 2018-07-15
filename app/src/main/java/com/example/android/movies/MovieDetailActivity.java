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
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
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
import com.example.android.movies.model.VideoDetails;
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
import java.util.Locale;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MovieDetailActivity extends AppCompatActivity implements
        VideoAdapter.VideoAdapterOnClickHandler {

    private static final String LOG_TAG = MovieDetailActivity.class.getSimpleName();

    private static final String API_MOVIE_ID_EXTRA = "movie_id";
    private static final int REVIEW_API_LOADER = 33;
    private static final int VIDEO_API_LOADER = 34;

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

    @BindView(R.id.rv_video_list)
    RecyclerView videosRecyclerView;

    @BindView(R.id.tv_video_error_message)
    TextView videosErrorMessage;

    @BindView(R.id.pb_video_loading)
    ProgressBar videosLoadingIndicator;

    private UserReviewAdapter reviewsListAdapter;
    private VideoAdapter videoListAdapter;

    private Movie movie;
    private List<UserReview> cachedReviews;
    private List<VideoDetails> cachedVideos;

    private LoaderManager.LoaderCallbacks<List<UserReview>> reviewsLoaderCallbacks =
            new LoaderManager.LoaderCallbacks<List<UserReview>>() {

                @NonNull
                @Override
                public Loader<List<UserReview>> onCreateLoader(int id, @Nullable final Bundle args) {
                    return new AsyncTaskLoader<List<UserReview>>(MovieDetailActivity.this) {
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
                                Log.w(LOG_TAG, "Exception encountered retrieving reviews: "
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
                public void onLoadFinished(@NonNull Loader<List<UserReview>> loader, List<UserReview> reviews) {
                    reviewsLoadingIndicator.setVisibility(View.INVISIBLE);

                    if (reviews != null && reviews.size() > 0) {
                        showReviewsDataView();
                        reviewsListAdapter.setReviews(reviews);
                    } else {
                        showReviewsErrorMessage("No reviews were found");
                    }
                }

                @Override
                public void onLoaderReset(@NonNull Loader<List<UserReview>> loader) {

                }
            };

    private LoaderManager.LoaderCallbacks<List<VideoDetails>> videosLoaderCallbacks =
            new LoaderManager.LoaderCallbacks<List<VideoDetails>>() {

                @NonNull
                @Override
                public Loader<List<VideoDetails>> onCreateLoader(int id, @Nullable final Bundle args) {
                    return new AsyncTaskLoader<List<VideoDetails>>(MovieDetailActivity.this) {
                        private final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
                                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

                        @Override
                        protected void onStartLoading() {
                            if (args == null) {
                                return;
                            }

                            if (cachedVideos != null) {
                                deliverResult(cachedVideos);
                            } else {
                                videosLoadingIndicator.setVisibility(View.VISIBLE);
                                forceLoad();
                            }
                        }

                        @Nullable
                        @Override
                        public List<VideoDetails> loadInBackground() {
                            Long movieId = args.getLong(API_MOVIE_ID_EXTRA);

                            String jsonResponse;
                            try {
                                URL url = NetworkUtils.buildDetailUrl(ApiDetailRequestType.VIDEOS, movieId);
                                jsonResponse = NetworkUtils.getResponseFromHttpUrl(url);
                                Log.v(LOG_TAG, "JSON Results: " + jsonResponse);
                            } catch (Exception e) {
                                Log.w(LOG_TAG, "Exception encountered retrieving videos: "
                                        + e.getMessage(), e);
                                return null;
                            }

                            if (jsonResponse != null) {
                                try {
                                    JsonNode rootNode = OBJECT_MAPPER.readTree(jsonResponse);
                                    JsonNode resultsNode = rootNode.at("/results");
                                    List<VideoDetails> videos = OBJECT_MAPPER.readValue(resultsNode.toString(),
                                            new TypeReference<List<VideoDetails>>(){
                                            });
                                    Log.d(LOG_TAG, "Parsed " + videos.size() + " videos from JSON results");
                                    return videos;
                                } catch (IOException e) {
                                    Log.w(LOG_TAG, "Exception encountered parsing JSON results: "
                                            + e.getMessage(), e);
                                    return null;
                                }
                            }

                            return null;
                        }

                        @Override
                        public void deliverResult(@Nullable List<VideoDetails> videos) {
                            cachedVideos = videos;
                            super.deliverResult(videos);
                        }
                    };
                }

                @Override
                public void onLoadFinished(@NonNull Loader<List<VideoDetails>> loader, List<VideoDetails> videos) {
                    videosLoadingIndicator.setVisibility(View.INVISIBLE);

                    if (videos != null && videos.size() > 0) {
                        showVideosDataView();
                        videoListAdapter.setVideos(videos);
                    } else {
                        showVideosErrorMessage("No videos were found");
                    }
                }

                @Override
                public void onLoaderReset(@NonNull Loader<List<VideoDetails>> loader) {

                }
            };

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
                        .getString(R.string.api_date_format), Locale.getDefault())
                        .parse(movie.getReleaseDate());

                releaseDateTextView.setText(new SimpleDateFormat(getResources()
                        .getString(R.string.display_date_format), Locale.getDefault())
                        .format(releaseDate));
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

            videosRecyclerView.setHasFixedSize(true);
            videosRecyclerView.setLayoutManager(new LinearLayoutManager(this));

            videoListAdapter = new VideoAdapter(this);
            videosRecyclerView.setAdapter(videoListAdapter);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchUserReviews();
        fetchVideos();
    }

    @Override
    public void onClick(VideoDetails video) {
        Intent videoIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube://" + video.getKey()));
        Intent webIntent = new Intent(Intent.ACTION_VIEW,
                Uri.parse("http://www.youtube.com/watch?v=" + video.getKey()));

        if (videoIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(videoIntent);
        } else {
            startActivity(webIntent);
        }
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
            Loader<String> reviewApiLoader = loaderManager.getLoader(REVIEW_API_LOADER);
            if (reviewApiLoader == null) {
                loaderManager.initLoader(REVIEW_API_LOADER, bundle, reviewsLoaderCallbacks);
            } else {
                loaderManager.restartLoader(REVIEW_API_LOADER, bundle, reviewsLoaderCallbacks);
            }
        } else {
            String error = getResources().getString(R.string.no_network_available_error);
            Log.w(LOG_TAG, error);
        }
    }

    private void fetchVideos() {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = Objects.requireNonNull(connectivityManager).getActiveNetworkInfo();

        // Perform the request if the network is available & connected
        if (networkInfo != null && networkInfo.isConnected()) {
            Bundle bundle = new Bundle();
            bundle.putLong(API_MOVIE_ID_EXTRA, movie.getId());

            LoaderManager loaderManager = getSupportLoaderManager();
            Loader<String> videoApiLoader = loaderManager.getLoader(VIDEO_API_LOADER);
            if (videoApiLoader == null) {
                loaderManager.initLoader(VIDEO_API_LOADER, bundle, videosLoaderCallbacks);
            } else {
                loaderManager.restartLoader(VIDEO_API_LOADER, bundle, videosLoaderCallbacks);
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

    private void showVideosDataView() {
        videosErrorMessage.setText(null);
        videosErrorMessage.setVisibility(View.INVISIBLE);
        videosRecyclerView.setVisibility(View.VISIBLE);
    }

    private void showVideosErrorMessage(String message) {
        videosErrorMessage.setText(message);
        videosErrorMessage.setVisibility(View.VISIBLE);
        videosRecyclerView.setVisibility(View.INVISIBLE);
    }

}
