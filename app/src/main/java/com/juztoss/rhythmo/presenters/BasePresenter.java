package com.juztoss.rhythmo.presenters;

/**
 * Created by JuzTosS on 4/25/2016.
 */
public class BasePresenter {

    private RhythmoApp mApp;

    private BasePresenter() {
    }

    public BasePresenter(RhythmoApp app) {
        mApp = app;
    }

    public RhythmoApp getApp() {
        return mApp;
    }
}
