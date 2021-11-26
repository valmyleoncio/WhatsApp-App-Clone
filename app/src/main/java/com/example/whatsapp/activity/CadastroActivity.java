package com.example.whatsapp.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.whatsapp.R;
import com.example.whatsapp.config.ConfiguracaoFirebase;
import com.example.whatsapp.helper.Base64Custom;
import com.example.whatsapp.helper.UsuarioFirebase;
import com.example.whatsapp.model.Usuario;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;

public class CadastroActivity extends AppCompatActivity
{

    private TextInputEditText campoNome, campoEmail, campoSenha;
    private FirebaseAuth autenticacao;
    private Usuario usuario;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro);

        campoNome = findViewById(R.id.editPerfilNome);
        campoEmail = findViewById(R.id.editEmail);
        campoSenha = findViewById(R.id.editSenha);
    }

    public void validarCadastroUsuario(View view)
    {
        //Recuperar textos dos campos
        String textoNome = campoNome.getText().toString();
        String textoEmail = campoEmail.getText().toString();
        String textoSenha = campoSenha.getText().toString();

        if( !textoNome.isEmpty() )
        {

            if( !textoEmail.isEmpty() )
            {

                if( !textoSenha.isEmpty() )
                {


                   usuario = new Usuario(textoNome, textoEmail, textoSenha);
                   cadastrarUsuario();


                }else {
                    Toast.makeText(CadastroActivity.this, "Preencha a senha!", Toast.LENGTH_SHORT).show();
                }

            }else {
                Toast.makeText(CadastroActivity.this, "Preencha o email!", Toast.LENGTH_SHORT).show();
            }

        }else {
            Toast.makeText(CadastroActivity.this, "Preencha o nome!", Toast.LENGTH_SHORT).show();
        }
    }

    public void cadastrarUsuario() {
        autenticacao = ConfiguracaoFirebase.getFirebaseAuth();
        autenticacao.createUserWithEmailAndPassword(usuario.getEmail(), usuario.getSenha()).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task)
            {
                if (task.isSuccessful())
                {
                    String idUsuario = Base64Custom.codificarBase64(usuario.getEmail());
                    usuario.setIdUsuario( idUsuario );
                    usuario.salvar();
                    UsuarioFirebase.atualizarNomeUsuario(usuario.getNome());
                    abrirTelaPrincipal();
                    finish();
                }
                else{

                    String excecao = "";

                    try
                    {
                        throw task.getException();

                    }catch (FirebaseAuthWeakPasswordException e)
                    {
                        excecao = "Digite uma senha mais forte!";

                    }catch (FirebaseAuthInvalidCredentialsException e)
                    {
                        excecao = "Por favor, digite um e-mail válido!";

                    }catch (FirebaseAuthUserCollisionException e)
                    {
                        excecao = "Esta conta ja foi cadastrada!";

                    }catch (Exception e)
                    {
                        excecao = "Erro ao cadastrar usuário!" + e.getMessage();
                    }

                    Toast.makeText(getApplicationContext(), excecao , Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void abrirTelaPrincipal() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}