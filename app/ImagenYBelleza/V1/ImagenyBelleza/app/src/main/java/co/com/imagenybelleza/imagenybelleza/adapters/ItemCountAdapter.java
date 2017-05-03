package co.com.imagenybelleza.imagenybelleza.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import java.util.List;

import co.com.imagenybelleza.imagenybelleza.R;
import co.com.imagenybelleza.imagenybelleza.helpers.RobotoRegularTextView;
import co.com.imagenybelleza.imagenybelleza.models.Count;

/**
 * Created by Manuela Duque M on 14/03/2017.
 */

public class ItemCountAdapter extends ArrayAdapter<Count> {

    private List<Count> counts;
    private int resource;
    private Context context;

    public ItemCountAdapter(Context context, int resource, List<Count> counts) {
        super(context, resource, counts);
        this.context = context;
        this.resource = resource;
        this.counts = counts;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = View.inflate(context, resource, null);

            holder.cdbLabel = (RobotoRegularTextView) convertView.findViewById(R.id.cdb_label);
            holder.localLabel = (RobotoRegularTextView) convertView.findViewById(R.id.local_label);
            holder.tableLabel = (RobotoRegularTextView) convertView.findViewById(R.id.table_label);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Count count = counts.get(position);
        if (count != null) {
            holder.cdbLabel.setText(String.valueOf(count.getCdb()));
            holder.localLabel.setText(String.valueOf(count.getLocal()));
            holder.tableLabel.setText(count.getTable());
        }

        return convertView;
    }

    private class ViewHolder {
        RobotoRegularTextView tableLabel, localLabel, cdbLabel;
    }
}
