package cn.edu.scnu.ljh.chinesepoetry;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
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
import android.widget.FrameLayout;
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
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.edu.scnu.ljh.chinesepoetry.entity.Poem;
import cn.edu.scnu.ljh.chinesepoetry.entity.Poetry;
import cn.edu.scnu.ljh.chinesepoetry.entity.PoetryAuthor;
import cn.edu.scnu.ljh.chinesepoetry.entity.Star;
import cn.edu.scnu.ljh.chinesepoetry.myview.MyFragmentPoem;
import cn.edu.scnu.ljh.chinesepoetry.myview.MyFragmentPoetry;
import cn.edu.scnu.ljh.chinesepoetry.myview.MyFragmentAuthor;
import cn.edu.scnu.ljh.chinesepoetry.myview.MyFragmentStar;
import cn.edu.scnu.ljh.chinesepoetry.service.AsyncClient;
import cn.edu.scnu.ljh.chinesepoetry.service.MyHelper;
import cz.msebera.android.httpclient.Header;

public class MainActivity extends AppCompatActivity {
    private static final int PERMISSION_INTERNET = 1;//网络请求值
    private static final int FRAGMENT_POETRY = 1;//唐诗页面
    private static final int FRAGMENT_POEM = 2;//宋词页面
    private static final int FRAGMENT_AUTHOR = 3;//作者页面
    private static final int FRAGMENT_STAR = 4;//收藏页面
    private static int FRAGMENT_CURRENT;//当前页面
    private AsyncHttpResponseHandler responseHandler;//刷新事件显示内容handler

    private MyFragmentPoetry myFragmentPoetry;//唐诗fragment
    private MyFragmentAuthor myFragmentAuthor;
    private MyFragmentPoem myFragmentPoem;
    private MyFragmentStar myFragmentStar;

    SmartRefreshLayout smartRefreshLayout;//刷新布局
    DrawerLayout drawer;//抽屉布局
    MaterialSearchView searchView;//搜索显示
    Button bt_nav;//切换显示导航
    NavigationView navigation;//导航view
    Toolbar toolbar;//工具栏
    List<Poetry> poetriesSuggestion;//搜索建议
    List<PoetryAuthor> poetryAuthorsSuggestion;//搜索建议
    List<Poem> poemSuggestion;//搜索建议
    MyHelper myHelper;//数据库
    Integer order;//异步按序回调
    TextView tv_toolbar_title;//工具栏
    Handler handler;//主线程handler
    FrameLayout mainFrame;//主页


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        smartRefreshLayout = findViewById(R.id.fg_poetry_refresh);
        drawer = findViewById(R.id.drawer);
        searchView = findViewById(R.id.search_view);
        bt_nav = findViewById(R.id.bt_nav);
        navigation = findViewById(R.id.navigation);
        tv_toolbar_title = findViewById(R.id.tv_toolbar_title);
        order = 0;
        handler = new Handler();
        myHelper = new MyHelper(this);
        mainFrame = findViewById(R.id.main_frame);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        //toolbar设定
        toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_action_navigation_arrow_back_inverted);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        init();
    }

    private void init() {
        //左侧菜单切换按钮
        drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
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
                        tv_toolbar_title.setText("唐诗");
                        showFragment(FRAGMENT_POETRY);
                        break;
                    case R.id.menu_item_poem:
                        tv_toolbar_title.setText("宋词");
                        showFragment(FRAGMENT_POEM);
                        break;
                    case R.id.menu_item_author:
                        tv_toolbar_title.setText("作者");
                        showFragment(FRAGMENT_AUTHOR);
                        break;
                    case R.id.menu_item_star:
                        tv_toolbar_title.setText("收藏");
                        showFragment(FRAGMENT_STAR);
                        break;
                }

                drawer.closeDrawer(Gravity.START);
                return false;
            }
        });

        //设置初始fragment
        showFragment(FRAGMENT_POETRY);

        //刷新布局显示设置
        smartRefreshLayout.setEnableHeaderTranslationContent(false);

        //设置事件监听器
        initHandler();
        initRefreshListener();
        initSearchListener();

        //主页特效
        mainFrame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backgroundTransition();
            }
        });
    }

    Random rand = new Random();
    //主页背景概率变换
    private int bg[] = new int[]{R.drawable.bg1, R.drawable.bg2, R.drawable.bg3};
    private int bg_iter = 0;
    private void backgroundTransition() {
        if (rand.nextFloat() < 1) {
            TransitionDrawable transitionDrawable = new TransitionDrawable(new Drawable[]{
                    getDrawable(bg[(bg_iter++) % 3]),
                    getDrawable(bg[(bg_iter) % 3])
            });
            mainFrame.setBackground(transitionDrawable);
            transitionDrawable.startTransition(3000);
        }
    }

    //初始化监听器
    private void initHandler() {
        responseHandler = new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                try {
                    switch (FRAGMENT_CURRENT) {
                        case FRAGMENT_POETRY:
                            poetriesSuggestion = JSON.parseArray(new String(responseBody, "UTF-8"), Poetry.class);
                            if (poetriesSuggestion.size() == 0) return;
                            smartRefreshLayout.finishRefresh(1000);
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    myFragmentPoetry.setPoetry(poetriesSuggestion.get(0));
                                }
                            }, 1200);
                            break;
                        case FRAGMENT_POEM:
                            poemSuggestion = JSON.parseArray(new String(responseBody, "UTF-8"), Poem.class);
                            if (poetriesSuggestion.size() == 0) return;
                            smartRefreshLayout.finishRefresh(1000);
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    myFragmentPoem.setPoem(poemSuggestion.get(0));
                                }
                            }, 1200);
                            break;
                        case FRAGMENT_AUTHOR:
                            poetryAuthorsSuggestion = JSON.parseArray(new String(responseBody, "UTF-8"), PoetryAuthor.class);
                            if (poetryAuthorsSuggestion.size() == 0) return;
                            smartRefreshLayout.finishRefresh(1000);
                            myFragmentAuthor.setPoetryAuthor(poetryAuthorsSuggestion.get(0));
                            break;
                    }
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
                        AsyncClient.getPoetry(null, responseHandler, 0);
                        break;
                    case FRAGMENT_POEM:
                        AsyncClient.getPoem(null, responseHandler, 0);
                        break;
                    case FRAGMENT_AUTHOR:
                        AsyncClient.getPoetryAuthor(null, responseHandler, 0);
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
                        AsyncClient.getPoetry(new RequestParams("id", poetry.getId()), responseHandler, order);
                        break;
                    case FRAGMENT_POEM:
                        Poem poem = poemSuggestion.get(position);
                        AsyncClient.getPoem(new RequestParams("id", poem.getId()), responseHandler, order);
                        break;
                    case FRAGMENT_AUTHOR:
                        PoetryAuthor poetryAuthor = poetryAuthorsSuggestion.get(position);
                        AsyncClient.getPoetryAuthor(new RequestParams("id", poetryAuthor.getId()), responseHandler, order);
                        break;
                    default:
                        break;
                }
                searchView.closeSearch();
            }
        });
        //搜索栏文本变化事件
        final Pattern pattern = Pattern.compile("[a-zA-Z]");
        //本文变化回调建议handler
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
                    String[] suggs = null;
                    switch (FRAGMENT_CURRENT) {
                        case FRAGMENT_POETRY:
                            poetriesSuggestion = JSON.parseArray(new String(responseBody, "UTF-8"), Poetry.class);
                            if (poetriesSuggestion.size() == 0) return;
                            suggs = new String[poetriesSuggestion.size()];
                            for (int i = 0; i < poetriesSuggestion.size(); i++)
                                suggs[i] = poetriesSuggestion.get(i).getAuthorAndTitle();
                            break;
                        case FRAGMENT_POEM:
                            poemSuggestion = JSON.parseArray(new String(responseBody, "UTF-8"), Poem.class);
                            if (poemSuggestion.size() == 0) return;
                            suggs = new String[poemSuggestion.size()];
                            for (int i = 0; i < poemSuggestion.size(); i++) {
                                suggs[i] = poemSuggestion.get(i).getAuthorAndTitle();
                            }
                            break;
                        case FRAGMENT_AUTHOR:
                            poetryAuthorsSuggestion = JSON.parseArray(new String(responseBody, "UTF-8"), PoetryAuthor.class);
                            if (poetryAuthorsSuggestion.size() == 0) return;
                            suggs = new String[poetryAuthorsSuggestion.size()];
                            for (int i = 0; i < poetryAuthorsSuggestion.size(); i++)
                                suggs[i] = poetryAuthorsSuggestion.get(i).getNameAndDynasty();
                            break;
                        default:
                            break;
                    }

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
                boolean isFilter = false;
                ++order;

                switch (FRAGMENT_CURRENT) {
                    case FRAGMENT_POETRY:
                        for (int i = 0; i < poetriesSuggestion.size(); i++) {
                            if (poetriesSuggestion.get(i).getTitle().indexOf(newText) == 0) {
                                isFilter = true;
                                break;
                            }
                        }
                        if (!isFilter)
                            AsyncClient.getPoetry(new RequestParams("title", newText), textChangeHandler, order);
                        break;
                    case FRAGMENT_POEM:
                        for (int i = 0; i < poemSuggestion.size(); i++) {
                            if (poemSuggestion.get(i).getTitle().indexOf(newText) == 0) {
                                isFilter = true;
                                break;
                            }
                        }
                        if (!isFilter)
                            AsyncClient.getPoem(new RequestParams("title", newText), textChangeHandler, order);
                    case FRAGMENT_AUTHOR:
                        AsyncClient.getPoetryAuthor(new RequestParams("name", newText), textChangeHandler, order);
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
                    myFragmentPoetry.setMyHelper(myHelper);
                    smartRefreshLayout.autoRefresh();
                }
                transaction.replace(R.id.main_frame, myFragmentPoetry);
                break;

            case FRAGMENT_POEM:
                if (myFragmentPoem == null) {
                    myFragmentPoem = new MyFragmentPoem();
                    myFragmentPoem.setMyHelper(myHelper);
                    smartRefreshLayout.autoRefresh();
                }
                transaction.replace(R.id.main_frame, myFragmentPoem);
                break;

            case FRAGMENT_AUTHOR:
                if (myFragmentAuthor == null) {
                    myFragmentAuthor = new MyFragmentAuthor();
                    smartRefreshLayout.autoRefresh();
                }
                transaction.replace(R.id.main_frame, myFragmentAuthor);
                break;

            case FRAGMENT_STAR:
                if (myFragmentStar == null) {
                    myFragmentStar = new MyFragmentStar();
                    myFragmentStar.setMyHelper(myHelper);
                    myFragmentStar.setActivity(this);
                }
                transaction.replace(R.id.main_frame, myFragmentStar);
                break;
        }
        FRAGMENT_CURRENT = fragment_number;
        transaction.commit();
    }

    public void onStarItemClick(Star star) {
        if (star.getType() == 1) {//唐诗
            showFragment(FRAGMENT_POETRY);
            AsyncClient.getPoetry(new RequestParams("id", star.getId()), responseHandler, order);
        } else {//宋词
            showFragment(FRAGMENT_POEM);
            AsyncClient.getPoem(new RequestParams("id", star.getId()), responseHandler, order);
        }
    }
}
