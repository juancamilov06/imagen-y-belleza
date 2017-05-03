package co.com.imagenybelleza.imagenybelleza.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;

import java.util.List;

import co.com.imagenybelleza.imagenybelleza.R;
import co.com.imagenybelleza.imagenybelleza.helpers.RobotoLightTextView;
import co.com.imagenybelleza.imagenybelleza.helpers.RobotoRegularTextView;
import co.com.imagenybelleza.imagenybelleza.models.Message;

/**
 * Created by Manuela Duque M on 22/03/2017.
 */

public class MessagesAdapter extends ArrayAdapter<Message> {

    private final List<Message> messages;
    private final Context context;
    private final int resource;

    public MessagesAdapter(Context context, int resource, List<Message> messages) {
        super(context, resource, messages);

        this.context = context;
        this.resource = resource;
        this.messages = messages;

    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = View.inflate(context, resource, null);

            holder.dateLabel = (RobotoLightTextView) convertView.findViewById(R.id.date_label);
            holder.headerLabel = (RobotoRegularTextView) convertView.findViewById(R.id.header_label);
            holder.readIndicator = (LinearLayout) convertView.findViewById(R.id.read_view);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Message message = messages.get(position);
        if (message != null) {
            holder.dateLabel.setText("Fecha: " + message.getDate());
            holder.headerLabel.setText(message.getMessage());
            if (message.isRead()) {
                holder.readIndicator.setVisibility(View.GONE);
            }
        }
        return convertView;
    }

    private class ViewHolder {
        RobotoLightTextView dateLabel;
        RobotoRegularTextView headerLabel;
        LinearLayout readIndicator;
    }

}
