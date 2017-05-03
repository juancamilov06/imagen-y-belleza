package co.com.imagenybelleza.imagenybelleza.main;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.common.hash.Hashing;
import com.nononsenseapps.filepicker.FilePickerActivity;

import org.json.JSONObject;

import java.io.File;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import co.com.imagenybelleza.imagenybelleza.R;
import co.com.imagenybelleza.imagenybelleza.database.DatabaseHelper;
import co.com.imagenybelleza.imagenybelleza.enums.Url;
import co.com.imagenybelleza.imagenybelleza.helpers.RobotoRegularTextView;
import co.com.imagenybelleza.imagenybelleza.helpers.Utils;
import co.com.imagenybelleza.imagenybelleza.models.User;

/*
* Actividad que permite al usuario configurar varias preferencias de la aplicacion
* Tales como la carpeta de imagenes, cambio de ip y cambio de contraseña
*
* */

public class SettingsActivity extends AppCompatActivity {

    private static final int FILE_CODE = 1;
    private Context context;
    private DatabaseHelper database;
    private TextInputEditText ipInput, pathInput, oldPasswordInput, newPasswordInput;
    private RobotoRegularTextView versionLabel;
    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        context = SettingsActivity.this;
        database = new DatabaseHelper(context);

        pathInput = (TextInputEditText) findViewById(R.id.path_input);
        pathInput.setText(database.getDirectory());

        ipInput = (TextInputEditText) findViewById(R.id.ip_input);
        ipInput.setText(database.getIpAdress());

        Button pathButton = (Button) findViewById(R.id.path_button);
        pathButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(context, FilePickerActivity.class);

                i.putExtra(FilePickerActivity.EXTRA_ALLOW_MULTIPLE, false);
                i.putExtra(FilePickerActivity.EXTRA_ALLOW_CREATE_DIR, false);
                i.putExtra(FilePickerActivity.EXTRA_MODE, FilePickerActivity.MODE_DIR);
                i.putExtra(FilePickerActivity.EXTRA_START_PATH, Environment.getExternalStorageDirectory().getPath());

                startActivityForResult(i, FILE_CODE);
            }
        });

        Button ipButton = (Button) findViewById(R.id.ip_button);
        ipButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeIpAdress();
            }
        });

        user = database.getCurrentUser();

        oldPasswordInput = (TextInputEditText) findViewById(R.id.password_old_input);
        newPasswordInput = (TextInputEditText) findViewById(R.id.password_new_input);
        Button changeButton = (Button) findViewById(R.id.change_button);
        changeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String oldPassword = oldPasswordInput.getText().toString();
                final String newPassword = newPasswordInput.getText().toString();

                if (TextUtils.isEmpty(oldPassword) || TextUtils.isEmpty(newPassword)) {
                    Utils.showSnackbar("Debes llenar todos los campos", SettingsActivity.this, R.id.activity_settings);
                    return;
                }

                if (!Hashing.sha256().hashString(oldPassword, Charset.forName("UTF-8")).toString().equals(user.getIdentificator())) {
                    Utils.showSnackbar("La contraseña que ingresaste no coincide con la actual", SettingsActivity.this, R.id.activity_settings);
                    return;
                }

                if (oldPassword.equals(newPassword)) {
                    Utils.showSnackbar("Ambas contraseñas son iguales", SettingsActivity.this, R.id.activity_settings);
                    return;
                }

                final Dialog dialog = Utils.getAlertDialog(context);
                dialog.show();

                RequestQueue queue = Volley.newRequestQueue(context);
                StringRequest request = new StringRequest(Request.Method.POST, database.getIpAdress() + Url.UPDATE_PASSWORD_SERVICE_URL, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            dialog.dismiss();
                            JSONObject jsonObject = new JSONObject(response);
                            int mensaje = Integer.valueOf(jsonObject.getString("mensaje"));
                            System.out.println("Mensaje: " + mensaje);
                            if (mensaje == 1) {
                                Utils.showSnackbar("Espera...", SettingsActivity.this, R.id.activity_settings);
                                user.setIdentificator(Hashing.sha256()
                                        .hashString(newPassword, Charset.forName("UTF-8")).toString());
                                if (database.updateUserPassword(user)) {
                                    Utils.showSnackbar("Contraseña cambiada con exito", SettingsActivity.this, R.id.activity_settings);
                                    clear();
                                    startActivity(new Intent(SettingsActivity.this, MainActivity.class));
                                    finish();
                                } else {
                                    Utils.showSnackbar("Error actualizando contraseña local, borre los datos y reinicie la app", SettingsActivity.this, R.id.activity_settings);
                                }
                            } else {
                                Utils.showSnackbar("Hubo un error en el servidor, intenta luego y verifica tu conexion", SettingsActivity.this, R.id.activity_settings);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            dialog.dismiss();
                            Utils.showSnackbar("Hubo un error en el servidor, intenta luego y verifica tu conexion", SettingsActivity.this, R.id.activity_settings);
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        dialog.dismiss();
                        Utils.showSnackbar("Hubo un error en el servidor, intenta luego y verifica tu conexion", SettingsActivity.this, R.id.activity_settings);
                    }
                }) {

                    @Override
                    protected Map<String, String> getParams() throws AuthFailureError {
                        Map<String, String> params = new HashMap<>();
                        params.put("password", Hashing.sha256()
                                .hashString(newPassword, Charset.forName("UTF-8")).toString());
                        params.put("id", String.valueOf(user.getId()));
                        return params;
                    }

                    @Override
                    public Priority getPriority() {
                        return Priority.IMMEDIATE;
                    }
                };

                queue.add(request);
            }
        });

    }

    private void clear() {
        oldPasswordInput.setText("");
        newPasswordInput.setText("");
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(SettingsActivity.this, MainActivity.class));
        finish();
    }

    private void changeIpAdress() {
        final Dialog dialog = new Dialog(context, R.style.StyledDialog);
        View view = View.inflate(context, R.layout.dialog_ip, null);
        dialog.setContentView(view);
        final TextInputEditText ipDialogInput = (TextInputEditText) dialog.findViewById(R.id.ip_input);
        ipDialogInput.setText(database.getIpAdress().replace("http://", ""));
        RobotoRegularTextView titleLabel = (RobotoRegularTextView) view.findViewById(R.id.title_label);
        titleLabel.setVisibility(View.GONE);
        Button acceptButton = (Button) dialog.findViewById(R.id.accept_button);
        acceptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String ip = ipDialogInput.getText().toString();
                if (ip.contains(" ")) {
                    Utils.showSnackbar("Ingresa una direccion valida", SettingsActivity.this, R.id.activity_settings);
                    return;
                }
                if (ip.equals("")) {
                    Utils.showSnackbar("Ingresa una direccion valida", SettingsActivity.this, R.id.activity_settings);
                    return;
                }

                if (("http://" + ip).equals(ipInput.getText().toString())) {
                    Utils.showSnackbar("Ingresa una direccion ip diferente a la actual", SettingsActivity.this, R.id.activity_settings);
                    return;
                }

                RequestQueue queue = Volley.newRequestQueue(context);
                final Dialog loading = Utils.getAlertDialog(context);
                loading.show();
                StringRequest request = new StringRequest(Request.Method.GET, "http://" + ip + Url.VERIFY_SERVICE_URL, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        loading.dismiss();
                        System.out.println(response);
                        if (response.equals("ok")) {
                            if (database.insertIp("http://" + ip)) {
                                System.out.println("Ip nueva: " + database.getIpAdress());
                                Utils.showSnackbar("Ip cambiada con exito", SettingsActivity.this, R.id.activity_settings);
                                ipInput.setText(ip);
                                dialog.dismiss();
                            } else {
                                dialog.dismiss();
                                Utils.showSnackbar("Error cambiando la ip, intenta de nuevo", SettingsActivity.this, R.id.activity_settings);
                            }
                        } else {
                            Utils.showSnackbar("La ip no responde, verifiquela", SettingsActivity.this, R.id.activity_settings);
                            dialog.dismiss();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Utils.showSnackbar("La ip no responde, verifiquela", SettingsActivity.this, R.id.activity_settings);
                        dialog.dismiss();
                        loading.dismiss();
                    }
                });
                queue.add(request);
            }
        });
        Button cancelButton = (Button) dialog.findViewById(R.id.cancel_button);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    private boolean hasImageFolder(String path) {
        File file = new File(path, "items");
        return file.exists();
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == FILE_CODE && resultCode == Activity.RESULT_OK) {
            Uri uri = data.getData();
            if (hasImageFolder(uri.getPath())) {
                if (database.insertDirectory(uri.getPath())) {
                    Utils.showSnackbar("El directorio fue cambiado con exito", SettingsActivity.this, R.id.activity_settings);
                    pathInput.setText(uri.getPath());
                } else {
                    Utils.showSnackbar("Error cambiando el directorio, intenta de nuevo", SettingsActivity.this, R.id.activity_settings);
                }
            } else {
                Utils.showSnackbar("La carpeta seleccionada no contiene el catalogo, intente nuevamente", SettingsActivity.this, R.id.activity_settings);
            }
        }
    }
}
