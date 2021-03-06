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
package com.example.fluxjava.rx.adapters;

import android.graphics.Paint;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.example.fluxjava.rx.R;
import com.example.fluxjava.rx.domain.models.Todo;
import com.example.fluxjava.rx.domain.stores.TodoStore;
import io.wzcodes.fluxjava.FluxContext;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

import static com.example.fluxjava.rx.domain.Constants.DATA_TODO;
import static com.example.fluxjava.rx.domain.Constants.TODO_CLOSE;

public class TodoAdapter extends RecyclerView.Adapter<TodoAdapter.ViewHolder> {

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        TextView dueDate;
        TextView memo;
        CheckBox closed;

        ViewHolder(final View inItemView) {
            super(inItemView);
            this.title = (TextView)inItemView.findViewById(R.id.title);
            this.dueDate = (TextView)inItemView.findViewById(R.id.dueDate);
            this.memo = (TextView)inItemView.findViewById(R.id.memo);
            this.closed = (CheckBox)inItemView.findViewById(R.id.closed);
        }
    }

    private TodoStore mStore;

    public TodoAdapter() {
        // get the instance of store that will provide data
        this.mStore = (TodoStore)FluxContext.getInstance().getStore(DATA_TODO, null, null);
        this.mStore.toObservable(TodoStore.ListChangeEvent.class)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        new Action1<TodoStore.ListChangeEvent>() {
                            @Override
                            public void call(final TodoStore.ListChangeEvent inEvent) {
                                TodoAdapter.super.notifyDataSetChanged();
                            }
                        },
                        new Action1<Throwable>() {
                            @Override
                            public void call(Throwable inThrowable) {
                                // put error handle here
                            }
                        });
        this.mStore.toObservable(TodoStore.ItemChangeEvent.class)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        new Action1<TodoStore.ItemChangeEvent>() {
                            @Override
                            public void call(final TodoStore.ItemChangeEvent inEvent) {
                                TodoAdapter.super.notifyItemChanged(inEvent.position);
                            }
                        },
                        new Action1<Throwable>() {
                            @Override
                            public void call(Throwable inThrowable) {
                                // put error handle here
                            }
                        });
    }

    @Override
    public ViewHolder onCreateViewHolder(final ViewGroup inParent, final int inViewType) {
        final View itemView = LayoutInflater
                .from(inParent.getContext()).inflate(R.layout.item_todo, inParent, false);

        // use custom ViewHolder to display data
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final ViewHolder inViewHolder, final int inPosition) {
        final Todo item = this.mStore.getItem(inPosition);

        // bind data into item view of RecyclerView
        inViewHolder.title.setText(item.title);
        if (item.closed) {
            inViewHolder.title.setPaintFlags(
                    inViewHolder.title.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            inViewHolder.title.setPaintFlags(
                    inViewHolder.title.getPaintFlags() & (~ Paint.STRIKE_THRU_TEXT_FLAG));
        }
        inViewHolder.dueDate.setText(item.dueDate);
        inViewHolder.memo.setText(item.memo);
        inViewHolder.closed.setOnCheckedChangeListener(null);
        inViewHolder.closed.setChecked(item.closed);
        inViewHolder.closed.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(final CompoundButton inButtonView, final boolean inIsChecked) {
                item.closed = inIsChecked;
                FluxContext.getInstance()
                        .getActionCreator()
                        .sendRequestAsync(TODO_CLOSE, item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return this.mStore.getCount();
    }

    public void dispose() {
        // Clear object reference to avoid memory leak issue
        FluxContext.getInstance().unregisterStore(this.mStore, this);
    }

}