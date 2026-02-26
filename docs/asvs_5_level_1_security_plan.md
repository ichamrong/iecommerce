# OWASP ASVS 5.0 Level 1 - Security Verification Plan & Checklist

> **Purpose:** This document formalizes the security and code review standards based on the final components of OWASP ASVS 5.0 Level 1. It acts as both an architectural guide and a mandatory checklist for all code reviewers working on this platform.

---

## 1. The Fallacy of Testability (ទស្សនវិជ្ជានៃ "ការធ្លាក់ចុះនៃភាពអាចធ្វើតេស្តបាន")
*   **Concept**: ASVS 5.0 Level 1 rejects the old notion that Level 1 must be strictly externally testable via automated vulnerability scanners.
*   **Standard**: Internal architectural defenses must exist. Although verifying 100% Parameterized Query usage is difficult from an external black-box perspective, it is a non-negotiable Level 1 baseline.
*   **Code Review Verification**: 
    - Every database transaction MUST route through Spring Data JPA / Hibernate repositories (which natively utilize compiled, parameterized prepared statements). 
    - Any instance of raw SQL string concatenation must trigger an immediate Pull Request rejection.

## 2. Configuration & Leakage (ការគ្រប់គ្រងការសម្ងាត់ និងការលេចធ្លាយ)
*   **Requirement (V13.4.1)**: Ensure source control directories (`.git`, `.svn`) are securely blocked from public access.
*   **Standard**: Configuration files must not bleed source code or excessive debug data to the public internet.
*   **Code Review Verification**: 
    - Verify that `.dockerignore` and build tools actively strip the `.git` folder from the final production container/artifact.
    - Verify NGINX or API Gateway configurations explicitly drop any traffic targeting `/\.git`.
    - `application-prod.yml` must NOT have debug mode enabled, and global exception handlers must strip stack traces from API responses.

## 3. Authentication Essentials (ការបញ្ជាក់អត្តសញ្ញាណកម្រិតមូលដ្ឋាន)
*   **Requirement (V6.2.7)**: Paste functionality must NOT be disabled on password fields to ensure seamless compatibility with Password Managers (LastPass, 1Password, Bitwarden).
*   **Requirement (V6.4.2)**: "Security Questions" (e.g., "What is your pet's name?") are strictly prohibited as account recovery mechanisms due to extreme social engineering vulnerabilities.
*   **Code Review Verification**: 
    - Frontend reviewers must ensure no `onPaste` event blockers exist natively or within UI components for password input fields.
    - Password recovery architectures MUST default to a time-limited OTP, secure magic link, or hardware token flow.

## 4. API & Web Services Security (សុវត្ថិភាព API និង Web Services - V4 & V10)
*   **Requirement (V4.4.1)**: All WebSocket connections must upgrade to `WSS://` (Secure WebSockets). Plain text `WS://` is banned in production environments.
*   **Requirement (V10.4.1)**: When operating as an Identity Provider or OAuth Authorization Server, all Redirect URIs must be strictly whitelisted and validated server-side.
*   **Code Review Verification**: 
    - Enforce TLS termination at the ingress controller or load balancer.
    - Verify `application.yml` and Keycloak configurations rigidly stipulate strict redirect URI arrays. Wildcard domains (`*.example.com`) are banned for redirect URIs.

## 5. Software Procurement Standard (លក្ខខណ្ឌតម្រូវសម្រាប់ "ការទិញសូហ្វវែរ")
*   **Concept**: "Software MUST comply with ASVS 5.0 Level 1".
*   **Execution**: This is the unified legal and technical threshold ensuring secure deliverables from any external contractors, guaranteeing they do not ship inherently vulnerable applications.

---

## ✅ Final Reviewer Checklist (សរុបសេចក្តីអំពី Checklist ចុងក្រោយ)

Every Pull Request and Release Candidate MUST pass the following checklist to achieve Level 1 compliance:

- [ ] **Parameterized Query 100%:** Are all database queries verifiably parameterized to mathematically eliminate SQL Injection?
- [ ] **No Exposed Context:** Is the `.git` folder comprehensively blocked from the deployment artifact, and is application Debug Mode definitively disabled in Production?
- [ ] **Authentication Strengths:** Does the application enforce a minimum 8-character password, permit clipboard pasting, implement brute-force protections (throttle/lockout), and absolutely avoid the use of "Security Questions"?
- [ ] **Documented Security Decisions:** Are brief architectural security decisions (e.g., Threat Models, cryptography choices) transparently documented within the repository documentation?
- [ ] **Vulnerability-Free Dependencies:** Are all 3rd-party libraries (Maven, NPM) continuously scanned via pipeline tools (e.g., Snyk, OWASP Dependency Check) and verified to be free of critical/high known CVEs?
