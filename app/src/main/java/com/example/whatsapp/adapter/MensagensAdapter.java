package com.example.whatsapp.adapter;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.whatsapp.R;
import com.example.whatsapp.helper.UsuarioFirebase;
import com.example.whatsapp.model.Mensagem;
import com.example.whatsapp.model.Usuario;

import java.util.ArrayList;
import java.util.List;

public class MensagensAdapter extends RecyclerView.Adapter<MensagensAdapter.MyViewHolder> {

    private List<Mensagem> mensagens = new ArrayList<>();
    private Context context;
    private static final int TIPO_REMETENTE = 0;
    private static final int TIPO_DESTINATARIO = 1;



    public MensagensAdapter(List<Mensagem> listaMensagens, Context context) {

        this.mensagens = listaMensagens;
        this.context = context;
    }

    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View item = null;

        if (viewType == TIPO_REMETENTE){

            item = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_mensagem_remetente, parent, false);

        }else if (viewType == TIPO_DESTINATARIO){

            item = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_mensagem_destinatario, parent, false);

        }
        return new MyViewHolder(item);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {

        Mensagem mensagem = mensagens.get(position);

        String msg = mensagem.getMensagem();
        String imagem = mensagem.getImagem();

        if ( imagem != null ){

            Uri url = Uri.parse( imagem );
            Glide.with(context).load(url).into( holder.imagem );

            String nome = mensagem.getNome();
            if( !nome.isEmpty() ){
                holder.nome.setText( nome );
            }else{
                holder.nome.setVisibility(View.GONE);
            }

            //Esconder o texto
            holder.mensagem.setVisibility(View.GONE);

        }else {
            holder.mensagem.setText( msg );

            String nome = mensagem.getNome();
            if( !nome.isEmpty() ){
                holder.nome.setText( nome );
            }else{
                holder.nome.setVisibility(View.GONE);
            }

            //Esconder a imagem
            holder.imagem.setVisibility(View.GONE);

        }
    }

    @Override
    public int getItemCount()
    {
        return mensagens.size();
    }

    @Override
    public int getItemViewType(int position) {

        Mensagem mensagem = mensagens.get(position);
        String idUsuario = UsuarioFirebase.getUsuarioIdentificador();

        if(idUsuario.equals( mensagem.getIdUsuario() )){
            return TIPO_REMETENTE;
        }
        return TIPO_DESTINATARIO;
    }




    public class MyViewHolder extends RecyclerView.ViewHolder
    {
        TextView mensagem;
        TextView nome;
        ImageView imagem;
        public MyViewHolder(@NonNull View itemView)
        {
            super(itemView);

            mensagem = itemView.findViewById(R.id.textMensagemTexto);
            nome = itemView.findViewById(R.id.textNomeExibicao);
            imagem = itemView.findViewById(R.id.imageMensagemFoto);

        }
    }
}
