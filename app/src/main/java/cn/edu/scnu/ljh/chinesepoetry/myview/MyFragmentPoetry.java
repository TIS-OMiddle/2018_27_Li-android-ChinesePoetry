package cn.edu.scnu.ljh.chinesepoetry.myview;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.hanks.htextview.base.AnimationListener;
import com.hanks.htextview.base.HTextView;
import com.hanks.htextview.fade.FadeTextView;

import cn.edu.scnu.ljh.chinesepoetry.R;
import cn.edu.scnu.ljh.chinesepoetry.entity.Poetry;
import cn.edu.scnu.ljh.chinesepoetry.service.MyHelper;

public class MyFragmentPoetry extends Fragment {
    private View rootView;
    private TextView poetry_title;
    private TextView poetry_author;
    private ImageView img;
    private FadeTextView[] poetry_content;
    private String[] content;
    private int contentLength = 0;
    private Poetry poetry;
    private MyHelper myHelper;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.fragment_poetry, null);
            poetry_content = new FadeTextView[4];

            //控件初始化
            img = rootView.findViewById(R.id.img_poetry);
            img.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SQLiteDatabase db = myHelper.getWritableDatabase();
                    if ((boolean) img.getTag()) {//已收藏，那么取消
                        db.execSQL("delete from data where id=? and type=1",
                                new Object[]{poetry.getId()});
                        Toast.makeText(getContext(), "已取消收藏", Toast.LENGTH_SHORT).show();
                        img.setImageResource(R.drawable.star_no);
                    } else {//未收藏，那么添加到收藏
                        db.execSQL("insert into data (id,title,author,type) values(?,?,?,1)",
                                new Object[]{poetry.getId(), poetry.getTitle(), poetry.getAuthor()});
                        Toast.makeText(getContext(), "已添加到收藏", Toast.LENGTH_SHORT).show();
                        img.setImageResource(R.drawable.star_yes);
                    }
                }
            });
            poetry_title = rootView.findViewById(R.id.fg_poetry_title);
            poetry_author = rootView.findViewById(R.id.fg_poetry_author);
            poetry_content[0] = rootView.findViewById(R.id.fg_poetry_content_1);
            poetry_content[1] = rootView.findViewById(R.id.fg_poetry_content_2);
            poetry_content[2] = rootView.findViewById(R.id.fg_poetry_content_3);
            poetry_content[3] = rootView.findViewById(R.id.fg_poetry_content_4);
            setInOrderAnimate();
        }
        ViewGroup parent = (ViewGroup) rootView.getParent();
        if (parent != null) {
            parent.removeView(rootView);
        }
        return rootView;
    }

    private void setInOrderAnimate() {
        for (int i = 0; i < 3; i++) {
            final int finalI = i;
            poetry_content[i].setAnimationListener(new AnimationListener() {
                @Override
                public void onAnimationEnd(HTextView hTextView) {
                    if (contentLength > (finalI + 1))
                        poetry_content[finalI + 1].animateText(content[finalI + 1]);
                }
            });
        }
    }

    public void setTitle(String title) {
        poetry_title.setText(title);
    }

    public void setAuthor(String author) {
        poetry_author.setText(author);
    }

    public void setContent(String content) {
        this.content = content.split("\\|");
        contentLength = this.content.length;
        for (int i = 1; i < 4; i++)
            poetry_content[i].setText("");
        poetry_content[0].animateText(this.content[0]);

        if (contentLength > 3) {//显示第3，4句
            poetry_content[2].setVisibility(View.VISIBLE);
            poetry_content[3].setVisibility(View.VISIBLE);
        } else {//不显示第3，4句
            poetry_content[2].setVisibility(View.GONE);
            poetry_content[3].setVisibility(View.GONE);
        }
    }

    public void setPoetry(Poetry poetry) {
        setTitle(poetry.getTitle());
        setAuthor(poetry.getAuthor());
        setContent(poetry.getContent());
        SQLiteDatabase db = myHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from data where type=1 and id=?", new String[]{String.valueOf(poetry.getId())});
        if (cursor.moveToNext()) {
            img.setImageResource(R.drawable.star_yes);
            img.setTag(true);
        } else {
            img.setImageResource(R.drawable.star_no);
            img.setTag(false);
        }
        cursor.close();
        this.poetry = poetry;
    }

    public Poetry getPoetry() {
        return poetry;
    }

    public void setMyHelper(MyHelper myHelper) {
        this.myHelper = myHelper;
    }
}
