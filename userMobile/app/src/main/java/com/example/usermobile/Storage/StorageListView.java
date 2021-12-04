package com.example.usermobile.Storage;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;


import com.example.usermobile.ProductAddition.ProductAdditionMenu;
import com.example.usermobile.R;
import com.example.usermobile.Settings.SettingsMenu;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;


public class StorageListView extends AppCompatActivity {
    private ListView storageListView;
    private Storage productStorage;
    private ArrayList<Product> productList;
    StorageListAdapter storageListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_storage_list_view);

        storageListView = (ListView)findViewById(R.id.storageList);
        populateProductList();

        storageListAdapter = new StorageListAdapter(productList, R.layout.product_list_row, this );
        storageListView.setAdapter(storageListAdapter);

        storageListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View v, int position, long id){
                Product product = (Product) parent.getItemAtPosition(position);

                ProductDialog productDialog = new ProductDialog(product, StorageListView.this);
                productDialog.show(getSupportFragmentManager(), "productDialog");

            }
        });

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation_view);
        bottomNavigationView.setSelectedItemId(R.id.storageListView);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.storageListView:
                        return true;
                    case R.id.settingsMenu:
                        startActivity(new Intent(getApplicationContext(), SettingsMenu.class));
                        overridePendingTransition(0, 0);
                        return true;
                    case R.id.productAdditionMenu:
                        startActivity(new Intent(getApplicationContext(), ProductAdditionMenu.class));
                        overridePendingTransition(0, 0);
                        return true;
                }

                return true;
            }
        });
    }

    private void populateProductList () {
        productStorage = new Storage();


        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                      productStorage.clearProducts();
                      for (DataSnapshot product : snapshot.getChildren()) {
                          Product currProduct = product.getValue(Product.class);
                          currProduct.setIdCode(product.getKey());
                          productStorage.addProduct(currProduct);
                      }
                      productList = productStorage.getProductList();
                      updateProductStorage(productStorage);
                      storageListAdapter.notifyDataSetChanged();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };

        FirebaseDatabase.getInstance().getReference().child("Users").child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())
                .child("Storage").addValueEventListener(valueEventListener);

        productList = productStorage.getProductList();
        sortProductListAscending();
    }

    /** Updates the products in the list. */
    public void updateProductStorage (Storage productStorage){
        this.productStorage = productStorage;
        productList = productStorage.getProductList();
        sortProductListAscending();
    }

    /** Sorts the productList in ascending order after expiration date. */
    public void sortProductListAscending () {
        Collections.sort(productList, new Comparator<Product>() {
            @Override
            public int compare(Product o1, Product o2) {
                return o1.getExpirationDate().compareTo(o2.getExpirationDate());
            }
        });
    }

    /** Deletes the product from the list and updates the listview. */
    public void deleteProduct (Product product) {

        FirebaseDatabase.getInstance().getReference().child("Users").child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())
                .child("Storage").child(product.getIdCode()).removeValue();

        Toast.makeText(this, "Product successfully deleted", Toast.LENGTH_SHORT).show();
    }
}