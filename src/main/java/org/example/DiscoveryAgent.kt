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

class DiscoveryAgent : Agent() {
    private lateinit var grid: GridWorld
    private var x: Int = 0
    private var y: Int = 0

    // Matrix to track visited positions
    private val visitedMatrix: Array<Array<Boolean>> = Array(10) { Array(10) { false } }

    override fun setup() {
        // Initialize or get the shared grid (10x10 by default)
        grid = GridWorld.getInstance()
        
        x = Random.nextInt(0, grid.width)
        y = Random.nextInt(0, grid.height)
        grid.addAgent(x, y, localName)

        // Mark the agent's initial position as visited
        visitedMatrix[x][y] = true

        // Register this agent with the Directory Facilitator (DF)
        registerWithDF()

        sendPositionToAgents()

        // Add behavior to move and send position
        addBehaviour(object : TickerBehaviour(this, 2000) {
            override fun onTick() {
                val newX = (x + listOf(-1, 0, 1).random()).coerceIn(0, grid.width - 1)
                val newY = (y + listOf(-1, 0, 1).random()).coerceIn(0, grid.height - 1)
                
                if (grid.moveAgent(x, y, newX, newY, localName)) {
                    x = newX
                    y = newY

                    // Mark the new position as visited
                    visitedMatrix[x][y] = true

                    sendPositionToAgents()
                }

                printVisitedMatrix()
                grid.printGrid() // Optional: Print grid state after each move
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

    private fun registerWithDF() {
        val dfd = DFAgentDescription()
        dfd.name = aid // This agent's AID
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
                if (dfd.name.localName != localName) { // Avoid sending the message to itself
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

        grid.printGrid() // Final state when agent dies
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
