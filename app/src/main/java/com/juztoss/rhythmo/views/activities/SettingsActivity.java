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
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

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
import com.juztoss.rhythmo.services.LibraryServiceBuilder;
import com.juztoss.rhythmo.utils.SystemHelper;
import com.juztoss.rhythmo.views.items.MusicLibraryPreference;

import de.psdev.licensesdialog.LicenseResolver;
import de.psdev.licensesdialog.LicensesDialog;
import de.psdev.licensesdialog.licenses.License;

/**
 * Created by JuzTosS on 5/27/2016.
 */
public class SettingsActivity extends BasePlayerActivity implements SharedPreferences.OnSharedPreferenceChangeListener
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

            mMusicLibraryPreference.setOnPreferenceClickListener(preference -> {
                if (!((RhythmoApp) getActivity().getApplicationContext()).isBuildingLibrary())
                {
                    new LibraryServiceBuilder(getActivity())
                            .scanMediaStore()
                            .detectBpm()
                            .enableNotifications()
                            .stopCurrentlyExecuting()
                            .start();
                }
                else
                {
                    Intent intent = new Intent(getActivity().getApplicationContext(), BuildMusicLibraryService.class);
                    getActivity().getApplicationContext().stopService(intent);
                }
                return true;
            });

            Preference clearLibraryPreference = findPreference(getString(R.string.pref_clear_library));
            clearLibraryPreference.setOnPreferenceClickListener(preference -> {
                Toast.makeText(getActivity(), R.string.hold_hint, Toast.LENGTH_SHORT).show();
                return true;
            });

            Preference licensePref = findPreference(getString(R.string.pref_license_button));

            licensePref.setOnPreferenceClickListener(preference -> {
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
            });
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
        {
            View view = super.onCreateView(inflater, container, savedInstanceState);
            ListView listView = (ListView) view.findViewById(android.R.id.list);
            listView.setOnItemLongClickListener((parent, view1, position, id) -> {
                ListView listView1 = (ListView) parent;
                ListAdapter listAdapter = listView1.getAdapter();
                Object obj = listAdapter.getItem(position);
                if (obj != null && obj instanceof View.OnLongClickListener) {
                    View.OnLongClickListener longListener = (View.OnLongClickListener) obj;
                    return longListener.onLongClick(view1);
                }
                return false;
            });

            return view;
        }

        public void updateBuildMusicLibrarySetting(String header, int overallProgress, int maxProgress)
        {
            mMusicLibraryPreference.update(header, overallProgress, maxProgress);
        }
    }
}
