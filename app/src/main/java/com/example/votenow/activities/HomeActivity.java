package com.example.votenow.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.votenow.R;
import com.example.votenow.adapters.CategoryAdapter;
import com.example.votenow.adapters.ProductAdapter;
import com.example.votenow.databinding.ActivityHomeBinding;
import com.example.votenow.model.Category;
import com.example.votenow.model.Product;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.hishd.tinycart.model.Cart;
import com.hishd.tinycart.util.TinyCartHelper;
import com.mancj.materialsearchbar.MaterialSearchBar;


import org.imaginativeworld.whynotimagecarousel.model.CarouselItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Objects;

public class HomeActivity extends AppCompatActivity {

    ActivityHomeBinding binding;
    CategoryAdapter categoryAdapter;
    ArrayList<Category> categories;

    ProductAdapter productAdapter;
    ArrayList<Product> products;
    DatabaseReference database;
    RecyclerView productList;

    int cartItem;

    NetworkChangeListener networkChangeListener = new NetworkChangeListener();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);

        Objects.requireNonNull(getSupportActionBar()).hide();


        initCategorise();
        //initProducts();
        initSlider();

        //Firebase for fetching data....
        productList = findViewById(R.id.productList);
        database = FirebaseDatabase.getInstance().getReference("deliexpress/Product");
        productList.setHasFixedSize(true);
        productList.setLayoutManager(new LinearLayoutManager(this));
        GridLayoutManager layoutManager = new GridLayoutManager(this,2);
        productList.setLayoutManager((layoutManager));


        products = new ArrayList<>();
        productAdapter = new ProductAdapter(this,products);
        productList.setAdapter(productAdapter);
        System.out.println("Conexion a la base de datos: "+database);

        database.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                products.clear();
                for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                    Product product = dataSnapshot.getValue(Product.class);
                    products.add(product);
                    System.out.println("AAAAAA: "+Product.class);
                }
                productAdapter.notifyDataSetChanged();
                //Log.i("Saim",products.toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(HomeActivity.this, "Error Occurred", Toast.LENGTH_SHORT).show();
            }
        });


        //Seaching for Product

        binding.searchBar.setOnSearchActionListener(new MaterialSearchBar.OnSearchActionListener() {
            @Override
            public void onSearchStateChanged(boolean enabled) {}

            @Override
            public void onSearchConfirmed(CharSequence text) {
                Intent intent = new Intent(HomeActivity.this, SearchActivity.class);
                intent.putExtra("query", text.toString());
                startActivity(intent);
            }

            @Override
            public void onButtonClicked(int buttonCode) {}
        });



        //Cart Badge added...
        Cart cart = TinyCartHelper.getCart();
        if(cart.getAllItemsWithQty().entrySet().size() !=0 ) {
            BadgeDrawable badgeDrawable = bottomNavigationView.getOrCreateBadge(R.id.cart);
            badgeDrawable.setVisible(true);
            badgeDrawable.setNumber(cart.getAllItemsWithQty().entrySet().size());
        }

        //Clearing Cart
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String value = extras.getString("check");
            //The key argument here must match that used in the other activity
            if(value.equals("placed")){
                Intent intent = new Intent(HomeActivity.this,OrderedProduct.class);
                startActivity(intent);
                cart.clearCart();
                BadgeDrawable badgeDrawable = bottomNavigationView.getOrCreateBadge(R.id.cart);
                badgeDrawable.setVisible(false);
            }

        }



//////////////// ..................Bottom Navigation.............../////////////////////////////////////


        bottomNavigationView.setSelectedItemId(R.id.home);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.cart:
                        startActivity(new Intent(getApplicationContext(), CartActivity.class));
                        overridePendingTransition(0, 0);
                        return true;
                    case R.id.home:
                        return true;
                    case R.id.profile:
                        startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
                        overridePendingTransition(0, 0);
                        return true;
                }
                return false;
            }
        });



    }

    protected void onStart() {

        IntentFilter intentFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(networkChangeListener,intentFilter);
        super.onStart();
    }

    protected void onStop() {
        unregisterReceiver(networkChangeListener);
        super.onStop();
    }


    private void initSlider() {

        binding.carousel.addData(new  CarouselItem("https://img.freepik.com/free-vector/flat-design-food-banner-template_23-2149076251.jpg", "Título de la imagen"));
        binding.carousel.addData(new  CarouselItem("https://www.comedera.com/wp-content/uploads/2022/03/Anticucho-shutterstock_185287433.jpg", "Título de la imagen"));
        binding.carousel.addData(new  CarouselItem("https://calleysazonhome.files.wordpress.com/2019/04/5b06e729b7e0f.jpeg", "Título de la imagen"));
        binding.carousel.addData(new  CarouselItem("https://comeperuano.b-cdn.net/wp-content/uploads/2020/04/teque%C3%B1os-peruanos.jpg", "Título de la imagen"));

        /*
        FirebaseFirestore dbroot = FirebaseFirestore.getInstance();
        dbroot.collection("deliexpress").document("sliderImage")
                .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if(documentSnapshot.exists()){
                            String img = documentSnapshot.getString("Products");

                            binding.carousel.addData(new CarouselItem(documentSnapshot.getString("a")));
                            binding.carousel.addData(new CarouselItem(documentSnapshot.getString("b")));
                            binding.carousel.addData(new CarouselItem(documentSnapshot.getString("c")));
                            binding.carousel.addData(new CarouselItem(documentSnapshot.getString("d")));
                            binding.carousel.addData(new CarouselItem(documentSnapshot.getString("e")));
                            binding.carousel.addData(new CarouselItem(documentSnapshot.getString("f")));
                            binding.carousel.addData(new CarouselItem(documentSnapshot.getString("g")));
                            binding.carousel.addData(new CarouselItem(documentSnapshot.getString("h")));
                            binding.carousel.addData(new CarouselItem(documentSnapshot.getString("i")));
                            binding.carousel.addData(new CarouselItem(documentSnapshot.getString("j")));

                        }

                    }


                });*/

    }

    void initCategorise(){
        categories = new ArrayList<>();
        categories.add(new Category("Comida criolla","https://cdn-icons-png.flaticon.com/128/2474/2474411.png","#d8d8d8","Some text",1));
        categories.add(new Category("Mariscos","https://cdn-icons-png.flaticon.com/128/1261/1261163.png","#d8d8d8","Some text",1));
        categories.add(new Category("Parrillas","https://cdn-icons-png.flaticon.com/128/857/857455.png","#d8d8d8","Some text",1));
        categories.add(new Category("Piqueos","https://cdn-icons-png.flaticon.com/128/2450/2450309.png","#d8d8d8","Some text",1));
        categories.add(new Category("Pastas","https://cdn-icons-png.flaticon.com/128/3893/3893209.png","#d8d8d8","Some text",1));
        categories.add(new Category("Sopas","https://cdn-icons-png.flaticon.com/128/1005/1005769.png","#d8d8d8","Some text",1));
        categories.add(new Category("Refrescos","https://cdn-icons-png.flaticon.com/128/1084/1084008.png","#d8d8d8","Some text",1));
        categories.add(new Category("Comidas Oriental","https://cdn-icons-png.flaticon.com/512/3109/3109867.png","#d8d8d8","Some text",1));

        categoryAdapter = new CategoryAdapter(this, categories);

        GridLayoutManager layoutManager = new GridLayoutManager(this,4);
        binding.categoriesList.setLayoutManager((layoutManager));
        binding.categoriesList.setAdapter(categoryAdapter);
    }

    void  initProducts(){
        products = new ArrayList<>();
        //products.add(new Product("Ceviche",
        //        "https://blog.plazavea.com.pe/wp-content/uploads/2022/01/Ceviche-1200x675.jpg",
        //        "Nothing","Mariscos",30,5,10,1));
        //products.add(new Product("Tallarines rojos",
        //        "https://i.ytimg.com/vi/9Vg4LIB3-KA/maxresdefault.jpg",
        //        "Nothing","Pastas",25,2,10,1));
        //products.add(new Product("Arroz con leche",
        //        "https://imagesvc.meredithcorp.io/v3/mm/image?url=https%3A%2F%2Fstatic.onecms.io%2Fwp-content%2Fuploads%2Fsites%2F21%2F2018%2F03%2F25%2Frecetas-1092-arroz-con-leche-2000.jpg",
        //        "Nothing","Postres",10,10,10,1));
        //productAdapter = new ProductAdapter(this,products);

        GridLayoutManager layoutManager = new GridLayoutManager(this,2);
        binding.productList.setLayoutManager((layoutManager));
        binding.productList.setAdapter(productAdapter);
    }

}