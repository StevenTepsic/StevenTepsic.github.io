---
title: Breakout Game
---
{% include nav.html %}

# Breakout Game: Algorithms and Data Structures

**Course:** CS 330, Computational Graphics and Visualization (C-2 term, 2026)

## Code

- [Original code (77 individual bricks)](https://github.com/StevenTepsic/StevenTepsic.github.io/tree/main/code/breakout/original)
- [Enhanced code (vector and spatial grid)](https://github.com/StevenTepsic/StevenTepsic.github.io/tree/main/code/breakout/enhanced)
- [Performance benchmark](https://github.com/StevenTepsic/StevenTepsic.github.io/blob/main/code/breakout/enhanced/`collision_benchmark.cpp`)

## Narrative

My Breakout game is a C++/OpenGL project I built in CS 330, Computational Graphics and Visualization, during the C-2 term of 2026. It's a Minecraft-themed take on Breakout: a paddle, a ball, and a 77-brick level built out of grass, dirt, stone, and gold textures, each with different hit points.

I picked this artifact because the original code had a clear algorithms problem worth fixing. The level's 77 bricks were each their own named variable, and every frame the game ran through all 77 of them one by one calling CheckCollision, no matter where the ball actually was. That's exactly the kind of brute-force approach Outcome 3 asks me to recognize and redesign. For the enhancement, I moved all 77 bricks into a single vector, then built a spatial grid on top of it: an 11 by 7 array that maps each brick's position to one cell. Now the collision check only looks at the ball's current cell and the 8 cells around it, at most 9 checks instead of 77, every single frame. The draw loop got simplified the same way, just looping over the vector instead of naming every brick one by one.

This met Outcome 3: designing and evaluating a computing solution using algorithmic principles, and managing the trade-off in the design choice. The trade-off here is real. The grid costs a bit of memory and setup work, but it saves a lot more in collision checks every frame, and that gap only grows if the level gets bigger. My outcome coverage didn't change from my Module One plan. I set out to cover Outcome 3 with this artifact, and that's exactly what happened. I consider it fully met, and the benchmark I describe next puts numbers on that trade-off.

Professor Sanford's feedback on this milestone said replacing the 77 bricks with a vector and a spatial grid was a meaningful algorithm and data structure improvement, and suggested measuring the original 77 collision checks per frame against the grid, especially as the level gets bigger. I did that after the milestone. I wrote a small standalone program, `collision_benchmark.cpp`, that uses the same overlap test and grid constants as the game, without OpenGL. It runs 200,000 random ball positions against levels of 77, 770, 7,700, and 77,000 bricks, once checking every brick and once checking only the 3 by 3 block of grid cells around the ball. It also confirms that both methods find the same collisions. The number of checks per frame is the clearest result. The original does one check per brick, so it does 77, 770, 7,700, and 77,000. The grid does about 8 no matter how big the level is. On my machine, built with optimizations, the time per frame went from 79 to 35 nanoseconds at 77 bricks, from 588 to 34 at 770 bricks, from 5,661 to 39 at 7,700 bricks, and from 57,058 to 46 at 77,000 bricks. That's 2.3 times faster at the real level size and about 1,242 times faster at 77,000 bricks. At 77 bricks, both versions are far too fast to change the frame rate, so I'm not claiming the game runs faster today. The benefit is that the grid's cost stays flat as the level grows, while the original's grows with every brick.

The coding itself went pretty smoothly, mostly because the level turned out to already be laid out on a perfect grid. Every brick is exactly 0.18 units wide with no gaps between them, so I didn't need anything complicated like a hash map of buckets. A brick's x and y position maps directly to one column and row, so a plain 2D array works fine. That was a good reminder that the right data structure depends on the actual shape of your data, not just picking the fanciest option available.

The real challenge wasn't the algorithm, it was my build environment. I'm on Visual Studio Community 2026, and the project file was still targeting the v143 platform toolset from the 2022 version I originally used to code it. VS2026 doesn't install v143 by default, so the build failed right away with a missing toolset error. Visual Studio's own Setup Assistant is supposed to fix that automatically, but it opened empty and didn't offer to do anything. I had to go into the project file directly and change the platform toolset to v145, which is what VS2026 actually ships with. It wasn't a coding problem at all, but it was a real lesson in how much a C++ project depends on the exact tooling it was built with, and that sometimes the fix is in the project configuration, not the code.

The skills I used most on this project were choosing a data structure that fits the actual shape of the data, managing pointer safety when a grid points into a vector, measuring a change before claiming it helps, and fixing build configuration problems.

## Benchmark results

Results from my machine, built with optimizations. The original approach does one check per brick per frame. The grid checks the 3 by 3 block of cells around the ball.

| Bricks | Checks per frame (original) | Checks per frame (grid) | ns per frame (original) | ns per frame (grid) | Speedup |
|---|---|---|---|---|---|
| 77 | 77.0 | 7.6 | 79 | 35 | 2.3x |
| 770 | 770.0 | 8.4 | 588 | 34 | 17.1x |
| 7,700 | 7,700.0 | 8.4 | 5,661 | 39 | 146.6x |
| 77,000 | 77,000.0 | 8.5 | 57,058 | 46 | 1,241.7x |
