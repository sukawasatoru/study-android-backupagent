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

import android.os.Bundle
import androidx.activity.ComponentActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        log("[MainActivity][onCreate]")
        super.onCreate(savedInstanceState)
    }

    override fun onDestroy() {
        log("[MainActivity][onDestroy]")
        super.onDestroy()
    }

    override fun onStart() {
        log("[MainActivity][onStart]")
        super.onStart()
    }

    override fun onStop() {
        log("[MainActivity][onStop]")
        super.onStop()
    }

    override fun onResume() {
        log("[MainActivity][onResume]")
        super.onResume()
    }

    override fun onPause() {
        log("[MainActivity][onPause]")
        super.onPause()
    }
}
