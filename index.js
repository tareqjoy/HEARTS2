'use strict';

const functions = require('firebase-functions');
const admin = require('firebase-admin');
const totalPlayer = 4;
const cardPerPlayer = 13;
admin.initializeApp();
/*
exports.countPlayer = functions.database.ref('/game/{gameCode}/player/{uuid}')
	.onWrite(async (change) => {
		const collectionRef = change.after.ref.parent;
		const countRef = collectionRef.parent.child('playersCount');
		const readyRef = collectionRef.parent.child('readyCount');

		let increment;
		if (change.after.exists() && !change.before.exists()) {
			increment = 1;
		} else if (!change.after.exists() && change.before.exists()) {
			increment = -1;
			await readyRef.transaction((current) => {
				var res = (current || 0) - 1;
				if (res >= 0) {
					return res;
				}
				else {
					return null;
				}

			});
		} else {
			return null;
		}

		// Return the promise from countRef.transaction() so our function
		// waits for this async event to complete before it exits.
		await countRef.transaction((current) => {
			var res = (current || 0) + increment;
			if (res >= 0) {
				return res;
			}
			else {
				return null;
			}
		});
		//console.log('Player Counter updated.');
		return null;

	});


*/


//triggers when a player changes to ready or not
exports.countReadyPlayer = functions.database.ref('/game/{gameCode}/player/{uuid}')
	.onUpdate(async (change, context) => {
		//change.after.ref = UUID
		const collectionRef = change.after.ref.parent; //player
		const countRef = collectionRef.parent.child('readyCount'); //gameCode / readyCount



		const readyRef = change.after.ref.parent; //player

		var playerJoinedCount;

		await collectionRef.once("value").then(function (playerSnap) {
			playerJoinedCount = playerSnap.numChildren();
		});

		var uuidArr = [];
		var playerPointDict = {};
		let cnt = 0;
		await readyRef.once('value').then(function (uuidSnap) {

			uuidSnap.forEach(function (playerSnap) {
				//	uuidArr.push(playerSnap.key);
				//	playerPointDict[playerSnap.key] = 0;
				if (playerSnap.val().ready) {
					cnt++;
				}

			});
		});
		await countRef.set(cnt);
		//console.log("player avail: " + playerJoinedCount);
		//console.log("ready: " + cnt);
		if (cnt == playerJoinedCount) { //everyone ready
			var countBot = 4 - playerJoinedCount;
			if (countBot > 0) {
				for (var i = 1; i <= countBot; i++) {
					await collectionRef.child("bot_" + i).set({
						"name": "BOT " + i,
						"ready": true,
						"uuid": "bot_" + i
					});
				}
			}
			await readyRef.once('value').then(function (uuidSnap) {

				uuidSnap.forEach(function (playerSnap) {
					uuidArr.push(playerSnap.key);
					playerPointDict[playerSnap.key] = 0;
				});
			});
			await countRef.set(4);
			//randomize the player positions
			for (var i = uuidArr.length - 1; i > 0; i--) {
				var j = Math.floor(Math.random() * (i + 1));
				var temp = uuidArr[i];
				uuidArr[i] = uuidArr[j];
				uuidArr[j] = temp;
			}

			//push the randomize list
			await readyRef.parent.child("playerOrder").set(uuidArr).then(function () {

				collectionRef.parent.child("playerPoints").set(playerPointDict).then(function () {
					readyRef.parent.child("gameRunning").set(false);
				});
				//manually trigger to rearrange all card

			});





		}


		//console.log('Player Ready Counter updated.');
		return null;

	});

//passing cards, trigerts when a user selected cards for passing 
//selectedCards
//  |
//  ---UUID1
//       |
//       ----0: HEART_c2
//       ----1: CLUB_c5
//       ----2: SPADE_cQ
//  ----UUID2
//      |
//............................
exports.passCards = functions.database.ref('/game/{gameCode}/selectedCards')
	.onUpdate(async (change) => {



		const collectionRef = change.after.ref; //selectedCards
		const cardsRef = collectionRef.parent.child("playerCards");
		const gameCodeRef = change.after.ref.parent; //{gameCode}

		var passType;
		var cardDict = {};
		var playerCards = {};
		var newCardDict = {};

		var snapIf=await collectionRef.once("value");
		if(snapIf.val()==false){
			return null;
		}


		await gameCodeRef.child("gameState").once("value").then(function (gameStateSnap) {
			passType = gameStateSnap.val();
		});

		await cardsRef.once("value").then(function (playerSnap) {
			playerSnap.forEach(function (cardsListSnap) {
				playerCards[cardsListSnap.key] = cardsListSnap.val();
			});
		});

		var flag = false;

		await collectionRef.once("value").then(function (snap) {
			if (snap.numChildren() == 4) {
				flag = true;
				snap.forEach(function (childSnap) {
					var selCardArr = childSnap.val();
					cardDict[childSnap.key] = selCardArr; //getting selected cards to pass by users
					//console.log(selCardArr);
					for (var i = 0; i < selCardArr.length; i++) {
						var idx = playerCards[childSnap.key].indexOf(selCardArr[i]);
						playerCards[childSnap.key].splice(idx, 1);
					}
				});
			}
		});

		//console.log(playerCards);

		if (flag == false) {
			return null;
		}

		//await cardsRef.set(playerCards);
		console.log("playerCardsOld:" + playerCards.toString());
		await gameCodeRef.child("playerOrder").once("value").then(function (playerOrderSnap) {
			var playerOrderList = playerOrderSnap.val(); //getting palyer order

			if (passType == "pl") { //passing left
				for (var i = 0; i < playerOrderList.length - 1; i++) {
					newCardDict[playerOrderList[i + 1]] = cardDict[playerOrderList[i]];
					playerCards[playerOrderList[i + 1]].concat(newCardDict[playerOrderList[i + 1]]);
				}
				newCardDict[playerOrderList[0]] = cardDict[playerOrderList[playerOrderList.length - 1]];
				playerCards[playerOrderList[0]].concat(cardDict[playerOrderList[playerOrderList.length - 1]]);

			} else if (passType == "pr") { //passing right
				for (var i = 0; i < playerOrderList.length - 1; i++) {
					newCardDict[playerOrderList[i]] = cardDict[playerOrderList[i + 1]];
					playerCards[playerOrderList[i]].concat(cardDict[playerOrderList[i + 1]]);
				}
				newCardDict[playerOrderList[playerOrderList.length - 1]] = cardDict[playerOrderList[0]];
				playerCards[playerOrderList[playerOrderList.length - 1]].concat(cardDict[playerOrderList[0]]);
			} else if (passType == "ps" && playerOrderList % 2 == 0) { //passing straight if player numbers are even
				newCardDict[playerOrderList[0]] = cardDict[playerOrderList[2]];
				playerCards[playerOrderList[0]].concat(cardDict[playerOrderList[2]]);

				newCardDict[playerOrderList[2]] = cardDict[playerOrderList[0]];
				playerCards[playerOrderList[2]].concat(cardDict[playerOrderList[0]]);

				newCardDict[playerOrderList[1]] = cardDict[playerOrderList[3]];
				playerCards[playerOrderList[1]].concat(cardDict[playerOrderList[3]]);


				newCardDict[playerOrderList[3]] = cardDict[playerOrderList[1]];
				playerCards[playerOrderList[3]].concat(cardDict[playerOrderList[1]]);
			} else if (passType == "np") { //no passing
				//already should have start the game without passing
			}

		});

		console.log("playerCardsNew:" + playerCards.toString());
		
		if(passType!="np"){
			await cardsRef.set(playerCards);
			return gameCodeRef.child("passedCards").set(newCardDict).then(async function () { //should notfy the users about new 3 cards
				await gameCodeRef.child("gameRunning").set(true); //start the game, trigger users
			});
		}else{
			return gameCodeRef.child("passedCards").set(false).then(async function () { //should notfy the users about new 3 cards
				await gameCodeRef.child("gameRunning").set(true); //start the game, trigger users
			});
		}
	});

	//create new game
exports.checkNewGameCreate = functions.database.ref('/game/{gameCode}/gameRunning')
.onCreate(async (change) => {
	checkNewGameFunc(change.ref);
});

//create new game
exports.checkNewGameUpdate = functions.database.ref('/game/{gameCode}/gameRunning')
	.onUpdate(async (change) => {
		checkNewGameFunc(change.after.ref);
	});

async function checkNewGameFunc(change){
	

	const collectionRef = change; //gameRunning
	const gameCodeRef = collectionRef.parent; //gameCode
	const gameCountRef = gameCodeRef.child("gameCount");

	var gameRunVal;
	var uuidArr = [];
	var uuidCards = {};
	var gameCountInt = 0;
	var allCards = [];
	var palyerToMove;

	await collectionRef.once('value').then(function (gameRunSnap) {
		gameRunVal = gameRunSnap.val();
	});

	if (gameRunVal == false) {
		await gameCodeRef.child("selectedCards").set(false);
		await gameCodeRef.child("passedCards").set(false);
		await gameCodeRef.child("move").child("playerToMove").set(false);
		await gameCodeRef.child("move").child("movedPlayer").set(false);
		await gameCodeRef.child("playerOrder").once("value").then(function (playerOrderSnap) {
			uuidArr = playerOrderSnap.val();
			//create all possible cards

			for (var i = 0; i < 52; i++) {
				var cardId = Math.floor(i / 13);
				var cardTypeStr = "";
				if (cardId == 0) {
					cardTypeStr = "CLUB_";
				} else if (cardId == 1) {
					cardTypeStr = "SPADE_";
				} else if (cardId == 2) {
					cardTypeStr = "DIAMOND_";
				} else {
					cardTypeStr = "HEART_";
				}
				var cardNumInt = (i % 13) + 2;
				var cardNumStr = "";
				if (cardNumInt == 11) {
					cardNumStr = "J";
				} else if (cardNumInt == 12) {
					cardNumStr = "Q";
				} else if (cardNumInt == 13) {
					cardNumStr = "K";
				} else if (cardNumInt == 14) {
					cardNumStr = "A";
				} else {
					cardNumStr = cardNumInt.toString();
				}
				var cardId = cardTypeStr + "c" + cardNumStr;
				allCards.push(cardId);
			}

			//distrbuting cards randomly
			for (var i = 0; i < uuidArr.length; i++) {
				var randomCards = [];

				for (var j = 0; j < 13; j++) {
					var randomInt = Math.floor(Math.random() * (allCards.length));
					if (allCards.length != 1) {
						randomCards.push(allCards[randomInt]);
						allCards.splice(randomInt, 1);
					} else {
						randomCards.push(allCards[0]);
					}
					randomCards.sort();

				}
				uuidCards[uuidArr[i]] = randomCards;
			}
		});

		await gameCodeRef.child("playerCards").set(uuidCards);


		await gameCountRef.transaction((current) => {

			gameCountInt = (current || 0) + 1;
			//game state: 1=pl,2=pr,3=ps,0=np,gs,ge
			var gameStateStr = "";
			if (gameCountInt % 4 == 1) {
				gameStateStr = "pl";
			} else if (gameCountInt % 4 == 2) {
				gameStateStr = "pr";
			} else if (gameCountInt % 4 == 3 && uuidArr.length == 4) {
				gameStateStr = "ps";
			} else if (gameCountInt % 4 == 0) {
				gameStateStr = "np";
				gameCodeRef.child("gameRunning").set(true); //no passing, start the game now and trigger users
			}


			gameCodeRef.child("gameState").set(gameStateStr);
			//	console.log('Game Started.');
			return gameCountInt;
		});
	} else if (gameRunVal == true) {
		await gameCodeRef.child("playerCards").once('value').then(function (gameCodeSnap) {
			gameCodeSnap.forEach(function (playerSnap) {
				playerSnap.forEach(function (cardSnap) {
					if (cardSnap.val() == "CLUB_c2") { //if player has club 2, he has to move first
						//card = any, when a player can move any card now (when the player have to move first)
						palyerToMove = playerSnap.key;
						return;

					}
				});
			});

		});

		return gameCodeRef.child("move").child("playerToMove").set({
			"card": {
				"type": "CLUB",
				"number": "c2"
			},
			"player": palyerToMove //trigger player to make move
		});

	} else {
		return null;
	}
}

//player moved a card, make descition to select, and set next player
exports.checkCardMove = functions.database.ref('/game/{gameCode}/move/movedPlayer')
	.onUpdate(async (change) => {


		const collectionRef = change.after.ref; //movedPlayer
		const moveRef = collectionRef.parent; //move
		const countPlayedRef = moveRef.child("countPlayed");
		const gameCodeRef = moveRef.parent; //gameCode
		const cardPlayedRef = moveRef.child("cardPlayed");


		var movedPlayerCard; // player: XXX, card: [ type: HEART, number: cA ]
		var movedCard;  //type: HEART, number: cA
		var movedPlayer; //some UUID
		var toCard; //type: CLUB, number: c2
		var toPlayer; //some UUID

		await collectionRef.once("value").then(function (movedPlayerSnap) {
			movedPlayerCard = movedPlayerSnap.val();
			movedCard = movedPlayerCard.card;
			movedPlayer = movedPlayerCard.player;
		});

		await moveRef.child("playerToMove").once("value").then(function (playerToMoveSnap) {
			toCard = playerToMoveSnap.val().card;
			toPlayer = playerToMoveSnap.val().player;
		});



		if (movedPlayer == toPlayer && toPlayer != null) { //valid player as expected in server

			var cardPlayedList = [];

			await cardPlayedRef.once("value").then(function (cardPlayedSnap) {
				cardPlayedSnap.forEach(function (playCardSnap) {
					cardPlayedList.push(playCardSnap.val());
				});
			});
			if (cardPlayedList.length == 3) { //3 player already played his card, and one player just played now
				//	await cardPlayedRef.set(null).then(function(){
				cardPlayedList.push(movedPlayerCard); //list size = 4, with the current player
				var cardGetPlayer; //if it is a game of CLUB, the player who played CLUB (He has CLUB card in his deck) added to this list. 
				var maxCard = -1;
				var point = 0;
				for (var i = 0; i < cardPlayedList.length; i++) {
					if (cardPlayedList[i].card.type.toUpperCase() == toCard.type.toUpperCase()) { //who has the played card type 
						var tempVal = cardPlayedList[i].card.number.substring(1).toUpperCase(); // c2 => 2, c7 => 7 or cJ =>J
						if (isNaN(tempVal) == false) { //tempVal is real number (for the card c2-c10)
							tempVal = parseInt(tempVal);
						} else { //tempVal is J, K, Q, A
							if (tempVal == "J") {
								tempVal = 11;
							} else if (tempVal == "Q") {
								tempVal = 12;
							} else if (tempVal == "K") {
								tempVal = 13;
							} else if (tempVal == "A") {
								tempVal = 14;
							} else { //invalid card number

							}
						}

						if (maxCard < tempVal) {
							maxCard = tempVal;
							cardGetPlayer = cardPlayedList[i].player;
						}
					}
					if (cardPlayedList[i].card.type.toUpperCase() == "HEART") {
						point++;
					} else if (cardPlayedList[i].card.type.toUpperCase() == "SPADE" && cardPlayedList[i].card.number.toUpperCase() == "CQ") {
						point = point + 13;
					}
				}
				await gameCodeRef.child("playerPoints").child(cardGetPlayer).transaction((current) => {
					return (current || 0) + point;
				});

				await cardPlayedRef.set(null).then(function () {
					gameCodeRef.child("move").child("playerGot").set(cardGetPlayer).then(function () {
						countPlayedRef.transaction((current) => {
							var res = (current || 0) + 1;
							if (res == cardPerPlayer) {
								res = 0;
								gameCodeRef.child("gameRunning").set(false);
							} else {
								gameCodeRef.child("move").child("playerToMove").set({
									"card": {
										"type": "ANY",
										"number": "ANY"
									},
									"player": cardGetPlayer //trigger player to make move
								}).then(function () {
									gameCodeRef.child("move").child("playerGot").set(null);
								});
							}
							return res;
						});
					});
				});
				/*
				await cardPlayedRef.set(null).then(function(){
					gameCodeRef.child("move").child("playerGot").set(cardGetPlayer).then(function(){
						gameCodeRef.child("move").child("playerToMove").set({
							"card": {
								"type": "ANY",
								"number": "ANY"
							},
							"player": cardGetPlayer //trigger player to make move
						}).then(function(){
							countPlayedRef.transaction((current) => {
								var res=(current || 0) + 1;
								if(res==cardPerPlayer){

								}else{

								}
								return res;
							});
						}).then(function(){
							gameCodeRef.child("move").child("playerGot").set(null);
						});
					});
				});
				*/



				//		});

			} else { //one or more or all player may left to give a card

				//select next player to move
				await gameCodeRef.child("playerOrder").once("value").then(function (playerOrderSnap) {
					var playerOrderList = playerOrderSnap.val();
					var nextPlayer = -1;
					for (var i = 0; i < playerOrderList.length; i++) {
						if (playerOrderList[i] == movedPlayer) {
							nextPlayer = i;
							break;
						}
					}
					nextPlayer++;
					if (nextPlayer == totalPlayer) {
						nextPlayer = 0;
					}
					//	console.log(playerOrderList);
					//	console.log(nextPlayer);
					//this is the first move, so the next player has to give this type card
					if (cardPlayedList.length == 0) {
						gameCodeRef.child("move").child("playerToMove").set({
							"card": {
								"type": movedCard.type,
								"number": "ANY"
							},
							"player": playerOrderList[nextPlayer] //trigger player to make move
						});
					} else { //2nd and 3rd player has to make move withe current card type, hence, the current card type won't update
						gameCodeRef.child("move").child("playerToMove").child("player").set(playerOrderList[nextPlayer]);
					}

				});
				//put played card on table
				await collectionRef.once("value").then(function (movedPlayerSnap) {
					var newMovedPlayer = movedPlayerSnap.val();
					cardPlayedRef.push(newMovedPlayer);
				});
			}
		} else { //wrong player moved, may be bug in game or hacked

		}



	});

//only for bots
exports.botPassCreate = functions.database.ref('/game/{gameCode}/gameState')
	.onCreate(async (change, context) => {
		await botPassFunc(change.ref);
	});

//only for bots
exports.botPassUpdate = functions.database.ref('/game/{gameCode}/gameState')
	.onUpdate(async (change, context) => {
		await botPassFunc(change.after.ref);
	});


async function botPassFunc(change){
	const collectionRef = change; //gameState
	const gameCodeRef = collectionRef.parent;
	const selectedCardsRef = collectionRef.parent.child("selectedCards");
	const playerCardsRef = collectionRef.parent.child("playerCards");
	var dir;
	await collectionRef.once("value").then(function (snap) {
		dir = snap.val();
	});



	//console.log("BOt Pass is running");
	if (dir != "np") {
		const cardsRef = collectionRef.parent.child("playerCards");
		await cardsRef.once("value").then(function (cardsSnap) {
			cardsSnap.forEach(function (playerUUIDSnap) {
				var uUID = playerUUIDSnap.key;
				if (uUID.includes("bot_")) {
					var cardArr = playerUUIDSnap.val();
					//console.log(cardArr);
					var toPass = findPassingCards(cardArr);

					//playerCardsRef.child(uUID).set(cardArr).then(async function(){
						selectedCardsRef.child(uUID).set(toPass);
					//}); 



				}
			});
		});
	}else{
		await gameCodeRef.child("gameRunning").set(true);
	}
	return null;
}


//only for bots
exports.addPassedCards = functions.database.ref('/game/{gameCode}/passedCards')
	.onUpdate(async (change) => {


		const collectionRef = change.after.ref; //passedCards
		const cardsRef = collectionRef.parent.child("playerCards");
		const gameCodeRef = change.after.ref.parent; //{gameCode}




		var playerCards = {};



		await cardsRef.once("value").then(function (playerSnap) {
			playerSnap.forEach(function (cardsListSnap) {
				playerCards[cardsListSnap.key] = cardsListSnap.val();
			});
		});


		await collectionRef.once("value").then(function (snap) {
			if (snap.numChildren() == 4) {
				snap.forEach(function (childSnap) {
					var selCardArr = childSnap.val();
					//console.log(selCardArr);
					for (var i = 0; i < selCardArr.length; i++) {
						playerCards[childSnap.key].push(selCardArr[i]);
					}
				});
			}
		});

		return cardsRef.set(playerCards);


	});

var cMove=0;
exports.botCardMoveCreate = functions.database.ref('/game/{gameCode}/move/playerToMove')
	.onUpdate(async (change) => {
		const collectionRef = change.after.ref; //playerToMove
		const moveRef = collectionRef.parent; //playerToMove

		var currPlayerCard;
		var myCards = [];
		var myAvailCards = []; //stores index of the card of myCards

		cMove++;
		console.log("Called: "+cMove);

		await collectionRef.once("value").then(function (playerCardSnap) {
			currPlayerCard = playerCardSnap.val();
			
		});

		if(currPlayerCard==false){
			return;
		}

		if (currPlayerCard == null) { //delete operation
			return;
		}

		var player = currPlayerCard.player;
		var card = currPlayerCard.card;

		if (player.includes("bot_")) {
			const cardsRef = collectionRef.parent.parent.child("playerCards").child(player);
			await cardsRef.once("value").then(function (cardSnap) {
				myCards = cardSnap.val();
			});


			for (var i = 0; i < myCards.length; i++) { //error here
				var thisCard = myCards[i].split("_")[0];
				var thisNum = myCards[i].split("_")[1];

				if (thisCard.toUpperCase() == card.type.toUpperCase() && thisNum.toUpperCase() == card.number.toUpperCase()) {
					myAvailCards.push(i);
				} else if (card.number.toUpperCase() == "ANY" && thisCard.toUpperCase() == card.type.toUpperCase()) {
					myAvailCards.push(i);
				} else if (card.number.toUpperCase() == "ANY" && card.type.toUpperCase() == "ANY") {
					myAvailCards.push(i);
				}

			}

			if (myAvailCards.length == 0) {
				for (var i = 0; i < myCards.length; i++) {
					myAvailCards.push(i);
				}
			}

			var idx;
			if (myAvailCards.length == 1) {
				idx = 0;
			} else {
				idx = Math.floor(Math.random() * myAvailCards.length);
			}


			var cardNumStr = myCards[myAvailCards[idx]];

			var toCard = cardNumStr.split("_")[0];
			var toNum = cardNumStr.split("_")[1];

			myCards.splice(myAvailCards[idx], 1);
			console.log(myCards);
			await cardsRef.transaction(function (post) {
				return myCards;
			});


			await new Promise((resolve, reject) => {
				setTimeout(async function () {


					await moveRef.child("movedPlayer").set({
						"player": player,
						"card": {
							"type": toCard,
							"number": toNum
						}
					}).then(resolve, reject);
				}, 2000);
			});


			return null;

		}

		return null;
	});





//only for bots
function findPassingCards(cardArr) {
	var res = [];
	for (var i = 0; i < 3; i++) {
		var idx = Math.floor(Math.random() * cardArr.length);
		res.push(cardArr[idx]);
		cardArr.splice(idx, 1);
	}
	return res;
}