package com.example.canbefluent;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;

public class countryListActivity extends AppCompatActivity {
    private static final String TAG = "countryListActivity";
    RecyclerView country_list;
    countryListAdapter adapter;
    ArrayList<String> list;
    SearchView search_country;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_country_list);
        Intent intent = getIntent();
        list = (ArrayList<String>) intent.getSerializableExtra("country list");
        Log.e("country", " " + list.size());



        country_list = findViewById(R.id.country_list_recycle); // 국가 리스트를 보여주는 recycler View
        country_list.setLayoutManager(new LinearLayoutManager(this));
        adapter = new countryListAdapter(list);
        adapter.setOnItemClickListener(new countryListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View v, int position) {
                Toast.makeText(countryListActivity.this, list.get(position), Toast.LENGTH_SHORT).show();
                String country_name = list.get(position);

                // 유저가 선택한 국가를 Register_setLanguage로 넘겨준다.
                Intent intent = new Intent();
                intent.putExtra("name", country_name);
                setResult(RESULT_OK, intent);
                finish();
            }
        });
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
                adapter.filter(newText);
                Log.e("country", " " + list.size());
                return true;
            }
        });
    }

//    private ArrayList<String> filter(ArrayList<String> items, String query) {
//        query = query.toLowerCase();
//
//        final ArrayList<String> list2 = new ArrayList<>();
//        if (!query.equals("")) {
//
//            for(int i=0; i<items.size(); i++){
//                if(items.get(i).toLowerCase().contains(query)){
//                    list2.add(items.get(i));
//                }
//            }
//        }
//        return list2;
//    }
}