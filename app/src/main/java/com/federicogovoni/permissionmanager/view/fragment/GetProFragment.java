/*
 * Copyright (C) 2011-2015 Dominik Schürmann <dominik@dominikschuermann.de>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.federicogovoni.permissionmanager.view.fragment;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.federicogovoni.permissionmanager.Constants;
import com.federicogovoni.permissionmanager.R;
import com.federicogovoni.permissionmanager.controller.ProVersionChecker;

import timber.log.Timber;

public class GetProFragment extends Fragment {

    public GetProFragment() {
        super(R.layout.fragment_get_pro);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {

    }

    private void setProVersion(boolean isPro) {

    }

    //metto quì i componenti che devono essere dis/abilitati

}