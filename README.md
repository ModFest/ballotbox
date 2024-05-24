
---

**BallotBox** is an in-game voting utility for ModFest events.<br/>
Players can run the `/vote` command to open a GUI that allows them to select items to vote for across multiple categories. 

Items and voting categories are sent by the server, which fetches them from a REST API.<br/>
Categories and items can both contain descriptions, and items can contain URLs for the player to visit.<br/> 
On close, selections are sent to the server, which securely updates them to the API using the player UUID.

---