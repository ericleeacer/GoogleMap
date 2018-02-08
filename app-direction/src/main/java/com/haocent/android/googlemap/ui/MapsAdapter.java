package com.haocent.android.googlemap.ui;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.haocent.android.googlemap.R;
import com.haocent.android.googlemap.data.GoogleDirectionBean;

import java.net.URL;
import java.util.List;

/**
 * Created by Tnno Wu on 2018/02/07.
 */

public class MapsAdapter extends RecyclerView.Adapter<MapsAdapter.MapsViewHolder> {

    private Context mContext;

    private GoogleDirectionBean mBean;

    private List<GoogleDirectionBean.RoutesBean.LegsBean.StepsBean> mList;

    public MapsAdapter(Context context) {
        mContext = context;
    }

    public void setData(GoogleDirectionBean bean) {
        mBean = bean;

        mList = bean.getRoutes().get(0).getLegs().get(0).getSteps();

        notifyDataSetChanged();
    }

    @Override
    public MapsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_steps, parent, false);
        return new MapsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MapsViewHolder holder, int position) {
        holder.tvNum.setText(position + 1 + "");

        holder.tvDistance.setText("距离 " + mBean.getRoutes().get(0).getLegs().get(0).getSteps().get(position).getDistance().getText());

        holder.tvDuration.setText("用时 " + mBean.getRoutes().get(0).getLegs().get(0).getSteps().get(position).getDuration().getText());

        String travelMode = mBean.getRoutes().get(0).getLegs().get(0).getSteps().get(position).getTravel_mode() + " ";
        holder.tvInstructions.setText(travelMode + " " + Html.fromHtml(mBean.getRoutes().get(0).getLegs().get(0).getSteps().get(position).getHtml_instructions(), imageGetter, null));
    }

    @Override
    public int getItemCount() {
        return mList == null ? 0 : mList.size();
    }

    public class MapsViewHolder extends RecyclerView.ViewHolder {

        TextView tvNum, tvDistance, tvDuration, tvInstructions;

        public MapsViewHolder(View itemView) {
            super(itemView);
            tvNum = itemView.findViewById(R.id.tv_num);
            tvDistance = itemView.findViewById(R.id.tv_distance);
            tvDuration = itemView.findViewById(R.id.tv_duration);
            tvInstructions = itemView.findViewById(R.id.tv_instructions);
        }
    }

    final Html.ImageGetter imageGetter = new Html.ImageGetter() {
        public Drawable getDrawable(String source) {
            Drawable drawable = null;
            URL url;
            try {
                url = new URL(source);
                drawable = Drawable.createFromStream(url.openStream(), "");
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
            drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
            return drawable;
        }
    };
}
