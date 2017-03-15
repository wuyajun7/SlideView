package com.peter.slideview;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.peter.slideview.adapter.MainAdapter;
import com.peter.slideview.sview.NNSliderView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private NNSliderView sliderView;
    private ListView main_lv, sub_lv;

    private MainAdapter mainAdapter;
    private MainAdapter subAdapter;

    private List<Brand> brandList;
    private List<Brand> brandSubList;
    private Map<String, List<Brand>> brandMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initData();
        initView();
    }

    private List<Brand> brandListTemp;

    private void initData() {
        brandList = new ArrayList<>();
        brandSubList = new ArrayList<>();
        brandMap = new HashMap<>();

        for (int i = 0; i < 35; i++) {
            String key = "宝马" + i;
            brandList.add(new Brand(
                    "https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1488360445681&di=e54dda40311074f012b9a23ed0727ddd&imgtype=0&src=http%3A%2F%2Fwww.moonsun.cc%2FDIY_T%2Fup%2F201312%2F20131207221550_44.png",
                    key));
            brandListTemp = new ArrayList<>();
            for (int j = 0; j < 300; j++) {
                brandListTemp.add(new Brand(
                        "https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1488360445681&di=e54dda40311074f012b9a23ed0727ddd&imgtype=0&src=http%3A%2F%2Fwww.moonsun.cc%2FDIY_T%2Fup%2F201312%2F20131207221550_44.png",
                        key + " | " + j));
            }
            brandMap.put(key, brandListTemp);
        }
        mainAdapter = new MainAdapter(this, brandList, R.layout.lv_item);
        subAdapter = new MainAdapter(this, brandSubList, R.layout.lv_item);
    }

    private void initView() {
        sliderView = (NNSliderView) findViewById(R.id.slide_view);

        main_lv = (ListView) findViewById(R.id.main_lv);
        sub_lv = (ListView) findViewById(R.id.sub_lv);

        main_lv.setAdapter(mainAdapter);
        sub_lv.setAdapter(subAdapter);

        main_lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                grBrandItemClickListener(parent, position);
            }
        });
        sub_lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(MainActivity.this, "" + brandSubList.get(position).name, Toast.LENGTH_SHORT).show();
            }
        });

        main_lv.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                switch (scrollState) {
                    case AbsListView.OnScrollListener.SCROLL_STATE_IDLE:
                        break;
                    case AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
                        if (!sliderView.menuIsClose()) {
                            sliderView.closeMenu();
                        }
                        break;
                    case AbsListView.OnScrollListener.SCROLL_STATE_FLING:
                        break;
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            }
        });
    }

    protected void grBrandItemClickListener(AdapterView<?> parent, int position) {
        if (sliderView.menuIsClose()) {//如果是关闭状态则打开，否则不处理
            sliderView.openMenu();
        }
        Object object = parent.getItemAtPosition(position);
        if (object != null) {
            if (object instanceof Brand) {
                Brand brand = (Brand) object;
                brandSubList.clear();
                brandSubList.addAll(brandMap.get(brand.name));
                subAdapter.notifyDataSetChanged();
            }
        }
    }

}
