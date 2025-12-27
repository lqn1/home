// 当前选中的文件夹ID
let currentFolderId = null;
// 面包屑导航数据
let breadcrumbData = [{id: null, name: '首页'}];
// 存储导航历史记录
let navigationHistory = [];

// DOM元素
const sidebar = document.querySelector('.sidebar');
const toggleSidebarBtn = document.getElementById('toggleSidebar');
const folderList = document.getElementById('folderList');
const contentTitle = document.getElementById('contentTitle');
const contentBody = document.getElementById('contentBody');
const breadcrumb = document.getElementById('breadcrumb');
const backBtn = document.getElementById('backBtn');
const addFolderBtn = document.getElementById('addFolderBtn');
const addRootFolderBtn = document.getElementById('addRootFolder');
const importExcelBtn = document.getElementById('importExcelBtn');
const folderModal = document.getElementById('folderModal');
const excelModal = document.getElementById('excelModal');

// 初始化页面
document.addEventListener('DOMContentLoaded', function() {
    // 加载左侧导航数据
    loadFolders();

    // 绑定事件
    toggleSidebarBtn.addEventListener('click', toggleSidebar);

    addFolderBtn.addEventListener('click', () => showFolderModal('current'));
    addRootFolderBtn.addEventListener('click', () => showFolderModal('root'));

    importExcelBtn.addEventListener('click', () => showModal(excelModal));
    backBtn.addEventListener('click', goBack);

    // 关闭模态框
    document.querySelectorAll('.close-btn, #cancelFolderBtn, #cancelExcelBtn').forEach(btn => {
        btn.addEventListener('click', () => {
            folderModal.style.display = 'none';
            excelModal.style.display = 'none';
        });
    });


    // 保存文件夹
    document.getElementById('saveFolderBtn').addEventListener('click', saveFolder);

    // 导入Excel
    document.getElementById('saveExcelBtn').addEventListener('click', importExcel);

    // 点击模态框外部关闭
    window.addEventListener('click', (e) => {
        if (e.target === folderModal) folderModal.style.display = 'none';
        if (e.target === excelModal) excelModal.style.display = 'none';
    });
});

// 显示文件夹模态框，并设置父级ID
function showFolderModal(parentType) {
    // 设置隐藏字段标识父级类型
    document.getElementById('folderModal').setAttribute('data-parent-type', parentType);
    showModal(folderModal);
}

// 切换侧边栏显示/隐藏
function toggleSidebar() {
    sidebar.classList.toggle('collapsed');
}

    // 加载文件夹列表
function loadFolders() {
    // 显示加载状态
    folderList.innerHTML = '<div class="loading"><i class="fas fa-spinner"></i><p>加载中...</p></div>';

    // AJAX请求获取文件夹数据
    fetch('/ncy/topDirectory', {
        method: 'POST',
        headers: {
        'Content-Type': 'application/json'
    }
    })
    .then(response => {
    if (!response.ok) {
    throw new Error('网络响应不正常');
}
    return response.json();
})
    .then(data => {
    renderFolderList(data);

    // 页面加载完成后自动选择第一个目录
    if (data.length > 0) {
    // 模拟点击第一个文件夹
    setTimeout(() => {
    const firstFolder = document.querySelector('.folder-item');
    if (firstFolder) {
    firstFolder.click();
}
}, 100);
}
})
    .catch(error => {
    console.error('加载文件夹列表失败:', error);
    folderList.innerHTML = '<div class="error-state"><i class="fas fa-exclamation-circle"></i><p>加载失败，请重试</p></div>';
});
}

    // 渲染文件夹列表
    function renderFolderList(folders) {
    folderList.innerHTML = '';

    if (folders.length === 0) {
    folderList.innerHTML = '<div class="empty-state"><i class="fas fa-folder-open"></i><p>暂无文件夹</p></div>';
    return;
}

    folders.forEach(folder => {
    const li = document.createElement('li');
    li.className = 'folder-item';
    li.innerHTML = `
                <i class="fas ${folder.icon} folder-icon"></i>
                <span class="folder-name">${folder.translate}</span>
            `;
    li.addEventListener('click', () => {
    // 移除所有active类
    document.querySelectorAll('.folder-item').forEach(item => {
    item.classList.remove('active');
});
    // 添加active类到当前项
    li.classList.add('active');
    // 加载文件夹内容
    loadFolderContent(folder.id, folder.translate, folder.fatherid);
});
    folderList.appendChild(li);
});
}

// 加载文件夹内容
function loadFolderContent(folderId, folderName, parentId = null) {
    // 记录导航历史
    if (currentFolderId !== null) {
        navigationHistory.push({
            id: currentFolderId,
            name: contentTitle.textContent,
            parentId: parentId
        });
    }

    currentFolderId = folderId;

    // 更新面包屑
    updateBreadcrumb(folderId, folderName, parentId);

    // 更新内容标题
    contentTitle.textContent = folderName;

    // 显示加载状态
    contentBody.innerHTML = '<div class="loading"><i class="fas fa-spinner"></i><p>加载中...</p></div>';

    // 更新返回按钮显示状态
    updateBackButton();

    // AJAX请求获取文件夹内容
    fetch('/ncy/nextDirectory/contents', {
    method: 'POST',
    headers: {
    'Content-Type': 'application/json'
},
    body: JSON.stringify({ folderId: folderId })
})
    .then(response => {
    if (!response.ok) {
    throw new Error('网络响应不正常');
}
    return response.json();
})
    .then(contents => {
    renderFolderContents(contents);
})
    .catch(error => {
    console.error('加载文件夹内容失败:', error);
    contentBody.innerHTML = '<div class="error-state"><i class="fas fa-exclamation-circle"></i><p>加载失败，请重试</p></div>';
});
}

// 渲染文件夹内容
function renderFolderContents(contents) {
    if (contents.length === 0) {
        contentBody.innerHTML = `
                    <div class="empty-state">
                        <i class="fas fa-folder-open"></i>
                        <h3>文件夹为空</h3>
                        <p>请添加文件或文件夹</p>
                    </div>
                `;
        return;
    }

    let html = '<div class="file-grid">';
    contents.forEach(item => {
        const iconClass = item.icon === 'fa-folder' ? 'fa-folder' : 'fa-file-excel';
        html += `
                <div class="file-item" data-id="${item.id}" data-type="${item.icon === 'fa-folder' ? 'folder' : 'file'}" data-fatherid="${item.fatherid}">
                    <i class="fas ${iconClass} file-icon"></i>
                    <div class="file-name">${item.translate}</div>
                </div>
            `;
    });
    html += '</div>';

    contentBody.innerHTML = html;

    // 绑定文件项点击事件
    document.querySelectorAll('.file-item').forEach(item => {
        item.addEventListener('click', function() {
            const id = this.getAttribute('data-id');
            const type = this.getAttribute('data-type');
            const fatherid = this.getAttribute('data-fatherid');

            if (type === 'folder') {
                // 如果是文件夹，通过AJAX实时查询加载其内容
                const folderName = this.querySelector('.file-name').textContent;
                loadFolderContent(parseInt(id), folderName, fatherid ? parseInt(fatherid) : null);
            } else {
                // 如果是文件，打开文件（扩展功能）
                openFile(id);
            }
        });
    });
}

// 更新面包屑导航
function updateBreadcrumb(folderId, folderName, parentId = null) {
    // 如果是根目录，重置面包屑
    if (folderId === null) {
        breadcrumbData = [{id: null, name: '首页'}];
    } else {
        // 构建面包屑路径
        breadcrumbData = buildBreadcrumbPath(folderId, folderName, parentId);
    }

    let html = '';

    // 添加返回按钮（非首页时显示）
    if (folderId !== null) {
    html += `<button class="back-btn" id="backBtn">
                <i class="fas fa-arrow-left"></i>返回上级
            </button>`;
}

    breadcrumbData.forEach((item, index) => {
    if (index > 0) {
    html += `<div class="breadcrumb-separator"><i class="fas fa-chevron-right"></i></div>`;
}

    if (index === breadcrumbData.length - 1) {
    // 最后一项不可点击
    html += `<div class="breadcrumb-item">${item.name}</div>`;
} else {
    html += `<div class="breadcrumb-item"><a href="#" data-id="${item.id}">${item.name}</a></div>`;
}
});

    breadcrumb.innerHTML = html;

    // 重新绑定返回按钮事件
    const newBackBtn = document.getElementById('backBtn');
    if (newBackBtn) {
    newBackBtn.addEventListener('click', goBack);
}

    // 绑定面包屑点击事件
    breadcrumb.querySelectorAll('a').forEach(link => {
    link.addEventListener('click', function(e) {
    e.preventDefault();
    const id = this.getAttribute('data-id');
    if (id === 'null') {
    // 返回首页
    goToHome();
} else {
    // 通过AJAX查询指定文件夹
    loadFolderById(parseInt(id));
}
});
});
}

// 通过ID加载文件夹
function loadFolderById(folderId) {
    // 显示加载状态
    contentBody.innerHTML = '<div class="loading"><i class="fas fa-spinner"></i><p>加载中...</p></div>';
    const folder = findFolderInfo(folderId);
    loadFolderContent(folder.id, folder.translate, folder.fatherid);
}

// 构建面包屑路径
function buildBreadcrumbPath(folderId, folderName, parentId) {
    const path = [];

    // 添加当前文件夹
    path.unshift({id: folderId, name: folderName});

    // 递归查找父级文件夹
    let currentParentId = parentId;
    while (currentParentId !== null) {
        // 通过AJAX实时查询父级文件夹信息
        const parentFolder = findFolderInfo(currentParentId);

        if (parentFolder && parentFolder.id) {
            path.unshift({id: parentFolder.id, name: parentFolder.translate});
            currentParentId = parentFolder.fatherid;
        } else {
            break;
        }
    }

    // 添加首页
    path.unshift({id: null, name: '首页'});

    return path;
}

function findFolderInfo(folderId) {
    let result = [];
    $.ajax({
        url: '/ncy/getDirectoryInfoById/',
        type: 'POST',
        contentType: 'application/json',
        data: JSON.stringify({folderId: folderId}),
        dataType: 'json',
        timeout: 10000,
        async: false,
        success: function(response) {
            if (!Array.isArray(response) || response.length === 0) {
                console.error('返回的数据不是数组格式或为空');
            }else{
                console.log('获取文件夹信息成功:', response[0]);
                result = response[0];
            }

        },
        error: function(xhr, status, error) {
            console.error('AJAX请求失败:', error);
            return null;
        }
    });
    return result;
}

// 返回上级目录
function goBack() {
    const folder = findFolderInfo(currentFolderId);
    if (!folder.fatherid) {
       goToHome();
        return;
    } else {
        const fatherFolder = findFolderInfo(folder.fatherid);
        // 显示加载状态
        contentBody.innerHTML = '<div class="loading"><i class="fas fa-spinner"></i><p>加载中...</p></div>';

        // 使用AJAX请求获取上级目录内容
        fetch('/ncy/nextDirectory/contents', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ folderId: folder.fatherid })
        })
        .then(response => {
            if (!response.ok) {
                throw new Error('网络响应不正常');
            }
            return response.json();
        })
        .then(contents => {
            // 更新当前文件夹ID
            currentFolderId = fatherFolder.id;
            loadFolderContent(fatherFolder.id,fatherFolder.translate,fatherFolder.fatherid);

            // 更新返回按钮显示状态
            updateBackButton();
        })
        .catch(error => {
            console.error('加载上级目录失败:', error);
            contentBody.innerHTML = '<div class="error-state"><i class="fas fa-exclamation-circle"></i><p>加载失败，请重试</p></div>';
        });
    }
}

// 更新面包屑UI
function updateBreadcrumbUI() {
    let html = '';

    breadcrumbData.forEach((item, index) => {
        if (index > 0) {
            html += `<div class="breadcrumb-separator"><i class="fas fa-chevron-right"></i></div>`;
        }

        if (index === breadcrumbData.length - 1) {
            // 最后一项不可点击
            html += `<div class="breadcrumb-item">${item.name}</div>`;
        } else {
            html += `<div class="breadcrumb-item"><a href="#" data-id="${item.id}">${item.name}</a></div>`;
        }
    });
    // 添加返回按钮（非首页时显示）
    if (currentFolderId !== null) {
        html += `<button class="back-btn" id="backBtn">
                    <i class="fas fa-arrow-left"></i>返回上级
                </button>`;
    }
    breadcrumb.innerHTML = html;

    // 重新绑定事件
    const newBackBtn = document.getElementById('backBtn');
    if (newBackBtn) {
        newBackBtn.addEventListener('click', goBack);
    }

    breadcrumb.querySelectorAll('a').forEach(link => {
        link.addEventListener('click', function(e) {
        e.preventDefault();
        const id = this.getAttribute('data-id');
        if (id === 'null') {
            goToHome();
        } else {
            loadFolderById(parseInt(id));
        }
    });
    });
}

    // 前往首页
    function goToHome() {
    currentFolderId = null;
    contentTitle.textContent = '首页';
    contentBody.innerHTML = `
            <div class="empty-state">
                <i class="fas fa-folder-open"></i>
                <h3>暂无内容</h3>
                <p>请选择左侧文件夹或上传新文件</p>
            </div>
        `;
    breadcrumbData = [{id: null, name: '首页'}];
    updateBreadcrumbUI();
    navigationHistory = [];
    updateBackButton();

    // 移除所有文件夹的active状态
    document.querySelectorAll('.folder-item').forEach(item => {
    item.classList.remove('active');
});

    // 重新加载左侧文件夹列表以确保数据最新
    loadFolders();
}

// 更新返回按钮显示状态
function updateBackButton() {
    const backBtn = document.getElementById('backBtn');
    if (backBtn) {
        backBtn.style.display = (currentFolderId !== null && navigationHistory.length > 0) ? 'flex' : 'none';
    }
}

// 显示模态框
function showModal(modal) {
    modal.style.display = 'flex';
}

// 保存文件夹
function saveFolder() {
    var parentType = document.getElementById('folderModal').getAttribute('data-parent-type');
    //parentType==current 二级目录     parentType==root 一级目录
    const folderName = document.getElementById('folderName').value;
    if (!folderName) {
        alert('请输入文件夹名称');
        return;
    }

    // AJAX请求创建文件夹
    $.ajax({
        url: '/ncy/createFolder/',
        type: 'POST',
        contentType: 'application/json',
        data: JSON.stringify({
            name: folderName,
            parentType: parentType,
            parentId: currentFolderId
        }),
        dataType: 'json',
        timeout: 10000,
        async: false,
        success: function (response) {
            folderModal.style.display = 'none';
            document.getElementById('folderName').value = '';
            // 刷新当前内容
            if (parentType == "current") {
                loadFolderContent(currentFolderId, contentTitle.textContent);
            } else {
                // 如果是根目录，重新加载左侧列表
                loadFolders();
            }
        },
        error: function (xhr, status, error) {

        }
    });


}
// 导入Excel
function importExcel() {
    const fileName = document.getElementById('excelName').value;
    const fileInput = document.getElementById('excelFile');
    const file = fileInput.files[0];

    if (!fileName) {
    alert('请输入文件名称');
    return;
}

    if (!file) {
    alert('请选择Excel文件');
    return;
}

    // 创建FormData对象
    const formData = new FormData();
    formData.append('file', file);
    formData.append('fileName', fileName);
    formData.append('parentId', currentFolderId);

    // AJAX请求导入Excel
    fetch('/ncy/importExcel', {
    method: 'POST',
    body: formData
})
    .then(response => {
    if (!response.ok) {
    throw new Error('网络响应不正常');
}
    return response.json();
})
    .then(result => {
    alert(`Excel文件"${fileName}"导入成功!`);
    excelModal.style.display = 'none';
    document.getElementById('excelName').value = '';
    document.getElementById('excelFile').value = '';

    // 刷新当前内容
    if (currentFolderId) {
    loadFolderContent(currentFolderId, contentTitle.textContent);
}
})
    .catch(error => {
    console.error('导入Excel失败:', error);
    alert('导入Excel失败，请重试');
});
}

    // 打开文件（扩展功能接口）
    function openFile(fileId) {
    // 通过AJAX获取文件信息
    fetch('/ncy/getFileInfo', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({ fileId: fileId })
    })
        .then(response => {
            if (!response.ok) {
                throw new Error('网络响应不正常');
            }
            return response.json();
        })
        .then(fileInfo => {
            // 这里可以扩展为打开Excel文件的功能
            alert(`打开文件: ${fileInfo.translate} (扩展功能)`);
            // 在实际应用中，这里可能会打开一个新窗口或标签页显示Excel内容
        })
        .catch(error => {
            console.error('获取文件信息失败:', error);
            alert('打开文件失败，请重试');
        });
}
