# 高校实验室预约申请与设备管理系统

本项目用于《网络应用系统开发实习》，采用前后端分离架构。

## 技术栈

- 后端：Spring Boot 3、MyBatis Plus、MySQL、Redis
- 前端：Vue 3、Element Plus、Axios、Pinia、Vue Router
- 构建工具：Maven、Vite

## 目录说明

- `backend`：后端接口服务
- `frontend`：前端管理界面
- `docs`：数据库脚本、接口说明、开发文档

## 主要功能

- 用户登录
- 实验室信息管理
- 设备信息管理
- 实验室预约申请
- 设备借用申请
- 管理员审核
- 预约记录、设备借用记录、操作日志导出
- 实验室预约日历视图
- 待审核与审核结果提醒
- Redis 缓存公告列表
- 公告与数据统计扩展

## 角色设计

- 学生：查看实验室和设备，提交预约或借用申请，查看审核进度
- 教师审核员：审核预约申请和设备借用申请
- 管理员：维护用户、实验室、设备、公告和系统数据

## 后端启动

1. 创建 MySQL 数据库 `lab_reservation`
2. 执行 `docs/schema.sql`
3. 修改 `backend/src/main/resources/application.yml` 中数据库账号密码
4. 用 IDEA 打开 `backend` 目录，运行 `LabReservationApplication`

默认接口地址：`http://localhost:8080/api`

## 前端启动

进入 `frontend` 目录后安装依赖并运行：

```bash
npm install
npm run dev
```

默认页面地址：`http://localhost:5173`
