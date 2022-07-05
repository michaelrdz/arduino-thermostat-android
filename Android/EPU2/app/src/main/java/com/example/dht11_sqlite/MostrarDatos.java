package com.example.dht11_sqlite;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MostrarDatos extends AppCompatActivity {
    //Creamos los elementos a usar para comunicacion entre clase .java y .xml
    TextView tv_lecturas;
    Button btn_from_mostrar_datos_datos_to_main;
    String datos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mostrar_datos);
        //Inicializamos elementos de la vista en local
        tv_lecturas=findViewById(R.id.tv_lecturas);
        btn_from_mostrar_datos_datos_to_main=findViewById(R.id.btn_from_mostrar_datos_datos_to_main);
        //Revisamos los datos extra mandados desde el MainActivity
        Intent intent=getIntent();
        //Guardamos la cadena recibida en una variable local
        datos=intent.getStringExtra(MainActivity.datos);
        //Mostramos valores en la vista
        tv_lecturas.setText(datos);
        //Creamos listener para botonregresar
        btn_from_mostrar_datos_datos_to_main.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bak2Main();
            }
        });

    }
    //Metodo para regresar a MainActivity
    private void Bak2Main() {
        Intent i = new Intent(this, MainActivity.class);
        i.putExtra(DispositivosVinculados.EXTRA_DEVICE_ADDRESS, DispositivosVinculados.address);
        startActivity(i);
    }
}