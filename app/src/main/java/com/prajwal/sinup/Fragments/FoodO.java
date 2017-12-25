package com.prajwal.sinup.Fragments;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.prajwal.sinup.ConnectivityBroadcastReceiver;
import com.prajwal.sinup.Constant;
import com.prajwal.sinup.MovieBriefsLargeAdapter;
import com.prajwal.sinup.MovieBriefsSmallAdapter;
import com.prajwal.sinup.MovieGenres;
import com.prajwal.sinup.NetworkConnection;
import com.prajwal.sinup.R;
import com.prajwal.sinup.network.ApiClient;
import com.prajwal.sinup.network.ApiInterface;
import com.prajwal.sinup.network.movies.GenresList;
import com.prajwal.sinup.network.movies.MovieBrief;
import com.prajwal.sinup.network.movies.NowShowingMoviesResponse;
import com.prajwal.sinup.network.movies.PopularMoviesResponse;
import com.prajwal.sinup.network.movies.UpcomingMoviesResponse;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class FoodO extends Fragment {
    private ProgressBar mProgressBar;
    private boolean mNowShowingSectionLoaded;
    private boolean mPopularSectionLoaded;

    private FrameLayout mNowShowingLayout;
    private TextView mNowShowingViewAllTextView;
    private RecyclerView mNowShowingRecyclerView;
    private List<MovieBrief> mNowShowingMovies;
    private MovieBriefsLargeAdapter mNowShowingAdapter;

    private FrameLayout mPopularLayout;
    private TextView mPopularViewAllTextView;
    private RecyclerView mPopularRecyclerView;
    private List<MovieBrief> mPopularMovies;
    private MovieBriefsSmallAdapter mPopularAdapter;

    private boolean isFragmentLoaded;

    private Call<GenresList> mGenresListCall;
    private Call<NowShowingMoviesResponse> mNowShowingMoviesCall;
    private Call<PopularMoviesResponse> mPopularMoviesCall;

    private Snackbar mConnectivitySnackbar;
    private ConnectivityBroadcastReceiver mConnectivityBroadcastReceiver;
    private boolean isBroadcastReceiverRegistered;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view= inflater.inflate(R.layout.fragment_food_o, container, false);
        mProgressBar =  view.findViewById(R.id.progress_bar);
        mProgressBar.setVisibility(View.GONE);
        mNowShowingSectionLoaded = false;
        mPopularSectionLoaded = false;


        mNowShowingLayout = (FrameLayout) view.findViewById(R.id.layout_now_showing);
        mPopularLayout = (FrameLayout) view.findViewById(R.id.layout_popular);

        mNowShowingViewAllTextView = (TextView) view.findViewById(R.id.text_view_view_all_now_showing);
        mPopularViewAllTextView = (TextView) view.findViewById(R.id.text_view_view_all_popular);

        mNowShowingRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view_now_showing);
        (new LinearSnapHelper()).attachToRecyclerView(mNowShowingRecyclerView);
        mPopularRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view_popular);

        mNowShowingMovies = new ArrayList<>();
        mPopularMovies = new ArrayList<>();
        mNowShowingAdapter = new MovieBriefsLargeAdapter(getContext(), mNowShowingMovies);
        mPopularAdapter = new MovieBriefsSmallAdapter(getContext(), mPopularMovies);

        mNowShowingRecyclerView.setAdapter(mNowShowingAdapter);
        mNowShowingRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        mPopularRecyclerView.setAdapter(mPopularAdapter);
        mPopularRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        mNowShowingViewAllTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!NetworkConnection.isConnected(getContext())) {
                    Toast.makeText(getContext(), R.string.no_network, Toast.LENGTH_SHORT).show();
                    return;
                }

            }
        });
        mPopularViewAllTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!NetworkConnection.isConnected(getContext())) {
                    Toast.makeText(getContext(), R.string.no_network, Toast.LENGTH_SHORT).show();
                    return;
                }

            }
        });
        if (NetworkConnection.isConnected(getContext())) {
            isFragmentLoaded = true;
            loadFragment();
        }

        return view;

    }

    private void loadFragment() {

        if (MovieGenres.isGenresListLoaded()) {
            loadNowShowingMovies();
            loadPopularMovies();

        } else {
            ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
            mProgressBar.setVisibility(View.VISIBLE);
            mGenresListCall = apiService.getMovieGenresList(getResources().getString(R.string.MOVIE_DB_API_KEY));
            mGenresListCall.enqueue(new Callback<GenresList>() {
                @Override
                public void onResponse(Call<GenresList> call, Response<GenresList> response) {
                    if (!response.isSuccessful()) {
                        mGenresListCall = call.clone();
                        mGenresListCall.enqueue(this);
                        return;
                    }

                    if (response.body() == null) return;
                    if (response.body().getGenres() == null) return;

                    MovieGenres.loadGenresList(response.body().getGenres());
                    loadNowShowingMovies();
                    loadPopularMovies();
                }

                @Override
                public void onFailure(Call<GenresList> call, Throwable t) {

                }
            });
        }

    }

    @Override
    public void onStart() {
        super.onStart();

        mNowShowingAdapter.notifyDataSetChanged();
        mPopularAdapter.notifyDataSetChanged();

    }

    @Override
    public void onResume() {
        super.onResume();

        if (!isFragmentLoaded && !NetworkConnection.isConnected(getContext())) {
            mConnectivitySnackbar = Snackbar.make(getActivity().findViewById(R.id.content), R.string.no_network, Snackbar.LENGTH_INDEFINITE);
            mConnectivitySnackbar.show();
            mConnectivityBroadcastReceiver = new ConnectivityBroadcastReceiver(new ConnectivityBroadcastReceiver.ConnectivityReceiverListener() {
                @Override
                public void onNetworkConnectionConnected() {
                    mConnectivitySnackbar.dismiss();
                    isFragmentLoaded = true;
                    loadFragment();
                    isBroadcastReceiverRegistered = false;
                    getActivity().unregisterReceiver(mConnectivityBroadcastReceiver);
                }
            });
            IntentFilter intentFilter = new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE");
            isBroadcastReceiverRegistered = true;
            getActivity().registerReceiver(mConnectivityBroadcastReceiver, intentFilter);
        } else if (!isFragmentLoaded && NetworkConnection.isConnected(getContext())) {
            isFragmentLoaded = true;
            loadFragment();
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        if (isBroadcastReceiverRegistered) {
            mConnectivitySnackbar.dismiss();
            isBroadcastReceiverRegistered = false;
            getActivity().unregisterReceiver(mConnectivityBroadcastReceiver);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (mGenresListCall != null) mGenresListCall.cancel();
        if (mNowShowingMoviesCall != null) mNowShowingMoviesCall.cancel();
        if (mPopularMoviesCall != null) mPopularMoviesCall.cancel();

    }

    private void loadNowShowingMovies() {
        ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
        mProgressBar.setVisibility(View.VISIBLE);
        mNowShowingMoviesCall = apiService.getNowShowingMovies(getResources().getString(R.string.MOVIE_DB_API_KEY), 1, "US");
        mNowShowingMoviesCall.enqueue(new Callback<NowShowingMoviesResponse>() {
            @Override
            public void onResponse(Call<NowShowingMoviesResponse> call, Response<NowShowingMoviesResponse> response) {
                if (!response.isSuccessful()) {
                    mNowShowingMoviesCall = call.clone();
                    mNowShowingMoviesCall.enqueue(this);
                    return;
                }

                if (response.body() == null) return;
                if (response.body().getResults() == null) return;

                mNowShowingSectionLoaded = true;
                checkAllDataLoaded();
                for (MovieBrief movieBrief : response.body().getResults()) {
                    if (movieBrief != null && movieBrief.getBackdropPath() != null)
                        mNowShowingMovies.add(movieBrief);
                }
                mNowShowingAdapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(Call<NowShowingMoviesResponse> call, Throwable t) {

            }
        });
    }

    private void loadPopularMovies() {
        ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
        mProgressBar.setVisibility(View.VISIBLE);
        mPopularMoviesCall = apiService.getPopularMovies(getResources().getString(R.string.MOVIE_DB_API_KEY), 1, "US");
        mPopularMoviesCall.enqueue(new Callback<PopularMoviesResponse>() {
            @Override
            public void onResponse(Call<PopularMoviesResponse> call, Response<PopularMoviesResponse> response) {
                if (!response.isSuccessful()) {
                    mPopularMoviesCall = call.clone();
                    mPopularMoviesCall.enqueue(this);
                    return;
                }

                if (response.body() == null) return;
                if (response.body().getResults() == null) return;

                mPopularSectionLoaded = true;
                checkAllDataLoaded();
                for (MovieBrief movieBrief : response.body().getResults()) {
                    if (movieBrief != null && movieBrief.getPosterPath() != null)
                        mPopularMovies.add(movieBrief);
                }
                mPopularAdapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(Call<PopularMoviesResponse> call, Throwable t) {

            }
        });
    }





    private void checkAllDataLoaded() {
        if (mNowShowingSectionLoaded && mPopularSectionLoaded ) {
            mProgressBar.setVisibility(View.GONE);
            mNowShowingLayout.setVisibility(View.VISIBLE);
            mNowShowingRecyclerView.setVisibility(View.VISIBLE);
            mPopularLayout.setVisibility(View.VISIBLE);
            mPopularRecyclerView.setVisibility(View.VISIBLE);

        }
    }


}
