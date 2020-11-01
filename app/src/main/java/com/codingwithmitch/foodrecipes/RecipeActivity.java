package com.codingwithmitch.foodrecipes;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;


import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.codingwithmitch.foodrecipes.models.Recipe;
import com.codingwithmitch.foodrecipes.util.Resource;
import com.codingwithmitch.foodrecipes.viewmodels.RecipeViewModel;

public class RecipeActivity extends BaseActivity {

    private static final String TAG = "RecipeActivity";

    // UI components
    private AppCompatImageView mRecipeImage;
    private TextView mRecipeTitle, mRecipeRank;
    private LinearLayout mRecipeIngredientsContainer;
    private ScrollView mScrollView;

    private RecipeViewModel mRecipeViewModel;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe);
        mRecipeImage = findViewById(R.id.recipe_image);
        mRecipeTitle = findViewById(R.id.recipe_title);
        mRecipeRank = findViewById(R.id.recipe_social_score);
        mRecipeIngredientsContainer = findViewById(R.id.ingredients_container);
        mScrollView = findViewById(R.id.parent);

        mRecipeViewModel = new ViewModelProvider(this).get(RecipeViewModel.class);

        getIncomingIntent();
    }

    private void getIncomingIntent() {
        if (getIntent().hasExtra("recipe")) {
            Recipe recipe = getIntent().getParcelableExtra("recipe");
            Log.d(TAG, "getIncomingIntent: " + recipe.getTitle());

            subscribeObservers(recipe.getRecipe_id());

        }
    }

    private void subscribeObservers(final String recipeId) {
        mRecipeViewModel.searchRecipeApi(recipeId).observe(this, new Observer<Resource<Recipe>>() {
            @Override
            public void onChanged(Resource<Recipe> recipeResource) {
                if (recipeResource != null) {
                    if (recipeResource.data != null) {
                        switch (recipeResource.status) {
                            case LOADING: {
                                showProgressBar(true);
                                break;
                            }
                            case ERROR: {
                                Log.e(TAG, "onChanged: status: ERROR, Recipe: " + recipeResource.data.getTitle());
                                Log.e(TAG, "onChanged: ERROR message: " + recipeResource.message);
                                showParent();
                                showProgressBar(false);
                                setRecipeProperties(recipeResource.data);
                                break;
                            }
                            case SUCCESS: {
                                Log.d(TAG, "onChanged: cache has been refreshed");
                                Log.d(TAG, "onChanged: status: SUCCESS, Recipe : " + recipeResource.data.getTitle());
                                showParent();
                                showProgressBar(false);
                                setRecipeProperties(recipeResource.data);
                                break;
                            }
                        }
                    }
                }
            }
        });
    }

    private void setRecipeProperties(Recipe recipe) {
        if (recipe != null) {
            RequestOptions requestOptions = new RequestOptions()
                    .placeholder(R.drawable.white_background)
                    .error(R.drawable.white_background);

            Glide.with(this)
                    .setDefaultRequestOptions(requestOptions)
                    .load(recipe.getImage_url())
                    .into(mRecipeImage);

            mRecipeTitle.setText(recipe.getTitle());
            mRecipeRank.setText(String.valueOf(Math.round(recipe.getSocial_rank())));

            setIngredients(recipe);
        }
    }

    private void setIngredients(Recipe recipe) {
        mRecipeIngredientsContainer.removeAllViews();
        if (recipe.getIngredients() != null) {

            for (String ingredient : recipe.getIngredients()) {
                TextView textview = new TextView(this);
                textview.setText(ingredient);
                textview.setTextSize(15);
                mRecipeIngredientsContainer.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                mRecipeIngredientsContainer.addView(textview);
            }
        }
        else {
            TextView textview = new TextView(this);
            textview.setText("Error retrieving ingredients. \nPlease check network connection.");
            textview.setTextSize(15);
            mRecipeIngredientsContainer.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            mRecipeIngredientsContainer.addView(textview);
        }
    }


    private void showParent() {
        mScrollView.setVisibility(View.VISIBLE);
    }
}














