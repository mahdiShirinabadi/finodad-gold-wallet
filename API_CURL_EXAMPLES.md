# API CURL Examples for Role Management

## 🔐 Resource Permissions

### 1. ROLE_MANAGE
All role-related operations require `ROLE_MANAGE` permission:
- Create role
- List roles  
- Update role
- Delete role
- Assign resources to role
- Add resources to role
- Remove resources from role
- Get role details
- Get role resources

### 2. RESOURCE_MANAGE
All resource-related operations require `RESOURCE_MANAGE` permission:
- Create resource
- List resources
- Update resource
- Delete resource

### 3. CHANNEL_MANAGE
All channel-related operations require `CHANNEL_MANAGE` permission:
- List channels
- Assign role to channel
- Remove role from channel

---

## 📋 CURL Examples

### 🔑 Authentication
```bash
# Get JWT Token
curl -X POST "http://localhost:8080/api/v1/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "password"
  }'
```

---

## 🎭 ROLE_MANAGE Operations

### 1. Create Role
```bash
curl -X POST "http://localhost:8080/api/v1/panel/role-management/role/create" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "name": "ADMIN_ROLE",
    "persianDescription": "نقش مدیر سیستم",
    "additionalData": "Admin role for system management",
    "endTime": "2024-12-31T23:59:59"
  }'
```

### 2. List Roles
```bash
curl -X GET "http://localhost:8080/api/v1/panel/role-management/role/list?page=0&size=10&sort=name,asc" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### 3. Assign Resources to Role
```bash
curl -X POST "http://localhost:8080/api/v1/panel/role-management/1/assign-resources" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "resourceIds": [1, 2, 3, 4]
  }'
```

### 4. Add Resources to Role (Replaces all existing resources)
```bash
curl -X POST "http://localhost:8080/api/v1/panel/role-management/1/add-resources" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "resourceIds": [5, 6]
  }'
```

**Note:** This operation will first delete all existing resources assigned to the role, then assign the new resources. The entire operation is transactional - if any step fails, all changes will be rolled back.

### 5. Remove Resources from Role
```bash
curl -X DELETE "http://localhost:8080/api/v1/panel/role-management/1/remove-resources" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "resourceIds": [3, 4]
  }'
```

### 6. Get Role Details
```bash
curl -X GET "http://localhost:8080/api/v1/panel/role-management/1/details" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### 7. Get Role Resources
```bash
curl -X GET "http://localhost:8080/api/v1/panel/role-management/1/resources" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

## 📦 RESOURCE_MANAGE Operations

### 1. Create Resource
```bash
curl -X POST "http://localhost:8080/api/v1/panel/role-management/resource/create" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "name": "USER_CREATE",
    "faName": "ایجاد کاربر",
    "display": "Create User"
  }'
```

### 2. List Resources
```bash
curl -X GET "http://localhost:8080/api/v1/panel/role-management/resource/list?page=0&size=10&sort=name,asc" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

## 🌐 CHANNEL_MANAGE Operations

### 1. List Channels
```bash
curl -X GET "http://localhost:8080/api/v1/panel/role-management/channel/list" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Response Example:**
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "name": "John Doe",
      "username": "john.doe",
      "description": "ACTIVE",
      "isActive": true,
      "assignedRoles": [
        {
          "id": 1,
          "name": "ADMIN_ROLE",
          "persianDescription": "نقش مدیر سیستم"
        }
      ]
    }
  ]
}
```

### 2. Assign Role to Channel (Replaces all existing roles)
```bash
curl -X POST "http://localhost:8080/api/v1/panel/role-management/assign-role-to-channel" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "roleId": 1,
    "channelId": 1
  }'
```

**Note:** This operation will first delete all existing roles assigned to the channel, then assign the new role. The entire operation is transactional - if any step fails, all changes will be rolled back.

### 3. Remove Role from Channel
```bash
curl -X DELETE "http://localhost:8080/api/v1/panel/role-management/remove-role-from-channel" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "roleId": 1,
    "channelId": 1
  }'
```

---

## 🔒 Permission Matrix

| Operation | Required Permission | Endpoint |
|-----------|-------------------|----------|
| Create Role | `ROLE_MANAGE` | `POST /role/create` |
| List Roles | `ROLE_MANAGE` | `GET /role/list` |
| Assign Resources | `ROLE_MANAGE` | `POST /{roleId}/assign-resources` |
| Add Resources | `ROLE_MANAGE` | `POST /{roleId}/add-resources` |
| Remove Resources | `ROLE_MANAGE` | `DELETE /{roleId}/remove-resources` |
| Get Role Details | `ROLE_MANAGE` | `GET /{roleId}/details` |
| Get Role Resources | `ROLE_MANAGE` | `GET /{roleId}/resources` |
| Create Resource | `RESOURCE_MANAGE` | `POST /resource/create` |
| List Resources | `RESOURCE_MANAGE` | `GET /resource/list` |
| List Channels | `CHANNEL_MANAGE` | `GET /channel/list` |
| Assign Role to Channel | `CHANNEL_MANAGE` | `POST /assign-role-to-channel` |
| Remove Role from Channel | `CHANNEL_MANAGE` | `DELETE /remove-role-from-channel` |

---

## 📝 Notes

1. **JWT Token**: Replace `YOUR_JWT_TOKEN` with the actual JWT token received from login
2. **Base URL**: Update the base URL according to your environment
3. **Pagination**: Use `page`, `size`, and `sort` parameters for paginated endpoints
4. **Error Handling**: Check HTTP status codes and response bodies for error details
5. **Validation**: All request bodies are validated using `@Valid` annotations
