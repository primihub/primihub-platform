import request from '@/utils/request'

export const submitScoreModelRequest = (data) => request({ url: '/data/pir/submitScoreModel', method: 'post', data })

export const getScoreModelListRequest = () => request({ url: '/data/pir/getScoreModelList', method: 'get' })
