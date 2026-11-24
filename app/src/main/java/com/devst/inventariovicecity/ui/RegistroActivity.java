package com.devst.inventariovicecity.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.devst.inventariovicecity.R;
import com.google.firebase.auth.FirebaseAuth;

public class RegistroActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private EditText etEmail, etPassword, etConfirmar;
    private Button btnRegistrar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);

        auth = FirebaseAuth.getInstance();
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmar = findViewById(R.id.etConfirmar);
        btnRegistrar = findViewById(R.id.btnRegistrar);
        TextView tvIrLogin = findViewById(R.id.tvIrLogin);

        btnRegistrar.setOnClickListener(v -> registrar());
        tvIrLogin.setOnClickListener(v -> finish());
    }

    private void registrar() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString();
        String confirmar = etConfirmar.getText().toString();

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Correo no válido");
            return;
        }
        if (password.length() < 6) {
            etPassword.setError("Mínimo 6 caracteres");
            return;
        }
        if (!password.equals(confirmar)) {
            etConfirmar.setError("Las contraseñas no coinciden");
            return;
        }

        btnRegistrar.setEnabled(false);
        auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(r -> {
                    Toast.makeText(this, "Cuenta creada", Toast.LENGTH_SHORT).show();
                    Intent i = new Intent(this, MainActivity.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(i);
                    finish();
                })
                .addOnFailureListener(e -> {
                    btnRegistrar.setEnabled(true);
                    Toast.makeText(this, "No se pudo registrar: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }
}
