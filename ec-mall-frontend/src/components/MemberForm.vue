<template>
  <el-form ref="formRef" :model="form" :rules="rules" label-position="top" :disabled="busy" @submit.prevent="submit">
    <el-form-item label="氏名" prop="name"><el-input v-model.trim="form.name" autocomplete="name" /></el-form-item>
    <el-form-item label="メールアドレス" prop="email"><el-input v-model.trim="form.email" type="email" autocomplete="email" /></el-form-item>
    <el-form-item label="年齢" prop="age"><el-input-number v-model="form.age" :min="0" :max="150" :precision="0" /></el-form-item>
    <el-form-item :label="creating ? 'パスワード' : '新しいパスワード（変更する場合のみ）'" prop="password">
      <el-input v-model="form.password" type="password" show-password autocomplete="new-password" />
    </el-form-item>
    <el-form-item label="パスワードの確認" prop="confirmation">
      <el-input v-model="form.confirmation" type="password" show-password autocomplete="new-password" />
    </el-form-item>
    <el-button type="primary" native-type="submit" :loading="busy">{{ creating ? '登録する' : '保存する' }}</el-button>
  </el-form>
</template>
<script setup>
import { reactive, ref, watch } from 'vue'
const props = defineProps({ member: { type: Object, default: () => ({}) }, creating: Boolean, busy: Boolean })
const emit = defineEmits(['submit'])
const formRef = ref()
const form = reactive({ name: '', email: '', age: 18, password: '', confirmation: '' })
watch(() => props.member, (member) => Object.assign(form, {
  name: member.name || '', email: member.email || '', age: member.age ?? 18, password: '', confirmation: '',
}), { immediate: true })
const rules = {
  name: [{ required: true, whitespace: true, message: '氏名を入力してください。', trigger: 'blur' }],
  email: [{ required: true, message: 'メールアドレスを入力してください。', trigger: 'blur' }, { type: 'email', message: 'メールアドレスの形式をご確認ください。', trigger: 'blur' }],
  age: [{ required: true, type: 'integer', min: 0, max: 150, message: '年齢は0〜150の整数で入力してください。', trigger: 'change' }],
  password: [{ validator: (_, value, done) => {
    const valid = !props.creating && !value || value.trim() && value.length >= 6 && value.length <= 20
    done(valid ? undefined : new Error('パスワードは6〜20文字で入力してください。'))
  }, trigger: 'blur' }],
  confirmation: [{ validator: (_, value, done) => done(value === form.password ? undefined : new Error('パスワードが一致しません。')), trigger: 'blur' }],
}
async function submit() {
  if (props.busy || !await formRef.value.validate().catch(() => false)) return
  const data = { name: form.name, email: form.email, age: form.age }
  if (form.password) data.password = form.password
  emit('submit', data)
}
</script>
