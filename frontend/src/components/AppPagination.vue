<script setup>
defineProps({ page: Number, size: Number, total: Number }); defineEmits(['update:page','update:size','change'])
</script>
<template><div class="pagination-row"><el-pagination background layout="total, sizes, prev, pager, next" :current-page="page" :page-size="size" :page-sizes="[10,20,50]" :total="total" @update:current-page="(v)=>$emit('update:page',v)" @update:page-size="(v)=>$emit('update:size',v)" @change="$emit('change')" /></div></template>
