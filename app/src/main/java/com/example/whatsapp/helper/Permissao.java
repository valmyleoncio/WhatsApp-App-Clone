package com.example.whatsapp.helper;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public class Permissao {

    public static boolean validarPermissoes(String[] permissoes, Activity activity, int requestCode){

        if ( Build.VERSION.SDK_INT >= 23 ){

            List<String> listaPermissoes = new ArrayList<>();

            //percorrer as permissões passadas, verificando uma a uma se já tem permissao liberada
            for (String permissao : permissoes){
               Boolean temPermissao = ContextCompat.checkSelfPermission(activity, permissao) == PackageManager.PERMISSION_GRANTED;

               if (!temPermissao)
               {
                   listaPermissoes.add(permissao);
               }
            }

            if (listaPermissoes.isEmpty())
                return true;

            else{

                String[] novasPermissoes = new String[listaPermissoes.size()];
                listaPermissoes.toArray(novasPermissoes);

                ActivityCompat.requestPermissions(activity, novasPermissoes, requestCode);
            }

        }

        return true;
    }
}
