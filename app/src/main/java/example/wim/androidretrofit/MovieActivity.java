package example.wim.androidretrofit;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.net.SocketTimeoutException;

import example.wim.androidretrofit.adapter.MovieListAdapter;
import example.wim.androidretrofit.listener.RecyclerViewItemClickListener;
import example.wim.androidretrofit.model.Movie;
import example.wim.androidretrofit.service.ApiService;
import example.wim.androidretrofit.util.DividerItemDecoration;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Wim on 7/20/16.
 */
public class MovieActivity extends AppCompatActivity implements RecyclerViewItemClickListener {

    private RecyclerView rvMovie;
    private SwipeRefreshLayout swipeRefreshLayout;

    private LinearLayoutManager linearLayoutManager;
    private MovieListAdapter movieListAdapter;

    private ApiService apiService;
    private String id;

    public static void start(Context context, String id) {
        Intent intent = new Intent(context, MovieActivity.class);
        intent.putExtra(MovieActivity.class.getSimpleName(), id);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie);

        id = getIntent().getStringExtra(MovieActivity.class.getSimpleName());

        rvMovie = (RecyclerView) findViewById(R.id.rv_movie);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.refresh);

        linearLayoutManager = new LinearLayoutManager(this);
        movieListAdapter = new MovieListAdapter(this);
        movieListAdapter.setRecyclerViewItemClickListener(this);

        rvMovie.setLayoutManager(linearLayoutManager);
        rvMovie.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        rvMovie.setAdapter(movieListAdapter);

        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshData();
            }
        });

        loadData(id);
    }

    private void loadData(String id){
        if (swipeRefreshLayout != null)
            swipeRefreshLayout.post(new Runnable() {
                @Override
                public void run() {
                    swipeRefreshLayout.setRefreshing(true);
                }
            });


        apiService = new ApiService();
        apiService.getMovieList(id, new Callback() {
            @Override
            public void onResponse(Call call, Response response) {
                Movie movie = (Movie) response.body();

                if(movie != null){
                    movieListAdapter.addAll(movie.getData());
                    Log.i("STATUS", movie.getStatus());
                }else{
                    Toast.makeText(MovieActivity.this, "No Data!", Toast.LENGTH_LONG).show();
                }

                if (swipeRefreshLayout != null)
                    swipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onFailure(Call call, Throwable t) {
                if(t instanceof SocketTimeoutException) {
                    Toast.makeText(MovieActivity.this, "Request Timeout. Please try again!", Toast.LENGTH_LONG).show();
                }else{
                    Toast.makeText(MovieActivity.this, "Connection Error!", Toast.LENGTH_LONG).show();
                }

                Log.i("FAILURE", t.toString());

                if (swipeRefreshLayout != null)
                    swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    private void refreshData(){
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                movieListAdapter.clear();
                loadData(id);
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    public void onItemClick(int position, View view) {
        ShowtimeActivity.start(this, movieListAdapter.getItem(position));
    }
}
