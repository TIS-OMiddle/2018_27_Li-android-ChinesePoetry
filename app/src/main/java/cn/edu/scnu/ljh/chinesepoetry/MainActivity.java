package cn.edu.scnu.ljh.chinesepoetry;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.github.glomadrian.grav.GravView;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.miguelcatalan.materialsearchview.MaterialSearchView;
import com.miguelcatalan.materialsearchview.SearchAdapter;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.edu.scnu.ljh.chinesepoetry.entity.Poem;
import cn.edu.scnu.ljh.chinesepoetry.entity.Poetry;
import cn.edu.scnu.ljh.chinesepoetry.entity.PoetryAuthor;
import cn.edu.scnu.ljh.chinesepoetry.entity.Star;
import cn.edu.scnu.ljh.chinesepoetry.myview.MyCircleImageView;
import cn.edu.scnu.ljh.chinesepoetry.myview.MyFragmentPoem;
import cn.edu.scnu.ljh.chinesepoetry.myview.MyFragmentPoetry;
import cn.edu.scnu.ljh.chinesepoetry.myview.MyFragmentAuthor;
import cn.edu.scnu.ljh.chinesepoetry.myview.MyFragmentSetting;
import cn.edu.scnu.ljh.chinesepoetry.myview.MyFragmentStar;
import cn.edu.scnu.ljh.chinesepoetry.service.AsyncClient;
import cn.edu.scnu.ljh.chinesepoetry.service.MyHelper;
import cz.msebera.android.httpclient.Header;
import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;

public class MainActivity extends AppCompatActivity {
    private static final int PERMISSION_INTERNET = 1;//网络请求值
    private static final int FRAGMENT_POETRY = 1;//唐诗页面
    private static final int FRAGMENT_POEM = 2;//宋词页面
    private static final int FRAGMENT_AUTHOR = 3;//作者页面
    private static final int FRAGMENT_STAR = 4;//收藏页面
    private static int FRAGMENT_CURRENT;//当前页面
    private int debug_pause = 1200;
    private AsyncHttpResponseHandler responseHandler;//刷新事件显示内容handler

    private MyFragmentPoetry myFragmentPoetry;//唐诗fragment
    private MyFragmentAuthor myFragmentAuthor;
    private MyFragmentPoem myFragmentPoem;
    private MyFragmentStar myFragmentStar;

    SmartRefreshLayout smartRefreshLayout;//刷新布局
    DrawerLayout drawer;//抽屉布局
    MaterialSearchView searchView;//搜索显示
    Button bt_nav;//切换显示导航
    Button bt_back;
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
    FrameLayout settingFrame;//设置页
    GravView gv;//特效球
    GifImageView imgLoading;//加载动画
    MyFragmentSetting myFragmentSetting;//设置页面
    private Map<String, String> userSetting = new HashMap<>();
    private Typeface tf;//字体2


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        smartRefreshLayout = findViewById(R.id.fg_poetry_refresh);
        drawer = findViewById(R.id.drawer);
        searchView = findViewById(R.id.search_view);
        bt_nav = findViewById(R.id.bt_nav);
        bt_back = findViewById(R.id.bt_back);
        navigation = findViewById(R.id.navigation);
        tv_toolbar_title = findViewById(R.id.tv_toolbar_title);
        order = 0;
        handler = new Handler();
        myHelper = new MyHelper(this);
        mainFrame = findViewById(R.id.main_frame);
        settingFrame = findViewById(R.id.setting_frame);
        gv = findViewById(R.id.grav_view);
        imgLoading = findViewById(R.id.img_loading);
        myFragmentSetting = new MyFragmentSetting();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        tf = Typeface.createFromAsset(getAssets(), "no2.ttf");

        //toolbar设定
        toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_action_navigation_arrow_back_inverted);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        init();
    }

    private void init() {
        //设置页面
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.setting_frame, myFragmentSetting);
        transaction.commit();
        bt_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawer.closeDrawer(Gravity.END);
            }
        });
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
                        showFragment(FRAGMENT_POETRY);
                        break;
                    case R.id.menu_item_poem:
                        showFragment(FRAGMENT_POEM);
                        break;
                    case R.id.menu_item_author:
                        showFragment(FRAGMENT_AUTHOR);
                        break;
                    case R.id.menu_item_star:
                        showFragment(FRAGMENT_STAR);
                        break;
                    case R.id.menu_item_setting:
                        drawer.openDrawer(Gravity.END);
                        break;
                    case R.id.menu_item_logout:
                        SharedPreferences.Editor editor = getSharedPreferences("mysetting", MODE_PRIVATE).edit();
                        editor.remove("username");
                        editor.apply();
                        Toast.makeText(getApplicationContext(), "已退出", Toast.LENGTH_SHORT).show();
                        MyCircleImageView mc = findViewById(R.id.header_touxiang);
                        TextView textView = findViewById(R.id.header_user_text);
                        mc.setImageResource(R.drawable.touxiang2);
                        textView.setText("点击头像登陆");
                        break;
                }

                drawer.closeDrawer(Gravity.START);
                return false;
            }
        });

        //刷新布局显示设置
        smartRefreshLayout.setEnableHeaderTranslationContent(false);

        //设置事件监听器
        initHandler();
        initRefreshListener();
        initSearchListener();
        initUserSetting();

        //主页特效
        mainFrame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backgroundTransition();
            }
        });

        //设置初始fragment
        showFragment(FRAGMENT_POETRY);
    }

    Random rand = new Random();
    //主页背景概率变换
    private int bg[] = new int[]{R.drawable.bg1, R.drawable.bg2, R.drawable.bg3};
    private int bg_iter = 0;

    private void backgroundTransition() {
        if (userSetting.get("background").equals("false"))
            return;
        if (rand.nextFloat() < 1) {
            TransitionDrawable transitionDrawable = new TransitionDrawable(new Drawable[]{
                    getDrawable(bg[(bg_iter++) % 3]),
                    getDrawable(bg[(bg_iter) % 3])
            });
            transitionDrawable.startTransition(2000);
            mainFrame.setBackground(transitionDrawable);

            //防卡机
            if (userSetting.get("youhua").equals("true")) {
                gv.setVisibility(View.INVISIBLE);
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mainFrame.setBackground(getDrawable(bg[bg_iter % 3]));
                        if (userSetting.get("lizi").equals("true"))
                            gv.setVisibility(View.VISIBLE);
                    }
                }, 2200);
            }
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
                                    imgLoading.setVisibility(View.INVISIBLE);
                                }
                            }, debug_pause);
                            break;
                        case FRAGMENT_POEM:
                            poemSuggestion = JSON.parseArray(new String(responseBody, "UTF-8"), Poem.class);
                            if (poetriesSuggestion.size() == 0) return;
                            smartRefreshLayout.finishRefresh(1000);
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    myFragmentPoem.setPoem(poemSuggestion.get(0));
                                    imgLoading.setVisibility(View.INVISIBLE);
                                }
                            }, debug_pause);
                            break;
                        case FRAGMENT_AUTHOR:
                            poetryAuthorsSuggestion = JSON.parseArray(new String(responseBody, "UTF-8"), PoetryAuthor.class);
                            if (poetryAuthorsSuggestion.size() == 0) return;
                            smartRefreshLayout.finishRefresh(1000);
                            myFragmentAuthor.setPoetryAuthor(poetryAuthorsSuggestion.get(0));
                            imgLoading.setVisibility(View.INVISIBLE);
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
                imgLoading.setVisibility(View.INVISIBLE);
            }
        };
    }

    //刷新的监听器
    private void initRefreshListener() {
        //smartRefreshLayout刷新事件
        smartRefreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                debug_pause = 1200;
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
                debug_pause = 2200;
                switch (FRAGMENT_CURRENT) {
                    case FRAGMENT_POETRY:
                        Poetry poetry = poetriesSuggestion.get(position);
                        AsyncClient.getPoetry(new RequestParams("id", poetry.getId()), responseHandler, order);
                        myFragmentPoetry.setPoetry(null);
                        break;
                    case FRAGMENT_POEM:
                        Poem poem = poemSuggestion.get(position);
                        AsyncClient.getPoem(new RequestParams("id", poem.getId()), responseHandler, order);
                        myFragmentPoem.setPoem(null);
                        break;
                    case FRAGMENT_AUTHOR:
                        PoetryAuthor poetryAuthor = poetryAuthorsSuggestion.get(position);
                        AsyncClient.getPoetryAuthor(new RequestParams("id", poetryAuthor.getId()), responseHandler, order);
                        break;
                    default:
                        break;
                }
                searchView.closeSearch();
                ((GifDrawable) imgLoading.getDrawable()).reset();
                imgLoading.setVisibility(View.VISIBLE);
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

    //用户设置
    private void initUserSetting() {
        SharedPreferences sp = getSharedPreferences("mysetting", MODE_PRIVATE);
        userSetting.put("ziti", sp.getString("ziti", "1"));
        userSetting.put("background", String.valueOf(sp.getBoolean("background", true)));
        userSetting.put("lizi", String.valueOf(sp.getBoolean("lizi", true)));
        userSetting.put("youhua", String.valueOf(sp.getBoolean("youhua", true)));

        MyCircleImageView myCircleImageView = navigation.getHeaderView(0).findViewById(R.id.header_touxiang);
        TextView textView = navigation.getHeaderView(0).findViewById(R.id.header_user_text);
        if (sp.getString("username", null) != null) {
            myCircleImageView.setImageResource(R.drawable.touxiang);
            textView.setText(sp.getString("username", null));
        } else {
            myCircleImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivityForResult(new Intent(MainActivity.this, LoginActivity.class), 1);
                }
            });
        }

        if (userSetting.get("lizi").equals("false"))
            gv.setVisibility(View.INVISIBLE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == 1) {
            if (resultCode == 2) {
                MyCircleImageView myCircleImageView = findViewById(R.id.header_touxiang);
                TextView textView = findViewById(R.id.header_user_text);
                myCircleImageView.setImageResource(R.drawable.touxiang);
                textView.setText(data.getStringExtra("username"));
            }
        }
    }

    //系统点击返回事件
    @Override
    public void onBackPressed() {
        if (searchView.isSearchOpen()) {
            searchView.closeSearch();
        } else if (drawer.isDrawerOpen(Gravity.END)) {
            drawer.closeDrawer(Gravity.END);
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
                    if (userSetting.get("ziti").equals("2"))
                        myFragmentPoetry.setTf(tf);
                    smartRefreshLayout.autoRefresh();
                }
                tv_toolbar_title.setText("唐诗");
                transaction.replace(R.id.main_frame, myFragmentPoetry);
                break;

            case FRAGMENT_POEM:
                if (myFragmentPoem == null) {
                    myFragmentPoem = new MyFragmentPoem();
                    myFragmentPoem.setMyHelper(myHelper);
                    if (userSetting.get("ziti").equals("2"))
                        myFragmentPoem.setTf(tf);
                    smartRefreshLayout.autoRefresh();
                }
                tv_toolbar_title.setText("宋词");
                transaction.replace(R.id.main_frame, myFragmentPoem);
                break;

            case FRAGMENT_AUTHOR:
                if (myFragmentAuthor == null) {
                    myFragmentAuthor = new MyFragmentAuthor();
                    smartRefreshLayout.autoRefresh();
                }
                tv_toolbar_title.setText("作者");
                transaction.replace(R.id.main_frame, myFragmentAuthor);
                break;

            case FRAGMENT_STAR:
                if (myFragmentStar == null) {
                    myFragmentStar = new MyFragmentStar();
                    myFragmentStar.setMyHelper(myHelper);
                    myFragmentStar.setActivity(this);
                }
                tv_toolbar_title.setText("收藏");
                transaction.replace(R.id.main_frame, myFragmentStar);
                break;
        }
        FRAGMENT_CURRENT = fragment_number;
        transaction.commit();
    }

    //收藏项被点击
    public void onStarItemClick(Star star) {
        if (star.getType() == 1) {//唐诗
            showFragment(FRAGMENT_POETRY);
            myFragmentPoetry.setPoetry(null);
            AsyncClient.getPoetry(new RequestParams("id", star.getId()), responseHandler, order);
        } else {//宋词
            showFragment(FRAGMENT_POEM);
            myFragmentPoem.setPoem(null);
            AsyncClient.getPoem(new RequestParams("id", star.getId()), responseHandler, order);
        }
        ((GifDrawable) imgLoading.getDrawable()).reset();
        imgLoading.setVisibility(View.VISIBLE);
        debug_pause = 2200;
    }
}
