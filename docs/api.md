# 接口说明

后端基础地址：`http://localhost:8080/api`

## 登录

- `POST /auth/login`
- 请求体：`username`、`password`

## 用户管理

- `GET /users`
- `POST /users`
- `PUT /users/{id}`
- `DELETE /users/{id}`

权限：仅管理员 `ADMIN`。

## 实验室管理

- `GET /labs`
- `POST /labs`
- `PUT /labs/{id}`
- `DELETE /labs/{id}`

权限：所有角色可查看；仅管理员 `ADMIN` 可新增、修改、删除。

## 设备管理

- `GET /equipment`
- `POST /equipment`
- `PUT /equipment/{id}`
- `DELETE /equipment/{id}`

权限：所有角色可查看；仅管理员 `ADMIN` 可新增、修改、删除。

## 实验室预约

- `GET /lab-reservations`
- `POST /lab-reservations`
- `PUT /lab-reservations/{id}/review`

权限：所有角色可查看；学生 `STUDENT` 可提交申请；教师审核员 `TEACHER` 可审核；管理员 `ADMIN` 可查看全部。

审核状态建议：

- `PENDING`：待审核
- `APPROVED`：已通过
- `REJECTED`：已驳回
- `CANCELED`：已取消

## 设备借用

- `GET /equipment-borrows`
- `POST /equipment-borrows`
- `PUT /equipment-borrows/{id}/review`

权限：所有角色可查看；学生 `STUDENT` 可提交申请；教师审核员 `TEACHER` 可审核；管理员 `ADMIN` 可查看全部。

## 公告管理

- `GET /notices`
- `POST /notices`

权限：所有角色可查看；仅管理员 `ADMIN` 可发布公告。

## 权限请求头

除登录接口外，前端会自动携带：

- `X-User-Role`：当前角色，例如 `ADMIN`、`TEACHER`、`STUDENT`
- `X-User-Id`：当前用户 ID
