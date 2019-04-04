package com.upv.dadm.valenparking.Fragments;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.JsonReader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.upv.dadm.valenparking.Parkings;
import com.upv.dadm.valenparking.R;
import com.upv.dadm.valenparking.Adapters.fauvoriteAdapter;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class FavouriteFragment extends Fragment {

    List<Parkings> listParkings = new ArrayList<Parkings>();
    fauvoriteAdapter adapter;
    Integer position = 0;
    RecyclerView recyclerview_parkings;
    View view;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private FirebaseFirestore db;
    private CollectionReference userDBRef;
    JSONArray favouritesJSON;
    Menu fav_menu;
    Boolean hideIcon = true;
    fauvoriteAdapter.OnFavouriteLongClickListener listener2;
    fauvoriteAdapter.OnFavouriteShortClickListener listener;
    private ProgressBar progressBar;


    public FavouriteFragment(){ }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(savedInstanceState != null) {
            position = savedInstanceState.getInt("position");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_favourite, null);

        setHasOptionsMenu(true);


        listener2 = new fauvoriteAdapter.OnFavouriteLongClickListener() {
            @Override
            public void onFavouriteLongClick() {
                fav_menu.findItem(R.id.menu_delete_all_quotations).setVisible(hideIcon);

            }
        };

        listener = new fauvoriteAdapter.OnFavouriteShortClickListener() {
            @Override
            public void onFavouriteShortClick() {
                Boolean aux = false;
                for(Parkings p : listParkings){
                    if(p.isSelected()){
                        aux = true;
                    }
                }
                if(!aux){
                    fav_menu.findItem(R.id.menu_delete_all_quotations).setVisible(!hideIcon);
                }
            }
        };

        GetUserFav(new MyCallback() {
            @Override
            public void onCallback(JSONArray value) {
                //Log.v("prueba", value.toString());
                try {
                    for (int i = 0; i < value.length(); i++) {
                        //Log.v("prueba", value.getJSONObject(i).get("name").toString());
                        //Log.v("prueba", value.getJSONObject(i).get("address").toString());

                        Parkings parking = new Parkings();
                        parking.setParkingName(value.getJSONObject(i).get("name").toString());
                        parking.setCalle(value.getJSONObject(i).get("address").toString());
                        listParkings.add(parking);
                    }
                    adapter = new fauvoriteAdapter(getContext(), R.layout.recyclerview_list, listParkings, listener2, listener);
                    recyclerview_parkings = view.findViewById(R.id.fauvorite_list);
                    recyclerview_parkings.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
                    recyclerview_parkings.setAdapter(adapter);
                    progressBar.setVisibility(view.INVISIBLE);
                }catch(Exception e){}
            }
        });
        return view;
    }
    /*public String getUserUID() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String uid = "";
        if (user != null) {
            uid = user.getUid();

            Log.v("prueba", uid);
        }
        return uid;
    }*/

    /*public void recuperarFav(final MyCallback myCallback){
        String user = getUserUID();
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("users").child(user);

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //String valor = dataSnapshot.getValue();
                String res = dataSnapshot.child("fav").child("name").getValue().toString();
                Log.v("prueba", res);
                myCallback.onCallback(res);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("error", "Error!", databaseError.toException());
            }

        });

    }*/


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }
    public void GetUserFav(final MyCallback myCallback){
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        userDBRef = db.collection("users");
        currentUser = mAuth.getCurrentUser();
        Query query = userDBRef.whereEqualTo("userID", currentUser.getUid());
        final Task<QuerySnapshot> taskQuery = query.get();

        taskQuery.addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    if (task.getResult().size() > 0) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Object[] data = document.getData().values().toArray();
                            try {
                                favouritesJSON = new JSONArray(data[1].toString());
                                myCallback.onCallback(favouritesJSON);
                            }catch (Exception e){}
                        }


                    }
                }
            }
        });
    }
    public interface MyCallback {
        void onCallback(JSONArray value);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
        menuInflater.inflate(R.menu.menu_favourite, menu);
        menu.findItem(R.id.menu_delete_all_quotations).setVisible(!hideIcon);
        progressBar = (ProgressBar) view.findViewById(R.id.progressBar_favourites);
        progressBar.setVisibility(view.VISIBLE);
        fav_menu = menu;
        super.onCreateOptionsMenu(menu,menuInflater);
    }


}
