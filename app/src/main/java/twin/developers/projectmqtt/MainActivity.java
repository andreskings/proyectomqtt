package twin.developers.projectmqtt;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.w3c.dom.Text;

import java.util.Calendar;


public class MainActivity extends AppCompatActivity {
    private Mqtt mqttManager;
    private Button btnSeleccionarFechaHora;
    private Button btnGuardarAlarma;
    private TextView textViewFechaSeleccionada;
    private TextView textViewHoraSeleccionada;
    private Calendar selectedDateTime = Calendar.getInstance();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnSeleccionarFechaHora = findViewById(R.id.btnSeleccionarFechaHora);
        btnGuardarAlarma = findViewById(R.id.btnGuardarAlarma);
        textViewFechaSeleccionada = findViewById(R.id.textViewFechaSeleccionada);
        textViewHoraSeleccionada = findViewById(R.id.textViewHoraSeleccionada);

        mqttManager = new Mqtt(getApplicationContext());
        mqttManager.connectToMqttBroker();

        btnSeleccionarFechaHora.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mostrarSelectorFechaHora();
            }
        });

        btnGuardarAlarma.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                programarAlarma();
                enviarMensajeMQTT("Alarma programada para: " + textViewFechaSeleccionada.getText() + " " + textViewHoraSeleccionada.getText());
            }
        });
    }

    private void mostrarSelectorFechaHora() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                selectedDateTime.set(Calendar.YEAR, year);
                selectedDateTime.set(Calendar.MONTH, month);
                selectedDateTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                TimePickerDialog timePickerDialog = new TimePickerDialog(MainActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        selectedDateTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        selectedDateTime.set(Calendar.MINUTE, minute);
                        actualizarTextViewsFechaHora();
                    }
                }, selectedDateTime.get(Calendar.HOUR_OF_DAY), selectedDateTime.get(Calendar.MINUTE), true);

                timePickerDialog.show();
            }
        }, selectedDateTime.get(Calendar.YEAR), selectedDateTime.get(Calendar.MONTH), selectedDateTime.get(Calendar.DAY_OF_MONTH));

        datePickerDialog.show();
    }

    private void actualizarTextViewsFechaHora() {
        String fechaSeleccionada = android.text.format.DateFormat.format("dd-MM-yyyy", selectedDateTime).toString();
        String horaSeleccionada = android.text.format.DateFormat.format("HH:mm", selectedDateTime).toString();

        textViewFechaSeleccionada.setText("Fecha seleccionada: " + fechaSeleccionada);
        textViewHoraSeleccionada.setText("Hora seleccionada: " + horaSeleccionada);
    }

    private void programarAlarma() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        Intent intent = new Intent(this, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);
        Calendar calendar = selectedDateTime;

        alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);

        Toast.makeText(this, "Alarma programada", Toast.LENGTH_SHORT).show();
        Log.d("Alarma", "Alarma programada para: " + calendar.getTime().toString());
    }

    private void enviarMensajeMQTT(String message) {
        mqttManager.publishMessage(message);
    }
}