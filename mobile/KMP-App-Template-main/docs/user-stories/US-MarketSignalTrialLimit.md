# User Story: Market Signal Trial Limit for New Users

## Story ID: SW-SIGNAL-001
## Epic: Subscription Monetization
## Priority: High
## Sprint: Current

---

## User Story

**As a** product owner,
**I want** newly registered users to only see today's market signals for one day after registration,
**So that** they can experience the value of the feature and be motivated to subscribe for continued access.

---

## Acceptance Criteria

### AC1: Trial Period Tracking
- [ ] The system stores the user's registration timestamp locally
- [ ] The system calculates if 24 hours have passed since registration
- [ ] Trial period is based on local device time for simplicity

### AC2: Market Signals Display During Trial
- [ ] New users (within 24 hours of registration) see the full "Today's Market Signals" section on the home page
- [ ] Signals display normally with pair, type, price range, and action (BUY/SELL/HOLD)
- [ ] Users can click on signals to view details during trial

### AC3: Upsell Banner After Trial Expires
- [ ] After 24 hours, the market signals section is replaced with an upsell banner
- [ ] The upsell banner displays:
  - Lock icon or premium badge
  - "Unlock Market Signals" title
  - Brief value proposition text
  - "Subscribe Now" CTA button
- [ ] CTA navigates to subscription plans screen

### AC4: Subscription Override
- [ ] Users with active subscription always see full market signals
- [ ] Subscription status is checked via RevenueCat
- [ ] No trial limit applies to subscribed users

---

## Technical Specifications

### Components to Modify/Create

1. **PreferencesManager** - Add registration timestamp storage
   - `saveUserRegistrationDate(timestamp: Long)`
   - `getUserRegistrationDate(): Long?`

2. **MarketSignalTrialUseCase** - New use case
   - Checks if user is within trial period (24 hours)
   - Considers subscription status override

3. **SubscriptionUpsellBanner** - New Composable component
   - Attractive upsell banner matching app theme
   - Navigation to subscription screen

4. **HomeScreenNew** - Modify existing
   - Accept trial status parameter
   - Conditionally render signals or upsell banner

5. **SignalsViewModel** - Modify existing
   - Expose trial status state
   - Check trial period on init

### Data Flow

```
App Launch
    ↓
SessionManager.checkSession()
    ↓
Save registration date if first time
    ↓
HomeScreen loads
    ↓
SignalsViewModel checks:
  1. RevenueCat subscription status
  2. Trial period (24h from registration)
    ↓
If subscribed OR within trial → Show signals
Else → Show upsell banner
```

---

## Edge Cases

| Scenario | Expected Behavior |
|----------|-------------------|
| User reinstalls app | Registration date lost, treated as new user (shows signals for 1 day) |
| User changes device time | Based on device time - acceptable trade-off |
| User was subscribed, subscription lapses | Show upsell banner (trial expired) |
| New user, no internet | Show upsell banner (can't verify subscription) |
| User subscribes during trial | Continue showing signals indefinitely |

---

## UI/UX Specifications

### Upsell Banner Design
- Background: Dark card matching `Color(0xFF2C3544)`
- Premium accent: Green gradient or `Green2` color
- Lock icon: 48dp, centered
- Title: "Unlock Daily Market Signals" - 18sp, Bold, White
- Subtitle: "Get real-time BUY, SELL, HOLD recommendations from our AI analysis" - 14sp, White 70%
- CTA Button: "Subscribe Now" - Green2 background, pill shape
- Card height: ~160dp with padding

---

## Definition of Done

- [ ] All acceptance criteria met
- [ ] Code reviewed and approved
- [ ] Unit tests for trial period calculation
- [ ] Integration tested on Android and iOS
- [ ] No regression in existing signal display functionality
- [ ] Analytics event for upsell banner impressions (future enhancement)

---

## Notes

- Trial period of 24 hours is configurable for future A/B testing
- Consider extending to 3 days based on conversion metrics
- Future enhancement: Show countdown timer during trial
