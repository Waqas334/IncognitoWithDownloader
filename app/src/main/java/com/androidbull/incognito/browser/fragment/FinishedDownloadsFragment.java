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

package com.androidbull.incognito.browser.fragment;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.CheckBox;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProviders;

import com.androidbull.incognito.R;
import com.androidbull.incognito.browser.adapter.DownloadItem;
import com.androidbull.incognito.browser.adapter.DownloadListAdapter;
import com.androidbull.incognito.browser.core.StatusCode;
import com.androidbull.incognito.browser.core.entity.DownloadInfo;
import com.androidbull.incognito.browser.core.utils.Utils;
import com.androidbull.incognito.browser.dialog.BaseAlertDialog;

import java.util.Collections;

import io.reactivex.disposables.Disposable;

public class FinishedDownloadsFragment extends DownloadsFragment
    implements DownloadListAdapter.FinishClickListener
{
    @SuppressWarnings("unused")
    private static final String TAG = FinishedDownloadsFragment.class.getSimpleName();

    private static final String TAG_DELETE_DOWNLOAD_DIALOG = "delete_download_dialog";
    private static final String TAG_DOWNLOAD_FOR_DELETION = "download_for_deletion";

    private BaseAlertDialog deleteDownloadDialog;
    private BaseAlertDialog.SharedViewModel dialogViewModel;
    private DownloadInfo downloadForDeletion;

    public static FinishedDownloadsFragment newInstance()
    {
        FinishedDownloadsFragment fragment = new FinishedDownloadsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);

        return fragment;
    }

    public FinishedDownloadsFragment()
    {
        super((item) -> {
            return StatusCode.isStatusCompleted(item.info.statusCode);
        });
    }

    @Override
    public void onStart()
    {
        super.onStart();

        subscribeAdapter();
        subscribeAlertDialog();
    }

    private void subscribeAlertDialog()
    {
        Disposable d = dialogViewModel.observeEvents()
                .subscribe((event) -> {
                    if (!event.dialogTag.equals(TAG_DELETE_DOWNLOAD_DIALOG) || deleteDownloadDialog == null)
                        return;
                    switch (event.type) {
                        case POSITIVE_BUTTON_CLICKED:
                            //This alert dialog is shown when we select a file by the 3 dots menu on it and select delete
                            Log.d("DELETE_TASK","FinishedDownloadsFragment: Positive Button Clicked ");
                            Dialog dialog = deleteDownloadDialog.getDialog();
                            if (dialog != null && downloadForDeletion != null) {
                                CheckBox withFile = dialog.findViewById(R.id.delete_with_file);
                                deleteDownload(downloadForDeletion, withFile.isChecked());
                            }
                        case NEGATIVE_BUTTON_CLICKED:
                            downloadForDeletion = null;
                            deleteDownloadDialog.dismiss();
                            break;
                    }
                });
        disposables.add(d);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState != null)
            downloadForDeletion = savedInstanceState.getParcelable(TAG_DOWNLOAD_FOR_DELETION);

        FragmentManager fm = getFragmentManager();
        if (fm != null)
            deleteDownloadDialog = (BaseAlertDialog)fm.findFragmentByTag(TAG_DELETE_DOWNLOAD_DIALOG);
        dialogViewModel = ViewModelProviders.of(activity).get(BaseAlertDialog.SharedViewModel.class);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState)
    {
   //     outState.putParcelable(TAG_DOWNLOAD_FOR_DELETION, downloadForDeletion);

        super.onSaveInstanceState(outState);
    }

    @Override
    public void onItemClicked(@NonNull DownloadItem item)
    {
        startActivity(Intent.createChooser(
                Utils.createOpenFileIntent(activity.getApplicationContext(), item.info),
                getString(R.string.open_using)));
    }

    @Override
    public void onItemMenuClicked(int menuId, @NonNull DownloadItem item)
    {
        switch (menuId) {
            case R.id.delete_menu:
                downloadForDeletion = item.info;
                showDeleteDownloadDialog();
                break;
            case R.id.open_details_menu:
                showDetailsDialog(item.info.id);
                break;
            case R.id.share_menu:
                shareDownload(item);
                break;
            case R.id.share_url_menu:
                shareUrl(item);
                break;
        }
    }

    private void showDeleteDownloadDialog()
    {
        FragmentManager fm = getFragmentManager();
        if (fm != null && fm.findFragmentByTag(TAG_DELETE_DOWNLOAD_DIALOG) == null) {
            deleteDownloadDialog = BaseAlertDialog.newInstance(
                    getString(R.string.deleting),
                    getString(R.string.delete_selected_download),
                    R.layout.dialog_delete_downloads,
                    getString(R.string.ok),
                    getString(R.string.cancel),
                    null,
                    false);

            deleteDownloadDialog.show(fm, TAG_DELETE_DOWNLOAD_DIALOG);
        }
    }

    private void deleteDownload(DownloadInfo info, boolean withFile)
    {
        viewModel.deleteDownload(info, withFile);
    }

    private void shareDownload(DownloadItem item)
    {
        startActivity(Intent.createChooser(
                Utils.makeFileShareIntent(activity.getApplicationContext(), Collections.singletonList(item)),
                getString(R.string.share_via)));
    }

    private void shareUrl(DownloadItem item)
    {
        startActivity(Intent.createChooser(
                Utils.makeShareUrlIntent(item.info.url),
                getString(R.string.share_via)));
    }
}
