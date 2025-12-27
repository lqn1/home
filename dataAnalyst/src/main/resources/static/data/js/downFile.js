// 文件下载功能
class FileDownloader {
    constructor() {
        this.downloadQueue = [];
        this.isDownloading = false;
    }

    /**
     * 下载单个文件
     */
    async downloadFile(fileId, fileName, options = {}) {
        const {
            showProgress = true,
            recordAccess = true,
            preview = false
        } = options;

        try {
            // 显示下载状态
            this.showDownloadStatus('准备下载...');

            // 验证文件是否存在
            const fileInfo = await this.getFileInfo(fileId);
            if (!fileInfo) {
                throw new Error('文件不存在');
            }


            // 开始下载
            const downloadUrl = preview ? '/ncy/preview' : '/ncy/download';

            const response = await fetch(downloadUrl, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({ fileId: fileId })
            });

            if (!response.ok) {
                throw new Error(`下载失败: ${response.status} ${response.statusText}`);
            }

            // 获取文件名
            const contentDisposition = response.headers.get('Content-Disposition');
            const finalFileName = this.extractFileName(contentDisposition) || fileName || `file_${fileId}.xlsx`;

            // 处理大文件下载（显示进度）
            const contentLength = response.headers.get('Content-Length');
            const total = parseInt(contentLength, 10);

            if (showProgress && total > 0) {
                return await this.downloadWithProgress(response, finalFileName, total);
            } else {
                return await this.downloadDirect(response, finalFileName);
            }

        } catch (error) {
            console.error('文件下载失败:', error);
            this.showDownloadError(error.message);
            throw error;
        }
    }

    /**
     * 带进度显示的下载
     */
    async downloadWithProgress(response, fileName, totalSize) {
        const reader = response.body.getReader();
        let receivedLength = 0;
        const chunks = [];

        // 显示进度条
        this.showProgressBar(totalSize);

        while (true) {
            const { done, value } = await reader.read();

            if (done) {
                break;
            }

            chunks.push(value);
            receivedLength += value.length;

            // 更新进度
            this.updateProgressBar(receivedLength, totalSize);
        }

        // 合并chunks
        const blob = new Blob(chunks);
        this.downloadBlob(blob, fileName);

        // 隐藏进度条
        this.hideProgressBar();
        this.showDownloadSuccess(fileName);

        return { success: true, fileName, size: receivedLength };
    }

    /**
     * 直接下载
     */
    async downloadDirect(response, fileName) {
        const blob = await response.blob();
        this.downloadBlob(blob, fileName);
        this.showDownloadSuccess(fileName);

        return { success: true, fileName, size: blob.size };
    }

    /**
     * 下载Blob文件
     */
    downloadBlob(blob, fileName) {
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = fileName;
        a.style.display = 'none';

        document.body.appendChild(a);
        a.click();

        // 清理
        setTimeout(() => {
            document.body.removeChild(a);
            window.URL.revokeObjectURL(url);
        }, 100);
    }

    /**
     * 批量下载文件
     */
    async downloadMultipleFiles(directoryId,directoryName, options = {}) {
        const {
            zip = true,
            showProgress = true
        } = options;

        // if (files.length === 0) {
        //     throw new Error('没有选择文件');
        // }
        //
        // if (files.length === 1 && !zip) {
        //     // 单个文件直接下载
        //     const file = files[0];
        //     return await this.downloadFile(file.id, file.name, options);
        // }

        if (zip) {
            // 打包下载
            return await this.downloadAsZip(directoryId,directoryName,options);
        } else {
            // 逐个下载
            return await this.downloadSequentially(files, options);
        }
    }

    /**
     * 打包下载
     */
    async downloadAsZip(directoryId,directoryName, options) {
        try {
            this.showDownloadStatus(`正在打包文件...`);

            const response = await fetch('/ncy/downloadDirectory', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({
                    directoryId: directoryId, // 假设文件在同一目录
                    directoryName: directoryName
                })
            });

            if (!response.ok) {
                throw new Error('打包下载失败');
            }

            const blob = await response.blob();
            const zipName = `files_${Date.now()}.zip`;

            this.downloadBlob(blob, zipName);
            this.showDownloadSuccess(`正在下载文件`);

            return { success: true, fileName: zipName };

        } catch (error) {
            console.error('打包下载失败:', error);
            this.showDownloadError('打包下载失败，尝试逐个下载');

            // 失败后尝试逐个下载
            // return await this.downloadSequentially(files, options);
        }
    }

    /**
     * 逐个顺序下载
     */
    async downloadSequentially(files, options) {
        const results = [];

        for (let i = 0; i < files.length; i++) {
            const file = files[i];

            try {
                this.showDownloadStatus(`正在下载 (${i + 1}/${files.length}): ${file.name}`);

                const result = await this.downloadFile(file.id, file.name, {
                    ...options,
                    showProgress: false // 批量下载时不显示单个进度
                });

                results.push({ ...result, success: true });

                // 延迟一下，避免同时下载太多文件
                if (i < files.length - 1) {
                    await this.delay(1000);
                }

            } catch (error) {
                results.push({
                    success: false,
                    fileName: file.name,
                    error: error.message
                });
            }
        }

        this.showBatchDownloadResult(results);
        return results;
    }

    /**
     * 文件预览
     */
    async previewFile(fileId) {
        try {
            const result = await this.downloadFile(fileId, null, {
                preview: true,
                showProgress: false
            });

            // 在新窗口打开预览
            window.open(`/api/file/preview?fileId=${fileId}`, '_blank');

            return result;
        } catch (error) {
            console.error('文件预览失败:', error);
            this.showDownloadError('预览失败: ' + error.message);
            throw error;
        }
    }

    // 辅助方法
    async getFileInfo(fileId) {
        try {
            const response = await fetch('/ncy/getDirectoryInfoById/', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({ fileId: fileId })
            });

            if (response.ok) {
                return await response.json();
            }
            return null;
        } catch (error) {
            console.error('获取文件信息失败:', error);
            return null;
        }
    }



    extractFileName(contentDisposition) {
        if (!contentDisposition) return null;

        const filenameMatch = contentDisposition.match(/filename[^;=\n]*=((['"]).*?\2|[^;\n]*)/);
        if (filenameMatch && filenameMatch[1]) {
            return filenameMatch[1].replace(/['"]/g, '');
        }
        return null;
    }

    delay(ms) {
        return new Promise(resolve => setTimeout(resolve, ms));
    }

    // UI相关方法
    showDownloadStatus(message) {
        this.updateStatusMessage(message);
    }

    showDownloadSuccess(fileName) {
        this.showNotification(`"${fileName}" 下载成功`, 'success');
    }

    showDownloadError(message) {
        this.showNotification(`下载失败: ${message}`, 'error');
    }

    showProgressBar(totalSize) {
        // 创建或显示进度条
        let progressBar = document.getElementById('download-progress-bar');
        if (!progressBar) {
            progressBar = this.createProgressBar();
        }
        progressBar.style.display = 'block';
    }

    updateProgressBar(current, total) {
        const progressBar = document.getElementById('download-progress');
        const percent = total > 0 ? Math.round((current / total) * 100) : 0;

        if (progressBar) {
            progressBar.style.width = percent + '%';
        }

        this.updateStatusMessage(`下载中... ${this.formatFileSize(current)} / ${this.formatFileSize(total)} (${percent}%)`);
    }

    hideProgressBar() {
        const progressBar = document.getElementById('download-progress-bar');
        if (progressBar) {
            progressBar.style.display = 'none';
        }
    }

    createProgressBar() {
        const container = document.createElement('div');
        container.id = 'download-progress-bar';
        container.innerHTML = `
            <div class="progress-container">
                <div class="progress-bar">
                    <div id="download-progress" class="progress"></div>
                </div>
            </div>
        `;
        document.body.appendChild(container);
        return container;
    }

    updateStatusMessage(message) {
        // 更新状态显示
        console.log('下载状态:', message); // 可以替换为实际的UI更新
    }

    showNotification(message, type) {
        // 显示通知
        console.log(`${type}: ${message}`); // 可以替换为实际的通知组件
    }

    showBatchDownloadResult(results) {
        const successCount = results.filter(r => r.success).length;
        const failCount = results.length - successCount;

        if (failCount === 0) {
            this.showNotification(`批量下载完成，成功 ${successCount} 个文件`, 'success');
        } else {
            this.showNotification(`批量下载完成，成功 ${successCount} 个，失败 ${failCount} 个文件`, 'warning');
        }
    }

    formatFileSize(bytes) {
        if (bytes === 0) return '0 B';

        const k = 1024;
        const sizes = ['B', 'KB', 'MB', 'GB'];
        const i = Math.floor(Math.log(bytes) / Math.log(k));

        return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
    }
}

// 全局下载器实例
const fileDownloader = new FileDownloader();

// 在现有的JavaScript代码中替换下载函数
function downloadExcel(fileId, fileName) {
    fileDownloader.downloadFile(fileId, fileName)
        .catch(error => {
            // 错误已经在downloadFile中处理
        });
}

function previewFile(fileId) {
    fileDownloader.previewFile(fileId)
        .catch(error => {
            // 错误处理
        });
}

function downloadMultipleFiles(directoryId,directoryName,) {
    fileDownloader.downloadMultipleFiles(directoryId,directoryName, { zip: true })
        .catch(error => {
            alert('下载失败: ' + error.message);
        });
}