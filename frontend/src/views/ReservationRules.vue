<template>
  <div>
    <div class="toolbar">
      <h2 class="page-title">预约规则配置</h2>
    </div>

    <div class="panel rule-panel">
      <el-form :model="form" label-width="150px">
        <el-form-item label="最多提前预约">
          <el-input-number v-model="form.maxAdvanceDays" :min="1" :max="60" />
          <span class="unit">天</span>
        </el-form-item>
        <el-form-item label="单次最长预约">
          <el-input-number v-model="form.maxDurationHours" :min="1" :max="24" />
          <span class="unit">小时</span>
        </el-form-item>
        <el-form-item label="每人每日最多预约">
          <el-input-number v-model="form.dailyLimit" :min="1" :max="20" />
          <span class="unit">次</span>
        </el-form-item>
        <el-form-item label="允许周末预约">
          <el-switch v-model="form.allowWeekend" :active-value="1" :inactive-value="0" active-text="允许" inactive-text="不允许" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="save">保存规则</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { onMounted, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { getActiveReservationRule, updateActiveReservationRule } from '../api/modules'

const form = reactive({
  id: null,
  maxAdvanceDays: 7,
  maxDurationHours: 4,
  dailyLimit: 2,
  allowWeekend: 1
})

const load = async () => {
  Object.assign(form, await getActiveReservationRule())
}

const save = async () => {
  if (!form.maxAdvanceDays || !form.maxDurationHours || !form.dailyLimit) {
    ElMessage.warning('请完整填写预约规则')
    return
  }
  await updateActiveReservationRule(form)
  ElMessage.success('预约规则已保存')
  load()
}

onMounted(load)
</script>

<style scoped>
.rule-panel {
  max-width: 720px;
}

.unit {
  margin-left: 10px;
  color: #6b7280;
}
</style>
