package com.example.socialchat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.socialchat.Utills.Comment;
import com.example.socialchat.Utills.Posts;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.auth.api.signin.internal.Storage;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;

import de.hdodenhof.circleimageview.CircleImageView;


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{
    Toolbar toolbar;
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    FirebaseAuth mAuth;
    FirebaseUser mUser;
    DatabaseReference mUserRef,PostRef,LikeRef,CommentRef;
    String progileImageUrlV,usernameV;
    CircleImageView profileImageHeader;
    TextView usernameHeader;
    ImageView addImagePost,sendImagePost;
    EditText inputPostDesc;
    private static final int REQUEST_CODE = 101;
    Uri imageUri;
    ProgressDialog mLoadingBar;
    StorageReference postImageRef;
    FirebaseRecyclerAdapter<Posts,MyViewHolder>adapter;
    FirebaseRecyclerOptions<Posts>options;
    RecyclerView recyclerView;
    FirebaseRecyclerOptions<Comment>CommentOption;
    FirebaseRecyclerAdapter<Comment,CommentViewHolder>CommentAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar=findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Chat App");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu);
        addImagePost=findViewById(R.id.addimagePost);
        sendImagePost=findViewById(R.id.send_post_imageView);
        inputPostDesc=findViewById(R.id.inputAddPost);
        mLoadingBar=new ProgressDialog(this);
        recyclerView=findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        mAuth=FirebaseAuth.getInstance();
        mUser=mAuth.getCurrentUser();
        mUserRef= FirebaseDatabase.getInstance().getReference().child("Users");
        PostRef= FirebaseDatabase.getInstance().getReference().child("Posts");
        LikeRef= FirebaseDatabase.getInstance().getReference().child("Likes");
        CommentRef= FirebaseDatabase.getInstance().getReference().child("Comments");
        postImageRef= FirebaseStorage.getInstance().getReference().child("PostImages");


        drawerLayout=findViewById(R.id.drawerLayout);
        navigationView=findViewById(R.id.navView);
        View view=navigationView.inflateHeaderView(R.layout.drawer_header);
        profileImageHeader=view.findViewById(R.id.profileImage_header);
        usernameHeader=view.findViewById(R.id.username_header);


        navigationView.setNavigationItemSelectedListener(this);
        sendImagePost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddPost();
            }
        });
        addImagePost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent,REQUEST_CODE);

            }
        });
        LoadPost();

    }

    private void LoadPost() {
        options=new FirebaseRecyclerOptions.Builder<Posts>().setQuery(PostRef,Posts.class).build();
        adapter=new FirebaseRecyclerAdapter<Posts, MyViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull MyViewHolder holder, int position, @NonNull Posts model) {
                String postKey=getRef(position).getKey();
                holder.postDesc.setText(model.getPostDesc());
                String timeAgo=calculateTimeAgo(model.getDatePost());
                holder.timeAgo.setText(timeAgo);
                holder.username.setText(model.getUsername());
                Picasso.get().load(model.getPostImageUrl()).into(holder.postImage);
                Picasso.get().load(model.getUserProfileImageUrl()).into(holder.profileImage);
                holder.countLikes(postKey,mUser.getUid(),LikeRef);
                holder.countComments(postKey,mUser.getUid(),CommentRef);
                holder.likeImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                         LikeRef.child(postKey).child(mUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                             @Override
                             public void onDataChange(@NonNull DataSnapshot snapshot) {
                                 if(snapshot.exists()){
                                     LikeRef.child(postKey).child(mUser.getUid()).removeValue();
                                     holder.likeImage.setColorFilter(Color.GRAY);
                                     notifyDataSetChanged();
                                 }else {
                                     LikeRef.child(postKey).child(mUser.getUid()).setValue("like");
                                     holder.likeImage.setColorFilter(Color.GREEN);
                                     notifyDataSetChanged();

                                 }
                             }

                             @Override
                             public void onCancelled(@NonNull DatabaseError error) {
                                 Toast.makeText(MainActivity.this, ""+error.getMessage(), Toast.LENGTH_SHORT).show();

                             }
                         });
                    }
                });
                holder.commentSend.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String comment=holder.inputComments.getText().toString();
                        if(comment.isEmpty()){
                            Toast.makeText(MainActivity.this, "Please write something in EditText", Toast.LENGTH_SHORT).show();
                        }else {
                            AddComment(holder,postKey,CommentRef,mUser.getUid(),comment);
                        }
                    }
                });
                LoadComment(postKey);
                holder.postImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent=new Intent(MainActivity.this,ImageViewActivity.class);
                        intent.putExtra("url",model.getPostImageUrl());
                        startActivity(intent);
                    }
                });


            }

            @NonNull
            @Override
            public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_view_post,parent,false);
                return new MyViewHolder(view);
            }
        };
        adapter.startListening();
        recyclerView.setAdapter(adapter);
    }

    private void LoadComment(String postKey) {
        MyViewHolder.recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
        CommentOption=new FirebaseRecyclerOptions.Builder<Comment>().setQuery(CommentRef.child(postKey),Comment.class).build();
        CommentAdapter=new FirebaseRecyclerAdapter<Comment, CommentViewHolder>(CommentOption) {
            @Override
            protected void onBindViewHolder(@NonNull CommentViewHolder holder, int position, @NonNull Comment model) {
                Picasso.get().load(model.getProfileImageUrl()).into(holder.profileImage);
                holder.username.setText(model.getUsername());
                holder.comment.setText(model.getComment());

            }

            @NonNull
            @Override
            public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view =LayoutInflater.from(parent.getContext()).inflate(R.layout.single_view_comment,parent,false);
                return new CommentViewHolder(view);
            }
        };
        CommentAdapter.startListening();
        MyViewHolder.recyclerView.setAdapter(CommentAdapter);

    }

    private void AddComment(MyViewHolder holder, String postKey, DatabaseReference commentRef, String uid, String comment) {
        HashMap hashMap = new HashMap();
        hashMap.put("username",usernameV);
        hashMap.put("profileImageUrl",progileImageUrlV);
        hashMap.put("comment",comment);

        commentRef.child(postKey).child(uid).updateChildren(hashMap).addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                if(task.isSuccessful()){
                    Toast.makeText(MainActivity.this, "Comments Added", Toast.LENGTH_SHORT).show();
                    adapter.notifyDataSetChanged();
                    holder.inputComments.setText(null);
                }
                else {
                    Toast.makeText(MainActivity.this, ""+task.getException().toString(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private String calculateTimeAgo(String datePost) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-M-yyy hh:mm:ss");

        try {
            long time = sdf.parse(datePost).getTime();
            long now = System.currentTimeMillis();
            CharSequence ago =
                    DateUtils.getRelativeTimeSpanString(time, now, DateUtils.MINUTE_IN_MILLIS);
            return ago+"";
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return "";
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==REQUEST_CODE && resultCode==RESULT_OK && data !=null){
            imageUri=data.getData();
            addImagePost.setImageURI(imageUri);
        }
    }

    private void AddPost() {
        String postDesc=inputPostDesc.getText().toString();
        if(postDesc.isEmpty()||postDesc.length()<3){
            inputPostDesc.setError("Please write something in post Des.");
        } else if (imageUri==null) {
            Toast.makeText(this, "Please select an image", Toast.LENGTH_SHORT).show();
            
        }else {
            mLoadingBar.setTitle("Adding Post");
            mLoadingBar.setCanceledOnTouchOutside(false);
            mLoadingBar.show();
            postImageRef.child(mUser.getUid()).putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if(task.isSuccessful()){
                        postImageRef.child(mUser.getUid()).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                Date date = new Date();
                                SimpleDateFormat formatter = new SimpleDateFormat("dd-M-yyy hh:mm:ss");
                                String strDate = formatter.format(date);

                                HashMap hashMap=new HashMap();
                                hashMap.put("datePost",strDate);
                                hashMap.put("postImageUrl",uri.toString());
                                hashMap.put("postDesc",postDesc);
                                hashMap.put("userProfileImageUrl",progileImageUrlV);
                                hashMap.put("username",usernameV);
                                PostRef.child(mUser.getUid()).updateChildren(hashMap).addOnCompleteListener(new OnCompleteListener() {
                                    @Override
                                    public void onComplete(@NonNull Task task) {
                                        if(task.isSuccessful()){
                                            mLoadingBar.dismiss();
                                            Toast.makeText(MainActivity.this, "Post Added", Toast.LENGTH_SHORT).show();
                                            addImagePost.setImageURI(null);
                                            inputPostDesc.setText("");
                                        }else{
                                            mLoadingBar.dismiss();
                                            Toast.makeText(MainActivity.this, ""+task.getException().toString(), Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            }
                        });

                    }else {
                        mLoadingBar.dismiss();
                        Toast.makeText(MainActivity.this, ""+task.getException().toString(), Toast.LENGTH_SHORT).show();

                    }

                }
            });

        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(mUser==null){
            SendUserToLoginActivity();
        }else {
            mUserRef.child(mUser.getUid()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists()){
                        progileImageUrlV=dataSnapshot.child("profileImage").getValue().toString();
                        usernameV=dataSnapshot.child("username").getValue().toString();
                        Picasso.get().load(progileImageUrlV).into(profileImageHeader);
                        usernameHeader.setText(usernameV);

                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(MainActivity.this, "Sorry! Something going Wrong", Toast.LENGTH_SHORT).show();

                }
            });
        }
    }

    private void SendUserToLoginActivity() {
        Intent intent=new Intent(MainActivity.this,LoginActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        switch (menuItem.getItemId()){
            case R.id.home:
                Toast.makeText(this, "Home", Toast.LENGTH_SHORT).show();
                break;
            case R.id.profile:
                startActivity(new Intent(MainActivity.this,ProfileActivity.class));
                break;
            case R.id.friend:
                startActivity(new Intent(MainActivity.this,FriendActitvity.class));
                break;
            case R.id.findFriend:
                startActivity(new Intent(MainActivity.this,FindFriendActivity.class));
                break;
            case R.id.chat:
                startActivity(new Intent(MainActivity.this,ChatUsersActivity.class));
                break;
            case R.id.logout:
                mAuth.signOut();
                Intent intent=new Intent(MainActivity.this,LoginActivity.class);
                startActivity(intent);
                finish();
                break;


        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId()==android.R.id.home){
            drawerLayout.openDrawer(GravityCompat.START);
            return true;
        }
        return true;
    }


}