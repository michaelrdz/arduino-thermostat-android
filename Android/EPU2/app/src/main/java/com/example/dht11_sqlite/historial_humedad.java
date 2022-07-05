package com.example.dht11_sqlite;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LegendEntry;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.DataSet;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.util.ArrayList;

public class historial_humedad extends AppCompatActivity {
    //Creamos los elementos a usar para comunicacion entre clase .java y .xml
    ArrayList<String> registros;
    String[] dates;
    private final int[]colors=new int[]{Color.BLACK, Color.RED, Color.GREEN, Color.BLUE, Color.LTGRAY};
    LineChart lineChart;
    Button btn_lineChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historial_humedad);
        //Inicializamos elementos de la vista en local
        btn_lineChart=findViewById(R.id.btn_lineChart);
        lineChart=findViewById(R.id.lineChart);
        //Revisamos los datos extra mandados desde el MainActivity
        Intent intent=getIntent();
        //Guardamos el arraylist recibido en una variable local
        registros=(ArrayList)intent.getParcelableArrayListExtra("listHum");
        //llenamos la lista que permitira identificar cada valor por su indice
        dates = new String[registros.size()];
        for (int i = 0; i < registros.size(); i++) {
            //En vez de la fecha completa mostramos reg+numero de registro dato por los indices
            dates[i]= "Reg"+i;
        }
        //Se genera la grafica llamando a todos los metodos en cadena
        createCharts();
        //Creamos listener para botonregresar
        btn_lineChart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Regresar_Main();
            }
        });
    }

    private void Regresar_Main(){
        Intent i = new Intent(this,MainActivity.class);
        i.putExtra(DispositivosVinculados.EXTRA_DEVICE_ADDRESS, DispositivosVinculados.address);
        startActivity(i);
    }

    private Chart getSameChart(Chart chart, String descripcion, int textColor, int background, int animatey, boolean leyenda) {
        chart.getDescription().setText(descripcion);
        chart.getDescription().setTextColor(textColor);
        chart.getDescription().setTextSize(15);
        chart.setBackgroundColor(background);
        chart.animateY(animatey);
        if(leyenda) {
            legend(chart);
        }
        return chart;
    }

    private void legend(Chart chart) {
        Legend legen=chart.getLegend();
        legen.setForm(Legend.LegendForm.CIRCLE);
        legen.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        ArrayList<LegendEntry> entries = new ArrayList<>();
        for (int i=0; i<dates.length; i++) {
            LegendEntry entry=new LegendEntry();
            //entry.formColor=colors[i];
            entry.label=dates[i];
            entries.add(entry);
        }
        legen.setCustom(entries);
    }
    private ArrayList<Entry> getLineEntries() {
        ArrayList<Entry> entries = new ArrayList<>();
        float[] floatEntries = new float[registros.size()];
        for (int i=0; i<registros.size(); i++) {
            floatEntries[i]=Float.parseFloat(registros.get(i));
        }
        for (int i=0; i<floatEntries.length;i++){
            entries.add(new Entry(i,floatEntries[i]));
        }
        return entries;
    }
    private void axisX(XAxis axis) {
        axis.setGranularityEnabled(true);
        axis.setPosition(XAxis.XAxisPosition.BOTTOM);
        axis.setValueFormatter(new IndexAxisValueFormatter(dates));
    }
    private void axisLeft(YAxis axis) {
        axis.setSpaceTop(30);
        axis.setAxisMinimum(0);
        axis.setGranularity(1f);
    }
    private  void  axisRight(YAxis axis) {
        axis.setEnabled(false);
    }
    private void createCharts() {
        lineChart=(LineChart)getSameChart(lineChart, "Historial Humedad", Color.BLUE, Color.YELLOW, 3000,true);
        lineChart.setData(getLineData());
        lineChart.invalidate();
        axisX(lineChart.getXAxis());
        axisLeft(lineChart.getAxisLeft());
        axisRight(lineChart.getAxisRight());
    }
    private DataSet getDataSame(DataSet dataSet) {
        dataSet.setColors(colors);
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueTextSize(10);
        return dataSet;
    }
    private LineData getLineData() {
        LineDataSet lineDataSet=(LineDataSet) getDataSame(new LineDataSet(getLineEntries(), ""));
        lineDataSet.setLineWidth(2.5f);
        //lineDataSet.setCircleColors(colors);
        lineDataSet.setCircleRadius(5f);
        lineDataSet.setDrawFilled(true);
        lineDataSet.setMode(LineDataSet.Mode.LINEAR);
        return new LineData(lineDataSet);
    }
}