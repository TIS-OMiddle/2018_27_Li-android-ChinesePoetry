package cn.edu.scnu.ljh.chinesepoetry;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.miguelcatalan.materialsearchview.MaterialSearchView;
import com.miguelcatalan.materialsearchview.SearchAdapter;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.edu.scnu.ljh.chinesepoetry.entity.Poetry;
import cn.edu.scnu.ljh.chinesepoetry.myview.MyFragmentPoetry;
import cn.edu.scnu.ljh.chinesepoetry.service.PoetryClient;
import cz.msebera.android.httpclient.Header;

public class MainActivity extends AppCompatActivity {
    private static final int PERMISSION_INTERNET = 1;//网络请求值
    private static final int FRAGMENT_POETRY = 1;//唐诗页面
    private static final int FRAGMENT_POETRY_AUTHOR = 2;//唐诗作者页面
    private static final int FRAGMENT_STAR = 3;//收藏页面
    private static int FRAGMENT_CURRENT;//当前页面
    private AsyncHttpResponseHandler poetryResponseHandler;//唐诗回应handler
    private MyFragmentPoetry myFragmentPoetry;//唐诗fragment

    SmartRefreshLayout smartRefreshLayout;//刷新布局
    DrawerLayout drawer;//抽屉布局
    MaterialSearchView searchView;//搜索显示
    Button bt_nav;//切换显示导航
    Button bt_search;//??
    NavigationView navigation;//导航view
    Toolbar toolbar;//工具栏
    List<Poetry> poetriesSuggestion;//搜索建议
    Integer order;//异步按序回调
    TextView tv_toobal_title;//工具栏
    Handler handler;//主线程handler


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        smartRefreshLayout = findViewById(R.id.fg_poetry_refresh);
        drawer = findViewById(R.id.drawer);
        searchView = findViewById(R.id.search_view);
        bt_nav = findViewById(R.id.bt_nav);
        bt_search = findViewById(R.id.bt_search);
        navigation = findViewById(R.id.navigation);
        tv_toobal_title = findViewById(R.id.tv_toolbar_title);
        order = 0;
        handler = new Handler();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        //toolbar设定
        toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_action_navigation_arrow_back_inverted);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        init();
    }

    private void init() {
        //左侧菜单切换按钮
        bt_nav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawer.openDrawer(Gravity.START);
            }
        });
        //导航被点击
        navigation.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.menu_item_poetry:
                        tv_toobal_title.setText("唐诗");
                        showFragment(FRAGMENT_POETRY);
                        break;
                    case R.id.menu_item_poetry_author:
                        tv_toobal_title.setText("唐诗作者");
                        showFragment(FRAGMENT_POETRY_AUTHOR);
                        break;
                    case R.id.menu_item_star:
                        tv_toobal_title.setText("收藏");
                        showFragment(FRAGMENT_STAR);
                        break;
                }

                drawer.closeDrawer(Gravity.START);
                return false;
            }
        });

        //设置初始fragment
        showFragment(FRAGMENT_POETRY);

        //设置事件监听器
        initHandler();
        initRefreshListener();
        initSearchListener();

        //刷新布局显示设置
        smartRefreshLayout.setEnableHeaderTranslationContent(false);
        smartRefreshLayout.autoRefresh();
    }

    //初始化监听器
    private void initHandler() {
        poetryResponseHandler = new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                try {
                    poetriesSuggestion = JSON.parseArray(new String(responseBody, "UTF-8"), Poetry.class);
                    if (poetriesSuggestion.size() == 0) return;
                    smartRefreshLayout.finishRefresh(1000);
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            myFragmentPoetry.setPoetry(poetriesSuggestion.get(0));
                        }
                    }, 1200);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), "服务器异常", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                smartRefreshLayout.finishRefresh();
                Toast.makeText(getApplicationContext(), "网络异常", Toast.LENGTH_SHORT).show();
            }
        };
    }

    //刷新的监听器
    private void initRefreshListener() {
        //smartRefreshLayout刷新事件
        smartRefreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                switch (FRAGMENT_CURRENT) {
                    case FRAGMENT_POETRY:
                        PoetryClient.get(null, poetryResponseHandler, 0);
                        break;
                    case FRAGMENT_POETRY_AUTHOR:
                        break;
                    case FRAGMENT_STAR:
                        break;
                    default:
                        break;
                }
            }
        });
    }

    //搜索栏的监听器
    private void initSearchListener() {
        //关掉语音输入
        searchView.setVoiceSearch(false);
        //搜索项被点击事件
        searchView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                switch (FRAGMENT_CURRENT) {
                    case FRAGMENT_POETRY:
                        Poetry poetry = poetriesSuggestion.get(position);
                        PoetryClient.get(new RequestParams("id", poetry.getId()), poetryResponseHandler, order);
                        break;
                    case FRAGMENT_POETRY_AUTHOR:
                        break;
                    case FRAGMENT_STAR:
                        break;
                    default:
                        break;
                }
                searchView.closeSearch();
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
                ++order;

                switch (FRAGMENT_CURRENT) {
                    case FRAGMENT_POETRY:
                        PoetryClient.get(new RequestParams("title", newText), textChangeHandler, order);
                        break;
                    case FRAGMENT_POETRY_AUTHOR:
                        break;
                    case FRAGMENT_STAR:
                        break;
                    default:
                        break;
                }
                return false;
            }
        });
    }


    //系统点击返回事件
    @Override
    public void onBackPressed() {
        if (searchView.isSearchOpen()) {
            searchView.closeSearch();
        } else {
            super.onBackPressed();
        }
    }

    //搜索初始化+检测网络
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

    /**
     * @param fragment_number 代表fragment的静态常量
     *                        使main_frame显示指定fragment
     */
    private void showFragment(int fragment_number) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        switch (fragment_number) {
            case FRAGMENT_POETRY:
                if (myFragmentPoetry == null) {
                    myFragmentPoetry = new MyFragmentPoetry();
                }
                transaction.replace(R.id.main_frame, myFragmentPoetry);
                break;

            case FRAGMENT_POETRY_AUTHOR:
                break;

            case FRAGMENT_STAR:
                break;
        }
        FRAGMENT_CURRENT = fragment_number;
        transaction.commit();
    }
}
