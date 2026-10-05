#include <GL/glew.h>
#include <GLFW\glfw3.h>
#include "linmath.h"
#include <stdlib.h>
#include <stdio.h>
#include <conio.h>
#include <iostream>
#include <vector>
#include <windows.h>
#include <time.h>
#include <string.h>

#ifndef STB_IMAGE_IMPLEMENTATION
#define STB_IMAGE_IMPLEMENTATION
#include "stb_image.h"
#endif

// GLM Math Header inclusions
#include <glm/glm.hpp>
#include <glm/gtx/transform.hpp>
#include <glm/gtc/type_ptr.hpp>

using namespace std;

const float DEG2RAD = 3.14159 / 180;
const char* g_ModelName = "model";
const char* g_ColorValueName = "objectColor";
const char* g_TextureValueName = "objectTexture";
const char* g_UseTextureName = "bUseTexture";
const char* g_UseLightingName = "bUseLighting";
// Initial size of the player paddle
const glm::vec2 PLAYER_SIZE(100.0f, 20.0f);
// Initial velocity of the player paddle
const float PLAYER_VELOCITY(250.0f);

void processInput(GLFWwindow* window);
struct TEXTURE_INFO
{
	std::string tag;
	uint32_t ID;
};


// total number of loaded textures
int m_loadedTextures;
// loaded textures info
TEXTURE_INFO m_textureIDs[16];

enum BRICKTYPE { REFLECTIVE, DESTRUCTABLE };
enum ONOFF { ON, OFF };

bool CreateGLTexture(const char* filename, std::string tag)
{
	int width = 0;
	int height = 0;
	int colorChannels = 0;
	GLuint textureID = 0;

	// indicate to always flip images vertically when loaded
	stbi_set_flip_vertically_on_load(true);

	// try to parse the image data from the specified image file
	unsigned char* image = stbi_load(
		filename,
		&width,
		&height,
		&colorChannels,
		0);

	// if the image was successfully read from the image file
	if (image)
	{
		std::cout << "Successfully loaded image:" << filename << ", width:" << width << ", height:" << height << ", channels:" << colorChannels << std::endl;

		glGenTextures(1, &textureID);
		glBindTexture(GL_TEXTURE_2D, textureID);

		// set the texture wrapping parameters
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
		// set texture filtering parameters
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

		// if the loaded image is in RGB format
		if (colorChannels == 3)
			glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB8, width, height, 0, GL_RGB, GL_UNSIGNED_BYTE, image);
		// if the loaded image is in RGBA format - it supports transparency
		else if (colorChannels == 4)
			glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, image);
		else
		{
			std::cout << "Not implemented to handle image with " << colorChannels << " channels" << std::endl;
			return false;
		}

		// generate the texture mipmaps for mapping textures to lower resolutions
		glGenerateMipmap(GL_TEXTURE_2D);

		// free the image data from local memory
		stbi_image_free(image);
		glBindTexture(GL_TEXTURE_2D, 0); // Unbind the texture

		// register the loaded texture and associate it with the special tag string
		m_textureIDs[m_loadedTextures].ID = textureID;
		m_textureIDs[m_loadedTextures].tag = tag;
		m_loadedTextures++;



		return true;
	}

	std::cout << "Could not load image:" << filename << std::endl;

	// Error loading the image
	return false;
}

void BindGLTextures()
{
	for (int i = 0; i < m_loadedTextures; i++)
	{
		// bind textures on corresponding texture units
		glActiveTexture(GL_TEXTURE0 + i);
		glBindTexture(GL_TEXTURE_2D, m_textureIDs[i].ID);
	}
}

/***********************************************************
  *  LoadSceneTextures()
  *
  *  This method is used for preparing the 3D scene by loading
  *  the shapes, textures in memory to support the 3D scene
  *  rendering
  ***********************************************************/
void LoadSceneTextures()
{
	/*** STUDENTS - add the code BELOW for loading the textures that ***/
	/*** will be used for mapping to objects in the 3D scene. Up to  ***/
	/*** 16 textures can be loaded per scene. Refer to the code in   ***/
	/*** the OpenGL Sample for help.                                 ***/

	bool bReturn = false;

	//utilities moved to project file for local textures
	bReturn = CreateGLTexture("./Utilities/textures/grass.jpg", "grass");

	bReturn = CreateGLTexture("./Utilities/textures/wood.jpg", "wood");

	bReturn = CreateGLTexture("./Utilities/textures/stone.jpg", "stone");

	bReturn = CreateGLTexture("./Utilities/textures/dirt.jpg", "dirt");

	bReturn = CreateGLTexture("./Utilities/textures/gold.jpg", "gold");

	bReturn = CreateGLTexture("./Utilities/textures/Lava004.png", "lava");

	bReturn = CreateGLTexture("./Utilities/textures/paddle.png", "paddle");

	bReturn = CreateGLTexture("./Utilities/textures/background.png", "background");

	// after the texture image data is loaded into memory, the
	// loaded textures need to be bound to texture slots - there
	// are a total of 16 available slots for scene textures
}

/***********************************************************
 *  FindTextureSlot()
 *
 *  This method is used for getting a slot index for the previously
 *  loaded texture bitmap associated with the passed in tag.
 ***********************************************************/
int FindTextureSlot(std::string tag)
{
	int textureSlot = -1;
	int index = 0;
	bool bFound = false;

	while ((index < m_loadedTextures) && (bFound == false))
	{
		if (m_textureIDs[index].tag.compare(tag) == 0)
		{
			textureSlot = index;
			bFound = true;
		}
		else
			index++;
	}

	return(textureSlot);
}

class Paddle  //player controlled paddle moves with arrow keys
{
public:
	float x, y;
	float width, height;
	float speed = 0.02f;
	string texture;

	Paddle(float xx, float yy, float w, float h, string tex)
	{
		x = xx; y = yy; width = w; height = h; texture = tex;
	}

	void Move(int direction) // -1 = left, 1 = right
	{
		float newX = x + (speed * direction);

		// halt at edges
		if (newX - width / 2 > -1.0f && newX + width / 2 < 1.0f)
			x = newX;
	}

	void Draw()
	{
		float halfW = width / 2;
		float halfH = height / 2;

		int slot = FindTextureSlot(texture);
		if (slot >= 0)
		{
			glEnable(GL_TEXTURE_2D);
			glBindTexture(GL_TEXTURE_2D, m_textureIDs[slot].ID);
		}

		glColor3f(1.0f, 1.0f, 1.0f);
		glBegin(GL_POLYGON);
		glTexCoord2f(1, 1); glVertex2f(x + halfW, y + halfH);
		glTexCoord2f(1, 0); glVertex2f(x + halfW, y - halfH);
		glTexCoord2f(0, 0); glVertex2f(x - halfW, y - halfH);
		glTexCoord2f(0, 1); glVertex2f(x - halfW, y + halfH);
		glEnd();

		if (slot >= 0)
			glDisable(GL_TEXTURE_2D);
	}
};

class Brick  //represents a single brick in layout
{
public:
	float red, green, blue;
	float x, y, width;
	BRICKTYPE brick_type;
	ONOFF onoff;
	string texture;
	int health;

	Brick(BRICKTYPE bt, float xx, float yy, float ww, float r, float g, float b)
	{
		brick_type = bt; x = xx; y = yy; width = ww;
		red = r; green = g; blue = b;
		onoff = ON;
		texture = "";  // default empty
		health = 1;  // default 1 hit
	};

	// New constructor overload that accepts a texture tag
	Brick(BRICKTYPE bt, float xx, float yy, float ww, string tex, float r, float g, float b)
	{
		brick_type = bt; x = xx; y = yy; width = ww;
		red = r; green = g; blue = b;
		onoff = ON;
		texture = tex;
		if (tex == "stone") { health = 20; };  // ← stone takes 20 hits
		if (tex == "gold") { health = 2; };  // ← gold takes 2 hits
	};



	void drawBrick()
	{
		if (onoff == ON)
		{
			double halfside = width / 2;

			int slot = FindTextureSlot(texture);

			// if stone and damaged, tint it darker
			if (texture == "gold" && health == 1)
				glColor3f(0.4f, 0.4f, 0.4f);  // ← darkened on second hit
			else
				glColor3f(1.0f, 1.0f, 1.0f);

			if (slot >= 0)
			{
				glActiveTexture(GL_TEXTURE0);
				glEnable(GL_TEXTURE_2D);
				glBindTexture(GL_TEXTURE_2D, m_textureIDs[slot].ID);
			}

			glBegin(GL_POLYGON);
			glTexCoord2f(1, 1); glVertex2d(x + halfside, y + halfside);
			glTexCoord2f(1, 0); glVertex2d(x + halfside, y - halfside);
			glTexCoord2f(0, 0); glVertex2d(x - halfside, y - halfside);
			glTexCoord2f(0, 1); glVertex2d(x - halfside, y + halfside);
			glEnd();

			if (slot >= 0)
				glDisable(GL_TEXTURE_2D);
		}
	}
};


class Circle  //class for ball that game is played with
{
public:
	float red, green, blue;
	float radius;
	float x;
	float y;
	float speed = 0.015;
	int direction; // 1=up 2=right 3=down 4=left 5 = up right   6 = up left  7 = down right  8= down left
	bool stuck = true;
	bool justBounced = false;

	Circle(double xx, double yy, double rr, int dir, float rad, float r, float g, float b)
	{
		x = xx;
		y = yy;
		radius = rr;
		red = r;
		green = g;
		blue = b;
		radius = rad;
		direction = dir;
	}

	bool CheckCollision(Brick* brk)  // ← now returns bool
	{
		if (brk->onoff == OFF) return false;
		if (justBounced) return false;

		float halfW = brk->width / 2;
		float halfH = brk->width / 2;

		if (x + radius > brk->x - halfW && x - radius < brk->x + halfW &&
			y + radius > brk->y - halfH && y - radius < brk->y + halfH)
		{
			float overlapLeft = (x + radius) - (brk->x - halfW);
			float overlapRight = (brk->x + halfW) - (x - radius);
			float overlapBottom = (y + radius) - (brk->y - halfH);
			float overlapTop = (brk->y + halfH) - (y - radius);

			float minOverlap = min({ overlapLeft, overlapRight, overlapBottom, overlapTop });

			if (minOverlap == overlapLeft || minOverlap == overlapRight)
			{
				if (direction == 2) direction = 4;
				else if (direction == 4) direction = 2;
				else if (direction == 5) direction = 6;
				else if (direction == 6) direction = 5;
				else if (direction == 7) direction = 8;
				else if (direction == 8) direction = 7;
			}
			else
			{
				if (direction == 1) direction = 3;
				else if (direction == 3) direction = 1;
				else if (direction == 5) direction = 7;
				else if (direction == 7) direction = 5;
				else if (direction == 6) direction = 8;
				else if (direction == 8) direction = 6;
			}

			if (minOverlap == overlapLeft)        x = brk->x - halfW - radius;
			else if (minOverlap == overlapRight)  x = brk->x + halfW + radius;
			else if (minOverlap == overlapBottom) y = brk->y - halfH - radius;
			else if (minOverlap == overlapTop)    y = brk->y + halfH + radius;

			// health system
			brk->health--;
			if (brk->health <= 0)
				brk->onoff = OFF;

			justBounced = true;
			return true;  // ← hit occurred
		}
		return false;
	}

	void CheckCollisionPaddle(Paddle* p)
	{
		if (stuck) return;

		if (x > p->x - p->width / 2 && x < p->x + p->width / 2 &&
			y - radius < p->y + p->height / 2 && y >= p->y - p->height / 2)
		{
			float hitOffset = (x - p->x) / (p->width / 2);
			if (hitOffset < -0.33f)      direction = 6;  // up-left
			else if (hitOffset > 0.33f)  direction = 5;  // up-right
			else                         direction = 1;  // straight up
		}
	}

	int GetRandomDirection()
	{
		return (rand() % 8) + 1;
	}

	void MoveOneStep()
	{
		if (stuck) return;

		float vx = 0, vy = 0;

		if (direction == 1) { vy = speed; }
		else if (direction == 2) { vx = speed; }
		else if (direction == 3) { vy = -speed; }
		else if (direction == 4) { vx = -speed; }
		else if (direction == 5) { vx = speed; vy = speed; }
		else if (direction == 6) { vx = -speed; vy = speed; }
		else if (direction == 7) { vx = speed; vy = -speed; }
		else if (direction == 8) { vx = -speed; vy = -speed; }

		// left/right walls — flip and clamp
		if (x + vx < -1.0f + radius)
		{
			vx = -vx;
			x = -1.0f + radius;  // ← push out of wall
		}
		else if (x + vx > 1.0f - radius)
		{
			vx = -vx;
			x = 1.0f - radius;   // ← push out of wall
		}

		// top wall — flip and clamp
		if (y + vy > 1.0f - radius)
		{
			vy = -vy;
			y = 1.0f - radius;   // ← push out of wall
		}

		x += vx;
		y += vy;

		// update direction to match new velocity
		if (vx == 0 && vy > 0) direction = 1;
		else if (vx > 0 && vy == 0) direction = 2;
		else if (vx == 0 && vy < 0) direction = 3;
		else if (vx < 0 && vy == 0) direction = 4;
		else if (vx > 0 && vy > 0)  direction = 5;
		else if (vx < 0 && vy > 0)  direction = 6;
		else if (vx > 0 && vy < 0)  direction = 7;
		else if (vx < 0 && vy < 0)  direction = 8;
	}

	void DrawCircle()
	{
		glColor3f(red, green, blue);
		glBegin(GL_POLYGON);
		for (int i = 0; i < 360; i++) {
			float degInRad = i * DEG2RAD;
			glVertex2f((cos(degInRad) * radius) + x, (sin(degInRad) * radius) + y);
		}
		glEnd();
	}
};

void DrawBackground()
{
	int slot = FindTextureSlot("background");
	if (slot >= 0)
	{
		glActiveTexture(GL_TEXTURE0);
		glEnable(GL_TEXTURE_2D);
		glBindTexture(GL_TEXTURE_2D, m_textureIDs[slot].ID);
	}

	glColor3f(1.0f, 1.0f, 1.0f);
	glBegin(GL_POLYGON);
	glTexCoord2f(0, 1); glVertex2f(-1.0f, 1.0f);  // top left
	glTexCoord2f(1, 1); glVertex2f(1.0f, 1.0f);  // top right
	glTexCoord2f(1, 0); glVertex2f(1.0f, -1.0f);  // bottom right
	glTexCoord2f(0, 0); glVertex2f(-1.0f, -1.0f);  // bottom left
	glEnd();

	if (slot >= 0)
		glDisable(GL_TEXTURE_2D);
}


vector<Circle> world;

Circle ball(0.0f, -0.80f, 0.05, 1, 0.05, 1.0, 1.0, 1.0);

Paddle player(0.0f, -0.85f, 0.3f, 0.05f, "paddle");

// ---- spatial grid for brick collision lookups ----
// The level is 11 columns by 7 rows. Every brick is exactly 0.18 units
// wide with no gaps between neighbors. So each brick's position maps to
// exactly one grid cell, and a plain 2D array of pointers works fine
// here. We don't need something more general like a hash map of buckets.
const int GRID_COLS = 11;
const int GRID_ROWS = 7;
const float GRID_CELL_SIZE = 0.18f;
const float GRID_ORIGIN_X = -0.99f;   // left edge of column 0
const float GRID_ORIGIN_Y = -0.24f;   // bottom edge of row 0

int ColForX(float x)
{
	int col = (int)floor((x - GRID_ORIGIN_X) / GRID_CELL_SIZE);
	if (col < 0) col = 0;
	if (col >= GRID_COLS) col = GRID_COLS - 1;
	return col;
}

int RowForY(float y)
{
	int row = (int)floor((y - GRID_ORIGIN_Y) / GRID_CELL_SIZE);
	if (row < 0) row = 0;
	if (row >= GRID_ROWS) row = GRID_ROWS - 1;
	return row;
}

vector<Brick> bricks;
vector<vector<Brick*>> grid(GRID_ROWS, vector<Brick*>(GRID_COLS, nullptr));

// Fills the grid from the bricks vector. Call this once, after bricks
// is fully populated. If a brick gets destroyed later (onoff = OFF), it
// can just stay in the grid. CheckCollision() already skips OFF bricks,
// so there's nothing extra to clean up.
void BuildGrid()
{
	for (Brick& b : bricks)
	{
		int col = ColForX(b.x);
		int row = RowForY(b.y);
		grid[row][col] = &b;
	}
}

int main(void) {
	srand(time(NULL));

	if (!glfwInit()) {
		exit(EXIT_FAILURE);
	}
	glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 2);
	glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 0);
	GLFWwindow* window = glfwCreateWindow(720, 720, "8-2 Assignment", NULL, NULL);
	if (!window) {
		glfwTerminate();
		exit(EXIT_FAILURE);
	}
	glfwMakeContextCurrent(window);
	glfwSwapInterval(1);

	//initialize GLEW
	glewExperimental = GL_TRUE;
	if (glewInit() != GLEW_OK) {
		exit(EXIT_FAILURE);
	}



	// Call LoadSceneTextures before the game loop
	LoadSceneTextures();

	// Reserve before any push_back. BuildGrid() below stores pointers
	// into this vector. If the vector resizes after that, every one of
	// those pointers goes bad.
	bricks.reserve(77);   // 7 rows x 11 columns

	// --- Grass row (11 columns) ---
	bricks.push_back(Brick(DESTRUCTABLE, -0.90f, -0.15f, 0.18f, "grass", 1, 1, 1));
	bricks.push_back(Brick(DESTRUCTABLE, -0.72f, -0.15f, 0.18f, "grass", 1, 1, 1));
	bricks.push_back(Brick(DESTRUCTABLE, -0.54f, -0.15f, 0.18f, "grass", 1, 1, 1));
	bricks.push_back(Brick(DESTRUCTABLE, -0.36f, -0.15f, 0.18f, "grass", 1, 1, 1));
	bricks.push_back(Brick(DESTRUCTABLE, -0.18f, -0.15f, 0.18f, "grass", 1, 1, 1));
	bricks.push_back(Brick(DESTRUCTABLE, 0.00f, -0.15f, 0.18f, "grass", 1, 1, 1));
	bricks.push_back(Brick(DESTRUCTABLE, 0.18f, -0.15f, 0.18f, "grass", 1, 1, 1));
	bricks.push_back(Brick(DESTRUCTABLE, 0.36f, -0.15f, 0.18f, "grass", 1, 1, 1));
	bricks.push_back(Brick(DESTRUCTABLE, 0.54f, -0.15f, 0.18f, "grass", 1, 1, 1));
	bricks.push_back(Brick(DESTRUCTABLE, 0.72f, -0.15f, 0.18f, "grass", 1, 1, 1));
	bricks.push_back(Brick(DESTRUCTABLE, 0.90f, -0.15f, 0.18f, "grass", 1, 1, 1));
	// --- Mixed row 1 ---
	bricks.push_back(Brick(DESTRUCTABLE, -0.90f, 0.03f, 0.18f, "dirt", 1, 1, 1));
	bricks.push_back(Brick(REFLECTIVE, -0.72f, 0.03f, 0.18f, "stone", 1, 1, 1));
	bricks.push_back(Brick(DESTRUCTABLE, -0.54f, 0.03f, 0.18f, "dirt", 1, 1, 1));
	bricks.push_back(Brick(DESTRUCTABLE, -0.36f, 0.03f, 0.18f, "dirt", 1, 1, 1));
	bricks.push_back(Brick(DESTRUCTABLE, -0.18f, 0.03f, 0.18f, "dirt", 1, 1, 1));
	bricks.push_back(Brick(DESTRUCTABLE, 0.00f, 0.03f, 0.18f, "dirt", 1, 1, 1));
	bricks.push_back(Brick(DESTRUCTABLE, 0.18f, 0.03f, 0.18f, "dirt", 1, 1, 1));
	bricks.push_back(Brick(REFLECTIVE, 0.36f, 0.03f, 0.18f, "stone", 1, 1, 1));
	bricks.push_back(Brick(DESTRUCTABLE, 0.54f, 0.03f, 0.18f, "dirt", 1, 1, 1));
	bricks.push_back(Brick(DESTRUCTABLE, 0.72f, 0.03f, 0.18f, "dirt", 1, 1, 1));
	bricks.push_back(Brick(DESTRUCTABLE, 0.90f, 0.03f, 0.18f, "dirt", 1, 1, 1));
	// --- Mixed row 2 ---
	bricks.push_back(Brick(REFLECTIVE, -0.90f, 0.21f, 0.18f, "stone", 1, 1, 1));
	bricks.push_back(Brick(DESTRUCTABLE, -0.72f, 0.21f, 0.18f, "dirt", 1, 1, 1));
	bricks.push_back(Brick(DESTRUCTABLE, -0.54f, 0.21f, 0.18f, "dirt", 1, 1, 1));
	bricks.push_back(Brick(REFLECTIVE, -0.36f, 0.21f, 0.18f, "stone", 1, 1, 1));
	bricks.push_back(Brick(REFLECTIVE, -0.18f, 0.21f, 0.18f, "gold", 1, 1, 1));
	bricks.push_back(Brick(REFLECTIVE, 0.00f, 0.21f, 0.18f, "stone", 1, 1, 1));
	bricks.push_back(Brick(DESTRUCTABLE, 0.18f, 0.21f, 0.18f, "dirt", 1, 1, 1));
	bricks.push_back(Brick(DESTRUCTABLE, 0.36f, 0.21f, 0.18f, "dirt", 1, 1, 1));
	bricks.push_back(Brick(REFLECTIVE, 0.54f, 0.21f, 0.18f, "stone", 1, 1, 1));
	bricks.push_back(Brick(DESTRUCTABLE, 0.72f, 0.21f, 0.18f, "dirt", 1, 1, 1));
	bricks.push_back(Brick(DESTRUCTABLE, 0.90f, 0.21f, 0.18f, "dirt", 1, 1, 1));
	// --- Mixed row 3 ---
	bricks.push_back(Brick(REFLECTIVE, -0.90f, 0.39f, 0.18f, "gold", 1, 1, 1));
	bricks.push_back(Brick(DESTRUCTABLE, -0.72f, 0.39f, 0.18f, "dirt", 1, 1, 1));
	bricks.push_back(Brick(REFLECTIVE, -0.54f, 0.39f, 0.18f, "stone", 1, 1, 1));
	bricks.push_back(Brick(REFLECTIVE, -0.36f, 0.39f, 0.18f, "gold", 1, 1, 1));
	bricks.push_back(Brick(DESTRUCTABLE, -0.18f, 0.39f, 0.18f, "dirt", 1, 1, 1));
	bricks.push_back(Brick(DESTRUCTABLE, 0.00f, 0.39f, 0.18f, "dirt", 1, 1, 1));
	bricks.push_back(Brick(REFLECTIVE, 0.18f, 0.39f, 0.18f, "stone", 1, 1, 1));
	bricks.push_back(Brick(DESTRUCTABLE, 0.36f, 0.39f, 0.18f, "dirt", 1, 1, 1));
	bricks.push_back(Brick(DESTRUCTABLE, 0.54f, 0.39f, 0.18f, "dirt", 1, 1, 1));
	bricks.push_back(Brick(REFLECTIVE, 0.72f, 0.39f, 0.18f, "stone", 1, 1, 1));
	bricks.push_back(Brick(DESTRUCTABLE, 0.90f, 0.39f, 0.18f, "dirt", 1, 1, 1));
	// --- Extra row 1 ---
	bricks.push_back(Brick(REFLECTIVE, -0.90f, 0.57f, 0.18f, "stone", 1, 1, 1));
	bricks.push_back(Brick(DESTRUCTABLE, -0.72f, 0.57f, 0.18f, "dirt", 1, 1, 1));
	bricks.push_back(Brick(REFLECTIVE, -0.54f, 0.57f, 0.18f, "gold", 1, 1, 1));
	bricks.push_back(Brick(DESTRUCTABLE, -0.36f, 0.57f, 0.18f, "dirt", 1, 1, 1));
	bricks.push_back(Brick(REFLECTIVE, -0.18f, 0.57f, 0.18f, "stone", 1, 1, 1));
	bricks.push_back(Brick(REFLECTIVE, 0.00f, 0.57f, 0.18f, "gold", 1, 1, 1));
	bricks.push_back(Brick(DESTRUCTABLE, 0.18f, 0.57f, 0.18f, "dirt", 1, 1, 1));
	bricks.push_back(Brick(REFLECTIVE, 0.36f, 0.57f, 0.18f, "stone", 1, 1, 1));
	bricks.push_back(Brick(DESTRUCTABLE, 0.54f, 0.57f, 0.18f, "dirt", 1, 1, 1));
	bricks.push_back(Brick(REFLECTIVE, 0.72f, 0.57f, 0.18f, "gold", 1, 1, 1));
	bricks.push_back(Brick(REFLECTIVE, 0.90f, 0.57f, 0.18f, "stone", 1, 1, 1));
	// --- Extra row 2 ---
	bricks.push_back(Brick(DESTRUCTABLE, -0.90f, 0.75f, 0.18f, "dirt", 1, 1, 1));
	bricks.push_back(Brick(REFLECTIVE, -0.72f, 0.75f, 0.18f, "dirt", 1, 1, 1));
	bricks.push_back(Brick(DESTRUCTABLE, -0.54f, 0.75f, 0.18f, "dirt", 1, 1, 1));
	bricks.push_back(Brick(REFLECTIVE, -0.36f, 0.75f, 0.18f, "stone", 1, 1, 1));
	bricks.push_back(Brick(REFLECTIVE, -0.18f, 0.75f, 0.18f, "dirt", 1, 1, 1));
	bricks.push_back(Brick(DESTRUCTABLE, 0.00f, 0.75f, 0.18f, "dirt", 1, 1, 1));
	bricks.push_back(Brick(REFLECTIVE, 0.18f, 0.75f, 0.18f, "stone", 1, 1, 1));
	bricks.push_back(Brick(REFLECTIVE, 0.36f, 0.75f, 0.18f, "gold", 1, 1, 1));
	bricks.push_back(Brick(DESTRUCTABLE, 0.54f, 0.75f, 0.18f, "dirt", 1, 1, 1));
	bricks.push_back(Brick(REFLECTIVE, 0.72f, 0.75f, 0.18f, "stone", 1, 1, 1));
	bricks.push_back(Brick(DESTRUCTABLE, 0.90f, 0.75f, 0.18f, "dirt", 1, 1, 1));
	// --- Extra row 3 (top) ---
	bricks.push_back(Brick(REFLECTIVE, -0.90f, 0.93f, 0.18f, "gold", 1, 1, 1));
	bricks.push_back(Brick(REFLECTIVE, -0.72f, 0.93f, 0.18f, "stone", 1, 1, 1));
	bricks.push_back(Brick(REFLECTIVE, -0.54f, 0.93f, 0.18f, "gold", 1, 1, 1));
	bricks.push_back(Brick(REFLECTIVE, -0.36f, 0.93f, 0.18f, "stone", 1, 1, 1));
	bricks.push_back(Brick(REFLECTIVE, -0.18f, 0.93f, 0.18f, "gold", 1, 1, 1));
	bricks.push_back(Brick(REFLECTIVE, 0.00f, 0.93f, 0.18f, "stone", 1, 1, 1));
	bricks.push_back(Brick(REFLECTIVE, 0.18f, 0.93f, 0.18f, "gold", 1, 1, 1));
	bricks.push_back(Brick(REFLECTIVE, 0.36f, 0.93f, 0.18f, "stone", 1, 1, 1));
	bricks.push_back(Brick(REFLECTIVE, 0.54f, 0.93f, 0.18f, "gold", 1, 1, 1));
	bricks.push_back(Brick(REFLECTIVE, 0.72f, 0.93f, 0.18f, "stone", 1, 1, 1));
	bricks.push_back(Brick(REFLECTIVE, 0.90f, 0.93f, 0.18f, "gold", 1, 1, 1));

	// Build the grid after the vector above is fully populated, and
	// after reserve(). That way nothing can resize the vector and break
	// these pointers.
	BuildGrid();

	while (!glfwWindowShouldClose(window)) {
		// ---- Game loop order ----
		// 1. Process input
		// 2. Draw background
		// 3. Move and draw world circles, check circle-circle collisions
		// 4. Reset justBounced flag
		// 5. Check ball-brick collisions, update color on hit
		// 6. Draw bricks
		// 7. Draw paddle
		// 8. Move and draw ball
		// 
		//Setup View
		float ratio;
		int width, height;
		glfwGetFramebufferSize(window, &width, &height);
		ratio = width / (float)height;
		glViewport(0, 0, width, height);
		glClear(GL_COLOR_BUFFER_BIT);

		processInput(window);

		DrawBackground();

		for (int i = 0; i < world.size(); i++)
		{
			world[i].MoveOneStep();
			world[i].DrawCircle();

			// circle-circle collision
			for (int j = i + 1; j < world.size(); j++)
			{
				float dx = world[i].x - world[j].x;
				float dy = world[i].y - world[j].y;
				float dist = sqrt(dx * dx + dy * dy);

				if (dist < world[i].radius + world[j].radius)
				{
					world[i].red = (float)(rand() % 100) / 100.0f;
					world[i].green = (float)(rand() % 100) / 100.0f;
					world[i].blue = (float)(rand() % 100) / 100.0f;
					world[j].red = world[i].red;
					world[j].green = world[i].green;
					world[j].blue = world[i].blue;

					int tempDir = world[i].direction;
					world[i].direction = world[j].direction;
					world[j].direction = tempDir;
				}
			}
		}


		//keeps ball on paddle
		if (ball.stuck) {
			ball.x = player.x;
		}
		//ends game if ball goes under
		if (ball.y - ball.radius < -1.0f)
		{
			ball.stuck = true;
			ball.x = player.x;
			ball.y = -0.80f;
		}

		// reset bounce flag FIRST
		ball.justBounced = false;
		bool hitOccurred = false;

		// Collisions. Only check the grid cells around the ball's current
		// position instead of all 77 bricks every frame. A 3x3 block is
		// enough. The ball's radius (0.05) is well under one full cell
		// width (0.18), so it can never overlap a brick more than one
		// cell away.
		int ballCol = ColForX(ball.x);
		int ballRow = RowForY(ball.y);

		for (int r = ballRow - 1; r <= ballRow + 1; r++)
		{
			if (r < 0 || r >= GRID_ROWS) continue;
			for (int c = ballCol - 1; c <= ballCol + 1; c++)
			{
				if (c < 0 || c >= GRID_COLS) continue;
				Brick* brk = grid[r][c];
				if (brk != nullptr)
					hitOccurred |= ball.CheckCollision(brk);
			}
		}
		ball.CheckCollisionPaddle(&player);

		// change ball color on hit
		if (hitOccurred)
		{
			ball.red = (float)(rand() % 100) / 100.0f;
			ball.green = (float)(rand() % 100) / 100.0f;
			ball.blue = (float)(rand() % 100) / 100.0f;
		}

		// Draw every brick. Every one is on screen regardless of where
		// the ball is, so there's nothing to spatially partition here.
		// Just loop over the vector instead of naming each brick.
		for (Brick& b : bricks)
		{
			b.drawBrick();
		}

		player.Draw();
		ball.MoveOneStep();
		ball.DrawCircle();


		glfwSwapBuffers(window);
		glfwPollEvents();
	}

	glfwDestroyWindow(window);
	glfwTerminate();
	exit(EXIT_SUCCESS);
}


void processInput(GLFWwindow* window)
{
	if (glfwGetKey(window, GLFW_KEY_ESCAPE) == GLFW_PRESS)
		glfwSetWindowShouldClose(window, true);

	// Single unified space handler
	static bool spaceWasPressed = false;
	bool spaceDown = glfwGetKey(window, GLFW_KEY_SPACE) == GLFW_PRESS;
	if (spaceDown && !spaceWasPressed)
	{
		if (ball.stuck)
		{
			ball.stuck = false;
			ball.direction = 1;
		}
		else
		{
			double r = rand() / 10000.0;
			double g = rand() / 10000.0;
			double b = rand() / 10000.0;
			Circle B(0, 0, 0, 2, 0.05, r, g, b);
			B.stuck = false;
			B.direction = (rand() % 8) + 1;
			world.push_back(B);
		}
	}
	spaceWasPressed = spaceDown;

	if (glfwGetKey(window, GLFW_KEY_LEFT) == GLFW_PRESS) { player.Move(-1); }
	if (glfwGetKey(window, GLFW_KEY_RIGHT) == GLFW_PRESS) { player.Move(1); }
}