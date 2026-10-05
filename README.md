# Steven Tepsic

Computer Science (B.S., Software Engineering concentration), Southern New Hampshire University. I'm aiming for software engineering and red team penetration testing work.

This is my CS-499 capstone ePortfolio. It shows three projects from earlier courses and what I changed in each one: software design and engineering, algorithms and data structures, and databases. All three are course projects, and I say so up front. Grazioso Salvare, the client in the third project, is fictional.

## Start Here

1. **Professional Self-Assessment**
   <!-- TODO: link the self-assessment page. It goes first, per the Module Seven requirements. -->
2. [Code Review Video](#code-review-video) (my walkthrough of the original code and my enhancement plans)
3. **Projects**
   - [InvenTepsic: Software Design and Engineering](#1-inventepsic-software-design-and-engineering)
   - [Breakout Game: Algorithms and Data Structures](#2-breakout-game-algorithms-and-data-structures)
   - [Grazioso Salvare Dashboard: Databases](#3-grazioso-salvare-dashboard-databases)
4. [Course Outcomes](#course-outcomes)
5. [Instructor Feedback](#instructor-feedback)

## About Me

I'm a Computer Science student at SNHU with a Software Engineering concentration, finishing my B.S. with a 3.966 GPA. I expect to graduate in Spring 2027.

I'm also a Fractional AI Programming Developer at Rewyndr, a Pittsburgh startup. Most of my day is building and reviewing code, including AI-assisted code, across AWS, React, and RESTful application work. That job is where a lot of my coursework shows up in practice, from password hashing to RESTful design.

I want to work in software engineering, and I'm especially interested in red team penetration testing. I like building things that work, and I like finding out why they break. I also work full time at Target in Pittsburgh, where I've spent about a decade in leadership roles.

## Code Review Video

This is my informal code review of all three original projects, recorded before I started the enhancements. It walks through how each one works, what I found wrong with it, and what I planned to change. It runs about 30 minutes.

<iframe width="560" height="315" src="https://www.youtube.com/embed/yUilzRHOY7I" title="CS-499 Code Review" frameborder="0" allowfullscreen></iframe>

[Watch on YouTube](https://youtu.be/yUilzRHOY7I)

## Projects

Each project below shows the original version, the enhanced version, and my narrative. The narrative explains why I picked the project, what I learned, and which course outcomes it covers.

### 1. InvenTepsic: Software Design and Engineering

**Original course:** CS 360, Mobile Architecture and Programming (June 2026)

InvenTepsic is an Android inventory app. A user can log in, add and search inventory items by SKU, description, quantity, and location, and get SMS alerts when an item hits zero.

**Before:** Passwords were stored and compared as plain text, and every item had to be typed in by hand.

**After:**
- Added a UPC column to the inventory schema.
- Built CameraX and ML Kit barcode scanning into the add-item screen, so scanning a product fills in the UPC field instead of typing it.
- Added the same scanning to the search screen.
- Replaced plaintext passwords with salted PBKDF2 hashes. Each user gets a random salt, and login re-hashes the entered password and compares it to the stored hash.

**Skills shown:** Android development, camera and ML integration, secure password storage, manual regression testing.

**Course outcomes covered:** 4 (well-founded and innovative techniques) and 5 (a security mindset).

**What I learned:** The security fix was bigger than I expected. Once passwords were hashed, I could no longer compare them inside the SQL query, so I had to rewrite the login check to look up the user first and compare the hash in Java.

<!-- TODO: link original code, enhanced code, and narrative for this project. -->

### 2. Breakout Game: Algorithms and Data Structures

**Original course:** CS 330, Computational Graphics and Visualization (C-2 term, 2026)

A Minecraft-themed Breakout game written in C++ and OpenGL.

**Before:** The level's 77 bricks were each their own named variable. Every frame, the game made 77 collision calls and 77 draw calls, typed out one by one, no matter where the ball was.

**After:**
- Replaced the 77 brick variables with a single `vector<Brick>`.
- Built a spatial grid on top of that vector, so the ball only gets checked against the bricks near it instead of all 77 every frame.
- Replaced the 77-call collision chain and the 77-call draw chain with short loops.

**Skills shown:** Data structure design, spatial partitioning, reasoning about performance trade-offs, C++ memory safety.

**Course outcome covered:** 3 (algorithmic principles and design trade-offs).

**What I learned:** The grid stores pointers into the vector, so the vector can never resize after the grid is built. Reserving all 77 slots up front avoids that. At this level size you won't see a frame rate difference. The point is that the design holds up as the level gets bigger.

<!-- TODO: link original code, enhanced code, and narrative for this project. -->

### 3. Grazioso Salvare Dashboard: Databases

**Original course:** CS 340, Advanced Programming Concepts (C-3 term, 2026)

A dashboard for a fictional rescue-animal training company. A Python CRUD module talks to MongoDB through PyMongo, and a Dash dashboard lets a user filter shelter animals by rescue type, browse them in a table, see a breed chart, and view an animal's location on a map.

**Before:** The CRUD methods accepted any input. `create()` wrote to a hardcoded collection instead of `self.collection`. The breed chart pulled every matching animal back from the database and counted them in Python.

**After:**
- Added type validation to all four CRUD methods, so bad input fails with a clear error.
- Fixed the `create()` collection bug.
- Added an `aggregate()` method so the module can run MongoDB aggregation pipelines.
- Rebuilt the breed chart around a real aggregation pipeline, so MongoDB does the counting and sends back one row per breed.
- Pulled the duplicated filter logic into one shared function.

**Skills shown:** MongoDB aggregation pipelines, input validation, refactoring, writing plain-language code comments.

**Course outcomes covered:** 1 (supporting organizational decision-making) and 2 (clear communication with stakeholders).

**What I learned and what I couldn't do:** I no longer have the dataset or the course's Codio environment, so I couldn't run the dashboard against real data. I checked the changes by reading the code carefully, confirming both files parse, and confirming the old logic was fully replaced. That isn't the same as a live test, and I say so in my narrative.

<!-- TODO: link original code, enhanced code, and narrative for this project. -->

## Course Outcomes

| Outcome | What it covers | Where I show it |
|---|---|---|
| 1 | Collaborative environments and organizational decision-making | Grazioso Salvare dashboard |
| 2 | Professional-quality communication | Grazioso Salvare dashboard, narratives, code comments |
| 3 | Algorithmic principles and design trade-offs | Breakout game |
| 4 | Well-founded and innovative techniques | InvenTepsic |
| 5 | A security mindset | InvenTepsic |

## Skills

**Languages:** Java, C++, Python, JavaScript
**Mobile and graphics:** Android, CameraX, ML Kit, OpenGL
**Databases:** SQLite, MongoDB, PyMongo
**Web:** Dash, Angular, Express
**Security:** Password hashing (PBKDF2), secure coding, OWASP Dependency-Check

## Instructor Feedback

Every graded piece of CS-499 so far, with Prof. Sanford's feedback.

| Assignment | Grade |
|---|---|
| 1-2 Module One Assignment | 45 / 45 (A) |
| 2-1 Journal: What Makes a Productive Code Review? | 14.33 / 15 (A) |
| 2-2 Milestone One: Code Review | 40 / 40 (A) |
| 3-1 Journal: Marketing With ePortfolios and Artifact Update | 14.33 / 15 (A) |
| 3-2 Milestone Two: Enhancement One, Software Design and Engineering | 40 / 40 (A) |
| 4-1 Journal: Career Choice and Artifact Update | 12.98 / 15 (B+) |
| 4-2 Milestone Three: Enhancement Two, Algorithms and Data Structure | 40 / 40 (A) |
| 5-1 Journal: Computer Science Trends and Artifact Update | 15 / 15 (A) |
| 5-2 Milestone Four: Enhancement Three, Databases | 40 / 40 (A) |

### Enhancement milestones

**Module One plan (45/45).** The plan was called "complete, detailed, and professionally developed." My artifacts lined up with the three categories, and the plans showed technical depth in Android development, barcode scanning, password hashing, spatial grid collision detection, MongoDB aggregation, indexing, validation, and secure database use. The advice was to carry that same level of specificity into the actual enhancements.

**Milestone One code review (40/40).** The review covered software design and engineering, algorithms and data structures, and databases, using relevant code from my Android inventory app, my C++ work, and my Python/MongoDB implementation. The presentation was organized, professional, and about 30 minutes long.

**InvenTepsic (40/40).** The submission met expectations. Barcode scanning and salted PBKDF2 password hashing were a meaningful improvement, and my testing showed attention to functionality, security, permissions, and regressions. Next step: build automated tests around authentication and database operations to back up the manual testing.

**Breakout (40/40).** The submission met expectations. Replacing 77 individually managed bricks with a vector and spatial grid was called a meaningful algorithm and data-structure improvement, and the code matched my narrative. Next step: add performance measurements comparing the original 77 collision checks per frame to the grid approach, especially as the level size increases.

**Grazioso Salvare dashboard (40/40).** The submission met expectations. CRUD validation, corrected collection handling, and database-side aggregation improved the app, and moving the breed counting into MongoDB cuts unnecessary data retrieval. The feedback also said I "appropriately distinguished code-level verification from live testing, demonstrating good judgment rather than claiming testing that could not be completed."

### Journals

**2-1 (14.33/15).** Good explanation of code review and its role in functionality, security, efficiency, and quality assurance across the SDLC, with a systematic checklist approach for my three artifacts. Adding more personal or professional experience would deepen the reflection.

**3-1 (14.33/15).** The journal answered all four reflection questions, and the checkpoint document gave a complete status update for all three categories. Points came off because the required APA citations and references were missing.

**4-1 (12.98/15).** The feedback called out a useful connection between my coursework and my new Fractional AI Programming Developer role, including AWS, React, RESTful applications, password hashing, and independent AI study. It also liked my point that developers don't need to know everything, but do need to be able to research and learn well. Points came off because the Part Two Status Checkpoints table was left out of the document.

**5-1 (15/15).** A thoughtful analysis of emerging trends tied directly to my professional experience and future development work, going past description to how these technologies change developers' responsibilities. The checkpoint table clearly showed my progress, instructor feedback, and the work that remains.

## Next Steps

All three enhancements are submitted and graded. Before the final ePortfolio:
- **InvenTepsic:** add automated tests around authentication and database operations.
- **Breakout:** measure the 77-check approach against the grid as the level grows.
- **Grazioso Salvare:** no action items from feedback. If I can get a working MongoDB instance and the dataset back, I'll run the dashboard live to close the gap between code-level verification and a real test.

## Contact

Steven Tepsic
stevent@rewyndr.com
