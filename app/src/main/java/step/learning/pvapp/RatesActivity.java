package step.learning.pvapp;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import step.learning.pvapp.orm.NbuRate;

public class RatesActivity extends AppCompatActivity {

    private TextView tvContent;
    private static final byte[] buffer = new byte[2048];
    private List<NbuRate> rates;
    private LinearLayout ratesContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_rates);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        tvContent = findViewById(R.id.rates_tv_date);
        ratesContainer = findViewById(R.id.rates_container);
        new Thread(this::loadRates).start();
    }

    public void loadRates() {
        String url = "https://bank.gov.ua/NBUStatService/v1/statdirectory/exchange?json";
        try (InputStream urlStream = new URL(url).openStream()) {
            ByteArrayOutputStream byteBuilder = new ByteArrayOutputStream();
            int len;
            while ((len = urlStream.read(buffer)) != -1) {
                byteBuilder.write(buffer, 0, len);
            }
            String str = new String(byteBuilder.toByteArray(), StandardCharsets.UTF_8);
            rates = mapJson(str);
            runOnUiThread(this::showRates);
        } catch (MalformedURLException ex) {
            Log.d("loadRates", "MalformedURLException");
        } catch (IOException ex) {
            Log.d("loadRates", ex.getMessage());
        }

    }

    private boolean rightTurn = true;
    private void showRates() {
        tvContent.setText(rates.get(0).getExchangeDate());
        Drawable rateBgL = AppCompatResources.getDrawable(getApplicationContext(), R.drawable.rate_bg_shape);
        Drawable rateBgR = AppCompatResources.getDrawable(getApplicationContext(), R.drawable.rate_bg_l_shape);
        for (NbuRate rate : rates) {
            TextView tv = new TextView(this);
            tv.setText(rate.getTxt());
            LinearLayout.LayoutParams layoutParams;
            if (rightTurn) {
                tv.setBackground(rateBgL);
                layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                layoutParams.setMargins(5, 10, 5, 10);
                rightTurn = false;
            }
            else {
                tv.setBackground(rateBgR);
                layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                layoutParams.gravity = Gravity.RIGHT;
                layoutParams.setMargins(5, 10, 5, 10);
                rightTurn = true;
            }
            tv.setLayoutParams(layoutParams);
            tv.setPadding(15, 10, 15, 10);
            tv.setTag(rate);
            tv.setOnClickListener(this::rateClick);
            ratesContainer.addView(tv);
        }
    }



    private void rateClick(View view) {
        NbuRate rate = (NbuRate) view.getTag();

        Toast.makeText(this, rate.getRate() + "", Toast.LENGTH_SHORT).show();
        new AlertDialog.Builder(
                RatesActivity.this,
                androidx.appcompat.R.style. Theme_AppCompat_Dialog_Alert )// Widget_AppCompat_Button
                        .setTitle(rate.getTxt())
                        .setMessage(String.format(Locale.UK, "%s (%d): %f e", rate.getCc(), rate.getR030(), rate.getRate()))
.setIcon(android.R.drawable.ic_dialog_info)
                .setPositiveButton ("Закрити", null)
.show();
    }

    private List<NbuRate> mapJson(String jsonString) {
        try {
            JSONArray array = new JSONArray(jsonString);
            List<NbuRate> result = new ArrayList<>();
            for (int i = 0; i < array.length(); ++i) {
                result.add(NbuRate.fromJson(array.getJSONObject(i)));
            }
            return result;
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

}