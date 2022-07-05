package com.example.dht11_sqlite;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    //Definimos los elementos con los que trabajaremos la comunicaciòn entre la clase .java y la vista .xml
    Button btnDesconectar, btn_consultar_datos, btn_graphTemp, btn_graphHum;
    TextView txtMensaje, tv_temperatura, tv_humedad, tv_fecha_hora;

    Handler bluetoothIn;
    //Definimos variables locales y globales
    final int handlerState=0;
    private BluetoothAdapter btAdapter=null;
    private BluetoothSocket btSocket=null;
    private StringBuilder DataStringIN= new StringBuilder();
    private ConnectedThread MyConexionBT;
    private static final UUID BTMODULEUUID= UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private String address= null;
    public static String datos;
    public static ArrayList listTemp;
    public static ArrayList listDates;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //instanciamos bluetoothIn como un elemento Handler
        bluetoothIn = new Handler() {
            public void handleMessage(android.os.Message msg) {
                if (msg.what == handlerState) {
                    //Del handler recibiremos una cadena de texto desde Serial
                    String readMessage= (String)msg.obj;
                    //Leemos la cADENA recibida
                    DataStringIN.append(readMessage);
                    int endOfLineIndex= DataStringIN.indexOf("#");
                    if (endOfLineIndex>0){
                        String dataInPrint= DataStringIN.substring(0,endOfLineIndex);
                        //Mostramos el valor de la cadena en pantalla
                        txtMensaje.setText(dataInPrint);
                        DataStringIN.delete(0,DataStringIN.length());
                    }
                    //Actualizamos los campos para mostrar el ùltimo registro de temperatura/humendad recibido
                    UltimoRegistro();
                }
            }
        };
        btAdapter= BluetoothAdapter.getDefaultAdapter();
        //Verficamos el estado de la conexion bluetooth
        VerificarEstadoBT();

        //traemos los elementos precentes en la vista pasandolos a elementos locales de su mismo tipo
        btnDesconectar=(Button)findViewById(R.id.btnDesconectar);
        txtMensaje=(TextView)findViewById(R.id.txtMensaje);
        tv_temperatura= findViewById(R.id.tv_temperatura);
        tv_humedad= findViewById(R.id.tv_humedad);
        tv_fecha_hora= findViewById(R.id.tv_fecha_hora);
        btn_consultar_datos=findViewById(R.id.btn_consultar_datos);
        btn_graphTemp=findViewById(R.id.btn_graphTemp);
        btn_graphHum=findViewById(R.id.btn_graphHump);

        //Creamos un listener para el boton desconectar
        btnDesconectar.setOnClickListener(v -> {
            //Vemos si e socket està definido y activo
            if (btSocket != null){
                try {
                    //cerramos la conexiòn por bluetooth
                    btSocket.close();
                } catch (IOException e){
                    Toast.makeText(getBaseContext(),"Error al cerrar",Toast.LENGTH_SHORT).show();
                }
            }else {

            }
            //Finalizamos la aplicacion
            finish();
        });

        //Creamos un listener para el boton buscar el cual manda al metodo buscar()
        btn_consultar_datos.setOnClickListener(v -> buscar());

        //Creamos listener para el boton hisotiral temperatura
        btn_graphTemp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Mandamos metodo historialT()
                historialT();
            }
        });

        //Creamos listener para el boton hisotiral temperatura
        btn_graphHum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Mandamos metodo historialH()
                historialH();
            }
        });
    }

    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {
        return device.createRfcommSocketToServiceRecord(BTMODULEUUID);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent intent = getIntent();
        address = intent.getStringExtra(DispositivosVinculados.EXTRA_DEVICE_ADDRESS);
        BluetoothDevice device = btAdapter.getRemoteDevice(address);
        try {
            btSocket = createBluetoothSocket(device);
        } catch (IOException e) {
            Toast.makeText(getBaseContext(), "La creacción del Socket falló", Toast.LENGTH_SHORT).show();
        }
        try {
            btSocket.connect();
        } catch (IOException e) {
            Toast.makeText(getBaseContext(), "No se pudo conectar a Bluetooth", Toast.LENGTH_SHORT).show();
        }
        MyConexionBT = new ConnectedThread(btSocket);
        MyConexionBT.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        try
        {
            btSocket.close();
        } catch (IOException e) {
            Toast.makeText(getBaseContext(), "--Falló al correr el socket--", Toast.LENGTH_SHORT).show();
        }
    }

    private void VerificarEstadoBT() {
        if(btAdapter == null) {
            Toast.makeText(getBaseContext(), "El dispositivo no soporta Bluetooth", Toast.LENGTH_SHORT).show();
        } else {
            if (btAdapter.isEnabled()) {
                Toast.makeText(getBaseContext(), "Bluetooth Habilitado", Toast.LENGTH_SHORT).show();
            } else {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 1);
            }
        }
    }

    private class ConnectedThread extends Thread {
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            InputStream tmpIn= null;
            OutputStream tmpOut= null;
            try
            {
                tmpIn= socket.getInputStream();
                tmpOut= socket.getOutputStream();
            } catch (IOException e) {
                Toast.makeText(getBaseContext(), "No se pudo obtener información", Toast.LENGTH_SHORT).show();
            }
            mmInStream= tmpIn;
            mmOutStream= tmpOut;
        }

        public void run() {
            byte[] buffer=new byte[256];
            int bytes;
            //Permite obtener las salidas del Serial de Arduino
            while (true) {
                try {
                    bytes=mmInStream.read(buffer);
                    //Lo recibido por Serial se lee como String
                    String readMessage= new String(buffer,0,bytes);
                    bluetoothIn.obtainMessage(handlerState, bytes,-1,readMessage).sendToTarget();
                } catch (IOException e) {
                    break;
                }
            }
        }

        public void write(String input)
        {
            try {
                mmOutStream.write(input.getBytes());
            }
            catch (IOException e)
            {
                Toast.makeText(getBaseContext(), "La Conexión falló", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    //Cargar datos del handler en los campos txtView
    public void UltimoRegistro() {
        String currenDateTimeSring= java.text.DateFormat.getDateTimeInstance().format(new Date());
        String str_temHum =(String)txtMensaje.getText();
        if (str_temHum.equals("Esperando nueva lectura...")){

        } else  {
            String[] splitStr = str_temHum.split(",");
            tv_temperatura.setText(splitStr[0]);
            tv_humedad.setText(splitStr[1]);
            tv_fecha_hora.setText(currenDateTimeSring);
            registrar();
        }
    }

    // Altas en SQLite
    public void registrar() {
        //Abrimos conexion
        AdminSQLifeOpenHelper admin= new AdminSQLifeOpenHelper(this, "Administracion",null,1);
        SQLiteDatabase BaseDeDatos= admin.getWritableDatabase();
        //Tomamos los valores de los campos en la vista
        String temperatura= tv_temperatura.getText().toString();
        String humendad= tv_humedad.getText().toString();
        String fechayhora= tv_fecha_hora.getText().toString();
        //Si ninguno es bacio guardamos en SQLite
        if (!temperatura.isEmpty() && !humendad.isEmpty() && !fechayhora.isEmpty()){
            ContentValues registro= new ContentValues();
            registro.put("temperatura",temperatura);
            registro.put("humedad",humendad);
            registro.put("fecha_hora",fechayhora);

            BaseDeDatos.insert("sensores",null,registro);
            //Cerramos conexion
            BaseDeDatos.close();

            Toast.makeText(this, "Registro exitoso", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Error al registrar", Toast.LENGTH_SHORT).show();
        }
    }
    //Metodo que obtiene los datos de Sqlite y manda a la Activity de la grafica temperatura
    public void historialT() {
        //creamos un arraylist para guardar los datos que nos interesan del SQLite
        ArrayList<String> registrosT = new ArrayList<>();
        //Abrimos conexion a SQLite
        AdminSQLifeOpenHelper admin= new AdminSQLifeOpenHelper(this, "Administracion",null,1);
        SQLiteDatabase bdforT= admin.getWritableDatabase();

        try {
            //Ejecutamos query Select
            Cursor fila = bdforT.rawQuery("Select * from sensores", null);
            if (fila.moveToFirst()) {
                do {
                    //Vamos guardando cada dato en el arraylist (solo valor Temperatura)
                    String datoT = fila.getString(1);
                    registrosT.add(datoT);
                } while (fila.moveToNext());
            }else {}
            fila.close();
            bdforT.close();
        }catch(Exception e){
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        //Mandamos a la activity de la grafica y le enviamos el arraylist
        Intent i = new Intent(this, historial_temperatura.class);
        i.putStringArrayListExtra("listTemp", registrosT);
        startActivity(i);
    }

    //Metodo que obtiene los datos de Sqlite y manda a la Activity de la grafica humedad
    public void historialH() {
        //creamos un arraylist para guardar los datos que nos interesan del SQLite
        ArrayList<String> registrosH = new ArrayList<>();
        //Abrimos conexion a SQLite
        AdminSQLifeOpenHelper admin= new AdminSQLifeOpenHelper(this, "Administracion",null,1);
        SQLiteDatabase bdforH= admin.getWritableDatabase();

        try {
            //Ejecutamos query Select
            Cursor fila = bdforH.rawQuery("Select * from sensores", null);
            if (fila.moveToFirst()) {
                do {
                    //Vamos guardando cada dato en el arraylist (solo valor Humedad)
                    String datoH = fila.getString(2);
                    registrosH.add(datoH);
                } while (fila.moveToNext());
            }else {}
            fila.close();
            bdforH.close();
        }catch(Exception e){
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        //Mandamos a la activity de la grafica y le enviamos el arraylist
        Intent i = new Intent(this, historial_humedad.class);
        i.putStringArrayListExtra("listHum", registrosH);
        startActivity(i);
    }
    //Metodo que consulta los registros en SQLite y muestra en otra Activity
    public void buscar() {
        AdminSQLifeOpenHelper admin= new AdminSQLifeOpenHelper(this, "Administracion",null,1);
        SQLiteDatabase BaseDeDatos= admin.getWritableDatabase();

        datos= "===============================================\n";
        datos= datos +"|  lectura      temp      hum      fecha_hora\n";
        datos= datos +"===============================================\n";

        try {
            Cursor fila = BaseDeDatos.rawQuery("Select * from sensores", null);
            if (fila.moveToFirst()) {
                do {
                    String dato1 = fila.getString(0);
                    String dato2 = fila.getString(1);
                    String dato3 = fila.getString(2);
                    String dato4 = fila.getString(3);
                    datos = datos + "|  " + dato1 + "     | " + dato2 + "     | " + dato3 + "     | " + dato4 + "     |\n";
                } while (fila.moveToNext());
            }
            fila.close();
            BaseDeDatos.close();
            Intent to_MostrarDatos = new Intent(this, MostrarDatos.class);
            to_MostrarDatos.putExtra(datos, datos);
            startActivity(to_MostrarDatos);
        }catch(Exception e){
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}