package com.example.dht11_sqlite;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class AdminSQLifeOpenHelper extends SQLiteOpenHelper {


    public AdminSQLifeOpenHelper(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase BaseDeDatos) {
        //BaseDeDatos.execSQL("Create table articulos(codigo int primary key, descripcion text, precio real)");
        BaseDeDatos.execSQL("Create table sensores(lectura integer primary key autoincrement not null, temperatura real, humedad real, fecha_hora date_time)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
