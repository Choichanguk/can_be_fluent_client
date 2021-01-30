package com.example.canbefluent.login_process;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.canbefluent.R;
import com.example.canbefluent.adapter.countryListAdapter;
import com.example.canbefluent.items.language_code_item;

import java.util.ArrayList;

public class countryListActivity extends AppCompatActivity {
    private static final String TAG = "countryListActivity";
    private static final int GET_LEVEL = 6;

    RecyclerView country_list;
    countryListAdapter adapter;
    ArrayList<language_code_item> list;
    SearchView search_country;

    String country_name;
    String lang_code;
    String level;
    String type;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_country_list);
        Intent intent = getIntent();
        list = (ArrayList<language_code_item>) intent.getSerializableExtra("country list");
        type = intent.getStringExtra("type");
        Log.e("country", " " + list.size());
        Log.e("type", " " + type);

        country_list = findViewById(R.id.country_list); // 국가 리스트를 보여주는 recycler View
        country_list.setLayoutManager(new LinearLayoutManager(this));
        adapter = new countryListAdapter(list);

        // 모어(natice)를 선택할 땐 난이도를 선택하는 다이얼로그가 뜨지 않고, 연습언어(practice)를 선택할때만 난이도를 선택할 수 있는 다이얼로그가 띄워진다.
        if(type.equals("native")){
            adapter.setOnItemClickListener(new countryListAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(View v, int position, language_code_item item) {
//                    Toast.makeText(countryListActivity.this, list.get(position), Toast.LENGTH_SHORT).show();
                    country_name = item.getLang_name() + "(" + item.getLang_ko_name() + ")";
                    lang_code = item.getLang_code();


                    // 유저가 선택한 국가를 Register_setLanguage로 넘겨준다.
                    Intent intent = new Intent();
                    intent.putExtra("name", country_name);
                    intent.putExtra("lang code", lang_code);
                    setResult(RESULT_OK, intent);
                    finish();
                }
            });
        }
        else if(type.equals("practice")){
            adapter.setOnItemClickListener(new countryListAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(View v, int position, language_code_item item) {
//                    Toast.makeText(countryListActivity.this, list.get(position), Toast.LENGTH_SHORT).show();
                    country_name = item.getLang_name() + "(" + item.getLang_ko_name() + ")";
                    lang_code = item.getLang_code();

                    // 난이도를 선택하는 lang_level_popup 팝업 액티비티로 이동한다.
                    Intent intent = new Intent(getApplicationContext(), lang_level_popup.class);
                    startActivityForResult(intent, GET_LEVEL);
                }
            });
        }

        country_list.setAdapter(adapter);

        search_country= findViewById(R.id.search_country);  // 국가 검색 창
        search_country.onActionViewExpanded(); //바로 검색 할 수 있도록
        search_country.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.getFilter().filter(newText);
//                adapter.filter(newText);
                Log.e("country", " " + list.size());
                return true;
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == GET_LEVEL){
            level = data.getStringExtra("level");
            Log.e(TAG, "level: " + level);
            Intent intent = new Intent();
            intent.putExtra("name", country_name);
            intent.putExtra("lang code", lang_code);
            intent.putExtra("level", level);
            setResult(RESULT_OK, intent);

            finish();
        }
    }
}