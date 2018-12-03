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

import cn.edu.scnu.ljh.chinesepoetry.R;
import cn.edu.scnu.ljh.chinesepoetry.entity.Poem;
import cn.edu.scnu.ljh.chinesepoetry.service.MyHelper;

public class MyFragmentPoem extends Fragment {
    private View rootView;
    private TextView poem_title;
    private TextView poem_author;
    private TextView poem_content;
    private Poem poem;
    private MyHelper myHelper;
    private ImageView img;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.fragment_poem, null);
            img = rootView.findViewById(R.id.img_poem);
            img.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SQLiteDatabase db = myHelper.getWritableDatabase();
                    if ((boolean) img.getTag()) {//已收藏，那么取消
                        db.execSQL("delete from data where id=? and type=2",
                                new Object[]{poem.getId()});
                        Toast.makeText(getContext(), "已取消收藏", Toast.LENGTH_SHORT).show();
                        img.setImageResource(R.drawable.star_no);
                    } else {//未收藏，那么添加到收藏
                        db.execSQL("insert into data (id,title,author,type) values(?,?,?,2)",
                                new Object[]{poem.getId(), poem.getTitle(), poem.getAuthor()});
                        Toast.makeText(getContext(), "已添加到收藏", Toast.LENGTH_SHORT).show();
                        img.setImageResource(R.drawable.star_yes);
                    }
                }
            });
            poem_title = rootView.findViewById(R.id.fg_poem_title);
            poem_author = rootView.findViewById(R.id.fg_poem_author);
            poem_content = rootView.findViewById(R.id.fg_poem_content);
        }
        ViewGroup parent = (ViewGroup) rootView.getParent();
        if (parent != null) {
            parent.removeView(rootView);
        }
        return rootView;
    }

    public void setPoem(Poem poem) {
        if (poem == null) {
            poem_title.setText("");
            poem_author.setText("");
            poem_content.setText("");
            return;
        }
        poem_title.setText(poem.getTitle());
        poem_author.setText(poem.getAuthor());
        poem_content.setText(poem.getContent().replace('|', '\n'));
        SQLiteDatabase db = myHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from data where type=2 and id=?", new String[]{String.valueOf(poem.getId())});
        if (cursor.moveToNext()) {
            img.setImageResource(R.drawable.star_yes);
            img.setTag(true);
        } else {
            img.setImageResource(R.drawable.star_no);
            img.setTag(false);
        }
        cursor.close();
        this.poem = poem;
    }

    public Poem getPoem() {
        return poem;
    }

    public void setMyHelper(MyHelper myHelper) {
        this.myHelper = myHelper;
    }
}
