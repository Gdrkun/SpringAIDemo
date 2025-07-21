<template>
  <div class="file-management-container">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>知识文件管理</span>
          <div>
            <el-button type="primary" @click="showUpload()">上传文件</el-button>
            <el-button @click="$emit('backToChat')">返回对话</el-button>
          </div>
        </div>
      </template>

      <div class="actions">
        <el-button type="danger" @click="handleBatchDelete" :disabled="selectedFiles.length === 0">批量删除</el-button>
        <el-button type="info" @click="handleBatchVectorize" :disabled="selectedFiles.length === 0">批量向量化</el-button>
      </div>

      <el-table :data="files" @selection-change="handleSelectionChange" style="width: 100%">
        <el-table-column type="selection" width="55" />
        <el-table-column prop="fileName" label="文件名" />
        <el-table-column prop="fileType" label="类型" width="120" />
        <el-table-column prop="fileSize" label="大小" width="120">
          <template #default="scope">
            {{ formatFileSize(scope.row.fileSize) }}
          </template>
        </el-table-column>
        <el-table-column prop="vectorizationStatus" label="向量化状态" width="150">
          <template #default="scope">
            <el-tag :type="getTagType(scope.row.vectorizationStatus)">{{ getStatusText(scope.row.vectorizationStatus) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180">
          <template #default="scope">
            {{ new Date(scope.row.createdAt).toLocaleString() }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="scope">
            <el-button size="small" type="danger" @click="handleDelete(scope.row.id)">删除</el-button>
            <el-button size="small" type="info" @click="handleVectorize(scope.row.id)" :disabled="scope.row.vectorizationStatus === 2 || scope.row.vectorizationStatus === 1">向量化</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        background
        layout="prev, pager, next"
        :total="totalFiles"
        :page-size="pageSize"
        @current-change="handlePageChange"
        class="pagination"
      />
    </el-card>

    <el-dialog v-model="showUploadModal" title="上传新文件">
      <el-upload
        drag
        :auto-upload="false"
        :on-change="handleFileChange"
        :before-remove="handleFileRemove"
        action="#"
      >
        <i class="el-icon-upload"></i>
        <div class="el-upload__text">将文件拖到此处，或<em>点击上传</em></div>
      </el-upload>
      <el-input v-model="uploadDescription" placeholder="输入文件描述 (可选)" style="margin-top: 1rem;" />
      <el-checkbox v-model="enableVectorization" label="上传后立即向量化" style="margin-top: 1rem;" />
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showUploadModal = false">取消</el-button>
          <el-button type="primary" @click="handleUpload">确认上传</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { knowledgeFileAPI } from '../services/api';
import type { KnowledgeFile } from '../types';
import type { UploadFile } from 'element-plus';

const files = ref<KnowledgeFile[]>([]);
const selectedFiles = ref<number[]>([]);
const showUploadModal = ref(false);
const newFile = ref<File | null>(null);
const uploadDescription = ref('');
const enableVectorization = ref(true);
const currentPage = ref(1);
const totalFiles = ref(0);
const pageSize = 10;

const fetchFiles = async () => {
  try {
    const response = await knowledgeFileAPI.getKnowledgeFiles(currentPage.value, pageSize);
    files.value = response.records;
    totalFiles.value = response.total;
  } catch (error) {
    console.error('加载文件列表失败:', error);
  }
};

onMounted(fetchFiles);

const formatFileSize = (bytes: number) => {
  if (bytes === 0) return '0 B';
  const k = 1024;
  const sizes = ['B', 'KB', 'MB', 'GB', 'TB'];
  const i = Math.floor(Math.log(bytes) / Math.log(k));
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
};

const handleFileChange = (file: UploadFile) => {
  newFile.value = file.raw as File;
};

const handleFileRemove = () => {
  newFile.value = null;
};

const showUpload = () => {
  showUploadModal.value = true;
  uploadDescription.value='';
  newFile.value = null;
};

const handleUpload = async () => {
  if (!newFile.value) return;
  try {
    await knowledgeFileAPI.uploadKnowledgeFile(newFile.value, uploadDescription.value, enableVectorization.value);
    showUploadModal.value = false;
    newFile.value = null;
    uploadDescription.value = '';
    fetchFiles(); // Refresh list
  } catch (error) {
    console.error('上传失败:', error);
  }
};

const handleDelete = async (id: number) => {
  if (confirm('确定要删除这个文件吗？')) {
    try {
      await knowledgeFileAPI.deleteKnowledgeFile(id);
      fetchFiles(); // Refresh list
    } catch (error) {
      console.error('删除失败:', error);
    }
  }
};

const handleBatchDelete = async () => {
  if (confirm(`确定要删除选中的 ${selectedFiles.value.length} 个文件吗？`)) {
    try {
      await knowledgeFileAPI.batchDeleteKnowledgeFiles(selectedFiles.value);
      selectedFiles.value = [];
      fetchFiles(); // Refresh list
    } catch (error) {
      console.error('批量删除失败:', error);
    }
  }
};

const handleVectorize = async (id: number) => {
  try {
    await knowledgeFileAPI.vectorizeFile(id);
    alert('向量化任务已开始');
    fetchFiles(); // Refresh list to show status change
  } catch (error) {
    console.error('向量化失败:', error);
  }
};

const handleBatchVectorize = async () => {
  if (confirm(`确定要向量化选中的 ${selectedFiles.value.length} 个文件吗？`)) {
    try {
      await knowledgeFileAPI.batchVectorizeFiles(selectedFiles.value);
      alert('批量向量化任务已开始');
      selectedFiles.value = [];
      fetchFiles(); // Refresh list
    } catch (error) {
      console.error('批量向量化失败:', error);
    }
  }
};

const handleSelectionChange = (selection: KnowledgeFile[]) => {
  selectedFiles.value = selection.map(item => item.id);
};

const getTagType = (status: number) => {
  switch (status) {
    case 0:
      return 'info';
    case 1:
      return 'warning';
    case 2:
      return 'success';
    case 3:
      return 'danger';
    default:
      return '';
  }
};

const getStatusText = (status: number) => {
  switch (status) {
    case 0:
      return '未向量化';
    case 1:
      return '向量化中';
    case 2:
      return '向量化成功';
    case 3:
      return '向量化失败';
    default:
      return '未知状态';
  }
};

const handlePageChange = (page: number) => {
  currentPage.value = page;
  fetchFiles();
};
</script>

<style scoped>
.file-management-container {
  padding: 2rem;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.actions {
  margin-bottom: 1rem;
}
.pagination {
  margin-top: 1rem;
  justify-content: flex-end;
}
</style>

