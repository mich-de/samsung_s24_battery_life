package com.s24optimizer

import android.app.Application

import com.s24optimizer.exec.AdbExecutor

class S24OptimizerApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize AdbExecutor early
        AdbExecutor.instance
    }
}
