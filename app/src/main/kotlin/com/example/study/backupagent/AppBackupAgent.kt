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

import android.app.backup.BackupAgent
import android.app.backup.BackupDataInput
import android.app.backup.BackupDataOutput
import android.app.backup.FullBackupDataOutput
import android.os.ParcelFileDescriptor
import java.io.BufferedInputStream
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStreamReader
import java.io.PipedInputStream
import java.io.PipedOutputStream
import java.nio.file.Files
import java.nio.file.Paths
import java.time.Instant
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import suspendRunCatching

class AppBackupAgent : BackupAgent() {
    private lateinit var scope: CoroutineScope
    private lateinit var dispatcherIO: CoroutineDispatcher

    /**
     * Auto Backup uses [android.app.Application] instead of the
     * [com.example.study.backupagent.App], so this class cannot be used
     * [dagger.hilt.android.EntryPointAccessors].
     *
     * Must be used the Dagger if use DI of hilt system.
     */
    override fun onCreate() {
        log("[AppBackupAgent][onCreate] Application: ${applicationContext::class.qualifiedName}")

        with(DaggerAppComponent.create()) {
            scope = appCoroutineScope()
            dispatcherIO = dispatcherIO()
        }
    }

    override fun onDestroy() {
        log("[AppBackupAgent][onDestroy]")
    }

    @Throws(IOException::class)
    override fun onBackup(
        oldState: ParcelFileDescriptor,
        data: BackupDataOutput,
        newState: ParcelFileDescriptor,
    ) = Unit

    @Throws(IOException::class)
    override fun onRestore(
        data: BackupDataInput,
        appVersionCode: Int,
        newState: ParcelFileDescriptor,
    ) = Unit

    @Throws(IOException::class)
    override fun onFullBackup(data: FullBackupDataOutput) {
        log("[AppBackupAgent][onFullBackup]")
        val tmpFilePath = Paths.get(filesDir.toPath().toString(), "backup-tmp", "tmpfile")
        try {
            log("[AppBackupAgent][onFullBackup] create dir")
            Files.createDirectories(tmpFilePath.parent)

            log("[AppBackupAgent][onFullBackup] write hello")
            val writer = Files.newBufferedWriter(tmpFilePath)
            writer.appendLine("Hello: ${Instant.now()}")
            writer.appendLine("Hello2: ${Instant.now()}")
            writer.appendLine("Hello3: ${Instant.now()}")
            writer.appendLine()
            writer.appendLine("Hello4: ${Instant.now()}")

            log("[AppBackupAgent][onFullBackup] flush")
            writer.flush()

            log("[AppBackupAgent][onFullBackup] invoke fullBackupFile")
            fullBackupFile(tmpFilePath.toFile(), data)
        } catch (e: Exception) {
            log("[AppBackupAgent][onFullBackup] exception: $e")
        } finally {
            try {
                log("[AppBackupAgent][onFullBackup] delete file")
                Files.deleteIfExists(tmpFilePath)

                log("[AppBackupAgent][onFullBackup] delete dir")
                Files.delete(tmpFilePath.parent)
            } catch (e: Exception) {
                log("[AppBackupAgent][onFullBackup] finally exception: $e")
            }
        }
    }

    override fun onQuotaExceeded(backupDataBytes: Long, quotaBytes: Long) {
        log(
            "[AppBackupAgent][onQuotaExceeded] backupDataBytes: $backupDataBytes" +
                    ", quotaBytes: $quotaBytes"
        )
    }

    @Throws(IOException::class)
    override fun onRestoreFile(
        data: ParcelFileDescriptor,
        size: Long,
        destination: File,
        type: Int,
        mode: Long,
        mtime: Long,
    ) {
        log(
            "[AppBackupAgent][onRestoreFile] size: $size" +
                    ", destination: $destination, type: $type, mode: $mode, mtime: $mtime" +
                    ", thread: ${Thread.currentThread()}"
        )

        val pipeErrorFlow = MutableStateFlow<Throwable?>(null)
        val ist = PipedInputStream()
        val ost = PipedOutputStream(ist)
        scope.launch(dispatcherIO) {
            log("[AppBackupAgent][onRestoreFile] launch pipe")
            suspendRunCatching {
                BufferedInputStream(FileInputStream(data.fileDescriptor)).use { fileIstream ->
                    val buf = ByteArray(8192)
                    var remain = size

                    while (0 < remain) {
                        val nRead = fileIstream.read(buf, 0, buf.size)
                        ost.write(buf, 0, nRead)
                        if (nRead == -1) {
                            log("[AppBackupAgent][onRestoreFile] pipe EOF")
                            break
                        }
                        remain -= nRead
                        log("[AppBackupAgent][onRestoreFile] pipe yield")
                    }
                    log("[AppBackupAgent][onRestoreFile] pipe close")
                    ost.flush()
                    ost.close()
                }
            }.getOrElse { e ->
                log("[AppBackupAgent][onRestoreFile] pipe error caused: $e")
                pipeErrorFlow.value = e
            }
        }

        log("[AppBackupAgent][onRestoreFile] launch reader")
        BufferedReader(InputStreamReader(ist)).use { reader ->
            for (line in reader.lines()) {
                log("[AppBackupAgent][onRestoreFile] line: $line")
            }
        }

        pipeErrorFlow.value?.let { e ->
            log("[AppBackupAgent][onRestoreFile] throw pipe error: $e")
            throw e
        }
    }

    override fun onRestoreFinished() {
        log("[AppBackupAgent][onRestoreFinished]")
    }
}
