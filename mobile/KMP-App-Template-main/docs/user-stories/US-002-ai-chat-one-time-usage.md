# User Story: One-Time AI Chat Usage for Non-Subscribed Users

## Story ID
**US-002**

## Title
Allow non-subscribed users to use AI Chat feature once before requiring subscription

## User Type
Non-subscribed users (free users)

## Description
As a non-subscribed user, I want to try the AI Chat feature once so that I can experience its value before deciding to subscribe.

## Acceptance Criteria

### AC1: First-Time Usage
- **Given** I am a non-subscribed user
- **When** I open the AI Chat screen for the first time
- **Then** I should be able to send one message and receive a response
- **And** the chat should work normally for this first interaction

### AC2: Usage Tracking
- **Given** I have used the AI Chat feature once
- **When** I try to send a second message
- **Then** I should see a message indicating I've reached my free usage limit
- **And** I should be prompted to subscribe to continue using the feature

### AC3: Subscription Bypass
- **Given** I have an active subscription
- **When** I use the AI Chat feature
- **Then** I should have unlimited usage
- **And** no usage limits should apply

### AC4: Clear Messaging
- **Given** I've reached my free usage limit
- **When** I view the AI Chat screen
- **Then** I should see a clear message explaining the limit
- **And** I should see a prominent "Subscribe" button to upgrade

### AC5: Usage Persistence
- **Given** I've used my free AI Chat once
- **When** I close and reopen the app
- **Then** my usage status should be remembered
- **And** I should still see the subscription prompt

## Technical Requirements

### Data Storage
- Store `hasUsedFreeAIChat` boolean flag in PreferencesManager
- Key: `"has_used_free_ai_chat"`
- Default: `false`

### Subscription Check
- Use `RevenueCatRepository.hasActiveSubscription()` to check subscription status
- If subscribed: Allow unlimited usage, ignore usage flag
- If not subscribed: Check usage flag before allowing chat

### UI Components
- Show usage limit message when limit reached
- Display subscription CTA button
- Disable input field when limit reached
- Show remaining usage indicator (e.g., "1 free message remaining" → "0 free messages remaining")

### Business Logic
- Check subscription status first (highest priority)
- If subscribed: Allow unlimited usage
- If not subscribed: Check if `hasUsedFreeAIChat` is true
- If false: Allow one message, then set flag to true
- If true: Block further usage, show subscription prompt

## Implementation Details

### Files to Modify
1. `PreferencesManager.kt` - Add methods to track AI chat usage
2. `AndroidPreferencesManager.kt` - Implement Android storage
3. `IOSPreferencesManager.kt` - Implement iOS storage
4. `ChatViewModel.kt` - Add subscription check and usage tracking
5. `AIChatScreen.kt` - Add UI for usage limit and subscription prompt

### Flow Diagram
```
User opens AI Chat
    ↓
Check subscription status
    ↓
Is subscribed?
    ├─ Yes → Allow unlimited usage
    └─ No → Check hasUsedFreeAIChat flag
            ├─ False → Allow one message → Set flag to true
            └─ True → Show subscription prompt → Block usage
```

## Edge Cases

1. **User subscribes after using free chat**: Usage flag should be ignored, unlimited access granted
2. **User unsubscribes**: Usage flag remains, but subscription check takes precedence
3. **App reinstall**: Usage flag resets (new installation = new free trial)
4. **Multiple devices**: Usage is tracked per device (local storage)

## Success Metrics
- Conversion rate: % of users who subscribe after using free AI Chat
- Engagement: Number of users who try the free AI Chat feature
- Retention: Users who return after free usage

## Priority
**High** - Important for user acquisition and conversion

## Dependencies
- RevenueCat integration for subscription checking
- PreferencesManager for local storage

## Notes
- This is a one-time free usage, not a recurring free tier
- Usage is tracked locally (per device)
- Consider resetting usage flag if user subscribes and then unsubscribes (future enhancement)
