# User Story: Persistent Login Storage

## US-001: Remember Me & Auto-Login

### Overview
As a user, I want the app to remember my login session so that I don't have to enter my credentials every time I open the app.

---

## User Story

**As a** myOpenClaw user
**I want** the app to remember my login session
**So that** I can quickly access the app without re-entering my credentials each time

---

## Acceptance Criteria

### AC1: Session Persistence
- [ ] When I successfully sign in, my session is saved locally
- [ ] The auth token and user information are securely stored
- [ ] Session data persists across app restarts

### AC2: Auto-Login on App Launch
- [ ] When I open the app, it checks for an existing valid session
- [ ] If a valid session exists, I am automatically signed in
- [ ] I am navigated directly to the home screen (skipping sign-in)

### AC3: Remember Me Option
- [ ] The sign-in screen has a "Remember Me" checkbox
- [ ] When enabled, the session persists indefinitely (until logout)
- [ ] When disabled, the session is cleared on app close

### AC4: Token Validation
- [ ] On auto-login, the saved token is validated with Firebase
- [ ] If the token is expired, attempt silent refresh
- [ ] If refresh fails, redirect to sign-in screen

### AC5: Logout Clears Session
- [ ] When I log out, all stored credentials are cleared
- [ ] Next app launch shows the sign-in screen

---

## Technical Implementation

### Components to Modify/Create

1. **SessionManager** (New - Common)
   - Check for existing session on startup
   - Validate stored tokens
   - Handle auto-login flow
   - Clear session on logout

2. **PreferencesManager** (Existing)
   - Already has `saveAuthToken`, `saveRememberMe` methods
   - Add secure storage for refresh tokens

3. **App.kt** (Modify)
   - Add splash/loading screen during session check
   - Route to appropriate screen based on session state

4. **SignInViewModel** (Modify)
   - Save session data on successful login
   - Respect "Remember Me" preference

5. **Navigation** (Modify)
   - Support conditional start destination

---

## Flow Diagram

```
┌─────────────────────────────────────────────────────────────┐
│                      APP LAUNCH                              │
└─────────────────────────────────────────────────────────────┘
                             │
                             ▼
┌─────────────────────────────────────────────────────────────┐
│                   Splash Screen                              │
│              (Check Session State)                           │
└─────────────────────────────────────────────────────────────┘
                             │
              ┌──────────────┴──────────────┐
              │                             │
              ▼                             ▼
┌─────────────────────┐        ┌─────────────────────┐
│  Has Saved Session? │        │   No Saved Session  │
│        YES          │        │                     │
└─────────────────────┘        └─────────────────────┘
              │                             │
              ▼                             │
┌─────────────────────┐                     │
│  Validate Token     │                     │
│  with Firebase      │                     │
└─────────────────────┘                     │
              │                             │
    ┌─────────┴─────────┐                   │
    │                   │                   │
    ▼                   ▼                   ▼
┌─────────┐       ┌─────────┐        ┌─────────────┐
│ Valid   │       │ Invalid │        │  Sign In    │
│ Token   │       │ Token   │        │  Screen     │
└─────────┘       └─────────┘        └─────────────┘
    │                   │
    │                   ▼
    │            ┌─────────────┐
    │            │ Clear Data  │
    │            │ → Sign In   │
    │            └─────────────┘
    ▼
┌─────────────────────┐
│    Home Screen      │
│  (Auto-logged in)   │
└─────────────────────┘
```

---

## Security Considerations

1. **Token Storage**
   - Use Android Keystore / iOS Keychain for sensitive data (future enhancement)
   - Currently using SharedPreferences/NSUserDefaults (acceptable for MVP)

2. **Token Refresh**
   - Firebase handles token refresh automatically
   - Tokens expire after 1 hour but refresh silently

3. **Clear on Logout**
   - All stored credentials must be cleared on explicit logout

---

## Priority
**High** - Core UX improvement for user retention

## Estimated Effort
- Implementation: 2-4 hours
- Testing: 1-2 hours

---

## Related Files

- `composeApp/src/commonMain/kotlin/com/myopenclaw/data/local/PreferencesManager.kt`
- `composeApp/src/commonMain/kotlin/com/myopenclaw/data/auth/AuthManager.kt`
- `composeApp/src/commonMain/kotlin/com/myopenclaw/ui/viewmodel/auth/SignInViewModel.kt`
- `composeApp/src/commonMain/kotlin/com/myopenclaw/App.kt`
- `composeApp/src/commonMain/kotlin/com/myopenclaw/ui/navigation/NavGraph.kt`
