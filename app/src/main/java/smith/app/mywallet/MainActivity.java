package smith.app.mywallet;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.icu.text.DecimalFormat;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import smith.app.mywallet.databinding.ActivityMainBinding;
import smith.lib.alerts.dialog.AlertSDialog;
import smith.lib.alerts.dialog.InputSDialog;
import smith.lib.alerts.dialog.SDialog;
import smith.lib.alerts.toast.SToast;
import smith.lib.tools.animate.SAnimType;
import smith.lib.tools.animate.SAnimation;

public class MainActivity extends AppCompatActivity {

	private ActivityMainBinding b;
    private SharedPreferences sp;
    private List<Map<String, Object>> data = new ArrayList<>();
    private boolean hide = true;
    private String balance;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // LogSender.startLogging(this);
        super.onCreate(savedInstanceState);
        b = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());
        
        sp = getSharedPreferences("sp", 0);
        
        SAnimation.setClickAnimation(b.hide, SAnimType.SCALE_DOWN);
        SAnimation.setClickAnimation(b.pie, SAnimType.SCALE_DOWN);
        SAnimation.setClickAnimation(b.currency, SAnimType.SCALE_DOWN);
        SAnimation.setClickAnimation(b.in, SAnimType.SCALE_DOWN);
        SAnimation.setClickAnimation(b.debt, SAnimType.SCALE_DOWN);
        SAnimation.setClickAnimation(b.out, SAnimType.SCALE_DOWN);
        
        b.rv.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        b.rv.setVerticalScrollBarEnabled(false);
        
        b.currency.setText(sp.getString("currency", "IQD"));
        
        getItems();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        getItems();
    }
    
    public void hide(View view) {
        if (hide) {
            StringBuilder buffer = new StringBuilder();
            for (char c: balance.toCharArray()) buffer.append("*");
            b.balance.setText(buffer.toString());
            
            b.hide.setImageResource(R.drawable.eye_show);
        } else {
            getItems();
            
            b.hide.setImageResource(R.drawable.eye_hide);
        }
        hide = !hide;
    }
    
    public void add(View view) {
        Intent intent = new Intent(this, AddActivity.class);
        
        if (view.getId() == R.id.in) intent.putExtra("type","in");
        if (view.getId() == R.id.out) intent.putExtra("type","out");
        if (view.getId() == R.id.debt) intent.putExtra("type","debt");
        
        intent.putExtra("position", 0);
        intent.putExtra("edit", false);
        
        startActivity(intent);
        Animatoo.animateZoom(this);
    }
    
    public void pie(View view) {
        startActivity(new Intent(this, PieActivity.class));
        Animatoo.animateZoom(this);
    }
    
    public void currency(View view) {
        InputSDialog dialog = new InputSDialog(this);
        dialog.setAccentColor(getColor(R.color.accent));
        dialog.setInputFieldHint("Input Currency");
        dialog.setTitle("Wallet Currency");
        dialog.setText("Set your local currency as a default for your wallet.");
        dialog.setTheme(SDialog.THEME_BY_SYSTEM);
        dialog.setNegativeButtonText("Cancel");
        dialog.setPositiveButtonAction("Save", text -> {
            sp.edit().putString("currency", text).apply();
            new SToast.Mode(this, SToast.SHORT)
                    .setMode(SToast.MODE_DONE)
                    .setTitle("Currency Changed!")
                    .setText("Currency set to " + text)
                    .show();
            b.currency.setText(sp.getString("currency", ""));
        });
        dialog.show();
    }
    
    private void getItems() {
        if (!sp.getString("items", "").equals("")) {
            data = new Gson().fromJson(sp.getString("items", ""),
                new TypeToken<List<Map<String, Object>>>(){}.getType());
            balance = format(getTotalBalance());
            updateRV();
        }
    }
    
    private void updateRV() {
        b.rv.setAdapter(new ActionsAdapter(this));
        b.balance.setText(balance);
    }
    
    private double getTotalBalance() {
        double total = 0d;
        for (Map<String, Object> item: data) {
            if (Objects.equals(item.get("type"), "out"))
                total -= Double.parseDouble(Objects.requireNonNull(item.get("amount")).toString());
            else total += Double.parseDouble(Objects.requireNonNull(item.get("amount")).toString());
        }
        return total;
    }
    
    private String format(double num) {
        return new DecimalFormat("###,###,###.##").format(num);
    }
    
    private class ActionsAdapter extends RecyclerView.Adapter<ActionsAdapter.ViewHolder> {
        
        private final Activity a;
        
        public ActionsAdapter(Activity a) {
            this.a = a;
        }
        
        @NonNull
        @Override
    	public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
	    	@SuppressLint("InflateParams") View v = getLayoutInflater().inflate(R.layout.item_rv, null);
	    	return new ViewHolder(v);
    	}

        @Override
        public void onBindViewHolder(ViewHolder holder, final int position) {
            holder.itemView.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT));
            holder.item.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            
            String type = Objects.requireNonNull(data.get(position).get("type")).toString();
            switch (type) {
                case "add" -> updateType(holder.type, R.drawable.increase, R.drawable.in, R.color.green);
                case "out" -> updateType(holder.type, R.drawable.decrease, R.drawable.out, R.color.red);
                case "debt" -> updateType(holder.type, R.drawable.stay, R.drawable.debt, R.color.yellow);
            }
            
            double amount = Double.parseDouble(Objects.requireNonNull(data.get(position).get("amount")).toString());
            holder.amount.setText(format(amount));
            
            String action = Objects.requireNonNull(data.get(position).get("action")).toString();
            holder.action.setText(action);
            
            String date = Objects.requireNonNull(data.get(position).get("date")).toString();
            holder.date.setText(date);

            holder.item.setOnClickListener(v -> {
                Intent intent = new Intent(a, AddActivity.class);
                
                intent.putExtra("type", type);
                intent.putExtra("position", position);
                intent.putExtra("edit", true);

                startActivity(intent);
                Animatoo.animateZoom(a);
            });

            holder.item.setOnLongClickListener(v -> {
                AlertSDialog dialog = new AlertSDialog(a);
                dialog.setAccentColor(getColor(R.color.accent));
                dialog.setNegativeButton("Cancel", () -> {});
                dialog.setPositiveButton("Delete", () -> {
                    data.remove(position);
                    sp.edit().putString("items", new Gson().toJson(data)).apply();
                    updateRV();
                });
                dialog.setTitle("Delete Action");
                dialog.setText("This action will be deleted permanently!");
                dialog.setTheme(SDialog.THEME_BY_SYSTEM);
                dialog.show();
               
                return false;
            });
        }
        
        @SuppressLint("UseCompatLoadingForDrawables")
        private void updateType(ImageView iv, int icon, int drawable, int color) {
            iv.setImageResource(icon);
            iv.setBackground(getDrawable(drawable));
            iv.setColorFilter(getColor(color));
        }

        @Override
        public int getItemCount() {
            return data.size();
        }
        
        public class ViewHolder extends RecyclerView.ViewHolder {
        
            LinearLayout item;
            ImageView type;
            TextView amount, action, date;
        
            public ViewHolder(View v) {
                super(v);
                item = v.findViewById(R.id.item);
                type = v.findViewById(R.id.type);
                amount = v.findViewById(R.id.amount);
                action = v.findViewById(R.id.action);
                date = v.findViewById(R.id.date);
            }
        }
    }
}
