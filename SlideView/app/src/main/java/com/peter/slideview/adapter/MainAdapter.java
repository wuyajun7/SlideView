package com.peter.slideview.adapter;

import android.content.Context;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.peter.slideview.Brand;
import com.peter.slideview.R;
import com.peter.slideview.wnadapter.WNBaseAdapter;
import com.peter.slideview.wnadapter.WNViewHolder;

import java.util.List;

/**
 * Created by peter on 2017/3/1.
 */
public class MainAdapter extends WNBaseAdapter<Brand> {

    public MainAdapter(Context context, List<?> data, int itemId) {
        super(context, data, itemId);
    }

    @Override
    public void convertView(WNViewHolder holder, Brand brand, int position) {

        ImageView brand_img = holder.getView(R.id.brand_img);
        TextView brand_name = holder.getView(R.id.brand_name);

        Glide.with(mContext)
                .load(brand.imgUrl)
                .into(brand_img);

        brand_name.setText(convertString(brand.name));
    }
}
