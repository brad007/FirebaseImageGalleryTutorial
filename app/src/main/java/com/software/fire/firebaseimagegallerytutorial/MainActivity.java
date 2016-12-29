package com.software.fire.firebaseimagegallerytutorial;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class MainActivity extends AppCompatActivity {

    private static final String IMAGES = "images";
    private RecyclerView mRecyclerView;
    private FirebaseRecyclerAdapter<String, FirebaseViewHolder> mAdapter;
    private DatabaseReference mRef;
    private StorageReference mSRef;

    private static final int RC_PHOTO_PICKER = 2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initialiseView();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/jpeg");
                intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                startActivityForResult(Intent.createChooser(intent, "Complete action using"), RC_PHOTO_PICKER);
            }
        });
    }

    private void initialiseView() {
        mRef = FirebaseDatabase.getInstance().getReference(IMAGES);
        mSRef = FirebaseStorage.getInstance().getReference(IMAGES);

        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerview);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));

        setupAdapter();
        mRecyclerView.setAdapter(mAdapter);
    }

    private void setupAdapter() {
        mAdapter = new FirebaseRecyclerAdapter<String, FirebaseViewHolder>(
                String.class,
                R.layout.layout_image,
                FirebaseViewHolder.class,
                mRef
        ) {
            @Override
            protected void populateViewHolder(FirebaseViewHolder viewHolder, String model, int position) {
                Glide.with(MainActivity.this)
                        .load(model)
                        .into(viewHolder.image);
            }
        };
    }

    public static class FirebaseViewHolder extends RecyclerView.ViewHolder {
        ImageView image;

        public FirebaseViewHolder(View itemView) {
            super(itemView);
            image = (ImageView) itemView.findViewById(R.id.image);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_PHOTO_PICKER) {
            if (resultCode == RESULT_OK) {
                Uri selectedImageUri = data.getData();
                mSRef.child(selectedImageUri.getLastPathSegment())
                        .putFile(selectedImageUri)
                        .addOnSuccessListener(MainActivity.this, new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                String downloadUrl = taskSnapshot.getDownloadUrl().toString();
                                mRef.push().setValue(downloadUrl);
                            }
                        });
            }
        }
    }
}
