// collision_benchmark.cpp
// Standalone timing harness for the Breakout brick-collision change.
// No OpenGL needed. Build:  g++ -O2 -std=c++17 collision_benchmark.cpp -o collision_benchmark
//
// It copies the same overlap test that Circle::CheckCollision uses, but
// leaves out the bounce and health changes so every frame does the same work.
// That keeps the timing about the lookup strategy and nothing else.

#include <algorithm>
#include <chrono>
#include <cmath>
#include <cstdio>
#include <random>
#include <vector>
using namespace std;

struct Brick { float x, y, width; bool on = true; };

// Same constants as MainCode.cpp
const int   GRID_COLS      = 11;
const float GRID_CELL_SIZE = 0.18f;
const float GRID_ORIGIN_X  = -0.99f;
const float GRID_ORIGIN_Y  = -0.24f;

// The same overlap test as Circle::CheckCollision, minus the response.
static inline bool Overlaps(float bx, float by, float radius, const Brick* b)
{
    if (!b->on) return false;
    float half = b->width / 2;
    return bx + radius > b->x - half && bx - radius < b->x + half &&
           by + radius > b->y - half && by - radius < b->y + half;
}

int main()
{
    const float radius = 0.03f;
    const int   FRAMES = 200000;
    const int   levelRows[] = { 7, 70, 700, 7000 };   // 77, 770, 7,700, 77,000 bricks

    printf("%-10s %-14s %-14s %-12s %-12s %-8s\n",
           "bricks", "tests/frame", "tests/frame", "ns/frame", "ns/frame", "speedup");
    printf("%-10s %-14s %-14s %-12s %-12s\n", "", "(original)", "(grid)", "(original)", "(grid)");

    for (int rows : levelRows)
    {
        // Build the level: GRID_COLS x rows bricks, no gaps, like the real layout.
        vector<Brick> bricks;
        bricks.reserve(GRID_COLS * rows);   // never resize after the grid is built
        for (int r = 0; r < rows; r++)
            for (int c = 0; c < GRID_COLS; c++)
                bricks.push_back({ GRID_ORIGIN_X + (c + 0.5f) * GRID_CELL_SIZE,
                                   GRID_ORIGIN_Y + (r + 0.5f) * GRID_CELL_SIZE,
                                   GRID_CELL_SIZE });

        vector<vector<Brick*>> grid(rows, vector<Brick*>(GRID_COLS, nullptr));
        for (Brick& b : bricks) {
            int col = (int)floor((b.x - GRID_ORIGIN_X) / GRID_CELL_SIZE);
            int row = (int)floor((b.y - GRID_ORIGIN_Y) / GRID_CELL_SIZE);
            grid[row][col] = &b;
        }

        // Same random ball positions for both methods.
        mt19937 rng(499);
        uniform_real_distribution<float> rx(GRID_ORIGIN_X, GRID_ORIGIN_X + GRID_COLS * GRID_CELL_SIZE);
        uniform_real_distribution<float> ry(GRID_ORIGIN_Y, GRID_ORIGIN_Y + rows * GRID_CELL_SIZE);
        vector<pair<float,float>> pos(FRAMES);
        for (auto& p : pos) p = { rx(rng), ry(rng) };

        long long testsOriginal = 0, testsGrid = 0, hitsOriginal = 0, hitsGrid = 0;

        // Original approach: check every brick, every frame.
        auto t0 = chrono::steady_clock::now();
        for (auto& p : pos)
            for (Brick& b : bricks) { testsOriginal++; hitsOriginal += Overlaps(p.first, p.second, radius, &b); }
        auto t1 = chrono::steady_clock::now();

        // Grid approach: check the 3x3 cells around the ball.
        for (auto& p : pos) {
            int bc = min(max((int)floor((p.first  - GRID_ORIGIN_X) / GRID_CELL_SIZE), 0), GRID_COLS - 1);
            int br = min(max((int)floor((p.second - GRID_ORIGIN_Y) / GRID_CELL_SIZE), 0), rows - 1);
            for (int r = br - 1; r <= br + 1; r++) {
                if (r < 0 || r >= rows) continue;
                for (int c = bc - 1; c <= bc + 1; c++) {
                    if (c < 0 || c >= GRID_COLS) continue;
                    Brick* b = grid[r][c];
                    if (b) { testsGrid++; hitsGrid += Overlaps(p.first, p.second, radius, b); }
                }
            }
        }
        auto t2 = chrono::steady_clock::now();

        double nsOrig = chrono::duration<double, nano>(t1 - t0).count() / FRAMES;
        double nsGrid = chrono::duration<double, nano>(t2 - t1).count() / FRAMES;

        // Sanity check: both methods must find the same collisions.
        if (hitsOriginal != hitsGrid)
            printf("WARNING: hit counts differ (%lld vs %lld)\n", hitsOriginal, hitsGrid);

        printf("%-10zu %-14.1f %-14.1f %-12.0f %-12.0f %-8.1fx\n",
               bricks.size(), (double)testsOriginal / FRAMES, (double)testsGrid / FRAMES,
               nsOrig, nsGrid, nsOrig / nsGrid);
    }
    return 0;
}
