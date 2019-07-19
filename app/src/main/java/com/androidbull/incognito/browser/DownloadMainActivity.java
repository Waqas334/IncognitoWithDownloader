/*
 * Copyright (C) 2018, 2019 Tachibana General Laboratories, LLC
 * Copyright (C) 2018, 2019 Yaroslav Pronin <proninyaroslav@mail.ru>
 *
 * This file is part of Download Navi.
 *
 * Download Navi is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Download Navi is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Download Navi.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.androidbull.incognito.browser;

import android.app.Dialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.androidbull.incognito.R;
import com.androidbull.incognito.browser.adapter.DownloadListPagerAdapter;
import com.androidbull.incognito.browser.adapter.drawer.DrawerExpandableAdapter;
import com.androidbull.incognito.browser.adapter.drawer.DrawerGroup;
import com.androidbull.incognito.browser.adapter.drawer.DrawerGroupItem;
import com.androidbull.incognito.browser.core.DownloadEngine;
import com.androidbull.incognito.browser.core.utils.Utils;
import com.androidbull.incognito.browser.dialog.BaseAlertDialog;
import com.androidbull.incognito.browser.receiver.NotificationReceiver;
import com.androidbull.incognito.browser.service.DownloadService;
import com.androidbull.incognito.browser.settings.DownloadSettingsActivity;
import com.androidbull.incognito.browser.settings.SettingsPrefrence;
import com.androidbull.incognito.browser.ui.MainActivity;
import com.androidbull.incognito.browser.viewmodel.DownloadsViewModel;
import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.AdListener;
import com.facebook.ads.AdSize;
import com.facebook.ads.AdView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;
import com.h6ah4i.android.widget.advrecyclerview.animator.GeneralItemAnimator;
import com.h6ah4i.android.widget.advrecyclerview.animator.RefactoredDefaultItemAnimator;
import com.h6ah4i.android.widget.advrecyclerview.expandable.RecyclerViewExpandableItemManager;

import java.util.List;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

import static com.androidbull.incognito.browser.others.Constants.FULL_SCREEN_STATE;

public class DownloadMainActivity extends AppCompatActivity {
    @SuppressWarnings("unused")
    private static final String TAG = DownloadMainActivity.class.getSimpleName();

    private static final String TAG_PERM_DIALOG_IS_SHOW = "perm_dialog_is_show";
    private static final String TAG_ABOUT_DIALOG = "about_dialog";

    /* Android data binding doesn't work with layout aliases */
    private Toolbar toolbar;

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ActionBarDrawerToggle toggle;
    private RecyclerView drawerItemsList;
    private LinearLayoutManager layoutManager;
    private DrawerExpandableAdapter drawerAdapter;
    private RecyclerView.Adapter wrappedDrawerAdapter;
    private RecyclerViewExpandableItemManager drawerItemManager;

    private TabLayout tabLayout;
    private ViewPager viewPager;
    private DownloadListPagerAdapter pagerAdapter;
    private DownloadsViewModel fragmentViewModel;
    private FloatingActionButton fab;
    private SearchView searchView;
    private boolean permDialogIsShow = false;
    private DownloadEngine engine;
    protected CompositeDisposable disposables = new CompositeDisposable();
    private BaseAlertDialog.SharedViewModel dialogViewModel;
    private BaseAlertDialog aboutDialog;
    private RelativeLayout adContainer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(Utils.getAppTheme(getApplicationContext()));
        super.onCreate(savedInstanceState);
        Log.d("PACKAGE_NAME_TESTING", getPackageName());
        if (getIntent().getAction() != null &&
                getIntent().getAction().equals(NotificationReceiver.NOTIFY_ACTION_SHUTDOWN_APP)) {
            finish();
            return;
        }

        fragmentViewModel = ViewModelProviders.of(this).get(DownloadsViewModel.class);
        dialogViewModel = ViewModelProviders.of(this).get(BaseAlertDialog.SharedViewModel.class);
        aboutDialog = (BaseAlertDialog) getSupportFragmentManager().findFragmentByTag(TAG_ABOUT_DIALOG);

        if (savedInstanceState != null)
            permDialogIsShow = savedInstanceState.getBoolean(TAG_PERM_DIALOG_IS_SHOW);

        if (!Utils.checkStoragePermission(getApplicationContext()) && !permDialogIsShow) {
            permDialogIsShow = true;
            startActivity(new Intent(this, RequestPermissions.class));
        }

        setContentView(R.layout.activity_main_download);

        engine = ((MainApplication) getApplication()).getDownloadEngine();

        initLayout();
        loadBannerAd();
        engine.restoreDownloads();

        Log.d(TAG, "onCreate: isPaid: " + MainActivity.isPaid);
        restoreFullScreenSettings();
    }


    private void restoreFullScreenSettings() {
        WindowManager.LayoutParams attrs = getWindow().getAttributes();
        if (SettingsPrefrence.getFullScreenState(FULL_SCREEN_STATE, this)) {
            attrs.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
        } else {
            attrs.flags &= ~WindowManager.LayoutParams.FLAG_FULLSCREEN;
        }
        getWindow().setAttributes(attrs);
    }

    private void loadBannerAd() {
        if (!MainActivity.isPaid) {
            String adId = (String) MainActivity.firebaseDefaultHashMap.get(com.androidbull.incognito.browser.others.Utils.Constant.DOWNLOAD_BANNER_AD);
            Log.d(TAG, "loadBannerAd: adUnitId: " + adId);
            AdView adView = new AdView(this, adId, AdSize.BANNER_HEIGHT_50);
            adView.loadAd();
            adView.setAdListener(new AdListener() {
                @Override
                public void onError(Ad ad, AdError adError) {
                    Log.d(TAG, "onError: " + adError.getErrorMessage());
                }

                @Override
                public void onAdLoaded(Ad ad) {

                }

                @Override
                public void onAdClicked(Ad ad) {

                }

                @Override
                public void onLoggingImpression(Ad ad) {

                }
            });
            adContainer.addView(adView);
        } else {
            if (adContainer.getVisibility() == View.VISIBLE) {
                adContainer.setVisibility(View.GONE);
            }
        }
    }

    private void initLayout() {
        toolbar = findViewById(R.id.toolbar);
        navigationView = findViewById(R.id.navigation_view);
        drawerLayout = findViewById(R.id.drawer_layout);
        tabLayout = findViewById(R.id.download_list_tabs);
        viewPager = findViewById(R.id.download_list_viewpager);
        fab = findViewById(R.id.add_fab);
        drawerItemsList = findViewById(R.id.drawer_items_list);
        layoutManager = new LinearLayoutManager(this);
        adContainer = findViewById(R.id.rl_download_main_container);

        toolbar.setTitle(R.string.app_name);
        /* Disable elevation for portrait mode */
        if (!Utils.isTwoPane(this) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            toolbar.setElevation(0);
        setSupportActionBar(toolbar);

        if (drawerLayout != null) {
            toggle = new ActionBarDrawerToggle(this,
                    drawerLayout,
                    toolbar,
                    R.string.open_navigation_drawer,
                    R.string.close_navigation_drawer);
            drawerLayout.addDrawerListener(toggle);
        }
        initDrawer();
        fragmentViewModel.resetSearch();

        pagerAdapter = new DownloadListPagerAdapter(getApplicationContext(), getSupportFragmentManager());
        viewPager.setAdapter(pagerAdapter);
        viewPager.setOffscreenPageLimit(DownloadListPagerAdapter.NUM_FRAGMENTS);
        tabLayout.setupWithViewPager(viewPager);

        fab.setOnClickListener((v) ->
        {
            FacebookLogger.facebookLog(this, "Add Download was clicked from Download Activity");
            startActivity(new Intent(this, AddDownloadActivity.class));
        });
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START))
            drawerLayout.closeDrawer(GravityCompat.START);
        else
            super.onBackPressed();
        Log.d(TAG, "onBackPressed: ");
    }

    private void initDrawer() {
        drawerItemManager = new RecyclerViewExpandableItemManager(null);
        drawerItemManager.setDefaultGroupsExpandedState(false);
        drawerItemManager.setOnGroupCollapseListener((groupPosition, fromUser, payload) -> {
            if (fromUser)
                saveGroupExpandState(groupPosition, false);
        });
        drawerItemManager.setOnGroupExpandListener((groupPosition, fromUser, payload) -> {
            if (fromUser)
                saveGroupExpandState(groupPosition, true);
        });
        GeneralItemAnimator animator = new RefactoredDefaultItemAnimator();
        /*
         * Change animations are enabled by default since support-v7-recyclerview v22.
         * Need to disable them when using animation indicator.
         */
        animator.setSupportsChangeAnimations(false);

        List<DrawerGroup> groups = Utils.getNavigationDrawerItems(this,
                PreferenceManager.getDefaultSharedPreferences(this));
        drawerAdapter = new DrawerExpandableAdapter(groups, drawerItemManager, this::onDrawerItemSelected);
        wrappedDrawerAdapter = drawerItemManager.createWrappedAdapter(drawerAdapter);
        onDrawerGroupsCreated();

        drawerItemsList.setLayoutManager(layoutManager);
        drawerItemsList.setAdapter(wrappedDrawerAdapter);
        drawerItemsList.setItemAnimator(animator);
        drawerItemsList.setHasFixedSize(false);

        drawerItemManager.attachRecyclerView(drawerItemsList);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putBoolean(TAG_PERM_DIALOG_IS_SHOW, permDialogIsShow);

        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        if (toggle != null)
            toggle.syncState();
    }

    @Override
    public void onStart() {
        super.onStart();

        subscribeAlertDialog();
    }

    @Override
    protected void onStop() {
        super.onStop();

        disposables.clear();
    }

    private void subscribeAlertDialog() {
        Disposable d = dialogViewModel.observeEvents()
                .subscribe((event) -> {
                    if (!event.dialogTag.equals(TAG_ABOUT_DIALOG))
                        return;
                    switch (event.type) {
                        case NEGATIVE_BUTTON_CLICKED:
                            openChangelogLink();
                            break;
                        case DIALOG_SHOWN:
                            initAboutDialog();
                            break;
                    }
                });
        disposables.add(d);
    }

    private void onDrawerGroupsCreated() {
        for (int pos = 0; pos < drawerAdapter.getGroupCount(); pos++) {
            DrawerGroup group = drawerAdapter.getGroup(pos);
            if (group == null)
                return;

            Resources res = getResources();
            if (group.id == res.getInteger(R.integer.drawer_category_id)) {
                fragmentViewModel.setCategoryFilter(
                        Utils.getDrawerGroupCategoryFilter(this, group.getSelectedItemId()), false);

            } else if (group.id == res.getInteger(R.integer.drawer_status_id)) {
                fragmentViewModel.setStatusFilter(
                        Utils.getDrawerGroupStatusFilter(this, group.getSelectedItemId()), false);

            } else if (group.id == res.getInteger(R.integer.drawer_date_added_id)) {
                fragmentViewModel.setDateAddedFilter(
                        Utils.getDrawerGroupDateAddedFilter(this, group.getSelectedItemId()), false);

            } else if (group.id == res.getInteger(R.integer.drawer_sorting_id)) {
                fragmentViewModel.setSort(Utils.getDrawerGroupItemSorting(this, group.getSelectedItemId()), false);
            }

            applyExpandState(group, pos);
        }
    }

    private void applyExpandState(DrawerGroup group, int pos) {
        if (group.getDefaultExpandState())
            drawerItemManager.expandGroup(pos);
        else
            drawerItemManager.collapseGroup(pos);
    }

    private void saveGroupExpandState(int groupPosition, boolean expanded) {
        DrawerGroup group = drawerAdapter.getGroup(groupPosition);
        if (group == null)
            return;

        Resources res = getResources();
        String prefKey = null;
        if (group.id == res.getInteger(R.integer.drawer_category_id))
            prefKey = getString(R.string.drawer_category_is_expanded);

        else if (group.id == res.getInteger(R.integer.drawer_status_id))
            prefKey = getString(R.string.drawer_status_is_expanded);

        else if (group.id == res.getInteger(R.integer.drawer_date_added_id))
            prefKey = getString(R.string.drawer_time_is_expanded);

        else if (group.id == res.getInteger(R.integer.drawer_sorting_id))
            prefKey = getString(R.string.drawer_sorting_is_expanded);

        if (prefKey != null)
            PreferenceManager.getDefaultSharedPreferences(this)
                    .edit()
                    .putBoolean(prefKey, expanded)
                    .apply();
    }

    private void onDrawerItemSelected(DrawerGroup group, DrawerGroupItem item) {
        Resources res = getResources();
        String prefKey = null;
        if (group.id == res.getInteger(R.integer.drawer_category_id)) {
            prefKey = getString(R.string.drawer_category_selected_item);
            fragmentViewModel.setCategoryFilter(
                    Utils.getDrawerGroupCategoryFilter(this, item.id), true);

        } else if (group.id == res.getInteger(R.integer.drawer_status_id)) {
            prefKey = getString(R.string.drawer_status_selected_item);
            fragmentViewModel.setStatusFilter(
                    Utils.getDrawerGroupStatusFilter(this, item.id), true);

        } else if (group.id == res.getInteger(R.integer.drawer_date_added_id)) {
            prefKey = getString(R.string.drawer_time_selected_item);
            fragmentViewModel.setDateAddedFilter(
                    Utils.getDrawerGroupDateAddedFilter(this, item.id), true);

        } else if (group.id == res.getInteger(R.integer.drawer_sorting_id)) {
            prefKey = getString(R.string.drawer_sorting_selected_item);
            fragmentViewModel.setSort(Utils.getDrawerGroupItemSorting(this, item.id), true);
        }

        if (prefKey != null)
            saveSelectionState(prefKey, item);
    }

    private void saveSelectionState(String prefKey, DrawerGroupItem item) {
        PreferenceManager.getDefaultSharedPreferences(this)
                .edit()
                .putLong(prefKey, item.id)
                .apply();
    }

    ;


    private void initSearch() {
        searchView.setMaxWidth(Integer.MAX_VALUE);
        searchView.setOnCloseListener(() -> {
            fragmentViewModel.resetSearch();

            return false;
        });
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                fragmentViewModel.setSearchQuery(query);
                /* Submit the search will hide the keyboard */
                searchView.clearFocus();

                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                fragmentViewModel.setSearchQuery(newText);

                return true;
            }
        });
        searchView.setQueryHint(getString(R.string.search));
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        /* Assumes current activity is the searchable activity */
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.download_menu, menu);

        searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        initSearch();

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.pause_all_menu:
                pauseAll();
                break;
            case R.id.resume_all_menu:
                resumeAll();
                break;
            case R.id.settings_menu:
                startActivity(new Intent(this, DownloadSettingsActivity.class));
                break;
//            case R.id.about_menu:
//                showAboutDialog();
//                break;
            case R.id.shutdown_app_menu:
                closeOptionsMenu();
                shutdown();
                break;
        }

        return true;
    }

    private void pauseAll() {
        engine.pauseAllDownloads();
    }

    private void resumeAll() {
        engine.resumeDownloads(false);
    }

   /* private void showAboutDialog() {
        FragmentManager fm = getSupportFragmentManager();
        if (fm.findFragmentByTag(TAG_ABOUT_DIALOG) == null) {
            aboutDialog = BaseAlertDialog.newInstance(
                    getString(R.string.about_title),
                    null,
                    R.layout.dialog_about,
                    getString(R.string.ok),
                    getString(R.string.about_changelog),
                    null,
                    true);
            aboutDialog.show(fm, TAG_ABOUT_DIALOG);
        }
    }*/

    private void initAboutDialog() {
        if (aboutDialog == null)
            return;

        Dialog dialog = aboutDialog.getDialog();
        if (dialog != null) {
            TextView versionTextView = dialog.findViewById(R.id.about_version);
            TextView descriptionTextView = dialog.findViewById(R.id.about_description);
            String versionName = Utils.getAppVersionName(this);
            if (versionName != null)
                versionTextView.setText(versionName);
            descriptionTextView.setText(Html.fromHtml(getString(R.string.about_description)));
            descriptionTextView.setMovementMethod(LinkMovementMethod.getInstance());
        }
    }

    private void openChangelogLink() {
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(getString(R.string.about_changelog_link)));
        startActivity(i);
    }

    public void shutdown() {
        Intent i = new Intent(getApplicationContext(), DownloadService.class);
        i.setAction(DownloadService.ACTION_SHUTDOWN);
        startService(i);
        finish();
    }
}
