package com.tny.khonde;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.ProgressBar;

/**
 * Created by Thitiphat on 5/11/2016.
 */
public class AutoCompleteLoading extends AutoCompleteTextView {

    private ProgressBar mLoadingIndicator;

    public AutoCompleteLoading(Context context, AttributeSet attrs) {
        super(context,attrs);
    }

    public void setLoadingIndicator(ProgressBar view) {
        mLoadingIndicator = view;
    }

    @Override
    protected void performFiltering(CharSequence text, int keyCode) {
        // the AutoCompleteTextview is about to start the filtering so show
        // the ProgressPager
        mLoadingIndicator.setVisibility(View.VISIBLE);
        super.performFiltering(text, keyCode);
    }

    @Override
    public void onFilterComplete(int count) {
        // the AutoCompleteTextView has done its job and it's about to show
        // the drop down so close/hide the ProgreeBar
        mLoadingIndicator.setVisibility(View.INVISIBLE);
        super.onFilterComplete(count);
    }

}