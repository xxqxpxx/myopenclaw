# User Story: Forgot Password Feature

## Story ID
**US-003**

## Title
Allow users to reset their password via email when they forget it

## User Type
All users (authenticated and unauthenticated)

## Description
As a user who has forgotten their password, I want to request a password reset email so that I can regain access to my account without needing to contact support.

## Acceptance Criteria

### AC1: Access Forgot Password Screen
- **Given** I am on the Sign In screen
- **When** I click "Forgot Password?"
- **Then** I should be navigated to the Forgot Password screen
- **And** I should see a form to enter my email address

### AC2: Email Input Validation
- **Given** I am on the Forgot Password screen
- **When** I enter an invalid email (empty, no @ symbol, malformed)
- **Then** I should see a validation error message
- **And** the "Send Reset Link" button should be disabled or show an error

### AC3: Send Reset Email
- **Given** I have entered a valid email address
- **When** I click "Send Reset Link"
- **Then** The app should send a password reset email via Firebase
- **And** I should see a loading indicator while the request is processing

### AC4: Success State
- **Given** I have successfully requested a password reset
- **When** The email is sent successfully
- **Then** I should see a success message
- **And** I should see the email address where the reset link was sent
- **And** I should see options to "Back to Sign In" or "Resend Email"

### AC5: Error Handling
- **Given** I have entered an email address
- **When** The password reset request fails (network error, invalid email, etc.)
- **Then** I should see a clear error message explaining what went wrong
- **And** I should be able to try again

### AC6: Email Not Found Handling
- **Given** I enter an email that doesn't exist in the system
- **When** I request a password reset
- **Then** Firebase should still return success (for security - prevents email enumeration)
- **And** I should see the same success message (to prevent revealing if email exists)

### AC7: Resend Email
- **Given** I have already sent a reset email
- **When** I click "Resend Email"
- **Then** I should be able to send another reset email
- **And** The form should reset to allow entering email again

### AC8: Navigation
- **Given** I am on the Forgot Password screen
- **When** I click "Back to Sign In"
- **Then** I should be navigated back to the Sign In screen
- **And** The email field should be preserved (if possible)

## Technical Requirements

### Firebase Integration
- Use Firebase `sendPasswordResetEmail()` method
- Handle Firebase error codes appropriately
- Security: Don't reveal if email exists (Firebase handles this)

### State Management
- Loading state while sending email
- Success state after email sent
- Error state if request fails
- Idle state for initial form

### Error Messages
- Invalid email: "Please enter a valid email address"
- Network error: "Network error. Please check your connection and try again."
- Too many requests: "Too many requests. Please try again later."
- Generic error: "Failed to send reset email. Please try again."

### UI Components
- Email input field with validation
- Send Reset Link button (disabled during loading)
- Success message with email confirmation
- Error message display
- Back to Sign In button
- Resend Email option

## Implementation Details

### Files to Modify
1. `ForgotPasswordScreen.kt` - Connect to ViewModel, handle states
2. `ForgotPasswordViewModel.kt` - Already exists, verify it's working correctly
3. `AuthManager.kt` - Already has `sendPasswordResetEmail`, verify error handling
4. `NavGraph.kt` - Already connected, verify navigation

### Flow Diagram
```
User clicks "Forgot Password?"
    ↓
Navigate to ForgotPasswordScreen
    ↓
User enters email
    ↓
User clicks "Send Reset Link"
    ↓
Validate email format
    ↓
Call Firebase sendPasswordResetEmail()
    ↓
Show loading indicator
    ↓
Success → Show success message
Error → Show error message
    ↓
User can resend or go back to Sign In
```

## Edge Cases

1. **User enters email that doesn't exist**: Firebase returns success anyway (security feature)
2. **Network failure**: Show network error, allow retry
3. **Invalid email format**: Show validation error before API call
4. **Too many requests**: Show rate limit error
5. **User closes app before clicking reset link**: Email still valid, can use link later

## Success Metrics
- Password reset email delivery rate
- User completion rate (users who click reset link)
- Time to password reset completion
- Error rate and types

## Priority
**High** - Essential for user account recovery

## Dependencies
- Firebase Authentication (already integrated)
- Email service configured in Firebase Console
- Password reset email template configured

## Notes
- Firebase automatically handles email enumeration protection
- Password reset links expire after a set time (configurable in Firebase)
- Users can request multiple reset emails (rate limited by Firebase)
- The reset link opens in a browser/web view, not in-app
