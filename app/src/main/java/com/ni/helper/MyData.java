package com.ni.helper;

import android.util.Log;

import com.google.gson.Gson;
import com.ni.model.BeforeAddResponse;

import java.util.ArrayList;
import java.util.List;

public class MyData {
    private static final String TAG = MyData.class.getSimpleName();
    private static MyData instance = new MyData();
    private List<BeforeAddResponse.Category> categoryList;
    private List<BeforeAddResponse.Subcategory> subCategoryList;

    public MyData() {
        this.categoryList = new ArrayList<>();
        this.subCategoryList = new ArrayList<>();
    }

    public static MyData getInstance() {
        /**
         * Synchronizing this method is not needed if you only use it in
         * Activity life-cycle methods, like onCreate(). But if you use
         * the singleton outside the UI-thread you must synchronize this
         * method (preferably using the Double-checked locking pattern).
         */
        return instance != null ? instance : (instance = new MyData());
    }

    public List<BeforeAddResponse.Category> getData() {
        return categoryList;
    }

    public void setData(List<BeforeAddResponse.Category> categoryList) {
        this.categoryList.clear();
        this.categoryList.addAll(categoryList);
    }

    public List<BeforeAddResponse.Subcategory> getSubcategoryList() {
        return subCategoryList;
    }

    public void setSelectedSubCategoryList(List<BeforeAddResponse.Subcategory> subCategoryList) {
        for (BeforeAddResponse.Subcategory subcategory : subCategoryList) {
            Log.i(TAG, "setSelectedSubCategoryList: " + new Gson().toJson(subcategory));
        }
        this.subCategoryList.clear();
        this.subCategoryList.addAll(subCategoryList);
    }
}