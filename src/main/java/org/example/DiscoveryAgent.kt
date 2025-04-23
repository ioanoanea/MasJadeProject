package org.example

import jade.core.Agent
import jade.core.behaviours.TickerBehaviour
import kotlin.random.Random

class DiscoveryAgent : Agent() {
    private lateinit var grid: GridWorld
    private var x: Int = 0
    private var y: Int = 0

    override fun setup() {
        // Initialize or get the shared grid (10x10 by default)
        grid = GridWorld.getInstance()
        
        // Random starting position
        x = Random.nextInt(0, grid.width)
        y = Random.nextInt(0, grid.height)
        grid.addAgent(x, y, localName)

        // Add behavior to move randomly every 2 seconds
        addBehaviour(object : TickerBehaviour(this, 2000) {
            override fun onTick() {
                val newX = (x + listOf(-1, 0, 1).random()).coerceIn(0, grid.width - 1)
                val newY = (y + listOf(-1, 0, 1).random()).coerceIn(0, grid.height - 1)
                
                grid.moveAgent(x, y, newX, newY, localName)
                x = newX
                y = newY
                
                grid.printGrid() // Optional: Print grid state after each move
            }
        })
    }

    override fun takeDown() {
        grid.printGrid() // Final state when agent dies
    }
}
