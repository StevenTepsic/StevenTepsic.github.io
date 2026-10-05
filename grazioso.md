---
title: Grazioso Salvare Dashboard
---
{% include nav.html %}

# Grazioso Salvare Dashboard: Databases

**Course:** CS 340, Client/Server Development (C-3 term, 2026)

## Code

- [Original code](https://github.com/StevenTepsic/StevenTepsic.github.io/tree/main/code/grazioso/original)
- [Enhanced code (validation, aggregation pipeline)](https://github.com/StevenTepsic/StevenTepsic.github.io/tree/main/code/grazioso/enhanced)

## Narrative

The Grazioso Salvare dashboard is a database application I built in CS-340, Client/Server Development, during the C-3 term of 2026. It's two files: a Python CRUD module called `CRUD_Python_Module.py` that talks to a MongoDB database through PyMongo, and a Jupyter notebook called `ProjectTwoDashboard.ipynb` that turns that data into an interactive dashboard using Dash. The dashboard lets a user filter animal shelter records by rescue type, browse them in a sortable table, see a breed breakdown in a pie chart, and click a row to see that animal's location on a map. It was built for Grazioso Salvare, a fictional company that trains rescue animals, so the point of the tool is helping their staff pick out good candidate animals from a shelter's records.

I included this artifact because it's my strongest example of database work, and my Module One plan mapped it to the Databases category. The original version worked, but it had real gaps. The CRUD module's create() method had a bug where it wrote straight to a hardcoded collection instead of using `self.collection`, which only worked by accident because the collection was always named animals anyway. None of the CRUD methods checked the type of what they were given, so bad input either failed with a confusing MongoDB error or silently did nothing. And the dashboard's breed-count chart pulled every matching animal record back from the database and let the charting library count them in Python, instead of letting MongoDB do that work.

For this enhancement, I added type validation to all four CRUD methods, so bad input now fails immediately with a clear error instead of reaching MongoDB or getting silently dropped. I fixed the create() bug while I was in there. I added a new aggregate() method to the CRUD class so it can run MongoDB aggregation pipelines, which it couldn't do before. And I rebuilt the breed-count chart around a real aggregation pipeline: it matches the current filter, groups by breed, counts, and sorts, all inside MongoDB, so the database only sends back one row per breed instead of every matching animal. I also pulled the filter-building logic, which used to be typed out twice, into one shared function that both the table and the chart use now.

This enhancement was planned to cover Outcomes 1 and 2, and I think it does. Outcome 1 is about building solutions that support organizational decision-making for a diverse audience, and that's exactly what the aggregation pipeline does. It turns a table of individual animal records into summary numbers Grazioso Salvare's staff could actually use to decide which dogs fit which rescue program, instead of making them scroll through raw rows themselves. Outcome 2 is about communicating clearly with stakeholders, and for this artifact that shows up less in the code itself and more in how I explained it. Every change has a comment next to it in plain language covering not just what the code does but why, and this narrative is doing the same job for a non-technical reader. I don't have any updates to my outcome-coverage plan. Category One and Category Two are already done and graded, and this one still lines up with what I mapped out in Module One. To be specific about how fully: I consider Outcome 2 fully met. I consider Outcome 1 met in design but only partly proven, because I couldn't run the dashboard against real data to confirm the breed chart shows staff what they need.

Professor Sanford's feedback on this milestone didn't ask for any changes. It said the changes improved the app, and it noted that I appropriately distinguished code-level verification from live testing instead of claiming testing I couldn't complete. I kept that distinction in this final version. I still haven't run the dashboard against real data, and nothing in this narrative says otherwise.

The biggest challenge on this one wasn't the code, it was not having a live environment to test against. My original plan was to stand up a local MongoDB instance, load the real data, and test every change against a running dashboard, the same way I did for Category One and Category Two. I got partway into setting that up, troubleshooting MongoDB Database Tools so I could import the data, before I ran into the real problem: I had already deleted the AAC dataset CSV after finishing CS-340, and once that course dropped off Brightspace, I lost access to the Codio page it had lived in too. Without either one, testing against a live dashboard wasn't possible.

So I made the changes directly, based on reading the existing code carefully instead of testing against a live run. I confirmed both files still parse correctly, confirmed the new functions exist where they should, and confirmed the old logic was actually replaced and not just duplicated alongside it. That's real verification, but it isn't the same as watching the dashboard run with real data, and I'd rather say that plainly than claim I tested something I couldn't.

What I learned from this had less to do with MongoDB and more to do with working around a constraint I didn't choose. I would have rather tested this live. I couldn't, so I did the next best thing and made sure the reasoning behind every change was sound and documented well enough that someone else could pick it up and verify it later. If I get a working environment and the dataset back running it for real is the first thing I'll do.

The skills I used most on this project were MongoDB aggregation pipelines, input validation, refactoring duplicated logic into one shared function, and writing comments in plain language so a non-technical reader can follow the reasoning.
