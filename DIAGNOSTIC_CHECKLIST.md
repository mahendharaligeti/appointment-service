# 403 Forbidden - Diagnostic Checklist

## ✅ Code Verification (Already Confirmed)

- ✅ **CustomUserDetailsService** correctly adds "ROLE_" prefix
- ✅ **JwtAuthFilter** properly calls loadUserFromToken()
- ✅ **DoctorController** has `@PreAuthorize("hasRole('ADMIN')")` 
- ✅ **JWT extraction** from token is correct
- ✅ **Role conversion** from "ADMIN" → "ROLE_ADMIN" is correct

---

## 🔍 What to Check NOW

### 1. **Verify JWT Payload on jwt.io**

Paste your token here: https://jwt.io

✅ Check these fields exist:
```json
{
  "sub": "testadmin@hospital.com",     ← Username
  "role": "ADMIN",                      ← Must be "ADMIN" not "admin"
  "exp": 1780383316,                    ← Must be FUTURE timestamp
  "iat": 1780296916                     ← Issue timestamp
}
```

**❌ If `"role"` is missing:**
→ User-service is not adding role to JWT. Contact user-service owner.

**❌ If role is lowercase `"role":"admin"`:**
→ CustomUserDetailsService will fix it with `.toUpperCase()` so this should work.

**❌ If `exp` is in the past:**
→ Token is expired. Get a new one.

---

### 2. **Check JWT_SECRET Match**

Run this in **two separate terminals**:

**Terminal 1 - Check what User Service is using:**
```bash
# Look at user-service logs for secret being loaded
# Or check user-service config
```

**Terminal 2 - Check what Appointment Service has:**
```bash
echo $JWT_SECRET
```

✅ **They MUST be identical!**

If you see:
```
User-service secret: abc123def456...
Appointment-service secret: xyz789uv...
```
❌ That's your problem!

**Fix:**
```bash
# Set the SAME secret for both
export JWT_SECRET="same-secret-value-here"

# Restart BOTH services
```

---

### 3. **Test Token Validation**

In appointment-service logs, look for:

```
ERROR ... Could not set user authentication
```

If you see this, it means:
- Token exists ✓
- But validation is failing ✗
- Likely cause: **JWT_SECRET mismatch**

---

### 4. **Test Public Endpoint First**

```bash
curl http://localhost:8082/api/doctors
```

✅ **If this works:**
- Service is running
- Configuration is loaded
- Issue is authentication-specific

❌ **If this fails:**
- Service isn't running
- Wrong port
- Connection problem

---

### 5. **Test with curl -v for Verbose Output**

```bash
curl -v -X POST http://localhost:8082/api/doctors \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN_HERE" \
  -d '{
    "name": "Dr. Test",
    "specialization": "Cardiology",
    "email": "doc@test.com",
    "phone": "9876543210",
    "availableFrom": "09:00",
    "availableTo": "17:00"
  }'
```

Look for:
- **403 Forbidden** → Authentication failed (role issue)
- **401 Unauthorized** → Token rejected (JWT_SECRET issue)
- **400 Bad Request** → Request validation issue
- **201 Created** → Success! ✅

---

## 🛠️ Step-by-Step Verification

### Step 1: Get Fresh Token
```bash
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "testadmin@hospital.com",
    "password": "YOUR_PASSWORD"
  }' -s | jq -r '.token'
```

Copy the token output.

---

### Step 2: Decode on jwt.io
1. Go to https://jwt.io
2. Paste token in left side
3. Verify payload section shows:
   - `"sub": "testadmin@hospital.com"`
   - `"role": "ADMIN"`
   - `"exp"` is a future timestamp

---

### Step 3: Show Environment Variables
```bash
echo "JWT_SECRET=$JWT_SECRET" | head -c 50
echo "..."
```

---

### Step 4: Test Public Endpoint
```bash
curl http://localhost:8082/api/doctors | jq '.data | length'
```

Should return a number (count of doctors) or empty list.

---

### Step 5: Test Protected Endpoint
```bash
curl -X POST http://localhost:8082/api/doctors \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer PASTE_TOKEN_HERE" \
  -d '{
    "name": "Dr. Verification",
    "specialization": "Cardiology",
    "email": "verify'$(date +%s)'@test.com",
    "phone": "9876543210",
    "availableFrom": "09:00",
    "availableTo": "17:00"
  }' | jq '.status'
```

✅ Should return `"success"` (status 201)
❌ If you get `"error"`, check status code

---

## 📋 Common Results & Solutions

| Symptom | Cause | Solution |
|---------|-------|----------|
| 403 Forbidden with valid token | JWT_SECRET mismatch | Check `echo $JWT_SECRET` matches both services |
| 403 Forbidden, role missing from token | User-service issue | Contact user-service owner |
| 401 Unauthorized | Token rejected by parser | Check JWT_SECRET, or token expired |
| 400 Bad Request | Request body invalid | Check request JSON format |
| 200 OK but "error" in response | Business logic error | Check error message in response |
| Connection refused | Service not running | Start appointment service on port 8082 |

---

## 🔧 Nuclear Option - Complete Fresh Start

If nothing works, try this:

```bash
# 1. Stop both services

# 2. Use a simple, identical secret
export JWT_SECRET="simple-test-secret-123456789012345678901234"

# 3. Delete and recreate databases (if needed)

# 4. Start User Service
cd user-service
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev" &

# Wait for it to start (should see "Started" message)
sleep 10

# 5. Start Appointment Service in another terminal
cd appointment-service
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev"

# 6. Test
curl http://localhost:8082/api/doctors
```

---

## 📞 Final Troubleshooting

If you provide:
1. The exact HTTP response (headers + body)
2. The appointment service logs (last 50 lines)
3. The JWT token (decoded on jwt.io)
4. Whether `echo $JWT_SECRET` matches between services

I can pinpoint the exact issue! 🎯

