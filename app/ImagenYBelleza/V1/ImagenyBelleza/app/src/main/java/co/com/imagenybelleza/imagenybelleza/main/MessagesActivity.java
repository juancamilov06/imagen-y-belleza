package co.com.imagenybelleza.imagenybelleza.main;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import co.com.imagenybelleza.imagenybelleza.R;
import co.com.imagenybelleza.imagenybelleza.adapters.MessagesAdapter;
import co.com.imagenybelleza.imagenybelleza.database.DatabaseHelper;
import co.com.imagenybelleza.imagenybelleza.enums.Url;
import co.com.imagenybelleza.imagenybelleza.helpers.ConnectivityReceiver;
import co.com.imagenybelleza.imagenybelleza.helpers.Utils;
import co.com.imagenybelleza.imagenybelleza.models.Message;

public class MessagesActivity extends AppCompatActivity {

    private ListView messagesListView;
    private List<Message> messages;
    private MessagesActivity context;
    private DatabaseHelper database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messages);

        messagesListView = (ListView) findViewById(R.id.messages_list_view);
        context = MessagesActivity.this;
        database = new DatabaseHelper(context);

        getData();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MessagesActivity.this);
                builder.setTitle("Mensajes");
                builder.setMessage("Marcar todos como leidos");
                builder.setPositiveButton("Si", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        final Dialog loadingDialog = Utils.getAlertDialog(context);
                        loadingDialog.show();
                        RequestQueue queue = Volley.newRequestQueue(context);
                        StringRequest request = new StringRequest(Request.Method.POST, database.getIpAdress() + Url.SET_READ_MESSAGES_URL, new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                loadingDialog.dismiss();
                                try{
                                    System.out.println(response);
                                    JSONObject objectResponse = new JSONObject(response);
                                    String message = objectResponse.getString("mensaje");

                                    if (message.equals("Creacion exitosa")){
                                        Toast.makeText(context, "Todos los mensajes se han marcado como leidos", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(context, "Error, intente de nuevo mas tarde", Toast.LENGTH_SHORT).show();
                                    }

                                } catch (Exception e){
                                    e.printStackTrace();
                                    Toast.makeText(context, "Error, intente de nuevo mas tarde", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                loadingDialog.dismiss();
                                Toast.makeText(context, "Error interno del servidor", Toast.LENGTH_SHORT).show();
                            }
                        }){
                            @Override
                            public Priority getPriority() {
                                return Priority.IMMEDIATE;
                            }
                        };
                        queue.add(request);
                    }
                });
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });
    }

    private void getData() {
        if (ConnectivityReceiver.isConnected()) {
            final Dialog dialog = Utils.getAlertDialog(context);
            dialog.show();
            RequestQueue queue = Volley.newRequestQueue(context);
            StringRequest request = new StringRequest(Request.Method.GET, database.getIpAdress() + Url.GET_MESSAGES_SERVICE_URL + "?unread=false", new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    System.out.println("Respuesta: " + response);
                    dialog.dismiss();
                    try {
                        JSONObject object = new JSONObject(response);
                        JSONArray messagesResponse = object.getJSONArray("messages");
                        if (messagesResponse.length() > 0) {
                            messages = new ArrayList<>();
                            for (int i = 0; i < messagesResponse.length(); i++) {
                                JSONObject messageObject = messagesResponse.getJSONObject(i);
                                Message message = new Message();
                                message.setId(messageObject.getInt("id"));
                                message.setMessage(messageObject.getString("message"));
                                message.setRead(messageObject.getInt("is_read") > 0);
                                message.setDate(messageObject.getString("created"));

                                messages.add(message);
                            }
                            messagesListView.setAdapter(new MessagesAdapter(context, R.layout.item_message, messages));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Utils.showSnackbar("Error, intenta de nuevo", MessagesActivity.this, R.id.activity_messages);
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    dialog.dismiss();
                }
            });
            queue.add(request);
        } else {
            Utils.showSnackbar("Activa la conexion a internet e intenta de nuevo", MessagesActivity.this, R.id.activity_messages);
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    finish();
                }
            }, 3000);
        }
    }

}
