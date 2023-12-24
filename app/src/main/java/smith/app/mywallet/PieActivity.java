package smith.app.mywallet;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.icu.text.DecimalFormat;
import android.icu.text.SimpleDateFormat;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.MainThread;
import androidx.appcompat.app.AppCompatActivity;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import smith.app.mywallet.databinding.ActivityPieBinding;
import smith.lib.alerts.dialog.AlertSDialog;
import smith.lib.alerts.dialog.SDialog;
import smith.lib.alerts.toast.SToast;

public class PieActivity extends AppCompatActivity {
    
    ActivityPieBinding b;
    SharedPreferences sp;
    List<Map<String, Object>> data = new ArrayList<>();
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityPieBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());
        
        sp = getSharedPreferences("sp", 0);
        
        data = new Gson().fromJson(sp.getString("items", "[]"),
                new TypeToken<List<Map<String, Object>>>(){}.getType());
        
        updatePie();
    }
    
    @Override
    @MainThread
    public void onBackPressed() {
        super.onBackPressed();
        Animatoo.animateZoom(this);
    }

    @SuppressLint("SimpleDateFormat")
    public void update(View view) {
        AlertSDialog dialog = new AlertSDialog(this);
        dialog.setAccentColor(getColor(R.color.accent));
        dialog.setNegativeButton("Cancel", () -> {});
        dialog.setPositiveButton("Save", () -> {
            sp.edit().putFloat("lastBalance", (float) getTotalBalance()).apply();
            sp.edit().putString("lastDate",new SimpleDateFormat("dd/MM/yyyy, hh:mm aa").format(Calendar.getInstance(Locale.ENGLISH))).apply();
            
            data = data.stream()
                    .filter(item -> !Objects.requireNonNull(item.get("type")).toString().equals("out"))
                    .collect(Collectors.toList());
            sp.getString("items", new Gson().toJson(data));
            
            updatePie();
            
            new SToast.Mode(this,SToast.SHORT)
                .setMode(SToast.MODE_DONE)
                .setText("Saved As Last Balance, All CashOut Actions Deleted")
                .setTitle("Done Saving Last Balance!")
                .show();
        });
        dialog.setTitle("Save Last Balance");
        dialog.setText("The current balance will be saved as the last checked balance.\n\nAll 'CashOut Actions' will be deleted!");
        dialog.setTheme(SDialog.THEME_BY_SYSTEM);
        dialog.show();
    }
    
    @SuppressLint("SetTextI18n")
    private void updatePie() {
        double currentBalance = getTotalBalance();
        double lastBalance = (double) sp.getFloat("lastBalance", 0f);
        
        double profit = currentBalance - lastBalance;
        double percent = profit / lastBalance * 100;
        
        b.lastBalance.setText(format(lastBalance));
        
        if (currentBalance > lastBalance) {
            updateProfit(R.drawable.in, R.color.green, R.drawable.increase);
        } else if (currentBalance == lastBalance) {
            updateProfit(R.drawable.debt, R.color.yellow, R.drawable.stay);
        } else if (currentBalance < lastBalance) {
            updateProfit(R.drawable.out, R.color.red, R.drawable.decrease);
        }
        
        b.profit.setText(format(profit) + " (" + (currentBalance >= lastBalance ? "+" : "")
            + format("#.##", percent) + "%)");
        
        b.lastUpdate.setText("Last Action Was At (" + sp.getString("lastDate", "") + ")");
        
        b.in.setText(format(getBalance("in")));
        
        b.out.setText(format(getBalance("out")));
        
        b.debt.setText(format(getBalance("debt")));
    }
    
    @SuppressLint("UseCompatLoadingForDrawables")
    private void updateProfit(int background, int color, int icon) {
        b.profitContainer.setBackground(getDrawable(background));
        b.profit.setTextColor(getColor(color));
        b.profitIcon.setImageResource(icon);
        b.profitIcon.setColorFilter(getColor(color));
    }

    private String format(double num) {
        return sp.getString("currency", "IQD") + " " + new DecimalFormat("###,###,###.##").format(num);
    }
    
    private String format(String format, double num) {
        return new DecimalFormat(format).format(num);
    }
    
    private double getTotalBalance() {
        double total = 0d;
        for (Map<String, Object> item: data) {
            if (Objects.requireNonNull(item.get("type")).toString().equals("out"))
                total -= Double.parseDouble(Objects.requireNonNull(item.get("amount")).toString());
            else total += Double.parseDouble(Objects.requireNonNull(item.get("amount")).toString());
        }
        return total;
    }
    
    private double getBalance(String key) {
        double total = 0d;
        for (Map<String, Object> item: data) {
            if (Objects.requireNonNull(item.get("type")).toString().equals(key))
                total += Double.parseDouble(Objects.requireNonNull(item.get("amount")).toString());
        }
        return total;
    }
}
