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

package com.example.android.movies.model;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

import com.example.android.movies.data.BitmapConverter;
import com.fasterxml.jackson.annotation.JsonProperty;

@Entity(tableName = "movies")
public class Movie implements Parcelable {

    @Ignore
    public static final String MOVIE_INTENT_KEY = "movieData";

    @PrimaryKey
    @JsonProperty("id")
    private long id;

    @JsonProperty("title")
    private String title;

    @ColumnInfo(name = "original_title")
    @JsonProperty("original_title")
    private String originalTitle;

    @JsonProperty("overview")
    private String overview;

    @ColumnInfo(name = "release_date")
    @JsonProperty("release_date")
    private String releaseDate;

    @ColumnInfo(name = "vote_average")
    @JsonProperty("vote_average")
    private float voteAverage;

    @Ignore
    @JsonProperty("poster_path")
    private String posterPath;

    @Ignore
    @JsonProperty("backdrop_path")
    private String backdropPath;

    @ColumnInfo(name = "movie_poster_img")
    private Bitmap posterBitmap;

    @Ignore
    public Movie() {
        // This constructor is used for Jackson JSON parsing
    }

    public Movie(long id, String title, String originalTitle, String overview, String releaseDate,
                 float voteAverage, Bitmap posterBitmap) {
        this.id = id;
        this.title = title;
        this.originalTitle = originalTitle;
        this.overview = overview;
        this.releaseDate = releaseDate;
        this.voteAverage = voteAverage;
        this.posterBitmap = posterBitmap;
    }

    @Ignore
    protected Movie(Parcel in) {
        id = in.readLong();
        title = in.readString();
        originalTitle = in.readString();
        overview = in.readString();
        releaseDate = in.readString();
        voteAverage = in.readFloat();
        posterPath = in.readString();
        backdropPath = in.readString();

        int byteArraySize = in.readInt();
        if (byteArraySize != 0) {
            byte[] byteArray = new byte[byteArraySize];
            in.readByteArray(byteArray);
            posterBitmap = BitmapConverter.toBitmap(byteArray);
        }
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getOriginalTitle() {
        return originalTitle;
    }

    public void setOriginalTitle(String originalTitle) {
        this.originalTitle = originalTitle;
    }

    public String getOverview() {
        return overview;
    }

    public void setOverview(String overview) {
        this.overview = overview;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }

    public float getVoteAverage() {
        return voteAverage;
    }

    public void setVoteAverage(float voteAverage) {
        this.voteAverage = voteAverage;
    }

    public String getPosterPath() {
        return posterPath;
    }

    public void setPosterPath(String posterPath) {
        this.posterPath = posterPath;
    }

    public String getBackdropPath() {
        return backdropPath;
    }

    public void setBackdropPath(String backdropPath) {
        this.backdropPath = backdropPath;
    }

    public Bitmap getPosterBitmap() {
        return posterBitmap;
    }

    public void setPosterBitmap(Bitmap posterBitmap) {
        this.posterBitmap = posterBitmap;
    }

    public static final Creator<Movie> CREATOR = new Creator<Movie>() {
        @Override
        public Movie createFromParcel(Parcel in) {
            return new Movie(in);
        }

        @Override
        public Movie[] newArray(int size) {
            return new Movie[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(title);
        dest.writeString(originalTitle);
        dest.writeString(overview);
        dest.writeString(releaseDate);
        dest.writeFloat(voteAverage);
        dest.writeString(posterPath);
        dest.writeString(backdropPath);

        if (posterBitmap != null) {
            byte[] byteArray = BitmapConverter.toByteArray(posterBitmap);
            dest.writeInt(byteArray.length);
            dest.writeByteArray(byteArray);
        } else {
            dest.writeInt(0);
        }
    }
}
