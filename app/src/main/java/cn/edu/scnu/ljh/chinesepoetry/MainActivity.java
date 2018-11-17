package cn.edu.scnu.ljh.chinesepoetry;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.miguelcatalan.materialsearchview.MaterialSearchView;
import com.miguelcatalan.materialsearchview.SearchAdapter;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.edu.scnu.ljh.chinesepoetry.entity.Poetry;
import cn.edu.scnu.ljh.chinesepoetry.service.PoetryClient;
import cz.msebera.android.httpclient.Header;

public class MainActivity extends AppCompatActivity {
    private static final int PERMISSION_INTERNET = 1;
    DrawerLayout drawer;//抽屉布局
    FrameLayout mainFrame;//中间内容
    MaterialSearchView searchView;//搜索显示
    Button bt_nav;//切换显示导航
    Button bt_search;//
    NavigationView navigation;//导航view
    Toolbar toolbar;//工具栏
    List<Poetry> poetriesSuggestion;//搜索建议
    Integer order;//异步按序回调

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        drawer = findViewById(R.id.drawer);
        mainFrame = findViewById(R.id.main_frame);
        searchView = findViewById(R.id.search_view);
        bt_nav = findViewById(R.id.bt_nav);
        bt_search = findViewById(R.id.bt_search);
        navigation = findViewById(R.id.navigation);
        order = 0;

        init();
    }

    private void init() {
        //左侧菜单切换按钮
        bt_nav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (drawer.isDrawerOpen(Gravity.START)) {
                    drawer.closeDrawer(Gravity.START);
                } else {
                    drawer.openDrawer(Gravity.START);
                }
            }
        });
        //搜索按钮?暂时保留，作用待定
        bt_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawer.openDrawer(Gravity.END);
            }
        });
        //导航被点击
        navigation.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.menu_item_poetry:
                        ((TextView) findViewById(R.id.main_frame_text)).setText("唐诗");
                        break;
                    case R.id.menu_item_poetry_author:
                        ((TextView) findViewById(R.id.main_frame_text)).setText("唐诗作者");
                        break;
                    case R.id.menu_item_star:
                        ((TextView) findViewById(R.id.main_frame_text)).setText("收藏");
                        break;
                }

                drawer.closeDrawer(Gravity.START);
                return false;
            }
        });
        //搜索栏设置
        searchView.setVoiceSearch(false);
        //搜索项被点击事件
        final AsyncHttpResponseHandler itemClickHandler = new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                try {
                    List<Poetry> poetries = JSON.parseArray(new String(responseBody, "UTF-8"), Poetry.class);
                    if (poetries.size() == 0) return;
                    TextView tv = findViewById(R.id.main_frame_text);
                    tv.setText(poetries.get(0).getAuthor() + "\n" + poetries.get(0).getContent());
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Toast.makeText(getApplicationContext(), "网络异常", Toast.LENGTH_SHORT).show();
            }
        };
        searchView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                Poetry poetry = poetriesSuggestion.get(position);
                searchView.closeSearch();
                PoetryClient.get(new RequestParams("id", poetry.getId()), itemClickHandler, order);
            }
        });
        //搜索栏文本变化事件
        final Pattern pattern = Pattern.compile("[a-zA-Z]");
        final AsyncHttpResponseHandler textChangeHandler = new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                try {
                    //按序异步回调
                    for (Header header : headers) {
                        if (header.getName().equals("order")) {
                            if (!header.getValue().equals(order.toString()))
                                return;
                            break;
                        }
                    }
                    poetriesSuggestion = JSON.parseArray(new String(responseBody, "UTF-8"), Poetry.class);
                    if (poetriesSuggestion.size() == 0) return;
                    String[] suggs = new String[poetriesSuggestion.size()];
                    for (int i = 0; i < poetriesSuggestion.size(); i++)
                        suggs[i] = poetriesSuggestion.get(i).getAuthorAndTitle();
                    searchView.setAdapter(new SearchAdapter(getApplicationContext(), suggs));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Toast.makeText(getApplicationContext(), "网络异常" + statusCode, Toast.LENGTH_SHORT).show();
            }
        };
        searchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.length() == 0) return false;
                Matcher matcher = pattern.matcher(newText);
                if (matcher.find()) return false;//拦截输入法英文
                Toast.makeText(getApplicationContext(), "开始搜索建议", Toast.LENGTH_SHORT).show();
                ++order;
                PoetryClient.get(new RequestParams("title", newText), textChangeHandler, order);
                return false;
            }
        });
        //toolbar设定
        toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_action_navigation_arrow_back_inverted);
        toolbar.setTitle("工具栏");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
    }

    //搜索栏点击返回事件
    @Override
    public void onBackPressed() {
        if (searchView.isSearchOpen()) {
            searchView.closeSearch();
        } else {
            super.onBackPressed();
        }
    }

    //搜索栏初始化
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu);
        MenuItem item = menu.findItem(R.id.action_search);
        searchView.setMenuItem(item);
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.INTERNET}, PERMISSION_INTERNET);
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_INTERNET) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "已获取权限", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "网络权限受阻，程序将收到影响", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
