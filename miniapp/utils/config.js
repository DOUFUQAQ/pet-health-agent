// utils/config.js - 环境配置中心
//
// 移植部署时只改这一个文件即可。
// 不同环境切换：把下面 BASE_URL 换成对应的就行。
//
// 注意：微信小程序无法使用 localhost / 127.0.0.1，必须用电脑实际 IP 或公网域名。
// 用 ipconfig (Win) / ifconfig (Mac/Linux) 查电脑当前网络的 IPv4 地址。

const ENV = {
  dev: 'http://100.125.67.9:8080/api',     // 本机调试：电脑当前实际 IP
  staging: 'http://your-staging.com/api',  // 预发环境
  prod: 'https://api.pethealth.com/api',   // 生产：必须 https + ICP 备案
}

// 切换环境只需改这里
const ACTIVE = 'dev'

module.exports = {
  BASE_URL: ENV[ACTIVE],
  ENV: ACTIVE,
}
