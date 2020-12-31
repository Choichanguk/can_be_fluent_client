package com.example.canbefluent;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.canbefluent.adapter.languageCodeAdapter;
import com.example.canbefluent.user_info.set_translate_language;
import com.example.canbefluent.utils.MyApplication;

import java.util.ArrayList;

public class MyDialogFragment extends DialogFragment {


    private MyDialogListener myListener;
    languageCodeAdapter adapter;
    String lang_code, type;

    public interface MyDialogListener {

        public void myCallback(ArrayList list);

    }


    public MyDialogFragment(String lang_code, String type) {
        this.lang_code = lang_code;
        this.type = type;
    }


    @Override

    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);


        try {

            myListener = (MyDialogListener) getTargetFragment();

        } catch (ClassCastException e) {

            throw new ClassCastException();

        }

    }


    @Override

    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_lang_list, null);
        RecyclerView recyclerView = view.findViewById(R.id.lang_code_recycle);
        adapter = new languageCodeAdapter(MyApplication.list, lang_code);
        adapter.setOnItemClickListener(new languageCodeAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View v, int position) {
                String lang_code = MyApplication.list.get(position).getLang_code();
                ArrayList list = new ArrayList();
                list.add(type);
                list.add(lang_code);
                myListener.myCallback(list);
                dismissDialog();
            }
        });

        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        builder.setView(view);
        return builder.create();

    }

    private void dismissDialog() {
        this.dismiss();
    }


}
