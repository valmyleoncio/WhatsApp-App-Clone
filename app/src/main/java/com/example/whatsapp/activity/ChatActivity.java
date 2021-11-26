package com.example.whatsapp.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import com.bumptech.glide.Glide;
import com.example.whatsapp.adapter.MensagensAdapter;
import com.example.whatsapp.config.ConfiguracaoFirebase;
import com.example.whatsapp.helper.Base64Custom;
import com.example.whatsapp.helper.UsuarioFirebase;
import com.example.whatsapp.model.Conversa;
import com.example.whatsapp.model.Grupo;
import com.example.whatsapp.model.Mensagem;
import com.example.whatsapp.model.Usuario;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.whatsapp.R;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private TextView textViewNome;
    private CircleImageView circleImageViewFoto;
    private Usuario usuarioDestinatario;
    private Usuario usuarioRemetente;
    private ImageButton voltar;
    private EditText editMensagem;
    private ImageView imageCamera;
    private RecyclerView recyclerMensagens;
    private List<Mensagem> listaMensagens = new ArrayList<>();
    private MensagensAdapter adapter;
    private DatabaseReference database;
    private DatabaseReference mensagensRef;
    private ChildEventListener childEventListenerMensagens;
    private static final int SELECAO_CAMERA = 100;
    private StorageReference storage;
    private Grupo grupo;

    //identificador usuarios remetente e destinatario
    private String idUsuarioRemetente;
    private String idUsuarioDestinatario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        //Configurações iniciais
        textViewNome = findViewById(R.id.textNomeChat);
        circleImageViewFoto = findViewById(R.id.circleImageFotoChat);
        voltar = findViewById(R.id.imageButtonBack);
        editMensagem = findViewById(R.id.editMensagem);
        recyclerMensagens = findViewById(R.id.recyclerMensagens);
        imageCamera = findViewById(R.id.imageCamera);
        storage = ConfiguracaoFirebase.getFirebaseStorage();



        //Recuperar dados do usuário remetente
        idUsuarioRemetente = UsuarioFirebase.getUsuarioIdentificador();
        usuarioRemetente = UsuarioFirebase.getDadosUsuarioLogado();

        //Recuperar dados do usuário destinatario
        Bundle bundle = getIntent().getExtras();
        if (bundle != null){

            if( bundle.containsKey("chatGrupo") ){

                grupo = (Grupo) bundle.getSerializable("chatGrupo");
                idUsuarioDestinatario = grupo.getId();
                textViewNome.setText( grupo.getNome());


                String foto = grupo.getFoto();
                if (foto != null){

                    Glide.with(ChatActivity.this).load( Uri.parse(foto) ).into( circleImageViewFoto );

                }else{
                    circleImageViewFoto.setImageResource(R.drawable.padrao);
                }

            }else{

                usuarioDestinatario = (Usuario) bundle.getSerializable("chatContato");
                textViewNome.setText( usuarioDestinatario.getNome() );
                String foto = usuarioDestinatario.getFoto();

                if (foto != null){

                    Glide.with(ChatActivity.this).load( Uri.parse(foto) ).into(circleImageViewFoto);

                }else{
                    circleImageViewFoto.setImageResource(R.drawable.padrao);
                }

                //Recuperando id do usuario destinatario
                idUsuarioDestinatario = Base64Custom.codificarBase64( usuarioDestinatario.getEmail() );

            }

        }


        //Configuração do adapter
        adapter = new MensagensAdapter(listaMensagens, ChatActivity.this);

        //Configuração do RecyclerView
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerMensagens.setLayoutManager( layoutManager );
        recyclerMensagens.setHasFixedSize( true );
        recyclerMensagens.setAdapter( adapter );


        database = ConfiguracaoFirebase.getFirebaseDatabase();
        mensagensRef = database.child("Mensagens")
                .child( idUsuarioRemetente )
                .child( idUsuarioDestinatario );


        //Evento de clique na camera
        imageCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if(i.resolveActivity(getPackageManager()) != null){
                    startActivityForResult(i, SELECAO_CAMERA);
                }
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Bitmap imagem = null;

        if ( resultCode == RESULT_OK) {

            try
            {
                switch (requestCode)
                {
                    case SELECAO_CAMERA:
                        imagem = (Bitmap) data.getExtras().get("data");
                        break;
                }

                if (imagem != null){

                    //Recuperar dados da imagem para o firebase
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    imagem.compress(Bitmap.CompressFormat.JPEG, 70, baos);
                    byte[] dadosImagem = baos.toByteArray();

                    //Criar nome da imagem
                    String nomeImagem = UUID.randomUUID().toString();

                    //Configurar referencia do firebase
                    final StorageReference imagemRef = storage.child("Imagens")
                            .child("fotos")
                            .child(idUsuarioRemetente)
                            .child( nomeImagem );

                    UploadTask uploadTask = imagemRef.putBytes( dadosImagem );
                    uploadTask.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {



                        }
                    }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            imagemRef.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>()
                            {
                                @Override
                                public void onComplete(@NonNull Task<Uri> task)
                                {
                                    String dowloadUrl = task.getResult().toString();


                                    if( usuarioDestinatario != null ){ //mensagem normal

                                        Mensagem mensagem = new Mensagem();
                                        mensagem.setIdUsuario( idUsuarioRemetente );
                                        mensagem.setMensagem("imagem.jpeg");
                                        mensagem.setImagem( dowloadUrl );

                                        //Salvar Mensagem para o Remetente
                                        salvarMensagem(idUsuarioRemetente, idUsuarioDestinatario, mensagem );

                                        //Salvar Mensagem para o Destinatário
                                        salvarMensagem(idUsuarioDestinatario, idUsuarioRemetente, mensagem );

                                    }else{ //mensagem em grupo

                                        for( Usuario membro: grupo.getMembros()){

                                            String idRemetenteGrupo = Base64Custom.codificarBase64( membro.getEmail() );
                                            String idUsuarioLogadoGrupo = UsuarioFirebase.getUsuarioIdentificador();

                                            Mensagem mensagem = new Mensagem();
                                            mensagem.setIdUsuario( idUsuarioLogadoGrupo );
                                            mensagem.setMensagem( "imagem.jpeg" );
                                            mensagem.setNome( usuarioRemetente.getNome() );
                                            mensagem.setImagem( dowloadUrl );

                                            //salvar mensagem para o membro
                                            salvarMensagem( idRemetenteGrupo, idUsuarioDestinatario, mensagem );

                                            //Salvar conversa
                                            salvarConversa( mensagem, true );
                                        }

                                    }

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

    private void salvarConversa( Mensagem msg, boolean isGroup){

        Conversa conversaRemetente = new Conversa();
        Conversa conversaDestinatario = new Conversa();

        conversaRemetente.setIdRemetente( idUsuarioRemetente);
        conversaRemetente.setIdDestinatario( idUsuarioDestinatario );
        conversaRemetente.setUltimaMensagem( msg.getMensagem() );

        conversaDestinatario.setIdRemetente( idUsuarioDestinatario);
        conversaDestinatario.setIdDestinatario( idUsuarioRemetente );
        conversaDestinatario.setUltimaMensagem( msg.getMensagem() );

        if( isGroup ){ //Conversa de grupo

            conversaRemetente.setIsGroup("true");
            conversaRemetente.setGrupo( grupo );

            conversaDestinatario.setIsGroup("true");
            conversaDestinatario.setGrupo( grupo );


        }else{ //Conversa Normal

            conversaRemetente.setUsuarioExibicao( usuarioDestinatario );
            conversaRemetente.setIsGroup("false");

            conversaDestinatario.setUsuarioExibicao( UsuarioFirebase.getDadosUsuarioLogado());
            conversaDestinatario.setIsGroup("false");
        }

        conversaRemetente.salvar();
        conversaDestinatario.salvar();

    }

    @Override
    protected void onStart() {
        super.onStart();
        recuperarMensagens();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mensagensRef.removeEventListener( childEventListenerMensagens);
    }


    public void voltar(View view){
        Intent i = new Intent(this, MainActivity.class);
        startActivity(i);
    }

    public void enviarMensagem(View view){

        String textoMensagem = editMensagem.getText().toString();

        if( !textoMensagem.isEmpty() ){

            if( usuarioDestinatario != null ){

                Mensagem mensagem = new Mensagem();
                mensagem.setIdUsuario( idUsuarioRemetente);
                mensagem.setMensagem( textoMensagem );

                //Salvar Mensagem para o Remetente
                salvarMensagem(idUsuarioRemetente, idUsuarioDestinatario, mensagem );

                //Salvar Mensagem para o Destinatário
                salvarMensagem(idUsuarioDestinatario, idUsuarioRemetente, mensagem );

                //Salvar conversa
                salvarConversa( mensagem, false );

            }else{

                for( Usuario membro: grupo.getMembros()){

                    String idRemetenteGrupo = Base64Custom.codificarBase64( membro.getEmail() );
                    String idUsuarioLogadoGrupo = UsuarioFirebase.getUsuarioIdentificador();

                    Mensagem mensagem = new Mensagem();
                    mensagem.setIdUsuario( idUsuarioLogadoGrupo );
                    mensagem.setMensagem( textoMensagem );
                    mensagem.setNome( usuarioRemetente.getNome() );

                    //salvar mensagem para o membro
                    salvarMensagem( idRemetenteGrupo, idUsuarioDestinatario, mensagem );

                    //Salvar conversa
                    salvarConversa( mensagem, true );
                }
            }
        }else{ }
    }

    private void salvarMensagem(String remetente, String destinatario, Mensagem mensagem){

        database = ConfiguracaoFirebase.getFirebaseDatabase();
        mensagensRef = database.child("Mensagens");

        mensagensRef.child( remetente )
                    .child( destinatario )
                    .push()
                    .setValue(mensagem);

        //limpar texto
        editMensagem.setText("");
    }

    private void recuperarMensagens(){

        listaMensagens.clear();

        childEventListenerMensagens = mensagensRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                Mensagem mensagem = dataSnapshot.getValue( Mensagem.class);
                listaMensagens.add( mensagem );

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}