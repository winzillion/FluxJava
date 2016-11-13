/*
 * Copyright (C) 2016 Bugs will find a way (https://wznote.blogspot.com)
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
package com.example.fluxjava.eventbus.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.fluxjava.eventbus.R;
import com.example.fluxjava.eventbus.domain.stores.UserStore;
import io.wzcodes.fluxjava.FluxContext;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import static com.example.fluxjava.eventbus.domain.Constants.DATA_USER;

public class UserAdapter extends BaseAdapter {

    private UserStore mStore;

    public UserAdapter() {
        // get the instance of store that will provide data
        this.mStore = (UserStore)FluxContext.getInstance().getStore(DATA_USER, null, this);
    }

    @Override
    public int getCount() {
        return this.mStore.getCount();
    }

    @Override
    public Object getItem(final int inPosition) {
        return this.mStore.getItem(inPosition);
    }

    @Override
    public long getItemId(final int inPosition) {
        return inPosition;
    }

    @Override
    public View getView(final int inPosition, final View inConvertView, final ViewGroup inParent) {
        View itemView = inConvertView;

        // bind data into item view of Spinner
        if (itemView == null) {
            itemView = LayoutInflater
                    .from(inParent.getContext())
                    .inflate(R.layout.item_user, inParent, false);
        }

        if (itemView instanceof TextView) {
            ((TextView)itemView).setText(this.mStore.getItem(inPosition).name);
        }

        return itemView;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(final UserStore.ListChangeEvent inEvent) {
        super.notifyDataSetChanged();
    }

    public void dispose() {
        // Clear object reference to avoid memory leak issue
        FluxContext.getInstance().unregisterStore(this.mStore, this);
    }

}