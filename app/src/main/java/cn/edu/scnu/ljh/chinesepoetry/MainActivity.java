package cn.edu.scnu.ljh.chinesepoetry;

import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {
    DrawerLayout drawer;
    Button bt_nav;
    Button bt_search;
    Button bt_search_back;
    NavigationView navigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        drawer = findViewById(R.id.drawer);
        bt_nav = findViewById(R.id.bt_nav);
        bt_search = findViewById(R.id.bt_search);
        bt_search_back = findViewById(R.id.bt_search_back);
        navigation = findViewById(R.id.navigation);

        init();
    }

    private void init() {
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
        bt_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawer.openDrawer(Gravity.END);
            }
        });
        bt_search_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawer.closeDrawer(Gravity.END);
            }
        });
        navigation.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.menu_item_poetry:
                        bt_search.setText("唐诗");
                        break;
                    case R.id.menu_item_poetry_author:
                        bt_search.setText("唐诗作者");
                        break;
                    case R.id.menu_item_star:
                        bt_search.setText("收藏");
                        break;
                }

                drawer.closeDrawer(Gravity.START);
                return false;
            }
        });
    }
}
