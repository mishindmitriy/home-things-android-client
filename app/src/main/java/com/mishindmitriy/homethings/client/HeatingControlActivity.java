package com.mishindmitriy.homethings.client;

import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.TypedValue;
import android.view.View;
import android.widget.SeekBar;

import com.arellomobile.mvp.MvpAppCompatActivity;
import com.arellomobile.mvp.presenter.InjectPresenter;
import com.db.chart.model.LineSet;
import com.mishindmitriy.homethings.MonitoringData;
import com.mishindmitriy.homethings.client.databinding.ActivityHeatingControlBinding;

import org.joda.time.DateTime;

import java.util.List;

import static com.mishindmitriy.homethings.Config.MAX_TEMPERATURE;
import static com.mishindmitriy.homethings.Config.MIN_TEMPERATURE;

public class HeatingControlActivity extends MvpAppCompatActivity implements HeatingControlView {
    private static final double SCALE = 5.0;
    @InjectPresenter
    HeatingControlPresenter presenter;
    private ActivityHeatingControlBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_heating_control);
        final double temperatureDelta = MAX_TEMPERATURE - MIN_TEMPERATURE;
        binding.dayTempSeekBar.incrementProgressBy(1);
        binding.dayTempSeekBar.setMax((int) (temperatureDelta * SCALE));
        binding.dayTempSeekBar.setProgress((int) Math.round(MIN_TEMPERATURE * SCALE));
        binding.dayTempSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    presenter.setDayTemperature(MIN_TEMPERATURE + (double) progress / SCALE);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        binding.nightTempSeekBar.incrementProgressBy(1);
        binding.nightTempSeekBar.setMax((int) (temperatureDelta * SCALE));
        binding.nightTempSeekBar.setProgress((int) Math.round(MIN_TEMPERATURE * SCALE));
        binding.nightTempSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    presenter.setNightTemperature(MIN_TEMPERATURE + (double) progress / SCALE);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        TabLayout.Tab tab1 = binding.tabs.newTab();
        tab1.setText("1 hour");
        TabLayout.Tab tab2 = binding.tabs.newTab();
        tab2.setText("8 hours");
        binding.tabs.addTab(tab1);
        binding.tabs.addTab(tab2);
        binding.tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0:
                        presenter.loadDataForTime(DateTime.now().minusHours(1).getMillis());
                        break;
                    case 1:
                        presenter.loadDataForTime(DateTime.now().minusHours(8).getMillis());
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        presenter.loadDataForTime(DateTime.now().minusHours(1).getMillis());
    }

    @Override
    public void resetData() {
        binding.chart.reset();
        binding.chart.setVisibility(View.GONE);
        binding.titleTemp.setVisibility(View.GONE);
        binding.ppmChart.reset();
        binding.ppmChart.setVisibility(View.GONE);
        binding.titleCO2.setVisibility(View.GONE);
        binding.progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void showLastSensorsData(MonitoringData data) {
        binding.nowTemp.setText(String.format("Temperature: %.1f C°", data.temperature));
        binding.nowPressure.setText(String.format("Pressure: %.1f mmHg", data.pressure));
    }

    @Override
    public void updateMonitoringData(final List<MonitoringData> data) {
        binding.progressBar.setVisibility(View.GONE);
        binding.titleTemp.setVisibility(View.VISIBLE);
        binding.chart.reset();
        final float thickness = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                1.5f,
                getResources().getDisplayMetrics()
        );
        binding.chart.addData(
                createEntryList(data, Field.maintainedTemperature)
                        .setSmooth(true)
                        .setThickness(thickness)
                        .setFill(ContextCompat.getColor(this, R.color.maintainedTemperature))
        );
        binding.chart.addData(
                createEntryList(data, Field.temperature)
                        .setColor(ContextCompat.getColor(this, R.color.temperature))
                        .setThickness(thickness)
                        .setSmooth(true)
        );
        binding.chart.addData(
                createEntryList(data, Field.humidity)
                        .setColor(ContextCompat.getColor(this, R.color.humidity))
                        .setThickness(thickness)
                        .setSmooth(true)
        );
        binding.chart.addData(
                createEntryList(data, Field.boilerIsRun)
                        .setColor(Color.GRAY)
                        .setSmooth(true)
        );
        binding.chart.show();
        binding.chart.setVisibility(View.VISIBLE);

        binding.titleCO2.setVisibility(View.VISIBLE);
        binding.ppmChart.reset();
        binding.ppmChart.addData(
                createEntryList(data, Field.ppm)
                        .setColor(Color.GRAY)
                        .setSmooth(true)
                        .setThickness(thickness)
                        .setFill(Color.GRAY)
        );
        binding.ppmChart.show();
        binding.ppmChart.setVisibility(View.VISIBLE);
    }

    private LineSet createEntryList(List<MonitoringData> monitoringData, Field field) {
        final String[] labels = new String[monitoringData.size()];
        final float[] values = new float[monitoringData.size()];
        for (int i = 0; i < monitoringData.size(); i++) {
            switch (field) {
                case humidity:
                    values[i] = (float) monitoringData.get(i).humidity;
                    break;
                case temperature:
                    values[i] = (float) monitoringData.get(i).temperature;
                    break;
                case boilerIsRun:
                    values[i] = monitoringData.get(i).boilerIsRun ? 2 : 0;
                    break;
                case pressure:
                    values[i] = (float) monitoringData.get(i).pressure;
                    break;
                case maintainedTemperature:
                    values[i] = (float) monitoringData.get(i).maintainedTemperature;
                    break;
                case ppm:
                    values[i] = (float) monitoringData.get(i).ppm;
                    break;
            }
            final DateTime dateTime = new DateTime(monitoringData.get(i).timestamp);
            if ((i + 1) % (monitoringData.size() / 4) == 0) {
                labels[i] = DateTime.now().toLocalDate().isEqual(dateTime.toLocalDate())
                        ? dateTime.toString("HH:mm")
                        : dateTime.toString("dd MMMM HH:mm");
            } else {
                labels[i] = "";
            }
        }
        return new LineSet(labels, values);
    }

    @Override
    public void updateSettingDayTemp(double dayTemperature) {
        binding.settingDayTemp.setVisibility(View.VISIBLE);
        binding.dayTempSeekBar.setVisibility(View.VISIBLE);
        binding.settingDayTemp.setText(
                String.format("Day temp: %.1f ℃", dayTemperature)
        );
        binding.dayTempSeekBar.setProgress((int) Math.round((dayTemperature - MIN_TEMPERATURE) * SCALE));
    }

    @Override
    public void updateSettingNightTemp(double nightTemperature) {
        binding.settingNightTemp.setVisibility(View.VISIBLE);
        binding.nightTempSeekBar.setVisibility(View.VISIBLE);
        binding.settingNightTemp.setText(
                String.format("Night temp: %.1f ℃", nightTemperature)
        );
        binding.nightTempSeekBar.setProgress((int) Math.round((nightTemperature - MIN_TEMPERATURE) * SCALE));
    }

    @Override
    public void setHostOnline(boolean online) {
        final String offline = "offline";
        SpannableStringBuilder ssb = new SpannableStringBuilder(
                String.format(
                        "Things host is %s",
                        online ? "online" : offline
                )
        );
        if (!online) {
            int index = ssb.toString().indexOf(offline);
            ssb.setSpan(
                    new ForegroundColorSpan(Color.RED),
                    index,
                    index + offline.length(),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            );
        }
        binding.hostOnline.setText(ssb);
    }

    enum Field {
        temperature, humidity, boilerIsRun, pressure, maintainedTemperature, ppm
    }
}
