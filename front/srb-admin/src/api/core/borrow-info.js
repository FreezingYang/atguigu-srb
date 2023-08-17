import request from '@/utils/request'

export default {
  getPageList(page, limit) {
    return request({
      url: `/admin/core/borrowInfo/list/${page}/${limit}`,
      method: 'get'
    })
  },

  show(id) {
    return request({
      url: `/admin/core/borrowInfo/show/${id}`,
      method: 'get'
    })
  },

  approval(borrowInfoApproval) {
    return request({
      url: '/admin/core/borrowInfo/approval',
      method: 'post',
      data: borrowInfoApproval
    })
  }

}
