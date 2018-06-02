package me.jfenn.feedage.activities;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import java.util.List;

import me.jfenn.attribouter.Attribouter;
import me.jfenn.feedage.Feedage;
import me.jfenn.feedage.R;
import me.jfenn.feedage.fragments.BaseFragment;
import me.jfenn.feedage.fragments.BookmarksFragment;
import me.jfenn.feedage.fragments.CategoriesFragment;
import me.jfenn.feedage.fragments.FeedsFragment;
import me.jfenn.feedage.lib.FeedageLib;
import me.jfenn.feedage.lib.data.CategoryData;
import me.jfenn.feedage.lib.data.FeedData;
import me.jfenn.feedage.views.TintedImageView;

public class MainActivity extends AppCompatActivity implements FeedageLib.OnCategoriesUpdatedListener, FragmentManager.OnBackStackChangedListener {

    private Feedage feedage;
    private BaseFragment fragment;

    private TintedImageView home;
    private TintedImageView feeds;
    private TintedImageView bookmarks;

    private int textColor;
    private int accentColor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        feedage = (Feedage) getApplicationContext();

        if (savedInstanceState == null) {
            fragment = new CategoriesFragment();

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment, fragment)
                    .commit();
        } else {
            if (fragment == null)
                fragment = new CategoriesFragment();

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment, fragment)
                    .commit();
        }

        getSupportFragmentManager().addOnBackStackChangedListener(this);
        feedage.addListener(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        home = findViewById(R.id.home);
        feeds = findViewById(R.id.feeds);
        bookmarks = findViewById(R.id.bookmarks);

        setSupportActionBar(toolbar);

        textColor = ContextCompat.getColor(this, R.color.colorTextSecondary);
        accentColor = ContextCompat.getColor(this, R.color.colorAccent);

        home.setTint(accentColor);
        home.setOnClickListener(v -> {
            if (!(fragment instanceof CategoriesFragment)) {
                fragment = new CategoriesFragment();
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment, fragment)
                        .commit();

                home.tint(accentColor);
                feeds.tint(textColor);
                bookmarks.tint(textColor);
            }
        });

        feeds.setTint(textColor);
        feeds.setOnClickListener(v -> {
            if (!(fragment instanceof FeedsFragment)) {
                fragment = new FeedsFragment();
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment, fragment)
                        .commit();

                home.tint(textColor);
                feeds.tint(accentColor);
                bookmarks.tint(textColor);
            }
        });

        bookmarks.setTint(textColor);
        bookmarks.setOnClickListener(v -> {
            if (!(fragment instanceof BookmarksFragment)) {
                fragment = new BookmarksFragment();
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment, fragment)
                        .commit();

                home.tint(textColor);
                feeds.tint(textColor);
                bookmarks.tint(accentColor);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        feedage.removeListener(this);
    }

    @Override
    public void onFeedsUpdated(List<FeedData> feeds) {

    }

    @Override
    public void onCategoriesUpdated(List<CategoryData> categories) {

    }

    @Override
    public void onBackStackChanged() {
        fragment = (BaseFragment) getSupportFragmentManager().findFragmentById(R.id.fragment);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_about)
            Attribouter.from(this).show();

        return super.onOptionsItemSelected(item);
    }
}
