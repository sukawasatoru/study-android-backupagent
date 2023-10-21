/*
 * Copyright 2023 sukawasatoru
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.study.backupagent

import android.app.Application
import android.util.Log
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope

fun log(msg: String) {
    Log.i("StudyBackupAgent", msg)
}

@HiltAndroidApp
class App : Application() {
    @AppCoroutineScope
    @Inject
    lateinit var appCoroutineScope: CoroutineScope

    override fun onCreate() {
        log("[App][onCreate]")

        super.onCreate()

        log("[App][onCreate] inject by hilt: $appCoroutineScope")
    }
}
