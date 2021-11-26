package com.example.whatsapp.fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.example.whatsapp.R;
import com.example.whatsapp.activity.ChatActivity;
import com.example.whatsapp.activity.ConfiguracoesActivity;
import com.example.whatsapp.activity.GrupoActivity;
import com.example.whatsapp.adapter.ContatosAdapter;
import com.example.whatsapp.adapter.ConversasAdapter;
import com.example.whatsapp.config.ConfiguracaoFirebase;
import com.example.whatsapp.helper.RecyclerItemClickListener;
import com.example.whatsapp.helper.UsuarioFirebase;
import com.example.whatsapp.model.Conversa;
import com.example.whatsapp.model.Usuario;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.List;

public class ContatosFragment extends Fragment {

    private RecyclerView recyclerViewListaContatos;
    private ContatosAdapter adapter;
    private ArrayList<Usuario> listContatos = new ArrayList<>();
    private DatabaseReference usuarioRef;
    private ValueEventListener valueEventListenerContatos;
    private FirebaseUser usuarioAtual;

    public ContatosFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_contatos, container, false);

        //Configurações iniciais
        recyclerViewListaContatos = view.findViewById(R.id.recyclerViewListaContatos);
        usuarioRef = ConfiguracaoFirebase.getFirebaseDatabase().child("Usuarios");
        usuarioAtual = UsuarioFirebase.getUsuarioAtual();


        //configurar adapter
        adapter = new ContatosAdapter( listContatos, getActivity() );

        //configurar recyclerView
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager( getActivity() );
        recyclerViewListaContatos.setLayoutManager( layoutManager );
        recyclerViewListaContatos.setHasFixedSize( true );
        recyclerViewListaContatos.setAdapter(adapter);

        //configurar evento de clique no recyclerView
        recyclerViewListaContatos.addOnItemTouchListener(new RecyclerItemClickListener(getActivity(), recyclerViewListaContatos, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {

                List<Usuario> ListaUsuariosAtualizada = adapter.getContatos();

                Usuario usuarioSelecionado = ListaUsuariosAtualizada.get(position);
                boolean cabecalho = usuarioSelecionado.getEmail().isEmpty();

                if( cabecalho ){

                    Intent i = new Intent(getActivity(), GrupoActivity.class);
                    startActivity( i );

                }else{

                    Intent i = new Intent(getActivity(), ChatActivity.class);
                    i.putExtra("chatContato", usuarioSelecionado);

                    startActivity(i);

                }
            }

            @Override
            public void onLongItemClick(View view, int position) {

            }

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
        }));

        /*Define usuário com e-mail vazio
        em caso de e-mail vazio o usuário será utilizado como
        cabecalho, exibindo novo grupo
         */
        adicionarMenuNovoGrupo();

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        recuperarContatos();
    }

    @Override
    public void onStop() {
        super.onStop();
        usuarioRef.removeEventListener( valueEventListenerContatos );
    }

    public void recuperarContatos() {

        valueEventListenerContatos = usuarioRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                limparListaContatos();

                for (DataSnapshot dados: dataSnapshot.getChildren()){

                    Usuario usuario = dados.getValue( Usuario.class );

                    String emailUsuarioAtual = usuarioAtual.getEmail();
                    if( !emailUsuarioAtual.equals( usuario.getEmail() ) )
                    {
                        listContatos.add(usuario);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    public void limparListaContatos(){

        listContatos.clear();
        adicionarMenuNovoGrupo();
    }

    public void adicionarMenuNovoGrupo(){

        /*Define usuário com e-mail vazio
        em caso de e-mail vazio o usuário será utilizado como
        cabecalho, exibindo novo grupo
         */

        Usuario itemGrupo = new Usuario();
        itemGrupo.setNome("Novo grupo");
        itemGrupo.setEmail("");

        listContatos.add( itemGrupo );
    }

    public void pesquisarContatos( String texto ){

        List<Usuario> listaContatosBuscas = new ArrayList<>();

        for (Usuario usuario: listContatos){

            String nome = usuario.getNome().toLowerCase();
            if(nome.contains( texto )){
                listaContatosBuscas.add(usuario);
            }
        }

        adapter = new ContatosAdapter( listaContatosBuscas, getActivity());
        recyclerViewListaContatos.setAdapter( adapter );
        adapter.notifyDataSetChanged();


    }

    public void recarregarContatos(){

        //troca a lista do adapter, para voltar ao que era antes a lista de conversas, voltar ao normal no caso

        adapter = new ContatosAdapter( listContatos, getActivity());
        recyclerViewListaContatos.setAdapter( adapter );
        adapter.notifyDataSetChanged();
    }
}