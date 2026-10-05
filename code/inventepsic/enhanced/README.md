 InvenTepsic - Inventory Management App
================================================================================
--------------------------------------------------------------------------------
 APP SUMMARY
--------------------------------------------------------------------------------

InvenTepsic is an Android inventory management application built for small
business owners and warehouse staff who need a straightforward, mobile-first
way to track stock without the overhead of an enterprise system. The core
user need driving the design was simple: a single employee should be able to
log in, see exactly what's in stock, add or adjust items on the floor, and
get notified the moment something runs out, all from their phone.

The app addresses three specific pain points: secure, role-separated access
to inventory data; a clean, readable grid that makes stock levels immediately
visible without any training; and automatic low-stock alerts via SMS so
restocking action can happen before a zero-quantity item causes a disruption
in operations.

--------------------------------------------------------------------------------
 UI DESIGN AND USER-CENTERED DECISIONS
--------------------------------------------------------------------------------

The app required five screens to fully support user needs: a Login screen,
a Database (grid) screen, an Add Item screen, a Notifications/SMS screen,
and a Search screen. A persistent bottom navigation bar ties them together
so users can move between core functions without losing context.

Every UI decision was made with a specific user action in mind. The login
screen uses a single card layout with clearly separated username/password
fields and two visually distinct buttons (filled for Log In, outlined for
Create Account) so a first-time user immediately understands both paths
without reading any instructions. The password field uses inputType=
textPassword to enforce dot masking, which is both a security requirement
and a user expectation. The database grid uses color-coded quantity values
(green for in-stock, red for zero) so a manager scanning the list can spot
problems instantly without reading every row. The Notifications screen was
designed around two states (permission granted and permission denied)
so the UI responds meaningfully to the user's choice rather than silently
failing or crashing when SMS access is declined.

The designs were successful because they followed Material Design 3
conventions that Android users already recognize: floating action buttons
for primary add actions, AlertDialogs for destructive confirmations, and
contextual toasts for brief status feedback. Familiarity lowers the learning
curve for any new user.

--------------------------------------------------------------------------------
 CODING APPROACH AND STRATEGIES
--------------------------------------------------------------------------------

I built the app in a deliberate order: database layer first, then login,
then the grid and CRUD wiring, then SMS permission handling. Each layer
depended on the one before it, so building them in sequence meant I could
test each piece in isolation before adding complexity on top.

The most important structural decision was separating the database schema
(DatabaseContract), the database logic (InventoryDbHelper), the data model
(InventoryItem), and the UI coordination (DatabaseActivity) into distinct
classes with single responsibilities. This kept each class small, testable,
and easy to reason about. When I needed to add SMS-on-edit functionality
late in development (firing an alert the moment a user edits an item to
zero rather than only on initial permission grant), the refactor was a
single method call added to showEditQuantityDialog() because the SMS logic
was already isolated in a static method in NotificationsActivity. That kind
of composability is something I'll carry into every project going forward.

Using the AndroidX ActivityResultContracts API for runtime permission
handling instead of the older onRequestPermissionsResult() pattern was a
deliberate choice to write current, non-deprecated Android code. The pattern
is cleaner, more testable, and is the direction the Android ecosystem is
moving, and learning it on this project rather than the legacy approach means
the skill transfers directly to professional Android work.

--------------------------------------------------------------------------------
 TESTING APPROACH
--------------------------------------------------------------------------------

Testing was done primarily through the Android Emulator using targeted
manual test cases at each development phase rather than all at once at the
end. After building the database layer, I added a temporary Log.d block in
onCreate() to confirm insert and read operations before wiring up any UI.
After building login, I tested empty fields, wrong credentials, duplicate
usernames, and successful registration in sequence. After wiring the grid,
I tested all four CRUD operations: adding items, editing quantities,
deleting rows, and confirming the empty state appeared correctly when the
list was cleared. For SMS, I tested both the grant and deny paths explicitly,
confirmed the denied path left the rest of the app fully functional, and
verified actual SMS messages arrived in the emulator's Messages app.

This incremental testing approach matters because bugs caught close to where
they were introduced are dramatically faster to fix than bugs discovered
after multiple layers of code have been built on top of them. It also
revealed a real issue early: the sendSmsForOutOfStockItems() method was
initially only called at the moment of permission grant, which meant future
zero-quantity edits didn't trigger alerts. Catching this during SMS testing
led to the static method refactor that made alert-sending reusable from
DatabaseActivity.

--------------------------------------------------------------------------------
 INNOVATION AND CHALLENGES
--------------------------------------------------------------------------------

One of the more interesting design challenges in this project was figuring
out how to present inventory data in a way that felt immediately usable
without prior training. Early in the UI planning phase, I looked at how
existing inventory and warehouse apps handle this problem. Apps like
Sortly and inFlow Inventory lean heavily on card-based layouts with large
product images, which works well for retail but adds visual noise in a
warehouse context where speed of scanning matters more than aesthetics.
Spreadsheet-style tools like Airtable are powerful but present too steep a
learning curve for a floor employee checking stock on a phone.

The design decision that came out of that comparison was to use a compact,
column-aligned grid with color-coded quantity values rather than cards or
a spreadsheet view. The column headers stay pinned above the RecyclerView
so the user always knows what they're looking at as they scroll, and the
red-versus-green quantity coloring means a manager can identify problem rows
without reading a single number. This was a deliberate middle ground between
the visual richness of consumer inventory apps and the raw density of
enterprise tools, optimized for the specific context of a small team on a
warehouse floor.

The same comparison process shaped the navigation structure. Most dedicated
inventory apps bury the notification or alert system several taps deep.
Surfacing alerts as a first-class bottom navigation destination in InvenTepsic
means a user can go directly from seeing an out-of-stock row in the grid to
viewing and acting on the alert in one tap, which reduces the friction that
causes notification features to go ignored in practice.

--------------------------------------------------------------------------------
 STRONGEST COMPONENT
--------------------------------------------------------------------------------

The SMS permission and notification system is the component where the
knowledge and skill demonstrated most clearly exceeded the baseline
requirement. The rubric required a runtime permission request and graceful
degradation if denied. What was delivered goes further in three meaningful
ways:

First, the permission check on screen entry (checkExistingPermissionState)
prevents the permission card from re-appearing on every visit after the user
has already granted access, which is the behavior a real user would expect.

Second, the sendOutOfStockSms() method is implemented as a public static
utility rather than inline logic, which makes it callable from any screen
without duplicating the SmsManager logic or the permission guard. This is
a real software design decision, not just boilerplate.

Third, the method is invoked proactively from DatabaseActivity whenever a
quantity edit drops an item to zero, meaning alerts fire in real time as
data changes, not only at the specific moment the user visits the
Notifications screen. This closes a meaningful gap between "technically
meets the requirement" and "works the way a real inventory notification
system should work."
