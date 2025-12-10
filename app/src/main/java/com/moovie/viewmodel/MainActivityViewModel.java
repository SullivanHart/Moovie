/**
 * Copyright 2017 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 package com.moovie.viewmodel;

import androidx.lifecycle.ViewModel;

import com.moovie.Filters;

/**
 * ViewModel for {@link com.moovie.MainActivity}.
 */

public class MainActivityViewModel extends ViewModel {

    private boolean mIsSigningIn;
    private Filters mFilters;

    /**
     * Constructor for MainActivityViewModel.
     */
    public MainActivityViewModel() {
        mIsSigningIn = false;
        mFilters = Filters.getDefault();
    }

    /**
     * Checks if the user is currently signing in.
     * @return true if signing in, false otherwise.
     */
    public boolean getIsSigningIn() {
        return mIsSigningIn;
    }

    /**
     * Sets the signing in state.
     * @param mIsSigningIn The new signing in state.
     */
    public void setIsSigningIn(boolean mIsSigningIn) {
        this.mIsSigningIn = mIsSigningIn;
    }

    /**
     * Gets the current filters.
     * @return The current Filters object.
     */
    public Filters getFilters() {
        return mFilters;
    }

    /**
     * Sets the filters.
     * @param mFilters The new Filters object.
     */
    public void setFilters(Filters mFilters) {
        this.mFilters = mFilters;
    }
}
