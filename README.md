# Easy Soccer Lib

A Java library to help in developing teams for Robocup 2D simulator. The objective is to simplifiy the development, for didactical purposes. (It was created as part of an undergraduate course in Multiagent Systems).

It is **not** compliant with the oficial Robocup 2D competition.

## About the Library

This library offers classes for creating players and teams. The class used by the players offers high-level methods for sending actions to the simulator, 
and for receiving percetions from it. The player has access to global perceptions (perfect information).

The library is an Eclipse project that you can import.


## About the Sample Teams

The repository has also some sample teams, divided in two Eclipse projects. 

Project **Sample Teams 1 - Basic** has the simplest teams to help starters:

- *ball_follower_team* - All players simply runs towards the ball and kicks it straight ahead (not aware of the goal direction).
- *keyboard_team* - A team with one player that you can control using the keyboard.
- *organized_team* - A team that simply enters in formation in the start of the game (it doesn't really play).

Project **Sample Teams 2 - Architectures** has three teams using three different multiagent architectures (all are/were popular in game industry):
- *fsm_team* - each player (agent) is developed using a *finite-state machine* architecture
- *bt_team* - each player (agent) uses a *behavior tree* (which, basically, defines the behavior of the agent as composition of simpler behaviors, organized as a tree)
- *utility_team* - each player (agent) is a *utility-based* agent from game industry (which is composed by a set of behaviors, each with its individual "utility function" to indicates how suitable is the behavior for the current state of the agent)


## Acknowledgement

The first version of the library was called robosoccerlib and it is still available on bitbucket. It was developed with the help of Bruno Marques and André Lucas. 
