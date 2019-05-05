package com.github.a18antsv.medialibraryapp;

import android.content.DialogInterface;
import android.database.Cursor;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.github.a18antsv.medialibraryapp.fragments.FragmentAddList;

import java.util.ArrayList;
import java.util.List;

import static com.github.a18antsv.medialibraryapp.DataContract.Entry.*;

public class MainActivity extends AppCompatActivity implements FragmentAddList.onDataPassListener {
    private List<String> lists;
    private DbHelper dbHelper;
    private ListView listView;
    private ArrayAdapter adapter;
    private FloatingActionButton addListFab;
    private FragmentAddList fragmentAddList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView = (ListView) findViewById(R.id.listview_lists);
        lists = new ArrayList<>();
        addListFab = (FloatingActionButton) findViewById(R.id.fab_add_list);

        dbHelper = new DbHelper(this);
        dbHelper.getWritableDatabase();

        Cursor c = dbHelper.getData("SELECT * FROM " + LIST_TABLE_NAME);
        for(c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
            lists.add(c.getString(c.getColumnIndex(LIST_COL_NAME)));
        }
        c.close();
        adapter = new ArrayAdapter(this, R.layout.listview_lists_item, R.id.list_name, lists);
        listView.setAdapter(adapter);
        registerForContextMenu(listView);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Toast.makeText(getApplicationContext(), (String) adapterView.getItemAtPosition(i), Toast.LENGTH_SHORT).show();
            }
        });

        addListFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(findViewById(R.id.fragment_addlist_container) != null) {
                    fragmentAddList = new FragmentAddList();
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_addlist_container, fragmentAddList).commit();
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.test:
                Toast.makeText(getApplicationContext(), "TEST", Toast.LENGTH_SHORT).show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        getMenuInflater().inflate(R.menu.context_menu, menu);
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        String listName = (String) adapter.getItem(info.position);
        menu.setHeaderTitle(listName);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        final AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        final String listName = adapter.getItem(info.position).toString();

        switch(item.getItemId()) {
            case R.id.option_edit:

                return true;
            case R.id.option_delete:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Are you sure?");
                builder.setMessage("This list and all its content will be deleted.");
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        int deletedRows = dbHelper.deleteList(listName);
                        lists.remove((int)(long)info.id);
                        adapter.notifyDataSetChanged();
                        Toast.makeText(getApplicationContext(), "Successfully deleted " + deletedRows + " row(s)", Toast.LENGTH_SHORT).show();
                    }
                });
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                });
                builder.create().show();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    @Override
    public void onDataPass(String data) {
        if(!dbHelper.duplicateData(LIST_TABLE_NAME, LIST_COL_NAME, data)) {
            if(dbHelper.insertList(data)) {
                lists.add(data);
                adapter.notifyDataSetChanged();
                Toast.makeText(getApplicationContext(), "List created!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(), "Something went wrong...", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getApplicationContext(), "Multiple lists cannot have the same name...", Toast.LENGTH_SHORT).show();
        }
    }
}