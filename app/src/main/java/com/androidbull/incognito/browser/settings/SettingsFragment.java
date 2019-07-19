/*
 * Copyright (C) 2019 Tachibana General Laboratories, LLC
 * Copyright (C) 2019 Yaroslav Pronin <proninyaroslav@mail.ru>
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

package com.androidbull.incognito.browser.settings;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProviders;
import androidx.preference.Preference;

import com.androidbull.incognito.R;
import com.androidbull.incognito.browser.core.utils.Utils;
import com.androidbull.incognito.browser.viewmodel.settings.SettingsViewModel;
import com.takisoft.preferencex.PreferenceFragmentCompat;

public class SettingsFragment extends PreferenceFragmentCompat
{
    @SuppressWarnings("unused")
    private static final String TAG = SettingsFragment.class.getSimpleName();

    private static final String AppearanceSettings = "AppearanceSettingsFragment";
    private static final String BehaviorSettings = "BehaviorSettingsFragment";
    private static final String StorageSettings = "StorageSettingsFragment";

    private AppCompatActivity activity;
    private SettingsViewModel viewModel;

    public static SettingsFragment newInstance()
    {
        SettingsFragment fragment = new SettingsFragment();

        fragment.setArguments(new Bundle());

        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context)
    {
        super.onAttach(context);

        if (context instanceof AppCompatActivity)
            activity = (AppCompatActivity)context;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        if (activity == null)
            activity = (AppCompatActivity)getActivity();

        viewModel = ViewModelProviders.of(activity).get(SettingsViewModel.class);

        if (Utils.isTwoPane(activity)) {
            Fragment f = activity.getSupportFragmentManager()
                    .findFragmentById(R.id.detail_fragment_container);
            //TODO PREFERNCE EDIT HERE
//
//            if (f == null)
//                setFragment(AppearanceSettingsFragment.newInstance(),
//                        getString(R.string.pref_header_appearance));
        }

        //TODO PREFERNCE EDIT HERE
//        Preference appearance = findPreference(AppearanceSettingsFragment.class.getSimpleName());
//        appearance.setOnPreferenceClickListener(prefClickListener);

        Preference behavior = findPreference(BehaviorSettingsFragment.class.getSimpleName());
        behavior.setOnPreferenceClickListener(prefClickListener);

        Preference storage = findPreference(StorageSettingsFragment.class.getSimpleName());
        storage.setOnPreferenceClickListener(prefClickListener);
    }

    private Preference.OnPreferenceClickListener prefClickListener = (preference) -> {
        openPreference(preference.getKey());
        return true;
    };

    private void openPreference(String prefName)
    {
        switch (prefName) {
            //TODO PREFERNCE EDIT HERE

//            case AppearanceSettings:
//                if (Utils.isLargeScreenDevice(getActivity())) {
//                    setFragment(AppearanceSettingsFragment.newInstance(),
//                            getString(R.string.pref_header_appearance));
//                } else {
//                    startActivity(AppearanceSettingsFragment.class,
//                            getString(R.string.pref_header_appearance));
//                }
//                break;
            case BehaviorSettings:
                if (Utils.isLargeScreenDevice(getActivity())) {
                    setFragment(BehaviorSettingsFragment.newInstance(),
                            getString(R.string.pref_header_behavior));
                } else {
                    startActivity(BehaviorSettingsFragment.class,
                            getString(R.string.pref_header_behavior));
                }
                break;
            case StorageSettings:
                if (Utils.isLargeScreenDevice(getActivity())) {
                    setFragment(StorageSettingsFragment.newInstance(),
                            getString(R.string.pref_header_storage));
                } else {
                    startActivity(StorageSettingsFragment.class,
                            getString(R.string.pref_header_storage));
                }
                break;
        }
    }

    @Override
    public void onCreatePreferencesFix(Bundle savedInstanceState, String rootKey)
    {
        setPreferencesFromResource(R.xml.pref_headers, rootKey);
    }

    private <F extends PreferenceFragmentCompat> void setFragment(F fragment, String title)
    {
        viewModel.detailTitleChanged.setValue(title);

        if (Utils.isLargeScreenDevice(activity)) {
            activity.getSupportFragmentManager().beginTransaction()
                    .replace(R.id.detail_fragment_container, fragment)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    .commit();
        }
    }

    private <F extends PreferenceFragmentCompat> void startActivity(Class<F> fragment, String title)
    {
        Intent i = new Intent(activity, PreferenceActivity.class);
        PreferenceActivityConfig config = new PreferenceActivityConfig(
                fragment.getSimpleName(),
                title);

        i.putExtra(PreferenceActivity.TAG_CONFIG, config);
        startActivity(i);
    }
}
