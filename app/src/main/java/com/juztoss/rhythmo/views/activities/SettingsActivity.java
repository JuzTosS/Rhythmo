package com.juztoss.rhythmo.views.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.juztoss.rhythmo.R;
import com.juztoss.rhythmo.presenters.RhythmoApp;
import com.juztoss.rhythmo.services.BuildMusicLibraryService;
import com.juztoss.rhythmo.views.items.MusicLibraryPreference;

import de.psdev.licensesdialog.LicenseResolver;
import de.psdev.licensesdialog.LicensesDialog;
import de.psdev.licensesdialog.licenses.ApacheSoftwareLicense20;
import de.psdev.licensesdialog.licenses.GnuLesserGeneralPublicLicense21;
import de.psdev.licensesdialog.licenses.License;
import de.psdev.licensesdialog.model.Notice;
import de.psdev.licensesdialog.model.Notices;

/**
 * Created by JuzTosS on 5/27/2016.
 */
public class SettingsActivity extends AppCompatActivity
{
    private PrefsFragment mPrefsFragment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setTitle(R.string.settings_title);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mPrefsFragment = new PrefsFragment();
        getFragmentManager().beginTransaction().replace(R.id.fragment_container,
                mPrefsFragment).commit();

        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(mUpdateMusicLibraryPrefReceiver, new IntentFilter(BuildMusicLibraryService.UPDATE_PROGRESS_ACTION));
    }

    private BroadcastReceiver mUpdateMusicLibraryPrefReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            updateBuildMusicLibrarySetting(intent.getExtras());
        }
    };

    @Override
    protected void onResume()
    {
        super.onResume();
        updateBuildMusicLibrarySetting(getIntent().getExtras());
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(mUpdateMusicLibraryPrefReceiver);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        onBackPressed();
        return super.onOptionsItemSelected(item);
    }

    private void updateBuildMusicLibrarySetting(@Nullable Bundle bundle)
    {
        if (bundle == null) return;

        String header = bundle.getString(BuildMusicLibraryService.PROGRESS_ACTION_HEADER);
        int overallProgress = bundle.getInt(BuildMusicLibraryService.PROGRESS_ACTION_OVERALL_PROGRESS, 0);
        int maxProgress = bundle.getInt(BuildMusicLibraryService.PROGRESS_ACTION_MAX_PROGRESS, 0);

        mPrefsFragment.updateBuildMusicLibrarySetting(header, overallProgress, maxProgress);
    }


    @Override
    protected void onNewIntent(Intent intent)
    {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    /**
     * This fragment shows the preferences for the first header.
     */
    public static class PrefsFragment extends PreferenceFragment
    {
        MusicLibraryPreference mMusicLibraryPreference;

        @Override
        public void onCreate(Bundle savedInstanceState)
        {
            super.onCreate(savedInstanceState);

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.preferences);


            mMusicLibraryPreference = (MusicLibraryPreference) findPreference(getString(R.string.pref_build_library));

            mMusicLibraryPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener()
            {
                @Override
                public boolean onPreferenceClick(Preference preference)
                {
                    if (!((RhythmoApp) getActivity().getApplicationContext()).isBuildingLibrary())
                    {
                        Intent intent = new Intent(getActivity().getApplicationContext(), BuildMusicLibraryService.class);
                        getActivity().getApplicationContext().startService(intent);
                    }
                    else
                    {
                        Intent intent = new Intent(getActivity().getApplicationContext(), BuildMusicLibraryService.class);
                        getActivity().getApplicationContext().stopService(intent);
                    }
                    return true;
                }
            });

            Preference clearLibraryPreference = findPreference(getString(R.string.pref_clear_library));
            clearLibraryPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener()
            {
                @Override
                public boolean onPreferenceClick(Preference preference)
                {
                    Intent intent = new Intent(getActivity().getApplicationContext(), BuildMusicLibraryService.class);
                    intent.putExtra(BuildMusicLibraryService.STOP_AND_CLEAR, true);
                    getActivity().getApplicationContext().startService(intent);
                    return true;
                }
            });

            Preference licensePref = findPreference(getString(R.string.pref_license_button));

            licensePref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener()
            {
                @Override
                public boolean onPreferenceClick(Preference preference)
                {
                    LicenseResolver.registerLicense(new License()
                    {
                        @Override
                        public String getName()
                        {
                            return "http://superpowered.com/license";
                        }

                        @Override
                        public String readSummaryTextFromResources(Context context)
                        {
                            return "http://superpowered.com/license";
                        }

                        @Override
                        public String readFullTextFromResources(Context context)
                        {
                            return "http://superpowered.com/license";
                        }

                        @Override
                        public String getVersion()
                        {
                            return "1.0";
                        }

                        @Override
                        public String getUrl()
                        {
                            return "http://superpowered.com/license";
                        }
                    });
                    new LicensesDialog.Builder(preference.getContext())
                            .setNotices(R.raw.licenses)
                            .setIncludeOwnLicense(true)
                            .build()
                            .show();

                    return true;
                }
            });
        }

        public void updateBuildMusicLibrarySetting(String header, int overallProgress, int maxProgress)
        {
            mMusicLibraryPreference.update(header, overallProgress, maxProgress);
        }
    }
}
