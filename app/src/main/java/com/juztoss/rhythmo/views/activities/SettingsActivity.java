package com.juztoss.rhythmo.views.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.juztoss.rhythmo.R;
import com.juztoss.rhythmo.presenters.RhythmoApp;
import com.juztoss.rhythmo.services.BuildMusicLibraryService;
import com.juztoss.rhythmo.utils.SystemHelper;
import com.juztoss.rhythmo.views.items.MusicLibraryPreference;

import de.psdev.licensesdialog.LicenseResolver;
import de.psdev.licensesdialog.LicensesDialog;
import de.psdev.licensesdialog.licenses.License;

/**
 * Created by JuzTosS on 5/27/2016.
 */
public class SettingsActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener
{
    private PrefsFragment mPrefsFragment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        SystemHelper.updateTheme(this);
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

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
    {
        if(key.equals(getResources().getString(R.string.pref_theme)))
            recreate();
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
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.unregisterOnSharedPreferenceChangeListener(this);
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
        private MusicLibraryPreference mMusicLibraryPreference;

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
                        intent.putExtra(BuildMusicLibraryService.SCAN_MEDIA_STORE, true);
                        intent.putExtra(BuildMusicLibraryService.DETECT_BPM, true);
                        intent.putExtra(BuildMusicLibraryService.ENABLE_NOTIFICATIONS, true);
                        intent.putExtra(BuildMusicLibraryService.STOP_CURRENTLY_ECECUTING, true);
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
                    Toast.makeText(getActivity(), R.string.hold_hint, Toast.LENGTH_SHORT).show();
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

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
        {
            View view = super.onCreateView(inflater, container, savedInstanceState);
            ListView listView = (ListView) view.findViewById(android.R.id.list);
            listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                    ListView listView = (ListView) parent;
                    ListAdapter listAdapter = listView.getAdapter();
                    Object obj = listAdapter.getItem(position);
                    if (obj != null && obj instanceof View.OnLongClickListener) {
                        View.OnLongClickListener longListener = (View.OnLongClickListener) obj;
                        return longListener.onLongClick(view);
                    }
                    return false;
                }
            });

            return view;
        }

        public void updateBuildMusicLibrarySetting(String header, int overallProgress, int maxProgress)
        {
            mMusicLibraryPreference.update(header, overallProgress, maxProgress);
        }
    }
}
