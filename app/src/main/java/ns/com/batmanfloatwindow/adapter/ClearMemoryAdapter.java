package ns.com.batmanfloatwindow.adapter;

import android.view.View;
import android.widget.RadioButton;


import java.util.List;

import ns.com.batmanfloatwindow.R;
import ns.com.batmanfloatwindow.bean.AppProcessInfo;
import ns.com.batmanfloatwindow.util.StorageUtil;

public class ClearMemoryAdapter extends BaseListRvAdapter<AppProcessInfo> {


    public ClearMemoryAdapter(List<AppProcessInfo> data) {
        super(data);
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }


    @Override
    public int getItemResId() {
        return R.layout.listview_memory_clean;
    }

    @Override
    public void bindBodyData(AutoViewHolder holder, int bodyPos, final AppProcessInfo data) {
        holder.imageDrawable(R.id.image,data.icon);
        holder.text(R.id.name,data.appName);
        holder.text(R.id.memory,StorageUtil.convertStorage(data.memory));
        RadioButton radioButton =holder.get(R.id.choice_radio);
        radioButton.setChecked(data.checked?true:false);
        radioButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (data.checked) {
                    data.checked = false;
                } else {
                    data.checked = true;
                }
                notifyDataSetChanged();
            }
        });
    }

}
