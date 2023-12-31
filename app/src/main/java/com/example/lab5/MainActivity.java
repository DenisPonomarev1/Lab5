package com.example.lab5;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;

import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;


import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    EditText editTextName;
    EditText editTextPrice;
    Button buttonAddProduct;
    ListView listViewProducts;

    List<Product> products;
    DatabaseReference databaseProducts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        editTextName = (EditText) findViewById(R.id.editTextName);
        editTextPrice = (EditText) findViewById(R.id.editTextPrice);
        listViewProducts = (ListView) findViewById(R.id.listViewProducts);
        buttonAddProduct = (Button) findViewById(R.id.addButton);

        products = new ArrayList<>();

        databaseProducts = FirebaseDatabase.getInstance("https://product-c3220-default-rtdb.firebaseio.com/").getReference("products");
        FirebaseApp.initializeApp(this);


        //adding an onclicklistener to button
        buttonAddProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addProduct();
            }
        });

        listViewProducts.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                Product product = products.get(i);
                showUpdateDeleteDialog(product.getId(), product.getProductName());
                return true;
            }
        });


    }


    @Override
    protected void onStart() {
        super.onStart();
        // attaching value event listener
        databaseProducts.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                products.clear();

                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()){
                    //getting product
                    Product product = postSnapshot.getValue(Product.class);
                    // adding product to the list
                    products.add(product);
                }
                // creating adapter
                ProductList productsAdapter = new ProductList(MainActivity.this,products);
                // attaching adapter to listview
                listViewProducts.setAdapter(productsAdapter);

            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }


    private void showUpdateDeleteDialog(final String productId, String productName) {

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.update_dialog, null);
        dialogBuilder.setView(dialogView);

        final EditText editTextName = (EditText) dialogView.findViewById(R.id.editTextName);
        final EditText editTextPrice  = (EditText) dialogView.findViewById(R.id.editTextPrice);
        final Button buttonUpdate = (Button) dialogView.findViewById(R.id.buttonUpdateProduct);
        final Button buttonDelete = (Button) dialogView.findViewById(R.id.buttonDeleteProduct);

        dialogBuilder.setTitle(productName);
        final AlertDialog b = dialogBuilder.create();
        b.show();

        buttonUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = editTextName.getText().toString().trim();
                double price = Double.parseDouble(String.valueOf(editTextPrice.getText().toString()));
                if (!TextUtils.isEmpty(name)) {
                    updateProduct(productId, name, price);
                    b.dismiss();
                }
            }
        });

        buttonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteProduct(productId);
                b.dismiss();
            }
        });
    }

    private void updateProduct(String id, String name, double price) {
        //getting the specific product reference
        DatabaseReference dR = FirebaseDatabase.getInstance("https://product-c3220-default-rtdb.firebaseio.com/").getReference("products").child(id);
        // Updating the product
        Product product = new Product(id,name ,price);
        dR.setValue(product);

        // Success message
        Toast.makeText(getApplicationContext(), "Product Updated", Toast.LENGTH_LONG).show();
    }

    private boolean deleteProduct(String id) {


        // getting the specified product reference
        DatabaseReference dR = FirebaseDatabase.getInstance("https://product-c3220-default-rtdb.firebaseio.com/").getReference("products").child(id);

        // removing product from the database
        dR.removeValue();

        // success message
        Toast.makeText(getApplicationContext(), "Product Removed", Toast.LENGTH_LONG).show();
        return true;
    }

    private void addProduct() {
        // getting values to save
        String name = editTextName.getText().toString().trim();
        double price = Double.parseDouble(editTextPrice.getText().toString());
        if (!TextUtils.isEmpty(name)){

            //displaying a success toast
            // getting the unique id and use it as Primary key for product
            String id = databaseProducts.push().getKey();
            // creating a Product object
            Product product = new Product(id, name, price);
            //saving the Product
            databaseProducts.child(id).setValue(product);
            //clear the TextBoxes
            editTextName.setText("");
            editTextPrice.setText("");

            Toast.makeText(this, "Product added", Toast.LENGTH_LONG).show();
        }
        else{
            Toast.makeText(this, "Please enter a name", Toast.LENGTH_LONG).show();
        }
    }

}