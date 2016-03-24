package com.gotb.parsemeduzajsoup;

import android.os.AsyncTask;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;


public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener, SwipeRefreshLayout.OnRefreshListener{


    public final String NAKED_SCIENCE_URL = "http://naked-science.ru";
    boolean doubleBackToExitPressedOnce = false;
    TextView tvNews;
    ListView lvTitles;
    Map<String, String> hrefTitles;
    List<String> arrayTitles;
    ArrayAdapter<String> arrayAdapterTitles;
    SwipeRefreshLayout mSwipeRefreshLayout;
    ParseTitles parseTitles;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvNews = (TextView) findViewById(R.id.tvNews);
        lvTitles = (ListView) findViewById(R.id.lvTitles);
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.refresh);
        if (mSwipeRefreshLayout != null) {
            mSwipeRefreshLayout.setOnRefreshListener(this);
        }

        parseTitles = new ParseTitles();
        parseTitles.execute();
        try {
            hrefTitles = parseTitles.get();
            arrayTitles = new ArrayList<>();
            for (Map.Entry entry: hrefTitles.entrySet()){
                arrayTitles.add(entry.getKey().toString());
            }
            arrayAdapterTitles = new ArrayAdapter<>(this, R.layout.list_item, arrayTitles);
            lvTitles.setAdapter(arrayAdapterTitles);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        lvTitles.setOnItemClickListener(this);
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        ParseTexts parseTexts = new ParseTexts();
        parseTexts.execute(hrefTitles.get(arrayTitles.get(position)));
        try {
            mSwipeRefreshLayout.setVisibility(View.GONE);
            lvTitles.setVisibility(View.GONE);
            tvNews.setVisibility(View.VISIBLE);
            tvNews.setText(parseTexts.get());
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRefresh() {
        parseTitles = new ParseTitles();
        parseTitles.execute();
        arrayAdapterTitles.notifyDataSetChanged();
        mSwipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onBackPressed() {
        mSwipeRefreshLayout.setVisibility(View.VISIBLE);
        lvTitles.setVisibility(View.VISIBLE);
        tvNews.setVisibility(View.GONE);

        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }
        this.doubleBackToExitPressedOnce = true;
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, 2000);
    }

    class ParseTexts extends AsyncTask<String, Void, String>{

        @Override
        protected String doInBackground(String... params) {
            String textNews = "";

            try {
                Document document = Jsoup.connect(params[0]).get();
                Element element = document.select(".content").first();
                textNews = element.text();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return textNews;
        }
    }

    class ParseTitles extends AsyncTask<Void, Void, Map<String, String>>{

        @Override
        protected Map<String, String> doInBackground(Void... params) {
            Map<String, String> hrefTitle = new HashMap<>();

            try {
                Document document = Jsoup.connect(NAKED_SCIENCE_URL).get();
                Elements elements = document.select(".views-field-title");
                for (Element element: elements){
                    Element hrefElement = element.select("a[href]").first();
                    hrefTitle.put(element.text(), hrefElement.attr("abs:href"));
                }

            } catch (IOException e) {
                e.printStackTrace();
            }

            return hrefTitle;
        }
    }
}
