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
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.android.movies.model.Movie;
import com.example.android.movies.util.ApiRequestType;
import com.example.android.movies.util.NetworkUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MovieListActivity extends AppCompatActivity
        implements MovieListAdapter.MovieListAdapterOnClickHandler,
        LoaderManager.LoaderCallbacks<List<Movie>> {

    private static final String LOG_TAG = MovieListActivity.class.getSimpleName();

    private static final String API_CALL_TYPE_EXTRA = "query_url";
    private static final int MOVIE_API_LOADER = 32;

    @BindView(R.id.rv_movie_list)
    RecyclerView recyclerView;

    @BindView(R.id.tv_error_message)
    TextView errorMessage;

    @BindView(R.id.pb_loading)
    ProgressBar loadingIndicator;

    private ApiRequestType currentApiRequestType;
    private MovieListAdapter movieListAdapter;

    ApiRequestType cachedRequestType = null;
    List<Movie> cachedMovies;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_list);

        ButterKnife.bind(this);

        if (savedInstanceState != null && savedInstanceState.containsKey(getResources()
                .getString(R.string.current_api_request_type_key))) {
            currentApiRequestType = ApiRequestType.valueOf(savedInstanceState
                    .getString(getResources().getString(R.string.current_api_request_type_key)));
        }

        if (currentApiRequestType == null) {
            currentApiRequestType = ApiRequestType.POPULAR;
        }

        recyclerView.setHasFixedSize(true);

        int spanCount;
        if (Configuration.ORIENTATION_LANDSCAPE == getResources().getConfiguration().orientation) {
            spanCount = getResources().getInteger(R.integer.number_of_columns_landscape);
        } else {
            spanCount = getResources().getInteger(R.integer.number_of_columns_portrait);
        }
        recyclerView.setLayoutManager(new GridLayoutManager(this, spanCount));

        movieListAdapter = new MovieListAdapter(this);
        recyclerView.setAdapter(movieListAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchMovies();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(getResources().getString(R.string.current_api_request_type_key),
                currentApiRequestType.name());
        outState.putString(getResources().getString(R.string.cached_api_request_type_key),
                cachedRequestType.name());
        outState.putParcelableArrayList(getResources().getString(R.string.cached_movies_key),
                (ArrayList<? extends Parcelable>) cachedMovies);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(getResources()
                    .getString(R.string.current_api_request_type_key))) {
                currentApiRequestType = ApiRequestType.valueOf(savedInstanceState
                        .getString(getResources().getString(R.string.current_api_request_type_key)));
            }

            if (savedInstanceState.containsKey(getResources()
                    .getString(R.string.cached_api_request_type_key))) {
                cachedRequestType = ApiRequestType.valueOf(savedInstanceState
                        .getString(getResources().getString(R.string.cached_api_request_type_key)));
            }

            if (savedInstanceState.containsKey(getResources()
                    .getString(R.string.cached_movies_key))) {
                cachedMovies = savedInstanceState.getParcelableArrayList(getResources()
                        .getString(R.string.cached_movies_key));
            }
        }
    }

    @Override
    public void onClick(Movie movie) {
        Intent detailIntent = new Intent(this, MovieDetailActivity.class);
        detailIntent.putExtra(Movie.MOVIE_INTENT_KEY, movie);
        startActivity(detailIntent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.sort_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.menu_sort_popular) {
            currentApiRequestType = ApiRequestType.POPULAR;
            fetchMovies();
        } else if (id == R.id.menu_sort_top_rated) {
            currentApiRequestType = ApiRequestType.TOP_RATED;
            fetchMovies();
        }

        changeTitle(currentApiRequestType);

        return super.onOptionsItemSelected(item);
    }

    private void fetchMovies() {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = Objects.requireNonNull(connectivityManager).getActiveNetworkInfo();

        // Perform the request if the network is available & connected
        if (networkInfo != null && networkInfo.isConnected()) {
            Bundle bundle = new Bundle();
            bundle.putString(API_CALL_TYPE_EXTRA, currentApiRequestType.name());

            LoaderManager loaderManager = getSupportLoaderManager();
            Loader<String> githubSearchLoader = loaderManager.getLoader(MOVIE_API_LOADER);
            if (githubSearchLoader == null) {
                loaderManager.initLoader(MOVIE_API_LOADER, bundle, this);
            } else {
                loaderManager.restartLoader(MOVIE_API_LOADER, bundle, this);
            }

            changeTitle(currentApiRequestType);
            showMovieDataView();
        } else {
            String error = getResources().getString(R.string.no_network_available_error);
            Log.w(LOG_TAG, error);
            showErrorMessage(error);
        }
    }

    private void changeTitle(ApiRequestType requestType) {
        if (requestType.equals(ApiRequestType.POPULAR)) {
            setTitle(R.string.sort_popular_title);
        } else if (requestType.equals(ApiRequestType.TOP_RATED)) {
            setTitle(R.string.sort_top_rated_title);
        }
    }

    private void showMovieDataView() {
        errorMessage.setText(null);
        errorMessage.setVisibility(View.INVISIBLE);
        recyclerView.setVisibility(View.VISIBLE);
    }

    private void showErrorMessage(String message) {
        errorMessage.setText(message);
        errorMessage.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.INVISIBLE);
    }

    @Override
    public Loader<List<Movie>> onCreateLoader(int id, final Bundle args) {
        return new AsyncTaskLoader<List<Movie>>(this) {
            private final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
                    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

            @Override
            protected void onStartLoading() {
                if (args == null) {
                    return;
                }

                ApiRequestType bundledRequestType = ApiRequestType.valueOf(
                        args.getString(API_CALL_TYPE_EXTRA));
                if (cachedRequestType != null && cachedRequestType.equals(bundledRequestType)
                        && cachedMovies != null) {
                    deliverResult(cachedMovies);
                } else {
                    cachedRequestType = bundledRequestType;
                    loadingIndicator.setVisibility(View.VISIBLE);
                    forceLoad();
                }
            }

            @Override
            public List<Movie> loadInBackground() {
                if (cachedRequestType == null) {
                    return null;
                }

                String jsonResponse;
                try {
                    URL url = NetworkUtils.buildUrl(cachedRequestType);
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
                        List<Movie> movies = OBJECT_MAPPER.readValue(resultsNode.toString(),
                                new TypeReference<List<Movie>>() {
                                });
                        Log.d(LOG_TAG, "Parsed " + movies.size() + " movies from JSON results");
                        return movies;
                    } catch (IOException e) {
                        Log.w(LOG_TAG, "Exception encountered parsing JSON results: "
                                + e.getMessage(), e);
                        return null;
                    }
                }

                return null;
            }

            @Override
            public void deliverResult(@Nullable List<Movie> movies) {
                cachedMovies = movies;
                super.deliverResult(movies);
            }
        };
    }

    @Override
    public void onLoadFinished(Loader<List<Movie>> loader, List<Movie> movies) {
        loadingIndicator.setVisibility(View.INVISIBLE);

        if (movies != null && movies.size() > 0) {
            showMovieDataView();
            movieListAdapter.setMovies(movies);
        } else {
            showErrorMessage("No movies were found");
        }
    }

    @Override
    public void onLoaderReset(Loader<List<Movie>> loader) {

    }

}
