package com.dimar.map2saveme.fragments;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.TypedValue;
import android.view.*;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ScrollView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import android.widget.TextView;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import com.dimar.map2saveme.FindList;
import com.dimar.map2saveme.R;
import com.dimar.map2saveme.firebaseAuth.FirebaseCallback;
import com.dimar.map2saveme.maps.MapsActivity;
import com.dimar.map2saveme.models.Photo;
import com.dimar.map2saveme.models.User;
import com.dimar.map2saveme.repository.Repository;
import com.dimar.map2saveme.viewModel.FindListViewModel;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Optional;

/**
 * A simple {@link Fragment} subclass.
 */

//test deletePhotoAnimal
public class DetailsFragment extends Fragment {

    LatLng latLng;
    Button startMap;
    Button adoptInfo;
    TextView infoTxt;
    Toolbar mytoolbar;
    Button delete;
    Repository repository;
    View rootView;

    public DetailsFragment() {
        // Required empty public constructor
    }

    public static DetailsFragment newInstance(int index) {
        DetailsFragment f = new DetailsFragment();

        // Supply index input as an argument.
        Bundle args = new Bundle();
        args.putInt("index", index);
        f.setArguments(args);

        return f;
    }

    public int getShownIndex() {
        return getArguments().getInt("index", 0);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (container == null) {
            // We have different layouts, and in one of them this
            // fragment's containing frame doesn't exist. The fragment
            // may still be created from its saved state, but there is
            // no reason to try to create its view hierarchy because it
            // isn't displayed. Note this isn't needed -- we could just
            // run the code below, where we would create and return the
            // view hierarchy; it would just never be used.
            return null;
        }
        repository=new Repository();

        rootView=inflater.inflate(R.layout.activity_details, container, false);
         startMap = rootView.findViewById(R.id.buttonFindMap);
         adoptInfo=rootView.findViewById(R.id.buttonAdopt);
         infoTxt=rootView.findViewById(R.id.textViewDetails);

        mytoolbar= rootView.findViewById(R.id.toolbar2);
        ((AppCompatActivity)getActivity()).setSupportActionBar(mytoolbar);


        return rootView;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        FindListViewModel findListViewModel = new ViewModelProvider(requireActivity()).get(FindListViewModel.class);
        Photo photo=findListViewModel.getSelected().getValue();

        LiveData<Photo> photoModel=findListViewModel.getSelected();
        photoModel.observe(getViewLifecycleOwner(), item -> findUserCallback(item,view));


        if(photo==null && getArguments().getString("object")!=null){
            findUserCallback(stringToobject(),view);
        }
    }

    private void findUserCallback(Photo photo,View view){
        repository.findUser(new FirebaseCallback() {
            @Override
            public void onCallback(@Nullable User flag) {
                if (flag != null){
                    //aku e najden
                    updateView(photo,view, Optional.of(flag));
                }else {
                    //error aku ne e najden
                    updateView(photo,view,Optional.empty());

                }
            }
            @Override
            public void onStatCallback(String num) {}
        }, photo.getPhotographerID());
    }

    private void updateView(Photo item, View rootView, Optional<User> user) {

        LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(item.getDate()), ZoneId.systemDefault());

        String base64Image=item.getImageBase64();
        byte[] data = Base64.decode(base64Image.getBytes(), Base64.DEFAULT);

        ((ImageView)rootView.findViewById(R.id.imageViewDetails))
                .setImageBitmap(BitmapFactory.decodeByteArray(data,0,data.length));

        latLng=new LatLng(item.getLtd(),item.getLng());
        mapClickListener();

        String text="Animal name(ID):  "+item.getAndimalID() + "\n" +
                "Date: "+localDateTime.toString() + "\n" +
                "Photographer: ";

        //"Photographer:  "+user.get().getName() + "\n"
        if(user.isPresent()){

            String textToShow=text.concat(user.get().getName());
            infoTxt.setText(textToShow);

            adoptInfoClick(textToShow,item,user.get());
            deletePhotoClick(item.getImageID(),item.getAndimalID().concat("_").concat(item.getImageID()),user.get());
        }else{

            String userText="Not user found \n as author of the photo";
            String textNoUser=text.concat(userText);
            infoTxt.setText(textNoUser);
        }
    }

    private void deletePhotoClick(String imageID, String animalID, User user) {
        String firebaseUserID= FirebaseAuth.getInstance().getCurrentUser().getUid();
        if (user.getUserId().equals(firebaseUserID) && user.isAdoptHelper()){
            delete=rootView.findViewById(R.id.deleteBT);
            delete.setVisibility(View.VISIBLE);
            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    repository.removePhotoAndDog(imageID,animalID);
                }
            });
        }
    }

    private void adoptInfoClick(String text, Photo item,User user) {
        adoptInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (user.isAdoptHelper()) {
                    infoTxt.setText(new StringBuilder(text)
                            .append(System.lineSeparator() + "E-mail:  " + user.getEmail())
                            .append(System.lineSeparator() + "Phone:  " + user.getPhone())
                            .toString());
                } else {
                    infoTxt.setText(new StringBuilder(text)
                            .append(System.lineSeparator() + "No adopt helper for this photo")
                            .toString());
                }
            }
        });
    }

    private void mapClickListener() {
        startMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(getActivity(), MapsActivity.class);
                intent.putExtra("lat",latLng.latitude);
                intent.putExtra("lng",latLng.longitude);
                startActivity(intent);
            }
        });

    }

    private Photo stringToobject(){

        Photo photo1=new Photo();
        String object=getArguments().getString("object");
        String[] list=object.split(",");
//            imageID+","+imageBase64+","+photographerID+","+andimalID+","+lng+","+ltd+","+date;
        photo1.setImageID(list[0]);
        photo1.setImageBase64(list[1]);
        photo1.setPhotographerID(list[2]);
        photo1.setAndimalID(list[3]);
        photo1.setLng(Double.valueOf(list[4]));
        photo1.setLtd(Double.valueOf(list[5]));
        photo1.setDate(Long.valueOf(list[6]));
        return photo1;
    }

}
