package com.example.myapplication.ui.login;

import static com.google.android.material.internal.ContextUtils.getActivity;

import android.Manifest;
import android.app.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.InApp;
import com.example.myapplication.R;
import com.example.myapplication.databinding.ActivityLoginBinding;

import java.util.Objects;

public class LoginActivity extends AppCompatActivity {
    private View vv;
    protected String username, password, roomNumber;
    private LoginViewModel loginViewModel;
    private ActivityLoginBinding binding;

    private static final int PERMISSION_REQUEST_BLUETOOTH = 1;

    private static final int REQUEST_ENABLE_BT = 1;

    private static final int PERMISSION_REQUEST_CODE = 2;
    public static final int REQUEST_LOCATION_PERMISSION = 1;

    private BluetoothAdapter mBluetoothAdapter;
    private static final String PREFS_NAME = "preferences";
    private static final String PREF_UNAME = "Username";
    private static final String PREF_PASSWORD = "Password";

    private static SharedPreferences settings;
    private final String DefaultUnameValue = "";
    private String UnameValue;

    private final String DefaultPasswordValue = "";
    private String PasswordValue;
    private boolean isLoggedIn = false;



    @Override
    public void onPause() {
        super.onPause();
        savePreferences();

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Objects.requireNonNull(getSupportActionBar()).hide();
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // check the permission and grant for them
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        requestPermission();

        // loginView
        loginViewModel = new ViewModelProvider(this, new LoginViewModelFactory())
                .get(LoginViewModel.class);

        final EditText usernameEditText = binding.username;
        final EditText passwordEditText = binding.password;
        final Button loginButton = binding.login;
        final ProgressBar loadingProgressBar = binding.loading;

        settings = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.UnameValue = settings.getString(PREF_UNAME, DefaultUnameValue);
        this.PasswordValue = settings.getString(PREF_PASSWORD, DefaultPasswordValue);


        if (savedInstanceState != null) {
            isLoggedIn = savedInstanceState.getBoolean("isLoggedIn", false);
            if (isLoggedIn) {
                // Đăng nhập thành công, chuyển sang activity khác
                Intent intent = new Intent(LoginActivity.this, InApp.class);
                startActivity(intent);
            }
        }

        loginViewModel.getLoginFormState().observe(this, new Observer<LoginFormState>() {
            @Override
            public void onChanged(@Nullable LoginFormState loginFormState) {
                if (loginFormState == null) {
                    return;
                }
                loginButton.setEnabled(loginFormState.isDataValid());
                if (loginFormState.getUsernameError() != null) {
                    usernameEditText.setError(getString(loginFormState.getUsernameError()));
                }
                if (loginFormState.getPasswordError() != null) {
                    passwordEditText.setError(getString(loginFormState.getPasswordError()));
                }
            }
        });

        loginViewModel.getLoginResult().observe(this, new Observer<LoginResult>() {
            @Override
            public void onChanged(@Nullable LoginResult loginResult) {
                if (loginResult == null) {
                    return;
                }
                loadingProgressBar.setVisibility(View.GONE);
                if (loginResult.getError() != null) {
                    showLoginFailed(loginResult.getError());
                }
                if (loginResult.getSuccess() != null) {
                    updateUiWithUser(loginResult.getSuccess());
                    switchActivities(loginResult.getSuccess().roomNumber, loginResult.getSuccess().linkLocal);
                    UnameValue = username;
                    PasswordValue = password;
                }
                setResult(Activity.RESULT_OK);

            }
        });

        TextWatcher afterTextChangedListener = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // ignore
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // ignore
            }

            @Override
            public void afterTextChanged(Editable s) {
                loginViewModel.loginDataChanged(usernameEditText.getText().toString(),
                        passwordEditText.getText().toString());
                getFields(usernameEditText.getText().toString(), passwordEditText.getText().toString());
            }
        };

        if(!UnameValue.equals("") && !PasswordValue.equals("")) {
            this.username = UnameValue;
            this.password = PasswordValue;
            usernameEditText.setText(UnameValue);
            passwordEditText.setText(PasswordValue);
            loginViewModel.login(UnameValue, PasswordValue);
        } else {
            usernameEditText.addTextChangedListener(afterTextChangedListener);
            passwordEditText.addTextChangedListener(afterTextChangedListener);
            passwordEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {

                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if (actionId == EditorInfo.IME_ACTION_DONE) {
                        loginViewModel.login(usernameEditText.getText().toString(),
                                passwordEditText.getText().toString());
                    }
                    return false;
                }
            });
        }

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadingProgressBar.setVisibility(View.VISIBLE);
                loginViewModel.login(usernameEditText.getText().toString(),
                        passwordEditText.getText().toString());
            }
        });
    }

    private void updateUiWithUser(LoggedInUserView model) {
        String welcome = getString(R.string.welcome) + model.getDisplayName();
        // TODO : initiate successful logged in experience
//        Toast.makeText(getApplicationContext(), welcome, Toast.LENGTH_LONG).show();
    }


    private void switchActivities(String roomNumber, String linkLocal) {
        isLoggedIn = true;
        Intent switchActivityIntent = new Intent(LoginActivity.this, InApp.class);
        switchActivityIntent.putExtra("Username", this.username);
        switchActivityIntent.putExtra("Password", this.password);
        switchActivityIntent.putExtra("RoomNumber", roomNumber);
        switchActivityIntent.putExtra("lickLocal", linkLocal);

        startActivity(switchActivityIntent);
//        finish();
    }
    private void getFields(String username, String password) {

        this.username = username;
        this.password = password;

    }
    private void showLoginFailed(@StringRes Integer errorString) {
        Toast.makeText(getApplicationContext(), errorString, Toast.LENGTH_SHORT).show();
    }

    private void savePreferences() {
//        SharedPreferences settings = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();

        // Edit and commit
        System.out.println("Save name: " + this.UnameValue);
        System.out.println("Save password: " + this.PasswordValue);

        editor.putString(PREF_UNAME, this.UnameValue);
        editor.putString(PREF_PASSWORD, this.PasswordValue);
        editor.apply();
    }


    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("isLoggedIn", isLoggedIn);
    }


    private void requestPermission(){
//        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
//        System.out.println("Hello");
        // Kiểm tra xem Bluetooth được hỗ trợ hay không
        if (mBluetoothAdapter == null) {
            // Thiết bị không hỗ trợ Bluetooth
        } else if (!mBluetoothAdapter.isEnabled()) {
            // Bluetooth đã tắt, yêu cầu người dùng bật Bluetooth
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH)!= PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH}, PERMISSION_REQUEST_CODE);
            }

            if(Build.VERSION_CODES.M <= Build.VERSION.SDK_INT && Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_CODE);

                }
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if(ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{
                                    Manifest.permission.BLUETOOTH_CONNECT,
                                    Manifest.permission.BLUETOOTH_SCAN},
                            PERMISSION_REQUEST_CODE);
                }
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_LOCATION_PERMISSION);
                }
            }
        }

    }

    // Xử lý phản hồi từ người dùng
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                System.out.println("Bluetooth is activated");
            } else {
                // Bluetooth không được bật
            }
        }
    }

}