package com.dimar.map2saveme.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.dimar.map2saveme.DetailsActivity;
import com.dimar.map2saveme.R;
import com.dimar.map2saveme.adapters.CustomListAdapter;
import com.dimar.map2saveme.clickListener.RecyclerViewClickListener;
import com.dimar.map2saveme.models.Photo;
import com.dimar.map2saveme.viewModel.FindListViewModel;

import java.util.List;


public class FindListFragment extends Fragment implements RecyclerViewClickListener {

    boolean dualPane;
    int curCheckPosition=0;

    CustomListAdapter adapter;
    FindListViewModel findListViewModel;

    public FindListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView=inflater.inflate(R.layout.activity_find_list, container, false);

        initListView(rootView);
        initData();

        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        View detailsFrame = getActivity().findViewById(R.id.details);
        dualPane = detailsFrame != null && detailsFrame.getVisibility() == View.VISIBLE;

        if (savedInstanceState != null) {
            // Restore last state for checked position.
            curCheckPosition = savedInstanceState.getInt("curChoice", 0);
        }

        if (dualPane) {

            showDetails(curCheckPosition);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("curChoice", curCheckPosition);
    }

    void showDetails(int index) {
        curCheckPosition = index;

        if (dualPane) {

            // Check what fragment is currently shown, replace if needed.
            DetailsFragment details = (DetailsFragment)
                    getActivity().getSupportFragmentManager().findFragmentById(R.id.details);
            if (details == null || details.getShownIndex() != index) {
                // Make new fragment to show this selection.
                details = DetailsFragment.newInstance(index);


                FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();

                ft.replace(R.id.details, details);
                ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                ft.commit();
            }

        } else {

            Intent intent = new Intent();
            intent.setClass(requireActivity(), DetailsActivity.class);
            intent.putExtra("index", index);
            intent.putExtra("object",adapter.getDataset().get(index).toString());
            startActivity(intent);
        }
    }

    public void initData() {
        findListViewModel=new ViewModelProvider(requireActivity()).get(FindListViewModel.class);
        LiveData<List<Photo>> photoLiveData=findListViewModel.getDataSnapshotLiveData();

        photoLiveData.observe(getViewLifecycleOwner(),data -> {
            if(data!=null){
                adapter.updateDataset(data.get(0));
            }
        });
    }

    private void initListView(View root) {
        RecyclerView recyclerView =  root.findViewById(R.id.recylerView_photos);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        adapter = new CustomListAdapter(getContext(),this);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void recyclerViewListClicked(View view, int position) {

        findListViewModel.select(adapter.getDataset().get(position));
        showDetails(position);

    }
}