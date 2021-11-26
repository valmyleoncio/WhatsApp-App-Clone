package com.example.whatsapp.model;

import com.example.whatsapp.activity.ConfiguracoesActivity;
import com.example.whatsapp.config.ConfiguracaoFirebase;
import com.example.whatsapp.helper.UsuarioFirebase;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.FirebaseDatabase;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Usuario implements Serializable
{
    private String idUsuario;
    private String nome;
    private String email;
    private String senha;
    private String foto;

    /***** Construtores *****/


    public Usuario(String nome, String email, String senha) { //para o cadastro
        this.nome = nome;
        this.email = email;
        this.senha = senha;
    }

    public Usuario(String email, String senha) { // para o login
        this.email = email;
        this.senha = senha;
    }

    public Usuario() {
    }

    /***** Métodos *****/


    public void salvar() {
        DatabaseReference firebase = ConfiguracaoFirebase.getFirebaseDatabase();
        firebase.child("Usuarios")
                .child(idUsuario)
                .setValue(this); //salvando o objeto usuario no idUsuario codificado
    }

    public void atualizar(){

        String identificadorUsuario = UsuarioFirebase.getUsuarioIdentificador();
        DatabaseReference database = ConfiguracaoFirebase.getFirebaseDatabase();

        DatabaseReference usuarioRef = database.child("Usuarios").child(identificadorUsuario);

        Map<String, Object> valoresUsuario = converterParaMap();

        usuarioRef.updateChildren(valoresUsuario);
    }

    @Exclude
    public Map<String, Object> converterParaMap(){

        HashMap<String, Object> usuarioMap = new HashMap<>();
        usuarioMap.put("email", getEmail());
        usuarioMap.put("nome", getNome());
        usuarioMap.put("foto", getFoto());

        return usuarioMap;
    }






    /***** Getter and Setter*****/


    @Exclude
    public String getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(String idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Exclude
    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public String getFoto() {
        return foto;
    }

    public void setFoto(String foto) {
        this.foto = foto;
    }
}
