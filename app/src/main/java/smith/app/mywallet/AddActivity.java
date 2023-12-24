package smith.app.mywallet;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.icu.text.DecimalFormat;
import android.icu.text.SimpleDateFormat;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;

import androidx.annotation.MainThread;
import androidx.appcompat.app.AppCompatActivity;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import smith.app.mywallet.databinding.ActivityAddBinding;
import smith.lib.alerts.toast.SToast;
import smith.lib.tools.animate.SAnimType;
import smith.lib.tools.animate.SAnimation;

public class AddActivity extends AppCompatActivity {
    
    ActivityAddBinding b;
    SharedPreferences sp;
    
    List<Map<String, Object>> data = new ArrayList<>();
    Set<String> set = new HashSet<>();
    
    int position = 0;
    String type = "in";
    boolean isForEdit = false;
    
    @SuppressLint({"UseCompatLoadingForDrawables", "SetTextI18n"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivityAddBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());
        
        sp = getSharedPreferences("sp", 0);
        
        SAnimation.setClickAnimation(b.one, SAnimType.SCALE_DOWN);
        SAnimation.setClickAnimation(b.two, SAnimType.SCALE_DOWN);
        SAnimation.setClickAnimation(b.three, SAnimType.SCALE_DOWN);
        SAnimation.setClickAnimation(b.four, SAnimType.SCALE_DOWN);
        SAnimation.setClickAnimation(b.five, SAnimType.SCALE_DOWN);
        SAnimation.setClickAnimation(b.six, SAnimType.SCALE_DOWN);
        SAnimation.setClickAnimation(b.seven, SAnimType.SCALE_DOWN);
        SAnimation.setClickAnimation(b.eight, SAnimType.SCALE_DOWN);
        SAnimation.setClickAnimation(b.nine, SAnimType.SCALE_DOWN);
        SAnimation.setClickAnimation(b.zero, SAnimType.SCALE_DOWN);
        SAnimation.setClickAnimation(b.twzero, SAnimType.SCALE_DOWN);
        SAnimation.setClickAnimation(b.thzero, SAnimType.SCALE_DOWN);
        SAnimation.setClickAnimation(b.undo, SAnimType.SCALE_DOWN);
        SAnimation.setClickAnimation(b.save, SAnimType.SCALE_DOWN);
        
        data = new Gson().fromJson(sp.getString("items", "[]"),
                new TypeToken<List<Map<String, Object>>>(){}.getType());
        
        type = getIntent().getStringExtra("type");
        position = getIntent().getIntExtra("position", 0);
        isForEdit = getIntent().getBooleanExtra("edit", false);
        
        set = sp.getStringSet("hints", new HashSet<String>());
        b.action.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, set.toArray(new String[0])));

        switch (type) {
            case "add" -> b.save.setBackground(getDrawable(R.drawable.green));
            case "out" -> b.save.setBackground(getDrawable(R.drawable.red));
            case "debt" -> b.save.setBackground(getDrawable(R.drawable.yellow));
        }
        
        if (isForEdit) {
            b.save.setText("Save Action");
            
            double amount = Double.parseDouble(Objects.requireNonNull(data.get(position).get("amount")).toString());
            b.amount.setText(format(amount));
            
            b.action.setText(Objects.requireNonNull(data.get(position).get("action")).toString());
        } else {
            b.save.setText("Add Action");
        }
        
        b.undo.setOnLongClickListener(v-> {
            b.amount.setText("0");
            return false;
        });
    }
    
    @Override
    @MainThread
    public void onBackPressed() {
        super.onBackPressed();
        Animatoo.animateZoom(this);
    }
    
    @SuppressLint("SimpleDateFormat")
    public void save(View view) {
        Map<String, Object> item = new HashMap<>();
        item.put("type", type);
        
        String amount = !b.amount.getText().toString().isEmpty() ? clear(b.amount.getText().toString()) : "0";
        item.put("amount", amount);
        
        String action = !b.action.getText().toString().isEmpty() ? b.action.getText().toString() : "For a purpose.";
        item.put("action", action);
        
        item.put("date", new SimpleDateFormat("dd/MM/yyyy, hh:mm aa").format(new Date()));

        if (isForEdit) data.remove(position);
        data.add(0, item);
        
        sp.edit().putString("items", new Gson().toJson(data)).apply();
        
        set.add(b.action.getText().toString());
        sp.edit().putStringSet("hints", set).apply();
        
        new SToast.Mode(this, SToast.SHORT)
                .setText("Action Saved")
                .setTitle("Done!")
                .setMode(SToast.MODE_DONE)
                .show();
        
        onBackPressed();
    }
    
    public void write(View view) {
        String text = clear(b.amount.getText().toString());
        
        if (view.getId() == R.id.one) text += "1";
        if (view.getId() == R.id.two) text += "2";
        if (view.getId() == R.id.three) text += "3";
        if (view.getId() == R.id.four) text += "4";
        if (view.getId() == R.id.five) text += "5";
        if (view.getId() == R.id.six) text += "6";
        if (view.getId() == R.id.seven) text += "7";
        if (view.getId() == R.id.eight) text += "8";
        if (view.getId() == R.id.nine) text += "9";
        if (view.getId() == R.id.thzero) text += "000";
        if (view.getId() == R.id.twzero) text += "00";
        if (view.getId() == R.id.zero) text += "0";
        if (view.getId() == R.id.undo) {
            if (text.length() > 1) text = text.substring(0, text.length()-1);
            else text = "0";
        }
        
        double amount = Double.parseDouble(text);
        b.amount.setText(format(amount));
    }
    
    private String format(double num) {
        return new DecimalFormat("###,###,###,###.##").format(num);
    }
    
    private String clear(String text) {
        return text.replaceAll(",", "");
    }
}
