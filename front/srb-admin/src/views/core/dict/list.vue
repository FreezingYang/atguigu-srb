<template>
  <div class="app-container">
    <div style="margin-bottom: 10px;">
      <!-- Excel导入按钮 -->
      <el-button @click="dialogVisible = true" type="primary" size="mini" icon="el-icon-download">
        导入Excel
      </el-button>

      <!-- Excel导出按钮 -->
      <el-button @click="exportData" type="primary" size="mini" icon="el-icon-upload2">
        导出Excel
      </el-button>
    </div>

    <!-- Excel文件上传 -->
    <el-dialog title="数据字典导入" :visible.sync="dialogVisible" width="30%">
      <el-form>
        <el-form-item label="请选择Excel文件">
          <el-upload :auto-upload="true" :multiple="false" :limit="1" :on-exceed="fileUploadExceed"
            :on-success="fileUploadSuccess" :on-error="fileUploadError" :action="BASE_API + '/admin/core/dict/import'"
            name="file"
            accept="application/vnd.ms-excel,application/vnd.openxmlformats-officedocument.spreadsheetml.sheet">
            <el-button size="small" type="primary">点击上传</el-button>
          </el-upload>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false">
          取消
        </el-button>
      </div>
    </el-dialog>

    <!-- 数据字典表格展示 -->
    <el-table :data="list" style="width: 100%" row-key="id" border lazy :load="load"
      :tree-props="{children: 'children', hasChildren: 'hasChildren'}">
      <el-table-column prop="name" label="名称" align="left">
      </el-table-column>
      <el-table-column prop="dictCode" label="编码">
      </el-table-column>
      <el-table-column prop="value" label="值" align="left">
      </el-table-column>
    </el-table>
  </div>
</template>

<script>
  import dictApi from '@/api/core/dict'

  export default {
    // 定义数据
    data() {
      return {
        dialogVisible: false, //文件上传对话框是否显示
        BASE_API: process.env.VUE_APP_BASE_API, //获取后端接口地址
        list: []
      }
    },

    created() {
      this.fetchData()
    },

    methods: {

      fetchData() {
        dictApi.listByParentId(1).then(response => {
          this.list = response.data.list
        })
      },

      // 加载子节点
      load(row, treeNode, resolve) {
        console.log(row);
        console.log(treeNode);
        // 获取数据
        dictApi.listByParentId(row.id).then(response => {
          // treeNode.children = response.data.list
          resolve(response.data.list)
        })
        console.log(resolve);

      },


      // 上传多于一个文件时
      fileUploadExceed() {
        this.$message.warning('只能选取一个文件')
      },

      //上传成功回调
      fileUploadSuccess(response) {
        if (response.code === 200) {
          //业务成功
          this.$message.success('数据导入成功')
          this.dialogVisible = false
        } else {
          // 业务失败
          this.$message.error(response.message)
        }
      },

      //上传失败回调
      fileUploadError(error) {
        this.$message.error('数据导入失败')
      },

      //Excel数据导出
      exportData() {
        window.location.href = this.BASE_API + '/admin/core/dict/export'
      }


    }

  }

</script>
