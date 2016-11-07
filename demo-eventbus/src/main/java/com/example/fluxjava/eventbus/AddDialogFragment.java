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
package com.example.fluxjava.eventbus;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.example.fluxjava.eventbus.domain.models.Todo;
import io.wzcodes.fluxjava.FluxContext;

import static com.example.fluxjava.eventbus.domain.Constants.TODO_ADD;

public class AddDialogFragment extends AppCompatDialogFragment {

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inInflater,
                             @Nullable final ViewGroup inContainer,
                             @Nullable final Bundle inSavedInstanceState) {
        return inInflater.inflate(R.layout.dialog_add, inContainer);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(final Bundle inSavedInstanceState) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(super.getActivity());
        final LayoutInflater inflater = super.getActivity().getLayoutInflater();
        final ViewGroup nullParent = null;

        // display an alertDialog for input a new todo item
        builder.setView(inflater.inflate(R.layout.dialog_add, nullParent))
                .setTitle(R.string.dialog_title)
                .setPositiveButton(R.string.add, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface inDialog, final int inId) {
                        final AlertDialog alertDialog = (AlertDialog)inDialog;
                        final Todo todo = new Todo();
                        final EditText title = (EditText)alertDialog.findViewById(R.id.title);
                        final EditText memo = (EditText)alertDialog.findViewById(R.id.memo);
                        final EditText dueDate = (EditText)alertDialog.findViewById(R.id.dueText);

                        if (title != null) {
                            todo.title = title.getText().toString();
                        }
                        if (memo != null) {
                            todo.memo = memo.getText().toString();
                        }
                        if (dueDate != null) {
                            todo.dueDate = dueDate.getText().toString();
                        }
                        // the input data will be sent to store by using bus
                        FluxContext.getInstance()
                                .getActionCreator()
                                .sendRequestAsync(TODO_ADD, todo);
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface inDialog, final int inId) {
                        // Do nothing
                    }
                });

        return builder.create();
    }

}
