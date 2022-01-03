package com.federicogovoni.permissionsmanager.view.main.contexts;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.RelativeLayout;

import androidx.fragment.app.Fragment;

import com.federicogovoni.permissionsmanager.controller.ProVersionChecker;
import com.federicogovoni.permissionsmanager.model.CurrentContext;
import com.federicogovoni.permissionmanager.R;
import com.federicogovoni.permissionsmanager.utils.TmpContextKeeper;
import com.federicogovoni.permissionsmanager.controller.ContextManager;
import com.federicogovoni.permissionsmanager.view.main.contexts.newcontext.NewContextActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import timber.log.Timber;

public class ContextsFragment extends Fragment {

    @BindView(R.id.fragment_contexts_add_context_button)
    FloatingActionButton addContextButton;

    @BindView(R.id.fragment_contexts_main_list_view)
    ListView contextsListView;

    @BindView(R.id.fragment_contexts_empty_relative_layout)
    RelativeLayout emptyRelativeLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_contexts, container, false);
        ButterKnife.bind(this, view);

        ProVersionChecker.checkIfPro(getActivity(), isPro -> {
            Timber.d("IProVersionListener invoked for %s", getClass().toString());
            if(isPro) {
                RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) addContextButton.getLayoutParams();
                layoutParams.setMargins(0, 0, layoutParams.getMarginEnd(), layoutParams.getMarginEnd());
                addContextButton.setLayoutParams(layoutParams);

                float density = getResources().getDisplayMetrics().density;
                contextsListView.setPadding(0, 0, 0, Math.round((float) 80 * density));
            }
        });

        if(ContextManager.getInstance(getActivity()).getContexts() != null && ContextManager.getInstance(getActivity()).getContexts().size() > 0) {
            emptyRelativeLayout.setVisibility(View.GONE);
            contextsListView.setVisibility(View.VISIBLE);
            ContextCardAdapter adapter = new ContextCardAdapter(getActivity());
            contextsListView.setAdapter(adapter);
        } else {
            contextsListView.setVisibility(View.GONE);
            emptyRelativeLayout.setVisibility(View.VISIBLE);
        }

        return view;
    }

    @Override
    public void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().setTitle(R.string.contexts);
    }

    @OnClick(R.id.fragment_contexts_add_context_button)
    public void onAddContextClick(View v) {
        Intent intent = new Intent(getActivity(), NewContextActivity.class);
        TmpContextKeeper.getInstance().setCurrentContext(new CurrentContext("", getActivity().getApplicationContext()));
        getActivity().startActivity(intent);
    }

    @Override
    public void onResume() {
        super.onResume();
        contextsListView.setAdapter(new ContextCardAdapter(getActivity()));
    }
}
