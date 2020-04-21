const functions = require('firebase-functions');

// // Create and Deploy Your First Cloud Functions
// // https://firebase.google.com/docs/functions/write-firebase-functions
//
// exports.helloWorld = functions.https.onRequest((request, response) => {
//  response.send("Hello from Firebase!");
// });


// The Firebase Admin SDK to access the Firebase Realtime Database.
const admin = require('firebase-admin');
admin.initializeApp();

exports.deleteGroupCascade = functions.database.ref('/groups/{groupId}')
    .onDelete((snapshot, context) => {
	const memberIds = snapshot.child('members').val().keys();
	console.log(memberIds);
	const groupId = context.params.groupId;
	console.log(groupId);
    });

exports.removeGroupMemberCascade = functions.database.ref('/groups/{groupId}/members/{memberId}')
    .onDelete((snapshot, context) => {
	const memberId = context.params.memberId;
	const groupId = context.params.groupId;
	return snapshot
	    .ref
	    .root
	    .child(`users/${memberId}/groups/${groupId}`)
	    .remove();
    });
