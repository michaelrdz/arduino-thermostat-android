package com.example.dht11_sqlite;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Set;

public class DispositivosVinculados extends AppCompatActivity {
    private static final String TAG= "DispositivosVinculados";
    ListView IdLista;
    public static String EXTRA_DEVICE_ADDRESS= "device_address";
    public  static String address;
    private BluetoothAdapter mBtAdapter;
    private ArrayAdapter mPairedDevicesArrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dispositivos_vinculados);
    }

    @Override
    protected void onResume() {
        super.onResume();
        VerificarEstadoBT();
        mPairedDevicesArrayAdapter=new ArrayAdapter(this, R.layout.dispositivos_encontrados);
        IdLista= (ListView)findViewById(R.id.IdLista);
        IdLista.setAdapter(mPairedDevicesArrayAdapter);
        IdLista.setOnItemClickListener(mDeviceClickListener);
        mBtAdapter= BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> pairedDevices= mBtAdapter.getBondedDevices();

        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device:pairedDevices){
                mPairedDevicesArrayAdapter.add(device.getName()+"\n"+device.getAddress());
            }
        }
    }

    private AdapterView.OnItemClickListener mDeviceClickListener= new AdapterView.OnItemClickListener(){
        @Override
        public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
            String info= ((TextView) v).getText().toString();
            address= info.substring(info.length() - 17);
            finish();
            Intent intend= new Intent(DispositivosVinculados.this, MainActivity.class);
            intend.putExtra(EXTRA_DEVICE_ADDRESS, address);
            startActivity(intend);
        }
    };

    private void VerificarEstadoBT(){
        mBtAdapter= BluetoothAdapter.getDefaultAdapter();
        if (mBtAdapter == null){
            Toast.makeText(getBaseContext(), "El dispositivo no soporta Bluetooth", Toast.LENGTH_SHORT).show();
        } else{
            if (mBtAdapter.isEnabled()){
                Log.d(TAG,"...Bluetooth Activado...");
            } else {
                Intent enableBtIntent= new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent,1);
            }
        }
    }
}