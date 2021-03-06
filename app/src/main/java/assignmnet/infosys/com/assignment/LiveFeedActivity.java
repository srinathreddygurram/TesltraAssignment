package assignmnet.infosys.com.assignment;

import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;

import assignmnet.infosys.com.adapter.ContentListAdapter;
import assignmnet.infosys.com.connection.ContentRequester;
import assignmnet.infosys.com.connection.OnRequestListeners;
import assignmnet.infosys.com.feeds.FeedDetails;
import assignmnet.infosys.com.utils.Constants;
import assignmnet.infosys.com.utils.Utilities;

public class LiveFeedActivity extends AppCompatActivity implements OnRequestListeners, SwipeRefreshLayout.OnRefreshListener {

    ContentListAdapter contentAdapter = null;
    SwipeRefreshLayout contentRefreshLayout = null;
    public static final String TAG = LiveFeedActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_live_feed);
        contentRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.content_swipe_refresh_layout);
        contentRefreshLayout.setOnRefreshListener(this);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        //initializing Adapter
        createContentList();

        //Fetching content on screen load by refreshing layout
        contentRefreshLayout.post(new Runnable() {
                                      @Override
                                      public void run() {
                                          contentRefreshLayout.setRefreshing(true);
                                          ContentRequester.getInstance(LiveFeedActivity.this).getContent(LiveFeedActivity.this);
                                      }
                                  }
        );

    }

    void createContentList() {
        RecyclerView contentRecyclerView = (RecyclerView) findViewById(R.id.contentList);
        contentRecyclerView.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        contentRecyclerView.setLayoutManager(llm);
        contentAdapter = new ContentListAdapter(this);
        contentRecyclerView.setAdapter(contentAdapter);
    }

    /*
         Creates Content array list from JSONObject
      */
    @Override
    public void onRequestSuccess(JSONObject jsonObject) {
        if (jsonObject != null) {
            try {
                Type listType = new TypeToken<ArrayList<FeedDetails>>() {
                }.getType();
                getSupportActionBar().setTitle(jsonObject.getString(Constants.TITLE));
                ArrayList<FeedDetails> yourList = new Gson().fromJson(jsonObject.getJSONArray(Constants.ROW).toString(), listType);
                if (yourList != null) {
                    contentAdapter.setContent(Utilities.removeEmptyFeeds(yourList));
                }
                contentRefreshLayout.post(new Runnable() {
                                              @Override
                                              public void run() {
                                                  contentRefreshLayout.setRefreshing(false);
                                              }
                                          }
                );

            } catch (Exception e) {
                Log.d(TAG, e.getMessage());
            }

        }

    }


    @Override
    public void onRequestFail(VolleyError volleyError) {
        contentRefreshLayout.post(new Runnable() {
                                      @Override
                                      public void run() {
                                          contentRefreshLayout.setRefreshing(false);
                                          showConnectionError();
                                      }
                                  }
        );
    }

    public void showConnectionError() {
        Snackbar snackbar = Snackbar
                .make(findViewById(R.id.main_layout), R.string.connection_error, Snackbar.LENGTH_LONG).setAction(R.string.retry, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        contentRefreshLayout.setRefreshing(true);
                        ContentRequester.getInstance(LiveFeedActivity.this).getContent(LiveFeedActivity.this);
                    }
                });
        snackbar.setActionTextColor(Color.YELLOW);
        View snackBarView = snackbar.getView();
        TextView textView = (TextView) snackBarView.findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(Color.WHITE);
        snackbar.show();
    }

    @Override
    public void onRefresh() {
        //Fetching Content
        ContentRequester.getInstance(LiveFeedActivity.this).getContent(LiveFeedActivity.this);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
