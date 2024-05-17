import request from '@/utils/request'

export const pirRecordPageRequest = (params) => request({ url: '/data/record/pirRecordPage', method: 'get', params })

export const psiRecordPageRequest = (params) => request({ url: '/data/record/psiRecordPage', method: 'get', params })
