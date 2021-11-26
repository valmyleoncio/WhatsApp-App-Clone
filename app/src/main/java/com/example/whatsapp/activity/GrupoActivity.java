package com.example.whatsapp.activity;

import android.content.Intent;
import android.os.Bundle;

import com.example.whatsapp.adapter.ContatosAdapter;
import com.example.whatsapp.adapter.GrupoSelecionadoAdapter;
import com.example.whatsapp.config.ConfiguracaoFirebase;
import com.example.whatsapp.helper.RecyclerItemClickListener;
import com.example.whatsapp.helper.UsuarioFirebase;
import com.example.whatsapp.model.Usuario;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;

import com.example.whatsapp.R;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class GrupoActivity extends AppCompatActivity {

     private RecyclerView recyclerMembrosSelecionados, recyclerMembros;
     private ContatosAdapter contatosAdapter;
     private GrupoSelecionadoAdapter grupoSelecionadoAdapter;
     private List<Usuario> listaMembros = new ArrayList<>();
     private List<Usuario> listaMembrosSelecionados = new ArrayList<>();
     private ImageView separador;
     private ValueEventListener valueEventListenerMembros = null;
     private DatabaseReference usuarioRef;
     private FirebaseUser usuarioAtual;
     private Toolbar toolbar;
     private FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grupo);

        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Novo grupo");

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); //botao de voltar automático

        fab = findViewById(R.id.fabAvancarCadastro);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent i = new Intent( getApplicationContext(), CadastroGrupoActivity.class);
                i.putExtra("Membros", (Serializable) listaMembrosSelecionados);

                startActivity( i );
            }
        });


        //Configurações iniciais
        recyclerMembrosSelecionados = findViewById(R.id.recyclerMembrosSelecionados);
        recyclerMembros = findViewById(R.id.recyclerMembrosGrupo);
        separador = findViewById(R.id.imageSeparador);
        usuarioRef = ConfiguracaoFirebase.getFirebaseDatabase().child("Usuarios");
        usuarioAtual = UsuarioFirebase.getUsuarioAtual();




        //Configurar adapter
        contatosAdapter = new ContatosAdapter( listaMembros, getApplicationContext() );

        //Configurar RecyclerView
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager( getApplicationContext() );
        recyclerMembros.setLayoutManager( layoutManager );
        recyclerMembros.setHasFixedSize(true);
        recyclerMembros.setAdapter( contatosAdapter );

        recyclerMembros.addOnItemTouchListener(new RecyclerItemClickListener(getApplicationContext(), recyclerMembros, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {

                Usuario usuarioSelecionado = listaMembros.get( position );

                //Adicionar usuario na nova lista de selecionados
                listaMembrosSelecionados.add( usuarioSelecionado );
                grupoSelecionadoAdapter.notifyDataSetChanged();

                //Remover usuario selecionado da lista
                listaMembros.remove( usuarioSelecionado );
                contatosAdapter.notifyDataSetChanged();

                atualizarMembrosToolbar();
            }

            @Override
            public void onLongItemClick(View view, int position) {

            }

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
        }));




        //Configurar adapter
        grupoSelecionadoAdapter = new GrupoSelecionadoAdapter( listaMembrosSelecionados, getApplicationContext() );

        //Configurar RecyclerView
        RecyclerView.LayoutManager layoutManagerHorizontal = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, false );
        recyclerMembrosSelecionados.setLayoutManager( layoutManagerHorizontal );
        recyclerMembrosSelecionados.setHasFixedSize(true);
        recyclerMembrosSelecionados.setAdapter( grupoSelecionadoAdapter );

        recyclerMembrosSelecionados.addOnItemTouchListener(new RecyclerItemClickListener(getApplicationContext(), recyclerMembrosSelecionados, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {

                Usuario usuarioJaSelecionado = listaMembrosSelecionados.get( position );

                //Remover usuario Selecionado
                listaMembrosSelecionados.remove( usuarioJaSelecionado );
                grupoSelecionadoAdapter.notifyDataSetChanged();

                //Retornar usuario para a lista de contatos
                listaMembros.add( usuarioJaSelecionado);
                contatosAdapter.notifyDataSetChanged();

                atualizarMembrosToolbar();
            }

            @Override
            public void onLongItemClick(View view, int position) {

            }

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
        }));

    }

    @Override
    public void onStart() {
        super.onStart();
        recuperarContatos();
    }

    @Override
    public void onStop() {
        super.onStop();
        usuarioRef.removeEventListener( valueEventListenerMembros);
    }

    public void recuperarContatos() {

        valueEventListenerMembros = usuarioRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot dados: dataSnapshot.getChildren()){

                    Usuario usuario = dados.getValue( Usuario.class );

                    String emailUsuarioAtual = usuarioAtual.getEmail();
                    if( !emailUsuarioAtual.equals( usuario.getEmail() ) )
                    {
                        listaMembros.add(usuario);
                    }
                }

                contatosAdapter.notifyDataSetChanged();

                atualizarMembrosToolbar();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void atualizarMembrosToolbar(){

        int totalSelecionados = listaMembrosSelecionados.size();
        int total = listaMembros.size() + totalSelecionados;

        if( totalSelecionados == 0 ){

            toolbar.setSubtitle("Adicionar participantes");

            //Linhas separadora
            separador.setVisibility(View.GONE);

        }else{

            toolbar.setSubtitle(totalSelecionados + " de " + total + " selecionados");

            //Linhas separadora
            separador.setVisibility(View.VISIBLE);

        }
    }
}