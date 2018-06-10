package me.jfenn.feedage;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import james.colorpickerdialog.ColorPicker;
import me.jfenn.feedage.lib.FeedageLib;
import me.jfenn.feedage.lib.data.AtomFeedData;
import me.jfenn.feedage.lib.data.CategoryData;
import me.jfenn.feedage.lib.data.FeedData;
import me.jfenn.feedage.lib.data.PostData;
import me.jfenn.feedage.services.SyncService;
import me.jfenn.feedage.utils.HackyCacheInterface;
import me.jfenn.feedage.utils.PreferenceUtils;
import me.jfenn.feedage.utils.ServiceUtils;
import me.jfenn.feedage.utils.tasks.ArticlesGetterTask;
import me.jfenn.feedage.utils.tasks.ArticlesPutterTask;

public class Feedage extends ColorPicker implements FeedageLib.OnCategoriesUpdatedListener {

    public static final String PREF_THEME = "theme";
    public static final String PREF_FEEDS = "feeds";
    public static final String PREF_CATEGORIES = "categories";
    public static final String PREF_BOOKMARKS = "bookmarks";
    public static final String PREF_SYNC_TIME = "syncTime";

    public static final int THEME_LIGHT = 0;
    public static final int THEME_DARK = 1;
    public static final int THEME_AMOLED = 2;

    private FeedageLib feedage;
    private List<FeedageLib.OnCategoriesUpdatedListener> listeners;
    private OnProgressUpdateListener progressListener;
    private List<OnPreferenceListener> preferenceListeners;

    private List<FeedData> feeds;
    private List<CategoryData> categories;
    private List<PostData> bookmarks;

    private SharedPreferences prefs;
    private boolean isLoading;

    private ArticlesGetterTask getterTask;
    private ArticlesPutterTask putterTask;

    @Override
    public void onCreate() {
        super.onCreate();
        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        listeners = new ArrayList<>();
        preferenceListeners = new ArrayList<>();

        feeds = new ArrayList<>();
        categories = new ArrayList<>();
        bookmarks = PreferenceUtils.getPostList(prefs, PREF_BOOKMARKS);

        feeds = PreferenceUtils.getFeedList(prefs, PREF_FEEDS);
        if (feeds.size() == 0) {
            feeds.addAll(Arrays.asList(
                    new AtomFeedData("https://www.androidpolice.com/feed/", Color.parseColor("#af1c1c"), Color.WHITE),
                    new AtomFeedData("https://www.androidauthority.com/feed/", Color.parseColor("#01e0bd"), Color.BLACK),
                    new AtomFeedData("https://www.theverge.com/rss/index.xml", Color.parseColor("#e5127d"), Color.WHITE),
                    new AtomFeedData("https://techaeris.com/feed/", Color.parseColor("#212121"), Color.WHITE),
                    new AtomFeedData("https://www.engadget.com/rss.xml", Color.WHITE, Color.BLACK),
                    new AtomFeedData("http://rss.nytimes.com/services/xml/rss/nyt/Technology.xml", Color.WHITE, Color.BLACK),
                    new AtomFeedData("https://www.xda-developers.com/feed/", Color.parseColor("#f59714"), Color.BLACK),
                    new AtomFeedData("https://www.wired.com/feed", Color.parseColor("#BDBDBD"), Color.BLACK),
                    new AtomFeedData("https://www.techradar.com/rss", Color.parseColor("#2f6e91"), Color.WHITE),
                    new AtomFeedData("https://techcrunch.com/feed/", Color.parseColor("#00a562"), Color.WHITE)
            ));
        }

        feedage = new FeedageLib(
                new HackyCacheInterface(prefs),
                false,
                feeds.toArray(new FeedData[feeds.size()])
        );

        ServiceUtils.startService(this, new Intent(this, SyncService.class));
        getterTask = new ArticlesGetterTask(this);
        getterTask.execute();
    }

    public SharedPreferences getPrefs() {
        return prefs;
    }

    public int getThemeRes() {
        int theme = getThemePreference();
        if (theme == THEME_DARK)
            return R.style.AppTheme_Dark;
        else if (theme == THEME_AMOLED)
            return R.style.AppTheme_AMOLED;
        else return R.style.AppTheme;
    }

    public int getThemePreference() {
        return prefs.getInt(PREF_THEME, THEME_LIGHT);
    }

    public void setSyncTime(int syncTime) {
        prefs.edit().putInt(PREF_SYNC_TIME, syncTime).apply();
        ServiceUtils.startService(this, new Intent(this, SyncService.class));
    }

    public int getSyncTime() {
        return prefs.getInt(PREF_SYNC_TIME, 0);
    }

    public int getTextColorPrimary() {
        return ContextCompat.getColor(this, getThemePreference() == THEME_LIGHT ? R.color.textColorPrimary : R.color.textColorPrimaryInverse);
    }

    public int getTextColorSecondary() {
        return ContextCompat.getColor(this, getThemePreference() == THEME_LIGHT ? R.color.textColorSecondary : R.color.textColorSecondaryInverse);
    }

    public int getTextColorTertiary() {
        return ContextCompat.getColor(this, getThemePreference() == THEME_LIGHT ? R.color.textColorTertiary : R.color.textColorTertiaryInverse);
    }

    public int getTextColorPrimaryInverse() {
        return ContextCompat.getColor(this, getThemePreference() != THEME_LIGHT ? R.color.textColorPrimary : R.color.textColorPrimaryInverse);
    }

    public int getTextColorSecondaryInverse() {
        return ContextCompat.getColor(this, getThemePreference() != THEME_LIGHT ? R.color.textColorSecondary : R.color.textColorSecondaryInverse);
    }

    public int getTextColorTertiaryInverse() {
        return ContextCompat.getColor(this, getThemePreference() != THEME_LIGHT ? R.color.textColorTertiary : R.color.textColorTertiaryInverse);
    }

    public void setTheme(int theme) {
        prefs.edit().putInt(PREF_THEME, theme).apply();
        for (OnPreferenceListener listener : preferenceListeners)
            listener.onThemeChanged();
    }

    public void addOnPreferenceListener(OnPreferenceListener listener) {
        preferenceListeners.add(listener);
    }

    public void removeOnPreferenceListener(OnPreferenceListener listener) {
        preferenceListeners.remove(listener);
    }

    public void addListener(FeedageLib.OnCategoriesUpdatedListener listener) {
        listeners.add(listener);
    }

    public void removeListener(FeedageLib.OnCategoriesUpdatedListener listener) {
        listeners.remove(listener);
    }

    public void setProgressListener(OnProgressUpdateListener listener) {
        progressListener = listener;
    }

    public void getNext() {
        feedage.getNext(this);
        isLoading = true;
    }

    public void setFeeds(List<FeedData> feeds) {
        this.feeds = feeds;
        PreferenceUtils.putFeedList(prefs.edit(), PREF_FEEDS, feeds).apply();

        for (OnPreferenceListener listener : preferenceListeners)
            listener.onFeedsChanged();
    }

    public List<FeedData> getFeeds() {
        return feeds;
    }

    public List<CategoryData> getCategories() {
        return categories;
    }

    public List<PostData> getBookmarks() {
        return bookmarks;
    }

    public void setBookmarked(PostData post, boolean isBookmarked) {
        if (isBookmarked)
            bookmarks.add(post);
        else bookmarks.remove(post);

        PreferenceUtils.putPostList(prefs.edit(), PREF_BOOKMARKS, bookmarks).apply();

        for (OnPreferenceListener listener : preferenceListeners)
            listener.onBookmarksChanged();
    }

    public boolean isBookmarked(PostData post) {
        return bookmarks.contains(post);
    }

    public boolean isLoading() {
        return isLoading;
    }

    @Override
    public void onFeedsUpdated(final List<FeedData> feeds) {
        onFeedsUpdated(feeds, true);
    }

    public void onFeedsUpdated(final List<FeedData> feeds, final boolean shouldSave) {
        this.feeds = feeds;
        new Handler(Looper.getMainLooper()).post(() -> {
            if (shouldSave) {
                if (putterTask != null)
                    putterTask.cancel(true);

                putterTask = new ArticlesPutterTask(Feedage.this);
                putterTask.execute();
            }

            for (FeedageLib.OnCategoriesUpdatedListener listener : listeners)
                listener.onFeedsUpdated(feeds);

            onProgressUpdate(feeds);
        });
    }

    @Override
    public void onCategoriesUpdated(final List<CategoryData> categories) {
        onCategoriesUpdated(categories, true);
    }

    public void onCategoriesUpdated(final List<CategoryData> categories, boolean shouldSave) {
        this.categories = categories;
        new Handler(Looper.getMainLooper()).post(() -> {
            if (shouldSave) {
                if (putterTask != null)
                    putterTask.cancel(true);

                putterTask = new ArticlesPutterTask(Feedage.this);
                putterTask.execute();
            }

            for (FeedageLib.OnCategoriesUpdatedListener listener : listeners)
                listener.onCategoriesUpdated(categories);

            onProgressUpdate(feeds);
        });
    }

    private void onProgressUpdate(List<FeedData> feeds) {
        int loaded = 0;
        for (FeedData feed : feeds) {
            if (!feed.isLoading())
                loaded++;
        }

        isLoading = loaded == feeds.size();
        if (progressListener != null)
            progressListener.onProgressUpdate(isLoading, (float) loaded / feeds.size());
    }

    public interface OnProgressUpdateListener {
        void onProgressUpdate(boolean isLoading, float progress);
    }

    public interface OnPreferenceListener {
        void onThemeChanged();

        void onFeedsChanged();

        void onBookmarksChanged();
    }
}
