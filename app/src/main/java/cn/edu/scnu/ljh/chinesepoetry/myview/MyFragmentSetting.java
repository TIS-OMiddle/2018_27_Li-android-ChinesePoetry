package cn.edu.scnu.ljh.chinesepoetry.myview;

import android.os.Bundle;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.widget.Toast;

import cn.edu.scnu.ljh.chinesepoetry.R;

public class MyFragmentSetting extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        getPreferenceManager().setSharedPreferencesName("mysetting");
        addPreferencesFromResource(R.xml.preferences);
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        Toast.makeText(getContext(), "重启应用后生效", Toast.LENGTH_SHORT).show();
        return super.onPreferenceTreeClick(preference);
    }
}
