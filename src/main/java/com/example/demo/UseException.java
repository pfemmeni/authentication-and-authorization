package com.example.demo;

public class UseException extends Exception {
    Activity activity;
    UseExceptionType useExceptionType;

    public UseException(Activity activity, UseExceptionType useExceptionType) {
        super(activity + " failed because " + useExceptionType);
        this.activity = activity;
        this.useExceptionType = useExceptionType;

    }

    public Activity getActivity() {
        return activity;
    }

    public UseExceptionType getUserExceptionType() {
        return useExceptionType;
    }

}



