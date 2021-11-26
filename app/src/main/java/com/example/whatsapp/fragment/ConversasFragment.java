package com.example.whatsapp.fragment;


import android.content.Intent;
import android.os.Bundle;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.whatsapp.R;
import com.example.whatsapp.activity.ChatActivity;
import com.example.whatsapp.adapter.ContatosAdapter;
import com.example.whatsapp.adapter.ConversasAdapter;
import com.example.whatsapp.config.ConfiguracaoFirebase;
import com.example.whatsapp.helper.RecyclerItemClickListener;
import com.example.whatsapp.helper.UsuarioFirebase;
import com.example.whatsapp.model.Conversa;
import com.example.whatsapp.model.Usuario;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ConversasFragment extends Fragment
{

    private RecyclerView recyclerViewListaConversas;
    private ConversasAdapter adapter;
    private List<Conversa> listaConversas = new ArrayList<>();
    private DatabaseReference database;
    private DatabaseReference conversaRef;
    private ChildEventListener childEventListenerConversas;


    public ConversasFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_conversas, container, false);

        //Configurações iniciais
        recyclerViewListaConversas = view.findViewById(R.id.recyclerViewListaConversas);


        //configurar adapter
        adapter = new ConversasAdapter( listaConversas, getActivity() );

        //configurar recyclerView
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager( getActivity() );
        recyclerViewListaConversas.setLayoutManager( layoutManager );
        recyclerViewListaConversas.setHasFixedSize( true );
        recyclerViewListaConversas.setAdapter( adapter );

        //Configurar evento de clique
        recyclerViewListaConversas.addOnItemTouchListener(
                new RecyclerItemClickListener(
                        getActivity(),
                        recyclerViewListaConversas,
                        new RecyclerItemClickListener.OnItemClickListener() {
                            @Override
                            public void onItemClick(View view, int position) {


                                List<Conversa> listaConversasAtualizadas = adapter.getConversas();
                                Conversa conversaSelecionada = listaConversasAtualizadas.get( position );

                                if( conversaSelecionada.getIsGroup().equals("true") ){

                                    Intent i = new Intent(getActivity(), ChatActivity.class);
                                    i.putExtra("chatGrupo", conversaSelecionada.getGrupo());
                                    startActivity(i);

                                }else{

                                    Intent i = new Intent(getActivity(), ChatActivity.class);
                                    i.putExtra("chatContato", conversaSelecionada.getUsuarioExibicao() );
                                    startActivity(i);

                                }
                            }

                            @Override
                            public void onLongItemClick(View view, int position) {

                            }

                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                            }
                        }
                )
        );

        //Configura conversas ref
        String identificadorUsuario = UsuarioFirebase.getUsuarioIdentificador();
        database = ConfiguracaoFirebase.getFirebaseDatabase();
        conversaRef =  database.child("Conversas").child( identificadorUsuario );

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        recuperarConversas();
    }

    @Override
    public void onStop() {
        super.onStop();
        conversaRef.removeEventListener( childEventListenerConversas );
    }

    public void pesquisarConversas( String texto ){

        List<Conversa> listaConversasBuscas = new ArrayList<>();

        for (Conversa conversa : listaConversas){

            if( conversa.getUsuarioExibicao() != null ){

                String nome = conversa.getUsuarioExibicao().getNome().toLowerCase();
                String ultimaMsg = conversa.getUltimaMensagem().toLowerCase();

                if ( nome.contains( texto ) || ultimaMsg.contains( texto )){

                    listaConversasBuscas.add( conversa );

                }

            }else{

                String nome = conversa.getGrupo().getNome().toLowerCase();
                String ultimaMsg = conversa.getUltimaMensagem().toLowerCase();

                if ( nome.contains( texto ) || ultimaMsg.contains( texto )){

                    listaConversasBuscas.add( conversa );

                }

            }
        }

        adapter = new ConversasAdapter( listaConversasBuscas, getActivity());
        recyclerViewListaConversas.setAdapter( adapter );
        adapter.notifyDataSetChanged();


    }

    public void recarregarConversas(){

        //troca a lista do adapter, para voltar ao que era antes a lista de conversas, voltar ao normal no caso

        adapter = new ConversasAdapter( listaConversas, getActivity());
        recyclerViewListaConversas.setAdapter( adapter );
        adapter.notifyDataSetChanged();
    }

    public void recuperarConversas() {

        listaConversas.clear();

        childEventListenerConversas = conversaRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                //Recuperar Conversas
                Conversa conversa = dataSnapshot.getValue( Conversa.class );
                listaConversas.add( conversa );
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