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

exports.deleteGroupCascade = functions
    .database
    .ref('/groups/{groupId}')
    .onDelete((snapshot, context) => {
	const groupId = context.params.groupId;
	return snapshot
	    .ref
	    .root
	    .child(`group_progress/${groupId}`)
	    .remove();
    });

exports.removeGroupMemberCascade = functions
    .database
    .ref('/groups/{groupId}/members/{memberId}')
    .onDelete((snapshot, context) => {
	const memberId = context.params.memberId;
	const groupId = context.params.groupId;
	return snapshot
	      .ref
	      .root
	      .child(`users/${memberId}/groups/${groupId}`)
	      .remove();
    });

exports.copyUserProgressToGroupProgress = functions
    .database
    .ref('/groups/{groupId}/members/{memberId}')
    .onCreate((snapshot, context) => {
	const memberId = context.params.memberId;
	const groupId = context.params.groupId;
	const dateStr = getDateStr();
	const root = snapshot.ref.root;

	const getGroupGoalIds = root
	      .child(`groups/${groupId}/metadata/goals`)
	      .once('value')
	      .then((goals) => {
		  if (goals.exists()) {
		      return Object.keys(goals.val());
		  }
		  return [];
	      });
	const getUserProgress = root
	      .child(`user_progress/${memberId}/${dateStr}`)
	      .once('value')
	      .then((userProgress) => {
		  if (userProgress.exists()) {
		      return userProgress.val();
		  }
		  return {};
	      });

	function addNewUserGroupProgress(goalId, amount) {
	    return root
		.child(`group_progress/${groupId}/${dateStr}/${goalId}/${memberId}/amount`)
		.set(amount);
	}
	
	return Promise.all([getUserProgress, getGroupGoalIds])
	    .then((values) => {
		const [userProgress, groupGoalIds] = values;
		const updateGroupProgressTasks = [];
		
		for (const userGoalId in userProgress) {
		    if (!groupGoalIds.includes(userGoalId)) {
			continue;
		    }
		    const userProgressAmount = userProgress[groupGoalIds].amount;
		    updateGroupProgressTasks.push(
			addNewUserGroupProgress(userGoalId, userProgressAmount));
		}
		
		return Promise.all(updateGroupProgressTasks);
	    });
    });


function getDateStr() {
    return new Date().toISOString().slice(0, 10).replace(/-/g,'');
}
