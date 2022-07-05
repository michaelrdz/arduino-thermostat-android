#include <LCD_I2C.h>
#include <DHT.h>
#define DHTPIN 9
#define DHTTYPE DHT11
//int v=150; // Valor de la velocidad del motor debe de estar entre 0 y 255.
LCD_I2C lcd(0x20);
//LCD_I2C lcd(0x24);
DHT dht(DHTPIN,DHTTYPE); // dht.reaHumidity(); dht.readTemperature(); isnana();
byte RH=0;
byte Temp=0;
char temperature[]="00.0";
char humidily[]="RH=00.0%";

void leerDHT11();

void setup() {
  Serial.begin(9600);
  dht.begin();
  
  lcd.begin();
  lcd.backlight();
  lcd.setCursor(0, 0); // 0 -> 15 columnas 0 -> 1 renglones.
  lcd.print("Temp / Hum");  // Imprimir textos.
  lcd.setCursor(0, 1);
  lcd.print("puerto serie");
}

void loop() {
  // Los pines analogicos tienen una resolución de 10 bits lo que equivale a un rango de 0 a 1023.
  // float valor=0.0;
  // v_recibido= (valor(PIN_A0) / 1023) * 5 --> Los resultados son enteros.
  // v_recibido= (valor(PIN_A0) / 1023.0) * 5.0 --> Los resultados son flotantes.
  // El lm35 varia 10mV por cada grado centigrado si tenemos 27C --> Voltaje= 27 * 10 mV= 27 mV = 270 mV= 0.27 V.
  // La temperatura que representa es temperatura= voltaje * 100.0;
  
  delay(1000); //Delay permite indicar un tiempo de espera para la siguinte linea (1000 == 1 segundo)
  leerDHT11();

//METODO PARA LEER DHT11
void leerDHT11(){
  //Serial.println("\f Leyendo temperatura y humedad.");
  float RH=dht.readHumidity();
  float Temp=dht.readTemperature();
  if (isnan(RH) || isnan(Temp)) {
    lcd.clear();
    lcd.setCursor(5,0);
    lcd.print("Error");
    return;
  }
  lcd.clear();
  /*temperature[5]=Temp/10+48;
  temperature[6]=Temp%10+48;
  temperature[9]=223;*/
  lcd.setCursor(0,0);
  lcd.print("Temp: ");
  lcd.print(Temp);
  //Serial.println(Temp);
  //humidily[3]=RH/10+48;
  //humidily[4]=RH%10+48;
  lcd.setCursor(0,1);
  lcd.print("Hum: ");
  lcd.print(RH);
  //Concatenamos hambos datos para mandarlos como una sola cadena
  Serial.println(String(Temp)+String(",")+String(RH));
  Serial.print("#");
}
