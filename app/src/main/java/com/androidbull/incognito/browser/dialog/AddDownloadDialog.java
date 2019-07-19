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

package com.androidbull.incognito.browser.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.format.Formatter;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProviders;

import com.androidbull.incognito.R;
import com.androidbull.incognito.browser.FragmentCallback;
import com.androidbull.incognito.browser.RequestPermissions;
import com.androidbull.incognito.browser.adapter.UserAgentAdapter;
import com.androidbull.incognito.browser.core.entity.UserAgent;
import com.androidbull.incognito.browser.core.exception.FreeSpaceException;
import com.androidbull.incognito.browser.core.exception.HttpException;
import com.androidbull.incognito.browser.core.exception.NormalizeUrlException;
import com.androidbull.incognito.browser.core.utils.FileUtils;
import com.androidbull.incognito.browser.core.utils.Utils;
import com.androidbull.incognito.browser.dialog.filemanager.FileManagerConfig;
import com.androidbull.incognito.browser.dialog.filemanager.FileManagerDialog;
import com.androidbull.incognito.browser.settings.SettingsManager;
import com.androidbull.incognito.browser.viewmodel.AddDownloadViewModel;
import com.androidbull.incognito.browser.viewmodel.AddInitParams;
import com.androidbull.incognito.databinding.DialogAddDownloadBinding;
import com.google.android.material.textfield.TextInputEditText;

import java.io.IOException;
import java.net.ConnectException;
import java.net.MalformedURLException;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class AddDownloadDialog extends DialogFragment
{
    @SuppressWarnings("unused")
    private static final String TAG = AddDownloadDialog.class.getSimpleName();

    private static final String TAG_ADD_USER_AGENT_DIALOG = "add_user_agent_dialog";
    private static final int CHOOSE_PATH_TO_SAVE_REQUEST_CODE = 1;
    private static final String TAG_INIT_PARAMS = "init_params";
    private static final String TAG_PERM_DIALOG_IS_SHOW = "perm_dialog_is_show";
    private static final String TAG_CREATE_FILE_ERROR_DIALOG = "create_file_error_dialog";
    private static final String TAG_INVALID_URL_DIALOG = "invalid_url_dialog";
    private static final String TAG_OPEN_DIR_ERROR_DIALOG = "open_dir_error_dialog";

    private AlertDialog alert;
    private AppCompatActivity activity;
    private UserAgentAdapter userAgentAdapter;
    private AddDownloadViewModel viewModel;
    private BaseAlertDialog addUserAgentDialog;
    private BaseAlertDialog.SharedViewModel dialogViewModel;
    private DialogAddDownloadBinding binding;
    private CompositeDisposable disposables = new CompositeDisposable();
    private boolean permDialogIsShow = false;

    public static AddDownloadDialog newInstance(@NonNull AddInitParams initParams)
    {
        AddDownloadDialog frag = new AddDownloadDialog();

        Bundle args = new Bundle();
        args.putParcelable(TAG_INIT_PARAMS, initParams);
        frag.setArguments(args);

        return frag;
    }

    @Override
    public void onAttach(@NonNull Context context)
    {
        super.onAttach(context);

        if (context instanceof AppCompatActivity)
            activity = (AppCompatActivity)context;
    }

    @Override
    public void onResume()
    {
        super.onResume();

        /* Back button handle */
        getDialog().setOnKeyListener((DialogInterface dialog, int keyCode, KeyEvent event) -> {
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                if (event.getAction() != KeyEvent.ACTION_DOWN) {
                    return true;
                } else {
                    onBackPressed();
                    return true;
                }
            } else {
                return false;
            }
        });
    }

    @Override
    public void onStop()
    {
        super.onStop();

        disposables.clear();
    }

    @Override
    public void onStart()
    {
        super.onStart();

        subscribeAlertDialog();
    }

    private void subscribeAlertDialog()
    {
        Disposable d = dialogViewModel.observeEvents().subscribe(this::handleAlertDialogEvent);
        disposables.add(d);
    }

    private void handleAlertDialogEvent(BaseAlertDialog.Event event)
    {
        if (!event.dialogTag.equals(TAG_ADD_USER_AGENT_DIALOG) || addUserAgentDialog == null)
            return;
        switch (event.type) {
            case POSITIVE_BUTTON_CLICKED:
                Dialog dialog = addUserAgentDialog.getDialog();
                if (dialog != null) {
                    TextInputEditText editText = dialog.findViewById(R.id.text_input_dialog);
                    Editable e = editText.getText();
                    String userAgent = (e == null ? null : e.toString());
                    if (!TextUtils.isEmpty(userAgent))
                        disposables.add(viewModel.addUserAgent(new UserAgent(userAgent))
                                .subscribeOn(Schedulers.io())
                                .subscribe());
                }
            case NEGATIVE_BUTTON_CLICKED:
                addUserAgentDialog.dismiss();
                break;
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        viewModel = ViewModelProviders.of(activity).get(AddDownloadViewModel.class);
        dialogViewModel = ViewModelProviders.of(activity).get(BaseAlertDialog.SharedViewModel.class);

        AddInitParams initParams = getArguments().getParcelable(TAG_INIT_PARAMS);
        /* Clear init params */
        getArguments().putParcelable(TAG_INIT_PARAMS, null);
        if (initParams != null)
            initParams(initParams);

        if (savedInstanceState != null)
            permDialogIsShow = savedInstanceState.getBoolean(TAG_PERM_DIALOG_IS_SHOW);

        if (!Utils.checkStoragePermission(activity.getApplicationContext()) && !permDialogIsShow) {
            permDialogIsShow = true;
            startActivity(new Intent(activity, RequestPermissions.class));
        }
    }

    private void initParams(AddInitParams initParams)
    {

        Log.d("DOWNLOAD_TESTING","What we got in intent: " + initParams);
        if (TextUtils.isEmpty(initParams.url)) {
            /* Inserting a link from the clipboard */
            String clipboard = Utils.getClipboard(activity.getApplicationContext());
            if (clipboard != null) {
                String c = clipboard.toLowerCase();
                if (c.startsWith(Utils.HTTP_PREFIX))
                    initParams.url = clipboard;
            }
        }
        viewModel.params.setUrl(initParams.url);
        viewModel.params.setFileName(initParams.fileName);
        viewModel.params.setDescription(initParams.description);
        viewModel.params.setUserAgent(initParams.userAgent == null ?
                viewModel.getPrefUserAgent().userAgent :
                initParams.userAgent);
        viewModel.params.setDirPath(initParams.dirPath == null ?
                Uri.parse(FileUtils.getDefaultDownloadPath()) :
                initParams.dirPath);
        viewModel.params.setUnmeteredConnectionsOnly(initParams.unmeteredConnectionsOnly);
        viewModel.params.setRetry(initParams.retry);
        viewModel.params.setReplaceFile(initParams.replaceFile);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        if (activity == null)
            activity = (AppCompatActivity)getActivity();

        FragmentManager fm = getFragmentManager();
        if (fm != null)
            addUserAgentDialog = (BaseAlertDialog)fm.findFragmentByTag(TAG_ADD_USER_AGENT_DIALOG);

        LayoutInflater i = LayoutInflater.from(activity);
        binding = DataBindingUtil.inflate(i, R.layout.dialog_add_download, null, false);
        binding.setViewModel(viewModel);

        initLayoutView();

        return alert;
    }

    private void initLayoutView()
    {
        binding.expansionHeader.setOnClickListener((View view) -> {
            binding.advancedLayout.toggle();
            binding.expansionHeader.toggleExpand();
        });

        binding.piecesNumberSelect.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
        {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
            {
                /* Increment because progress starts from zero */
                viewModel.params.setNumPieces(++progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { /* Nothing */}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { /* Nothing */}
        });
        binding.piecesNumberValue.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s)
            {
                if (TextUtils.isEmpty(s))
                    return;

                int numPieces;
                try {
                    numPieces = Integer.parseInt(s.toString());

                } catch (NumberFormatException e) {
                    binding.piecesNumberValue.setText(String.valueOf(viewModel.maxNumPieces));
                    return;
                }

                viewModel.params.setNumPieces(numPieces);
            }
        });
        binding.piecesNumberValue.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus && TextUtils.isEmpty(binding.piecesNumberValue.getText()))
                binding.piecesNumberValue.setText(String.valueOf(viewModel.params.getNumPieces()));
        });
        binding.link.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s)
            {
                binding.layoutLink.setErrorEnabled(false);
                binding.layoutLink.setError(null);
            }
        });
        binding.name.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s)
            {
                binding.layoutName.setErrorEnabled(false);
                binding.layoutName.setError(null);
            }
        });

        binding.folderChooserButton.setOnClickListener((v) -> showChooseDirDialog());

        userAgentAdapter = new UserAgentAdapter(activity, (userAgent) -> {
            disposables.add(viewModel.deleteUserAgent(userAgent)
                    .subscribeOn(Schedulers.io())
                    .subscribe());
        });
        viewModel.observeUserAgents().observe(this, (userAgentList) -> {
            int curUserAgentPos = -1;
            String curUserAgent = viewModel.params.getUserAgent();
            if (curUserAgent != null) {
                for (int i = 0; i < userAgentList.size(); i++) {
                    if (curUserAgent.equals(userAgentList.get(i).userAgent)) {
                        curUserAgentPos = i;
                        break;
                    }
                }
            }
            /* Add non-existent agent and make it read only */
            if (curUserAgentPos < 0 && curUserAgent != null) {
                UserAgent userAgent = new UserAgent(curUserAgent);
                userAgent.readOnly = true;
                userAgentList.add(userAgent);
                curUserAgentPos = userAgentList.size() - 1;
            }

            userAgentAdapter.clear();
            userAgentAdapter.addAll(userAgentList);
            binding.userAgent.setSelection(curUserAgentPos);
        });

        binding.userAgent.setAdapter(userAgentAdapter);
        binding.userAgent.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l)
            {
                Object item = binding.userAgent.getItemAtPosition(i);
                if (item != null) {
                    viewModel.params.setUserAgent(((UserAgent)item).userAgent);
                    viewModel.savePrefUserAgent(((UserAgent)item));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView)
            {
                /* Nothing */
            }
        });
        binding.addUserAgent.setOnClickListener((View v) -> addUserAgentDialog());

        binding.piecesNumberSelect.setEnabled(false);

        initAlertDialog(binding.getRoot());
    }

    private void initAlertDialog(View view)
    {
        alert = new AlertDialog.Builder(activity)
                .setTitle(R.string.add_download)
                .setPositiveButton(R.string.connect, null)
                .setNegativeButton(R.string.add, null)
                .setNeutralButton(R.string.cancel, null)
                .setView(view)
                .create();

        alert.setCanceledOnTouchOutside(false);
        alert.setOnShowListener((DialogInterface dialog) -> {
            viewModel.fetchState.observe(AddDownloadDialog.this, (state) -> {
                switch (state.status) {
                    case FETCHING:
                        onFetching();
                        break;
                    case ERROR:
                        onFetched();
                        showFetchError(state.error);
                        break;
                    case FETCHED:
                        onFetched();
                        break;
                    case UNKNOWN:
                        doAutoFetch();
                        break;
                }
            });

            Button connectButton = alert.getButton(AlertDialog.BUTTON_POSITIVE);
            Button addButton = alert.getButton(AlertDialog.BUTTON_NEGATIVE);
            Button cancelButton = alert.getButton(AlertDialog.BUTTON_NEUTRAL);
            connectButton.setOnClickListener((v) -> {
                AddDownloadViewModel.FetchState state = viewModel.fetchState.getValue();
                if (state == null)
                    return;
                switch (state.status) {
                    case UNKNOWN:
                    case ERROR:
                        fetchLink();
                        break;
                }
            });
            addButton.setOnClickListener((v) -> addDownload());
            cancelButton.setOnClickListener((v) ->
                    finish(new Intent(), FragmentCallback.ResultCode.CANCEL));
        });
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState)
    {
        outState.putBoolean(TAG_PERM_DIALOG_IS_SHOW, permDialogIsShow);

        super.onSaveInstanceState(outState);
    }

    private void addUserAgentDialog()
    {
        FragmentManager fm = getFragmentManager();
        if (fm != null && fm.findFragmentByTag(TAG_ADD_USER_AGENT_DIALOG) == null) {
            addUserAgentDialog = BaseAlertDialog.newInstance(
                    getString(R.string.dialog_add_user_agent_title),
                    null,
                    R.layout.dialog_text_input,
                    getString(R.string.ok),
                    getString(R.string.cancel),
                    null,
                    false);

            addUserAgentDialog.show(fm, TAG_ADD_USER_AGENT_DIALOG);
        }
    }

    private void onFetching()
    {
        hideFetchError();
        binding.link.setEnabled(false);
        binding.afterFetchLayout.setVisibility(View.GONE);
        binding.fetchProgress.show();
    }

    private void onFetched()
    {
        /* Hide connect button */
        alert.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);
        binding.link.setEnabled(false);
        binding.fetchProgress.hide();
        binding.afterFetchLayout.setVisibility(View.VISIBLE);
        binding.partialSupportWarning.setVisibility((viewModel.params.isPartialSupport() ? View.GONE : View.VISIBLE));
        binding.piecesNumber.setEnabled(viewModel.params.isPartialSupport() && viewModel.params.getTotalBytes() > 0);
        binding.piecesNumberSelect.setEnabled(viewModel.params.isPartialSupport() && viewModel.params.getTotalBytes() > 0);
    }

    private void hideFetchError()
    {
        binding.layoutLink.setErrorEnabled(false);
        binding.layoutLink.setError(null);
    }

    private boolean checkUrlField(Editable s)
    {
        if (s == null)
            return false;

        if (TextUtils.isEmpty(s)) {
            binding.layoutLink.setErrorEnabled(true);
            binding.layoutLink.setError(getString(R.string.download_error_empty_link));
            binding.layoutLink.requestFocus();

            return false;
        }

        binding.layoutLink.setErrorEnabled(false);
        binding.layoutLink.setError(null);

        return true;
    }

    private boolean checkNameField(Editable s)
    {
        if (s == null)
            return false;

        if (TextUtils.isEmpty(s)) {
            binding.layoutName.setErrorEnabled(true);
            binding.layoutName.setError(getString(R.string.download_error_empty_name));
            binding.layoutName.requestFocus();

            return false;
        }
        if (!FileUtils.isValidFatFilename(s.toString())) {
            String format = getString(R.string.download_error_name_is_not_correct);
            binding.layoutName.setErrorEnabled(true);
            binding.layoutName.setError(String.format(format,
                    FileUtils.buildValidFatFilename(s.toString())));
            binding.layoutName.requestFocus();

            return false;
        }

        binding.layoutName.setErrorEnabled(false);
        binding.layoutName.setError(null);

        return true;
    }

    private void showFetchError(Throwable e)
    {
        if (e == null) {
            hideFetchError();
            return;
        }

        String errorStr;

        if (e instanceof MalformedURLException) {
            errorStr = String.format(getString(R.string.fetch_error_invalid_url), e.getMessage());

        } else if (e instanceof ConnectException) {
            errorStr = String.format(getString(R.string.fetch_error_default_fmt),
                    getString(R.string.fetch_error_network_disconnected));

        } else if (e instanceof HttpException) {
            HttpException httpErr = (HttpException)e;
            if (httpErr.getResponseCode() > 0)
                errorStr = String.format(getString(R.string.fetch_error_http_response), httpErr.getResponseCode());
            else
                errorStr = String.format(getString(R.string.fetch_error_default_fmt), httpErr.getMessage());

        } else if (e instanceof IOException) {
            errorStr = String.format(getString(R.string.fetch_error_io), e.getMessage());

        } else {
            errorStr = String.format(getString(R.string.fetch_error_default_fmt), e.getMessage());
        }

        binding.link.setEnabled(true);
        binding.layoutLink.setErrorEnabled(true);
        binding.layoutLink.setError(errorStr);
        binding.layoutLink.requestFocus();
        /* Show connect button */
        alert.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(true);
    }

    private void doAutoFetch()
    {
        if (!TextUtils.isEmpty(viewModel.params.getUrl()) &&
            viewModel.pref.getBoolean(getString(R.string.pref_key_auto_connect),
                                      SettingsManager.Default.autoConnect))
            fetchLink();
    }

    private void fetchLink()
    {
        if (!checkUrlField(binding.link.getText()))
            return;

        viewModel.startFetchTask();
    }

    private void addDownload()
    {
        if (!checkUrlField(binding.link.getText()))
            return;
        if (!checkNameField(binding.name.getText()))
            return;

        try {
            viewModel.addDownload();

        } catch (IOException e) {
            showCreateFileErrorDialog();
            return;
        } catch (FreeSpaceException e) {
            showFreeSpaceErrorToast();
            return;
        } catch (NormalizeUrlException e) {
            showInvalidUrlDialog();
            return;
        }

        Toast.makeText(activity.getApplicationContext(),
                String.format(getString(R.string.download_ticker_notify),
                        viewModel.params.getFileName()),
                Toast.LENGTH_SHORT)
                .show();

        finish(new Intent(), FragmentCallback.ResultCode.OK);
    }

    private void showChooseDirDialog()
    {
        Intent i = new Intent(activity, FileManagerDialog.class);

        String dirPath = null;
        Uri dirUri = viewModel.params.getDirPath();
        if (dirUri != null && FileUtils.isFileSystemPath(dirUri))
            dirPath = dirUri.getPath();

        FileManagerConfig config = new FileManagerConfig(dirPath,
                getString(R.string.select_folder_to_save),
                FileManagerConfig.DIR_CHOOSER_MODE);

        i.putExtra(FileManagerDialog.TAG_CONFIG, config);
        startActivityForResult(i, CHOOSE_PATH_TO_SAVE_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (resultCode != CHOOSE_PATH_TO_SAVE_REQUEST_CODE && resultCode != Activity.RESULT_OK)
            return;

        if (data == null || data.getData() == null) {
            showOpenDirErrorDialog();
            return;
        }

        viewModel.params.setDirPath(data.getData());
    }

    private void showCreateFileErrorDialog()
    {
        FragmentManager fm = getFragmentManager();
        if (fm != null && fm.findFragmentByTag(TAG_CREATE_FILE_ERROR_DIALOG) == null) {
            BaseAlertDialog createFileErrorDialog = BaseAlertDialog.newInstance(
                    getString(R.string.error),
                    getString(R.string.unable_to_create_file),
                    0,
                    getString(R.string.ok),
                    null,
                    null,
                    true);

            createFileErrorDialog.show(fm, TAG_CREATE_FILE_ERROR_DIALOG);
        }
    }

    private void showOpenDirErrorDialog()
    {
        FragmentManager fm = getFragmentManager();
        if (fm != null && fm.findFragmentByTag(TAG_OPEN_DIR_ERROR_DIALOG) == null) {
            BaseAlertDialog openDirErrorDialog = BaseAlertDialog.newInstance(
                    getString(R.string.error),
                    getString(R.string.unable_to_open_folder),
                    0,
                    getString(R.string.ok),
                    null,
                    null,
                    true);

            openDirErrorDialog.show(fm, TAG_OPEN_DIR_ERROR_DIALOG);
        }
    }

    private void showInvalidUrlDialog()
    {
        FragmentManager fm = getFragmentManager();
        if (fm != null && fm.findFragmentByTag(TAG_CREATE_FILE_ERROR_DIALOG) == null) {
            BaseAlertDialog invalidUrlDialog = BaseAlertDialog.newInstance(
                    getString(R.string.error),
                    getString(R.string.add_download_error_invalid_url),
                    0,
                    getString(R.string.ok),
                    null,
                    null,
                    true);

            invalidUrlDialog.show(fm, TAG_CREATE_FILE_ERROR_DIALOG);
        }
    }

    private void showFreeSpaceErrorToast()
    {
        String totalSizeStr = Formatter.formatFileSize(activity, viewModel.params.getTotalBytes());
        String availSizeStr = Formatter.formatFileSize(activity, viewModel.params.getStorageFreeSpace());
        String format = activity.getString(R.string.download_error_no_enough_free_space);

        Toast.makeText(activity.getApplicationContext(),
                String.format(format, availSizeStr, totalSizeStr),
                Toast.LENGTH_LONG)
                .show();
    }

    public void onBackPressed()
    {
        finish(new Intent(), FragmentCallback.ResultCode.BACK);
    }

    private void finish(Intent intent, FragmentCallback.ResultCode code)
    {
        viewModel.finish();

        alert.dismiss();
        ((FragmentCallback)activity).fragmentFinished(intent, code);
    }
}
