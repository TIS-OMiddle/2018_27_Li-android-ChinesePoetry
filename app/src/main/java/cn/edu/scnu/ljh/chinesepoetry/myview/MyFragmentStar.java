package cn.edu.scnu.ljh.chinesepoetry.myview;

import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import cn.edu.scnu.ljh.chinesepoetry.MainActivity;
import cn.edu.scnu.ljh.chinesepoetry.R;
import cn.edu.scnu.ljh.chinesepoetry.entity.Star;
import cn.edu.scnu.ljh.chinesepoetry.service.MyHelper;

public class MyFragmentStar extends Fragment {
    private View rootView;
    private ListView lv;
    private ArrayAdapter<String> adapter;
    private MyHelper myHelper;
    private List<Star> list;
    private MainActivity activity;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.fragment_star, null);
            lv = rootView.findViewById(R.id.fg_star_lv);
            adapter = new ArrayAdapter<>(Objects.requireNonNull(getContext()), R.layout.lv_item, R.id.lv_item);
            list = new ArrayList<>();
            lv.setAdapter(adapter);

            lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Star star = list.get(position);
                    activity.onStarItemClick(star);
                }
            });
        }
        ViewGroup parent = (ViewGroup) rootView.getParent();
        if (parent != null) {
            parent.removeView(rootView);
        }
        refreshDataFromDataBase();
        return rootView;
    }

    private void refreshDataFromDataBase() {
        adapter.clear();
        list.clear();
        Cursor cursor = myHelper.getReadableDatabase().rawQuery("select * from data", null);
        while (cursor.moveToNext()) {
            Star star = Star.getStarFromCursor(cursor);
            list.add(star);
            adapter.add(star.toString());
        }
        adapter.notifyDataSetChanged();
    }

    public void setActivity(MainActivity activity) {
        this.activity = activity;
    }

    public void setMyHelper(MyHelper myHelper) {
        this.myHelper = myHelper;
    }
}
