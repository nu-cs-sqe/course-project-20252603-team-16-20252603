# Game Setup Phase — Class & Method Design

## Overview

This document covers the classes and public methods needed to fully initialize a Catan game and complete the Setup Phase (initial placement of settlements and roads, and distribution of starting resources).

---

## Dependency Graph

```
ResourceType (enum)
PlayerColor (enum)

Hex ──────────────────► ResourceType
Vertex ────────────────► Hex (adjacent hexes)
Edge ──────────────────► Vertex (two endpoints)
Board ─────────────────► Hex, Vertex, Edge
Player ────────────────► PlayerColor, ResourceType
Game ──────────────────► Board, Player
SetupPhase ────────────► Game, Vertex, Edge
```

---

## Enums

### `ResourceType`
```
BRICK, LUMBER, ORE, GRAIN, WOOL, DESERT
```

### `PlayerColor`
```
RED, BLUE, WHITE, ORANGE
```

---

## Classes

---

### `Hex`

Represents one terrain tile on the board.

| Field         | Type           | Description                                   |
|---------------|----------------|-----------------------------------------------|
| `resourceType`| `ResourceType` | The resource this hex produces                |
| `numberToken` | `int`          | Dice number (2–12, never 7); 0 for Desert     |

| Method                             | Return Type      | Description                                              |
|------------------------------------|------------------|----------------------------------------------------------|
| `Hex(ResourceType, int)`           | —                | Constructor; throws `IllegalArgumentException` if numberToken is invalid (7, <2, or >12) or if DESERT is given a token |
| `getResourceType()`                | `ResourceType`   | Returns the hex's resource type                          |
| `getNumberToken()`                 | `int`            | Returns the number token (0 if Desert)                   |
| `isDesert()`                       | `boolean`        | Returns true if resourceType is DESERT                   |
| `producesResource()`               | `boolean`        | Returns false for Desert, true otherwise                 |

**Invariants:**
- Desert hex always has `numberToken == 0`
- Non-desert hex has `numberToken` in {2,3,4,5,6,8,9,10,11,12}

---

### `Vertex`

An intersection on the board — the point where up to 3 hexes meet. Settlements and cities are placed here.

| Field           | Type           | Description                                     |
|-----------------|----------------|-------------------------------------------------|
| `id`            | `int`          | Unique identifier (0–53)                        |
| `adjacentHexes` | `List<Hex>`    | Hexes touching this intersection (1–3)          |
| `adjacentVertices` | `List<Vertex>` | Neighboring intersections (for Distance Rule) |
| `owner`         | `Player`       | Null if unoccupied                              |

| Method                        | Return Type   | Description                                         |
|-------------------------------|---------------|-----------------------------------------------------|
| `getId()`                     | `int`         | Returns the vertex id                               |
| `isOccupied()`                | `boolean`     | Returns true if a settlement or city is placed here |
| `getOwner()`                  | `Player`      | Returns the occupying player, or null               |
| `getAdjacentHexes()`          | `List<Hex>`   | Returns hexes adjacent to this vertex               |
| `getAdjacentVertices()`       | `List<Vertex>`| Returns neighboring vertices                        |

---

### `Edge`

A path on the board — the border between two hexes (or a hex and the frame). Roads are placed here.

| Field       | Type       | Description                             |
|-------------|------------|-----------------------------------------|
| `id`        | `int`      | Unique identifier (0–71)                |
| `endpoints` | `Vertex[]` | The two vertices this edge connects     |
| `owner`     | `Player`   | Null if no road placed                  |

| Method              | Return Type | Description                                   |
|---------------------|-------------|-----------------------------------------------|
| `getId()`           | `int`       | Returns the edge id                           |
| `hasRoad()`         | `boolean`   | Returns true if a road is placed here         |
| `getOwner()`        | `Player`    | Returns the player who placed a road, or null |
| `getEndpoints()`    | `Vertex[]`  | Returns the two endpoint vertices             |
| `connectsTo(Vertex)`| `boolean`   | Returns true if the given vertex is an endpoint |

---

### `Board`

Holds the complete board topology: all hexes, vertices, and edges.

| Field      | Type           | Description                           |
|------------|----------------|---------------------------------------|
| `hexes`    | `List<Hex>`    | The 19 terrain hexes                  |
| `vertices` | `List<Vertex>` | All 54 intersection points            |
| `edges`    | `List<Edge>`   | All 72 paths                          |

| Method                                    | Return Type    | Description                                                                 |
|-------------------------------------------|----------------|-----------------------------------------------------------------------------|
| `Board(List<Hex>)`                        | —              | Constructor; throws `IllegalArgumentException` if hex list is invalid (see invariants) |
| `getHexes()`                              | `List<Hex>`    | Returns all 19 hexes                                                        |
| `getVertices()`                           | `List<Vertex>` | Returns all 54 vertices                                                     |
| `getEdges()`                              | `List<Edge>`   | Returns all 72 edges                                                        |
| `getVertex(int id)`                       | `Vertex`       | Returns the vertex with the given id                                        |
| `getEdge(int id)`                         | `Edge`         | Returns the edge with the given id                                          |
| `getHexCount(ResourceType)`               | `int`          | Returns the number of hexes of the given type                               |
| `satisfiesDistanceRule(Vertex)`           | `boolean`      | Returns true if no adjacent vertex of the given vertex is occupied          |
| `isConnectedToPlayer(Vertex, Player)`     | `boolean`      | Returns true if any edge adjacent to the vertex has a road owned by player  |

**Invariants (validated in constructor):**
- Exactly 19 hexes total
- Exactly 1 DESERT hex
- Exactly 4 GRAIN, 4 PASTURE, 4 FOREST, 3 ORE, 3 BRICK hexes (using ResourceType values GRAIN, WOOL, LUMBER, ORE, BRICK)
- Exactly 18 non-desert hexes have valid number tokens

---

### `Player`

Represents a single player's state.

| Field       | Type                          | Description                            |
|-------------|-------------------------------|----------------------------------------|
| `name`      | `String`                      | The player's name                      |
| `color`     | `PlayerColor`                 | The player's color                     |
| `resources` | `Map<ResourceType, Integer>`  | Cards in hand; initialized to 0 for each resource type |

| Method                                      | Return Type | Description                                                    |
|---------------------------------------------|-------------|----------------------------------------------------------------|
| `Player(String name, PlayerColor color)`    | —           | Constructor; throws `IllegalArgumentException` if name is blank or null, or color is null |
| `getName()`                                 | `String`    | Returns the player's name                                      |
| `getColor()`                                | `PlayerColor`| Returns the player's color                                    |
| `getResourceCount(ResourceType)`            | `int`       | Returns how many of that resource the player holds             |
| `addResources(ResourceType, int amount)`    | `void`      | Adds the given amount of a resource; throws if amount < 0      |
| `getTotalResourceCount()`                   | `int`       | Returns the total number of resource cards in hand             |

---

### `Game`

Top-level class that holds all game state. Validates and stores players and the board.

| Field             | Type           | Description                                |
|-------------------|----------------|--------------------------------------------|
| `players`         | `List<Player>` | All players in turn order                  |
| `board`           | `Board`        | The game board                             |

| Method                              | Return Type    | Description                                                                      |
|-------------------------------------|----------------|----------------------------------------------------------------------------------|
| `Game(List<Player> players, Board)` | —              | Constructor; throws `IllegalArgumentException` if player count is not 2–4, or if any two players share a color |
| `getPlayers()`                      | `List<Player>` | Returns all players in order                                                     |
| `getPlayerCount()`                  | `int`          | Returns the number of players                                                    |
| `getBoard()`                        | `Board`        | Returns the board                                                                |
| `getPlayer(int index)`              | `Player`       | Returns the player at the given index                                            |

---

### `SetupPhase`

Manages the two-round initial placement sequence: clockwise in Round 1, counter-clockwise in Round 2. Distributes starting resources after each player's second settlement.

| Field                   | Type           | Description                                                      |
|-------------------------|----------------|------------------------------------------------------------------|
| `game`                  | `Game`         | The game being set up                                            |
| `placementOrder`        | `List<Player>` | Full sequence of placements (Round 1 order + Round 2 order)      |
| `currentPlacementIndex` | `int`          | Index into `placementOrder` for the current turn                 |
| `placedSettlements`     | `Map<Player, List<Vertex>>` | Tracks each player's placed settlement vertices    |

| Method                                          | Return Type    | Description                                                                            |
|-------------------------------------------------|----------------|----------------------------------------------------------------------------------------|
| `SetupPhase(Game game)`                         | —              | Constructor; builds the full placement order (Round 1 clockwise + Round 2 counter-clockwise) |
| `getPlacementOrder()`                           | `List<Player>` | Returns the full ordered list of players across both rounds (length = 2 × playerCount) |
| `getCurrentPlayer()`                            | `Player`       | Returns the player who must act next                                                   |
| `getCurrentRound()`                             | `int`          | Returns 1 or 2                                                                         |
| `isComplete()`                                  | `boolean`      | Returns true when all placements are done                                              |
| `placeSettlement(Player player, int vertexId)`  | `void`         | Validates and places a settlement; throws if wrong player, vertex occupied, or Distance Rule violated |
| `placeRoad(Player player, int edgeId)`          | `void`         | Validates and places a road adjacent to the last placed settlement; throws if wrong player or edge invalid |
| `distributeStartingResources(Player player)`    | `void`         | After a player's second settlement, gives 1 resource per adjacent hex; called internally after second road is placed |

**Placement order rules:**
- Round 1: players in original order (index 0, 1, 2, … n-1)
- Round 2: players in reverse order (index n-1, n-2, … 0)
- Full sequence length = 2 × playerCount

---

## Key Validation Rules (BVA Targets)

| Rule                        | Invalid (boundary)      | Valid (boundary)      |
|-----------------------------|-------------------------|-----------------------|
| Player count                | 1 player, 5 players     | 2 players, 4 players  |
| Hex count                   | 18 hexes, 20 hexes      | 19 hexes              |
| Number token value          | 1, 7, 13                | 2, 12                 |
| Distance Rule               | vertex with an occupied neighbor | vertex with all neighbors empty |
| Resource amount added       | -1                      | 0, 1                  |
| Player name                 | null, ""                | any non-blank string  |
| Placement by wrong player   | any other player acts   | only current player acts |

---

## Package Structure

```
src/main/java/domain/
├── ResourceType.java       (enum)
├── PlayerColor.java        (enum)
├── Hex.java
├── Vertex.java
├── Edge.java
├── Board.java
├── Player.java
├── Game.java
└── SetupPhase.java

src/test/java/domain/
├── HexTest.java
├── BoardTest.java
├── PlayerTest.java
├── GameTest.java
└── SetupPhaseTest.java
```
