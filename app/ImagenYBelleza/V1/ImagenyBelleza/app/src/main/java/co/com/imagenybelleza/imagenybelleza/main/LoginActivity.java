package co.com.imagenybelleza.imagenybelleza.main;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.wang.avi.AVLoadingIndicatorView;

import co.com.imagenybelleza.imagenybelleza.R;
import co.com.imagenybelleza.imagenybelleza.database.DatabaseHelper;
import co.com.imagenybelleza.imagenybelleza.enums.Roles;
import co.com.imagenybelleza.imagenybelleza.helpers.GPSReceiver;
import co.com.imagenybelleza.imagenybelleza.helpers.Utils;
import co.com.imagenybelleza.imagenybelleza.models.User;
import co.com.imagenybelleza.imagenybelleza.services.LocationService;

public class LoginActivity extends AppCompatActivity {

    private Button loginButton;
    private EditText passwordInput, userInput;
    private Context context;
    private Dialog dialog;
    private DatabaseHelper database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        context = LoginActivity.this;
        database = new DatabaseHelper(context);

        userInput = (EditText) findViewById(R.id.user_input);
        passwordInput = (EditText) findViewById(R.id.password_input);
        loginButton = (Button) findViewById(R.id.sign_in_button);


        dialog = new Dialog(context, R.style.LoadDialog);
        View view = View.inflate(context, R.layout.dialog_loading, null);
        dialog.setContentView(view);
        AVLoadingIndicatorView loadingIndicatorView = (AVLoadingIndicatorView) dialog.findViewById(R.id.loading_indicator);
        loadingIndicatorView.smoothToShow();

        loginButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.show();
                final String password = passwordInput.getText().toString();
                final String username = userInput.getText().toString();

                if (TextUtils.isEmpty(password) || TextUtils.isEmpty(username)) {
                    Utils.showSnackbar("Debes ingresar todos los campos", LoginActivity.this, R.id.login_form);
                    dialog.dismiss();
                    return;
                }

                if (database.startSession(username, password)) {
                    dialog.dismiss();
                    User user = database.getCurrentUser();
                    if (user.getRole().equals(Roles.ROLE_ADMIN)) {
                        startService(new Intent(LoginActivity.this, LocationService.class));
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        finish();
                    } else if (user.getRole().equals(Roles.ROLE_SELLER)) {
                        if (GPSReceiver.isGpsEnabled()) {
                            startService(new Intent(LoginActivity.this, LocationService.class));
                            startActivity(new Intent(LoginActivity.this, OrderActivity.class));
                            finish();
                        } else {
                            Utils.showSnackbar("Activa el GPS", LoginActivity.this, R.id.login_form);
                            startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), 1);
                        }
                    }
                } else {
                    dialog.dismiss();
                    Utils.showSnackbar("Usuario o contraseña incorrectos.", LoginActivity.this, R.id.login_form);
                }

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        Utils.isGpsEnabled(context);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == 1) {
            switch (requestCode) {
                case 1:
                    if (database.getCurrentUser().getRole().equals(Roles.ROLE_SELLER)) {
                        startService(new Intent(LoginActivity.this, LocationService.class));
                        startActivity(new Intent(LoginActivity.this, OrderActivity.class));
                        finish();
                    }
                    break;
            }
        } else {
            database.closeSession();
        }
    }

    @Override
    public void onBackPressed() {
        this.finishAffinity();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_login, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_info:
                startActivity(new Intent(LoginActivity.this, AboutActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}

