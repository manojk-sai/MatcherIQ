# MatchIQ - Complete Package

---

## 🚀 Quick Start

### Step 1: Extract and Setup

### Step 2: Add OpenAI API Key, URL, and Model name
Edit `src/main/resources/application.properties`:
```properties
llm.api.key=YOUR_ACTUAL_OPENAI_KEY_HERE
llm.api.url=YOUR_ACTUAL_OPENAI_URL_HERE
llm.model=YOUR_ACTUAL_MODEL_ID_HERE
```

### Step 3: Build and Run
```bash
./mvnw clean package
./mvnw spring-boot:run
```

### Step 4: Use the Features!
```bash
# Method 1: Upload resume + job URL (RECOMMENDED)
curl -X POST http://localhost:8080/api/optimizations/upload \
  -F "resumeFile=@my-resume.pdf" \
  -F "jobUrl=https://www.linkedin.com/jobs/view/JOB_ID"

# Method 2: Upload resume + paste job description
curl -X POST http://localhost:8080/api/optimizations/upload-resume \
  -F "resumeFile=@my-resume.pdf" \
  -F "jobDescription=Looking for Senior Java Developer..."

# Method 3: Original text-based method (still works)
curl -X POST http://localhost:8080/api/optimizations \
  -H "Content-Type: application/json" \
  -d '{"resumeText": "...", "jobDescription": "..."}'
```

---

## 🆕 Features Overview

### 1. Resume File Upload
**Supported formats:**
- ✅ PDF (.pdf) - Most common
- ✅ Word (.docx, .doc) - Microsoft Word
- ✅ Text (.txt) - Plain text
- 📏 Max size: 10MB

**How it works:**
- Upload your resume file
- System automatically extracts text
- No copying/pasting!

### 2. Job URL Fetching
**Supported sites:**
- ✅ LinkedIn Jobs
- ✅ Indeed
- ✅ Glassdoor
- ✅ Most other job sites

**How it works:**
- Paste job posting URL
- System fetches and parses the page
- Extracts job description automatically

### 3. Four Ways to Use
Choose what works best for you:

| Method | Resume | Job | Endpoint |
|--------|--------|-----|----------|
| **#1** | File upload | URL | `/upload` |
| **#2** | File upload | Text | `/upload-resume` |
| **#3** | Text | URL | `/fetch-job` |
| **Original** | Text | Text | `/` |

---

### After Starting the Application:

1. ✅ **Async Processing Works**
   - Jobs process in background (1-2 seconds)
   - Thread names: "Optimization-1", "Optimization-2"
   - Status: PENDING → PROCESSING → COMPLETED

2. ✅ **MongoDB Collections Created**
   - Database: `matchIQ`
   - Collection: `optimization_jobs`
   - All fields populated

3. ✅ **OpenAI Integration Works**
   - Real AI-generated content
   - Professional bullet points
   - Personalized cover letters

4. ✅ **File Upload Works**
   - Accepts PDF, DOCX, TXT
   - Automatic text extraction
   - Detailed logging

5. ✅ **Job URL Fetching Works**
   - Fetches from LinkedIn, Indeed, etc.
   - Smart parsing
   - Clean output

---

## 📊 Example: Complete Workflow

### Upload Resume + Job URL:
```bash
curl -X POST http://localhost:8080/api/optimizations/upload \
  -F "resumeFile=@John_Doe_Resume.pdf" \
  -F "jobUrl=https://www.linkedin.com/jobs/view/3845678901"
```

### Response:
```json
{
  "id": "67a1b2c3d4e5f6789abcdef0",
  "status": "PENDING"
}
```

### Check Results (wait 2-3 seconds):
```bash
curl http://localhost:8080/api/optimizations/67a1b2c3d4e5f6789abcdef0
```

### Final Result:
```json
{
  "id": "67a1b2c3d4e5f6789abcdef0",
  "status": "COMPLETED",
  "atsScore": 87,
  "extractedKeywords": ["java", "spring", "boot", "mongodb", "rest", "api"],
  "optimizedBulletPoints": "- Architected 15+ microservices using Spring Boot...",
  "tailoredCoverLetter": "Dear Hiring Manager, I am writing to express...",
  "errorMessage": null
}
```


---

## 🛠️ Troubleshooting

### File Upload Issues:

**"File size exceeds 10MB"**
→ Compress your PDF or remove images

**"Unsupported file type"**
→ Convert to PDF, DOCX, or TXT

**"Cannot extract text from PDF"**
→ PDF might be scanned image, use OCR software first

### Job URL Issues:

**"Failed to fetch job description"**
→ Check if URL is correct and job is still active

**"Connection timeout"**
→ Try again or copy/paste job description manually

**"Job description too short"**
→ Site might require login, use manual method

### OpenAI Issues:

**"⚠️ API Key not configured"**
→ Add your OpenAI API key to application.properties

**"❌ 401 Unauthorized"**
→ Invalid API key, get new one from platform.openai.com

**"❌ 429 Rate limited"**
→ Wait a few minutes, you've exceeded free tier

---
