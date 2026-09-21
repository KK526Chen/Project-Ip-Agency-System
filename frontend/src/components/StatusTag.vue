<script setup>import { text, tagType } from '../utils/enums'; defineProps({ group: String, value: [String,Number] })</script>
<template><el-tag :type="tagType(value)" effect="light">{{ text(group, value) }}</el-tag></template>
