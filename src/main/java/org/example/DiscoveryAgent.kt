package org.example

import jade.core.AID
import jade.core.Agent
import jade.core.behaviours.CyclicBehaviour
import jade.core.behaviours.TickerBehaviour
import jade.domain.DFService
import jade.domain.FIPAAgentManagement.DFAgentDescription
import jade.domain.FIPAAgentManagement.ServiceDescription
import jade.domain.FIPAException
import jade.lang.acl.ACLMessage
import kotlin.random.Random
import java.util.LinkedList
import java.util.Queue

class DiscoveryAgent : Agent() {
    private lateinit var grid: GridWorld
    private var path: Queue<Pair<Int, Int>> = LinkedList()
    private var x: Int = 0
    private var y: Int = 0

    // Matrix to track visited positions
    private val visitedMatrix: Array<Array<Boolean>> = Array(10) { Array(10) { false } }

    override fun setup() {
        grid = GridWorld.getInstance()
        
        x = Random.nextInt(0, grid.width)
        y = Random.nextInt(0, grid.height)
        grid.addAgent(x, y, localName)

        visitedMatrix[x][y] = true

        // Register this agent with the Directory Facilitator (DF)
        registerWithDF()

        sendPositionToAgents()

        // Add behavior to move and send position
        addBehaviour(object : TickerBehaviour(this, 200) {
            override fun onTick() {
                var newX: Int = x
                var newY: Int = y

                if (path.peek() == Pair(x, y)) {
                    path.poll() // Remove the current position from the path
                }

                if (path.isNotEmpty() && path.peek() != Pair(-1, -1)) {
                    val nextPosition = path.peek()
                    newX = nextPosition.first
                    newY = nextPosition.second
                } else {
                    path = bfs()
                    if (path?.peek() == Pair(-1, -1)) {
                        println("$localName: All nodes visited.")
                        return
                    }
                }

                if (grid.moveAgent(x, y, newX, newY, localName)) {
                    path.poll() // Remove the position from the path
                    x = newX
                    y = newY

                    visitedMatrix[x][y] = true

                    sendPositionToAgents()
                } else {
                    path = bfs()
                }

                // printVisitedMatrix()
                grid.printGrid(localName) // Optional: Print grid state after each move
            }
        })

        // Add behavior to receive messages
        addBehaviour(object : CyclicBehaviour() {
            override fun action() {
                val message = receive()
                if (message != null) {
                    // Parse the message content
                    val content = message.content
                    val sender = message.sender.localName

                    // Extract position from the message content
                    val (receivedX, receivedY) = message.content.split(",").map { it.trim() }
                    val posX = receivedX.toInt()
                    val posY = receivedY.toInt()

                    // Mark the received position as visited
                    visitedMatrix[posX][posY] = true
                } else {
                    block() // Wait for the next message
                }
            }
        })
    }

    // BFS to find the path to the closest unvisited node
    private fun bfs(): Queue<Pair<Int, Int>> {
        val directions = listOf(
            Pair(0, 1), Pair(1, 0), Pair(0, -1), Pair(-1, 0)
        )
        val path: Queue<Pair<Int, Int>> = LinkedList()

        val queue: Queue<Pair<Int, Int>> = LinkedList()
        queue.add(Pair(x, y))

        val visited = mutableSetOf<Pair<Int, Int>>()
        visited.add(Pair(x, y))

        // Map to track the parent of each node
        val parentMap = mutableMapOf<Pair<Int, Int>, Pair<Int, Int>?>()
        parentMap[Pair(x, y)] = null

        while (queue.isNotEmpty()) {
            val (currentX, currentY) = queue.poll()

            // If an unvisited node is found, reconstruct the path
            if (!visitedMatrix[currentX][currentY]) {
                var current: Pair<Int, Int>? = Pair(currentX, currentY)
                while (current != null) {
                    path.add(current)
                    current = parentMap[current]
                }
                return LinkedList(path.reversed()) // Reverse the path to get it from start to target
            }

            // Explore neighbors
            for ((dx, dy) in directions) {
                val neighborX = currentX + dx
                val neighborY = currentY + dy

                if (neighborX in 0 until grid.width && neighborY in 0 until grid.height) {
                    val neighbor = Pair(neighborX, neighborY)
                    if (neighbor !in visited) {
                        queue.add(neighbor)
                        visited.add(neighbor)
                        parentMap[neighbor] = Pair(currentX, currentY) // Track the parent
                    }
                }
            }
        }

            // If no unvisited node is found, return a path with (-1, -1)
        path.add(Pair(-1, -1))
        return path
    }

    private fun registerWithDF() {
        val dfd = DFAgentDescription()
        dfd.name = aid
        val sd = ServiceDescription()
        sd.type = "discovery-agent"
        sd.name = localName
        dfd.addServices(sd)

        try {
            DFService.register(this, dfd)
            println("$localName registered with the DF.")
        } catch (e: FIPAException) {
            e.printStackTrace()
        }
    }

    private fun sendPositionToAgents() {
        val message = ACLMessage(ACLMessage.INFORM)
        message.content = "$x, $y"

        // Query the DF for all agents of type "discovery-agent"
        val template = DFAgentDescription()
        val sd = ServiceDescription()
        sd.type = "discovery-agent"
        template.addServices(sd)

        try {
            val result = DFService.search(this, template)
            for (dfd in result) {
                if (dfd.name.localName != localName) {
                    message.addReceiver(dfd.name)
                }
            }
        } catch (e: FIPAException) {
            e.printStackTrace()
        }

        send(message) // Send the message
    }

    override fun takeDown() {
        // Deregister from the DF when the agent is terminated
        try {
            DFService.deregister(this)
            println("$localName deregistered from the DF.")
        } catch (e: FIPAException) {
            e.printStackTrace()
        }
    }

    fun printVisitedMatrix() {
        if(localName == "agent1") {
            println("Visited Matrix for $localName:")
        } else {
            return
        }
        for (row in visitedMatrix) {
            println(row.joinToString(" ") { if (it) "V" else " " })
        }
    }
}
