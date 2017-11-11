package com.example.fluxjava.rx2;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;

import com.example.fluxjava.rx2.adapters.TodoAdapter;
import com.example.fluxjava.rx2.adapters.UserAdapter;

import io.wzcodes.fluxjava.FluxContext;

import static com.example.fluxjava.rx2.domain.Constants.TODO_LOAD;
import static com.example.fluxjava.rx2.domain.Constants.USER_LOAD;

public class MainActivity extends AppCompatActivity {

    private UserAdapter mUserAdapter;
    private TodoAdapter mTodoAdapter;

    @Override
    protected void onCreate(final Bundle inSavedInstanceState) {
        super.onCreate(inSavedInstanceState);
        super.setContentView(R.layout.activity_main);
        this.setupRecyclerView();
        this.setupSpinner();
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu inMenu) {
        super.getMenuInflater().inflate(R.menu.menu_main, inMenu);

        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        // ask to get the list of user
        FluxContext.getInstance().getActionCreator().sendRequestAsync(USER_LOAD, USER_LOAD);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean result;

        switch (item.getItemId()) {
            case R.id.add:
                AddDialogFragment addDialog = new AddDialogFragment();

                // display an input dialog to get a new todo
                addDialog.show(super.getSupportFragmentManager(), "Add");
                result = true;
                break;
            default:
                result = super.onOptionsItemSelected(item);
                break;
        }

        return result;
    }

    @Override
    protected void onStop() {
        super.onStop();

        // release resources
        if (this.mUserAdapter != null) {
            this.mUserAdapter.dispose();
        }
        if (this.mTodoAdapter != null) {
            this.mTodoAdapter.dispose();
        }
    }

    private void setupSpinner() {
        final Spinner spinner = (Spinner)super.findViewById(R.id.spinner);

        if (spinner != null) {
            // configure spinner to show data
            this.mUserAdapter = new UserAdapter();
            spinner.setAdapter(this.mUserAdapter);
            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(final AdapterView<?> inParent, final View inView,
                                           final int inPosition, final long inId) {
                    // when user change the selection of spinner, change the list of recyclerView
                    FluxContext.getInstance()
                            .getActionCreator()
                            .sendRequestAsync(TODO_LOAD, TODO_LOAD + ":" + inPosition);
                }

                @Override
                public void onNothingSelected(final AdapterView<?> inParent) {
                    // Do nothing
                }
            });
        }
    }

    private void setupRecyclerView() {
        final RecyclerView recyclerView = (RecyclerView)super.findViewById(R.id.recyclerView);

        if (recyclerView != null) {
            // configure recyclerView to show data
            this.mTodoAdapter = new TodoAdapter();
            recyclerView.setAdapter(this.mTodoAdapter);
        }
    }

}
