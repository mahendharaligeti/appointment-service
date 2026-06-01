# JWT Token Debugging Guide

## Quick Diagnosis

### 1. Decode Your JWT Token
Go to https://jwt.io and paste your token to see its contents.

**Expected payload:**
```json
{
  "sub": "testadmin@hospital.com",
  "role": "ADMIN",
  "iat": 1780296916,
  "exp": 1780383316
}
```

### 2. Check Expiration
- **iat**: Token issue time (unix timestamp)
- **exp**: Token expiration time (unix timestamp)

If `exp` is in the past, the token is expired.

Convert unix timestamp: https://www.unixtimestamp.com/

### 3. Verify the Exact Error

Test with curl to see the actual error:

```bash
curl -v -X POST http://localhost:8082/api/doctors \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "name": "Dr. Test",
    "specialization": "Cardiology",
    "email": "doc@test.com",
    "phone": "9876543210",
    "availableFrom": "09:00",
    "availableTo": "17:00"
  }'
```

### 4. Check Appointment Service Logs

Look for messages like:
```
ERROR ... Could not set user authentication in security context
WARN ... Invalid token
ERROR ... Failed to parse JWT
```

---

## Common Issues & Fixes

### ❌ Issue 1: Token is Expired

**Sign:** `exp` timestamp is before current time

**Fix:** Get a new token from user-service:
```bash
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "testadmin@hospital.com",
    "password": "YOUR_PASSWORD"
  }'
```

---

### ❌ Issue 2: Wrong JWT_SECRET

**Sign:** Token validates in jwt.io but fails in app

**Symptoms:**
- Works in jwt.io but 403 in appointment-service
- Logs show "Could not set user authentication"

**Fix:** 
Ensure BOTH services use the SAME secret:

```bash
# Show what's set
echo $JWT_SECRET

# Set a new one (for testing)
export JWT_SECRET="test-secret-key-12345678901234567890"

# Restart BOTH services with this secret
```

---

### ❌ Issue 3: Token Missing "role" Claim

**Sign:** JWT has no "role" field

**Expected in jwt.io:**
```json
{
  "sub": "...",
  "role": "ADMIN",  // <-- This MUST exist
  ...
}
```

**Fix:** User service must add role to token. Check user-service JwtUtil to ensure it includes role claim.

---

### ❌ Issue 4: Case Sensitivity Issue

**Sign:** Role is "admin" (lowercase) instead of "ADMIN"

**Expected:** `"role": "ADMIN"`
**Wrong:** `"role": "admin"`

**Fix:** Appointment service CustomUserDetailsService does `.toUpperCase()`, so this should work anyway:
```java
authorities.add(new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()));
```

But verify user-service is sending uppercase role.

---

### ❌ Issue 5: Bearer Token Format Wrong

**Sign:** Token not being extracted

**Check in Postman headers:**
```
Authorization: Bearer eyJhbGci...  ← Note the space!
```

**Wrong:**
```
Authorization:Bearer eyJhbGci...  ← No space!
```

**Fix:** Ensure space between "Bearer" and token.

---

## Complete Verification Flow

### Step 1: Test Public Endpoint (No Auth)
```bash
curl http://localhost:8082/api/doctors
```
✅ Should return list of doctors (empty if none added)

### Step 2: Get Fresh Token
```bash
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "testadmin@hospital.com",
    "password": "PASSWORD"
  }' | jq '.token'
```

### Step 3: Decode Token
Paste the token on https://jwt.io
Verify:
- ✅ `"role": "ADMIN"` exists
- ✅ `"exp"` is in the future
- ✅ Signature matches

### Step 4: Test Protected Endpoint
```bash
curl -X POST http://localhost:8082/api/doctors \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer TOKEN_HERE" \
  -d '{
    "name": "Dr. Test",
    "specialization": "Cardiology",
    "email": "doc@test.com",
    "phone": "9876543210",
    "availableFrom": "09:00",
    "availableTo": "17:00"
  }' | jq '.'
```

### Step 5: If Still 403, Check Logs
```bash
# Find the last 50 lines of appointment service logs
# Look for error messages in the console output
```

---

## Appointment Service Debug Logging

To add debug logging, edit `src/main/resources/application-dev.yml`:

```yaml
logging:
  level:
    com.hospital.appointmentservice.config: DEBUG
    org.springframework.security: DEBUG
```

Restart the service for more detailed logs.

---

## Quick Test Script

Save this as `test-appointment.sh`:

```bash
#!/bin/bash

# Color codes
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

TOKEN="YOUR_TOKEN_HERE"
EMAIL="testadmin@hospital.com"

echo -e "${YELLOW}=== Testing Appointment Service ===${NC}\n"

# Test 1: Public endpoint
echo -e "${YELLOW}1. Testing public endpoint (no auth needed)${NC}"
curl -s http://localhost:8082/api/doctors | jq '.' && echo -e "${GREEN}✓ Public endpoint works${NC}\n" || echo -e "${RED}✗ Failed${NC}\n"

# Test 2: Protected endpoint with token
echo -e "${YELLOW}2. Testing protected endpoint (POST /api/doctors)${NC}"
curl -s -X POST http://localhost:8082/api/doctors \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "name": "Dr. Test",
    "specialization": "Cardiology",
    "email": "doc'$(date +%s)'@test.com",
    "phone": "9876543210",
    "availableFrom": "09:00",
    "availableTo": "17:00"
  }' | jq '.' && echo -e "${GREEN}✓ Protected endpoint works${NC}\n" || echo -e "${RED}✗ Failed (likely 403)${NC}\n"

# Test 3: Decode token
echo -e "${YELLOW}3. Token claims (from jwt.io):${NC}"
echo "Go to https://jwt.io and paste:"
echo "$TOKEN"
echo ""
```

