package com.codingwithmitch.foodrecipes.viewmodels;


import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.annotation.NonNull;
import androidx.lifecycle.Observer;

import com.codingwithmitch.foodrecipes.models.Recipe;
import com.codingwithmitch.foodrecipes.repositories.RecipeRepository;
import com.codingwithmitch.foodrecipes.requests.responses.ApiResponse;
import com.codingwithmitch.foodrecipes.util.Resource;

import java.util.List;

public class RecipeListViewModel extends AndroidViewModel {

    private static final String TAG = "RecipeListViewModel";

    public static final String QUERY_EXHAUSTED = "NO more results.";
    public enum ViewState {CATEGORIES, RECIPES};

    private MutableLiveData<ViewState> viewState;
    private MediatorLiveData<Resource<List<Recipe>>> recipes = new MediatorLiveData<>();
    private RecipeRepository recipeRepository;

    // query extras
    private boolean isQueryExhausted;
    private boolean isPerformingQuery;
    private int pageNumber;
    private String query;

    public RecipeListViewModel(@NonNull Application application) {
        super(application);
        recipeRepository = RecipeRepository.getInstance(application);
        init();

    }

    private void init() {
        if(viewState == null) {
            viewState = new MutableLiveData<>();
            viewState.setValue(ViewState.CATEGORIES);
        }
    }

    public LiveData<Resource<List<Recipe>>> getRecipes(){
        return recipes;
    }

    public LiveData<ViewState> getViewState(){
        return viewState;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public void setViewCategories(){
        viewState.setValue(ViewState.CATEGORIES);
    }

    public String getQuery() {
        return query;
    }

    public void searchRecipeApi(String query, int pageNumber) {
        if(!isPerformingQuery) {
            if(pageNumber == 0) {
                pageNumber = 1;
            }
            this.pageNumber = pageNumber;
            this.query = query;
            executeSearch();
        }
    }

    public void searchNextPage(){
        if(!isQueryExhausted && !isPerformingQuery) {
            pageNumber++;
            executeSearch();
        }
    }

    public void executeSearch(){
        isPerformingQuery = true;
        viewState.setValue(ViewState.RECIPES);

        final LiveData<Resource<List<Recipe>>> repositoryResource = recipeRepository.searchRecipesApi(query, pageNumber);
        recipes.addSource(repositoryResource, new Observer<Resource<List<Recipe>>>() {
            @Override
            public void onChanged(Resource<List<Recipe>> listResource) {

                if(listResource != null) {
                    recipes.setValue(listResource);
                    if(listResource.status == Resource.Status.SUCCESS) {
                        isPerformingQuery = false;
                        if(listResource.data != null) {
                            if(listResource.data.size() == 0) {
                                recipes.setValue(
                                        new Resource<List<Recipe>>(
                                                Resource.Status.ERROR,
                                                null,
                                                QUERY_EXHAUSTED
                                        )
                                );
                            }
                        }
                     recipes.removeSource(repositoryResource);
                    }
                    else if(listResource.status == Resource.Status.ERROR) {
                        isPerformingQuery = false;
                        recipes.removeSource(repositoryResource);
                    }

                    // In case of LoadingStatus, not doing anything.

                }
                else {
                    recipes.removeSource(repositoryResource);
                }
            }
        });
    }
}















