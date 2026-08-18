# Remove Background and Enhance Floating Effect for ActiveJourneyBar

The goal is to remove the solid background area behind the "In Transit" floating bar and update the bar itself to use a "glass" effect, making it consistent with the rest of the app's design and truly floating over the content.

## User Review Required

> [!IMPORTANT]
> Removing the bottom padding from the main navigation host will allow screen content to flow behind the floating bar. This creates a better "floating" effect, but some screen content might be partially obscured by the bar. I will ensure the bar has a glass effect so content is still faintly visible, but you may need to add padding to specific screens if important buttons are covered.

## Proposed Changes

### UI Components

#### [MODIFY] [ActiveJourneyBar.kt](file:///Users/leads/StudioProjects/MetroTransit/app/src/main/java/com/example/metrotransit/ui/screens/ActiveJourneyBar.kt)
- Update the `Surface` to use `extendedColors.glass` and `extendedColors.glassBorder`.
- This will change the solid white background to a translucent glass effect.

### Navigation and Layout

#### [MODIFY] [NavGraph.kt](file:///Users/leads/StudioProjects/MetroTransit/app/src/main/java/com/example/metrotransit/navigation/NavGraph.kt)
- Remove the `padding(bottom = ActiveJourneyBarHeight)` from the `Box` containing `TicketNavHost`. This eliminates the "gray bar" background area seen in the screenshot.
- Add additional bottom padding to the `ActiveJourneyBar` modifier to lift it slightly off the navigation bar, enhancing the floating appearance.

## Verification Plan

### Manual Verification
- Deploy the app and start a journey.
- Observe that the `ActiveJourneyBar` now has a translucent glass background.
- Verify that screen content (like lists or text) now flows behind the bar instead of being cut off by a solid gray background.
- Confirm the bar is properly floating with even padding from the bottom and sides.
