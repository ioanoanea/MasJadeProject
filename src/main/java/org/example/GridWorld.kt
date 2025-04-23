// src/main/kotlin/org/example/GridWorld.kt
package org.example

import java.util.concurrent.locks.ReentrantLock

class GridWorld private constructor(val width: Int, val height: Int) {
  private val grid = Array(width) { Array(height) { mutableSetOf<String>() } }
    private val lock = ReentrantLock()

    companion object {
        @Volatile
        private var instance: GridWorld? = null

        fun getInstance(): GridWorld {
            return instance ?: synchronized(this) {
                instance ?: GridWorld(10, 10).also {
                    instance = it
                    println("GridWorld initialized (10x10)")
                }
            }
        }
    }


  // Thread-safe operations
  fun addAgent(x: Int, y: Int, agentName: String) = lock.withLock {
      grid[x][y].add(agentName)
  }

    fun moveAgent(fromX: Int, fromY: Int, toX: Int, toY: Int, agentName: String) = lock.withLock {
        grid[fromX][fromY].remove(agentName)
        grid[toX][toY].add(agentName)
    }

    private fun removeAgent(x: Int, y: Int, agentName: String) {
        grid[x][y].remove(agentName)
    }

    fun getAgentsAt(x: Int, y: Int): Set<String> {
        return lock.withLock {
            grid[x][y].toSet()
        }
    }

    fun printGrid() {
        lock.withLock {
            println("\nCurrent Grid State:")
            grid.forEachIndexed { x, column ->
                column.forEachIndexed { y, agents ->
                    if (agents.isNotEmpty()) {
                        println("($x,$y): ${agents.joinToString()}")
                    }
                }
            }
        }
    }
}

private fun <T> ReentrantLock.withLock(action: () -> T): T {
    lock()
    try {
        return action()
    } finally {
        unlock()
    }
}
