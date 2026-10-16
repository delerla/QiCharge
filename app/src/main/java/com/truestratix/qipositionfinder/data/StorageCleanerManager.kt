package com.truestratix.qipositionfinder.data

import android.content.Context
import android.os.Environment
import android.os.StatFs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

data class JunkCategoryItem(
    val id: String,
    val name: String,
    val description: String,
    val sizeBytes: Long,
    val fileCount: Int,
    val targetFiles: List<File> = emptyList(),
    val isDirectlyDeletable: Boolean = true
)

data class CleanerScanResult(
    val totalJunkBytes: Long = 0L,
    val totalFilesFound: Int = 0,
    val categories: List<JunkCategoryItem> = emptyList(),
    val totalStorageBytes: Long = 0L,
    val freeStorageBytes: Long = 0L
)

class StorageCleanerManager(private val context: Context) {

    suspend fun scanJunk(): CleanerScanResult = withContext(Dispatchers.IO) {
        val categories = mutableListOf<JunkCategoryItem>()

        // 1. Obsolete APK Installer Packages in Downloads & App Dirs
        val apkFiles = mutableListOf<File>()
        findFilesByExtension(
            roots = listOfNotNull(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),
                context.getExternalFilesDir(null)
            ),
            extensions = setOf("apk"),
            result = apkFiles
        )
        val apkSize = apkFiles.sumOf { it.length() }
        if (apkFiles.isNotEmpty()) {
            categories.add(
                JunkCategoryItem(
                    id = "apk",
                    name = "Obsolete APK Installers",
                    description = "Downloaded app installation packages taking up storage",
                    sizeBytes = apkSize,
                    fileCount = apkFiles.size,
                    targetFiles = apkFiles,
                    isDirectlyDeletable = true
                )
            )
        }

        // 2. Partial & Temporary Download Files (.tmp, .crdownload, .part, .download, .log)
        val tempFiles = mutableListOf<File>()
        findFilesByExtension(
            roots = listOfNotNull(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                context.getExternalFilesDir(null),
                context.cacheDir,
                context.externalCacheDir
            ),
            extensions = setOf("tmp", "crdownload", "part", "download", "log", "bak", "temp"),
            result = tempFiles
        )
        val tempSize = tempFiles.sumOf { it.length() }
        if (tempFiles.isNotEmpty()) {
            categories.add(
                JunkCategoryItem(
                    id = "temp",
                    name = "Temporary & Partial Downloads",
                    description = "Interrupted browser downloads and temporary residual files",
                    sizeBytes = tempSize,
                    fileCount = tempFiles.size,
                    targetFiles = tempFiles,
                    isDirectlyDeletable = true
                )
            )
        }

        // 3. Application Cache & Memory Buffers
        val cacheFiles = mutableListOf<File>()
        collectAllFiles(context.cacheDir, cacheFiles)
        context.externalCacheDir?.let { collectAllFiles(it, cacheFiles) }
        collectAllFiles(context.codeCacheDir, cacheFiles)
        val cacheSize = cacheFiles.sumOf { it.length() }
        categories.add(
            JunkCategoryItem(
                id = "cache",
                name = "App Cache & Decoded Buffers",
                description = "Volatile image buffers, network response caches, and bytecode JIT traces",
                sizeBytes = cacheSize,
                fileCount = cacheFiles.size,
                targetFiles = cacheFiles,
                isDirectlyDeletable = true
            )
        )

        // 4. Empty Residual Directories
        val emptyDirs = mutableListOf<File>()
        findEmptyDirectories(
            roots = listOfNotNull(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                context.getExternalFilesDir(null)
            ),
            result = emptyDirs
        )
        if (emptyDirs.isNotEmpty()) {
            categories.add(
                JunkCategoryItem(
                    id = "empty_dirs",
                    name = "Empty Residual Directories",
                    description = "Orphaned empty folder trees left behind by removed tasks",
                    sizeBytes = 0L,
                    fileCount = emptyDirs.size,
                    targetFiles = emptyDirs,
                    isDirectlyDeletable = true
                )
            )
        }

        // Calculate real device storage statistics
        val stat = StatFs(Environment.getDataDirectory().path)
        val blockSize = stat.blockSizeLong
        val totalStorageBytes = stat.blockCountLong * blockSize
        val freeStorageBytes = stat.availableBlocksLong * blockSize

        val totalJunk = categories.sumOf { it.sizeBytes }
        val totalFiles = categories.sumOf { it.fileCount }

        CleanerScanResult(
            totalJunkBytes = totalJunk,
            totalFilesFound = totalFiles,
            categories = categories,
            totalStorageBytes = totalStorageBytes,
            freeStorageBytes = freeStorageBytes
        )
    }

    suspend fun cleanCategory(category: JunkCategoryItem): Long = withContext(Dispatchers.IO) {
        var freed = 0L
        for (file in category.targetFiles) {
            try {
                if (file.isDirectory) {
                    freed += deleteDirectoryRecursively(file)
                } else if (file.exists()) {
                    val len = file.length()
                    if (file.delete()) {
                        freed += len
                    }
                }
            } catch (_: Exception) {}
        }
        freed
    }

    suspend fun cleanAll(categories: List<JunkCategoryItem>): Long = withContext(Dispatchers.IO) {
        var totalFreed = 0L
        for (cat in categories) {
            if (cat.isDirectlyDeletable) {
                totalFreed += cleanCategory(cat)
            }
        }
        totalFreed
    }

    private fun findFilesByExtension(roots: List<File>, extensions: Set<String>, result: MutableList<File>) {
        for (root in roots) {
            if (!root.exists() || !root.canRead()) continue
            scanFilesRecursively(root, extensions, result, depth = 0)
        }
    }

    private fun scanFilesRecursively(dir: File, extensions: Set<String>, result: MutableList<File>, depth: Int) {
        if (depth > 4) return // Guard against circular or excessively deep links
        val files = dir.listFiles() ?: return
        for (f in files) {
            if (f.isDirectory) {
                scanFilesRecursively(f, extensions, result, depth + 1)
            } else {
                val ext = f.extension.lowercase()
                if (extensions.contains(ext)) {
                    result.add(f)
                }
            }
        }
    }

    private fun collectAllFiles(dir: File?, result: MutableList<File>) {
        if (dir == null || !dir.exists()) return
        val files = dir.listFiles() ?: return
        for (f in files) {
            if (f.isDirectory) {
                collectAllFiles(f, result)
            } else {
                result.add(f)
            }
        }
    }

    private fun findEmptyDirectories(roots: List<File>, result: MutableList<File>) {
        for (root in roots) {
            if (!root.exists() || !root.canRead()) continue
            val children = root.listFiles() ?: continue
            for (c in children) {
                if (c.isDirectory && (c.list() == null || c.list()?.isEmpty() == true)) {
                    result.add(c)
                }
            }
        }
    }

    private fun deleteDirectoryRecursively(dir: File): Long {
        var deleted = 0L
        val children = dir.listFiles() ?: return 0L
        for (c in children) {
            if (c.isDirectory) {
                deleted += deleteDirectoryRecursively(c)
            } else {
                val len = c.length()
                if (c.delete()) {
                    deleted += len
                }
            }
        }
        dir.delete()
        return deleted
    }
}
