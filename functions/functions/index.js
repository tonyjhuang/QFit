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


exports.addGroupMemberCascade = functions
    .database
    .ref('/groups/{groupId}/members/{memberId}')
    .onCreate((snapshot, context) => {
	const memberId = context.params.memberId;
	const groupId = context.params.groupId;
	return snapshot
	      .ref
	      .root
	      .child(`users/${memberId}/groups/${groupId}`)
	      .set(true);
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

	return Promise.all([getUserProgress, getGroupGoalIds])
	    .then((values) => {
		const [userProgress, groupGoalIds] = values;
		const updateGroupProgressTasks = [];

		for (const userGoalId in userProgress) {
		    if (!groupGoalIds.includes(userGoalId)) {
			delete userProgress[userGoalId];
		    }
		}
		return addUserProgress(root, memberId, groupId, userProgress);
	    });
    });

function addUserProgress(root, userId, groupId, userProgress) {
    function addNewUserGroupProgress(goalId, amount) {
	return root
	    .child(`group_progress/${groupId}/${getDateStr()}/${goalId}/${userId}/amount`)
	    .set(amount);
    }

    const updateGroupProgressTasks = [];
    for (const goalId in userProgress) {
	const userProgressAmount = userProgress[goalId].amount;
	updateGroupProgressTasks.push(
	    addNewUserGroupProgress(goalId, userProgressAmount));
    }
    
    return Promise.all(updateGroupProgressTasks);
}


exports.propagateUserProgress = functions
    .database
    .ref('/user_progress/{userId}/{dateStr}/{goalId}/amount')
    .onUpdate((change, context) => {
	const userId = context.params.userId;
	const dateStr = context.params.dateStr;
	const goalId = context.params.goalId;
	const root = change.after.ref.root;
	const amount = change.after.val();

	function doesGroupHaveGoal(groupId) {
	    return root
		.child(`groups/${groupId}/metadata/goals/${goalId}`)
		.once('value')
		.then((goals) => {
		    if (goals.exists()) {
			return [groupId, true];
		    }
		    return [groupId, false];
		});
	}

	function updateGroupGoalProgress(groupId) {
	    const path = `group_progress/${groupId}/${dateStr}/${goalId}/${userId}/amount`;
	    return root
		.child(path)
		.set(amount);
	}

	return root
	      .child(`users/${userId}/groups`)
	      .once('value')
	      .then((userGroups) => {
		  if (userGroups.exists()) {
		      return Object.keys(userGroups.val());		      
		  }
		  return [];
	      })
	      .then((groupIds) => {
		  const checkGoalInclusionTasks = [];
		  groupIds.forEach(groupId => {
		      checkGoalInclusionTasks.push(doesGroupHaveGoal(groupId));
		  });
		  return Promise.all(checkGoalInclusionTasks);
	      })
	      .then((groupUpdateEligibility) => {
		  const updateGroupProgressTasks = [];
		  groupUpdateEligibility.forEach(eligibility => {
		      const [groupId, isEligible] = eligibility;
		      if (!isEligible) {
			  return;
		      }
		      updateGroupProgressTasks.push(updateGroupGoalProgress(groupId));
		  });
		  return Promise.all(updateGroupProgressTasks);
	      });
    });


function getDateStr() {
    return new Date().toISOString().slice(0, 10).replace(/-/g,'');
}
