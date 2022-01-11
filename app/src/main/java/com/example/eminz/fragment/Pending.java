package com.example.eminz.fragment;

import static com.facebook.FacebookSdk.getApplicationContext;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eminz.database.AppDatabase;
import com.example.eminz.database.AppExecutors;
import com.example.eminz.database.DatabaseClient;
import com.example.eminz.database.SmsAdapter;
import com.example.eminz.database.daos.ScheduleDao;
import com.example.eminz.database.entities.Schedule;
import com.example.whatsappdemo.R;

import java.util.List;

public class Pending extends Fragment {

    AppDatabase appDatabase;
    RecyclerView recyclerView;
    SmsAdapter smsAdapter;
    MutableLiveData<List<Schedule>> pendingMutableLiveData = new MutableLiveData<>();
    int position;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup parent, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pending, parent, false);
        setHasOptionsMenu(true);


        recyclerView = view.findViewById(R.id.recycler);
        pendingMutableLiveData.observe(getViewLifecycleOwner(), pendings -> {
            smsAdapter = new SmsAdapter(getContext(), pendings);
            recyclerView.setAdapter(smsAdapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        });

        return view;


    }

    public void loaddata() {
        AppDatabase appDatabase = DatabaseClient.getInstance(getApplicationContext()).getAppDatabase();
        ScheduleDao scheduleDao = appDatabase.scheduleDaoDao();
        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                List<Schedule> pending = scheduleDao.findByStatus("Pending");
                pendingMutableLiveData.postValue(pending);


            }
        });
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        loaddata();


    }

    @Override
    public void onResume() {
        super.onResume();
        loaddata();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {

        MenuInflater inflater1 = getActivity().getMenuInflater();
        inflater1.inflate(R.menu.menuforpending, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.clearpending) {
            pendingMutableLiveData.observe(this, pendingdeletes -> {
                AsyncTask.execute(() -> {
                    appDatabase.scheduleDaoDao().delete(pendingdeletes.get(position));
                });

            });

        }
        return super.onOptionsItemSelected(item);
    }

}