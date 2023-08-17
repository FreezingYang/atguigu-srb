import request from '@/utils/request'

export default {
  getPageList(page, limit) {
    return request({
      url: `/admin/core/lend/list/${page}/${limit}`,
      method: 'get'
    })
  },

  show(id) {
    return request({
      url: `/admin/core/lend/show/${id}`,
      method: 'get'
    })
  },

  makeLoan(id) {
    return request({
      url: `/admin/core/lend/makeLoan/${id}`,
      method: 'get'
    })
  }

}
