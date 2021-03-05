package com.nexysquare.ddoyac.fragment;


import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceScreen;
import androidx.annotation.Nullable;

import com.nexysquare.ddoyac.R;
import com.nexysquare.ddoyac.activity.AppInfoActivity;
import com.nexysquare.ddoyac.activity.SearchDrugActivity;

public class SettingFragment extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.settings);


    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        PreferenceScreen bug_report_ps = findPreference("bug_report");
        bug_report_ps.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {

                send_email();


                return true;
            }
        });


//search_drug



        PreferenceScreen search_drug_ps = findPreference("search_drug");
        search_drug_ps.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {


                SearchDrugActivity.open(getContext());

                return true;
            }
        });


        PreferenceScreen app_info_ps = findPreference("app_info");
        app_info_ps.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {


                AppInfoActivity.open(getContext());

                return true;
            }
        });
    }

    private void send_email(){
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("message/rfc822");
        i.putExtra(Intent.EXTRA_EMAIL  , new String[]{"main.infinyx@gmail.com"});
        i.putExtra(Intent.EXTRA_SUBJECT, "[또약] 버그 리포트");
        i.putExtra(Intent.EXTRA_TEXT   , "");
        try {
            startActivity(Intent.createChooser(i, "버그 리포트"));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(getContext(), "이메일 관련 어플이 설치되어 있지 않았습니다.", Toast.LENGTH_SHORT).show();
        }
    }
}
