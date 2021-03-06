package com.codingwithmitch.foodrecipes;

import androidx.annotation.NonNull;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.integration.recyclerview.RecyclerViewPreloader;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.util.ViewPreloadSizeProvider;
import com.codingwithmitch.foodrecipes.adapters.OnRecipeListener;
import com.codingwithmitch.foodrecipes.adapters.RecipeRecyclerAdapter;
import com.codingwithmitch.foodrecipes.util.VerticalSpacingItemDecorator;
import com.codingwithmitch.foodrecipes.viewmodels.RecipeListViewModel;


public class RecipeListActivity extends BaseActivity implements OnRecipeListener {

    private static final String TAG = "RecipeListActivity";

    private RecipeListViewModel mRecipeListViewModel;
    private RecyclerView mRecyclerView;
    private RecipeRecyclerAdapter mAdapter;
    private SearchView mSearchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_list);
        mRecyclerView = findViewById(R.id.recipe_list);
        mSearchView = findViewById(R.id.search_view);

        mRecipeListViewModel = new ViewModelProvider(this).get(RecipeListViewModel.class);

        initRecyclerView();
        initSearchView();
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        subscribeObservers();
    }

    private void subscribeObservers() {
        mRecipeListViewModel.getRecipes().observe(this, listResource -> {
            if (listResource != null) {
                Log.d(TAG, "onChanged: status: " + listResource.status);

                if (listResource.data != null) {
                    switch (listResource.status) {
                        case LOADING: {
                            if (mRecipeListViewModel.getPageNumber() > 1) {
                                mAdapter.displayLoading();
                            } else {
                                mAdapter.displayOnlyLoading();
                            }
                            break;
                        }
                        case ERROR: {
                            Log.e(TAG, "onChanged: cannot refresh the cache.");
                            Log.e(TAG, "onChanged: ERROR message: " + listResource.message);
                            Log.e(TAG, "onChanged: status: ERROR, #recipes: " + listResource.data.size());
                            mAdapter.hideLoading();
                            mAdapter.setRecipes(listResource.data);  // we can still get the recipe from cache even if error occurs.
                            Toast.makeText(RecipeListActivity.this, listResource.message, Toast.LENGTH_LONG).show();

                            if (listResource.message.equals(mRecipeListViewModel.QUERY_EXHAUSTED)) {
                                Log.d(TAG, "RecipeListActivity: QUERY_EXHAUSTED");
                                mAdapter.setQueryExhausted();
                            }
                            break;
                        }
                        case SUCCESS: {
                            Log.d(TAG, "onChanged: cache has been refreshed");
                            Log.d(TAG, "onChanged: status: SUCCESS, #Recipes:" + listResource.data.size());
                            mAdapter.hideLoading();
                            mAdapter.setRecipes(listResource.data);
                            break;
                        }
                    }
                }

            }

        });

        mRecipeListViewModel.getViewstate().observe(this, new Observer<RecipeListViewModel.ViewState>() {
            @Override
            public void onChanged(@Nullable RecipeListViewModel.ViewState viewState) {
                if (viewState != null) {
                    switch (viewState) {
                        case RECIPES: {
                            // automatically show recipes from another observer.


                            break;
                        }
                        case CATEGORIES: {
                            displaySearchCategories();
                            break;
                        }

                    }
                }
            }
        });
    }

    private void displaySearchCategories() {
        mAdapter.displaySearchCategories();
    }

    private RequestManager initGlide() {
        RequestOptions options = new RequestOptions()
                .placeholder(R.drawable.white_background)
                .error(R.drawable.white_background);

        return Glide.with(this)
                .setDefaultRequestOptions(options);
    }

    private void searchRecipesApi(String query) {
        mRecyclerView.smoothScrollToPosition(0);
        mRecipeListViewModel.searchRecipesApi(query, 1);
        mSearchView.clearFocus();
    }

    private void initRecyclerView() {
        ViewPreloadSizeProvider<String> viewPreloadSizeProvider = new ViewPreloadSizeProvider<String>();
        mAdapter = new RecipeRecyclerAdapter(this, initGlide(), viewPreloadSizeProvider);
        VerticalSpacingItemDecorator itemDecorator = new VerticalSpacingItemDecorator(30);
        mRecyclerView.addItemDecoration(itemDecorator);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                if (!mRecyclerView.canScrollVertically(1)
                        && mRecipeListViewModel.getViewstate().getValue() == RecipeListViewModel.ViewState.RECIPES) {
                    mRecipeListViewModel.searchNextPage();
                }
            }
        });

        RecyclerViewPreloader<String> preloader = new RecyclerViewPreloader<String>
                        (initGlide(),
                        mAdapter,
                        viewPreloadSizeProvider,
                        30);

        mRecyclerView.addOnScrollListener(preloader);

        mRecyclerView.setAdapter(mAdapter);
    }

    private void initSearchView() {
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {

                searchRecipesApi(s);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });
    }

    @Override
    public void onRecipeClick(int position) {
        Intent intent = new Intent(this, RecipeActivity.class);
        intent.putExtra("recipe", mAdapter.getSelectedRecipe(position));
        startActivity(intent);
    }

    @Override
    public void onCategoryClick(String category) {
        searchRecipesApi(category);
    }

    @Override
    public void onBackPressed() {
        if (mRecipeListViewModel.getViewstate().getValue() == RecipeListViewModel.ViewState.RECIPES) {
            mRecipeListViewModel.cancelSearchRequest();
            mRecipeListViewModel.setViewCategories();
        } else {
            super.onBackPressed();
        }
    }
}

















