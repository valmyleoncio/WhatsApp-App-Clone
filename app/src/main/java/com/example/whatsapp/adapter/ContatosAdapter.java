package com.example.whatsapp.adapter;

import android.content.Context;
import android.net.Uri;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.whatsapp.R;
import com.example.whatsapp.model.Usuario;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ContatosAdapter extends RecyclerView.Adapter<ContatosAdapter.MyViewHolder> {

    private List<Usuario> contatos = new ArrayList<>();
    private Context context;

    public ContatosAdapter(List<Usuario> listaContatos, Context context) {

        this.contatos = listaContatos;
        this.context = context;
    }

    public List<Usuario> getContatos(){

        return this.contatos;

    }


    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {

        View itemLista = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_contatos, parent, false);

        return new MyViewHolder(itemLista);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position)
    {
        Usuario usuario = contatos.get(position);
        boolean cabecalho = usuario.getEmail().isEmpty();

        if (usuario.getFoto() != null){

            Uri uri = Uri.parse(usuario.getFoto());
            Glide.with( context ).load( uri ).into( holder.foto );

        }else {
            if ( cabecalho ){
                holder.foto.setImageResource(R.drawable.icone_grupo);
                holder.email.setVisibility(View.GONE);
                holder.divisor.setVisibility(View.GONE);
            }
            else {
                holder.foto.setImageResource(R.drawable.padrao);
            }
        }

        holder.nome.setText(usuario.getNome());
        holder.email.setText(usuario.getEmail());

    }

    @Override
    public int getItemCount()
    {
        return contatos.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder
    {
        private CircleImageView foto;
        private TextView nome;
        private TextView email;
        private View divisor;

        public MyViewHolder(@NonNull View itemView)
        {
            super(itemView);

            foto = itemView.findViewById(R.id.circleViewFotoContatos);
            nome = itemView.findViewById(R.id.textNomeContatos);
            email = itemView.findViewById(R.id.textEmailContatos);
            divisor = itemView.findViewById(R.id.barraDivisor);

        }
    }
}
