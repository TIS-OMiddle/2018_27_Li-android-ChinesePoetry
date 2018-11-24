package cn.edu.scnu.ljh.chinesepoetry.myview;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import cn.edu.scnu.ljh.chinesepoetry.R;
import cn.edu.scnu.ljh.chinesepoetry.entity.PoetryAuthor;

public class MyFragmentAuthor extends Fragment {
    private View rootView;
    TextView poetry_author_name;
    TextView poetry_author_intro;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.fragment_poetry_author, null);
            poetry_author_name = rootView.findViewById(R.id.fg_poetry_author_name);
            poetry_author_intro = rootView.findViewById(R.id.fg_poetry_author_intro);
        }
        ViewGroup parent = (ViewGroup) rootView.getParent();
        if (parent != null) {
            parent.removeView(rootView);
        }
        return rootView;
    }

    public void setName(String name) {
        poetry_author_name.setText(name);
    }

    public void setIntro(String intro) {
        poetry_author_intro.setText(intro);
    }

    public void setPoetryAuthor(PoetryAuthor poetryAuthor) {
        setName(poetryAuthor.getName());
        setIntro(poetryAuthor.getIntro());
    }
}
