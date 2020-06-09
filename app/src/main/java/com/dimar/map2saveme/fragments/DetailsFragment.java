package com.dimar.map2saveme.fragments;

import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.TypedValue;
import android.widget.ImageView;
import android.widget.ScrollView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import com.dimar.map2saveme.R;
import com.dimar.map2saveme.models.Photo;
import com.dimar.map2saveme.viewModel.FindListViewModel;
import com.google.android.gms.maps.model.LatLng;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class DetailsFragment extends Fragment {

    private LatLng latLng;

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

         View rootView=inflater.inflate(R.layout.activity_details, container, false);

        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        FindListViewModel findListViewModel = new ViewModelProvider(requireActivity()).get(FindListViewModel.class);
        Photo photo=findListViewModel.getSelected().getValue();

        LiveData<Photo> photoModel=findListViewModel.getSelected();
        photoModel.observe(getViewLifecycleOwner(), item -> updateView(item,view));

        if(photo==null && getArguments().getString("object")!=null){
            updateView(stringToobject(),view);
        }
    }

    private void updateView(Photo item,View rootView) {

//        rootView.findViewById(R.id.imageViewDetails).
        LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(item.getDate()), ZoneId.systemDefault());

//        String base64Image = base64string.split(",")[1];
        String base64Image=item.getImageBase64();
        byte[] data = Base64.decode(base64Image.getBytes(), Base64.DEFAULT);

        String text="Animal name(ID)"+item.getAndimalID() + "\n" +
                "Photographer ID"+item.getPhotographerID() + "\n" +
                "Date: ";

        latLng=new LatLng(item.getLtd(),item.getLng());

        String textToShow=text+" "+localDateTime.toString();

        ((ImageView)rootView.findViewById(R.id.imageViewDetails))
                .setImageBitmap(BitmapFactory.decodeByteArray(data,0,data.length));
        ((TextView)rootView.findViewById(R.id.textViewDetails)).setText(textToShow);

    }

    public Photo stringToobject(){

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