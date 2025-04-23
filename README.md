# MasJadeProject

### Prerequisites
- **JDK 23**
- **Gradle 7+**

### Run

``` .\gradle clean run ```

### Add more agents

In `build.gradle.kts` modify the third argument in the `args` list.

### Project description

- `GridWorld` is a singleton class and acts as an evironment to the agents. All operations performed by the `GridWorld` class are atomic.
- `DiscoveryAgent` is the agent class which must discover every cell inside the grid world. It implements `TickerBehaviour` in which the agent performs a random action every 2 seconds.
