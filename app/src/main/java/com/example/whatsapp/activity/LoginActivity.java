package com.example.whatsapp.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.whatsapp.R;
import com.example.whatsapp.config.ConfiguracaoFirebase;
import com.example.whatsapp.model.Usuario;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText campoEmail, campoSenha;
    private FirebaseAuth autenticacao;
    private Usuario usuario;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        autenticacao = ConfiguracaoFirebase.getFirebaseAuth();

        campoEmail = findViewById(R.id.editEmail);
        campoSenha = findViewById(R.id.editSenha);



    }


    public void ValidarAutenticacaoUsuario(View view) {

        //Recuperar textos dos campos
        String email = campoEmail.getText().toString();
        String senha = campoSenha.getText().toString();

        if( !email.isEmpty() )
        {

            if( !senha.isEmpty() ) {

                usuario = new Usuario(email, senha);
                validarLogin();


            }else {
                Toast.makeText(LoginActivity.this, "Preencha a senha!", Toast.LENGTH_SHORT).show();
            }

        }else {
            Toast.makeText(LoginActivity.this, "Preencha o email!", Toast.LENGTH_SHORT).show();
        }

    }

    public void validarLogin() {
        autenticacao.signInWithEmailAndPassword(usuario.getEmail(), usuario.getSenha()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task)
            {
                if (task.isSuccessful())
                {
                    abrirTelaPrincipal();
                }
                else
                {
                    String excecao = "";

                    try
                    {
                        throw task.getException();

                    }catch (FirebaseAuthInvalidUserException e)
                    {
                        excecao = "Usuário não está cadastrado!";

                    }catch ( FirebaseAuthInvalidCredentialsException e)
                    {
                        excecao = "Email e senha não correspondem a um usuário cadastrado!";

                    }catch (Exception e)
                    {
                        excecao = "Erro ao cadastrar usuário!" + e.getMessage();

                    }

                    Toast.makeText(LoginActivity.this, excecao, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void abrirTelaPrincipal() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    public void abrirTelaCadastro(View view) {
        Intent intent = new Intent(LoginActivity.this, CadastroActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onStart() {

        if(autenticacao.getCurrentUser() != null) {
           abrirTelaPrincipal();

        }

        super.onStart();
    }
}