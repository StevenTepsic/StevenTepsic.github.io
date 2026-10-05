---
title: InvenTepsic
---
{% include nav.html %}

# InvenTepsic: Software Design and Engineering

**Course:** CS 360, Mobile Architecture and Programming (June 2026)

## Code

- [Original code (plaintext passwords, manual entry)](https://github.com/StevenTepsic/StevenTepsic.github.io/tree/main/code/inventepsic/original)
- [Enhanced code (barcode scanning, salted PBKDF2 hashing)](https://github.com/StevenTepsic/StevenTepsic.github.io/tree/main/code/inventepsic/enhanced)
- [Automated tests (19 tests)](https://github.com/StevenTepsic/StevenTepsic.github.io/blob/main/code/inventepsic/enhanced/app/src/androidTest/java/com/zybooks/inventoryapp/InventoryDbHelperTest.java)

## Narrative

InvenTepsic is an Android inventory app I built in CS 360 (Mobile Architecture and Programming) in June 2026, term C-3. It lets a user log in, add and search inventory items by SKU, description, quantity, and location, and receive SMS alerts when an item's quantity reaches zero.

I chose this artifact because it let me demonstrate two things I care about: solid engineering practice and a security mindset. When I reviewed my original code, I found a real problem. The login system stored and compared passwords as plain text, so anyone with database access could read them. There was also a usability gap: every inventory item had to be typed in by hand, with no faster way to enter it. Fixing both let me show that I can read my own past code critically, catch a real security flaw, and fix it properly instead of patching around it.

For the enhancement, I added a UPC column to the inventory schema and built CameraX and ML Kit barcode scanning into AddItemActivity. Now scanning a product's barcode fills in the UPC field instead of requiring manual entry. I also added the same scanning and permission code to SearchActivity, so a barcode can be scanned to search instead of typed in. That part goes slightly beyond my original Module One plan, but it reused the permission handling I had already built, so it did not add much extra work. For the security fix, I replaced plaintext passwords with a salted PBKDF2 hash. Each user gets a random salt, and their password is hashed with that salt before it is stored. Login re-hashes the entered password and compares it to the stored hash, so the app never compares raw passwords.

I tested all of it myself. I created a new account and logged in with the correct password. I confirmed that a wrong password is rejected. I confirmed that the barcode scan fills in the UPC field correctly. I confirmed that manual entry still works if camera permission is declined. I also went back through the existing inventory CRUD, search, and SMS alert features to make sure none of them broke. Everything passed.

Professor Sanford's feedback on this milestone said my manual testing showed attention to functionality, security, permissions, and regressions, and suggested building automated tests around authentication and database operations to back it up. I did that after the milestone. I wrote a test class with 19 tests and ran them, and all 19 passed. The authentication tests cover correct and wrong passwords, unknown users, duplicate usernames, and a SQL injection attempt. Three more tests read the raw users table to confirm that no stored value contains the plaintext password, that a hash and salt are stored, and that two users with the same password get different stored values. The rest cover adding, reading, sorting, updating, and deleting items, the out-of-stock query, and saving and updating the UPC. To keep the tests from touching real app data, I added a second constructor to InventoryDbHelper that lets a test use its own database file. The tests only cover the database layer. I still checked the login screen, the add-item screen, and the camera scanning by hand.

I met both outcomes I planned for in Module One. Outcome 4, using well-founded and innovative techniques to deliver value, is covered by the CameraX and ML Kit scanning, since it is a real new capability added to an existing app. Outcome 5, a security mindset that finds and fixes real vulnerabilities, is covered by the password hashing fix. Nothing changed about which outcomes I am covering. The only update from my original plan is adding the scan feature to SearchActivity as well, and that is just an extension of the same Outcome 4 work, not a new outcome. I do want to be specific about Outcome 5. I fixed how passwords are stored, and the tests check that directly, but I would not call the whole app fully secured. The hashing uses 65,536 iterations, and current OWASP guidance for PBKDF2 with SHA-256 is much higher, so that is a number I would raise.

The security fix taught me more than I expected. I figured adding a salt column and hashing the password would stay contained inside createUser() and checkCredentials(). It did not. Once the password was no longer stored as plain text, I could not simply compare it inside the SQL query the way the old code did. A hashed password cannot be matched directly in the database, since the same password produces a different hash for every user's salt. I had to rewrite checkCredentials() to look up the user by username only, pull the stored hash and salt, and perform the actual comparison in Java. That was a good lesson: a security fix like this is not just "encrypt the field." It changes the shape of the logic around it.

I also hit a few smaller bugs while wiring up the camera permission flow across two activities. An unclosed XML tag broke the layout parser without any obvious error. A button ID in the XML did not match what the Java code was looking for. A local variable accidentally shadowed a class field with the same name, so that field stayed null even though everything compiled fine. None of these were hard to understand once I found them, but they were easy to miss, and tracking them down was a good reminder of how exactly the XML and Java have to match up in Android.

The skills I used most on this project were Android database design, secure password storage, integrating camera and machine learning libraries, debugging across XML and Java, and writing tests that check a security property directly instead of only checking that login works.

## Automated test results

All 19 tests passed.

![InventoryDbHelperTest results, 19 of 19 passed](/assets/inventepsic-tests.png)
