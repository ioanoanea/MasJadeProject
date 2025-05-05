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
    fun addAgent(x: Int, y: Int, agentName: String) {
        lock.lock()
        grid[x][y].add(agentName)
        lock.unlock()
    }

    fun moveAgent(fromX: Int, fromY: Int, toX: Int, toY: Int, agentName: String): Boolean {
        if (grid[toX][toY].isEmpty()) {
            lock.lock()
            grid[fromX][fromY].remove(agentName)
            grid[toX][toY].add(agentName)
            lock.unlock()
            return true
        } else {
            return false
        }
    }

    private fun removeAgent(x: Int, y: Int, agentName: String) {
        grid[x][y].remove(agentName)
    }

    fun getAgentsAt(x: Int, y: Int): Set<String> {
        lock.lock()
        return grid[x][y].toSet()
        lock.unlock()
    }

    fun printGrid() {
        // lock.lock()
        // println("\nCurrent Grid State:")
        // grid.forEachIndexed { x, column ->
        //     column.forEachIndexed { y, agents ->
        //         if (agents.isNotEmpty()) {
        //             println("($x,$y): ${agents.joinToString()}")
        //         }
        //     }
        // }
        // lock.unlock()
        lock.lock()
        println("\nCurrent Grid State:")
        grid.forEachIndexed { x, column ->
            column.forEachIndexed { y, agents ->
                if (agents.isNotEmpty()) {
                    print("X")
                } else {
                    print(" ")
                }
            }
            println()
        }
        lock.unlock()
    }
}

