package com.example.whatsapp.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import com.example.whatsapp.adapter.GrupoSelecionadoAdapter;
import com.example.whatsapp.config.ConfiguracaoFirebase;
import com.example.whatsapp.helper.UsuarioFirebase;
import com.example.whatsapp.model.Grupo;
import com.example.whatsapp.model.Usuario;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.whatsapp.R;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class CadastroGrupoActivity extends AppCompatActivity {

    private List<Usuario> listaMembrosSelecionados = new ArrayList<>();
    private TextView textTotalParticipantes;
    private GrupoSelecionadoAdapter grupoSelecionadoAdapter;
    private RecyclerView recyclerMembros;
    private CircleImageView imageGrupo;
    private FloatingActionButton fabSalvarGrupo;
    private EditText editNomeGrupo;
    private static final int SELECAO_GALERIA = 200;
    private StorageReference storageReference;
    private Grupo grupo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro_grupo);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Novo grupo");
        toolbar.setSubtitle("Defina o nome");
        setSupportActionBar(toolbar);

        //Configuracoes iniciais

        textTotalParticipantes = findViewById(R.id.textTotalParticipantes);
        recyclerMembros        = findViewById(R.id.recyclerMembrosGrupo);
        imageGrupo             = findViewById(R.id.imageGrupo);
        fabSalvarGrupo         = findViewById(R.id.fabSalvarCadastro);
        storageReference       = ConfiguracaoFirebase.getFirebaseStorage();
        editNomeGrupo          = findViewById(R.id.editNomeGrupo);
        grupo = new Grupo();


        //Configurar evento de clique imagem do grupo
        imageGrupo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                if(i.resolveActivity(getPackageManager()) != null){
                    startActivityForResult(i, SELECAO_GALERIA);
                }

            }
        });



        //Recuperar lista  de membros passada
        if (getIntent().getExtras() != null){

            List<Usuario> membros = (List<Usuario>) getIntent().getExtras().getSerializable( "Membros");
            listaMembrosSelecionados.addAll( membros );

            textTotalParticipantes.setText("Participantes: " + listaMembrosSelecionados.size());

        }

        //Configurar adapter
        grupoSelecionadoAdapter = new GrupoSelecionadoAdapter( listaMembrosSelecionados, getApplicationContext() );

        //Configurar RecyclerView
        RecyclerView.LayoutManager layoutManagerHorizontal = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, false );
        recyclerMembros.setLayoutManager( layoutManagerHorizontal );
        recyclerMembros.setHasFixedSize(true);
        recyclerMembros.setAdapter( grupoSelecionadoAdapter );

        //Configurar Floating action button
        fabSalvarGrupo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String nomeGrupo = editNomeGrupo.getText().toString();

                //adiciona à lista membros tbm o proprio usuario que está logado
                listaMembrosSelecionados.add(UsuarioFirebase.getDadosUsuarioLogado());

                grupo.setMembros( listaMembrosSelecionados );
                grupo.setNome( nomeGrupo );
                grupo.salvar();

                Intent i = new Intent(CadastroGrupoActivity.this, ChatActivity.class);
                i.putExtra("chatGrupo", grupo);
                startActivity(i);
            }
        });
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if ( resultCode == RESULT_OK){
            Bitmap imagem = null;

            try {

                Uri localImagemSelecionada = data.getData();
                imagem = MediaStore.Images.Media.getBitmap(getContentResolver(), localImagemSelecionada);

                if (imagem != null){
                    imageGrupo.setImageBitmap( imagem );

                    //Recuperar dados da imagem para o firebase
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    imagem.compress(Bitmap.CompressFormat.JPEG, 70, baos);
                    byte[] dadosImagem = baos.toByteArray();

                    //salvar imagem no firebase
                    final StorageReference imagemRef = storageReference
                            .child("Imagens")
                            .child("Grupos")
                            .child(grupo.getId() + ".jpeg");

                    UploadTask uploadTask = imagemRef.putBytes( dadosImagem );

                    uploadTask.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                            Toast.makeText(CadastroGrupoActivity.this,
                                    "Erro ao fazer upload da imagem",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            Toast.makeText(CadastroGrupoActivity.this,
                                    "Sucesso ao fazer upload da imagem",
                                    Toast.LENGTH_SHORT).show();

                            imagemRef.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>()
                            {
                                @Override
                                public void onComplete(@NonNull Task<Uri> task)
                                {

                                    Uri uri= task.getResult();

                                    String url = uri.toString();
                                    grupo.setFoto( url );
                                }
                            });
                        }
                    });

                }

            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }
}