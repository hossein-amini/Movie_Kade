package com.aminiam.moviekade.utility;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.util.Log;

import com.aminiam.moviekade.BuildConfig;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class NetworkUtility {
    private static final String LOG_TAG = NetworkUtility.class.getSimpleName();

    private static final String DB_MOVIE_BASE_URL = "http://api.themoviedb.org/3/";

    private static final String MOVIE_PATH = "movie";
    public static final String NOW_PLAYING_PATH = "now_playing";
    public static final String TOP_RATE_PATH = "top_rated";
    public static final String POPULAR_PATH = "popular";
    public static final String UPCOMING_PATH = "upcoming";
    private static final String APPEND_TO_RESPONSE = "append_to_response";
    private static final String API_KEY_PARAM = "api_key";

    /**
     * Create URL for getting all movies from api
     * @param collectionPath for specify which collection we want to get EXP popular, top_rate and ...
     * @return URL
     */
    public static URL getMoviesUrl(String collectionPath) {
        Uri moviesQueryUri = Uri.parse(DB_MOVIE_BASE_URL).buildUpon()
                .appendPath(MOVIE_PATH)
                .appendPath(collectionPath)
                .appendQueryParameter(API_KEY_PARAM, BuildConfig.THE_MOVIE_DB_API_KEY).build();
        try {
            return new URL(moviesQueryUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Create URL for getting a specific movie data from api
     * @param movieId is the ID of specific movie
     *@return URL
     */
    public static URL getMovieDataUrl(long movieId) {
        Uri movieDetailQueryUri = Uri.parse(DB_MOVIE_BASE_URL).buildUpon()
                .appendPath(MOVIE_PATH)
                .appendPath(String.valueOf(movieId))
                .appendQueryParameter(API_KEY_PARAM, BuildConfig.THE_MOVIE_DB_API_KEY)
                .appendQueryParameter(APPEND_TO_RESPONSE, "videos,reviews").build();
        Log.d(LOG_TAG, movieDetailQueryUri.toString());
        try {
            return new URL(movieDetailQueryUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Connect to network and get api response
     * @param url for connection to api
     * @return string that presents json
     * @throws IOException
     */
    public static String getResponseFromHttpUrl(URL url) throws IOException{
        InputStream stream = null;
        HttpURLConnection connection = null;
        String result = null;
        try {
            connection = (HttpURLConnection) url.openConnection();
            connection.setReadTimeout(3000);
            connection.setConnectTimeout(3000);
            connection.setRequestMethod("GET");
            connection.setDoInput(true);
            connection.connect();
            int responseCode = connection.getResponseCode();
            if(responseCode != HttpURLConnection.HTTP_OK) {
                throw new IOException("HTTP error code: " + responseCode);
            }
            stream = connection.getInputStream();
            if(stream != null) {
                result = convertStreamToString(stream);
            }

        } finally {
            if(stream != null) {
                stream.close();
            }
            if(connection != null) {
                connection.disconnect();
            }
        }

        return result;
    }

    /**
     * Convert Stream to String
     * @param inputStream that we want to convert to String
     * @return converted inputStream as String
     */
    private static String convertStreamToString(InputStream inputStream) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder sb = new StringBuilder();

        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }

    public static String buildPosterPath(String imageName) {
        return "http://image.tmdb.org/t/p/w342/".concat(imageName);
    }

    public static String buildBackdropPath(String imageName) {
        return "http://image.tmdb.org/t/p/w500/".concat(imageName);
    }

    public static String buildTrailerImagePath(String imageKey) {
        return "http://img.youtube.com/vi/" + imageKey +"/0.jpg";
    }

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
